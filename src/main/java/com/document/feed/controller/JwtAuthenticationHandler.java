package com.document.feed.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.document.feed.config.JwtTokenUtil;
import com.document.feed.model.JwtRequest;
import com.document.feed.model.JwtResponse;
import com.document.feed.model.User;
import com.document.feed.model.UserReactiveRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import reactor.core.publisher.Mono;

@Component
@Configuration
public class JwtAuthenticationHandler {
    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 100000;

    @Autowired
    private UserReactiveRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Bean
    public RouterFunction authRoute() {
        return RouterFunctions
                .route(POST("/auth/signin").and(accept(APPLICATION_JSON)).and(accept(
                        APPLICATION_JSON)),
                        this::signIn)
                .andRoute(POST("/auth/signup").and(accept(APPLICATION_JSON)),
                        this::signUp);
    }

    private Mono<ServerResponse> signUp(final ServerRequest request) {
        System.out.println("Start signUp() ");
        Mono<User> userMono = request.bodyToMono(User.class);
        return userMono.flatMap(user -> userRepository.findByUsername(user.getUsername())
                .flatMap(dbUser -> ServerResponse.badRequest().body(BodyInserters.fromValue("User already exist")))
                .switchIfEmpty(userRepository.save(user).flatMap(savedUser -> ServerResponse.ok().contentType(
                        APPLICATION_JSON).body(BodyInserters.fromValue(savedUser)))));
    }

    public Mono<ServerResponse> signIn(ServerRequest request) {
        System.out.println("Start signIn() ");
        Mono<JwtRequest> jwtRequestMono = request.bodyToMono(JwtRequest.class);
        return jwtRequestMono.flatMap(jwtRequest -> userRepository.findByUsername(jwtRequest.getUsername())
                .flatMap(user -> ServerResponse.ok().contentType(APPLICATION_JSON).body(
                        BodyInserters.fromValue(new JwtResponse(generateToken(user))))).switchIfEmpty(ServerResponse.badRequest().body(BodyInserters.fromValue("User does not exist"))));
    }

    public String generateToken(User user) {
        System.out.println("Start generateToken(): " + user.getUsername() + "," +user.getPassword());

        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put("scopes", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(SignatureAlgorithm.HS256, jwtTokenUtil.getSecret())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY_SECONDS * 1000));
        return builder.compact();
    }
}
