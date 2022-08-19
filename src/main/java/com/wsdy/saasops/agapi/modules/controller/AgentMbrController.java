package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.IpUtils;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Objects;


@RestController
@RequestMapping("/agapi/mbr")
@Api(tags = "代理会员")
public class AgentMbrController extends AbstractController {

    @Autowired
    private MbrAccountService mbrAccountService;

    @Autowired
    private AgentService agentService;

    @GetMapping("/list")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    @ApiOperation(value = "会员信息-列表", notes = "根据当前页及每页笔数显示")
    public R list(@ModelAttribute MbrAccount mbrAccount,
                  @RequestParam("pageNo") @NotNull Integer pageNo,
                  @RequestParam("pageSize") @NotNull Integer pageSize,
                  @RequestParam(value = "orderBy", required = true) String orderBy) {
        PageUtils page = mbrAccountService.queryAgentMbrListPage(mbrAccount, pageNo, pageSize, orderBy);
        return R.ok().put("page", page);
    }

    @AgentLogin
    @GetMapping("addMbrAccount")
    @ApiOperation(value = "代理新增会员", notes = "代理新增会员")
    public R addMbrAccount(@ModelAttribute MbrAccount account, HttpServletRequest request) {
        Assert.isNull(account.getCagencyId(), "所属代理不能为空");
        Assert.isNull(account.getTagencyId(), "会员总代不能为空");
        ValidRegUtils.validloginName(account, SysSetting.SysValueConst.require);
        ValidRegUtils.validPwd(account, SysSetting.SysValueConst.require);
        ValidRegUtils.validRealName(account, SysSetting.SysValueConst.visible);
        account.setLoginName(account.getLoginName().toLowerCase());
        account.setLoginIp(CommonUtil.getIpAddress(request));
        account.setRegisterUrl(IpUtils.getUrl(request));
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        // 代理后台添加会员，添加关联会员组逻辑
        if (Objects.nonNull(agentAccount.getGroupId())) {
            account.setGroupId(agentAccount.getGroupId());
        }
        agentService.addMbrAccount(account, agentAccount);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("accountAuditDetail")
    @ApiOperation(value = "代理会员调整明细", notes = "代理会员调整明细")
    public R accountAuditDetail(@RequestParam("loginName") @NotNull String loginName,
                                @RequestParam("date") @NotNull String date,
                                @RequestParam("pageNo") @NotNull Integer pageNo,
                                @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(agentService.accountAuditDetail(loginName, date, pageNo, pageSize));
    }


}
