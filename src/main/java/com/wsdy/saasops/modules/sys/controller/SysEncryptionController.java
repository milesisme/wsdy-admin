package com.wsdy.saasops.modules.sys.controller;


import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.service.*;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bkapi/sys/encrypt")
public class SysEncryptionController extends AbstractController {

    @Autowired
    private SysEncryptionService encryptionService;


    @GetMapping("info")
    @RequiresPermissions("sys:encrypt:info")
    @ApiOperation(value = "查看数据库加密", notes = "查看数据库加密")
    public R encryptInfo() {
        return R.ok().put(encryptionService.encryptInfo());
    }

    @GetMapping("update")
    @RequiresPermissions("sys:encrypt:update")
    @ApiOperation(value = "查看数据库加密设置 1开启 0关闭", notes = "查看数据库加密1开启 0关闭")
    public R updateEncryptInfo(@RequestParam String securepwd, @RequestParam String value) {
        Assert.isBlank(securepwd, "安全密码不能为空");
        Assert.isBlank(value, "value不能为空");
        encryptionService.updateEncryptInfo(getUserId(), securepwd, value);
        return R.ok();
    }

}
