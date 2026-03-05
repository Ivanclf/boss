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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
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

    private static final Set<String> WHITE_LIST = Set.of(
            "/user/auth/login/**",
            "/user/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();

        if (WHITE_LIST.stream().anyMatch(patten -> antPathMatcher.match(patten, path))) {
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("Authorization");
        if (token == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录状态已过期"));
        }

        String key = LOGIN_USER_KEY + token;

        if (stringRedisTemplate.hasKey(key)) {
            stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.HOURS);
            return chain.filter(exchange);
        } else {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录状态已过期"));
        }
    }
}