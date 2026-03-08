package com.boss.bossuserservice.service.serviceImpl;

import com.boss.bosscommon.clients.JobsClient;
import com.boss.bosscommon.exception.ClientException;
import com.boss.bosscommon.pojo.dto.UserJobApplyDTO;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.JobBasicInfoVO;
import com.boss.bossuserservice.mapper.CandidateMapper;
import com.boss.bossuserservice.service.CandidateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static com.boss.bosscommon.constant.JobApplyStatusConstant.PENDING;
import static com.boss.bosscommon.constant.RedisConstant.LOGIN_USER_KEY;
import static com.boss.bosscommon.util.SnowFlakeGenerator.generateId;

@Service
@Slf4j
public class CandidateServiceImpl implements CandidateService {

    @Resource
    private JobsClient jobsClient;
    @Resource
    private CandidateMapper candidateMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void apply(String token, UserJobApplyDTO userJobApplyDTO) throws ClientException {

        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
        if(CollectionUtils.isEmpty(map)) {
           throw new ClientException("token 不正确");
        }

        JobBasicInfoVO jobBasicInfo = jobsClient.getJobBasicInfo(userJobApplyDTO.getJobUid()).getData();

        LocalDateTime now = LocalDateTime.now();
        UserJobApply userJobApply = UserJobApply.builder()
                .id(generateId())
                .candidateUid(Long.valueOf((String) map.get("uid")))
                .hrUid(jobBasicInfo.getHrUid())
                .jobUid(jobBasicInfo.getUid())
                .status(PENDING)
                .applyMsg(userJobApplyDTO.getApplyMsg())
                .createTime(now)
                .updateTime(now)
                .build();
        candidateMapper.insertUserJobApply(userJobApply);
    }
}
