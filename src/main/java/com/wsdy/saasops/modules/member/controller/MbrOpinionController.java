package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrOpinion;
import com.wsdy.saasops.modules.member.service.MbrOpinionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/bkapi/member/opinion")
@Api(value = "MbrOpinionController", tags = "会员意见")
public class MbrOpinionController extends AbstractController {

    @Autowired
    private MbrOpinionService opinionService;

    @GetMapping("list")
    @RequiresPermissions("member:message:opinionlist")
    @ApiOperation(value = "查询页面", notes = "查询页面")
    public R finOpinionList(@ModelAttribute MbrOpinion opinion,
                            @RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(opinionService.finOpinionList(opinion, pageNo, pageSize));
    }

    @PostMapping("update")
    @RequiresPermissions("member:message:updateopinion")
    @ApiOperation(value = "修改状态", notes = "修改状态")
    public R update(@RequestBody MbrOpinion opinion) {
        Assert.isNull(opinion.getId(), "id不能为空!");
        Assert.isNull(opinion.getStatus(), "状态不能为空!");
        opinionService.update(opinion, getUser().getUsername());
        return R.ok();
    }
}
