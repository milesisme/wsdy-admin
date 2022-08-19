package com.wsdy.saasops.modules.sys.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.api.modules.user.service.DepotOperatService;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.common.validator.ValidatorUtils;
import com.wsdy.saasops.common.validator.group.AddGroup;
import com.wsdy.saasops.modules.analysis.controller.RetentionRateController;
import com.wsdy.saasops.modules.analysis.dto.RetentionRateDailyActiveDto;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.sys.dao.SysUserDao;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity.ErrorCode;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.wsdy.saasops.modules.sys.service.SysUserRoleService;
import com.wsdy.saasops.modules.sys.service.SysUserService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 系统用户
 */
@RestController
@RequestMapping("/bkapi/sys/user")
@Api(value = "SysUserController", tags = "系统用户")
@Slf4j
public class SysUserController extends AbstractController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private DepotOperatService depotOperatService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;

    /**
     * 所有用户列表
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:user:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R list(@ModelAttribute SysUserEntity userEntity) {
        //只有超级管理员，才能查看所有管理员列表
        if (getUser().getRoleId() != 1L) {
            userEntity.setCreateUserId(getUserId());
        }
        //查询列表数据
        PageUtils pageUtil = sysUserService.queryList(userEntity);
        return R.ok().put("page", pageUtil);
    }

    /**
     * 所有用户列表
     */
    @GetMapping("/queryConditions")
    @RequiresPermissions("sys:user:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryConditions(@ModelAttribute SysUserEntity userEntity) {
        SysUserEntity sysUserEntity = getUser();
        if (sysUserEntity == null) {
            return R.error(2000, "无法获取当前用户信息");
        }
        log.info(sysUserEntity.toString());
        if (sysUserEntity.getRoleId() == null) {
            sysUserEntity = sysUserDao.queryObject(sysUserEntity.getUserId());
            //return R.error(2000,"无法获取当前用户角色信息");
        }
        //只有超级管理员，才能查看所有管理员列表
        if (sysUserEntity.getRoleId() != 1 && sysUserEntity.getUserId() != null && sysUserEntity.getUserId() != 1) {
            userEntity.setCreateUserId(getUserId());
        } else {
            userEntity.setRoleId(1);
        }
        userEntity.setIsDelete(1);
        //查询列表数据
        //Query query = new Query(params);
        PageUtils pageUtil = sysUserService.queryConditions(userEntity);
        return R.ok().put("page", pageUtil);
    }

    /**
     * 获取登录的用户信息
     */
    @RequestMapping("/info")
    public R info() {
        SysUserEntity userEntity = getUser();
        userEntity.setMobile(null);
        userEntity.setPassword(null);
        userEntity.setSecurepwd(null);
        userEntity.setSalt(null);

        //排除admin
        if (1 != userEntity.getUserId()) {
            //在此做密码过期校验
            String expireFlag = sysUserDao.checkPasswordIsExpire(userEntity.getUserId());
            if ("true".equals(expireFlag)) {
                return R.ok(402, userEntity);
            }
        }
        return R.ok().put("user", userEntity);
    }

    /**
     * 认证安全密码
     */
//	@SysLog("认证安全密码")
    @RequestMapping("/authsecpwd")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R authsecPwd(@RequestBody SysUserEntity userEntity) {
        Assert.isBlank(userEntity.getSecurepwd(), "安全密码不为能空!");
        int code = sysUserService.authsecPwd(getUserId(), userEntity.getSecurepwd());
        switch (code) {
            case ErrorCode.code_06:
                return R.error(2000, "安全密码错误");
            default:
                return R.ok();
        }
    }


    @RequestMapping("/password")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R password(@RequestBody SysUserEntity userEntity, HttpServletRequest request) {
        Assert.isNull(userEntity, "用户信息不能为空");
        Assert.isBlank(userEntity.getNewPassword(), "新密码不为能空");
        Assert.isPwdCharacter(userEntity.getNewPassword(), "密码必须包含英文和数字，6-20个字符");
        Assert.checkSequentialSameChars(userEntity.getNewPassword(), "密码不允许出现连续的3个相同字符");
        Assert.checkLateralKeyboardSite(userEntity.getNewPassword(), "密码不允许出现连续的3个相邻字符");
        String password = StringUtils.EMPTY;
        SysUserEntity user;
        if(null == userEntity.getUserId()){
            user = new SysUserEntity();
            user.setUserId(getUser().getUserId());
            user.setSalt(getUser().getSalt());
            user.setUsername(getUser().getUsername());  // 操作日志需要用户名
        } else {
            user = sysUserService.queryUserEntityOne(userEntity.getUserId());
        }

        Assert.accEqualPwd(user.getUsername(), userEntity.getNewPassword(), "密码不能与用户名一致");

        if (StringUtils.isNotEmpty(userEntity.getPassword())) {
            password = new Sha256Hash(userEntity.getPassword(), user.getSalt()).toHex();
        }
        String newPassword = new Sha256Hash(userEntity.getNewPassword(), user.getSalt()).toHex(); //getUser().getSalt()).toHex()
        //更新密码
        int code = sysUserService.updatePassword(user.getUserId(), password, newPassword);
        switch (code) {
            case ErrorCode.code_01:
                return R.error(2000, "登陆密码与原登陆密码相同,请重新输入!");
            case ErrorCode.code_02:
                return R.error(2000, "登陆密码与安全密码相同,请重新输入!");
            case ErrorCode.code_05:
                return R.error(2000, "原密码错误");
            default:
                mbrAccountLogService.updateSysUserPwdLog(user, getUser().getUsername(), CommonUtil.getIpAddress(request));
                return R.ok();
        }
    }

    /**
     * 修改登录用户安全密码
     */
