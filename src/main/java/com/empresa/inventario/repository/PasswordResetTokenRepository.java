package com.empresa.inventario.repository;

import com.empresa.inventario.entity.PasswordResetToken;
import com.empresa.inventario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio para gestionar operaciones CRUD de tokens de recuperación de contraseña.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
    
    void deleteByUsuario(Usuario usuario);
}
