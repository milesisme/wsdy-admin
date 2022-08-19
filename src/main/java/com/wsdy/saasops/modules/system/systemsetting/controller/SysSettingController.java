package com.wsdy.saasops.modules.system.systemsetting.controller;


import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.systemsetting.dto.*;
import com.wsdy.saasops.modules.system.systemsetting.entity.SmsConfig;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import com.wsdy.saasops.modules.system.systemsetting.vo.BroadcastVo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/bkapi/setting/syssetting")
public class SysSettingController extends AbstractController {

    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private TCpSiteService tCpSiteService;

    @RequestMapping(value = "/saveWebTerms", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:save")
    @ApiOperation(value = "saveOrUpdate", notes = "saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R saveOrUpdateWebTerms(@RequestParam(value = "keyArray[]") List<String> keyArray, @RequestParam(value = "valueArray[]") List<String> valueArray) {
        List<SysSetting> ssList = new ArrayList<SysSetting>();
        for (int s = 0; s < keyArray.size(); s++) {
            SysSetting ss = new SysSetting();
            String key = keyArray.get(s);
            if ((key.equals(SystemConstants.MEMBER_REGISTER_DISPLAY_TERMS_OF_WEBSITE)) || (key.equals(SystemConstants.AGENT_REGISTER_DISPLAY_TERMS_OF_WEBSITE))) {
                ss.setSyskey(keyArray.get(s));
                ss.setSysvalue(valueArray.get(s));
            } else {
                ss.setSyskey(keyArray.get(s));
                ss.setWebsiteTerms(valueArray.get(s));
            }
            ssList.add(ss);
        }
        sysSettingService.modifyOrUpdate(ssList);
        return R.ok();
    }

    /**
     * 获取注册设置
     */
    @SysLog(module = "系统设置", methodText = "获取注册设置")
    @RequestMapping(value = "/queryRegisterSet", method = RequestMethod.GET)
//    @RequiresPermissions("setting:syssetting:info")
    @RequiresPermissions("setting:syssetting:RegisterSetQry")
    @ApiOperation(value = "queryRegisterSet", notes = "queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryRegisterSet() {
        RegisterSet registerSet = sysSettingService.queryRegisterSet();
        return R.ok().put(registerSet);
    }

    /**
     * 获取站点设置_后台登录前请求
     */
    @RequestMapping(value = "/queryStationSet", method = RequestMethod.GET)
    @ApiOperation(value = "queryStationSet", notes = "queryRegisterSet")
    //@RequiresPermissions("setting:syssetting:info")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryStationSet() {
        StationSet stationSet = sysSettingService.queryStationSet();
        return R.ok().put(stationSet);
    }

    /**
     * 获取站点设置_后台系统设置_站点设置请求(权限控制接口)
     */
    @RequestMapping(value = "/queryStationSetForSysSetting", method = RequestMethod.GET)
    @ApiOperation(value = "queryStationSetForSysSetting", notes = "queryStationSetForSysSetting")
    @RequiresPermissions("setting:syssetting:StationSetQry")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryStationSetForSysSetting() {
        StationSet stationSet = sysSettingService.queryStationSet();
        return R.ok().put(stationSet);
    }

    @RequestMapping(value = "/queryStationInfo", method = RequestMethod.GET)
    //@RequiresPermissions("setting:syssetting:info")
    @ApiOperation(value = "queryStationSet", notes = "queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryStationInfo() {
        StationSet stationSet = sysSettingService.queryStationSet();
        return R.ok().put(stationSet.getWebsiteTitle());
    }

