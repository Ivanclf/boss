package com.boss.bossuserservice.service.serviceImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.boss.bosscommon.exception.errorException;
import com.boss.bosscommon.pojo.dto.UserLoginPasswordDTO;
import com.boss.bosscommon.pojo.dto.UserLogoutDTO;
import com.boss.bosscommon.pojo.dto.UserRegistryDTO;
import com.boss.bosscommon.pojo.entity.User;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bossuserservice.mapper.AuthMapper;
import com.boss.bossuserservice.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_TTL;
import static com.boss.bosscommon.util.SnowFlakeGenerator.generateId;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AuthMapper authMapper;

    @Override
    @Transactional
    public UserBasicVO loginByPassword(UserLoginPasswordDTO userLoginPasswordDTO) {
        String phone = userLoginPasswordDTO.getPhone();
        String password = userLoginPasswordDTO.getPassword();
        Integer role = userLoginPasswordDTO.getRole();


        User user = authMapper.queryByPhoneAndPassWord(phone, password, role);
        if(user == null) {
            throw new errorException("请输入正确的账号和密码");
        }

        UserBasicVO userBasicVO = BeanUtil.copyProperties(user, UserBasicVO.class);
        String token = UUID.randomUUID().toString();
        userBasicVO.setAuthorization(token);
        Map<String, Object> redisMap = BeanUtil.beanToMap(userBasicVO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true).setFieldValueEditor(
                                (fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));
        String key = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(key, redisMap);
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.HOURS);
        log.info("账户 {} 已成功登录，token 为 {}", phone, token);
        return userBasicVO;
    }

    @Override
    public UserBasicVO registryByPassword(UserRegistryDTO userRegistryDTO) {
        String phone = userRegistryDTO.getPhone();
        Integer role = userRegistryDTO.getRole();


        User user = authMapper.queryByPhone(phone, role);
        if(user != null) {
            throw new errorException("用户已存在");
        }

        user = BeanUtil.copyProperties(userRegistryDTO, User.class);
        user.setAvatar("http://localhost:8080");
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUid(generateId());
        authMapper.insert(user);

        UserBasicVO userBasicVO = BeanUtil.copyProperties(user, UserBasicVO.class);
        String token = UUID.randomUUID().toString();
        userBasicVO.setAuthorization(token);
        Map<String, Object> redisMap = BeanUtil.beanToMap(userBasicVO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true).setFieldValueEditor(
                                (fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));
        String key = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(key, redisMap);
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.DAYS);
        log.info("账户 {} 已成功注册并登录，token 为 {}", phone, token);
        return userBasicVO;
    }

    @Override
    public void logout(UserLogoutDTO userLogoutDTO, String token) {
        String phone = userLogoutDTO.getPhone();
        Integer role = userLogoutDTO.getRole();
        String key = LOGIN_USER_KEY + token;

        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(key);
        if(map.isEmpty() || map.get("role") != role) {
            throw new errorException("用户已登出");
        }

        stringRedisTemplate.opsForHash().delete(key);
        log.info("用户 {} 已成功登出", phone);
    }
}
