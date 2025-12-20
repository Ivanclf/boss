package com.boss.bossgateway.filter;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_TTL;

@Component
@Slf4j
@Order(-1)
public class AuthFilter implements GlobalFilter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private static final List<String> WHITE_LIST = List.of(
            "/user/auth/login",
            "/user/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();

        for (String pattern : WHITE_LIST) {
            if (antPathMatcher.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        String token = request.getHeaders().getFirst("Authorization");
        if (token == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        String key = LOGIN_USER_KEY + token;

        Boolean hasKey = stringRedisTemplate.hasKey(key);
        if (hasKey != null && hasKey) {
            stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.HOURS);
            return chain.filter(exchange);
        } else {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
    }
}