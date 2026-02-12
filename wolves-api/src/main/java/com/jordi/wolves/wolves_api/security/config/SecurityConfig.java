package com.jordi.wolves.wolves_api.security.config;

import com.jordi.wolves.wolves_api.security.jwt.JwtAuthenticationFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.List;

import io.swagger.v3.oas.models.security.SecurityRequirement;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;


    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Publico
                        .requestMatchers(HttpMethod.POST, "/players").hasRole("ADMIN")
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Ranking y Perfil (Cualquier )
                        .requestMatchers("/players/ranking").authenticated()
                        .requestMatchers(HttpMethod.GET, "/players/{id}").hasRole("ADMIN")

                        // Solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/players").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/players/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/questions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/questions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/players/**").hasRole("ADMIN")
                        .requestMatchers("/me/**").hasAnyRole("USER", "ADMIN")

                        // Juego (USER y ADMIN)
                        //.requestMatchers(HttpMethod.PATCH, "/me/notes")
                        //.hasAnyRole("USER", "ADMIN")

                        .requestMatchers("/game/**").hasAnyRole("USER", "ADMIN")

                        // Cualquier otra cosa
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("JavaInUseSecurityScheme"))
                .components(new Components().addSecuritySchemes("JavaInUseSecurityScheme", new SecurityScheme()
                        .name("JavaInUseSecurityScheme")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://wolves-game-front.onrender.com"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE","PATCH", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}