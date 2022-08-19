package com.wsdy.saasops.agapi.modules.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentService;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.AESUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgyDomain;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.agent.service.AgentDomainService;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;


@RestController
@Slf4j
@RequestMapping("/agapi")
@Api(tags = "总代设置,代理设置")
public class AgentController extends AbstractController {

    @Autowired
    private AgentService agentService;
    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private Producer producer;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private AgentAccountService accountService;
    @Autowired
    private AgentDomainService agyDomainService;

    @Value("${is.verificationCode.verify}")
    private Boolean codeVerify;

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

    @GetMapping(value = "queryStationSet")
    public R queryStationSet() {
        return R.ok().put(sysSettingService.queryApiStationSet());
    }

    @PostMapping("login")
    @ApiOperation(value = "代理接口-登陆", notes = "代理接口-登陆")
    public R login(@RequestBody AgentAccount agyAccount, HttpSession session) {
        Assert.isBlank(agyAccount.getAgyAccount(), "会员账号不能为空");
        Assert.isBlank(agyAccount.getAgyPwd(), "会员密码不能为空");
        Assert.isBlank(agyAccount.getCaptcha(), "验证码不能为空");
        agyAccount.setAgyAccount(agyAccount.getAgyAccount().toLowerCase());
        //非正式环境取消验证码校验
        if (Boolean.TRUE.equals(codeVerify)) {
            String kaptcha = apiUserService.getKaptcha(session, ApiConstants.KAPTCHA_AGENT_SESSION_KEY);
            if (kaptcha == null) {
                return R.error(2000, "验证码无法获取，联系管理员");
            }
            if (!agyAccount.getCaptcha().equalsIgnoreCase(kaptcha)) {
                return R.error(2000, "验证码不正确");
            }
        }
        return R.ok().put(agentService.agentAccountLogin(agyAccount));
    }


    @ApiOperation(value = "验证码", notes = "代理登陆获取验证码")
    @RequestMapping(value = "agentCaptcha.jpg", method = RequestMethod.GET)
    public void captcha(HttpServletResponse response, HttpSession session) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        session.setAttribute(ApiConstants.KAPTCHA_AGENT_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    @PostMapping("/sendMobCode")
    @ApiOperation(value = "代理绑定手机号", notes = "代理绑定手机号")
    @AgentLogin
    public R sendMobCode(@RequestBody AgentAccount agentAccount, HttpServletRequest request, HttpSession session) {
        String language = request.getHeader("language");
        Assert.isBlank(agentAccount.getMobile(), "手机号码不能为空!");
        Assert.isPhone(agentAccount.getMobile(), "手机号码格式错误!");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        String code = apiUserService.sendAgentSmsCode(agentAccount, account.getId(), language);
        if (!StringUtils.isEmpty(code)) {
            session.setAttribute(agentAccount.getMobile().trim() + code, code);
            return R.ok();
        } else {
            return R.error();
        }
    }

    @PostMapping("/bindAgentMobile")
    @ApiOperation(value = "代理修改手机号", notes = "代理修改手机号")
    @AgentLogin
    public R bindAgentMobile(@RequestBody AgentAccount agentAccount, HttpServletRequest request, HttpSession session) {
        Assert.isBlank(agentAccount.getMobile(), "手机号码不能为空!");
        Assert.isPhone(agentAccount.getMobile(), "手机号码格式错误!");
        Assert.isBlank(agentAccount.getCaptcha(), "验证码不能为空!");
        Assert.isNumeric(agentAccount.getCaptcha(), "验证码输入有误!");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        account.setMobile(agentAccount.getMobile());
        Object kaptchaMb = session.getAttribute(agentAccount.getMobile().trim() + agentAccount.getCaptcha());
        if (Objects.isNull(kaptchaMb) || !agentAccount.getCaptcha().equals(kaptchaMb.toString())) {
            throw new R200Exception("验证码不正确！");
        }
        return R.ok().put(agentService.sendMobCode(account));
    }


    @AgentLogin
    @GetMapping("agyAccountInfo")
    @ApiOperation(value = "代理接口-个人资料", notes = "代理接口-个人资料")
    public R agyAccountInfo(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().put("info", agentService.agyAccountInfo(account));
    }

    @AgentLogin
    @PostMapping("agyDomainSave")
    @ApiOperation(value = "代理域名新增", notes = "代理域名新增")
    public R agyAccountSave(@RequestBody AgyDomain agyDomain, HttpServletRequest request) {
        Assert.isBlank(agyDomain.getAgyAccount(), "代理帐号不能为空");
        Assert.isBlank(agyDomain.getDomainUrl(), "主域名不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        agyDomainService.agyDomainSave(agyDomain, account.getAgyAccount(), Constants.EVNumber.zero);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("agyDomainList")
    @ApiOperation(value = "代理域名查询", notes = "代理域名查询")
    public R agyDomainList(@RequestParam("pageNo") @NotNull Integer pageNo,
                           @RequestParam("pageSize") @NotNull Integer pageSize,
                           HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().putPage(agentService.agyDomainList(pageNo, pageSize, account));
    }

    @AgentLogin
    @PostMapping("agyAccountPassword")
    @ApiOperation(value = "代理重置密码", notes = "代理重置密码")
    public R agyAccountPassword(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getOldAgyPwd(), "旧密码不能为空");
        Assert.isNull(agentAccount.getAgyPwd(), "新密码不能为空");
        agentAccount.setSecurePwd(null);
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        accountService.updateAgentPassword(agentAccount, account.getAgyAccount());
        return R.ok();
    }

    @PostMapping("agyAccountFundPassword")
    @ApiOperation(value = "代理重置资金密码", notes = "代理重置资金密码")
    public R agyAccountFundPassword(@RequestBody AgentAccount agentAccount) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getSecurePwd(), "密码不能为空");
        agentAccount.setAgyPwd(null);
        accountService.updateAgentPassword(agentAccount, getUser().getUsername());
        return R.ok();
    }

}
