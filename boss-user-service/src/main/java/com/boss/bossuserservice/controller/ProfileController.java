package com.boss.bossuserservice.controller;

import com.boss.bosscommon.exception.ClientException;
import com.boss.bosscommon.pojo.dto.UserUpdateDTO;
import com.boss.bosscommon.pojo.entity.User;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bosscommon.result.Result;
import com.boss.bossuserservice.service.ProfileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.boss.bosscommon.util.Md5Util.string2Md5;

/**
 * 用户信息相关接口
 */
@RestController
@RequestMapping("/user/profile")
public class ProfileController {
    @Resource
    private ProfileService profileService;

    /**
     * 从 redis 中拉取用户自己的数据
     * @param token
     * @return
     */
    @GetMapping
    public Result<UserBasicVO> getBasicInfo(@RequestHeader("Authorization") String token) {
        return Result.success(profileService.getBasicInfo(token));
    }

    /**
     * 更新用户数据
     * @param token
     * @param userUpdateDTO
     * @return
     */
    @PutMapping
    public Result updateUserInfo(@RequestHeader("Authorization") String token, @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            profileService.updateUserInfo(token, userUpdateDTO);
        } catch (ClientException e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }


    /**
     * 获取其他用户的数据
     * @param uid
     * @return
     */
    @GetMapping("/{uid}")
    public Result<UserBasicVO> getUserInfo(@NotNull @PathVariable Long uid) {
        return Result.success(profileService.getUserInfo(uid));
    }

    /**
     * 仅用于 ES，用于 ES 数据的全量同步
     * @return
     */
    @GetMapping("/es/apply")
    public List<UserJobApply> initElasticsearch() {
        return profileService.queryForElasticsearch();
    }

    /**
     * 仅用于 ES，用于查询单个用户的数据
     * @param uid
     * @return
     */
    @GetMapping("/es/user/{uid}")
    public User queryUserForElasticsearch(@PathVariable Long uid) {
        return profileService.queryUserForElasticsearch(uid);
    }

    /**
     * 仅用于 ES，用户查询单个用户的工作投递状态
     * @param uid
     * @return
     */
    @GetMapping("/es/apply/{uid}")
    public UserJobApply queryJobApplyForElasticsearch(@PathVariable Long uid) {
        return profileService.queryJobApplyForElasticsearch(uid);
    }
}
