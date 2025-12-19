package com.boss.bosscommon.clients;

import com.boss.bosscommon.pojo.entity.User;
import com.boss.bosscommon.pojo.entity.UserJobApply;
import com.boss.bosscommon.pojo.vo.UserBasicVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(value = "boss-user-service", path = "/user/profile")
public interface UserClient {
    @GetMapping
    UserBasicVO getBasicInfo(@RequestHeader("Authorization") String token);

    @GetMapping("/{uid}")
    UserBasicVO getUserInfo(@PathVariable Long uid);

    @GetMapping("/es/apply")
    List<UserJobApply> initElasticsearch();

    @GetMapping("/es/user/{uid}")
    User queryUserForElasticsearch(@PathVariable Long uid);

    @GetMapping("/es/apply/{uid}")
    UserJobApply queryJobApplyForElasticsearch(@PathVariable Long uid);
}
