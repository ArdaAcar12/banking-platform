package com.bank.banking_platform.auth.security;

import java.util.List;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bank.banking_platform.auth.service.RateLimiterService;

/**
 * Spring Security yapılandırması.
 * <p>
 * Stateless, JWT tabanlı erişim kontrolü sağlar. Eski {@code WebSecurityConfigurerAdapter}
 * tabanlı yapılandırma kullanılmamıştır.
 * <p>
 * Geliştirme profili (dev) içinde Swagger/OpenAPI endpoint'leri kimlik doğrulamasızdır.
 * Production'da bu endpoint'ler kimlik doğrulaması gerektirir.
 * <p>
 * Note: RateLimiterService ve RateLimitFilter devre dışı_bırakıldı; Redis yapılandırması
 * tamamlandığında yeniden eklenebilir. Bu sprintte Redis/Rate Limiting geliştirilmiyor.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAccessDeniedHandler accessDeniedHandler,
                          UserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Ana SecurityFilterChain zinciri.
     * Geliştirme profili (dev) içinde Swagger/OpenAPI endpoint'leri kimlik doğrulamasızdır.
     * Production'da tüm istekler kimlik doğrulaması gerektirir.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, Environment env) throws Exception {
        boolean isDev = env.acceptsProfiles(org.springframework.core.env.Profiles.of("dev"));

        http
            // Stateless: oturum oluşturulmaz, her istek JWT ile doğrulanır.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF, stateless JWT akışında gerekli değildir.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Rate limiting devre dışı_bırakıldı; Redis yapılandırması tamamlandığında yeniden etkinleştirilebilir.
            // .addFilterBefore(rateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Public endpoint'ler
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
                // Kimliği doğrulanmış kullanıcıya özel endpoint'ler
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                // Admin endpoint'leri yalnızca ADMIN rolüne açıktır.
                .requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasRole("ADMIN")
                // Swagger/OpenAPI - yalnızca geliştirme profili için herkese açıkktır.
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .access(isDev
                        ? new WebExpressionAuthorizationManager("permitAll")
                        : new WebExpressionAuthorizationManager("isAuthenticated()"))
                // Diğer tüm istekler kimlik doğrulaması gerektirir.
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Development: localhost origins
        // Production: environment variable ile yapılandırılacak (${CORS_ALLOWED_ORIGINS})
        // Wildcard kullanımı production'da yasak, sadece development'de localhost'lar enabled.
        String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            // Production: environment variable'den ayrılmış origin'ler
            String[] origins = allowedOrigins.split(",");
            configuration.setAllowedOriginPatterns(java.util.Arrays.asList(origins));
        } else {
            // Development default: localhost'lar
            configuration.setAllowedOriginPatterns(
                java.util.Arrays.asList("http://localhost:3000", "http://localhost:4200")
            );
        }

        // Credentials true olmalı JWT auth için, bu yüzden wildcard origin yasaklandığı
        // için specific origin'ler kullanıyoruz.
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(List.of(HttpMethod.GET.name(), HttpMethod.POST.name(),
            HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}