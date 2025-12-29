package com.myapp.complaints.config;

import com.myapp.complaints.DAO.RefreshTokenRepo;
import com.myapp.complaints.config.jwtAuth.JwtAccessTokenFilter;
import com.myapp.complaints.config.jwtAuth.JwtRefreshTokenFilter;
import com.myapp.complaints.config.jwtAuth.JwtTokenUtils;
import com.myapp.complaints.config.user.UserInfoManagerConfig;
import com.myapp.complaints.service.LogoutHandlerService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final RSAKeyRecord rsaKeyRecord;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserInfoManagerConfig userInfoManagerConfig;
    private final RefreshTokenRepo refreshTokenRepo;
    private final LogoutHandlerService logoutHandlerService;

    @Order(1)
    @Bean
    public SecurityFilterChain publicAuthChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/auth/sign-in/**",
                        "/auth/sign-up/**",
                        "/auth/verify",
                        "/auth/forgot-password/**",
                        "/auth/reset-password/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .userDetailsService(userInfoManagerConfig)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults())
                .build();
    }

@Order(2)
@Bean
public SecurityFilterChain refreshTokenChain(HttpSecurity http,
                                             JwtRefreshTokenFilter refreshFilter) throws Exception {
    return http
            .securityMatcher("/auth/refresh-token/**")
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(refreshFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> {
                ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint());
                ex.accessDeniedHandler(new BearerTokenAccessDeniedHandler());
            })
            .build();
}


    @Order(3)
    @Bean
    public SecurityFilterChain apiChain(HttpSecurity http,
                                        JwtAccessTokenFilter accessFilter) throws Exception {

        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

// TODO                       .requestMatchers("/api/employee/**").hasAnyRole("موظف الاستقبال","مدير")
//                        .requestMatchers("/api/citizen/**").hasRole("مواطن")

                        .requestMatchers(HttpMethod.GET, "/api/complaints/**").permitAll() // public GET
                                .requestMatchers(HttpMethod.GET,"/api/accounts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/complaints/**").authenticated() // must be logged in to add complaint
//                        .requestMatchers("/auth/change-password/**"
//                                , "/auth/logout/**"
//                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(accessFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()))
                .build();
    }

    @Order(4)
    @Bean
    public SecurityFilterChain logoutChain(HttpSecurity http,
                                           JwtRefreshTokenFilter refreshFilter) throws Exception {
        return http
                .securityMatcher("/auth/logout/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(refreshFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandlerService)
                        .logoutSuccessHandler((request, response, authentication) -> {})
                )
                .exceptionHandling(ex -> {
                    ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint());
                    ex.accessDeniedHandler(new BearerTokenAccessDeniedHandler());
                })
                .build();
    }

    @Order(6)
    @Bean
    public SecurityFilterChain changeChain(HttpSecurity http,
                                        JwtAccessTokenFilter accessFilter) throws Exception {

        return http
                .securityMatcher("/auth/change-password/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(accessFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()))
                .build();
    }
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKeyRecord rsaKeyRecord) {
        return NimbusJwtDecoder
                .withPublicKey(rsaKeyRecord.rsaPublicKey())
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKeyRecord rsaKeyRecord) {
        JWK jwk = new RSAKey.Builder(rsaKeyRecord.rsaPublicKey())
                .privateKey(rsaKeyRecord.rsaPrivateKey())
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userInfoManagerConfig);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new BearerTokenAuthenticationEntryPoint();
    }

//    @Bean // This bean defines your CORS rules for Spring Security
//    CorsConfigurationSource corsConfigurationSource() { // TODO do this for all beans !!
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Your frontend URL
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
//        configuration.setAllowCredentials(true); // Allow credentials (cookies, auth headers)
//        configuration.setMaxAge(3600L); // Max age for preflight cache
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration); // Apply this CORS config to all paths
//        return source;
//    }
//

////
////    @Bean
////    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authRequestRepository() {
////        return new HttpSessionOAuth2AuthorizationRequestRepository();
////    }
////    @Bean
////    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
////        return new DefaultAuthorizationCodeTokenResponseClient();
////    }
////    @Bean
////    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
////        return new CustomOidcUserService(); // Your custom implementation
////    }
//
//
}