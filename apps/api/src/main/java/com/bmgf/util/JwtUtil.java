package com.bmgf.util;
import com.bmgf.dao.UserRepository;
import com.bmgf.po.User;
import com.bmgf.service.impl.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    // 密钥与有效期从配置注入，禁止硬编码（值见环境变量 / application.properties）
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration; // token 有效期（毫秒），默认 24 小时

    private Key secretKey;

    @PostConstruct
    private void initKey() {
        // 由配置注入的密钥构建 HMAC-SHA 签名密钥
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     * @param username 用户名
     * @param userId 用户ID
     * @return JWT Token
     */
    public String generateToken(String username, String userId) {
        return Jwts.builder()
                .setSubject(username) // 设置主题（用户名）
                .claim("userId", userId) // 添加自定义声明（用户ID）
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 设置过期时间
                .signWith(secretKey) // 使用密钥签名
                .compact(); // 生成 Token
    }

    /**
     * 从 Token 中解析用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey) // 设置签名密钥
                .build()
                .parseClaimsJws(token) // 解析 Token
                .getBody()
                .getSubject(); // 获取主题（用户名）
    }

    /**
     * 从 Token 中解析用户名ID
     * @param token JWT Token
     * @return ID
     */
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey) // 设置签名密钥
                .build()
                .parseClaimsJws(token) // 解析 Token
                .getBody()
                .getSubject();
    }
    public String extractUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT is null or empty");
        }

        token = token.trim(); // 去除前后空格
        if (token.contains(" ")) {
            throw new IllegalArgumentException("Invalid JWT: contains whitespace.");
        }

        Claims claims = Jwts.parser()
                .setSigningKey(secretKey) // 你的密钥，必须与生成时一致
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", String.class); // 获取 userId 声明
    }
    /**
     * 验证 Token 是否有效
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token); // 解析 Token
            return true; // 解析成功则有效
        } catch (Exception e) {
            return false; // 解析失败则无效
        }
    }
    public String generateToken(String username, String userId, Collection<? extends GrantedAuthority> authorities) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("authorities", authorities.stream() // 新增权限声明
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }
    public List<String> getAuthoritiesFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("authorities", List.class);
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}