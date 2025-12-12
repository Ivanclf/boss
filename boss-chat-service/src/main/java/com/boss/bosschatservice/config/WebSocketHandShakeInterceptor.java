package com.boss.bosschatservice.config;

import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class WebSocketHandShakeInterceptor extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        Map<String, Object> userProperties = config.getUserProperties();
        Map<String, List<String>> headers = request.getHeaders();

        List<String> authorizationHeaders = headers.get("authorization");
        if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
            userProperties.put("authorization", authorizationHeaders.getFirst());
        }
    }
}