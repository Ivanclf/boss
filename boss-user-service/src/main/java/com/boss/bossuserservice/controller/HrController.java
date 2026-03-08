package com.boss.bossuserservice.controller;

import com.boss.bosscommon.exception.ClientException;
import com.boss.bosscommon.pojo.dto.UserApplyChangeDTO;
import com.boss.bosscommon.pojo.vo.UserHrShowVO;
import com.boss.bosscommon.result.Result;
import com.boss.bossuserservice.service.HrService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * HR 处理求职信息的接口
 */
@RestController
@RequestMapping("/hr")
public class HrController {

    @Resource
    private HrService hrService;

    /**
     * 获取指定范围内的所有求职者投递信息
     * @param token
     * @param jobUid 工作 id
     * @param status 投递状态
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/applications")
    public Result<PageInfo<UserHrShowVO>> getApplyList(
            @RequestHeader("authorization") String token,
            @RequestParam(required = false) Long jobUid,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        try {
            return Result.success(hrService.hetApplyList(token, jobUid, status, pageNum, pageSize));
        } catch (ClientException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更改投递信息的状态（即接受或拒绝）
     * @param userApplyChangeDTO
     * @return
     */
    @PutMapping("/applications")
    public Result updateApplications(@RequestBody UserApplyChangeDTO userApplyChangeDTO) {
        if(userApplyChangeDTO.getId() == null || userApplyChangeDTO.getStatus() < 0 || userApplyChangeDTO.getStatus() > 6) {
            return Result.error("传入的参数不正确");
        }
        hrService.update(userApplyChangeDTO);
        return Result.success();
    }
}
