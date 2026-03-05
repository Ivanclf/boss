package com.boss.bossuserservice.controller;

import com.boss.bosscommon.pojo.dto.UserApplyChangeDTO;
import com.boss.bosscommon.pojo.vo.UserHrShowVO;
import com.boss.bosscommon.result.Result;
import com.boss.bossuserservice.service.HrService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr")
public class HrController {

    @Resource
    private HrService hrService;

    @GetMapping("/applications")
    public Result<PageInfo<UserHrShowVO>> getApplyList(
            @RequestHeader("authorization") String token,
            @RequestParam(required = false) Long jobUid,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return Result.success(hrService.hetApplyList(token, jobUid, status, pageNum, pageSize));
    }

    @PutMapping("/applications")
    public Result updateApplications(@RequestBody UserApplyChangeDTO userApplyChangeDTO) {
        if(userApplyChangeDTO.getId() == null || userApplyChangeDTO.getStatus() < 0 || userApplyChangeDTO.getStatus() > 6) {
            return Result.error("传入的参数不正确");
        }
        hrService.update(userApplyChangeDTO);
        return Result.success();
    }
}
