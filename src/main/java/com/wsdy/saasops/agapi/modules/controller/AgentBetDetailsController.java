package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/agapi/agent/bet")
@Api(tags = "代理投注记录")
public class AgentBetDetailsController extends AbstractController {

    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private AgentService agentService;
    /**
     * 查询投注明细统计
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("finalBetDetailsAll")
    @ApiOperation(value = "投注记录", notes = "全部游戏")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @AgentLogin
    public R betDetailsData(@RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize,
                            GameReportQueryModel model,
                            HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        // 查询参数处理
        model.setSiteCode(CommonUtil.getSiteCode());
        // 判断是总代还是下级代理
        boolean isTagency = agentService.isTagency(account);
        if(isTagency){
            model.setParentAgentid(account.getId());
        }else{
            model.setAgentid(account.getId());
        }

        return R.ok().put("page", analysisService.getBkRptBetListPageForAgent(pageNo, pageSize, model));
    }

    /**
     * 查询游戏代码
     *
     * @return
     */
    @GetMapping("finalGameCode")
    @ApiOperation(value = "游戏代码", notes = "游戏代码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})    @AgentLogin
    public R finalGameCodByType(@RequestParam("codetype") String platFormId) {
        if (platFormId != null && !"".equals(platFormId.trim())) {
            return R.ok().put("page", analysisService.getGameType(platFormId, 0, CommonUtil.getSiteCode()));
        }
        return R.ok().put("page", analysisService.getPlatForm(CommonUtil.getSiteCode()));
    }
}
