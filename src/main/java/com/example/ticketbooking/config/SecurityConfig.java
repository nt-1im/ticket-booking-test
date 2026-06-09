package com.example.ticketbooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Lop cau hinh bao mat Spring Security cho ung dung.
 * Quan ly phan quyen truy cap endpoint, quy trinh dang nhap/dang xuat va ma hoa mat khau.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Bean dinh nghia thuat toan ma hoa mat khau.
     * Su dung BCryptPasswordEncoder de bam mat khau cua nguoi dung mot cach an toan.
     *
     * @return Doi tuong PasswordEncoder su dung giai thuat BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cau hinh bo loc bao mat (Security Filter Chain) de ap dung phan quyen va quy trinh xac thuc.
     *
     * @param http Doi tuong cau hinh bao mat HttpSecurity cua Spring Security
     * @return Bo loc SecurityFilterChain da duoc xay dung hoan tat
     * @throws Exception Cac ngoai le xay ra trong qua trinh thiet lap bao mat
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Cau hinh phan quyen yeu cau HTTP
            .authorizeHttpRequests(auth -> auth
                // Cho phep tat ca truy cap tai nguyen tinh (static resources)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // Cho phep tat ca truy cap cac trang cong khai
                .requestMatchers("/", "/login", "/register", "/trains", "/trains/detail/*", "/error").permitAll()
                // Cho phep truy cap console co so du lieu H2 (phuc vu moi truong phat trien)
                .requestMatchers("/h2-console/**").permitAll()
                // Yeu cau vai tro ADMIN doi voi cac trang quan tri admin
                .requestMatchers("/dashboard/admin/**", "/admin/**").hasRole("ADMIN")
                // Yeu cau vai tro SELLER hoac ADMIN de quan ly chuyen tau cua nguoi ban
                .requestMatchers("/dashboard/seller/**", "/trains/new", "/trains/edit/**", "/trains/delete/**").hasAnyRole("SELLER", "ADMIN")
                // Yeu cau vai tro BUYER hoac ADMIN de dat ve va huy ve
                .requestMatchers("/dashboard/buyer/**", "/tickets/book/**", "/tickets/cancel/**").hasAnyRole("BUYER", "ADMIN")
                // Yeu cau xac thuc voi trang dashboard dieu phoi chung
                .requestMatchers("/dashboard").authenticated()
                // Tat ca cac yeu cau con lai deu can phai dang nhap
                .anyRequest().authenticated()
            )
            // Cau hinh trang dang nhap (Form Login)
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // Cau hinh quy trinh dang xuat (Logout)
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Bo qua kiem tra CSRF cho H2 Console de tien thao tac trong moi truong dev
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            // Cho phep hien thi frame tu cung nguon (Same Origin) de hien thi H2 Console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

    /**
     * Dinh nghia Handler xu ly khi nguoi dung xac thuc thanh cong.
     * Dieu huong nguoi dung ve trang Dashboard tuong ung dua theo vai tro (Role) cua ho.
     *
     * @return AuthenticationSuccessHandler xu ly dieu huong sau dang nhap
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            String redirectUrl = "/dashboard";
            
            for (var authority : authorities) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    redirectUrl = "/dashboard/admin";
                    break;
                } else if (authority.getAuthority().equals("ROLE_SELLER")) {
                    redirectUrl = "/dashboard/seller";
                    break;
                } else if (authority.getAuthority().equals("ROLE_BUYER")) {
                    redirectUrl = "/"; // Redirect buyers to home page to browse events
                    break;
                }
            }
            response.sendRedirect(redirectUrl);
        };
    }
}
