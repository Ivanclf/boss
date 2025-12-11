package com.boss.bosscommon.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJobApplyDTO implements Serializable {
    private Long candidateUid;
    private Long jobUid;
    private String applyMsg;
}
