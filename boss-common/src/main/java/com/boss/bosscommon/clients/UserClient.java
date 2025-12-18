package com.boss.bosscommon.clients;

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

    @GetMapping("/es/apply")
    List<UserJobApply> initElasticsearch();

    @GetMapping("/{uid}")
    UserBasicVO getUserInfo(@PathVariable Long uid);
}
