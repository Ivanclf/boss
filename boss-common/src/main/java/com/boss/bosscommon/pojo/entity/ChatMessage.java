package com.boss.bosscommon.pojo.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ChatMessage implements Serializable {
    private Long fromUid;
    private Long toUid;
    private String message;
}
