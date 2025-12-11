package com.boss.bossuserservice.controller;

import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.UserUpdateDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bossuserservice.service.ProfileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;

import static com.boss.bosscommon.util.Md5Util.string2Md5;

@RestController
@RequestMapping("/user/profile")
public class ProfileController {
    @Resource
    private ProfileService profileService;

    @GetMapping
    public UserBasicVO getBasicInfo(HttpRequest httpRequest) {
        String token = httpRequest.headers().map().get("Authorization").getFirst();
        return profileService.getBasicInfo(token);
    }

    @PutMapping
    public void updateUserInfo(HttpRequest httpRequest, @RequestBody UserUpdateDTO userUpdateDTO) {
        String token = httpRequest.headers().map().get("Authorization").getFirst();
        if(userUpdateDTO.getPassword() != null) {
            userUpdateDTO.setPassword(string2Md5(userUpdateDTO.getPassword()));
        }
        profileService.updateUserInfo(token, userUpdateDTO);
    }


    @GetMapping("/{uid}")
    public UserBasicVO getUserInfo(@PathVariable Long uid) {
        if(uid == null) {
            throw new clientException("请输入必要的参数");
        }
        return profileService.getUserInfo(uid);
    }
}
