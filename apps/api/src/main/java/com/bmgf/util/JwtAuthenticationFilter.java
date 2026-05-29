package com.bmgf.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        if (isPublicPath(request)) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String token = extractToken(request);
            if (token != null && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                List<String> authorityStrings = jwtUtil.getAuthoritiesFromToken(token); // 新增方法
                List<GrantedAuthority> authorities = authorityStrings.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("认证失败", ex);
        }
        chain.doFilter(request, response);
    }

    private boolean isPublicPath(HttpServletRequest request) {
        List<String> publicPaths = Arrays.asList(
                "/user/login",
                "/user/register",
                "/admin/login",
                "/error",
                "/avatar/**"
        );
        return publicPaths.stream()
                .anyMatch(path -> new AntPathRequestMatcher(path).matches(request));
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return StringUtils.hasText(header) && header.startsWith("Bearer ") ?
                header.substring(7) : null;
    }
}