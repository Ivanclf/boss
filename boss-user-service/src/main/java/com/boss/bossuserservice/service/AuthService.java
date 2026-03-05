package com.boss.bossuserservice.service;

import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.UserLoginPasswordDTO;
import com.boss.bosscommon.pojo.dto.UserLogoutDTO;
import com.boss.bosscommon.pojo.dto.UserRegistryDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;

public interface AuthService {
    UserBasicVO loginByPassword(UserLoginPasswordDTO userLoginPasswordDTO) throws clientException;

    UserBasicVO registryByPassword(UserRegistryDTO userRegistryDTO) throws clientException;

    void logout(UserLogoutDTO userLogoutDTO, String token) throws clientException;
}
