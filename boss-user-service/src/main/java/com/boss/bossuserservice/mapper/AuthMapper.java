package com.boss.bossuserservice.mapper;

import com.boss.bosscommon.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper {
    User queryByPhoneAndPassWord(@Param("phone") String phone, @Param("password") String password, @Param("role") Integer role);

    User queryByPhone(@Param("phone") String phone, @Param("role") Integer role);

    void insert(User user);

    @Select("select * from users_db.user where uid = #{uid} and deleted = 0")
    User queryByUid(Long uid);
}
