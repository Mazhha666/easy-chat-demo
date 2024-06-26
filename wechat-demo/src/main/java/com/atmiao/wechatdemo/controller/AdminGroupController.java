package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.annotions.GlobalInterceptor;
import com.atmiao.wechatdemo.commons.ResponseStatusCode;
import com.atmiao.wechatdemo.commons.ResponseVo;
import com.atmiao.wechatdemo.exception.BusinessException;
import com.atmiao.wechatdemo.pojo.GroupInfo;
import com.atmiao.wechatdemo.pojo.UserInfoBeauty;
import com.atmiao.wechatdemo.service.GroupInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author miao
 * @version 1.0
 */
@RestController("adminGroupController")
@CrossOrigin
@Slf4j
@Tag(name = "管理员群组模块", description = "管理员群组设置等接口")
@RequestMapping("admin")
@Validated
public class AdminGroupController {
    @Autowired
    private GroupInfoService groupInfoService;

    @Operation(summary = "loadGroup", description = "得到全部群组信息")
    @GetMapping("loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo loadGroup() {
        Page<GroupInfo> page = groupInfoService.loadGroup();
        return ResponseVo.getSuccessResponseVo(page);
    }

    @Operation(summary = "dissolutionGroup", description = "解散群族")
    @GetMapping("dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVo dissolutionGroup(@RequestParam("groupId") @NotEmpty String groupId) {
        GroupInfo groupInfo = groupInfoService.getById(groupId);
        if(null == groupInfo){
            throw new BusinessException(ResponseStatusCode.STATUS_BUSINESS_ERROR);
        }
        groupInfoService.distributeGroup(groupInfo.getGroupOwnerId(), groupId);
        return ResponseVo.getSuccessResponseVo(null);
    }
}
