package org.lxly.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "blog.jwt") // ✅ Must match the YAML indentation
public class JwtProperties {
    /**
     * Secret key for signing the token
     */
    private String secret;

    /**
     * Expiration time in milliseconds
     */
    private Long expiration;
}