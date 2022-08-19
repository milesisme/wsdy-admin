package com.wsdy.saasops.agapi.modulesV2.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modulesV2.dto.AgentV2AccountLogDto;
import com.wsdy.saasops.agapi.modulesV2.service.AgentV2AccountLogService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
@Slf4j
@RequestMapping("/agapi/v2/log/account")
@Api(tags = "外围系统-账户记录")
public class AgentV2AccountLogController extends AbstractController {
    @Autowired
    private AgentV2AccountLogService agentV2AccountLogService;

    @AgentLogin
    @GetMapping("/getAccountLogList")
    @ApiOperation(value = "获取账户记录列表", notes = "账户记录列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R getAccountLogList(@ModelAttribute AgentV2AccountLogDto agentV2AccountLogDto, HttpServletRequest request) {
        // 参数校验 TODO
        Assert.isNull(agentV2AccountLogDto.getUserType(), "用户类型不能为空！");

        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        agentV2AccountLogDto.setAgyAccount(agentAccount.getAgyAccount());
        agentV2AccountLogDto.setAgyId(agentAccount.getId());

        // 默认搜索当前登录账号
        if(StringUtils.isEmpty(agentV2AccountLogDto.getSearchName())){
            agentV2AccountLogDto.setSearchName(agentAccount.getAgyAccount());
            agentV2AccountLogDto.setUserType("user");
        }


        return R.ok().putPage(agentV2AccountLogService.getAccountLogList(agentV2AccountLogDto));
    }
}
