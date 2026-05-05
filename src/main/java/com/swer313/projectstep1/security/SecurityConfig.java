package com.swer313.projectstep1.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swer313.projectstep1.errors.ApiError;
import java.time.Instant;
/**
 * ══════════════════════════════════════════════════════════════════
 * Security Rules — ملخص الصلاحيات:
 *
 * PUBLIC   (بدون token):
 *   POST /auth/register, POST /auth/login
 *   GET  /api/hotels/**  (تصفح الفنادق)
 *   GET  /api/rooms/**   (تصفح الغرف)
 *   GET  /api/availability/**
 *   GET  /api/amenities/**
 *   GET  /api/price-preview/**
 *   GET  /api/currency/**
 *   GET  /swagger-ui/**, /v3/api-docs/**
 *
 * GUEST (token لأي مستخدم مسجّل):
 *   POST /bookings              (إنشاء حجز)
 *   GET  /bookings/{id}         (تفاصيل حجز)
 *   GET  /bookings/my           (حجوزاتي)
 *   PATCH /bookings/{id}/cancel (إلغاء حجز)
 *   POST /api/payments/intents
 *   POST /api/payments/{id}/simulate-*
 *   GET  /api/payments/{id}
 *   POST /api/reviews/**
 *   GET  /api/reviews/**
 *   POST /api/waiting-list/**
 *   GET  /api/waiting-list/**
 *
 * MANAGER | ADMIN:
 *   PATCH /bookings/{id}/confirm|complete
 *   GET   /bookings/hotels/**
 *   GET   /bookings/upcoming
 *   POST|PUT|DELETE /admin/hotels/**
 *   POST|PUT|DELETE /admin/rooms/**
 *   POST|PUT|DELETE /api/pricing-rules/**
 *
 * ADMIN only:
 *   GET /bookings             (كل الحجوزات)
 *   GET /admin/reports/**
 *   GET /api/payments (قائمة كاملة)
 * ══════════════════════════════════════════════════════════════════
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // يتيح @PreAuthorize على مستوى الـ method
public class SecurityConfig {

    private final JwtAuthFilter          jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter    = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    // ── Filter Chain ──────────────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                // API مش بتستخدم CSRF (stateless JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless — مفيش sessions
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 401 handler موحّد
                .exceptionHandling(ex -> ex

                        // 401 Unauthorized
                        .authenticationEntryPoint((req, res, e) -> {

                            ApiError error = new ApiError(
                                    Instant.now().toString(),
                                    401,
                                    "Unauthorized",
                                    "Missing or invalid JWT token",
                                    req.getRequestURI()
                            );

                            res.setStatus(401);
                            res.setContentType("application/json");

                            ObjectMapper mapper = new ObjectMapper();
                            res.getWriter().write(mapper.writeValueAsString(error));
                        })

                        // 403 Forbidden
                        .accessDeniedHandler((req, res, e) -> {

                            ApiError error = new ApiError(
                                    Instant.now().toString(),
                                    403,
                                    "Forbidden",
                                    "You don't have permission for this action",
                                    req.getRequestURI()
                            );

                            res.setStatus(403);
                            res.setContentType("application/json");

                            ObjectMapper mapper = new ObjectMapper();
                            res.getWriter().write(mapper.writeValueAsString(error));
                        })
                )
                .authorizeHttpRequests(auth -> auth

                        // ===============================
                        // AUTH + SWAGGER (PUBLIC)
                        // ===============================
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        // ===============================
                        // PUBLIC READ (CATALOG)
                        // ===============================
                        .requestMatchers(HttpMethod.GET,
                                "/api/hotels",
                                "/api/hotels/*",
                                "/api/hotels/cities",
                                "/api/hotels/countries",
                                "/api/hotels/autocomplete",
                                "/api/hotels/*/room-types",
                                "/api/hotels/*/room-types/*",
                                "/api/hotels/*/room-types/meta/minimal",
                                "/api/hotels/*/room-types/suggest",
                                "/api/room-types",
                                "/api/room-types/*",
                                "/api/room-types/*/status",
                                "/api/amenities/**",
                                "/api/availability/**",
                                "/api/currencies/**",
                                "/pricing-rules/preview"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/api/reviews/**"
                        ).permitAll()

                        // ===============================
                        // ADMIN ONLY
                        // ===============================
                        .requestMatchers(HttpMethod.GET, "/api/bookings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/upcoming").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/payments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/payments/stats").hasRole("ADMIN")
                        .requestMatchers("/api/admin/reports/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST,   "/api/amenities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/amenities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/amenities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/amenities/**").hasRole("ADMIN")

                        // ===============================
                        // MANAGER + ADMIN
                        // ===============================
                        .requestMatchers("/api/admin/hotels/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers("/api/admin/room-types/**").hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers(HttpMethod.POST,   "/api/hotels/*/room-types/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/hotels/*/room-types/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/hotels/*/room-types/**").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/hotels/*/room-types/**").hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers(HttpMethod.POST,   "/pricing-rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/pricing-rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/pricing-rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/pricing-rules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/pricing-rules", "/pricing-rules/*", "/pricing-rules/active").hasRole("ADMIN")


                        .requestMatchers(HttpMethod.PATCH,
                                "/api/bookings/*/confirm",
                                "/api/bookings/*/complete"
                        ).hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/bookings/hotels/*",
                                "/api/bookings/hotels/*/upcoming"
                        ).hasAnyRole("MANAGER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("GUEST")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/my").hasRole("GUEST")
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/cancel").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/bookings/*").authenticated()

                        // ===============================
                        // DEFAULT
                        // ===============================
                        .anyRequest().authenticated()
                )
                // أضف الـ JWT filter قبل UsernamePasswordAuthenticationFilter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ── Beans ─────────────────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}