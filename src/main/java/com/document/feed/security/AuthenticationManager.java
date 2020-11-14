package com.document.feed.security;

import com.document.feed.config.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

  @Autowired private JwtTokenUtil jwtTokenUtil;

  @Override
  @SuppressWarnings("unchecked")
  public Mono authenticate(Authentication authentication) {
    String authToken = authentication.getCredentials().toString();
    System.out.println("AuthenticationManager.authToken " + authToken);
    String username;
    try {
      username = jwtTokenUtil.getUsernameFromToken(authToken);
    } catch (Exception e) {
      username = null;
    }
    System.out.println("AuthenticationManager.username: " + username);
    if (username != null && !jwtTokenUtil.isTokenExpired(authToken)) {
      Claims claims = jwtTokenUtil.getAllClaimsFromToken(authToken);
      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(username, null, null);
      //            SecurityContextHolder
      //                    .getContext().setAuthentication(auth);
      System.out.println("AuthenticationManager.getContext().setAuthentication:" + auth);
      return Mono.just(auth);
    } else {
      return Mono.empty();
    }
  }
}
