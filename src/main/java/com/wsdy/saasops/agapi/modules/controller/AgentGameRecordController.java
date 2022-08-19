package com.wsdy.saasops.agapi.modules.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentNewService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j
@RequestMapping("/agapi/n2")
@Api(tags = "投注记录")
public class AgentGameRecordController {

    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private AgentNewService agentNewService;

    @AgentLogin
    @GetMapping("/finalGameCode")
    @ApiOperation(value = "游戏代码", notes = "游戏代码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R finalGameCodByType(@RequestParam("codetype") String platFormId) {
        if (platFormId != null && !"".equals(platFormId.trim())) {
            return R.ok().put("page", analysisService.getGameType(platFormId, 0, CommonUtil.getSiteCode()));
        }
        return R.ok().put("page", analysisService.getPlatForm(CommonUtil.getSiteCode()));
    }

    @AgentLogin
    @RequestMapping("/finalBetDetailsAll")
    @ApiOperation(value = "投注记录", notes = "全部游戏")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R betDetailsData(@RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize,
                            GameReportQueryModel model,
                            HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            AgentAccount agentAccount = agentNewService.getAgentAccount(account.getAgentId());
            if (agentAccount.getAttributes() == 1) {
                model.setSubCagencyId(agentAccount.getId());
            } else {
                model.setAgentid(agentAccount.getId());
            }
        } else if (account.getAttributes() == Constants.EVNumber.one) {
            model.setSubCagencyId(account.getId());
        } else {
            model.setAgentid(account.getId());
        }
        model.setSiteCode(CommonUtil.getSiteCode());
        Map rptBetListReport = analysisService.getRptBetListReport(model);
        return R.ok().putPage(analysisService.getAgentBkRptBetListPage(pageNo, pageSize, model))
        		.put("total", rptBetListReport)
				.put("betCount",rptBetListReport.get("betCount"));
    }
}
