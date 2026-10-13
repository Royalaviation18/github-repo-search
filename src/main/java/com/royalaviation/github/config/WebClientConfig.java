package com.royalaviation.github.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder,
                               @Value("${github.api.token:}") String token) {
        HttpClient httpClient = HttpClient.create()
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .responseTimeout(Duration.ofSeconds(10));

        WebClient.Builder configuredBuilder = builder
                .baseUrl("https://api.github.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient));
        if (StringUtils.hasText(token)) {
            configuredBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return configuredBuilder.build();
    }
}