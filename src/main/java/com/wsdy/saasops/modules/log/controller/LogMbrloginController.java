package com.wsdy.saasops.modules.log.controller;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.log.entity.LogMbrLogin;
import com.wsdy.saasops.modules.log.service.LogMbrloginService;

@RestController
@RequestMapping("/bkapi/log/logmbrlogin")
public class LogMbrloginController {
    @Autowired
    private LogMbrloginService logMbrloginService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("member:mbraccount:list")
    public R list(@ModelAttribute LogMbrLogin logMbrlogin, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", logMbrloginService.queryListPage(logMbrlogin, pageNo, pageSize));
    }


    /**
     * 信息
     */
    @GetMapping("/info")
    @RequiresPermissions("member:mbraccount:info")
    public R info(@ModelAttribute LogMbrLogin logMbrlogin, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(logMbrloginService.findLogMemberLoginLastOne(logMbrlogin, pageNo, pageSize));
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("member:mbraccount:save")
    public R save(@RequestBody LogMbrLogin logMbrlogin) {
        logMbrloginService.save(logMbrlogin);
        return R.ok();
    }

}
