package com.wsdy.saasops.agapi.modulesV2.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modulesV2.service.AgentV2AccountManageService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.IpUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyTree;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAccountOther;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Objects;

import static com.wsdy.saasops.common.utils.CommonUtil.getIpAddress;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;


@RestController
@Slf4j
@RequestMapping("/agapi/v2/actManage")
@Api(tags = "外围系统-账户管理")
public class AgentV2AccountManageController extends AbstractController {
    @Autowired
    private AgentV2AccountManageService accountService;

    @AgentLogin
    @GetMapping("/getSubAgentList")
    @ApiOperation(value = "下线管理-代理管理-获取下级代理列表", notes = "获取下级代理列表")
    public R getSubAgentList(@ModelAttribute AgentAccount agentAccount,
                            @RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(accountService.getSubAgentList(agentAccount, pageNo, pageSize));
    }

    @AgentLogin
    @GetMapping("/getSubAccountList")
    @ApiOperation(value = "下线管理-客户管理-获取下级会员列表", notes = "获取下级会员列表")
    public R getSubAccountList(@ModelAttribute AgentAccount agentAccount,
                             @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(accountService.getSubAccountList(agentAccount, pageNo, pageSize));
    }

    @AgentLogin
    @PostMapping("/agyAccountSave")
    @ApiOperation(value = "新增下级代理", notes = "下线管理-代理管理/新增代理")
    public R agyAccountSave(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        // 参数校验
        Assert.isBlank(agentAccount.getAgyAccount(), "帐号不能为空");
        Assert.isBlank(agentAccount.getRealName(), "别名不能为空");
        Assert.isBlank(agentAccount.getAgyPwd(), "密码不能为空");
        Assert.isNumeric(agentAccount.getRealpeople(), "真人占成不能为空，且只能在0-100之间！");
        Assert.isNumeric(agentAccount.getElectronic(), "电子占成不能为空，且只能在0-100之间！");
        Assert.isNumeric(agentAccount.getRealpeoplewash(), "真人洗码佣金不能为空，且只能在0-100之间！");
        Assert.isNumeric(agentAccount.getRealpeoplewash(), "电子洗码佣金不能为空，且只能在0-100之间！");
        ValidRegUtils.validAgentName(agentAccount.getAgyAccount(), SysSetting.SysValueConst.require);

        // 数据处理
        agentAccount.setIp(getIpAddress(request));
        agentAccount.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));

        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        agentAccount.setCreateUser(account.getAgyAccount());

        if(Objects.isNull(agentAccount.getParentId())){
            agentAccount.setParentId(account.getId());
        }

        accountService.agyAccountSave(agentAccount,account);
        return R.ok();
    }

    @AgentLogin
    @PostMapping("/addMbrAccount")
    @ApiOperation(value = "新增下级会员", notes = "下线管理-客户管理/客户管理")
    public R addMbrAccount(@RequestBody MbrAccount account, HttpServletRequest request) {
        // 参数校验
        Assert.isBlank(account.getLoginName(), "帐号不能为空");
        Assert.isBlank(account.getRealName(), "别名不能为空");
        Assert.isBlank(account.getLoginPwd(), "密码不能为空");
        Assert.isNumeric(account.getRealpeoplewash(), "真人洗码佣金不能为空，且只能在0-100之间！");
        Assert.isNumeric(account.getRealpeoplewash(), "电子洗码佣金不能为空，且只能在0-100之间！");
        ValidRegUtils.validloginName(account, SysSetting.SysValueConst.require);
        ValidRegUtils.validPwd(account, SysSetting.SysValueConst.require);

        // 数据处理
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        // 是否有送上级代理id
        if(Objects.isNull(account.getCagencyId())){
            // 代理校验  公司，股东，总代 leve2  判断是否为总代
            AgyTree agyTree = accountService.getAgentByDepth(agentAccount.getId(), Constants.EVNumber.two);
            if(Objects.isNull(agyTree)){
                throw new R200Exception("仅允许代理添加下级会员！");
            }
            agentAccount.setParentId(agyTree.getParentId());
        }else{
            // 代理校验  公司，股东，总代 leve2  判断是否为总代
            AgyTree agyTree = accountService.getAgentByDepth(account.getCagencyId(), Constants.EVNumber.two);
            if(Objects.isNull(agyTree)){
                throw new R200Exception("仅允许代理添加下级会员！");
            }
            agentAccount.setId(account.getCagencyId());
            agentAccount.setParentId(agyTree.getParentId());
        }

        account.setLoginName(account.getLoginName().toLowerCase());
        account.setLoginIp(CommonUtil.getIpAddress(request));
        account.setRegisterUrl(IpUtils.getUrl(request));

        accountService.addMbrAccount(account, agentAccount);
        return R.ok();
    }

    @AgentLogin
    @PostMapping("/agyAccountUpdate")
    @ApiOperation(value = "下线管理-代理管理-代理设定", notes = "代理列表代理设定")
    public R agyAccountUpdate(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        // 数据校验
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        // 数据处理
        AgentAccount loginAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);

        accountService.agyAccountUpdate(agentAccount, loginAccount);
        return R.ok();
    }

    @AgentLogin
    @PostMapping("/updateMbrAccount")
    @ApiOperation(value = "下线管理-客户管理-会员设定", notes = "设定下级会员")
    public R updateMbrAccount(@RequestBody MbrAccount account, HttpServletRequest request) {
        // 数据校验
        Assert.isNull(account.getId(), "会员d不能为空");

        // 数据处理
        AgentAccount loginAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);

        accountService.updateMbrAccount(account,loginAccount);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/updateAgentAvailable")
    @ApiOperation(value = "下线管理-代理管理-修改代理状态", notes = "修改代理状态")
    public R updateAgentAvailable(@ModelAttribute AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getAvailable(), "状态不能为空");
        agentAccount.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));

        // 数据处理
        AgentAccount loginAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);

        accountService.updateAgentAvailable(agentAccount, loginAccount);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/updateMbrAvailable")
    @ApiOperation(value = "下线管理-客户管理-修改会员状态", notes = "修改会员状态")
    public R updateMbrAvailable(@ModelAttribute MbrAccount account, HttpServletRequest request) {
        Assert.isNull(account.getId(), "会员id不能为空");
        Assert.isNull(account.getAvailable(), "状态不能为空");
        account.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));

        // 数据处理
        AgentAccount loginAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);

        accountService.updateMbrAvailable(account,loginAccount);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/updateMbrBettingStatus")
    @ApiOperation(value = "下线管理-客户管理-修改会员投注状态", notes = "修改会员投注状态")
    public R updateMbrBettingStatus(@ModelAttribute MbrAccountOther account, HttpServletRequest request) {
        Assert.isNull(account.getId(), "会员id不能为空");
        Assert.isNull(account.getBettingStatus(), "投注状态不能为空");

        // 数据处理
        AgentAccount loginAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        accountService.updateMbrBettingStatus(account,loginAccount);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("/updateAgentBettingStatus")
    @ApiOperation(value = "下线管理-代理管理-修改代理状态", notes = "修改代理投注状态")
    public R updateAgentBettingStatus(@ModelAttribute AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getBettingStatus(), "投注状态不能为空");

        // 数据处理
        AgentAccount loginAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        accountService.updateAgentBettingStatus(agentAccount,loginAccount);
        return R.ok();
    }

}
