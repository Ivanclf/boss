package com.boss.bosscommon.pojo.entity;

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
public class ChatRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Integer status;
    private Long fromUid;
    private Long toUid;
    private Long jobUid;
    private LocalDateTime createTime;
    private String context;
    private Integer deleted;
}