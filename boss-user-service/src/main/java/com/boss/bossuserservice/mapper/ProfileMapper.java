package com.boss.bossuserservice.mapper;

import com.boss.bosscommon.pojo.entity.User;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProfileMapper {
    void update(User user);

    @Select("select * from users_db.user_job_apply where deleted = 0")
    List<UserJobApply> queryForElasticsearch();

    @Select("select * from users_db.user_job_apply where id = #{uid} and deleted = 0")
    UserJobApply queryJobApplyByUid(Long uid);
}
