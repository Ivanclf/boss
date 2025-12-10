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
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long uid;
    private String name;
    private String password;
    private String phone;
    private String avatar;
    private Integer role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}