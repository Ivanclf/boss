package com.boss.bossuserservice.controller;

import com.boss.bosscommon.pojo.dto.UserLoginDTO;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bossuserservice.service.AuthService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.boss.bosscommon.util.RegexUtil.isPhoneValid;

@RestController
@RequestMapping("/user/auth")
public class AuthController {
    @Resource
    private AuthService authService;

    @PostMapping("/login/password")
    public UserBasicVO loginByPassWord(@RequestBody UserLoginDTO userLoginDTO) {
        if(!isPhoneValid(userLoginDTO.getPhone())) {
            return null;
        }
        return authService.loginByPassword(userLoginDTO);
    }
}
