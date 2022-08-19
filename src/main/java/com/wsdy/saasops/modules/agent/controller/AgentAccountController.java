package com.wsdy.saasops.modules.agent.controller;

import static com.wsdy.saasops.common.utils.CommonUtil.getIpAddress;
import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.agapi.modules.service.AgentService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.IpUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyBankcard;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


@RestController
@RequestMapping("/bkapi/agent/account")
@Api(tags = "总代设置,代理设置")
public class AgentAccountController extends AbstractController {

    @Autowired
    private AgentAccountService accountService;
    @Autowired
    private AgentService agentService;

    @GetMapping("sublineAgentList")
    @ApiOperation(value = "分线查询总代", notes = "分线查询总代")
    public R sublineAgentList(@ModelAttribute AgentAccount agentAccount) {
        // agentAccount.setAgentType(Constants.EVNumber.one);
        agentAccount.setIsSign(Boolean.FALSE);
        return R.ok().put(accountService.totalAgentList(agentAccount));
    }

    @GetMapping("superiorAgentList")
    @ApiOperation(value = "查询上级代理", notes = "分线查询总代")
    public R superiorAgentList(@ModelAttribute AgentAccount agentAccount) {
        return R.ok().put(accountService.totalAgentList(agentAccount));
    }

    @GetMapping("agyAccountReviewList")
    @RequiresPermissions("agent:review:reviewinfo")
    @ApiOperation(value = "代理列表审核-2.0", notes = "代理列表审核-2.0")
    public R agyAccountReviewList(@ModelAttribute AgentAccount agentAccount,
                                  @RequestParam("pageNo") @NotNull Integer pageNo,
                                  @RequestParam("pageSize") @NotNull Integer pageSize) {
        if (isNull(agentAccount.getStatus())) {
            agentAccount.setReviewStatus(Boolean.TRUE);
        }
        return R.ok().putPage(accountService.agyAccountReviewList(getUserId(), agentAccount, pageNo, pageSize));
    }

    @PostMapping("agentReview")
    @RequiresPermissions("agent:review:agentReview")
    @ApiOperation(value = "代理审核-2.0", notes = "代理审核-2.0")
    public R agentReview(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "ID不能为空");
        Assert.isNull(agentAccount.getStatus(), "状态不能为空");
        if (agentAccount.getStatus() != 0) {
            Assert.isNull(agentAccount.getParentId(), "上级代理不能为空");
            Assert.isNull(agentAccount.getContractId(), "契约不能为空");
            Assert.isBlank(agentAccount.getContractStart(), "契约开始时间不能为空");
            Assert.isBlank(agentAccount.getContractEnd(), "契约结束时间不能为空");
        }
        accountService.agentReview(agentAccount, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("agyAccountList")
    @RequiresPermissions("agent:account:info")
    @ApiOperation(value = "代理列表-2.0", notes = "代理列表-2.0")
    public R agyAccountList(@ModelAttribute AgentAccount agentAccount,
                            @RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().putPage(accountService.newAgyAccountList(getUserId(), agentAccount, pageNo, pageSize, getUser().getRoleId()));
    }

    @GetMapping("agyAccountInfo")
    @RequiresPermissions("agent:account:info")
    @ApiOperation(value = "代理列表查询单个-2.0", notes = "代理列表查询单个-2.0")
    public R agyAccountInfo(@ModelAttribute AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "ID不能为空");
        return R.ok().put(accountService.agyAccountInfo(getUserId(), agentAccount.getId()));
    }
    
