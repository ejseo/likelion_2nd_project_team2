package com.example.boardpjt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 연결 및 설정을 관리하는 Configuration 클래스
 * AWS ElastiCache Serverless Redis 연결을 위한 SSL/TLS 설정 포함
 */
@Configuration
@EnableRedisRepositories
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.ssl.enabled:false}")
    private boolean sslEnabled;

    /**
     * Redis 연결 팩토리 설정
     * ElastiCache Serverless를 위한 SSL 및 타임아웃 설정 포함
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Redis 서버 기본 설정
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);

        // Lettuce 클라이언트 설정
        LettuceClientConfiguration clientConfig;

        // SSL이 활성화된 경우 (ElastiCache Serverless)
        if (sslEnabled) {
            clientConfig = LettuceClientConfiguration.builder()
                    .useSsl()
                    .build();
        } else {
            // 로컬 개발 환경 (SSL 없음)
            clientConfig = LettuceClientConfiguration.builder()
                    .build();
        }

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    /**
     * RedisTemplate 설정 (선택사항)
     * CrudRepository를 사용하므로 필수는 아니지만,
     * 추가적인 Redis 작업이 필요할 경우 사용
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }
}
