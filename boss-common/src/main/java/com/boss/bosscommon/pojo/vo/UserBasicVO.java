package com.boss.bosscommon.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBasicVO implements Serializable {
    private Long uid;
    private String name;
    private String phone;
    private String avatar;
    private Integer role;
}
