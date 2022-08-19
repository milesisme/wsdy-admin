package com.wsdy.saasops.modules.sys.controller;

import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.service.TCpSiteService;
import com.wsdy.saasops.common.annotation.SysLog;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.utils.AESUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.google.QRCodeUtils;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.controller.AbstractController;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.sys.dto.AuthenticatorDto;
import com.wsdy.saasops.modules.sys.entity.LoggingParam;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import com.wsdy.saasops.modules.sys.service.SysUserService;
import com.wsdy.saasops.modules.sys.service.SysUserTokenService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * 登录相关
 */
@Slf4j
@RestController
@RequestMapping("/bkapi/sys/")
public class SysLoginController extends AbstractController {

    @Autowired
    private Producer producer;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserTokenService sysUserTokenService;
    @Autowired
    private TCpSiteService tCpSiteService;
    @Value("${is.verificationCode.verify}")
    private Boolean codeVerify;
    @Autowired
    private TCpSiteService cpSiteService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    SysSettingService sysSettingService;


    @SysLog(module = "验证码", methodText = "后台系统登录获取验证码")
    @RequestMapping(value = "captcha.jpg", method = RequestMethod.GET)
    public void captcha(HttpServletResponse response, HttpSession session) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpg");
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        session.setAttribute(Constants.KAPTCHA_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    @GetMapping(value = "googleAvailable")
    public R getGoogleAvailable() {
        TCpSite cpSite = cpSiteService.queryPreOneCond(CommonUtil.getSiteCode());
        if (Objects.isNull(cpSite.getGoogleAvailable()) ||
                cpSite.getGoogleAvailable() == com.wsdy.saasops.common.constants.Constants.EVNumber.zero) {
            return new R().put(Boolean.TRUE);
        }
        return new R().put(Boolean.FALSE);
    }

    @PostMapping(value = "authenticatorLogin")
    @ApiImplicitParams({@ApiImplicitParam(name = "SToken", value = "token头部，随便填数字", required = true, dataType = "String", paramType = "header")})
    public R authenticatorLogin(@RequestBody LoggingParam loggingParam, HttpServletRequest request) {
        Assert.isBlank(loggingParam.getUsername(), "用户名不能为空");
        Assert.isBlank(loggingParam.getPassword(), "密码不能为空");
        loggingParam.setUsername(loggingParam.getUsername().toLowerCase());
        TCpSite cpSite = cpSiteService.queryPreOneCond(CommonUtil.getSiteCode());
        if (Objects.isNull(cpSite.getGoogleAvailable()) ||
                cpSite.getGoogleAvailable() == com.wsdy.saasops.common.constants.Constants.EVNumber.zero) {
            R r = sysUserService.userLogin(loggingParam, request.getRequestURL().toString(), Boolean.FALSE);
            return r.put("verify", com.wsdy.saasops.common.constants.Constants.EVNumber.zero);
        }
        SysUserEntity userEntity = sysUserService.checkoutUser(loggingParam.getUsername(), loggingParam.getPassword());
        int verify = com.wsdy.saasops.common.constants.Constants.EVNumber.one;
        String qrCode = "";
        if (StringUtils.isEmpty(userEntity.getAuthenticatorKey()) || Objects.isNull(userEntity.getAuthenticatorLogin())
                || userEntity.getAuthenticatorLogin() != 1) {
            AuthenticatorDto dto = sysUserService.getAuthenticator(userEntity, cpSite.getSiteCode(), userEntity.getAuthenticatorKey());
            verify = com.wsdy.saasops.common.constants.Constants.EVNumber.two;
            qrCode = CommonUtil.getBase64FromInputStream(QRCodeUtils.toBufferedImage(dto.getUrl(), 200, 200));
        }
        return new R().put("verify", verify).put("qrCode", qrCode);
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Map<String, Object> login(@RequestBody LoggingParam loggingParam, HttpServletRequest request) {
        Assert.isBlank(loggingParam.getUsername(), "用户名不能为空");
        Assert.isBlank(loggingParam.getPassword(), "密码不能为空");
        Assert.isBlank(loggingParam.getCaptcha(), "身份验证码不能为空");
        loggingParam.setUsername(loggingParam.getUsername().toLowerCase());
        return sysUserService.userLogin(loggingParam, request.getRequestURL().toString(), Boolean.TRUE);
    }

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

    @GetMapping(value = "/queryDomain")
    public R queryDomain() {
        return R.ok().put(tCpSiteService.queryDomain(CommonUtil.getSiteCode()));
    }


    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public R logout(HttpServletRequest request) {
        sysUserTokenService.logout(getUserId());
        //操作日志
        mbrAccountLogService.logoutLog(getUser().getUsername(), CommonUtil.getIpAddress(request));

        return R.ok();
    }

    @GetMapping(value = "/getI18n")
    public R getI18nFlag() {
        TCpSite cpSite = cpSiteService.queryPreOneCondNoCach(CommonUtil.getSiteCode());
        if (Objects.nonNull(cpSite.getIsI18n()) &&
                cpSite.getIsI18n() == com.wsdy.saasops.common.constants.Constants.EVNumber.one) {
            return new R().put("isI18n",cpSite.getIsI18n()).put("language",cpSite.getLanguage());
        }
        return new R().put("isI18n", com.wsdy.saasops.common.constants.Constants.EVNumber.zero);
    }

    // 获取EG三公标志
    @GetMapping(value = "/getEgSanGongFlg")
    public R getEgSanGongFlg() {
        SysSetting setting = sysSettingService.getSysSetting(SystemConstants.EG_SANGONG_FLG);
        return R.ok().put("egSanGongFlg",setting.getSysvalue());
    }
}
