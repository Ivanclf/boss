package com.boss.bosscommon.pojo.vo;

import com.boss.bosscommon.pojo.entity.JobTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobBasicInfoVO implements Serializable {
    private Long uid;
    private Long hrUid;
    private String title;
    private String description;
    private String requirement;
    private String city;
    private Integer salaryMin;
    private Integer salaryMax;
    private Integer status;
    private List<String> jobTags;
}
