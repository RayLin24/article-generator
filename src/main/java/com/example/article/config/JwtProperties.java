package com.example.article.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret = "article-generator-jwt-secret-key-very-long-string-for-security";
    private long expiration = 86400000; // 24小时
    private String header = "Authorization";
    private String prefix = "Bearer ";
}
