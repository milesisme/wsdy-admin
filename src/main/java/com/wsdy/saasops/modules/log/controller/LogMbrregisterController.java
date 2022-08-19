package com.wsdy.saasops.modules.log.controller;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.log.entity.LogMbrRegister;
import com.wsdy.saasops.modules.log.service.LogMbrregisterService;

@RestController
@RequestMapping("/bkapi/log/logmbrregister")
public class LogMbrregisterController {
    @Autowired
    private LogMbrregisterService logMbrregisterService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("member:mbraccount:list")
    public R list(@RequestBody LogMbrRegister logMbrregister, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", logMbrregisterService.queryListPage(logMbrregister,pageNo,pageSize));
    }


    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("member:mbraccount:info")
    public R info(@PathVariable("id") Integer id) {
        LogMbrRegister logMbrregister =logMbrregisterService.queryObject(id);

        return R.ok().put("logMbrregister", logMbrregister);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("member:mbraccount:save")
    public R save(@RequestBody LogMbrRegister logMbrregister) {
            logMbrregisterService.save(logMbrregister);

        return R.ok();
    }
}