    @GetMapping("/viewOther/{id}")
    @ApiOperation(value = "代理查询单独的权限", notes = "代理查询单独的权限")
    @RequiresPermissions("agent:account:info")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字",
            required = true, dataType = "Integer", paramType = "header")})
    public R viewOther(@PathVariable("id") Integer id) {
        return R.ok().put("other", accountService.viewOther(id, getUser().getRoleId()));
    }

    @PostMapping("saveEmployee")
    @RequiresPermissions("agent:account:saveEmployee")
    @ApiOperation(value = "员工新增-2.0", notes = "员工新增-2.0")
    public R saveEmployee(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isBlank(agentAccount.getAgyAccount(), "帐号不能为空");
        ValidRegUtils.validAgentName(agentAccount.getAgyAccount(), SysSetting.SysValueConst.require);
        Assert.isBlank(agentAccount.getAgyPwd(), "密码不能为空");
        Assert.isBlank(agentAccount.getSecurePwd(), "支付密码不能为空");
        Assert.isNull(agentAccount.getAttributes(), "属性不能为空");
        Assert.isNull(agentAccount.getDepartmentid(), "部门不能为空");
        agentAccount.setAgyAccount(agentAccount.getAgyAccount().toLowerCase());
        agentAccount.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agentAccount.setCreateUser(getUser().getUsername());
        agentAccount.setIp(getIpAddress(request));
        agentAccount.setFeeModel(null);
        // 代理费率默认0
        agentAccount.setDepositServicerate(BigDecimal.ZERO);
        agentAccount.setWithdrawServicerate(BigDecimal.ZERO);
        agentAccount.setAdditionalServicerate(BigDecimal.ZERO);
        agentAccount.setFeeModel(Constants.EVNumber.one);
        accountService.agyAccountSave(agentAccount, IpUtils.getUrl(request));
        return R.ok();
    }


    @PostMapping("/saveAgent")
    @RequiresPermissions(value = {"agent:account:save", "agent:account:agentSave"}, logical = Logical.OR)
    @ApiOperation(value = "代理新增", notes = "代理新增")
    public R agyAccountSave(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isBlank(agentAccount.getAgyAccount(), "帐号不能为空");
        ValidRegUtils.validAgentName(agentAccount.getAgyAccount(), SysSetting.SysValueConst.require);
        Assert.isBlank(agentAccount.getAgyPwd(), "密码不能为空");
        Assert.isBlank(agentAccount.getSecurePwd(), "支付密码不能为空");
        Assert.isNull(agentAccount.getAttributes(), "属性不能为空");
        Assert.isNull(agentAccount.getParentId(), "上级代理不能为空");
        if (agentAccount.getAttributes() == 0) {
            Assert.isNull(agentAccount.getContractId(), "契约不能为空");
            Assert.isBlank(agentAccount.getContractStart(), "契约开始时间不能为空");
            Assert.isBlank(agentAccount.getContractEnd(), "契约结束时间不能为空");
            Assert.isNull(agentAccount.getFeeModel(), "结算模式不能为空");
            if (agentAccount.getFeeModel() == Constants.EVNumber.three) {
            	Assert.isNull(agentAccount.getAdditionalServicerate(), "平台费额外比例不能为空");
            	Assert.isNull(agentAccount.getDepositServicerate(), "服务费存款比例不能为空");
            	Assert.isNull(agentAccount.getWithdrawServicerate(), "服务费取款比例不能为空");
            } else if (agentAccount.getFeeModel() == Constants.EVNumber.one) {
                Assert.isNull(agentAccount.getAdditionalServicerate(), "平台费额外比例不能为空");
            } else if (agentAccount.getFeeModel() == Constants.EVNumber.two) {
            	Assert.isNull(agentAccount.getDepositServicerate(), "服务费存款比例不能为空");
            	Assert.isNull(agentAccount.getWithdrawServicerate(), "服务费取款比例不能为空");
            }
        } else if (agentAccount.getAttributes() == 1) {
            agentAccount.setDepositServicerate(BigDecimal.ZERO);
            agentAccount.setWithdrawServicerate(BigDecimal.ZERO);
            agentAccount.setAdditionalServicerate(BigDecimal.ZERO);
            agentAccount.setFeeModel(Constants.EVNumber.one);
        }
        agentAccount.setAgyAccount(agentAccount.getAgyAccount().toLowerCase());
        agentAccount.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agentAccount.setCreateUser(getUser().getUsername());
        agentAccount.setIp(getIpAddress(request));
        accountService.agyAccountSave(agentAccount, IpUtils.getUrl(request));
        return R.ok();
    }

    @PostMapping("updateAgent")
    @RequiresPermissions("agent:account:update")
    @ApiOperation(value = "代理修改-2.0", notes = "代理修改-2.0")
    public R updateAgent(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "id不能为空");
        Assert.isNull(agentAccount.getAttributes(), "属性不能为空");
        Assert.isNull(agentAccount.getParentId(), "上级代理不能为空");
        if ("Invalid date".equalsIgnoreCase(agentAccount.getContractEnd()) 
        		|| "Invalid date".equalsIgnoreCase(agentAccount.getContractStart())) {
        	throw new R200Exception("契约时间格式错误");
        }
        accountService.updateAgent(agentAccount, getUser());
        return R.ok();
    }

    @PostMapping("updateFeeModel")
    @RequiresPermissions("agent:account:updateFeeModel")
    @ApiOperation(value = "代理批量修改结算模式", notes = "代理批量修改结算模式")
    public R updateFeeModel(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getIds(), "代理id不能为空");
        Assert.isNull(agentAccount.getFeeModel(), "结算模式不能为空");
        if (agentAccount.getFeeModel() == Constants.EVNumber.three) {
        	Assert.isNull(agentAccount.getAdditionalServicerate(), "平台费额外比例不能为空");
        	Assert.isNull(agentAccount.getDepositServicerate(), "服务费存款比例不能为空");
        	Assert.isNull(agentAccount.getWithdrawServicerate(), "服务费取款比例不能为空");
        } else if (agentAccount.getFeeModel() == Constants.EVNumber.one) {
            Assert.isNull(agentAccount.getAdditionalServicerate(), "平台费额外比例不能为空");
        } else if (agentAccount.getFeeModel() == Constants.EVNumber.two) {
        	Assert.isNull(agentAccount.getDepositServicerate(), "服务费存款比例不能为空");
        	Assert.isNull(agentAccount.getWithdrawServicerate(), "服务费取款比例不能为空");
        }
        accountService.updateFeeModel(agentAccount, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("agentList")
    @ApiOperation(value = "查询所有代理包括等级", notes = "查询所有代理包括等级")
    public R agentList(@RequestParam("agyAccount") String agyAccount,
                       @RequestParam(value = "parentId", required = false) Integer parentId) {
        return R.ok().put(accountService.agentList(agyAccount, parentId));
    }


    @GetMapping("agyAccountAuditInfo")
    @ApiOperation(value = "代理注册审核查询", notes = "代理注册审核查询")
    public R agyAccountAuditInfo(@RequestParam("id") Integer id) {
        Assert.isNull(id, "代理id不能为空");
        return R.ok().put(accountService.agyAccountAuditInfo(id));
    }

    @PostMapping("agyAccountAudit")
    @RequiresPermissions("agent:account:audit")
    @ApiOperation(value = "代理注册审核", notes = "代理注册审核")
    public R agyAccountAudit(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getStatus(), "状态不能为空");
        Assert.isBlank(agentAccount.getMemo(), "备注不能为空");
        accountService.agyAccountAudit(agentAccount, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("agyAccountDelete")
    @RequiresPermissions("agent:account:delete")
    @ApiOperation(value = "代理删除", notes = "代理删除")
    public R agyAccountDelete(@RequestParam("id") Integer id) {
        Assert.isNull(id, "代理id不能为空");
        accountService.agyAccountDelete(id, Constants.EVNumber.one);
        return R.ok();
    }

    @PostMapping("agyAccountAvailable")
    @RequiresPermissions("agent:account:available")
    @ApiOperation(value = "代理修改状态", notes = "代理修改状态")
    public R agyAccountAvailable(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getAvailable(), "状态不能为空");
        accountService.agyAccountAvailable(agentAccount.getId(), agentAccount.getAvailable(), getUser().getUsername());
        return R.ok();
    }

    @PostMapping("agyAccountUpdate")
    @RequiresPermissions("agent:account:update")
    @ApiOperation(value = "代理资料修改", notes = "代理资料修改")
    public R agyAccountUpdate(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "id不能为空");
        Assert.isLenght(agentAccount.getRealName(), "真实姓名为0-20位", 0, 20);
        if (StringUtil.isNotEmpty(agentAccount.getMobile())) {
            Assert.isPhone(agentAccount.getMobile(), "手机号码格式错误!");
        }
        Assert.isLenght(agentAccount.getMemo(), "备注内容为0-45位!", 0, 45);
        if (StringUtil.isNotEmpty(agentAccount.getEmail())) {
            Assert.checkEmail(agentAccount.getEmail(), "邮箱号码格式错误!");
        }
        if (StringUtil.isNotEmpty(agentAccount.getWeChat())) {
            Assert.isWeChat(agentAccount.getWeChat(), "微信号码格式错误!");
        }
        if (StringUtil.isNotEmpty(agentAccount.getQq())) {
            Assert.isQq(agentAccount.getQq(), "QQ号码格式错误!");
        }
        accountService.agyAccountUpdate(agentAccount, getUser().getUsername());
        return R.ok();
    }

    @GetMapping("agyBankList")
    @RequiresPermissions("agent:bank:list")
    @ApiOperation(value = "代理银行查看代理id", notes = "代理银行查看代理id")
    public R agyBankList(@RequestParam("id") Integer id) {
        Assert.isNull(id, "代理id不能为空");
        return R.ok().put(accountService.agyBankList(id));
    }

    @GetMapping("agyCryptocurrenciesList")
    @RequiresPermissions("agent:bank:list")
    @ApiOperation(value = "代理加密货币钱包查看代理id", notes = "代理加密货币钱包查看代理id")
    public R agyCryptocurrenciesList(@RequestParam("id") Integer id) {
        Assert.isNull(id, "代理id不能为空");
        return R.ok().put(accountService.agyCryptocurrenciesList(id));
    }

    @PostMapping("agyBankSave")
    @RequiresPermissions("agent:bank:add")
    @ApiOperation(value = "代理银行卡新增", notes = "代理银行卡新增")
    public R agyBankSave(@RequestBody AgyBankcard bankcard) {
        Assert.isNull(bankcard.getAccountId(), "代理id不能为空");
        Assert.isNull(bankcard.getBankCardId(), "银行卡id不能为空");
        Assert.isBlank(bankcard.getCardNo(), "银行卡号不能为空");
        Assert.isBlank(bankcard.getRealName(), "真实姓名不能为空");
        bankcard.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bankcard.setCreateUser(getUser().getUsername());
        accountService.agyBankSave(bankcard);
        return R.ok();
    }

    @PostMapping("agyBankUpdate")
    @RequiresPermissions("agent:bank:update")
    @ApiOperation(value = "代理银行卡修改", notes = "代理银行卡修改")
    public R agyBankUpdate(@RequestBody AgyBankcard bankcard) {
        Assert.isNull(bankcard.getBankCardId(), "银行卡id不能为空");
        Assert.isBlank(bankcard.getCardNo(), "银行卡号不能为空");
        bankcard.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bankcard.setModifyUser(getUser().getUsername());
        accountService.agyBankUpdate(bankcard, null);
        return R.ok();
    }

    @PostMapping("agyBankAvailable")
    @RequiresPermissions("agent:bank:update")
    @ApiOperation(value = "代理银行卡状态修改", notes = "代理银行卡状态修改")
    public R agyBankAvailable(@RequestBody AgyBankcard bankcard) {
        Assert.isNull(bankcard.getId(), "id不能为空");
        Assert.isNull(bankcard.getAvailable(), "状态不能为空");
        bankcard.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bankcard.setModifyUser(getUser().getUsername());
        accountService.agyBankAvailable(bankcard);
        return R.ok();
    }

    @GetMapping("agyBankDelete")
    @RequiresPermissions("agent:bank:delete")
    @ApiOperation(value = "代理银行卡删除", notes = "代理银行卡删除")
    public R agyBankDelete(@RequestParam("id") Integer id) {
        Assert.isNull(id, "id不能为空");
        accountService.agyBankDelete(id);
        return R.ok();
    }

    @PostMapping("agyAccountPassword")
    @RequiresPermissions("agent:account:password")
    @ApiOperation(value = "代理重置密码", notes = "代理重置密码")
    public R agyAccountPassword(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getAgyPwd(), "密码不能为空");
        agentAccount.setSecurePwd(null);
        accountService.agyAccountPassword(agentAccount, getUser().getUsername());
        return R.ok();
    }

    @PostMapping("agyAccountFundPassword")
    @RequiresPermissions("agent:account:fundPassword")
    @ApiOperation(value = "代理重置资金密码", notes = "代理重置资金密码")
    public R agyAccountFundPassword(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getSecurePwd(), "密码不能为空");
        agentAccount.setAgyPwd(null);
        accountService.agyAccountPassword(agentAccount, getUser().getUsername());
        return R.ok();
    }


    @GetMapping("/findAccountByName")
    @ApiOperation(value = "代理校验用户名是否存在", notes = "代理校验用户名是否存在")
    public R findAccountByName(@RequestParam("agyAccount") @NotNull String agyAccount) {
        return R.ok().put("name", accountService.findAccountByName(agyAccount));
    }

    @GetMapping("/findTopAccountAll")
    @ApiOperation(value = "查找所有代理", notes = "查找所有代理")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"), @ApiImplicitParam(name = "siteCode", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R findTopAccountAll(@RequestParam(value = "parentId", required = false) Integer parentId) {
        return R.ok().put("accounts", accountService.findTopAccountAll(parentId));
    }

    @GetMapping("/findTopAccountLike")
    @ApiOperation(value = "查找所有代理(模糊查询)", notes = "查找所有代理(模糊查询)")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"), @ApiImplicitParam(name = "siteCode", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R findTopAccountLike(@RequestParam(value = "agyAccount", required = false) String agyAccount) {
        return R.ok().put("accounts", accountService.findTopAccountLike(agyAccount));
    }

    @GetMapping("/findTopAccountAllIncludeDisable")
    @ApiOperation(value = "查找所有代理（含禁用）", notes = "查找所有代理（含禁用）")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header"), @ApiImplicitParam(name = "siteCode", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R findTopAccountAllIncludeDisable(@RequestParam(value = "parentId", required = false) Integer parentId) {
        return R.ok().put("accounts", accountService.findTopAccountAllIncludeDisable(parentId));
    }

    @GetMapping("/findAllSubAgency")
    @ApiOperation(value = "查找所有子代理", notes = "查找所有子代理")
    public R findAllSubAgency() {
        return R.ok().put("accounts", accountService.findAllSubAgency());
    }

    @GetMapping("/findAllSubAgencyIncludeDisable")
    @ApiOperation(value = "查找所有子代理（含禁用）", notes = "查找所有子代理（含禁用）")
    public R findAllSubAgencyIncludeDisable() {
        return R.ok().put("accounts", accountService.findAllSubAgencyIncludeDisable());
    }

    @GetMapping("/getAllParentAccount")
    @ApiOperation(value = "获取所有总代", notes = "获取所有总代")
    public R getAllParentAccount(@RequestParam(value = "parentId" , required = false) Integer parentId) {
       if(parentId != null){
           return R.ok().put("accounts", accountService.findBySubAgentPrentId(parentId));
       }
       return R.ok().put("accounts", accountService.findTopAccountAll());
    }

    @GetMapping("/selectAgentTree")
    @ApiOperation(value = "获取代理节点数", notes = "获取代理节点数")
    public R selectAgentTree() {
        return R.ok().put("agentTree", accountService.selectLevelsAgentTree());
    }

    @GetMapping("/queryAllDomains")
    @ApiOperation(value = "查询所有推广域名", notes = "查询所有推广域名")
    public R queryAllDomains() {
        return R.ok().put("list", agentService.queryAllDomains());
    }

    @GetMapping("/findDomainsById")
    @ApiOperation(value = "查询当前代理推广域名", notes = "查询当前代理推广域名")
    public R findDomainsById(@RequestParam("id") Integer id, @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("unbase", agentService.findNoBaseDomains(id, pageNo, pageSize)).put("base", agentService.findBaseDomains(id, CommonUtil.getSiteCode()));
    }

    @PostMapping("/updateAgyMerGroup")
    @ApiOperation(value = "修改代理关联会员组", notes = "修改代理关联会员组")
    @RequiresPermissions("agent:account:updateAgyMerGroup")
    public R updateAgyMerGroup(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getGroupId(), "会员组id不能为空");
        AgentAccount agentAccount2 = accountService.findAccountInfo(agentAccount.getId());
        if (Constants.EVNumber.zero == agentAccount2.getParentId()) {
            throw new R200Exception("总代不支持配置");
        }
        AgentAccount updateParams = new AgentAccount();
        updateParams.setId(agentAccount.getId());
        updateParams.setGroupId(agentAccount.getGroupId());
        accountService.update(updateParams);
        return R.ok();
    }

    @GetMapping("findAgentByloginName")
    @ApiOperation(value = "查找会员的所有代理-新", notes = "查找会员的所有代理")
    public R findAgentByloginName(@RequestParam("loginName") String loginName) {
        Assert.isBlank(loginName, "会员名不能为null");
        return R.ok().put(accountService.findAgentByloginName(loginName));
    }

    @GetMapping("findAgentByAgyaccount")
    @ApiOperation(value = "查找代理的所有上级代理", notes = "查找代理的所有上级代理")
    public R findAgentByAgyaccount(@RequestParam("agyAccount") String agyAccount) {
        Assert.isBlank(agyAccount, "代理不能为null");
        return R.ok().put(accountService.findAgentByAgyaccount(agyAccount));
    }

    @GetMapping("/getAgentBanner")
    @ApiOperation(value = "获取面包屑导航-代理", notes = "获取面包屑导航-代理")
    public R getAgentBanner(@ModelAttribute AgentAccount agentAccount) {
        return R.ok().put(accountService.getAgentBanner(agentAccount));
    }

    @GetMapping("/getMbrBanner")
    @ApiOperation(value = "获取面包屑导航-会员", notes = "获取面包屑导航-会员")
    public R getMbrBanner(@ModelAttribute AgentAccount agentAccount) {

        return R.ok().put(accountService.getMbrBanner(agentAccount));
    }

    @GetMapping("updateAgentRate")
   // @RequiresPermissions("agent:account:updateAgentRate")
    @ApiOperation(value = "更新费率", notes = "代理列表-2.0")
    public R updateRate(@ModelAttribute AgentAccount agentAccount) {
        return R.ok().putPage(accountService.updateAgentRate(agentAccount));
    }
}
