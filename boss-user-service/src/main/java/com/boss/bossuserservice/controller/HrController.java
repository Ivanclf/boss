package com.boss.bossuserservice.controller;

import com.boss.bosscommon.exception.clientException;
import com.boss.bosscommon.pojo.dto.UserApplyChangeDTO;
import com.boss.bosscommon.pojo.vo.UserHrShowVO;
import com.boss.bossuserservice.service.HrService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;

import static com.boss.bosscommon.util.TokenUtil.getToken;

@RestController
@RequestMapping("/hr")
public class HrController {

    @Resource
    private HrService hrService;

    @GetMapping("/applications")
    public PageInfo<UserHrShowVO> getApplyList(
            HttpRequest httpRequest,
            @RequestParam(required = false) Long jobUid,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        String token = getToken(httpRequest);
        return hrService.hetApplyList(token, jobUid, status, pageNum, pageSize);
    }

    @PutMapping("/applications")
    public void updateApplications(@RequestBody UserApplyChangeDTO userApplyChangeDTO) {
        if(userApplyChangeDTO.getId() == null || userApplyChangeDTO.getStatus() < 0 || userApplyChangeDTO.getStatus() > 6) {
            throw new clientException("传入的参数不正确");
        }
        hrService.update(userApplyChangeDTO);
    }
}
