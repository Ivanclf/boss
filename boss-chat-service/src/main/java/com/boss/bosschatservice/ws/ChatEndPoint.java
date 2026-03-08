package com.boss.bosschatservice.ws;

import com.boss.bosschatservice.config.WebSocketHandShakeInterceptor;
import com.boss.bosschatservice.service.ConversationService;
import com.boss.bosschatservice.util.SpringContextUtil;
import com.boss.bosscommon.exception.ClientException;
import com.boss.bosscommon.pojo.entity.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.boss.bosscommon.constant.ChatConstant.CHAT_HUMAN_RESOURCES;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;

@Slf4j
@ServerEndpoint(value = "/chat", configurator = WebSocketHandShakeInterceptor.class)
@Component
public class ChatEndPoint {

    // 存储在线用户的映射关系，以向特定用户推送消息
    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();
    // 存储已认证绘画的用户表示，验证当前的用户会话是否是有效用户连接
    private static final Map<Session, Long> authenticatedUsers = new ConcurrentHashMap<>();

    private static final StringRedisTemplate stringRedisTemplate;
    private static final ConversationService conversationService;
    private static final ObjectMapper objectMapper;

    static {
        stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        conversationService = SpringContextUtil.getBean(ConversationService.class);
        objectMapper = SpringContextUtil.getBean(ObjectMapper.class);
    }


    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket 链接建立: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (!authenticatedUsers.containsKey(session)) {
            try {
                String token = message.trim();
                String key = LOGIN_USER_KEY + token;
                Object uid = stringRedisTemplate.opsForHash().get(key, "uid");

                if (uid instanceof String) {
                    authenticatedUsers.put(session,Long.valueOf((String) uid));
                    onlineUsers.put(Long.valueOf((String) uid), session);

                    session.getBasicRemote().sendText("{\"type\":\"auth\",\"status\":\"success\"}");
                    log.info("用户 {} 认证成功", uid);
                    return;
                }

                session.getBasicRemote().sendText("{\"type\":\"auth\",\"status\":\"failed\",\"message\":\"Authentication failed\"}");
                session.close();
            } catch (IOException e) {
                log.error("发送信息失败", e);
            }
        }

        ChatMessage msgObj;
        try {
            msgObj = objectMapper.readValue(message, ChatMessage.class);
        } catch (Exception e) {
            log.error("消息解析失败", e);
            return;
        }

        Long senderUid = authenticatedUsers.get(session);
        if (senderUid == null) {
            log.warn("未找到发送者uid");
            return;
        }

        Long toUid = msgObj.getToUid();
        Session toSession = onlineUsers.get(toUid);
        if (toSession != null && toSession.isOpen()) {
            try {
                toSession.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("消息推送失败", e);
            }
        }

        if (!toUid.equals(senderUid)) {
            Session fromSession = onlineUsers.get(senderUid);
            if (fromSession != null && fromSession.isOpen()) {
                try {
                    fromSession.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("消息回推失败", e);
                }
            }
        }

        try {
            conversationService.saveChatRecord(senderUid, toUid, msgObj.getMessage(), CHAT_HUMAN_RESOURCES);
        } catch (JsonProcessingException e) {
            throw new ClientException("数据写入数据库失败");
        }
    }

    @OnClose
    public void onClose(Session session) {
        Long uid = authenticatedUsers.remove(session);
        if (uid != null) {
            onlineUsers.remove(uid);
            log.info("用户 {} 断开连接", uid);
        } else {
            log.info("未认证的连接 {} 关闭", session.getId());
        }
    }

    private void broadcasts(String message) {
        try {
            Set<Map.Entry<Long, Session>> entries = onlineUsers.entrySet();
            for (Map.Entry<Long, Session> entry : entries) {
                Session session = entry.getValue();
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            log.error("广播消息失败");
        }
    }
}