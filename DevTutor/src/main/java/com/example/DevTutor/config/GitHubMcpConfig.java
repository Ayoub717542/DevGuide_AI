package com.example.DevTutor.config;

import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;

import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpRequest;

@Configuration
public class GitHubMcpConfig {

    @Bean
    public McpClientCustomizer<HttpClientStreamableHttpTransport.Builder>
    githubAuthentication(
            @Value("${github.token}") String token
    ) {

        return (name, builder) -> {
            if ("github".equals(name)) {
                builder.requestBuilder(
                        HttpRequest.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .header("X-MCP-Readonly", "true")
                                .header("X-MCP-Toolsets", "repos,issues,pull_requests")
                );
            }
        };
    }
}