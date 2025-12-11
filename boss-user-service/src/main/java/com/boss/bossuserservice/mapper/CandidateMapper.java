package com.boss.bossuserservice.mapper;

import com.boss.bosscommon.pojo.entity.UserJobApply;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CandidateMapper {
    void insertUserJobApply(UserJobApply userJobApply);
}
