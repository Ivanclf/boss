package com.boss.bossuserservice.service.serviceImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.boss.bosscommon.exception.ClientException;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.boss.bosscommon.constant.NameConstant.DEFAULT_AVATAR_PATH;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_TTL;
import static com.boss.bosscommon.util.Md5Util.string2Md5;
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
    public UserBasicVO loginByPassword(UserLoginPasswordDTO userLoginPasswordDTO) throws ClientException {

        String phone = userLoginPasswordDTO.getPhone();
        String password = string2Md5(userLoginPasswordDTO.getPassword());
        Integer role = userLoginPasswordDTO.getRole();

        User user = Optional.ofNullable(authMapper.queryByPhoneAndPassWord(phone, password, role))
                .orElseThrow(() -> new ClientException("请输入正确的账号和密码"));

        // 生成 token，并将用户信息存到 redis 中
        UserBasicVO userBasicVO = UserBasicVO.builder()
                .uid(user.getUid())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
        String token = UUID.randomUUID().toString();
        userBasicVO.setAuthorization(token);
        Map<String, Object> redisMap = BeanUtil.beanToMap(userBasicVO, new HashMap<>(), CopyOptions.create()
                .setIgnoreNullValue(true)
                .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));
        String key = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(key, redisMap);
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.HOURS);
        log.info("账户 {} 已成功登录，token 为 {}", phone, token);
        return userBasicVO;
    }

    @Override
    public UserBasicVO registryByPassword(UserRegistryDTO userRegistryDTO) throws ClientException {
        String phone = userRegistryDTO.getPhone();
        Integer role = userRegistryDTO.getRole();

        userRegistryDTO.setPassword(string2Md5(userRegistryDTO.getPassword()));

        User user = authMapper.queryByPhone(phone, role);
        if(user != null) {
            throw new ClientException("用户已存在");
        }

        user = User.builder()
                .phone(userRegistryDTO.getPhone())
                .password(userRegistryDTO.getPassword())
                .role(userRegistryDTO.getRole())
                .name(userRegistryDTO.getName())
                .avatar(DEFAULT_AVATAR_PATH)
                .createTime(LocalDateTime.now())
                .uid(generateId())
                .build();
        authMapper.insert(user);

        // 注册后默认自动登录
        UserBasicVO userBasicVO = UserBasicVO.builder()
                .uid(user.getUid())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
        String token = UUID.randomUUID().toString();
        userBasicVO.setAuthorization(token);
        Map<String, Object> redisMap = BeanUtil.beanToMap(userBasicVO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));
        String key = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(key, redisMap);
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.HOURS);
        log.info("账户 {} 已成功注册并登录，token 为 {}", phone, token);
        return userBasicVO;
    }

    @Override
    public void logout(UserLogoutDTO userLogoutDTO, String token) throws ClientException {
        String phone = userLogoutDTO.getPhone();
        Integer role = userLogoutDTO.getRole();
        String key = LOGIN_USER_KEY + token;

        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(key);
        if(map.isEmpty() || !map.get("role").equals(role.toString())) {
            throw new ClientException("用户已登出");
        }

        stringRedisTemplate.delete(key);
        log.info("用户 {} 已成功登出", phone);
    }
}
