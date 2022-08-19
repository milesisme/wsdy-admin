package com.wsdy.saasops.modules.member.controller;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.IpUtils;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.fund.service.AgentTypeService;
import com.wsdy.saasops.modules.mbrRebateAgent.service.MbrRebateAgentLevelService;
import com.wsdy.saasops.modules.member.dto.BatchUpdateActLevelDto;
import com.wsdy.saasops.modules.member.dto.WaterDepotDto;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.member.service.*;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import com.wsdy.saasops.modules.sys.entity.SysLogMonitorEntity;
import com.wsdy.saasops.modules.sys.service.SysFileExportRecordService;
import com.wsdy.saasops.modules.sys.service.SysLogMonitorService;
import com.wsdy.saasops.modules.system.systemsetting.dto.StationSet;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@RestController
@RequestMapping("/bkapi/member/mbraccount")
@Api(value = "MbrAccount", tags = "会员信息")
public class MbrAccountController extends AbstractController {

    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private SysFileExportRecordService sysFileExportRecordService;
    @Value("${mbr.account.excel.path}")
    private String mbrAccountExcelTempPath;
    private final String module = "mbrAccount";
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysLogMonitorService sysLogMonitorService;
    @Autowired
    private MbrCryptoCurrenciesService mbrCryptoCurrenciesService;
    @Autowired
    private MbrRebateAgentLevelService mbrRebateAgentLevelService;
    @Autowired
    private AccountWaterSettlementService waterSettlementService;
    @Autowired
    private AgentTypeService agentTypeService;

    @GetMapping("/list")
    @RequiresPermissions(value = {"member:mbraccount:list", "member:mbraccount:info"}, logical = Logical.OR)
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @ApiOperation(value = "会员信息-列表", notes = "根据当前页及每页笔数显示")
    public R list(@ModelAttribute MbrAccount mbrAccount,
                  @RequestParam("pageNo") @NotNull Integer pageNo,
                  @RequestParam("pageSize") @NotNull Integer pageSize,
                  @RequestParam(value = "orderBy", required = false) String orderBy) {
        PageUtils page = mbrAccountService.queryListPage(mbrAccount, pageNo, pageSize, orderBy, getUser().getRoleId());
        return R.ok().put("page", page);
    }

    @GetMapping("/countOnline")
    @RequiresPermissions(value = {"member:mbraccount:list", "member:mbraccount:info"}, logical = Logical.OR)
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @ApiOperation(value = "会员信息-在线人数", notes = "根据当前页及每页笔数显示")
    public R countOnline() {
            Integer count  = mbrAccountService.countOnline();
        return R.ok().put("count", count);
    }

    @GetMapping("/seachList")
    @RequiresPermissions(value = {"member:mbraccount:list", "member:mbraccount:info"}, logical = Logical.OR)
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @ApiOperation(value = "查询会员列表展示列", notes = "根据当前页及每页笔数显示")
    public R seachList(@RequestParam("typeId") Long typeId) {
        Assert.isNull(typeId, "参数不能为空");
        return R.ok().put("items", mbrAccountService.querySeachCondition(getUser().getRoleId(), typeId));
    }

