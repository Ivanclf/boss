package com.boss.bosscommon.clients;

import com.boss.bosscommon.pojo.vo.UserBasicVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "boss-user-service", path = "/user/profile")
public interface UserClient {
    @GetMapping
    UserBasicVO getBasicInfo(@RequestHeader("authorization") String token);
}