//	@SysLog("修改安全密码")
    @RequestMapping("/secpassword")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R secpassword(@RequestBody SysUserEntity userEntity, HttpServletRequest request) {
        Assert.isNull(userEntity, "参数对象不能为空");
        Assert.isBlank(userEntity.getNewPassword(), "新安全密码不为能空!");
        Assert.isPwdCharacter(userEntity.getNewPassword(), "安全密码必须包含数字和字母，6-20个字符");
        Assert.checkSequentialSameChars(userEntity.getNewPassword(), "安全密码不允许出现连续的3个相同字符");
        Assert.checkLateralKeyboardSite(userEntity.getNewPassword(), "安全密码不允许出现连续的3个相邻字符");
        SysUserEntity user;                     // 被修改对象
        if(null == userEntity.getUserId()){     // 修改自己的安全密码
            user = sysUserService.queryUserEntityOne(getUser().getUserId());
        } else {                                // 修改别人的安全密码
            user = sysUserService.queryUserEntityOne(userEntity.getUserId());
        }

        Assert.accEqualPwd(user.getUsername(), userEntity.getNewPassword(), "安全密码不能与用户名一致");

        // 获取加密后的密码
        String newPassword = new Sha256Hash(userEntity.getNewPassword(), user.getSalt()).toHex();   // 新的安全密码
        //更新密码
        int code = sysUserService.updateSecPassword(user.getUserId(), newPassword);
        switch (code) {
            case ErrorCode.code_03:
                return R.error(2000, "安全密码与原安全密码相同,请重新输入!");
            case ErrorCode.code_04:
                return R.error(2000, "安全密码与登陆密码相同,请重新输入!");
            default:
                mbrAccountLogService.updateSysUserSafePwdLog(user, getUser().getUsername(), CommonUtil.getIpAddress(request));
                return R.ok();
        }
    }

    /**
     * 用户信息
     */
    @RequestMapping("/info/{userId}")
    @RequiresPermissions("sys:user:info")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("userId") Long userId) {
        SysUserEntity user = sysUserService.queryUserEntityOne(userId);
        //获取用户所属的角色列表
        List<Long> roleIdList = sysUserRoleService.queryRoleIdList(userId);
        if (roleIdList.size() == 1) {
            user.setRoleId(Integer.valueOf(roleIdList.get(0).toString()));
        }
        user.setRoleIdList(roleIdList);
        user.setPassword("******");
        user.setSecurepwd("******");
        return R.ok().put("user", user);
    }

    /**
     * 保存用户
     */
