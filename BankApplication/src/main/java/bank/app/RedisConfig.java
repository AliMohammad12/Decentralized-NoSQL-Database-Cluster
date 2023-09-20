package bank.app;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class RedisConfig {
//    @Bean
//    public LettuceConnectionFactory redisConnectionFactory() {
//        final RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
//        //config.setHostName("localhost");
//        //config.setPort(6379);
//        return new LettuceConnectionFactory(config);
//    }
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate() {
//        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory());
//        return redisTemplate;
//    }
}