    @GetMapping("/columnFrameList")
    @RequiresPermissions("member:mbraccount:list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @ApiOperation(value = "查询会员列表展示列", notes = "根据当前页及每页笔数显示")
    public R columnList() {
        return R.ok().put("items", mbrAccountService.columnFrameList(getUser().getRoleId()));
    }

    @GetMapping("/listOnline")
    @RequiresPermissions("member:mbraccount:listOnline")
    @ApiOperation(value = "在线会员信息-列表", notes = "根据当前页及每页笔数显示")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R listOnline(@ModelAttribute MbrAccountOnline mbrAccountOnline,
                        @RequestParam("pageNo") @NotNull Integer pageNo,
                        @RequestParam("pageSize") @NotNull Integer pageSize,
                        @RequestParam(value = "orderBy", required = false) String orderBy) {
        mbrAccountOnline.setIsOnline(Available.enable);
        mbrAccountOnline.setCagencyIdList(agentTypeService.checkAgentType(mbrAccountOnline.getCagencyIdList()));
        return R.ok().put("page", mbrAccountService.queryListOnlinePage(mbrAccountOnline, pageNo, pageSize, orderBy));
    }

    @PostMapping("/changeMbrGroup")
    @RequiresPermissions("member:mbraccount:changeMbrGroup")
    @ApiOperation(value = "切换会员组", notes = "切换会员组")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R changeMbrGroup(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        Assert.isNotEmpty(mbrAccount.getAccountIds(), "请选择会员");
        Assert.isNull(mbrAccount.getGroupId(), "请选择会员组");
        Assert.isNull(mbrAccount.getMemo(), "请输入备注");
        return R.ok().put(mbrAccountService.changeMbrGroup(mbrAccount, getUser().getUsername(), CommonUtil.getIpAddress(request)));
    }


    @GetMapping("/view/{id}")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-详细信息", notes = "根据会员Id,显示会员详细信息(包含 登陆信息、会员资料 其他资料等)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R view(@PathVariable("id") Integer id) {
        MbrAccount mbrAccount = mbrAccountService.queryObject(id);
        if (isNull(mbrAccount)) {
            return R.ok().put("betTotal", null).put("fundTotal", null);
        }
        return R.ok().put("betTotal", analysisService.getMbrBetListReport(CommonUtil.getSiteCode(),
                mbrAccount.getLoginName(), null, null, null, getUser().getRoleId()))
                .put("fundTotal", mbrAccountService.findMbrTotal(mbrAccount.getId(), getUser().getRoleId()));
    }

    @GetMapping("/viewAccountInfo/{id}")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-会员资料", notes = "会员信息-会员资料")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R viewAccountInfo(@PathVariable("id") Integer id) {
        MbrAccount mbrAccount = mbrAccountService.viewAccount(getUser().getRoleId(), getUserId(), id, CommonUtil.getSiteCode());
        if (!isNull(mbrAccount)) {
            StationSet stationSet = sysSettingService.queryStationSet();
            mbrAccount.setWebsiteTitle(stationSet.getWebsiteTitle());
        }
        // 统计会员银行卡和钱包数量
        Integer sum = mbrCryptoCurrenciesService.qryBankAndWalletSumById(id);
        return R.ok().put("mbr", mbrAccount).put("mbrcard",
                Optional.ofNullable(mbrBankcardService.findMemberCardOne(id)).orElse(new MbrBankcard())).put("sum",sum);
    }


    @GetMapping("/viewOther/{id}")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-详细信息", notes = "根据会员Id,显示会员详细信息(包含 登陆信息IP、注册信息、银行卡信息)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R viewOther(@PathVariable("id") Integer id) {
        MbrAccount mbrAccount = mbrAccountService.viewOtherAccount(getUser().getRoleId(), getUserId(), id);
        return R.ok().put("other", mbrAccount);
    }

    @GetMapping("/viewData/{id}")
    @ApiOperation(value = "会员信息-其他", notes = "会员信息-其他-代理")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R viewData(@PathVariable("id") Integer id) {
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(id);
        return R.ok().put("viewData", mbrAccount);
    }

    @GetMapping("/chkUser")
    @ApiOperation(value = "会员接口-账号检测", notes = "根据会员账号检测账号是否存在，存在msg为真，不存在msg为假!")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R chkUser(@RequestParam("loginName") String loginName) {
        Assert.isBlank(loginName, "用户名不能为空");
        MbrAccount entity = new MbrAccount();
        entity.setLoginName(loginName);
        int count = mbrAccountService.selectCount(entity);
        return R.ok(count > 0 ? Boolean.TRUE : Boolean.FALSE);
    }

    @PostMapping("/save")
    @RequiresPermissions(value = {"member:mbraccount:save", "agent:account:addmbr"}, logical = Logical.OR)
    @ApiOperation(value = "会员信息-保存", notes = "保存会员基本信息到资料库")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "新增会员")
    public R save(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        ValidRegUtils.validloginName(mbrAccount, SysSetting.SysValueConst.require);
        mbrAccount.setLoginName(mbrAccount.getLoginName().toLowerCase());
        ValidRegUtils.validPwd(mbrAccount, SysSetting.SysValueConst.require);
        ValidRegUtils.validRealName(mbrAccount, SysSetting.SysValueConst.visible);
        ValidRegUtils.validPhone(mbrAccount, SysSetting.SysValueConst.visible);
        Assert.isLenght(mbrAccount.getMemo(), "备注最大长度为", 0, 200);
        Assert.isNull(mbrAccount.getCagencyId(), "请选择代理");
        mbrAccount.setLoginIp(CommonUtil.getIpAddress(request));
        mbrAccount.setRegisterUrl(IpUtils.getUrl(request));
        mbrAccountService.adminSave(mbrAccount, null, getUser().getUsername(), CommonUtil.getIpAddress(request), Boolean.TRUE, Constants.EVNumber.one);
        return R.ok();
    }

    @PostMapping("/update")
