package com.wsdy.saasops.api.modules.user.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user/captcha")
@Api(value = "api", tags = "前端会员 验证码")
public class CaptchaOldController {

    @Autowired
    private Producer producer;
    @Autowired
    private RedisService redisService;


    @GetMapping("/reg.jpg")
    @ApiOperation(value = "会员注册验证码-已改")
    public void captchaReg(HttpServletResponse response, HttpSession session) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        // 生成文字验证码
        String text = producer.createText();
        // 生成图片验证码
        BufferedImage image = producer.createImage(text);
        session.setAttribute(ApiConstants.KAPTCHA_REG_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }


    @GetMapping("/login.jpg")
    @ApiOperation(value = "会员登陆验证码-已改")
    public void captchaLogin(HttpServletResponse response, HttpSession session) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        try {
            response.setHeader("Cache-Control", "no-store, no-cache");
            response.setContentType("image/jpeg");
            // 生成文字验证码
            String text = producer.createText();
            // 生成图片验证码
            BufferedImage image = producer.createImage(text);
            // 保存到shiro session
            session.setAttribute(ApiConstants.KAPTCHA_LOGIN_SESSION_KEY, text);
            ServletOutputStream out = response.getOutputStream();
            ImageIO.write(image, "jpg", out);
            IOUtils.closeQuietly(out);
        } catch (Exception e) {
            log.error("error:" + e);
            response.getWriter().print("----------Captcha Create Error!!!!!!");
        } finally {
            String endDate = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
            log.debug("==============================start time>:" + endDate + "=============================================");
        }
    }


    @GetMapping("/retrvpwd.jpg")
    @ApiOperation(value = "会员找回密码验证码-已改")
    public void captchaFindPwd(HttpServletResponse response, HttpSession session) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        // 生成文字验证码
        String text = producer.createText();
        // 生成图片验证码
        BufferedImage image = producer.createImage(text);
        session.setAttribute(ApiConstants.KAPTCHA_RETRVPWD_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }


    @GetMapping("/addAccount.jpg")
    @ApiOperation(value = "添加好友验证码-已改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    public void captchaAddAccount(HttpServletResponse response, HttpSession session) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        // 生成文字验证码
        String text = producer.createText();
        // 生成图片验证码
        BufferedImage image = producer.createImage(text);
        session.setAttribute(ApiConstants.KAPTCHA_ADDACCOUNT_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    @GetMapping("jpg")
    @ApiOperation(value = "图形验证码")
    public R captcha() {
        // 生成4位随机数字
        String text = producer.createText();
        String code;        // 图片编码
        log.info("验证码==text==" + text );

        // 选择返回类型
        long time = new Date().getTime();
        if(time % 2 == 0){   // 偶数:1位数加法
            String str1 = text.substring(0, 1);
            String str2 = text.substring(2, 3);
            text = String.valueOf(Integer.valueOf(str1) + Integer.valueOf(str2));
            log.info("验证码==str1==" + str1 + "==str2==" + str2 + "==text==" + text);
            BufferedImage image = producer.createImage(str1 + "+" + str2 + "=?");
            code = CommonUtil.getBase64FromInputStream(image);
        }else{  // 奇数：4位数字
            BufferedImage image = producer.createImage(text);
            code = CommonUtil.getBase64FromInputStream(image);
        }

        // 生成redis key, 并设置
        String codeSign = UUID.randomUUID().toString().replaceAll("-", "");
        redisService.setRedisExpiredTime(codeSign, text, 10, TimeUnit.MINUTES);

        return R.ok().put("code", code).put("codeSign", codeSign);
    }

}
