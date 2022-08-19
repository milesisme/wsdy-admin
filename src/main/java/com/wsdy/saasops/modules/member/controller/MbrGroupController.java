package com.wsdy.saasops.modules.member.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.member.entity.MbrRebate;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrGroupService;
import com.wsdy.saasops.modules.sys.entity.SysUserMbrgrouprelation;
import com.wsdy.saasops.modules.sys.service.SysUserMbrgrouprelationService;
import com.wsdy.saasops.modules.sys.service.SysUserService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/member/mbrgroup")
@Api(value = "MbrGroup", tags = "会员组")
public class MbrGroupController extends AbstractController {

    @Autowired
    private MbrGroupService mbrGroupService;
    @Autowired
    private SysUserMbrgrouprelationService sysUserMbrgrouprelationService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private SysSettingService sysSettingService;

    /**
     * 查询所启用会员组
     */
    @GetMapping("/listAll")
    @ResponseBody
    @ApiOperation(value = "会员组-所有已启用的会员组信息,存在权限", notes = "查询只有启用的会员组所有信息,存在权限")
    public R findGroupAll() {
        MbrGroup mbrGroup = new MbrGroup();
        mbrGroup.setAvailable(Available.enable);
        //mbrGroup.setBaseAuth(mbrGroupService.getRowAuth());
        return R.ok().put("page", mbrGroupService.queryListCondInAuth(mbrGroup));
    }

    @GetMapping("/findGroupAllNoAuth")
    @ResponseBody
    @ApiOperation(value = "会员组-所有已启用的会员组信息,不存在权限", notes = "查询只有启用的会员组所有信息,不存在权限")
    public R findGroupAllNoAuth() {
        MbrGroup mbrGroup = new MbrGroup();
        mbrGroup.setAvailable(Available.enable);
        //mbrGroup.setBaseAuth(new BaseAuth());
        return R.ok().put("page", mbrGroupService.queryListCondInAuth(mbrGroup));
    }

