package com.boss.bossuserservice.service;

import com.boss.bosscommon.pojo.dto.UserUpdateDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;

public interface ProfileService {
    UserBasicVO getBasicInfo(String token);

    void updateUserInfo(String token, UserUpdateDTO userUpdateDTO);

    UserBasicVO getUserInfo(Long uid);
}
