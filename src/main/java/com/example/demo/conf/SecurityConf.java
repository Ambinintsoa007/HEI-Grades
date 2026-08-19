package com.example.demo.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConf {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/login", "/ping", "/health/**")
                    .permitAll()
                    .requestMatchers("/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/promotions")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/promotions/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/academic-years")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/academic-years/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/groups")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/groups/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/students/me")
                    .hasRole("STUDENT")
                    .requestMatchers(HttpMethod.PATCH, "/students/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/students/*/group-assignments")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/students/*/courses")
                    .hasAnyRole("STUDENT", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/courses")
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, "/courses")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/courses/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/course-offerings")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.POST, "/course-offerings")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/course-offerings/*/teachers")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/course-offerings/*/exams")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.POST, "/course-offerings/*/exams")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.POST, "/grades")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.GET, "/grades/*/history")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.GET, "/exams/*/grades")
                    .hasAnyRole("ADMIN", "TEACHER")
                    .requestMatchers(HttpMethod.GET, "/students/me")
                    .hasRole("STUDENT")
                    .requestMatchers(HttpMethod.GET, "/students/me/grades")
                    .hasRole("STUDENT")
                    .requestMatchers(HttpMethod.GET, "/students/*/transcript")
                    .hasAnyRole("STUDENT", "ADMIN")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

    authoritiesConverter.setAuthoritiesClaimName("role");
    authoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

    return converter;
  }
}