//	@SysLog("保存用户")
    @RequestMapping("/save")
    @RequiresPermissions(value = {"agent:account:save", "agent:account:agentSave"}, logical = Logical.OR)
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R save(@RequestBody SysUserEntity user, HttpServletRequest request) {
        if (user.getRoleId() == 1) {
            return R.error(2000, "不可赋予超级管理员权限");
        }
        ValidatorUtils.validateEntity(user, AddGroup.class);
        ValidRegUtils.validUserRealName(user.getRealName(), SysSetting.SysValueConst.require);
        if (user.getPassword().equals(user.getSecurepwd())) {
            return R.error(2000, "密码与安全密码相同,请重新输入!");
        }
        user.setCreateUserId(getUserId());
        sysUserService.save(user, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @RequestMapping("/updateAuth")
    @RequiresPermissions("sys:user:save")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAuth(@RequestBody SysUserEntity user, HttpServletRequest request) {
        if (user.getRoleId() == 1 && user.getUserId() == 1L) {
            return R.error(2000, "超级管理员不可修改,请联系客服");
        }
        //更新權限
        sysUserService.updateDataAuth(user, CommonUtil.getSiteCode());
        return R.ok();
    }

    /**
     * 修改用户
     */
//	@SysLog("修改用户")
    @RequestMapping("/update")
    @RequiresPermissions("sys:user:update")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody SysUserEntity user, HttpServletRequest request) {
        if (user.getRoleId() == 1 && user.getUserId() == 1L) {
            return R.error(2000, "超级管理员不可修改,请联系客服");
        }
        if (user.getUserId().equals(getUserId())) {
            return R.error(2000, "自己不能修改自己的数据权限");
        }
        user.setCreateUserId(getUserId());
        sysUserService.update(user, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    //	@SysLog("修改用户状态")
    @PostMapping("/updateEnable")
    @RequiresPermissions("sys:user:available")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateEnable(@RequestBody SysUserEntity user, HttpServletRequest request) {
        if (user.getRoleId() == 1 && user.getUserId() == 1L) {
            return R.error(2000, "超级管理员不可修改,请联系客服");
        }
        sysUserService.updateEnable(user, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    /**
     * 删除用户
     */
//	@SysLog("删除用户")
    @RequestMapping("/delete")
    @RequiresPermissions("sys:user:delete")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R delete(@RequestBody SysUserEntity user, HttpServletRequest request) {
        if (user.getUserId() == 1L) {
            return R.error(2000, "系统管理员不能删除");
        }
        if (user.getUserId().equals(getUserId())) {
            return R.error(2000, "当前用户不能删除");
        }
        sysUserRoleService.delete(user.getUserId(), getUser().getUsername(), CommonUtil.getIpAddress(request));
        sysUserService.deleteCatchBatch(user.getUserId());
        return R.ok();
    }

    @GetMapping("/userAuth/{userId}")
    @ResponseBody
    @ApiOperation(value = "获取角色数据权限", notes = "获取角色数据权限")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getUserAuth(@ApiParam @PathVariable("userId") Long userId, HttpServletRequest request) {
        if (sysUserService.queryObject(userId).getRoleId() == 1 && userId == 1L) {
            return R.error(2000, "超级管理员不可修改,请联系客服");
        }
        return R.ok().put("authority", sysUserService.getUserAuth(userId, CommonUtil.getSiteCode()));
    }

    @GetMapping("/depotLogOut")
    @ApiOperation(value = "第三方平台登出", notes = "第三方平台登出")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"), @ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R ptLogOut(@RequestParam("depotId") Integer depotId, @RequestParam("userId") Integer userId, HttpServletRequest request) {
        return depotOperatService.LoginOut(depotId, userId, CommonUtil.getSiteCode());
    }


    public static final String EXPORT_USER_LIST_REPORT = "EXPORT_USER_LIST_REPORT";

    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;

    @GetMapping("/checkFile")
    @ApiOperation(value = "查询文件是否可下载", notes = "查询文件是否可下载")
    public R checkFile(@RequestParam("module") String module, HttpServletRequest request) {
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
            if(SysUserController.EXPORT_USER_LIST_REPORT.equals(module)){
                record.setDownloadFileName("系统管理员.xls");
            }
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    @GetMapping("/exportUserList")
    @RequiresPermissions("sys:user:list")
    @ApiOperation(value = "导出系统用户", notes = "导出系统用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R exportUserList(@ModelAttribute SysUserEntity userEntity) {
        SysUserEntity sysUserEntity = getUser();
        if (sysUserEntity == null) {
            return R.error(2000, "无法获取当前用户信息");
        }
        log.info(sysUserEntity.toString());
        if (sysUserEntity.getRoleId() == null) {
            sysUserEntity = sysUserDao.queryObject(sysUserEntity.getUserId());
            //return R.error(2000,"无法获取当前用户角色信息");
        }
        //只有超级管理员，才能查看所有管理员列表
        if (sysUserEntity.getRoleId() != 1 && sysUserEntity.getUserId() != null && sysUserEntity.getUserId() != 1) {
            userEntity.setCreateUserId(getUserId());
        } else {
            userEntity.setRoleId(1);
        }
        userEntity.setIsDelete(1);

        sysUserService.exportUserList(userEntity, sysUserEntity.getUserId(), SysUserController.EXPORT_USER_LIST_REPORT);

        return R.ok();
    }

}