//    @RequiresPermissions("member:mbraccount:update")
    @ApiOperation(value = "会员信息-更新会员资料", notes = "会员信息-更新会员资料")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "更新会员基本信息")
    public R update(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        MbrAccount account = new MbrAccount();
        account.setId(mbrAccount.getId());
        account.setRealName(mbrAccount.getRealName());
        account.setQq(mbrAccount.getQq());
        account.setWeChat(mbrAccount.getWeChat());
        account.setEmail(mbrAccount.getEmail());
        account.setMobile(mbrAccount.getMobile());
        account.setGender(mbrAccount.getGender());
        account.setBirthday(mbrAccount.getBirthday());
        if(StringUtils.isEmpty(mbrAccount.getMobileAreaCode())){
            mbrAccount.setMobileAreaCode("86");     // 默认中国区号
        }
        account.setMobileAreaCode(mbrAccount.getMobileAreaCode());
        mbrAccountService.updateMbrAccount(account, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping("/loginLockUpdate")
    @RequiresPermissions("member:mbraccount:loginLockUpdate")
    @ApiOperation(value = "会员详情-账户资料-修改登录锁定状态", notes = "会员详情-账户资料-修改登录锁定状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "修改登录锁定状态")
    public R loginLockUpdate(@ModelAttribute MbrAccount mbrAccount, HttpServletRequest request) {
        Assert.isNull(mbrAccount.getId(),"id不为空");
        Assert.isNull(mbrAccount.getLoginLock(),"loginLock不为空");
        mbrAccountService.loginLockUpdate(mbrAccount, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/updateAccountRest")
//    @RequiresPermissions("member:mbraccount:update")
    @ApiOperation(value = "会员信息-会员其他资料修改", notes = "会员其他资料修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "会员信息-会员其他资料修改")
    public R updateAccountRest(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
//        Assert.isNull(mbrAccount.getAvailable(), "状态不能为空");
        Assert.isNull(mbrAccount.getId(), "会员ID不能为空");
//        Assert.isNull(mbrAccount.getGroupId(), "会员组不能为空");
//        Assert.isNull(mbrAccount.getCagencyId(), "代理不能为空");
//        Assert.isNull(mbrAccount.getActLevelId(), "会员星级不能为空");
        BizEvent bizEvent = new BizEvent(this, CommonUtil.getSiteCode(), mbrAccount.getId(), null);
        mbrAccountService.updateAccountRest(mbrAccount, bizEvent, getUser().getUsername(), CommonUtil.getIpAddress(request));
        if (Objects.nonNull(bizEvent.getEventType())) {
            applicationEventPublisher.publishEvent(bizEvent);
        }
        return R.ok();
    }

    @PostMapping("/updateDepositLockStatus")
//    @RequiresPermissions("member:mbraccount:update")
    @ApiOperation(value = "会员详情--风控信息-存款锁定修改", notes = "会员详情--风控信息-存款锁定修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
//    @SysLog(module = "会员详情", methodText = "会员详情--风控信息-存款锁定修改")
    public R updateDepositLockStatus(@RequestBody MbrAccount mbrAccount) {
        Assert.isNull(mbrAccount.getId(), "会员ID不能为空");
        Assert.isNull(mbrAccount.getDepositLock(), "depositLock不能为空");

        mbrAccountService.updateDepositLockStatus(mbrAccount);
        return R.ok();
    }

    @PostMapping("/kickLine")
    @RequiresPermissions("member:mbraccount:kickLine")
    @ApiOperation(value = "会员信息-会员踢线", notes = "根据会员Id强制让会员下线")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "会员踢线")
    public R kickLine(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        mbrAccountService.kickLine(mbrAccount.getId());
        applicationEventPublisher.publishEvent(new BizEvent(this, CommonUtil.getSiteCode(), mbrAccount.getId(), BizEventType.FORCE_LOGOUT));
        mbrAccountLogService.accountKickLine(mbrAccount, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping("/updateLevel")
    @RequiresPermissions("member:activitylevel:update")
    @ApiOperation(value = "会员信息-修改活动等级", notes = "修改活动等级")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "修改活动等级")
    public R updateActivityLevel(@RequestBody MbrAccount mbrAccount) {
        Assert.isNull(mbrAccount.getIsActivityLock(), "状态不能为空");
        Assert.isNull(mbrAccount.getId(), "会员ID不能为空");
        mbrAccountService.updateActivityLevel(mbrAccount);
        return R.ok();
    }

    @PostMapping("/updateMbrAgentLevel")
    @RequiresPermissions("member:agentlevel:update")
    @ApiOperation(value = "会员信息-修改会员代理等级", notes = "修改会员代理等级")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "修改会员代理等级")
    public R updateMbrAgentLevel(@RequestBody MbrAccount mbrAccount) {
        Assert.isNull(mbrAccount.getAgyLevelId(), "代理会员id不能为空");
        Assert.isNull(mbrAccount.getId(), "会员ID不能为空");
        mbrRebateAgentLevelService.updateMbrAgentLevel(mbrAccount);
        return R.ok();
    }

    /**
     * 会员密码修改
     *
     * @return
     */
    @PostMapping("/pwdUpdate")
    @RequiresPermissions("member:mbraccount:pwdUpdate")
    @ApiOperation(value = "会员信息-会员登陆密码修改", notes = "根据会员Id修改会员密码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "修改会员登陆密码")
    public R pwdUpdate(@RequestBody MbrAccount mbraccModel, HttpServletRequest request) {
        Assert.isNull(mbraccModel.getId(), "会员ID不能为空");
        MbrAccount info = mbrAccountService.getAccountInfo(mbraccModel.getId());
        mbraccModel.setLoginName(info.getLoginName());
        ValidRegUtils.validPwd(mbraccModel, SysSetting.SysValueConst.visible);
        String salt = info.getSalt();
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setLoginPwd(new Sha256Hash(mbraccModel.getLoginPwd(), salt).toHex());
        mbrAccount.setId(mbraccModel.getId());

        mbrAccountService.update(mbrAccount);
        applicationEventPublisher.publishEvent(new BizEvent(this,
                CommonUtil.getSiteCode(), mbrAccount.getId(), BizEventType.MEMBER_MODIFY_PWD));
        mbrAccountLogService.updateAccountLoginPwd(mbrAccount, getUser().getUsername(), CommonUtil.getIpAddress(request));

        redisService.del(RedisConstants.REDIS_USER_LOGIN + info.getLoginName().toLowerCase() + "_" + CommonUtil.getSiteCode());
        redisService.del(RedisConstants.REDIS_MOBILE_LOGIN + info.getMobile() + "_" + CommonUtil.getSiteCode());
        return R.ok();
    }

    /**
     * 会员提款密码修改
     *
     * @return
     */
    @PostMapping("/secPwdUpdate")
    @RequiresPermissions("member:mbraccount:secPwdUpdate")
    @ApiOperation(value = "会员信息-会员提款密码修改", notes = "根据会员Id修改会员提款密码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "修改会员提款密码")
    public R secPwdUpdate(@RequestBody MbrAccount mbraccModel, HttpServletRequest request) {
        ValidRegUtils.validPwd(mbraccModel, SysSetting.SysValueConst.visible);
        MbrAccount info = mbrAccountService.getAccountInfo(mbraccModel.getId());
        String salt = info.getSalt();
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setSecurePwd(new Sha256Hash(mbraccModel.getSecurePwd(), salt).toHex());
        mbrAccount.setId(mbraccModel.getId());
        mbrAccountService.update(mbrAccount);
        mbrAccountLogService.updateAccountDrawPwd(mbrAccount, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    /**
     * 会员组修改
     *
     * @return
     */
    @PostMapping("/groupIdUpdate")
    @RequiresPermissions("member:mbraccount:groupIdUpdate")
    @ApiOperation(value = "会员信息-修改会员组信息", notes = "根据会员Id修改会员组信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "修改会员所属组")
    public R groupIdUpdate(@RequestBody MbrAccount mbraccModel) {
        mbrAccountService.updateGroupBatch(mbraccModel.getIds(), mbraccModel.getGroupId());
        return R.ok();
    }

    /**
     * 会员状态
     *
     * @return
     */
    @PostMapping("/avlUpdate")
    @RequiresPermissions("member:mbraccount:avlUpdate")
    @ApiOperation(value = "会员信息-修改会员状态", notes = "根据会员Id修改会员状态")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "修改会员状态")
    public R availableUpdate(@RequestBody MbrAccount mbraccModel, HttpServletRequest request) {
        Assert.isNull(mbraccModel.getIds(), "会员id不能为空");
        Assert.isNull(mbraccModel.getAvailable(), "修改的状态不能为空");
        for (int i = 0; i < mbraccModel.getIds().length; i++) {
            MbrAccount mbrAccount = mbrAccountService.updateAvailable(mbraccModel.getIds()[i],
                    mbraccModel.getAvailable(), getUser().getUsername(), CommonUtil.getIpAddress(request));
            if (mbraccModel.getAvailable().compareTo(new Byte("0")) == 0) {
                applicationEventPublisher.publishEvent(new BizEvent(this,
                        CommonUtil.getSiteCode(), mbrAccount.getId(), BizEventType.MEMBER_ACCOUNT_FREEZE));
            } else if (mbraccModel.getAvailable().compareTo(new Byte("2")) == 0) {
                applicationEventPublisher.publishEvent(new BizEvent(this,
                        CommonUtil.getSiteCode(), mbrAccount.getId(), BizEventType.MEMBER_WITHDRAWAL_REFUSE));
            }
        }
        return R.ok();
    }

    @GetMapping("/queryAccountAuditList")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-风控审核数据列表", notes = "会员信息-风控审核数据列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R queryAccountAuditList(@RequestParam("accountId") Integer accountId) {
        Assert.isNull(accountId, "会员id不能为空");
        return R.ok().put(mbrAccountService.queryAccountAuditList(accountId));
    }

    @GetMapping("/queryAccountAuditInfo")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-风控审核数据列表明细", notes = "会员信息-风控审核数据列表明细")
    public R queryAccountAuditInfo(@RequestParam("accountId") Integer accountId,
                                   @RequestParam("keys") String keys,
                                   @RequestParam("item") String item,
                                   @RequestParam("pageNo") @NotNull Integer pageNo,
                                   @RequestParam("pageSize") @NotNull Integer pageSize) {
        Assert.isNull(accountId, "会员id不能为空");
        return R.ok().put(mbrAccountService.queryAccountAuditInfo(accountId, keys, item, pageNo, pageSize));
    }

    @Deprecated
    @GetMapping("/queryAccountBonusRepor")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-输赢报表数据列表", notes = "会员信息-输赢报表数据列表")
    public R queryAccountBonusRepor(@RequestParam("accountId") Integer accountId) {
        Assert.isNull(accountId, "会员id不能为空!");
        return R.ok().put(mbrAccountService.queryAccountBonusReporList(accountId));
    }

    @GetMapping("/bonusList")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-红利记录", notes = "会员信息-红利记录")
    public R bonusList(@RequestParam("accountId") Integer accountId,
                       @RequestParam("pageNo") @NotNull Integer pageNo,
                       @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(mbrAccountService.bonusList(accountId, pageNo, pageSize));
    }

    @GetMapping("/withdrawList")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-提款记录", notes = "会员信息-提款记录")
    public R withdrawList(@RequestParam("accountId") Integer accountId,
                          @RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(mbrAccountService.withdrawList(accountId, pageNo, pageSize));
    }

    @GetMapping("/depositList")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-存款记录", notes = "会员信息-存款记录")
    public R depositList(@RequestParam("accountId") Integer accountId,
                         @RequestParam("pageNo") @NotNull Integer pageNo,
                         @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(mbrAccountService.depositList(accountId, pageNo, pageSize));
    }

    @GetMapping("/manageList")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-转账记录", notes = "会员信息-转账记录")
    public R manageList(@RequestParam("accountId") Integer accountId,
                        @RequestParam("pageNo") @NotNull Integer pageNo,
                        @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(mbrAccountService.manageList(accountId, pageNo, pageSize));
    }

    @GetMapping("/fundList")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-资金流水", notes = "会员信息-资金流水")
    public R fundList(@RequestParam("accountId") Integer accountId,
                      @RequestParam("pageNo") @NotNull Integer pageNo,
                      @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(mbrAccountService.fundList(accountId, pageNo, pageSize));
    }

    @GetMapping("/accountLogList")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-资料变更", notes = "会员信息-资料变更")
    public R accountLogList(@RequestParam("accountId") Integer accountId,
                            @RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(mbrAccountService.accountLogList(accountId, pageNo, pageSize, getUserId()));
    }

    @GetMapping("/taskList")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-任务返利", notes = "会员信息-任务返利")
    public R taskList(@RequestParam("accountId") Integer accountId,
                      @RequestParam("pageNo") @NotNull Integer pageNo,
                      @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(mbrAccountService.taskList(accountId, pageNo, pageSize));
    }

    @GetMapping("/findAccountByName")
    @ApiOperation(value = "会员信息根据NAME查询", notes = "会员信息根据NAME查询")
    public R findAccountByName(@RequestParam("loginName") String loginName) {
        return R.ok().put(mbrAccountService.findAccountByName(loginName));
    }

    @PostMapping("/findMbrLevelAndAgyInfo")
    @ApiOperation(value = "查询会员信息带有活动级别和代理", notes = "查询会员信息带有活动级别和代理")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R findMbrLevelAndAgyInfoByName(@RequestBody List<String> loginNames) {
        // return R.ok().put(mbrAccountService.findMbrLevelAndAgyInfoByName(loginName));
        return R.ok().put(mbrAccountService.findMbrLevelAndAgyInfoByLoginNames(loginNames));
    }

    @GetMapping("/findHomePageCount")
    @ApiOperation(value = "首页数据总览", notes = "首页数据总览")
    public R findHomePageCount() {
        return R.ok().put(mbrAccountService.findHomePageCount());
    }

    @GetMapping("/findHomePageCountEx")
    @ApiOperation(value = "首页数据总览", notes = "首页数据总览")
    public R findHomePageCountEx(@RequestParam("startTime") String startTime, @RequestParam("endTime") String endTime) {
        return R.ok().put(mbrAccountService.findHomePageCountEx(startTime,endTime));
    }

    @GetMapping("/accountQrCode")
    @ApiOperation(value = "会员详情查看二维码", notes = "会员详情查看二维码")
    public void accountQrCode(@RequestParam("accountId") Integer accountId, HttpServletResponse response) {
        Assert.isNull(accountId, "会员id不能为空");
        mbrAccountService.accountQrCode(accountId, response, CommonUtil.getSiteCode());
    }

    @GetMapping("/recommendAccounts")
    @ApiOperation(value = "会员详情查看推荐会员", notes = "会员详情查看推荐会员")
    public R recommendAccounts(@RequestParam("accountId") Integer accountId) {
        Assert.isNull(accountId, "会员id不能为空");
        return R.ok().put(mbrAccountService.recommendAccounts(accountId));
    }

    @GetMapping("findAccountOrAgentByName")
    @ApiOperation(value = "查询是会员还是代理", notes = "查询是会员还是代理")
    public R findAccountOrAgentByName(@RequestParam("username") String username) {
        Assert.isNull(username, "name不能为空");
        return R.ok().put(mbrAccountService.findAccountOrAgentByName(username));
    }

    @GetMapping("/findAccountOrAgentByNameEx")
    @ApiOperation(value = "查询是会员还是代理", notes = "查询是会员还是代理")
    public R findAccountOrAgentByNameEx(@RequestParam("username") String username,@RequestParam("flag") String flag) {
        Assert.isNull(username, "name不能为空");
        return R.ok().put(mbrAccountService.findAccountOrAgentByNameEx(username,flag));
    }

    @GetMapping("exportMbrAccountInfo")
    @RequiresPermissions(value = {"member:mbraccount:export"}, logical = Logical.OR)
    @ApiOperation(value = "导出会员信息", notes = "导出会员信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R exportMbrAccountInfo(@ModelAttribute MbrAccount mbrAccount) {
        log.info("exportMbrAccountInfo==request==" + getUser().getUsername());
        SysFileExportRecord record = mbrAccountService.exportMbrAccountInfo(mbrAccount, getUser(), module, mbrAccountExcelTempPath);
        log.info("exportMbrAccountInfo==record==" + Objects.nonNull(record));
        if (record == null) {
            throw new R200Exception("正在处理中，请稍后重试!");
        }
        return R.ok();
    }

    @GetMapping("checkFile")
    @ApiOperation(value = "查询文件是否可下载", notes = "查询文件是否可下载")
    public R checkFile() {
        Long userId = getUserId();
        SysFileExportRecord record = sysFileExportRecordService.getAsynFileExportRecordByUserId(userId, module);
        if (null != record) {
            String fileName = mbrAccountExcelTempPath.substring(mbrAccountExcelTempPath.lastIndexOf("/") + 1, mbrAccountExcelTempPath.length());
            record.setDownloadFileName(fileName);
            return R.ok().put(record);
        }
        return R.ok(false);
    }

    /**
     * 拨打电话
     *
     * @return
     */
    @GetMapping("/dial")
    @RequiresPermissions("member:mbraccount:call")
    @ApiOperation(value = "拨打会员电话", notes = "调用第三方接口拨打电话")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"), @ApiImplicitParam(name = "siteCode", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "拨打电话")
    public R dial(@RequestParam("accountId") Integer accountId) {
        Assert.isNull(accountId, "会员id不能为空");

        mbrAccountService.dial(accountId);
        return R.ok();
    }


    /**
     * 查询电话外拨记录
     *
     * @return
     */
    @GetMapping("/queryTelRecord")
    @RequiresPermissions("member:mbraccount:callrecord")
    @ApiOperation(value = "查询电话外拨记录", notes = "查询会员被拨打电话记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"), @ApiImplicitParam(name = "siteCode", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    @SysLog(module = "会员模块", methodText = "查询外拨电话记录")
    public R queryTelRecordByAccountId(@RequestParam("accountId") Integer accountId,
                                       @RequestParam("pageNo") Integer pageNo,
                                       @RequestParam("pageSize") Integer pageSize) {

        Assert.isNull(accountId, "会员id不能为空");
        Assert.isNull(pageNo, "当前页码不能为空");
        Assert.isNull(pageSize, "总分页数不能为空");
        return mbrAccountService.queryTelRecordByAccountId(accountId, pageNo, pageSize);
    }

    @PostMapping("/batchUpdateActLevel")
    @RequiresPermissions("member:mbraccount:batchUpdateActLevel")
    @ApiOperation(value = "批量调级", notes = "批量调级")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R batchUpdateActLevel(@RequestBody BatchUpdateActLevelDto dto) {
        Assert.isNull(dto.getNewLevelId(), "请选择调整至的目标等级");
        if (CollectionUtils.isEmpty(dto.getOldLevelIds()) && CollectionUtils.isEmpty(dto.getAccountIds())) {
            throw new R200Exception("请选择要调整的会员等级或会员");
        }
        if (CollectionUtils.isNotEmpty(dto.getOldLevelIds()) && CollectionUtils.isNotEmpty(dto.getAccountIds())) {
            throw new R200Exception("单次提交不能按等级和会员名同时调整等级");
        }
        return R.ok().put(mbrAccountService.batchUpdateActLevel(dto));
    }


    @GetMapping("/getLevelNotLockMbrList")
    @ApiOperation(value = "查询未锁定等级会员", notes = "查询未锁定等级会员")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getLevelNotLockMbrList() {
        return R.ok().put(mbrAccountService.getLevelNotLockMbrList());
    }


    @PostMapping("/modifyRebateRatio")
    @ApiOperation(value = "修改会员返利比例", notes = "修改会员返利比例")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R modifyRebateRatio(@RequestBody MbrAccount mbrAccount) {
        Assert.isNull(mbrAccount.getId(), "会员id不能为空");
        Assert.isPercent(mbrAccount.getRebateRatio(), "请输入正确的反水比例");
        mbrAccountService.modifyRebateRatio(mbrAccount);
        return R.ok();
    }

    @GetMapping("/massTexting")
    @ApiOperation(value = "会员群发短信", notes = "会员群发短信")
    @RequiresPermissions("member:mbraccount:massTexting")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R accountMassTexting(@ModelAttribute MbrAccount account) {
        Assert.isBlank(account.getContent(), "发送内容不能为空");
        Assert.isLenght(account.getContent(), "发送内容最大长度为1000", 0, 1000);
        // 群发配置
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.MASS_TEXT_FLAG);
        if ((Objects.nonNull(setting) && Objects.nonNull(setting.getSysvalue())
                && String.valueOf(Constants.EVNumber.zero).equals(setting.getSysvalue()))
                || isNull(setting)) {
            return R.error("该站点未配置群发功能！");
        }
        String key = RedisConstants.ACCOUNT_MESSAGE_MASS + CommonUtil.getSiteCode() + getUser().getUserId();
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, getUser().getUserId(), 2, TimeUnit.MINUTES);
        if (isExpired) {
            mbrAccountService.accountMassTexting(account, CommonUtil.getSiteCode());
        } else {
            return R.error("正在群发，请耐心等候，请勿重复操作...");
        }

        return R.ok("正在群发，请耐心等候...");
    }

    @PostMapping("updateAccountAgent")
    @ApiOperation(value = "会员修改代理", notes = "会员修改代理")
    @RequiresPermissions("member:mbraccount:updateAccountAgent")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateAccountAgent(@RequestBody MbrAccount account, HttpServletRequest request) {
        if (isNull(account.getCagencyId()) && StringUtils.isEmpty(account.getSupLoginName())) {
            throw new R200Exception("请选择代理或者上级会员");
        }
        Assert.isNull(account.getId(), "会员不能为空");
        mbrAccountService.updateAccountAgent(account.getCagencyId(), account.getId(), getUser().getUsername(),
                CommonUtil.getIpAddress(request), account.getSupLoginName());
        return R.ok();
    }

    @GetMapping("/massTextingCount")
    @ApiOperation(value = "会员群发短信统计", notes = "会员群发短信统计")
    @RequiresPermissions("member:mbraccount:massTexting")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R massTextingCount(@ModelAttribute MbrAccount account) {
        return R.ok(mbrAccountService.accountMassTextingCount(account));
    }

    @GetMapping("/chkUserInfo")
    @ApiOperation(value = "会员接口-账号检测", notes = "根据会员账号检测账号是否存在，存在msg为真，不存在msg为假!")
    @RequiresPermissions("member:mbraccount:chkUserInfo")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R chkUserInfo(@ModelAttribute MbrAccount mbrAccount, HttpServletRequest request) {
        Assert.isNull(mbrAccount.getId(), "id不能为空");
        String ip = CommonUtil.getIpAddress(request);
        MbrAccount account = mbrAccountService.checkUserInfo(mbrAccount, ip);
        return R.ok(account);
    }

    @GetMapping("accountAutoLog")
    @ApiOperation(value = "会员自动晋升日志", notes = "会员自动晋升日志")
    @RequiresPermissions("member:mbraccount:accountAutoLog")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R accountAutoLog(@ModelAttribute SysLogMonitorEntity sysLogMonitorEntity) {
        Assert.isNull(sysLogMonitorEntity.getAccountId(), "会员id不能为空");
        sysLogMonitorEntity.setModuleName(MbrAccountLog.ACCOUNT_AUTO);
        return R.ok().putPage(sysLogMonitorService.queryList(sysLogMonitorEntity));
    }

    @GetMapping("/getPromotionUrl")
    @ApiOperation(value = "查询当前会员推广链接", notes = "查询当前会员推广链接")
    public R getPromotionUrlById(@ModelAttribute MbrAccount account) {
        Assert.isNull(account.getId(), "会员id不能为空");
        MbrAccount mbrAccount = mbrAccountService.getPromotionUrl(account, CommonUtil.getSiteCode());
        return R.ok().put(mbrAccount);
    }

    @GetMapping("/mbrWaterRate/{id}")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-今天产生的返水金额", notes = "根据会员Id,显示今天产生的返水金额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R mbrWaterRate(@PathVariable("id") Integer id) {
        MbrAccount mbrAccount = mbrAccountService.queryObject(id);
        Assert.isNull(mbrAccount.getId(), "会员不能为空");
        List<WaterDepotDto> waterDepotDtoList = waterSettlementService.findAccountWaterRate(mbrAccount.getId(), CommonUtil.getSiteCode());
        BigDecimal minLimitAmount = waterSettlementService.findRuleRateLimit();

        Optional<BigDecimal> sumAmount = waterDepotDtoList.stream()
                .filter(p -> nonNull(p.getAmount()))
                .map(WaterDepotDto::getAmount).reduce(BigDecimal::add);

        return R.ok().put(waterDepotDtoList).put("sumAmount", sumAmount.isPresent() ? sumAmount.get() : BigDecimal.ZERO)
                .put("minLimitAmount", minLimitAmount);
    }

    @GetMapping("/mbrWaterRateYestday/{id}")
    @RequiresPermissions("member:mbraccount:info")
    @ApiOperation(value = "会员信息-今天待发放返水金额", notes = "根据会员Id,今天待发放返水金额")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R mbrWaterRateYestday(@PathVariable("id") Integer id) {
        MbrAccount mbrAccount = mbrAccountService.queryObject(id);
        Assert.isNull(mbrAccount.getId(), "会员不能为空");

        List<WaterDepotDto> waterDepotDtoList = waterSettlementService.findAccountWaterRateYestday(mbrAccount.getId(), CommonUtil.getSiteCode());
        BigDecimal minLimitAmount = waterSettlementService.findRuleRateLimit();

        Optional<BigDecimal> sumAmount = waterDepotDtoList.stream()
                .filter(p -> nonNull(p.getAmount()))
                .map(WaterDepotDto::getAmount).reduce(BigDecimal::add);

        return R.ok().put(waterDepotDtoList).put("sumAmount", sumAmount.isPresent() ? sumAmount.get() : BigDecimal.ZERO)
                .put("minLimitAmount", minLimitAmount);
    }

    @GetMapping("/accountMobileList/{id}")
    @RequiresPermissions("member:mbraccount:mobileHistory")
    @ApiOperation(value = "会员信息-绑定电话历史记录", notes = "会员信息-绑定电话历史记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R accountMobileList(@PathVariable("id") Integer id,@RequestParam("pageNo") @NotNull Integer pageNo,
                               @RequestParam("pageSize") @NotNull Integer pageSize,@RequestParam(value = "orderBy", required = false) String orderBy) {
        PageUtils page  = mbrAccountService.accountMobileList(id, pageNo, pageSize, orderBy);
        return R.ok().put("page", page);
    }
    
    @PostMapping("/checkUserNames")
    @RequiresPermissions("member:mbraccount:list")
    @ApiOperation(value = "查询多个用户名，不存在返回提示语，存在返回对应的用户id", notes = "查询多个用户名，不存在返回提示语，存在返回对应的用户id")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
    required = true, dataType = "String", paramType = "header")})
    public R checkUserNames(@RequestBody MbrAccount account) {
    	return R.ok(mbrAccountService.checkUserNames(account.getUserNames()));
    }

//    @GetMapping("/resetMobile/{id}")
//    @RequiresPermissions("member:mbraccount:mobileHistory")
//    @ApiOperation(value = "会员信息-绑定电话历史记录", notes = "会员信息-绑定电话历史记录")
//    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
//            required = true, dataType = "Integer", paramType = "header")})
//    public R resetMobile(@PathVariable("id") Integer id,@RequestParam("mobile") String mobile){
//        mbrAccountService.resetMobile(id,mobile);
//        return R.ok();
//    }
}
