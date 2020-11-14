package com.document.feed.security;

import com.document.feed.config.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

  private static final Logger logger = LoggerFactory.getLogger(SecurityContextRepository.class);

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Override
  public Mono save(ServerWebExchange serverWebExchange, SecurityContext sc) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Mono load(ServerWebExchange serverWebExchange) {
    System.out.println("serverWebExchange:" + serverWebExchange.getAttributes());
    ServerHttpRequest request = serverWebExchange.getRequest();
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    String authToken = null;
    if (authHeader != null && authHeader.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
      authToken = authHeader.replace(JwtTokenUtil.TOKEN_PREFIX, "");
    } else {
      logger.warn("couldn't find bearer string, will ignore the header.");
    }
    System.out.println(
        "SecurityContextRepository.authToken=" + authToken + "\nauthHeader=" + authHeader);
    String username;
    try {
      username = jwtTokenUtil.getUsernameFromToken(authToken);
    } catch (Exception e) {
      username = null;
    }
    System.out.println("SecurityContextRepository.username:" + username);
    if (authToken != null) {
      Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
      return authenticationManager
          .authenticate(auth)
          .map(
              (authentication) -> {
                SecurityContextHolder.getContext()
                    .setAuthentication((Authentication) authentication);
                return new SecurityContextImpl((Authentication) authentication);
              });
    } else {
      return Mono.empty();
    }
  }
}
