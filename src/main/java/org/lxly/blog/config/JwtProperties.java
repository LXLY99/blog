package org.lxly.blog.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /** HMAC secret（BASE64 编码） */
    private String secret;
    /** 失效时间（毫秒） */
    private long expiration;
}
