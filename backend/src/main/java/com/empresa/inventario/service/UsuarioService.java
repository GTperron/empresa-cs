package com.empresa.inventario.service;

import com.empresa.inventario.dto.UsuarioDTO;
import com.empresa.inventario.dto.EditarPerfilRequest;
import com.empresa.inventario.entity.Usuario;
import com.empresa.inventario.exception.RecursoNoEncontradoException;
import com.empresa.inventario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar operaciones relacionadas con usuarios.
 */
@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene el perfil del usuario actual.
     */
    public UsuarioDTO obtenerPerfil(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        return convertirADTO(usuario);
    }

    /**
     * Obtiene un usuario por ID.
     */
    public Usuario obtenerUsuarioPorId(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario con ID " + usuarioId + " no encontrado"));
    }

    /**
     * Edita el perfil del usuario actual.
     */
    public UsuarioDTO editarPerfil(Long usuarioId, EditarPerfilRequest request) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Perfil actualizado para usuario: {}", usuarioId);

        return convertirADTO(usuarioActualizado);
    }

    /**
     * Convierte una entidad Usuario a DTO.
     */
    public UsuarioDTO convertirADTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .activo(usuario.getActivo())
                .ultimoLogin(usuario.getUltimoLogin())
                .roles(usuario.getRoles()
                        .stream()
                        .map(rol -> rol.getNombre())
                        .collect(Collectors.toSet())
                )
                .createdAt(usuario.getCreatedAt())
                .updatedAt(usuario.getUpdatedAt())
                .build();
    }
}
