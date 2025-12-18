package com.boss.bosscommon.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobElasticsearchDTO implements Serializable {
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
    private List<String> tags;
}
