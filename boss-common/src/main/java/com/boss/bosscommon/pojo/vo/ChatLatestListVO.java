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
public class ChatLatestListVO implements Serializable {
    private UserBasicVO userBasicVO;
    private LocalDateTime latestTime;
    private String context;
}
