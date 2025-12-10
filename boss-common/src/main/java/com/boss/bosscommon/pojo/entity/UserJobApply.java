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
public class UserJobApply implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long candidateUid;
    private Long hrUid;
    private Long jobUid;
    private Integer status;
    private String applyMsg;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}