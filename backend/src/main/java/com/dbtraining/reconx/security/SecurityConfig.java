package com.dbtraining.reconx.security;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;




/**
 * ============================================================================
 * SecurityConfig — TICKET-ADV073 + TICKET-ADV074
 * ============================================================================
 * WHAT:    Spring Security filter chain. Production target: stateless JWT
 *          auth + method-level RBAC across ADMIN / TRADER / VIEWER /
 *          RECON_ANALYST roles.
 * HOW:     One SecurityFilterChain @Bean + PasswordEncoder @Bean +
 *          @EnableMethodSecurity. The JwtAuthenticationFilter is registered
 *          before UsernamePasswordAuthenticationFilter.
 * WHY:     Day 6 needs role-based protection on every endpoint, and the
 *          frontend uses bearer tokens issued at /auth/login.
 * OBSERVE: After Day-6 work is wired, GET /api/v1/trades without a token -> 401.
 * ============================================================================
 *
 *  TEMP OVERRIDE (below): all role-based rules are bypassed with
 *  `.anyRequest().permitAll()` so the current frontend can call every
 *  endpoint without sending a JWT. This is intentional and reversible —
 *  the full RBAC block is preserved below, commented out, so it can be
 *  restored by swapping the two blocks back. REMEMBER: TICKET-ADV073/074
 *  and the Day-6 "no token -> 401" check will fail until this is reverted.
 *
 *  TO RESTORE FULL RBAC: delete the TEMP block and uncomment the ORIGINAL
 *  block below it.
 *
 *  CORS: the frontend is served separately (Python http.server on :5500)
 *  from the API (:8080), so the browser enforces CORS on every fetch().
 *  corsConfigurationSource() below allows the dev frontend origins; without
 *  it, requests would be blocked by the browser even with permitAll().
 * ============================================================================
 */
@Configuration
public class SecurityConfig {




    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5500", "http://127.0.0.1:5500"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }




    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {}) // picks up the corsConfigurationSource bean above
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ---- TEMP: everything open for frontend dev ----
                .anyRequest().permitAll()


                // ---- ORIGINAL (restore this, delete the line above, when re-enabling RBAC) ----
                // .requestMatchers(
                //         "/auth/login",
                //         "/actuator/health/**",
                //         "/actuator/info",
                //         "/actuator/prometheus",
                //         "/swagger-ui.html",
                //         "/swagger-ui/**",
                //         "/v3/api-docs/**",
                //         "/h2/**"
                // ).permitAll()
                // .requestMatchers(HttpMethod.GET,    "/v1/trades/**").hasAnyRole("VIEWER","TRADER","RECON_ANALYST","ADMIN")
                // .requestMatchers(HttpMethod.POST,   "/v1/trades").hasAnyRole("TRADER","ADMIN")
                // .requestMatchers(HttpMethod.PUT,    "/v1/trades/**").hasAnyRole("TRADER","ADMIN")
                // .requestMatchers(HttpMethod.PATCH,  "/v1/trades/**").hasAnyRole("TRADER","ADMIN")
                // .requestMatchers(HttpMethod.DELETE, "/v1/trades/**").hasRole("ADMIN")
                // .requestMatchers("/v1/recon/**").hasAnyRole("RECON_ANALYST","ADMIN")
                // .requestMatchers("/v1/audit/**").hasAnyRole("RECON_ANALYST","ADMIN")
                // .anyRequest().authenticated()
            )
            .headers(h -> h.frameOptions(f -> f.disable()))   // for /h2 dev console
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}


