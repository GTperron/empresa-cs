package com.empresa.inventario.repository;

import com.empresa.inventario.entity.RefreshToken;
import com.empresa.inventario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio para gestionar operaciones CRUD de refresh tokens.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    
    void deleteByUsuario(Usuario usuario);
}
