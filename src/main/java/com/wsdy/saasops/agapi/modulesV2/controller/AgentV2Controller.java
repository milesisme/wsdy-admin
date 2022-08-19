package com.wsdy.saasops.agapi.modulesV2.controller;

import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modulesV2.service.AgentV2Service;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.AESUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;


@RestController
@Slf4j
@RequestMapping("/agapi/v2")
@Api(tags = "外围系统")
public class AgentV2Controller extends AbstractController {

    @Autowired
    private AgentV2Service agentService;
    @Autowired
    private TCpSiteService tCpSiteService;
    @Autowired
    private Producer producer;
    @Autowired
    private ApiUserService apiUserService;

//    @Value("${is.verificationCode.verify}")
//    private Boolean codeVerify;

    @GetMapping(value = "/getSiteCode")
    public R getSiteCode(@RequestParam("url") String url) {
        TCpSite tCpSite = tCpSiteService.queryOneCond(url);
        try {
            return R.ok().put(SystemConstants.STOKEN, AESUtil.encrypt(tCpSite.getSiteCode()));
        } catch (Exception e) {
            log.error("getSiteCode", e);
        }
        return R.ok();
    }

    @PostMapping("/login")
    @ApiOperation(value = "代理接口-登陆", notes = "代理接口-登陆")
    public R login(@RequestBody AgentAccount agyAccount, HttpServletRequest request) {
        Assert.isBlank(agyAccount.getAgyAccount(), "会员账号不能为空");
        Assert.isBlank(agyAccount.getAgyPwd(), "会员密码不能为空");

        String siteCode = CommonUtil.getSiteCode();

        return R.ok().put(agentService.agentAccountLogin(agyAccount,siteCode,request));
    }


    @ApiOperation(value = "验证码", notes = "代理登陆获取验证码")
    @RequestMapping(value = "/agentCaptcha.jpg", method = RequestMethod.GET)
    public void captcha(HttpServletResponse response, HttpSession session) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        session.setAttribute(ApiConstants.KAPTCHA_AGENT_V2_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    @AgentLogin
    @GetMapping("/agyAccountInfo")
    @ApiOperation(value = "账户概览-账户资料", notes = "获取账户资料")
    public R agyAccountInfo(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().put("info", agentService.agyAccountInfo(account));
    }

    @AgentLogin
    @PostMapping("/sendMobCode")
    @ApiOperation(value = "账户概览-代理绑定手机号-发送短信验证码", notes = "代理绑定手机号-发送短信验证码")
    public R sendMobCode(@RequestBody AgentAccount agentAccount, HttpServletRequest request, HttpSession session) {
        String language = request.getHeader("language");
        Assert.isBlank(agentAccount.getMobile(), "手机号码不能为空!");
        Assert.isPhone(agentAccount.getMobile(), "手机号码格式错误!");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        String code = apiUserService.sendAgentSmsCode(agentAccount, account.getId(), language);
        if (!StringUtils.isEmpty(code)) {
            session.setAttribute(account.getId() +agentAccount.getMobile().trim()+code, code);
            return R.ok();
        } else {
            return R.error();
        }
    }

    @PostMapping("/bindAgentMobile")
    @ApiOperation(value = "账户概览-代理绑定手机号", notes = "代理绑定手机号")
    @AgentLogin
    public R bindAgentMobile(@RequestBody AgentAccount agentAccount, HttpServletRequest request, HttpSession session) {
        Assert.isBlank(agentAccount.getMobile(), "手机号码不能为空!");
        Assert.isPhone(agentAccount.getMobile(), "手机号码格式错误!");
        Assert.isBlank(agentAccount.getCaptcha(), "验证码不能为空!");
        Assert.isNumeric(agentAccount.getCaptcha(), "验证码输入有误!");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        account.setMobile(agentAccount.getMobile());
        Object kaptchaMb = session.getAttribute(account.getId() + agentAccount.getMobile().trim()+agentAccount.getCaptcha());
        if(Objects.isNull(kaptchaMb) || !agentAccount.getCaptcha().equals(kaptchaMb.toString())){
            throw new R200Exception("验证码不正确！");
        }
        return R.ok().put(agentService.bindAgentMobile(account));
    }

    @AgentLogin
    @PostMapping("agyAccountPassword")
    @ApiOperation(value = "账户概览-修改密码", notes = "修改密码")
    public R agyAccountPassword(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isNull(agentAccount.getId(), "代理id不能为空");
        Assert.isNull(agentAccount.getOldAgyPwd(), "旧密码不能为空");
        Assert.isNull(agentAccount.getAgyPwd(), "新密码不能为空");
        agentAccount.setSecurePwd(null);
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        agentService.updateAgentPassword(agentAccount, account);
        return R.ok();
    }


    @AgentLogin
    @GetMapping("/search")
    @ApiOperation(value = "搜寻", notes = "搜寻")
    public R agyAccountInfo(@ModelAttribute AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isNull(agentAccount.getSearchName(), "名称不能为空！");

        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if(Objects.isNull(account)){
            throw new RRException("用户登录失效，请重新登录！");
        }
        account.setSearchName(agentAccount.getSearchName());
        return R.ok().put("data",agentService.search(account));
    }
}
