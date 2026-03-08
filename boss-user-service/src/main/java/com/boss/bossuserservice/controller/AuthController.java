package com.boss.bossuserservice.controller;

import cn.hutool.core.util.RandomUtil;
import com.boss.bosscommon.exception.ClientException;
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
import static com.boss.bosscommon.util.RegexUtil.isPhoneValid;

/**
 * 处理登录认证相关服务的接口
 */
@RestController
@RequestMapping("/user/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 通过手机号和密码登录
     * @param userLoginPasswordDTO
     * @return
     */
    @PostMapping("/login/password")
    public Result<UserBasicVO> loginByPassWord(@RequestBody UserLoginPasswordDTO userLoginPasswordDTO) {
        if(!isPhoneValid(userLoginPasswordDTO.getPhone())) {
            return Result.error("请输入正确的手机号");
        }
        try {
            UserBasicVO userBasicVO = authService.loginByPassword(userLoginPasswordDTO);
            return Result.success(userBasicVO);
        } catch (ClientException e) {
            return Result.error(e.getMessage());
        }

    }

    /**
     * 注册普通用户或 HR 账号
     * @param userRegistryDTO
     * @return
     */
    @PostMapping("/register")
    public Result<UserBasicVO> registerByPassword(@RequestBody UserRegistryDTO userRegistryDTO) {
        if(!isPhoneValid(userRegistryDTO.getPhone())) {
            return Result.error("请输入正确的手机号");
        }
        if(!StringUtils.hasText(userRegistryDTO.getName())) {
            userRegistryDTO.setName(DEFAULT_USER + RandomUtil.randomString(8));
        }
        try {
            UserBasicVO userBasicVO = authService.registryByPassword(userRegistryDTO);
            return Result.success(userBasicVO);
        } catch (ClientException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     * @param token
     * @param userLogoutDTO
     * @return
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("authorization") String token, @RequestBody UserLogoutDTO userLogoutDTO) {
        try {
            authService.logout(userLogoutDTO, token);
        } catch (ClientException e) {
            return Result.error(e.getMessage());
        }
        return Result.success();
    }
}
