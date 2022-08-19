package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.dto.MbrWarningDealWithDto;
import com.wsdy.saasops.modules.member.dto.MbrWarningQueryDto;
import com.wsdy.saasops.modules.member.dto.SwitchConditionDto;
import com.wsdy.saasops.modules.member.service.MbrWarningService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bkapi/member/mbrwarning")
@Api(value = "MbrWarningController", tags = "会员取款条件设置")
public class MbrWarningController extends AbstractController  {

    @Autowired
    private MbrWarningService mbrWarningService;

    /**
     * 分页查询预警信息
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:mbrwarning:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute MbrWarningQueryDto mbrWarningQueryDto) {
        Assert.isNull(mbrWarningQueryDto.getPageNo(), "PageNo不能为空");
        Assert.isNull(mbrWarningQueryDto.getPageSize(), "pageSize不能为空");
        PageUtils pageUtils = mbrWarningService.pageList(mbrWarningQueryDto);
        return R.ok().put(pageUtils);
    }



    /**
     * 处理预警信息
     */
    @PostMapping("/dealWith")
    @RequiresPermissions("sys:mbrwarning:dealWith")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R dealWith(@RequestBody MbrWarningDealWithDto mbrWarningDealWithDto) {
        Assert.isNull(mbrWarningDealWithDto.getId(), "ID不能为空");
        mbrWarningService.dealWith(mbrWarningDealWithDto, getUser().getUsername());
        return R.ok();
    }



    /**
     * 查询所有条件
     */
    @GetMapping("/conditionList")
    @RequiresPermissions("sys:mbrwarning:conditionList")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R conditionList() {
        return R.ok().put(mbrWarningService.conditionList());
    }


    /**
     * 开启条件
     */
    @GetMapping("/switchCondition")
    @RequiresPermissions("sys:mbrwarning:switchCondition")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R switchCondition(@ModelAttribute SwitchConditionDto switchConditionDto) {
        return R.ok().put(mbrWarningService.switchCondition(switchConditionDto,  getUser().getUsername()));
    }


}