    /**
     * 	会员组列表分页required
     */
    @GetMapping("/list")
    @RequiresPermissions("member:mbrgroup:list")
    @ApiOperation(value = "会员组-根据当前页和每页笔数列表显示会员信息", notes = "查询所有会员组信息,并分页")
    public R list(@ModelAttribute MbrGroup mbrGroup, @RequestParam("pageNo") @NotNull Integer pageNo,
                  @RequestParam("pageSize") @NotNull Integer pageSize,
                  @RequestParam(value = "orderBy", required = false) String orderBy) {
        PageUtils utils = mbrGroupService.queryListPage(mbrGroup, pageNo, pageSize);
        Map<String, String> differentParam = new HashMap<>(4);
        SysSetting enableDifferentName = sysSettingService.getSysSetting(SystemConstants.BANK_DIFFERENT_NAME_ENABLE);
        if (Objects.nonNull(enableDifferentName) && Objects.nonNull(enableDifferentName.getSysvalue())) {
            differentParam.put("bankDifferentName", enableDifferentName.getSysvalue());
        }
        SysSetting enableDifferentNumber = sysSettingService.getSysSetting(SystemConstants.BANK_DIFFERENT_NAME_NUMBER);
        if (Objects.nonNull(enableDifferentNumber) && Objects.nonNull(enableDifferentNumber.getSysvalue())) {
            differentParam.put("bankDifferentNumber", enableDifferentNumber.getSysvalue());
        }
        R r = R.ok().put("page", utils);
        r.put("different", differentParam);
        return r;
    }

    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    @RequiresPermissions("member:mbrgroup:info")
    @ApiOperation(value = "会员组-单笔会员信息", notes = "根据会员组Id,显示会员组明细信息")
    public R info(@PathVariable("id") Integer id) {
        MbrGroup mbrGroup = mbrGroupService.queryObject(id);
        if (mbrGroup == null) {
            mbrGroup = new MbrGroup();
        }
        return R.ok().put("mbrGroup", mbrGroup);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @ApiOperation(value = "会员组-保存", notes = "保存一条会员组明细信息到数据库")
    @RequiresPermissions("member:mbrgroup:save")
    @SysLog(module = "会员组模块", methodText = "保存会员组")
    @Transactional
    public R save(@RequestBody MbrGroup mbrGroup, HttpServletRequest request) {
        verifyMbrGroup(mbrGroup);
        mbrGroup.setIsLockUpgrade(Constants.EVNumber.zero);
        mbrGroup.setAvailable(Available.disable);
        mbrGroup.setIsBlackGroup(Integer.valueOf(Constants.EVNumber.zero).byteValue()); // 是否黑名单组 1是 0不是
        mbrGroupService.save(mbrGroup);
        // 添加会员组权限到用户权限
        Long userId = super.getUserId();
        SysUserMbrgrouprelation sysUserMbrgrouprelation = new SysUserMbrgrouprelation(mbrGroup.getId(), userId);
        sysUserMbrgrouprelationService.save(sysUserMbrgrouprelation, mbrGroup.getGroupName(), getUser().getUsername(), CommonUtil.getIpAddress(request));
        sysUserService.deleteAuthorityCache(userId, CommonUtil.getSiteCode());
        return R.ok().put("groupId", mbrGroup.getId());
    }

    @GetMapping("/queryListByUserId")
    @ApiOperation(value = "用户会员组权限", notes = "用户会员组权限")
    @RequiresPermissions("member:mbrgroup:info")
    public R queryListByUserId(@RequestParam("userId") Long userId) {
        return R.ok().put("data", sysUserMbrgrouprelationService.queryListByUserId(userId));
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation(value = "会员组-更新", notes = "更新一条会员组明细信息到数据库")
    @RequiresPermissions("member:mbrgroup:update")
    @SysLog(module = "会员组模块", methodText = "更新会员组")
    public R update(@RequestBody MbrGroup mbrGroup, HttpServletRequest request) {
        Assert.isNull(mbrGroup.getId(), "会员组id不能为空!");
        verifyMbrGroup(mbrGroup);
        mbrGroup.setAvailable(null);
        mbrGroupService.update(mbrGroup);
        mbrAccountLogService.updateAcccountGroup(mbrGroup, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation(value = "会员组-删除", notes = "根据会员组Id删除会员组并删除存款,取款条件信息")
    @SysLog(module = "会员组模块", methodText = "删除员组")
    @RequiresPermissions("member:mbrgroup:delete")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header") })
    public R deleteBatch(@RequestBody MbrGroup mbrGroup, HttpServletRequest request) {
        MbrGroup group1 = mbrGroupService.selectMbrGroupById(mbrGroup.getIds());
        if(!Objects.isNull(group1) &&  Integer.valueOf(Constants.EVNumber.one).equals(Integer.valueOf(group1.getIsBlackGroup()))){
            throw new R200Exception("默认黑名单组不可删除");
        }
        mbrGroupService.deleteBatch(mbrGroup.getIds());
        mbrAccountLogService.deleteAccountGroup(group1, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/updateAvailable")
    @ApiOperation(value = "会员组-更新状态", notes = "根据会员组Id更新一条会员组状态")
    @RequiresPermissions("member:mbrgroup:update")
    public R updateAvailable(@RequestBody MbrGroup mbrGroup, HttpServletRequest request) {
        Assert.isNull(mbrGroup.getId(), "会员组不能为空!");
        Assert.isNull(mbrGroup.getAvailable(), "会员组状态不能为空!");
        Integer mod = mbrGroupService.updateGroupAvil(mbrGroup.getId(), mbrGroup.getAvailable());
        if (mod == 0) {
            throw new R200Exception("不能修改状态!");
        }
        Long[] idArr = new Long[1];
        idArr[0] = Long.valueOf(mbrGroup.getId());
        mbrGroup = mbrGroupService.selectMbrGroupById(idArr);
        mbrAccountLogService.updateGroupAvailable(mbrGroup, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    private void verifyMbrGroup(MbrGroup mbrGroup) {
        Assert.isBlank(mbrGroup.getGroupName(), "会员组名不能为空!");
        Assert.isLenght(mbrGroup.getGroupName(), "会员组名最大长度为16位!", 1, 16);
        Assert.isLenght(mbrGroup.getMemo(), "会员组备注最大长度为200个字符", 0, 200);
    }

    @PostMapping("rebateSave")
    @ApiOperation(value = "会员组返点设置", notes = "会员组返点设置")
    @RequiresPermissions("member:mbrrebate:save")
    public R rebateSave(@RequestBody MbrRebate rebate, HttpServletRequest request) {
        Assert.isNull(rebate.getGroupId(), "会员组不能为空");
        Assert.isNull(rebate.getRebateCatDtos(), "返点内容不能为空");
        Assert.isNull(rebate.getAuditType(), "请选择是否稽核");
        mbrGroupService.rebateSave(rebate, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("rebateInfo")
    @ApiOperation(value = "会员组返点设置查看", notes = "会员组返点设置查看")
    @RequiresPermissions("member:mbrrebate:info")
    public R rebateInfo(@RequestParam("groupId") Integer groupId) {
        Assert.isNull(groupId, "会员组不能为空");
        return R.ok().put(mbrGroupService.rebateInfo(groupId));
    }

    @PostMapping("/bankDifferentName")
    @ApiOperation(value = "会员组不同名银行卡设置", notes = "会员组不同名银行卡设置")
    @RequiresPermissions("member:mbrgroup:bankDifferentName")
    public R bankDifferentName(@RequestBody MbrGroup group, HttpServletRequest request) {
        Assert.isNull(group.getBankDifferentName(), "是否开启不同名银行卡不能为空");
        Assert.isMax(new BigDecimal(group.getBankDifferentNumber()), new BigDecimal(5), "不同名银行卡不能大于5");
        mbrGroupService.bankDifferentName(group);
        return R.ok();
    }
}
