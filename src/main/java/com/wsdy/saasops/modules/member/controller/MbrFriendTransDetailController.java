package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.modules.member.dto.MbrFriendTransDetailDto;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import com.wsdy.saasops.modules.member.entity.MbrFriendTransDetail;
import com.wsdy.saasops.modules.member.service.MbrFriendTransDetailService;
import com.wsdy.saasops.common.utils.R;

import javax.validation.constraints.NotNull;


@RestController
@RequestMapping("/bkapi/mbrfreindtransdetail")
@Api(value = "MbrFreindTransDetail", tags = "好友转账")
public class MbrFriendTransDetailController {
    @Autowired
    private MbrFriendTransDetailService mbrFreindTransDetailService;

    @GetMapping("/list")
    @ApiOperation(value="好友转账列表", notes="好友转账列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @RequiresPermissions("member:mbrfreindtransdetail:list")
    public R list(MbrFriendTransDetailDto mbrFreindTransDetail, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", mbrFreindTransDetailService.queryListPage(mbrFreindTransDetail,pageNo,pageSize));
    }


    @GetMapping("/info/{mbdId}")
    @RequiresPermissions("member:mbrfreindtransdetail:info")
    @ApiOperation(value="好友转账详情", notes="好友转账详情")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R info(@PathVariable("mbdId") Integer mbdId) {
        MbrFriendTransDetail mbrFreindTransDetail =mbrFreindTransDetailService.findFriendsTransOneInfo(mbdId);
        return R.ok().put("mbrFreindTransDetail", mbrFreindTransDetail);
    }


}
