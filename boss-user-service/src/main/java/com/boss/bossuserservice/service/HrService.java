package com.boss.bossuserservice.service;

import com.boss.bosscommon.exception.ClientException;
import com.boss.bosscommon.pojo.dto.UserApplyChangeDTO;
import com.boss.bosscommon.pojo.vo.UserHrShowVO;
import com.github.pagehelper.PageInfo;

public interface HrService {
    PageInfo<UserHrShowVO> hetApplyList(String token, Long jobUid, Integer status, int pageNum, int pageSize) throws ClientException;

    void update(UserApplyChangeDTO userApplyChangeDTO);
}
