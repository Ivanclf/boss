package com.boss.bosscommon.pojo.vo;

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
public class ChatRecordVO implements Serializable {
    private Integer status;
    private Long fromUid;
    private Long toUid;
    private Long jobUid;
    private LocalDateTime createTime;
    private String context;
}
