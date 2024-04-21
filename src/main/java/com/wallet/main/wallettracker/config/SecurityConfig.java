package com.wallet.main.wallettracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/base/**", "/chromedriver/**", "/email/**", "/filter/**",
                "/wallet/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/**").authenticated()  // POST 메소드는 인증 필요
            .requestMatchers(HttpMethod.POST, "/**").authenticated()  // POST 메소드는 인증 필요
            .requestMatchers(HttpMethod.DELETE, "/**").authenticated()  // DELETE 메소드는 인증 필요
            .requestMatchers(HttpMethod.PUT, "/**").authenticated()  // PUT 메소드는 인증 필요
            .anyRequest().permitAll()  // 그 외 요청은 모두 허용 (호출할 메소드가 없으니..)
        )
        .httpBasic(httpBasicCustomizer -> httpBasicCustomizer.realmName(
            "Wallet-Tracker"));  // HTTP 기본 인증 사용

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    UserDetails user = User.builder()
        .username("user")
        .password(passwordEncoder().encode("password"))
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // BCryptPasswordEncoder를 사용
  }

}
