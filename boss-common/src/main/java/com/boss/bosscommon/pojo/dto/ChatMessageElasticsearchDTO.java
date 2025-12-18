package com.boss.bosscommon.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageElasticsearchDTO implements Serializable {
    private Long messageId;
    private Long fromUid;
    private Long toUid;
    private Long jobUid;
    private String context;
    private LocalDateTime createTime;
}
