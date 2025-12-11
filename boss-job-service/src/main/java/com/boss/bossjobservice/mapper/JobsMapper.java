package com.boss.bossjobservice.mapper;

import com.boss.bosscommon.pojo.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JobsMapper {
    Long insert(Job job);

    @Select("select * from job_db.job where uid = #{uid} and deleted = 0")
    Job getJobByUid(Long uid);
}
