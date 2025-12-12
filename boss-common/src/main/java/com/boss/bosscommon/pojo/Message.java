package com.boss.bosscommon.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Serializable {
    private Long fromUid;
    private Long toUid;
    private String message;
}
