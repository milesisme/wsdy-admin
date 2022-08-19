package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.dto.AgentWinLostReportModelDto;
import com.wsdy.saasops.agapi.modules.service.AgentGameWinLoseService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/agapi/manager")
@Api(tags = "输赢报表")
public class AgentManagerController {

    @Autowired
    private AgentGameWinLoseService winLoseService;
    @Autowired
    private RedisService redisService;

    @GetMapping("/agentSelectWinLostReportList")
    @ApiOperation(value = "按游戏类别汇总列表-表头", notes = "按游戏类别汇总列表-表头")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R findWinLostReportList(@ModelAttribute AgentWinLostReportModelDto reportModelDto, HttpServletRequest request) {
        // 处理catCodes
        dealCatCodes(reportModelDto);

        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().put(winLoseService.findWinLostReportList(reportModelDto));
    }

    @GetMapping("/agentSelectWinLostSum")
    @ApiOperation(value = "按游戏类别汇总列表-表头-总计", notes = "按游戏类别汇总列表-表头-总计")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R findWinLostSum(@ModelAttribute AgentWinLostReportModelDto reportModelDto, HttpServletRequest request) {
        // 处理catCodes
        dealCatCodes(reportModelDto);

        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().putPage(winLoseService.findWinLostSum(reportModelDto));
    }

    @GetMapping("/agentSelectWinLostListLevel")
    @ApiOperation(value = "直属会员列表", notes = "直属会员 1.代理视图-代理直属会员 2.会员视图--点击会员或 查询会员 ")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R findWinLostListLevel(@ModelAttribute AgentWinLostReportModelDto reportModelDto,
                                  @RequestParam("pageNo") @NotNull Integer pageNo,
                                  @RequestParam("pageSize") @NotNull Integer pageSize, HttpServletRequest request) {
        // 处理catCodes
        dealCatCodes(reportModelDto);

        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount()) && StringUtils.isEmpty(reportModelDto.getLoginName())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().putPage(winLoseService.findWinLostListLevel(reportModelDto, pageNo, pageSize, agentAccount));
    }


    @GetMapping("/agentSelectWinLostLoginName")
    @ApiOperation(value = "会员-跟进会员查询 只查询属于自己", notes = "1.点击会员进入会员视图， 2. 查询会员的会员视图")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R findWinLostLoginName(@ModelAttribute AgentWinLostReportModelDto reportModelDto, HttpServletRequest request) {
        // 处理catCodes
        dealCatCodes(reportModelDto);

        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount()) && StringUtils.isEmpty(reportModelDto.getLoginName())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().putPage(winLoseService.findWinLostLoginName(reportModelDto, agentAccount));
    }


    @GetMapping("/agentSelectWinLostSumByLoginName")
    @ApiOperation(value = "会员详情--会员总计", notes = "会员详情--会员总计")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R findWinLostSumByLoginName(@ModelAttribute AgentWinLostReportModelDto reportModelDto) {
        // 处理catCodes
        dealCatCodes(reportModelDto);

        Assert.isBlank(reportModelDto.getLoginName(), "会员名不能为空");
        return R.ok().putPage(winLoseService.findWinLostListSumLoginName(reportModelDto));
    }


    @GetMapping("/agentSelectWinLostLoginNameList")
    @ApiOperation(value = "同agentSelectWinLostLoginName 的分页结果", notes = "仅查询 会员列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R findWinLostLoginNameList(@ModelAttribute AgentWinLostReportModelDto reportModelDto,
                                      @RequestParam("pageNo") @NotNull Integer pageNo,
                                      @RequestParam("pageSize") @NotNull Integer pageSize,HttpSession session,HttpServletRequest request) {
        // 处理catCodes
        dealCatCodes(reportModelDto);

        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount()) && StringUtils.isEmpty(reportModelDto.getLoginName())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().putPage(winLoseService.findWinLostLoginNameList(reportModelDto, pageNo, pageSize, agentAccount));
    }


    @GetMapping("/agentSelectWinLostAccount")
    @ApiOperation(value = "会员详情-分类统计-仅查询 会员", notes = "会员详情--表头")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R findWinLostAccount(@ModelAttribute AgentWinLostReportModelDto reportModelDto,HttpSession session,HttpServletRequest request) {
        // 处理catCodes
        dealCatCodes(reportModelDto);

        Assert.isBlank(reportModelDto.getLoginName(), "会员名不能为空");
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount()) && StringUtils.isEmpty(reportModelDto.getLoginName())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().putPage(winLoseService.findWinLostAccount(reportModelDto));
    }

    @GetMapping("/agentSelectAgentList")
    @ApiOperation(value = "获取下级代理查询列表", notes = "获取下级代理查询列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R selectAgentList(@ModelAttribute AgentWinLostReportModelDto reportModelDto ,HttpSession session,HttpServletRequest request) {
        AgentAccount agentAccount = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(StringUtils.isEmpty(reportModelDto.getAgyAccount())){
            reportModelDto.setAgyAccount(agentAccount.getAgyAccount());
        }
        return R.ok().putPage(winLoseService.selectAgentByParentIdList(agentAccount.getId()));
    }

    // 处理catCodes
    public  static void dealCatCodes(AgentWinLostReportModelDto reportModelDto){
        // 处理传入的参数
        List<String> catCodes = reportModelDto.getCatCodes();
        if(!Objects.isNull(catCodes) && catCodes.size() > 0){
//            if(catCodes.contains("Sport")){
//                catCodes.add("Esport");
//            }
            if(catCodes.contains("Others")){
//                catCodes.add("Tip");
                catCodes.add("Activity");
                catCodes.add("Unknown");
            }
        }
    }

    @GetMapping("/findWinLostReportView")
    @ApiOperation(value = "直属会员列表", notes = "直属会员 1.代理视图-代理直属会员 2.会员视图--点击会员或 查询会员 ")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R findWinLostReportView(@ModelAttribute AgentWinLostReportModelDto reportModelDto) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostReportView(reportModelDto));
    }

    @GetMapping("/findWinLostReportViewAgent")
    @ApiOperation(value = "视图--直属代理", notes = "视图--直属代理")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    @AgentLogin
    public R findWinLostReportViewAgent(@ModelAttribute AgentWinLostReportModelDto reportModelDto,
                                        @RequestParam("pageNo") @NotNull Integer pageNo,
                                        @RequestParam("pageSize") @NotNull Integer pageSize) {
        // 处理catCodes
        dealCatCodes(reportModelDto);
        return R.ok().putPage(winLoseService.findWinLostReportViewAgent(reportModelDto,pageNo,pageSize));
    }
}
