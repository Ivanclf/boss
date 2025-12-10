package com.boss.bossuserservice.service;

import com.boss.bosscommon.pojo.dto.UserLoginDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;

public interface AuthService {
    UserBasicVO loginByPassword(UserLoginDTO userLoginDTO);
}
