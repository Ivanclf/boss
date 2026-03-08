package com.boss.bossuserservice.service;

import com.boss.bosscommon.exception.ClientException;
import com.boss.bosscommon.pojo.dto.UserJobApplyDTO;

public interface CandidateService {
    void apply(String token, UserJobApplyDTO userJobApplyDTO) throws ClientException;
}
