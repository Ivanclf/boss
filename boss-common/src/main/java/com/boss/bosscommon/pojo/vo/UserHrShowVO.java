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
public class UserHrShowVO implements Serializable {
    private Long id;
    private Long candidateUid;
    private Long jobUid;
    private Integer status;
    private String applyMsg;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
