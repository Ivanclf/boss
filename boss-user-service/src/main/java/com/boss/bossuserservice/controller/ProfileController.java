package com.boss.bossuserservice.controller;

import com.boss.bosscommon.pojo.dto.UserUpdateDTO;
import com.boss.bosscommon.pojo.entity.User;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bossuserservice.service.ProfileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.boss.bosscommon.util.Md5Util.string2Md5;


@RestController
@RequestMapping("/user/profile")
public class ProfileController {
    @Resource
    private ProfileService profileService;

    @GetMapping
    public UserBasicVO getBasicInfo(@RequestHeader("Authorization") String token) {
        return profileService.getBasicInfo(token);
    }

    @PutMapping
    public void updateUserInfo(@RequestHeader("Authorization") String token, @RequestBody UserUpdateDTO userUpdateDTO) {
        if(userUpdateDTO.getPassword() != null) {
            userUpdateDTO.setPassword(string2Md5(userUpdateDTO.getPassword()));
        }
        profileService.updateUserInfo(token, userUpdateDTO);
    }


    @GetMapping("/{uid}")
    public UserBasicVO getUserInfo(@NotNull @PathVariable Long uid) {
        return profileService.getUserInfo(uid);
    }

    @GetMapping("/es/apply")
    public List<UserJobApply> initElasticsearch() {
        return profileService.queryForElasticsearch();
    }

    @GetMapping("/es/user/{uid}")
    public User queryUserForElasticsearch(@PathVariable Long uid) {
        return profileService.queryUserForElasticsearch(uid);
    }

    @GetMapping("/es/apply/{uid}")
    public UserJobApply queryJobApplyForElasticsearch(@PathVariable Long uid) {
        return profileService.queryJobApplyForElasticsearch(uid);
    }
}
