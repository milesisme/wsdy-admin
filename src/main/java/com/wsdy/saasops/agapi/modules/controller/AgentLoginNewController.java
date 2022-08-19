package com.wsdy.saasops.agapi.modules.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.google.code.kaptcha.Producer;
import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.dto.DataTrendParamDto;
import com.wsdy.saasops.agapi.modules.service.AgentNewService;
import com.wsdy.saasops.agapi.modules.service.AgentSafeyInfoService;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.user.dto.VfyMailOrMobDto;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.AESUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/agapi/n2")
@Api(tags = "代理")
public class AgentLoginNewController {

    @Autowired
    private AgentNewService agentService;
    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private Producer producer;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private AgentSafeyInfoService safeyInfoService;
    @Autowired
    private CaptchaService captchaService;

    @GetMapping(value = "getSiteCode")
    public R getSiteCode(@RequestParam("url") String url) {
        TCpSite tCpSite = tCpSiteService.queryOneCond(url);
        try {
            return R.ok().put(SystemConstants.STOKEN, AESUtil.encrypt(tCpSite.getSiteCode()));
        } catch (Exception e) {
            log.error("getSiteCode", e);
        }
        return R.ok();
    }

    @GetMapping("/getSerUrl")
    @ApiOperation(value = "返回客服链接", notes = "返回客服链接")
    public R getSerUrl(@ApiParam("默认为PC ,PC 0,手机为 1") @RequestParam(value = "terminal", required = false) Byte terminal) {
        return R.ok().put(sysSettingService.getCustomerSerUrl(terminal));
    }

    @GetMapping("jpg")
    @ApiOperation(value = "图形验证码")
    public R captcha() {
        String text = producer.createText();
        log.info("图形验证码:" + text);
        BufferedImage image = producer.createImage(text);
        String code = CommonUtil.getBase64FromInputStream(image);
        String codeSign = UUID.randomUUID().toString().replaceAll("-", "");
        redisService.setRedisExpiredTime(codeSign, text, 10, TimeUnit.MINUTES);
        return R.ok().put("code", code).put("codeSign", codeSign);
    }

    @PostMapping("/sendMobCodeReg")
    @ApiOperation(value = "会员手机验证(注册)", notes = "会员手机验证")
    public R sendMobCodeReg(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        String language = request.getHeader("language");
        Assert.isPhoneAll(vfyDto.getMobile(), vfyDto.getMobileAreaCode());

        if (!StringUtil.isEmpty(vfyDto.getCaptchaVerification())) {   // 行为验证码
            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setCaptchaVerification(vfyDto.getCaptchaVerification());
            ResponseModel response = captchaService.verification(captchaVO);
            if (response.isSuccess() == false) {
                //验证码校验失败，返回信息告诉前端
                //repCode  0000  无异常，代表成功
                //repCode  9999  服务器内部异常
                //repCode  0011  参数不能为空
                //repCode  6110  验证码已失效，请重新获取
                //repCode  6111  验证失败
                //repCode  6112  获取验证码失败,请联系管理员

                switch (response.getRepCode()) {
                    case "9999":
                        response.setRepMsg("服务器异常,请联系客服");
                        break;
                    case "0011":
                        response.setRepMsg("安全验证失效，请重试");
                        break;
                    case "6110":
                        response.setRepMsg("安全验证失效，请重试");
                        break;
                    case "6111":
                        response.setRepMsg("验证失败，请重试");
                        break;
                    case "6112":
                        response.setRepMsg("未知异常,请联系客服");
                        break;
                    default:
                        response.setRepMsg("未知异常,请联系客服");
                }
                throw new R200Exception(response.getRepMsg() + "[" + response.getRepCode() + "]");
            }
        } else {
            Assert.isNull(vfyDto.getCodeSign(), "error1!");
            Assert.isNull(vfyDto.getKaptcha(), "error2!");
            String kaptcha = redisService.getKeyAndDel(vfyDto.getCodeSign());
            if (!vfyDto.getKaptcha().equals(kaptcha)) {
                return R.error("图形验证码错误！");
            }
        }

        agentService.checkoutAgentMobile(vfyDto.getMobile());
        String code = apiUserService.sendAgentSmsRegCode(vfyDto.getMobile(), vfyDto.getMobileAreaCode(), Constants.EVNumber.one, language);
        if (!StringUtils.isEmpty(code)) {
            log.info("代理短信注册验证码" + code + "," + vfyDto.getMobile());
            String key = RedisConstants.AGENT_REDIS_MOBILE_REGISTE_CODE + CommonUtil.getSiteCode() + vfyDto.getMobile();
            redisService.setRedisExpiredTime(key, code, 10, TimeUnit.MINUTES);
            return R.ok();
        }
        return R.error();
    }

