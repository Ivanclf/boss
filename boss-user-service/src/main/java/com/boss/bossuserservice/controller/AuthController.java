package com.boss.bossuserservice.controller;

import cn.hutool.core.util.RandomUtil;
import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.UserLoginPasswordDTO;
import com.boss.bosscommon.pojo.dto.UserLogoutDTO;
import com.boss.bosscommon.pojo.dto.UserRegistryDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
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
    public UserBasicVO loginByPassWord(@RequestBody UserLoginPasswordDTO userLoginPasswordDTO) {
        if(!isPhoneValid(userLoginPasswordDTO.getPhone())) {
            throw new clientException("请输入正确的手机号");
        }
        userLoginPasswordDTO.setPassword(string2Md5(userLoginPasswordDTO.getPassword()));
        return authService.loginByPassword(userLoginPasswordDTO);
    }

    @PostMapping("/register")
    public UserBasicVO registerByPassword(@RequestBody UserRegistryDTO userRegistryDTO) {
        if(!isPhoneValid(userRegistryDTO.getPhone())) {
            throw new clientException("请输入正确的手机号");
        }
        userRegistryDTO.setPassword(string2Md5(userRegistryDTO.getPassword()));
        if(!StringUtils.hasText(userRegistryDTO.getName())) {
            userRegistryDTO.setName(DEFAULT_USER + RandomUtil.randomString(8));
        }
        return authService.registryByPassword(userRegistryDTO);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("authorization") String token, @RequestBody UserLogoutDTO userLogoutDTO) {
        authService.logout(userLogoutDTO, token);
    }
}
