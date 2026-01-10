package org.lxly.blog.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * 获取签名用的 Key
     * 注意：配置文件中的 secret 必须是 Base64 编码的字符串！
     */
    private Key key() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        } catch (IllegalArgumentException e) {
            log.error("JWT Secret 配置错误，请确保 blog.jwt.secret 是有效的 Base64 字符串");
            throw e;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Long userId, String username, boolean isAdmin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("admin", isAdmin);
        return createToken(claims, String.valueOf(userId));
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}