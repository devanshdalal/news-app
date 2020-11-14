package com.document.feed.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private SecurityContextRepository securityContextRepository;

  @Bean
  SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
    String[] patterns = new String[] {"/auth/**", "/vanillalist", "/newsapi/**"};
    return http.cors()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(
            (swe, e) ->
                Mono.fromRunnable(
                    () -> {
                      swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    }))
        .accessDeniedHandler(
            (swe, e) ->
                Mono.fromRunnable(
                    () -> {
                      swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    }))
        .and()
        .csrf()
        .disable()
        .authenticationManager(authenticationManager)
        .securityContextRepository(securityContextRepository)
        .authorizeExchange()
        .pathMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .pathMatchers(patterns)
        .permitAll()
        .anyExchange()
        .authenticated()
        .and()
        .build();
  }
}
