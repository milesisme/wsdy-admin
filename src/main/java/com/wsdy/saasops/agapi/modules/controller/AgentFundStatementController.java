package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.analysis.entity.FundStatementModel;
import com.wsdy.saasops.modules.analysis.service.FundStatementService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.fund.dto.DepositStatisticsByPayDto;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/agapi/analysis/fundReport")
@Api(tags = "代理资金报表")
public class AgentFundStatementController extends AbstractController {

    @Autowired
    private FundStatementService fundStatementService;
    @Autowired
    private FundDepositService fundDepositService;

    @GetMapping("/totalInfo")
    @ApiOperation(value = "总体详情", notes = "总体详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R findFundTotalInfo(FundStatementModel model,
                               HttpServletRequest request) {
        // 获得登陆代理数据
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        model.setAgyId(account.getId());

        return R.ok().put("info", fundStatementService.findFundTotalInfo(model));
    }

    /***
     * 按天查询资金报表
     * @return
     */
    @GetMapping("/list")
    @ApiOperation(value = "资金报表", notes = "资金列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R findFundReportPage(FundStatementModel model,
                                HttpServletRequest request) {
        // 判断是否是按会员还是按代理查询
        if(StringUtil.isEmpty(model.getLoginName()) ){   // 代理的处理
            // 获得登陆代理数据
            AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
            model.setAgyId(account.getId());
        }
        return R.ok().put("page", fundStatementService.findFundReportPage(model));
    }

    @GetMapping("/judgeTagency")
    @ApiOperation(value = "判断总代子代", notes = "判断总代子代")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R judgeTagency(@RequestParam(value = "agyAccount", required = false) String agyAccount) {
        return R.ok().put("info", fundStatementService.judgeTagency(agyAccount));
    }

    @GetMapping("/depotList")
    @ApiOperation(value = "平台彩金", notes = "平台彩金")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R findDepotPayoutList(FundStatementModel model,
                                 HttpServletRequest request) {

        // 获得登陆代理数据
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        model.setAgyId(account.getId());

        return R.ok().put("page", fundStatementService.findDepotPayoutList(model));
    }

    @GetMapping("/agencyList")
    @ApiOperation(value = "总代列表", notes = "总代列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R findTagencyList(FundStatementModel model) {
        return R.ok().put("page", fundStatementService.findTagencyList(model));
    }

    @GetMapping("/memberList")
    @ApiOperation(value = "会员资金列表", notes = "会员资金列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R findMemberList(FundStatementModel model,
                            HttpServletRequest request) {

        // 如果根据会员参数查询，需要判断该会员是否是该代理下面的会员
        if(StringUtil.isNotEmpty(model.getLoginName()) ){   // 会员
            // 获得登陆代理数据
            AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
            model.setAgyId(account.getId());
        }

        return R.ok().put("page", fundStatementService.findMemberList(model));
    }

    @GetMapping("/depositStatisticByPay")
    @ApiOperation(value = "代理后台入款渠道统计", notes = "代理后台入款渠道统计")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R depositStatisticByPay(@ModelAttribute DepositStatisticsByPayDto depositStatisticsByPayDto) {

        return R.ok().putPage(fundDepositService.depositStatisticByPay(depositStatisticsByPayDto));
    }

    @GetMapping("/totalList")
    @ApiOperation(value = "视图表头", notes = "视图表头")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R totalList(FundStatementModel model) {
        return R.ok().put(fundStatementService.totalList(model));
    }
}