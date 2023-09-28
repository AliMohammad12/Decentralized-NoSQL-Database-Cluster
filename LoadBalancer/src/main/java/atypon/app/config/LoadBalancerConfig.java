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
    @Bean(name = "connectionBean")
    @LoadBalanced
    public RestTemplate connectionRestTemplate() {
        return new RestTemplate();
    }
    @Bean(name = "writeRequestsBean")
    @LoadBalanced
    public RestTemplate writeRequestsRestTemplate() {
        return new RestTemplate();
    }
    @Bean(name = "nonBalancedRestTemplateBean")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