    @PostMapping("register")
    @ApiOperation(value = "代理接口-注册", notes = "代理接口-注册")
    public R register(@RequestBody AgentAccount agyAccount) {
        Assert.isBlank(agyAccount.getAgyAccount(), "会员账号不能为空");
        ValidRegUtils.validAgentName(agyAccount.getAgyAccount(), SysSetting.SysValueConst.require);
        agyAccount.setAgyAccount(agyAccount.getAgyAccount().toLowerCase());
        agentService.checkoutUsername(agyAccount.getAgyAccount());
        Assert.isBlank(agyAccount.getAgyPwd(), "密码不能为空");
        Assert.isPhone(agyAccount.getMobile(), "手机号码格式错误!");
        Assert.isBlank(agyAccount.getMobileCaptchareg(), "短信验证码不能为空");
        agentService.checkoutAgentMobile(agyAccount.getMobile());
        String key = RedisConstants.AGENT_REDIS_MOBILE_REGISTE_CODE + CommonUtil.getSiteCode() + agyAccount.getMobile();
        Object obj = redisService.getRedisValus(key);
        if (Objects.isNull(obj)) {
            throw new R200Exception("确认时间超过10分钟，请重新注册！");
        }
        if (!agyAccount.getMobileCaptchareg().equalsIgnoreCase(obj.toString())) {
            throw new R200Exception("验证码不正确!");
        }
        Map<String, Object> objectMap = agentService.agentRegister(agyAccount);
        return R.ok().put(objectMap.get("agentToken"));
    }

    @PostMapping("perfectInfo")
    @ApiOperation(value = "代理接口-注册完善信息", notes = "代理接口-注册完善信息")
    public R perfectInfo(@RequestBody AgentAccount agyAccount) {
        Assert.isBlank(agyAccount.getAgentToken(), "agentToken不能为空");
        AgentAccount account = agentService.getAgentAccountByToken(agyAccount);
        safeyInfoService.updateContact(account, agyAccount);
        return R.ok();
    }

    @PostMapping("login")
    @ApiOperation(value = "代理接口-登陆", notes = "代理接口-登陆")
    public R login(@RequestBody AgentAccount agyAccount) {
        Assert.isBlank(agyAccount.getAgyAccount(), "会员账号不能为空");
        Assert.isBlank(agyAccount.getAgyPwd(), "会员密码不能为空");
        Assert.isBlank(agyAccount.getCaptcha(), "验证码不能为空");
        agyAccount.setAgyAccount(agyAccount.getAgyAccount().toLowerCase());
        String kaptcha = redisService.getKeyAndDel(agyAccount.getCodeSign());
        if (!agyAccount.getCaptcha().equals(kaptcha)) {
            return R.error("图形验证码错误！");
        }
        return R.ok().put(agentService.agentAccountLogin(agyAccount));
    }

    @AgentLogin
    @GetMapping("loginOut")
    @ApiOperation(value = "登出", notes = "根据当前TOKEN 登出此账号!")
    public R loginOut() {
        return R.ok(Boolean.TRUE);
    }


    @AgentLogin
    @GetMapping("overview")
    @ApiOperation(value = "代理接口-首页总览", notes = "代理接口-首页总览")
    public R agent0verview(@RequestParam("startTime") String startTime, @RequestParam("endTime") String entTime, HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().put(agentService.agent0verview(startTime, entTime, account));
    }

    @AgentLogin
    @GetMapping("dataTrendList")
    @ApiOperation(value = "代理接口-首页总览-数据走势", notes = "代理接口-首页总览-数据走势")
    public R dataTrendList(@ModelAttribute DataTrendParamDto dto,
                           HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().put(agentService.dataTrendList(account, dto));
    }
}
