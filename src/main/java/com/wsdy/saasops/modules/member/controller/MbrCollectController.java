package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrCollect;
import com.wsdy.saasops.modules.member.service.MbrCollectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bkapi/member/collect")
@Api(value = "collect", tags = "会员收藏")
public class MbrCollectController extends AbstractController {

    @Autowired
    private MbrCollectService collectService;

    @GetMapping("/menuList")
    @ApiOperation(value = "会员简版菜单查询", notes = "会员简版菜单查询")
    public R menuList() {
        return R.ok().put(collectService.findAccountMenuByRoleId(getUser().getRoleId()));
    }

    @GetMapping("/collectList")
    @ApiOperation(value = "会员收藏菜单查询", notes = "会员收藏菜单查询")
    public R collectList() {
        return R.ok().put(collectService.findCollectList(getUserId(), getUser().getRoleId()));
    }

    @PostMapping("/collectInsert")
    @ApiOperation(value = "会员收藏菜add", notes = "会员收藏菜add")
    public R collectInsert(@RequestBody MbrCollect collect) {
        collectService.collectInsert(getUserId(), collect.getMenuIds());
        return R.ok();
    }
}
