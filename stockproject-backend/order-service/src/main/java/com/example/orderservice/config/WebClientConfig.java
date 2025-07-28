package com.example.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced; // ⭐️ import 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced // ⭐️ 1. 이 어노테이션을 추가합니다.
    public WebClient.Builder webClientBuilder(){
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder){
        // ⭐️ 2. Builder를 주입받아 사용하고, baseUrl을 삭제합니다.
        return builder.build();
    }
}