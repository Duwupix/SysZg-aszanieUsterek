package com.usterki.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Zasoby publiczne
                .requestMatchers("/", "/panel.html", "/*.html", "/favicon.ico").permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                // Użytkownicy — lista techników dostępna dla zalogowanych, reszta tylko dla admina
                .requestMatchers("/api/uzytkownicy/technicy").authenticated()
                .requestMatchers("/api/uzytkownicy/**").hasRole("ADMINISTRATOR")

                // Przypisania — ważna kolejność (bardziej szczegółowe reguły pierwsze)
                // Technik może zakończyć swoje zlecenie
                .requestMatchers(HttpMethod.PUT, "/api/przypisania/*/zakoncz").hasAnyRole("TECHNIK", "ADMINISTRATOR")
                // Anulowanie — tylko administrator
                .requestMatchers(HttpMethod.PUT, "/api/przypisania/*/anuluj").hasRole("ADMINISTRATOR")
                // Ręczne przypisanie przez admina (POST na /api/przypisania/{id})
                .requestMatchers(HttpMethod.POST, "/api/przypisania/**").hasRole("ADMINISTRATOR")

                // Zgłoszenia — technik lub admin może „przyjąć" zgłoszenie
                .requestMatchers(HttpMethod.POST, "/api/zgloszenia/*/przyjmij").hasAnyRole("TECHNIK", "ADMINISTRATOR")

                // Pozostałe żądania wymagają zalogowania
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write(mapper.writeValueAsString(Map.of("error", "Nie zalogowano")));
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write(mapper.writeValueAsString(Map.of("error", "Brak uprawnień")));
                })
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
