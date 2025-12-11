package com.boss.bossjobservice.mapper;

import com.boss.bosscommon.pojo.entity.JobTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface JobTagMapper {
    void insertBatch(List<JobTag> jobTags);

    @Select("select * from job_db.job_tag where job_uid = #{JobUid} ")
    List<JobTag> getTagsByUid(Long jobUid);
}
