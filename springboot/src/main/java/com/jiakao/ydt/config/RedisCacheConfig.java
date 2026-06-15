package com.jiakao.ydt.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class RedisCacheConfig implements CachingConfigurer {

    public static final String SCHOOLS_PUBLIC_LIST = "schools:public:list";
    public static final String SCHOOLS_PUBLIC_RANKINGS = "schools:public:rankings";
    public static final String SCHOOLS_PUBLIC_DETAIL = "schools:public:detail";
    public static final String VENUES_PAGE = "venues:page";
    public static final String VENUES_ROUTES = "venues:routes";
    public static final String QUESTIONS_SUBJECT_ALL = "questions:subject:all";
    public static final String QUESTIONS_BY_IDS = "questions:by-ids";
    public static final String QUESTIONS_CHAPTERS = "questions:chapters";
    public static final String REVIEWS_PUBLIC_RECENT = "reviews:public:recent";
    public static final String AMAP_GEOCODE = "amap:geocode";
    public static final String AMAP_DIRECTION = "amap:direction";
    public static final String OFFICIAL_EXAM_SCHEDULE_INFO = "official-exam-schedule:info";

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .prefixCacheNameWith("ydt:")
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisJsonSerializer()));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration(SCHOOLS_PUBLIC_LIST, cacheConfiguration().entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration(SCHOOLS_PUBLIC_RANKINGS, cacheConfiguration().entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(SCHOOLS_PUBLIC_DETAIL, cacheConfiguration().entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(VENUES_PAGE, cacheConfiguration().entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration(VENUES_ROUTES, cacheConfiguration().entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration(QUESTIONS_SUBJECT_ALL, cacheConfiguration().entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration(QUESTIONS_BY_IDS, cacheConfiguration().entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration(QUESTIONS_CHAPTERS, cacheConfiguration().entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration(REVIEWS_PUBLIC_RECENT, cacheConfiguration().entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration(AMAP_GEOCODE, cacheConfiguration().entryTtl(Duration.ofDays(30)))
                .withCacheConfiguration(AMAP_DIRECTION, cacheConfiguration().entryTtl(Duration.ofDays(7)))
                .withCacheConfiguration(OFFICIAL_EXAM_SCHEDULE_INFO, cacheConfiguration().entryTtl(Duration.ofMinutes(15)));
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> method.getName() + ":" + Arrays.stream(params)
                .map(param -> param == null ? "null" : param.toString().trim())
                .collect(Collectors.joining(":"));
    }

    private GenericJackson2JsonRedisSerializer redisJsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
