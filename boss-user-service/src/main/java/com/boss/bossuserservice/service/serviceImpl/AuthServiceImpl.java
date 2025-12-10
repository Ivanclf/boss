package com.boss.bossuserservice.service.serviceImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.boss.bosscommon.pojo.dto.UserLoginDTO;
import com.boss.bosscommon.pojo.entity.User;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import com.boss.bossuserservice.mapper.AuthMapper;
import com.boss.bossuserservice.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AuthMapper authMapper;

    @Override
    @Transactional
    public UserBasicVO loginByPassword(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        String password = userLoginDTO.getPassword();
        Integer role = userLoginDTO.getRole();
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(phone);
        if(!map.isEmpty() && map.get(role) == role) {
            return null;
        }
        User user = authMapper.queryByPhoneAndPassWord(phone, password, role);
        if(user == null) {
            return null;
        }
        UserBasicVO userBasicVO = BeanUtil.copyProperties(user, UserBasicVO.class);
        Map<String, Object> redisMap = BeanUtil.beanToMap(userBasicVO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true).setFieldValueEditor(
                                (fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString()));
        stringRedisTemplate.opsForHash().putAll(phone, redisMap);
        log.info("账户" + phone + "已成功登录");
        return userBasicVO;
    }
}
