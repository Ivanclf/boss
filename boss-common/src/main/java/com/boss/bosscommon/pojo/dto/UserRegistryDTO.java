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
public class UserRegistryDTO implements Serializable {
    private String phone;
    private String password;
    private Integer role;
    private String name;
}
