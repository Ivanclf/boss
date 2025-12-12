package com.boss.bosschatservice.ws;

import com.boss.bosschatservice.config.WebSocketHandShakeInterceptor;
import com.boss.bosschatservice.service.ConversationService;
import com.boss.bosscommon.pojo.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
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

import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;

@Slf4j
@ServerEndpoint(value = "/chat", configurator = WebSocketHandShakeInterceptor.class)
@Component
public class ChatEndPoint {

    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ConversationService conversationService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        String token = (String) session.getUserProperties().get("authorization");
        if (token != null) {
            String key = LOGIN_USER_KEY + token;
            Object uidObj = stringRedisTemplate.opsForHash().get(key, "uid");
            if (uidObj != null) {
                Long uid = null;
                try {
                    uid = Long.valueOf(uidObj.toString());
                } catch (Exception e) {
                    log.error("uid 转换失败", e);
                }
                if (uid != null) {
                    onlineUsers.put(uid, session);
                }
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Message msgObj;
        try {
            msgObj = objectMapper.readValue(message, Message.class);
        } catch (Exception e) {
            log.error("消息解析失败", e);
            return;
        }

        Long senderUid = null;
        for (Map.Entry<Long, Session> entry : onlineUsers.entrySet()) {
            if (entry.getValue() == session) {
                senderUid = entry.getKey();
                break;
            }
        }
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

        // 落盘
        conversationService.saveChatRecord(senderUid, toUid, msgObj.getMessage());
    }

    @OnClose
    public void onClose(Session session) {
        Long offlineUid = null;
        for (Map.Entry<Long, Session> entry : onlineUsers.entrySet()) {
            if (entry.getValue() == session) {
                offlineUid = entry.getKey();
                break;
            }
        }
        if (offlineUid != null) {
            onlineUsers.remove(offlineUid);
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