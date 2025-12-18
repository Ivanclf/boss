package com.boss.bossjobservice.mapper;

import com.boss.bosscommon.pojo.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobsMapper {
    Long insert(Job job);

    @Select("select * from job_db.job where uid = #{uid} and deleted = 0")
    Job getJobByUid(Long uid);

    void update(Job job);

    @Select("select * from job_db.job where deleted = 0")
    List<Job> queryAll();
}
