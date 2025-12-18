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
public class JobApplyElasticsearchDTO implements Serializable {
    private Long applyId;
    private Long candidateUid;
    private String candidateName;
    private String candidatePhone;
    private Long hrUid;
    private Long jobUid;
    private String jobTitle;
    private String jobCity;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer status;
    private LocalDateTime applyTime;
    private List<String> tags;
}
