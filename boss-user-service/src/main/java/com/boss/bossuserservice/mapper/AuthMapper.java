package com.boss.bossuserservice.mapper;

import com.boss.bosscommon.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
    User queryByPhoneAndPassWord(@Param("phone") String phone, @Param("password") String password, @Param("role") Integer role);
}
