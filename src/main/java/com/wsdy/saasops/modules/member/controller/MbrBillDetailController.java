package com.wsdy.saasops.modules.member.controller;

import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.member.entity.MbrBillDetail;
import com.wsdy.saasops.modules.member.service.MbrBillDetailService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/member/mbrbilldetail")
@Api(value = "MbrBillDetail", tags = "会员账变明细交易表记录")
public class MbrBillDetailController {

    @Autowired
    private MbrBillDetailService mbrBillDetailService;

    /**
     * 列表
     */
    @GetMapping("/list")
    @RequiresPermissions("member:mbrbilldetail:list")
    @ApiOperation(value="会员账变记录", notes="会员账变列表明细交易记录")
    public R list(@RequestBody MbrBillDetail mbrBillDetail, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,@RequestParam(value="orderBy",required=false) String orderBy) {
        return R.ok().put("page", mbrBillDetailService.queryListPage(mbrBillDetail,pageNo,pageSize,orderBy));
    }


    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("member:mbrbilldetail:info")
    @ApiOperation(value="会员账变记录", notes="会员账变单条交易记录")
    public R info(@PathVariable("id") Integer id) {
        MbrBillDetail mbrBillDetail =mbrBillDetailService.queryObject(id);

        return R.ok().put("mbrBillDetail", mbrBillDetail);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @RequiresPermissions("member:mbrbilldetail:save")
    @ApiOperation(value="会员账变记录", notes="会员账变单条交易记录保存")
    public R save(@RequestBody MbrBillDetail mbrBillDetail) {
            mbrBillDetailService.save(mbrBillDetail);

        return R.ok();
    }
    @GetMapping("/getOrderNo")
    @ApiOperation(value="查询订单号", notes="查询订单号")
    public R getOrderNo() {
		SnowFlake snowFlake = new SnowFlake(2, 3);
		return R.ok().put("orderNo", snowFlake.nextId());

    }
}
