package com.empresa.inventario.service;

import com.empresa.inventario.dto.*;
import com.empresa.inventario.entity.RefreshToken;
import com.empresa.inventario.entity.Rol;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.exception.AutenticacionFallidaException;
import com.empresa.inventario.exception.RecursoNoEncontradoException;
import com.empresa.inventario.exception.RecursoYaExisteException;
import com.empresa.inventario.exception.TokenInvalidoException;
import com.empresa.inventario.repository.RefreshTokenRepository;
import com.empresa.inventario.repository.RolRepository;
import com.empresa.inventario.repository.UsuarioRepository;
import com.empresa.inventario.security.JwtProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio para gestionar autenticación y autorización.
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class AutenticacionService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UsuarioService usuarioService;

    /**
     * Registra un nuevo usuario.
     */
    public AuthResponse registrar(RegistroRequest request) {
        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getPasswordConfirmacion())) {
            throw new AutenticacionFallidaException("Las contraseñas no coinciden");
        }

        // Validar que el email no esté registrado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RecursoYaExisteException("El email " + request.getEmail() + " ya está registrado");
        }

        // Obtener rol de usuario por defecto
        Rol rolUsuario = rolRepository.findByNombre("USER")
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol USER no encontrado"));

        // Crear nuevo usuario
        Set<Rol> roles = new HashSet<>();
        roles.add(rolUsuario);

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .activo(true)
                .roles(roles)
                .build();

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Nuevo usuario registrado: {}", usuarioGuardado.getEmail());

        // Generar JWT
        String accessToken = jwtProvider.generarTokenPorEmail(usuarioGuardado.getEmail());
        String refreshToken = generarYGuardarRefreshToken(usuarioGuardado);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProvider.getTiempoExpiracion())
                .usuario(usuarioService.convertirADTO(usuarioGuardado))
                .build();
    }

    /**
     * Realiza el login del usuario.
     */
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String accessToken = jwtProvider.generarToken(authentication);
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AutenticacionFallidaException("Usuario no encontrado"));

            // Actualizar último login
            usuario.setUltimoLogin(LocalDateTime.now());
            usuarioRepository.save(usuario);
            log.info("Login exitoso para usuario: {}", usuario.getEmail());

            // Generar refresh token
            String refreshToken = generarYGuardarRefreshToken(usuario);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtProvider.getTiempoExpiracion())
                    .usuario(usuarioService.convertirADTO(usuario))
                    .build();

        } catch (Exception ex) {
            log.warn("Error en login para usuario: {}", request.getEmail());
            throw new AutenticacionFallidaException("Email o contraseña inválidos");
        }
    }

    /**
     * Refresca el access token usando el refresh token.
     */
    public AuthResponse refrescarToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenInvalidoException("Refresh token inválido"));

        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenInvalidoException("Refresh token expirado o revocado");
        }

        Usuario usuario = refreshToken.getUsuario();
        String nuevoAccessToken = jwtProvider.generarTokenPorEmail(usuario.getEmail());

        log.debug("Token refrescado para usuario: {}", usuario.getEmail());

        return AuthResponse.builder()
                .accessToken(nuevoAccessToken)
                .refreshToken(request.getRefreshToken())
                .expiresIn(jwtProvider.getTiempoExpiracion())
                .usuario(usuarioService.convertirADTO(usuario))
                .build();
    }

    /**
     * Genera y guarda un refresh token.
     */
    private String generarYGuardarRefreshToken(Usuario usuario) {
        // Limpiar tokens anteriores (opcional, para no acumular)
        refreshTokenRepository.deleteByUsuario(usuario);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiracion = LocalDateTime.now().plusDays(30);

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(token)
                .expiracion(expiracion)
                .revocado(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    /**
     * Cambia la contraseña del usuario.
     */
    public void cambiarContrasena(Long usuarioId, CambiarContrasenaRequest request) {
        if (!request.getNuevaContrasena().equals(request.getConfirmacion())) {
            throw new AutenticacionFallidaException("Las nuevas contraseñas no coinciden");
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getPasswordHash())) {
            throw new AutenticacionFallidaException("Contraseña actual inválida");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuario);

        // Revocar todos los refresh tokens
        refreshTokenRepository.deleteByUsuario(usuario);

        log.info("Contraseña cambiada para usuario: {}", usuarioId);
    }

    /**
     * Revoca un refresh token.
     */
    public void revocarRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token no encontrado"));

        refreshToken.setRevocado(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token revocado para usuario: {}", refreshToken.getUsuario().getId());
    }

    /**
     * Realiza logout revocando el refresh token.
     */
    public void logout(String refreshToken) {
        revocarRefreshToken(refreshToken);
        log.info("Logout exitoso");
    }
}