    /**
     * 获取邮件设置
     */
    @RequestMapping(value = "/queryMailSet", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:MailSetQry")
    @ApiOperation(value = "queryMailSet", notes = "queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryMailSet() {
        MailSet mailSet = sysSettingService.queryMaliSet();
        return R.ok().put(mailSet);
    }

    /**
     * 获取用户注册条款
     */
    @RequestMapping(value = "/queryWebTerms", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:RegisterSetQry")
    @ApiOperation(value = "queryWebTerms", notes = "queryWebTerms")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryWebTerms() {
        WebTerms webTerms = sysSettingService.queryWebTerms();
        return R.ok().put(webTerms);
    }

    //站点设置
    @SysLog(module = "系统设置", methodText = "保存或更新站点设置")
    @RequestMapping(value = "/modifyStationSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:StationSetEdit")
    @ApiOperation(value = "saveOrUpdate", notes = "saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyStationSet(@RequestParam String stationSet, @RequestParam(value = "logoPicFile", required = false) MultipartFile logoPicFile, @RequestParam(value = "titlePicFile", required = false) MultipartFile titlePicFile, HttpServletRequest request) {
        sysSettingService.saveStationSet(StringEscapeUtils.unescapeHtml4(stationSet), getUser().getUsername(), CommonUtil.getIpAddress(request));
        sysSettingService.modifyPic(titlePicFile, logoPicFile);
        return R.ok();
    }

    //邮件设置
    @SysLog(module = "系统设置", methodText = "保存或更新邮件设置")
    @RequestMapping(value = "/modifyMailSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:MailSetEdit")
    @ApiOperation(value = "saveOrUpdate", notes = "saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyMailSet(@RequestBody MailSet mailSet, HttpServletRequest request) {
        sysSettingService.saveMailSet(mailSet, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    //注册设置
    @SysLog(module = "系统设置", methodText = "保存或更新注册设置")
    @RequestMapping(value = "/modifyRegisterSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:RegisterSetEdit")
    @ApiOperation(value = "saveOrUpdate", notes = "saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyRegisterSet(@RequestBody RegisterSet registerSet, HttpServletRequest request) {
        sysSettingService.saveRegSet(registerSet, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    //注册设置--保存或更新是否允许前台注册设置
    @SysLog(module = "系统设置", methodText = "保存或更新是否允许前台注册设置")
    @RequestMapping(value = "/modifyAccWebRegister", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:RegisterSetEdit")
    @ApiOperation(value = "saveOrUpdate", notes = "saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyAccWebRegister(@RequestBody RegisterSet registerSet, HttpServletRequest request) {
        sysSettingService.saveAccWebRegSet(registerSet, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }


    //用户注册条款
    @SysLog(module = "系统设置", methodText = "保存或更新用户注册条款")
    @RequestMapping(value = "/modifyWebTerms", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:RegisterSetEdit")
    @ApiOperation(value = "saveOrUpdate", notes = "saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyWebTerms(@RequestBody WebTerms webTerms) {
        sysSettingService.saveWebTerms(webTerms, CommonUtil.getSiteCode());
        return R.ok();
    }

    //测试接收邮件
    @SysLog(module = "系统设置", methodText = "测试接收邮件")
    @RequestMapping(value = "/testReceiveMail", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:MailSetQry")
    @ApiOperation(value = "saveOrUpdate", notes = "saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R tryReceiveMail(@ModelAttribute MailSet mailSet) {
        String content = sysSettingService.testReceiveMail(mailSet);
        if (StringUtils.isEmpty(content)) {
            throw new RRException("发送邮件失败!");
        }
        return R.ok().put(content);
    }


    @SysLog(module = "系统设置", methodText = "出入款设置")
    @RequestMapping(value = "/modifyPaySet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:PaySetEdit")
    @ApiOperation(value = "modifyPaySet", notes = "modifyPaySet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyPaySet(@RequestBody PaySet paySet, HttpServletRequest request) {
        sysSettingService.modifyPaySet(paySet, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }
    
    @RequestMapping(value = "/queryVenturePlanSet", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:venturePlanSetQry")
    @ApiOperation(value = "获取合营计划设置", notes = "获取合营计划设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryVenturePlanSet() {
    	return R.ok().put(sysSettingService.queryVenturePlanSet());
    }
    
    @SysLog(module = "系统设置", methodText = "合营计划设置")
    @RequestMapping(value = "/venturePlanSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:venturePlanSetEdit")
    @ApiOperation(value = "合营计划设置", notes = "合营计划设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R venturePlanSet(@RequestBody VenturePlanSet venturePlanSet, HttpServletRequest request) {
    	sysSettingService.venturePlanSet(venturePlanSet, getUser().getUsername(), CommonUtil.getIpAddress(request));
    	return R.ok();
    }

    @SysLog(module = "系统设置", methodText = "好友转账设置")
    @RequestMapping(value = "/modifyFriendTransSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:FriendTransSetEdit")
    @ApiOperation(value = "modifyPaySet", notes = "modifyPaySet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyFriendTransSet(@RequestBody PaySet paySet, HttpServletRequest request) {
        sysSettingService.modifyFriendTransSet(paySet, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping(value = "/queryPaySet")
    @RequiresPermissions("setting:syssetting:PaySetQry")
    @ApiOperation(value = "查询出款设置", notes = "查询出款设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryPaySet() {
        return R.ok().put(sysSettingService.queryPaySet());
    }

    @GetMapping(value = "/queryFriendTransSet")
    @RequiresPermissions("setting:syssetting:FriendTransSetQry")
    @ApiOperation(value = "查询好友转账设置", notes = "查询好友转账设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryFriendTransSet() {
        return R.ok().put(sysSettingService.queryFriendTransSet());
    }

    @PostMapping(value = "/promotionSet")
//    @RequiresPermissions("setting:syssetting:PromotionSetEdit")
    @ApiOperation(value = "系统设置推广设置", notes = "系统设置推广设置")
    public R promotionSet(@RequestBody PromotionSet promotionSet, HttpServletRequest request) {
        Assert.isNull(promotionSet.getType(), "类型不能为空");
        Assert.isNull(promotionSet.getSiteUrlId(), "请选择域名");
        sysSettingService.promotionSet(promotionSet, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @PostMapping(value = "/promotionSetForSysSetting")
    @RequiresPermissions("setting:syssetting:PromotionSetEdit")
    @ApiOperation(value = "系统设置推广设置", notes = "系统设置推广设置")
    public R promotionSetForSysSetting(@RequestBody PromotionSet promotionSet, HttpServletRequest request) {
        Assert.isNull(promotionSet.getType(), "类型不能为空");
        Assert.isNull(promotionSet.getSiteUrlId(), "请选择域名");
        sysSettingService.promotionSet(promotionSet, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }

    @GetMapping(value = "/queryPromotionSet")
//    @RequiresPermissions("setting:syssetting:info")
    @ApiOperation(value = "系统设置推广设置查看", notes = "系统设置推广设置查看")
    public R queryPromotionSet() {
        return R.ok().put("account", sysSettingService.queryPromotionSet()).put("agent", sysSettingService.queryAgentPromotionSet());
    }

    @GetMapping(value = "/queryPromotionSetForSysSetting")
    @RequiresPermissions("setting:syssetting:PromotionSetQry")
    @ApiOperation(value = "系统设置推广设置查看-会员推广域名", notes = "系统设置推广设置查看-会员推广域名")
    public R queryPromotionSetForSysSetting() {
        return R.ok().put("account", sysSettingService.queryPromotionSet())
        		.put("agent", sysSettingService.queryAgentPromotionSet())
        				// 渠道的推广域名
        				.put("channel", sysSettingService.queryChannelPromotionSet());
    }
    @GetMapping(value = "/queryDomain")
    @RequiresPermissions("setting:syssetting:PromotionSetQry")
    @ApiOperation(value = "系统设置推广设置查看-代理推广域名", notes = "系统设置推广设置查看-代理推广域名")
    public R queryDomain() {
        return R.ok().put(tCpSiteService.queryDomain(CommonUtil.getSiteCode()));
    }

    @GetMapping("getRebateCastDepth")
    @ApiOperation(value = "查询返利计算层级设置", notes = "查询返利计算层级设置")
    public R getRebateCastDepth() {
        return R.ok().put(Integer.valueOf(sysSettingService.getSysSetting(SystemConstants.REBATE_CAST_DEPTH).getSysvalue()));
    }

    /**
     * 获取语音线路设置
     */
    @SysLog(module = "系统设置", methodText = "获取语音线路设置")
    @RequestMapping(value = "/queryOutCallset", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:PromotionSetQry")
    @ApiOperation(value = "queryOutCallset", notes = "queryRegisterSet")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryOutCallset() {
        return R.ok().put(SystemConstants.OUTCALL_PLATFORM,sysSettingService.queryOutCallset());
    }

    @PostMapping(value = "/outCallSet")
    @RequiresPermissions("setting:syssetting:PromotionSetEdit")
    @ApiOperation(value = "系统设置推广设置", notes = "系统设置推广设置")
    public R outCallSet(@RequestBody String outCallPlatform) {
        Assert.isNull(outCallPlatform, "语音线路不能为空");
        sysSettingService.outCallSet(outCallPlatform);
        return R.ok();
    }

    /**
     * 获取短信设置
     */
    @RequestMapping(value = "/querySmsConfig", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:SmsSetQry")
    @ApiOperation(value = "获取短信设置", notes = "获取短信设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R querySmsConfig() {
        List<SmsConfig> list = sysSettingService.querySmsConfig();
        return R.ok().put(list);
    }

    /**
     * 短信设置
     */
    @SysLog(module = "系统设置", methodText = "保存或更新短信设置")
    @RequestMapping(value = "/modifySmsConfig", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:SmsSetEdit")
    @ApiOperation(value = "短信设置", notes = "短信设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifySmsConfig(@RequestBody SmsConfigDto smsConfigDto) {
        // 校验
        if(Objects.isNull(smsConfigDto) || Objects.isNull(smsConfigDto.getSmsConfigs())
            || smsConfigDto.getSmsConfigs().size() == 0){
            throw new R200Exception("配置项不能为空！");
        }
        sysSettingService.modifySmsConfig(smsConfigDto,getUser().getUsername());
        return R.ok();
    }

    @GetMapping(value = "/queryAiRecommendSet")
    @RequiresPermissions("setting:syssetting:AiRecommend")
    @ApiOperation(value = "AI推荐设置", notes = "AI推荐设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryAIRecommendSet() {
        return R.ok().put(sysSettingService.queryAiRecommendSet());
    }

    //注册设置
    @SysLog(module = "系统设置", methodText = "AI推荐设置")
    @RequestMapping(value = "/modifyAiRecommendSet", method = RequestMethod.POST)
    @RequiresPermissions("setting:syssetting:AiRecommendEdit")
    @ApiOperation(value = "saveOrUpdate", notes = "saveOrUpdate")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modifyAiRecommendSet(@RequestBody AiRecommend aiRecommend, HttpServletRequest request) {
        sysSettingService.saveAiRecommendSet(aiRecommend, getUser().getUsername(), CommonUtil.getIpAddress(request));
        return R.ok();
    }
    /**
     *  测试接收短信
     */
    @SysLog(module = "系统设置", methodText = "测试接收短信")
    @RequestMapping(value = "/testReceiveSms", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:SmsSetQry")
    @ApiOperation(value = "系统设置", notes = "测试接收短信")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R tryReceiveSms(@ModelAttribute SmsConfig smsConfig) {
        // 入参校验
        Assert.isNull(smsConfig, "参数不能为空！");
        Assert.isNull(smsConfig.getMobile(), "手机号不能为空！");
        Assert.isNull(smsConfig.getPlatformId(), "platformId不能为空！");
        Assert.isNull(smsConfig.getTemplate(), "未配置验证码模板！");
        if(StringUtil.isEmpty(smsConfig.getMobileAreaCode())){
            smsConfig.setMobileAreaCode("86");
        }
        SmsResultDto result = sysSettingService.testReceiveSms(smsConfig);
        if(!result.isSuccess()){
            throw new R200Exception(result.getMessage());
        }
        return R.ok();
    }

    /**
     * 获取APP下载设置
     */
    @RequestMapping(value = "/queryAppDownloadSet", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:AppDownloadQry")
    @ApiOperation(value = "系统设置", notes = "APP下载设置")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryAppDownloadSet() {
        AppDownloadSet appDownloadSet = sysSettingService.queryAppDownloadSet();
        return R.ok().put(appDownloadSet);
    }

    /**
     * APP下载设置
     */
    @PostMapping(value = "/appDownloadSet")
    @RequiresPermissions("setting:syssetting:AppDownloadEdit")
    @ApiOperation(value = "系统设置推广设置", notes = "系统设置推广设置")
    public R appDownloadSet(@RequestBody AppDownloadSet appDownloadSet) {
        sysSettingService.appDownloadSet(appDownloadSet);
        return R.ok();
    }
    
    @RequestMapping(value = "/queryNativeSports", method = RequestMethod.GET)
    @RequiresPermissions("setting:syssetting:NativeSportsQry")
    @ApiOperation(value = "原生投注设置查询", notes = "原生投注设置查询")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryNativeSports() {
        return R.ok().put(sysSettingService.queryNativeSports());
    }
    
    @PostMapping(value = "/queryNativeSportsSet")
    @RequiresPermissions("setting:syssetting:NativeSportsEdit")
    @ApiOperation(value = "原生投注设置", notes = "原生投注设置")
    public R queryNativeSportsSet(@RequestBody NativeSports nativeSports) {
    	sysSettingService.updateNativeSports(nativeSports);
    	return R.ok();
    }


    @PostMapping(value = "/setCuiDanSet")
    @RequiresPermissions("setting:syssetting:setCuiDanSet")
    @ApiOperation(value = "原生投注设置", notes = "原生投注设置")
    public R setCuiDanSet(@RequestBody CuiDanSet cuiDanSet) {
        if(cuiDanSet.getCuiDan()==null){
            throw  new R200Exception("设置时间不能为空");
        }

        if(cuiDanSet.getCuiDan()< 0 ){
            throw  new R200Exception("设置时间不能为负数");
        }
        sysSettingService.updateCuiDanSet(cuiDanSet);
        return R.ok();
    }


    @GetMapping(value = "/queryCuiDanSet")
    @RequiresPermissions("setting:syssetting:queryCuiDanSet")
    @ApiOperation(value = "原生投注设置", notes = "原生投注设置")
    public R queryCuiDanSet() {
        return R.ok().put("time", sysSettingService.queryCuiDanSet());
    }

    /**
     * 获取播报通知设置
     * 新入款提醒播报、新出款提醒播报、新红利生成播报
     */
    @SysLog(module = "系统设置", methodText = "获取播报开关设置（新入款提醒播报、新出款提醒播报、新红利生成播报）")
    @RequestMapping(value = "/queryBroadcastSwitchSetting", method = RequestMethod.GET)
    // @RequiresPermissions("setting:syssetting:broadcastSwitchSettingQry")
    @ApiOperation(value = "queryBroadcastSwitchSetting", notes = "queryBroadcastSwitchSetting")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R queryBroadcastSwitchSetting() {
        return R.ok().put(sysSettingService.queryBroadcastSwitchSetting());
    }

    /**
     * 修改播报通知设置
     * 新入款提醒播报、新出款提醒播报、新红利生成播报
     */
    @SysLog(module = "系统设置", methodText = "修改播报通知设置（新入款提醒播报、新出款提醒播报、新红利生成播报）")
    @RequestMapping(value = "/updateBroadcastSwitchSetting", method = RequestMethod.POST)
    // @RequiresPermissions("setting:syssetting:broadcastSwitchSettingUpdate")
    @ApiOperation(value = "updateBroadcastSwitchSetting", notes = "updateBroadcastSwitchSetting")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R updateBroadcastSwitchSetting(@RequestBody BroadcastVo vo) {
        try {
            sysSettingService.updateBroadcastSwitchSetting(vo);
        } catch (Exception e) {
            log.error("更改广播状态异常：vo={}==", JSON.toJSONString(vo), e);
            throw new R200Exception(e.getMessage());
        }
        return R.ok().put("修改成功");
    }
    
}
