package com.boss.bossuserservice.service.serviceImpl;

import cn.hutool.core.bean.BeanUtil;
import com.boss.bosscommon.pojo.dto.UserApplyChangeDTO;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.UserHrShowVO;
import com.boss.bossuserservice.mapper.HrMapper;
import com.boss.bossuserservice.service.HrService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;

@Service
@Slf4j
public class HrServiceImpl implements HrService {

    @Resource
    private HrMapper hrMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public PageInfo<UserHrShowVO> hetApplyList(String token, Long jobUid, Integer status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        UserJobApply userJobApply = UserJobApply.builder()
                .hrUid((Long) stringRedisTemplate.opsForHash().get(LOGIN_USER_KEY + token, "uid"))
                .jobUid(jobUid)
                .status(status)
                .build();

        List<UserHrShowVO> userHrShowVOS = hrMapper.getApplyList(userJobApply)
                .stream().map(userJobApply1 -> {
                    return BeanUtil.copyProperties(userJobApply1, UserHrShowVO.class);
                }).toList();

        return new PageInfo<>(userHrShowVOS);
    }

    @Override
    public void update(UserApplyChangeDTO userApplyChangeDTO) {
        UserJobApply userJobApply = UserJobApply.builder()
                .id(userApplyChangeDTO.getId())
                .status(userApplyChangeDTO.getStatus())
                .build();
        hrMapper.update(userJobApply);
    }
}
