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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Authorize requests
            .authorizeHttpRequests(auth -> auth
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // Public views
                .requestMatchers("/", "/login", "/register", "/trains", "/trains/detail/*").permitAll()
                // H2 Console (dev fallback)
                .requestMatchers("/h2-console/**").permitAll()
                // Admin dashboard
                .requestMatchers("/dashboard/admin/**", "/admin/**").hasRole("ADMIN")
                // Seller trains management
                .requestMatchers("/dashboard/seller/**", "/trains/new", "/trains/edit/**", "/trains/delete/**").hasAnyRole("SELLER", "ADMIN")
                // Buyer ticket purchase & dashboard
                .requestMatchers("/dashboard/buyer/**", "/tickets/book/**", "/tickets/cancel/**").hasAnyRole("BUYER", "ADMIN")
                // General authenticated routes
                .requestMatchers("/dashboard").authenticated()
                .anyRequest().authenticated()
            )
            // Form Login
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // Logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // CSRF protection exception for H2 console
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            // Frame Options for H2 console
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }

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
