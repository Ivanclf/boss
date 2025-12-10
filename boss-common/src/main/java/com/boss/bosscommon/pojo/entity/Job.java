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
public class Job implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long uid;
    private Long hrUid;
    private String title;
    private String description;
    private String requirement;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime updateTime;
}