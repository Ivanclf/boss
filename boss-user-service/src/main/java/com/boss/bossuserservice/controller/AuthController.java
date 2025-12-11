package com.boss.bossuserservice.controller;

import cn.hutool.core.util.RandomUtil;
import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.UserLoginPasswordDTO;
import com.boss.bosscommon.pojo.dto.UserLogoutDTO;
import com.boss.bosscommon.pojo.dto.UserRegistryDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bossuserservice.service.AuthService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpRequest;

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
        if(userRegistryDTO.getName().isBlank()) {
            userRegistryDTO.setName("用户_" + RandomUtil.randomString(8));
        }
        return authService.registryByPassword(userRegistryDTO);
    }

    @PostMapping("/logout")
    public void logout(HttpRequest httpRequest, @RequestBody UserLogoutDTO userLogoutDTO) {
        String token = httpRequest.headers().map().get("Authorization").getFirst();
        authService.logout(userLogoutDTO, token);
    }
}
