package com.empresa.inventario.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Componente responsable de generar y validar JWT (JSON Web Tokens).
 */
@Component
@Slf4j
public class JwtProvider {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Genera un JWT a partir de la autenticación del usuario.
     */
    public String generarToken(Authentication authentication) {
        UsuarioUserDetails userPrincipal = (UsuarioUserDetails) authentication.getPrincipal();

        String roles = userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("id", userPrincipal.getId())
                .claim("roles", roles)
                .claim("nombre", userPrincipal.getNombre())
                .claim("apellido", userPrincipal.getApellido())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Genera un JWT a partir del email del usuario.
     */
    public String generarTokenPorEmail(String email) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtiene el nombre de usuario (email) del JWT.
     */
    public String getEmailDelToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /**
     * Obtiene el ID del usuario del JWT.
     */
    public Long getIdDelToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("id", Long.class);
    }

    /**
     * Valida un JWT.
     */
    public Boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.error("JWT token inválido: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT token vacío: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene la clave de firma para el JWT.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Obtiene el tiempo de expiración del token en segundos.
     */
    public Long getTiempoExpiracion() {
        return jwtExpirationMs / 1000;
    }
}