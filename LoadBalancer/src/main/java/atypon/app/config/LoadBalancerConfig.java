package atypon.app.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LoadBalancerConfig {
    @Bean(name = "registrationBean")
    @LoadBalanced
    public RestTemplate registrationRestTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "loginBean")
    @LoadBalanced
    public RestTemplate loginRestTemplate() {
        return new RestTemplate();
    }
}
