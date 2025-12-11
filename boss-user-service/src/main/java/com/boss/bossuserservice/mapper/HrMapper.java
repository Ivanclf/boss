package com.boss.bossuserservice.mapper;

import com.boss.bosscommon.pojo.entity.UserJobApply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrMapper {
    List<UserJobApply> getApplyList(UserJobApply userJobApply);
    
    void update(UserJobApply userJobApply);
}