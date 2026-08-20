package com.empresa.inventario.config;

import com.empresa.inventario.dto.ApiResponse;
import com.empresa.inventario.security.JwtAuthenticationFilter;
import com.empresa.inventario.security.JwtProvider;
import com.empresa.inventario.security.UsuarioUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Configuración de Spring Security para autenticación JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final UsuarioUserDetailsService usuarioUserDetailsService;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    /**
     * Codificador de contraseñas usando BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Proveedor de autenticación DAO.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Gestor de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Filtro JWT personalizado.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProvider, usuarioUserDetailsService);
    }

    /**
     * Configuración CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Punto de entrada para peticiones NO autenticadas (token ausente, inválido o expirado).
     * Devuelve 401 con el wrapper ApiResponse en JSON, para que el frontend dispare el
     * refresh automático y pueda leer el mensaje.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                escribirError(response, HttpStatus.UNAUTHORIZED,
                        "No autenticado: el token está ausente, es inválido o expiró");
    }

    /**
     * Handler para peticiones autenticadas pero SIN permisos suficientes (rol insuficiente).
     * Devuelve 403 con ApiResponse en JSON.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                escribirError(response, HttpStatus.FORBIDDEN,
                        "Acceso denegado: no tenés permisos para realizar esta operación");
    }

    /**
     * Escribe una respuesta de error uniforme (ApiResponse) en JSON.
     */
    private void escribirError(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> cuerpo = ApiResponse.<Void>builder()
                .exitoso(false)
                .mensaje(mensaje)
                .timestamp(LocalDateTime.now())
                .build();
        objectMapper.writeValue(response.getWriter(), cuerpo);
    }

    /**
     * Configuración de la cadena de filtros de seguridad HTTP.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos (sin prefijo /api: el context-path ya lo aplica)
                        .requestMatchers("/auth/**").permitAll()
                        
                        // Swagger/OpenAPI
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        
                        // Endpoints protegidos
                        .requestMatchers(HttpMethod.GET, "/usuarios/perfil").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/usuarios/perfil").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/usuarios/cambiar-contrasena").authenticated()
                        
                        // Admin only
                        .requestMatchers(HttpMethod.GET, "/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/**").hasRole("ADMIN")
                        
                        // Resto de endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
