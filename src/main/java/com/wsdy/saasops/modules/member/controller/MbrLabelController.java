package com.wsdy.saasops.modules.member.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrLabel;
import com.wsdy.saasops.modules.member.service.MbrLabelService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/member/mbrlabel")
@Api(value = "MbrLabel", tags = "会员标签")
public class MbrLabelController extends AbstractController {

    @Autowired
    private MbrLabelService mbrLabelService;

    @GetMapping("/listAll")
    @ResponseBody
    @ApiOperation(value = "会员标签-所有已启用的会员标签信息,存在权限", notes = "查询只有启用的会员标签所有信息,存在权限")
    @RequiresPermissions("member:mbrLabel:list")
    public R findGroupAll() {
        MbrLabel mbrLabel = new MbrLabel();
        return R.ok().put("page", mbrLabelService.queryListCond(mbrLabel));
    }

    /**
     * 	会员标签列表分页required
     */
    @GetMapping("/list")
    @RequiresPermissions("member:mbrLabel:list")
    @ApiOperation(value = "会员标签-根据当前页和每页笔数列表显示会员信息", notes = "查询所有会员标签信息,并分页")
    public R list(@ModelAttribute MbrLabel mbrLabel, @RequestParam("pageNo") @NotNull Integer pageNo,
                  @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", mbrLabelService.listPage(mbrLabel, pageNo, pageSize));
    }

    @PostMapping("/save")
    @ApiOperation(value = "会员标签-保存", notes = "保存一条会员标签明细信息到数据库")
    @RequiresPermissions("member:mbrLabel:save")
    @SysLog(module = "会员标签模块", methodText = "保存会员标签")
    @Transactional
    public R save(@RequestBody MbrLabel mbrLabel, HttpServletRequest request) {
        verifyMbrLabel(mbrLabel);
        mbrLabelService.save(mbrLabel);
        return R.ok().put("label", mbrLabel.getId());
    }

    @PostMapping("/update")
    @ApiOperation(value = "会员标签-更新", notes = "更新一条会员标签明细信息到数据库")
    @RequiresPermissions("member:mbrLabel:update")
    @SysLog(module = "会员标签模块", methodText = "更新会员标签")
    public R update(@RequestBody MbrLabel mbrLabel, HttpServletRequest request) {
        Assert.isNull(mbrLabel.getId(), "会员标签id不能为空!");
        verifyMbrLabel(mbrLabel);
        mbrLabelService.update(mbrLabel);
        return R.ok();
    }
    
    @PostMapping("/delete")
    @ApiOperation(value = "会员标签-删除", notes = "会员标签-删除")
    @RequiresPermissions("member:mbrLabel:delete")
    @SysLog(module = "会员标签模块", methodText = "删除会员标签")
    public R delete(@RequestBody MbrLabel mbrLabel, HttpServletRequest request) {
    	Assert.isNull(mbrLabel.getId(), "会员标签id不能为空!");
    	mbrLabelService.deleteOne(mbrLabel);
    	return R.ok();
    }

    @PostMapping("/updateAvailable")
    @ApiOperation(value = "会员标签-更新状态", notes = "根据会员标签Id更新一条会员标签状态")
    @RequiresPermissions("member:mbrLabel:update")
    public R updateAvailable(@RequestBody MbrLabel mbrLabel) {
        Assert.isNull(mbrLabel.getId(), "会员标签不能为空!");
        Assert.isNull(mbrLabel.getIsAvailable(), "会员标签状态不能为空!");
        mbrLabelService.updateAvailable(mbrLabel.getId(), mbrLabel.getIsAvailable());
        return R.ok();
    }

    private void verifyMbrLabel(MbrLabel mbrLabel) {
        Assert.isBlank(mbrLabel.getName(), "会员标签名不能为空!");
        int count = mbrLabelService.checkNameCount(mbrLabel.getName(), mbrLabel.getId());
        if (count > 0) {
        	throw new R200Exception("标签名已存在");
        }
    }

    @PostMapping("/setMbrLabel")
    @ApiOperation(value = "设置多个会员标签", notes = "设置多个会员标签")
    @RequiresPermissions("member:mbrLabel:update")
    public R setMbrLabel(@RequestBody MbrAccount mbrAccount) {
        Assert.isNull(mbrAccount.getLabelid(), "会员标签不能为空!");
        Assert.isNull(mbrAccount.getUserNames(), "会员名不能为空!");
        mbrLabelService.setMbrLabel(mbrAccount.getLabelid(), mbrAccount.getUserNames());
        return R.ok();
    }

}
