package com.school.research_system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    // 密钥 (实际开发中应该放在配置文件里，这里为了演示直接写死)
    // 必须足够长，否则报错
    private static final String SECRET = "YourSuperSecretKeyForSchoolResearchSystemShouldBeLongEnough";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Token 有效期：24小时
    private static final long EXPIRATION = 86400000L;

    /**
     * 生成 Token
     * 
     * @param username 工号
     * @return token字符串
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username) // 把工号放入 Token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 Token 获取工号
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null; // 解析失败（过期或伪造）
        }
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        return getUsernameFromToken(token) != null && !isTokenExpired(token);
    }

    // 辅助方法：判断是否过期
    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token).getBody();
        return claims.getExpiration();
    }
}