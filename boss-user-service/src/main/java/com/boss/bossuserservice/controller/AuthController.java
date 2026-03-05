package com.boss.bossuserservice.controller;

import cn.hutool.core.util.RandomUtil;
import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.UserLoginPasswordDTO;
import com.boss.bosscommon.pojo.dto.UserLogoutDTO;
import com.boss.bosscommon.pojo.dto.UserRegistryDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bosscommon.result.Result;
import com.boss.bossuserservice.service.AuthService;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import static com.boss.bosscommon.constant.NameConstant.DEFAULT_USER;
import static com.boss.bosscommon.util.Md5Util.string2Md5;
import static com.boss.bosscommon.util.RegexUtil.isPhoneValid;

@RestController
@RequestMapping("/user/auth")
public class AuthController {
    @Resource
    private AuthService authService;

    @PostMapping("/login/password")
    public Result<UserBasicVO> loginByPassWord(@RequestBody UserLoginPasswordDTO userLoginPasswordDTO) {
        if(!isPhoneValid(userLoginPasswordDTO.getPhone())) {
            return Result.error("请输入正确的手机号");
        }
        userLoginPasswordDTO.setPassword(string2Md5(userLoginPasswordDTO.getPassword()));

        try {
            UserBasicVO userBasicVO = authService.loginByPassword(userLoginPasswordDTO);
            return Result.success(userBasicVO);
        } catch (clientException e) {
            return Result.error(e.getMessage());
        }

    }

    @PostMapping("/register")
    public Result<UserBasicVO> registerByPassword(@RequestBody UserRegistryDTO userRegistryDTO) {
        if(!isPhoneValid(userRegistryDTO.getPhone())) {
            return Result.error("请输入正确的手机号");
        }
        userRegistryDTO.setPassword(string2Md5(userRegistryDTO.getPassword()));
        if(!StringUtils.hasText(userRegistryDTO.getName())) {
            userRegistryDTO.setName(DEFAULT_USER + RandomUtil.randomString(8));
        }
        try {
            UserBasicVO userBasicVO = authService.registryByPassword(userRegistryDTO);
            return Result.success(userBasicVO);
        } catch (clientException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result logout(@RequestHeader("authorization") String token, @RequestBody UserLogoutDTO userLogoutDTO) {
        try {
            authService.logout(userLogoutDTO, token);
        } catch (clientException e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }
}
