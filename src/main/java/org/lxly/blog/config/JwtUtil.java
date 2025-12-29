package org.lxly.blog.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties props;

    private Key key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes());
    }

    public String generateToken(Long userId, String username, boolean isAdmin) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.getExpiration());

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("admin", isAdmin)
                .setIssuedAt(now)
                .setExpiration(exp)
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
