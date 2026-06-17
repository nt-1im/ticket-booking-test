package com.example.ticketbooking.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Lop cau hinh bao mat Spring Security cho ung dung.
 * Quan ly phan quyen truy cap endpoint, quy trinh dang nhap/dang xuat dung ca Session va Token (JWT).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Ham khoi dung SecurityConfig.
     *
     * @param jwtRequestFilter Bo loc xac thuc JWT
     */
    @Autowired
    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    /**
     * Bean dinh nghia thuat toan ma hoa mat khau.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean quan ly xac thuc de ho tro AuthController dang nhap thu cong.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Cau hinh bo loc bao mat (Security Filter Chain).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Cau hinh CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Vo hieu hoa CSRF de phuc vu kien truc tach biet frontend-backend dung REST API
                .csrf(csrf -> csrf.disable())
                // Cau hinh phan quyen yeu cau HTTP
                .authorizeHttpRequests(auth -> auth
                        // Cho phep tat ca truy cap tai nguyen tinh (static resources) va cac trang HTML front-end
                        .requestMatchers("/", "/index.html", "/login.html", "/register.html", "/train-detail.html", "/train-form.html",
                                "/dashboard-admin.html", "/dashboard-seller.html", "/dashboard-buyer.html",
                                "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        // Cho phep console co so du lieu H2
                        .requestMatchers("/h2-console/**").permitAll()
                        // Cho phep dang nhap/dang ky cong khai
                        .requestMatchers("/api/auth/login/**", "/api/auth/register", "/api/auth/logout", "/api/auth/me").permitAll()
                        // Cho phep xem/tim kiem chuyen tau cong khai
                        .requestMatchers("/api/trains", "/api/trains/*").permitAll()
                        // Chi co BUYER moi duoc dat ve va huy ve
                        .requestMatchers("/api/tickets/book/**", "/api/tickets/cancel/**", "/api/dashboard/buyer").hasRole("BUYER")
                        // Chi co SELLER va ADMIN moi duoc tao/sua/xoa chuyen tau va tuyen duong co dinh
                        .requestMatchers("/api/routes/**", "/api/trains/new", "/api/trains/edit/**", "/api/dashboard/seller").hasAnyRole("SELLER", "ADMIN")
                        // Chi co ADMIN moi duoc truy cap cac dashboard va endpoints quan tri he thong
                        .requestMatchers("/api/dashboard/admin/**", "/api/users/**").hasRole("ADMIN")
                        // Tat ca cac yeu cau con lai deu can phai dang nhap
                        .anyRequest().authenticated())
                // Bo qua X-Frame-Options cho H2 Console
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                // Cau hinh quan ly session: IF_REQUIRED de ho tro Session truyen thong va Stateless JWT cung luc
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // Cau hinh logout REST API
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/api/auth/logout"))
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(200);
                            response.getWriter().write("{\"message\": \"Logged out successfully\"}");
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));

        // Them bo loc JWT vao truoc UsernamePasswordAuthenticationFilter cua Spring Security
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cau hinh CORS cho phep cac origin khac nhau (Localhost dev) goi den API cua backend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
