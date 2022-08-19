package com.wsdy.saasops.modules.sys.controller;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.sys.dto.SysPushDto;
import com.wsdy.saasops.modules.sys.entity.SysPush;
import com.wsdy.saasops.modules.sys.service.SysPushService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 系统用户
 */
@RestController
@RequestMapping("/bkapi/sys/push")
@Api(value = "SysUserController", tags = "系统用户")
@Slf4j
public class SysPushController extends AbstractController {
    @Autowired
    private SysPushService sysPushService;


    /**
     * 获取推送
     */
    @GetMapping("/get")
    //@RequiresPermissions("sys:push:get")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R get() {
        return R.ok().put(sysPushService.getByType(Constants.EVNumber.one));
    }

    /**
     * 保存并更新推送
     */
    @PostMapping("/saveAndUpdate")
    //@RequiresPermissions(value = {"sys:push:saveandupdate"})
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody SysPushDto sysPushDto, HttpServletRequest request) {
        Assert.isNull(sysPushDto.getSecret(), "Secret不能为空");
        Assert.isNull(sysPushDto.getPushKey(), "PushKey不能为空");
        sysPushDto.setCreator(getUser().getUsername());
        sysPushDto.setType(Constants.EVNumber.one);
        SysPush sysPush;

        SysPushDto rt = new SysPushDto();
        if(sysPushDto.getId() == null || sysPushDto.getId() <= 0){
            sysPush = sysPushService.save(sysPushDto);
        }else{
            sysPush = sysPushService.update(sysPushDto);
        }

        if(Objects.nonNull(sysPush)){
            BeanUtils.copyProperties(sysPush, rt);
        }
        return R.ok().put(rt);
    }


}
