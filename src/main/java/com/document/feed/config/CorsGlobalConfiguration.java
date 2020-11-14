package com.document.feed.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
@Import({CorsConfiguration.class})
public class CorsGlobalConfiguration implements WebFluxConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry corsRegistry) {
    corsRegistry
        .addMapping("/**")
        .allowedOrigins("*")
        .allowedHeaders("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .exposedHeaders(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers",
            "Access-Control-Max-Age",
            "Access-Control-Request-Headers",
            "Access-Control-Request-Method")
        .maxAge(100);
  }
}
