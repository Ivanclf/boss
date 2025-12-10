package com.boss.bossgateway.filter;

import com.boss.bosscommon.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Order(1)
@Component
@Slf4j
public class ResponseWrapperFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();

        ServerHttpResponse decoratedResponse = new ServerHttpResponseDecorator(response) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if(headers.getContentType() != null && (
                        headers.getContentType().includes(MediaType.TEXT_EVENT_STREAM) ||
                                headers.getContentType().includes(MediaType.APPLICATION_OCTET_STREAM)
                        )) {
                    return super.writeWith(body);
                }

                if(body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

                    return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                        DataBufferFactory dataBufferFactory = response.bufferFactory();
                        DataBuffer join = dataBufferFactory.join(dataBuffers);
                        byte[] content = new byte[join.readableByteCount()];
                        join.read(content);

                        try {
                            String originalResponse = new String(content, StandardCharsets.UTF_8);

                            if (getStatusCode().is2xxSuccessful()) {
                                ObjectMapper objectMapper = new ObjectMapper();
                                Object data;

                                // 尝试解析JSON
                                try {
                                    data = objectMapper.readValue(originalResponse, Object.class);
                                } catch (Exception e) {
                                    // 如果不是JSON，直接作为字符串处理
                                    data = originalResponse;
                                }

                                // 包装响应
                                Result<Object> wrappedResult = Result.success(data);
                                String wrappedResponse = objectMapper.writeValueAsString(wrappedResult);

                                // 返回新的数据缓冲区
                                byte[] wrappedBytes = wrappedResponse.getBytes(StandardCharsets.UTF_8);
                                return dataBufferFactory.wrap(wrappedBytes);
                            } else {
                                return dataBufferFactory.wrap(content);
                            }
                        } catch (Exception e) {
                            log.error("响应包装失败", e);
                            return dataBufferFactory.wrap(content);
                        }
                    }));
                }

                return super.writeWith(body);
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(p -> p));
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
}