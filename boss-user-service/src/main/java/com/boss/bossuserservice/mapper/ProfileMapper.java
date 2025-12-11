package com.boss.bossuserservice.mapper;

import com.boss.bosscommon.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProfileMapper {
    void update(User user);
}
