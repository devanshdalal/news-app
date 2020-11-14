//package com.document.feed.config;
//
//import com.arhs.spring.cache.mongo.MongoCacheBuilder;
//import com.arhs.spring.cache.mongo.MongoCacheManager;
//import java.util.ArrayList;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import org.springframework.cache.CacheManager;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.core.MongoTemplate;
//
//@Configuration
//@RequiredArgsConstructor
//public class MongoCacheConfig {
//  private final MongoTemplate mongoTemplate;
//
//  @Bean
//  public CacheManager cacheManager() {
//    // Create a "cacheName" cache that will use the collection "collectionName" with a TTL 7 days.
//    MongoCacheBuilder cache = MongoCacheBuilder.newInstance("cache", mongoTemplate, "cache");
//    cache.withTTL(24 * 60 * 60);
//    //   cache.withFlushOnBoot(false);
//    List<MongoCacheBuilder> caches = new ArrayList<>();
//    caches.add(cache);
//
//    // Create a manager which will make available the cache created previously.
//    return new MongoCacheManager(caches);
//  }
//}
