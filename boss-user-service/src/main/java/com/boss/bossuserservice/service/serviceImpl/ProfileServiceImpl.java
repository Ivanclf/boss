package com.boss.bossuserservice.service.serviceImpl;

import cn.hutool.core.bean.BeanUtil;
import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.UserUpdateDTO;
import com.boss.bosscommon.pojo.entity.User;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bossuserservice.mapper.AuthMapper;
import com.boss.bossuserservice.mapper.ProfileMapper;
import com.boss.bossuserservice.service.ProfileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_TTL;

@Service
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AuthMapper authMapper;
    @Resource
    private ProfileMapper profileMapper;

    @Override
    public UserBasicVO getBasicInfo(String token) {
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
        return BeanUtil.fillBeanWithMap(map, new UserBasicVO(), true);
    }

    @Override
    @Transactional
    public void updateUserInfo(String token, UserUpdateDTO userUpdateDTO) {
        User existed = authMapper.queryByPhone(userUpdateDTO.getPhone(), userUpdateDTO.getRole());
        if(existed == null) {
            throw new clientException("该用户不存在");
        }

        String key = LOGIN_USER_KEY + token;
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(key);
        User user = User.builder()
                .name(userUpdateDTO.getName())
                .password(userUpdateDTO.getPassword())
                .phone(userUpdateDTO.getPhone())
                .avatar(userUpdateDTO.getAvatar())
                .deleted(userUpdateDTO.getDeleted())
                .role(null)
                .updateTime(LocalDateTime.now())
                .uid((Long) map.get("uid"))
                .build();
        profileMapper.update(user);

        if(userUpdateDTO.getName() != null) {
            map.put("name", userUpdateDTO.getName());
        }
        if(userUpdateDTO.getPhone() != null) {
            map.put("phone", userUpdateDTO.getPhone());
        }
        if(userUpdateDTO.getAvatar() != null) {
            map.put("avatar", userUpdateDTO.getAvatar());
        }
        if(userUpdateDTO.getDeleted() != null && userUpdateDTO.getDeleted() == 1) {
            stringRedisTemplate.opsForHash().delete(key);
        }

        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.HOURS);
    }

    @Override
    public UserBasicVO getUserInfo(Long uid) {
        User user = authMapper.queryByUid(uid);
        if (user == null) {
            return null;
        }
        return UserBasicVO.builder()
                .uid(user.getUid())
                .name(user.getName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .build();
    }

    @Override
    public List<UserJobApply> queryForElasticsearch() {
        return profileMapper.queryForElasticsearch();
    }

    @Override
    public User queryUserForElasticsearch(Long uid) {
        return authMapper.queryByUid(uid);
    }

    @Override
    public UserJobApply queryJobApplyForElasticsearch(Long uid) {
        return profileMapper.queryJobApplyByUid(uid);
    }
}
