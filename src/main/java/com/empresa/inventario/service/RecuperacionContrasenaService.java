package com.empresa.inventario.service;

import com.empresa.inventario.dto.SolicitarRecuperacionRequest;
import com.empresa.inventario.dto.RestablecerContrasenaRequest;
import com.empresa.inventario.entity.PasswordResetToken;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.exception.RecursoNoEncontradoException;
import com.empresa.inventario.exception.TokenInvalidoException;
import com.empresa.inventario.exception.AutenticacionFallidaException;
import com.empresa.inventario.repository.PasswordResetTokenRepository;
import com.empresa.inventario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio para gestionar recuperación de contraseña.
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class RecuperacionContrasenaService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Solicita recuperación de contraseña.
     * En producción, aquí se enviaría el email con el token.
     */
    public void solicitarRecuperacion(SolicitarRecuperacionRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró usuario con email: " + request.getEmail()));

        // Generar token de recuperación
        String token = UUID.randomUUID().toString();
        LocalDateTime expiracion = LocalDateTime.now().plusHours(1);

        // Limpiar tokens anteriores
        passwordResetTokenRepository.deleteByUsuario(usuario);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .usuario(usuario)
                .token(token)
                .expiracion(expiracion)
                .utilizado(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // TODO: Enviar email con el token
        // mailService.enviarCorreoRecuperacion(usuario.getEmail(), token);

        log.info("Token de recuperación de contraseña generado para: {}", usuario.getEmail());
    }

    /**
     * Restablece la contraseña usando el token.
     */
    public void restablecerContrasena(RestablecerContrasenaRequest request) {
        if (!request.getNuevaContrasena().equals(request.getConfirmacion())) {
            throw new AutenticacionFallidaException("Las contraseñas no coinciden");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new TokenInvalidoException("Token de recuperación inválido"));

        if (!resetToken.isValid()) {
            throw new TokenInvalidoException("Token de recuperación ha expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuario);

        resetToken.setUtilizado(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Contraseña restablecida para usuario: {}", usuario.getEmail());
    }
}