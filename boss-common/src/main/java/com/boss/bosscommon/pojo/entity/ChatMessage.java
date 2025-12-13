package com.boss.bosscommon.pojo.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatMessage implements Serializable {
    private Long fromUid;
    private Long toUid;
    private String message;
}
