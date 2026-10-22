package com.example.DevTutor.config;

import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;

import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpRequest;

@Configuration
public class Context7Config {

    @Bean
    public McpClientCustomizer<HttpClientStreamableHttpTransport.Builder>
    context7Authentication(
            @Value("${context7.api-key}") String apiKey
    ) {

        return (name, builder) -> {
            if ("context7".equals(name)) {
                builder.requestBuilder(
                        HttpRequest.newBuilder()
                                .header(
                                        "Authorization",
                                        "Bearer " + apiKey
                                )
                );
            }
        };
    }
}