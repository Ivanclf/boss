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
public class UserUpdateDTO implements Serializable {
    private String name;
    private String password;
    private String phone;
    private String avatar;
    private Integer role;
    private Integer deleted;
}
