package com.wsdy.saasops.api.modules.user.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.user.dto.FindPwdDto;
import com.wsdy.saasops.api.modules.user.dto.ModPwdDto;
import com.wsdy.saasops.api.modules.user.entity.FindPwEntity;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * 注册
 */
@Slf4j
@RestController
@RequestMapping("/api/retrvpwd")
@Api(value = "api", tags = "前端找回密码接口")
public class ApiRetrvPwdController {

    @Autowired
    MbrAccountService mbrAccountService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private CaptchaService captchaService;

    @PostMapping("/validUser")
    @ApiOperation(value = "第一步：会员验证", notes = "第一步：会员验证!")
    public R fpUser(@RequestBody FindPwdDto model, HttpSession session) {
        Assert.isBlank(model.getUserName(), "用户名不能为空");
        model.setUserName(model.getUserName().toLowerCase());
        String kaptcha = "";
        if( String.valueOf(Constants.EVNumber.one).equals(model.getCodeFlag()) || StringUtils.isEmpty(model.getCodeFlag())){
            if (StringUtils.isNotEmpty(model.getCodeSign())) {
                kaptcha = redisService.getKeyAndDel(model.getCodeSign());
            } else {
                kaptcha = apiUserService.getKaptcha(session, ApiConstants.KAPTCHA_RETRVPWD_SESSION_KEY);
            }
            if (!model.getCaptcha().equalsIgnoreCase(kaptcha)) {
                Assert.message("验证码不正确!");
            }
        }
        if (mbrAccountService.getAccountNum(model.getUserName()) == 0) {
            return R.error("账号不存在 请先注册！");
        } else {
            LinkedHashMap<String, Object> userInfo = mbrAccountService.webFindPwdUserInfo(model.getUserName());
            return R.ok(getFindPwdToken(model.getUserName())).put("userInfo", userInfo);
        }

    }

    @GetMapping("/retrvway")
    @ApiOperation(value = "第二步：会员找回密码方式(邮件,短信)", notes = "validType 会员找回密码方式,0 邮箱,1短信")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R retrvway(@RequestParam("validType") String validType, HttpServletRequest request) {
        String language = request.getHeader("language");
        Assert.isBlank(validType, "找回方式不能为空!");
        if ((!"1".equals(validType)) && (!"0".equals(validType))) {
            Assert.isNull(validType, "没有此种方式找回密码!");
        }
        ValidRegUtils.checkFpValid(request, jwtUtils);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        FindPwEntity findPwEntity = new FindPwEntity();
        findPwEntity.setExpire(jwtUtils.getExpire());
        findPwEntity.setLoginName(loginName);
        findPwEntity.setVaildTimes(0);
        findPwEntity.setVaildType(new Byte(validType));
        findPwEntity.setApplyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        if ("0".equals(validType)) {
            apiUserService.saveMailCode(findPwEntity, cpSite.getSiteCode());
        } else {
            apiUserService.saveSmsCode(findPwEntity, language);
        }
        return R.ok();
    }

    @GetMapping("/validCode")
    @ApiOperation(value = "第二步：发邮短信或邮箱后，验证code", notes = "第二步：发邮短信或邮箱后，验证code,特别注意此处的TOKEN与第一步产生的TOKEN是不一样的!")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R validCode(@RequestParam(value = "code") String code, HttpServletRequest request) {
        Assert.isBlank(code, "验证码不能为空!");
        ValidRegUtils.checkFpValid(request, jwtUtils);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        boolean flag = apiUserService.validCode(code, loginName);
        if (!flag) {
            throw new RRException("错误验证码,请重试!");
        }
        return R.ok(getPassValidToken(loginName, code));
    }

    @GetMapping("/modpwd")
    @ApiOperation(value = "第三步：会员密码修改", notes = "第三步：会员密码修改!")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modpwd(@RequestParam("password") String password, HttpServletRequest request) {
        //Assert.isBlank(modPwd.getCode(), "验证码不能为空!");
        //Assert.isBlank(password, "密码不能为空!");
        Assert.isLenght(password, "会员密码,长度为6~18位!", 6, 18);
        ValidRegUtils.checkFpValid(request, jwtUtils);
        String str = (String) request.getAttribute(ApiConstants.USER_NAME);
        String[] urerInfos = str.split(ApiConstants.USER_TOKEN_SPLIT);
        if (urerInfos == null || urerInfos.length != 2) {
            throw new R200Exception("错误的Token!");
        }
        ModPwdDto modPwdDto = new ModPwdDto();
        modPwdDto.setCode(urerInfos[0]);
        modPwdDto.setLoginName(urerInfos[1]);
        modPwdDto.setPassword(password);
        Assert.accEqualPwd(modPwdDto.getLoginName(), modPwdDto.getPassword(), "会员密码不能与账号相同!");
        boolean flag = apiUserService.modPwd(modPwdDto, CommonUtil.getSiteCode());
        if (!flag) {
            throw new R200Exception("错误验证码,请重试!");
        }
        return R.ok();
    }

    public Map<String, Object> getFindPwdToken(String loginName) {
        String token = jwtUtils.generatefindPwdToken(loginName);
        return generateToken(token);
    }

    public Map<String, Object> getPassValidToken(String loginName, String code) {
        String token = jwtUtils.generatefindPwdToken(loginName, code);
        return generateToken(token);
    }

    private Map<String, Object> generateToken(String token) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("token", token);
        map.put("expire", jwtUtils.getExpireFindPwd());
        return map;
    }

    @GetMapping("/retrvwayEx")
    @ApiOperation(value = "wap端第一步使用手机号码，其余复用之前的逻辑", notes = "retrvwayEx->validCode->modpwd")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R retrvwayEx(@ModelAttribute FindPwdDto dto, HttpServletRequest request) {
        String language = request.getHeader("language");
		log.info("retrvwayEx接收参数 --- CaptchaVerificatio:{}, codeSign:{}, Mobile:{}", dto.getCaptchaVerification(),
				dto.getCodeSign(), dto.getMobile());
        Assert.isPhoneAll(dto.getMobile(), dto.getMobileAreaCode());

        if(!StringUtil.isEmpty(dto.getCaptchaVerification())){   // 行为验证码
            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setCaptchaVerification(dto.getCaptchaVerification());
            ResponseModel response = captchaService.verification(captchaVO);
            if(response.isSuccess() == false) {
                //验证码校验失败，返回信息告诉前端
                //repCode  0000  无异常，代表成功
                //repCode  9999  服务器内部异常
                //repCode  0011  参数不能为空
                //repCode  6110  验证码已失效，请重新获取
                //repCode  6111  验证失败
                //repCode  6112  获取验证码失败,请联系管理员
                switch (response.getRepCode()){
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
                throw new R200Exception(response.getRepMsg()+"["+response.getRepCode()+"]");
            }
        }else{
            Assert.isNull(dto.getCodeSign(), "error!");
            Assert.isNull(dto.getKaptcha(), "error!");
            String  captchareg= redisService.getKeyAndDel(dto.getCodeSign());
            if(!dto.getKaptcha().equals(captchareg)){
                return R.error("图形验证码错误！");
            }
        }

        // 通过手机号，找到会员
        List<MbrAccount> mbrList = mbrAccountService.getAccountInfoByMobile(dto.getMobile(),null);
        if(Objects.isNull(mbrList)){
            return R.error("该手机号未注册！");
        }
        if(mbrList.size() == Constants.EVNumber.zero){
            return R.error("该手机号未注册！");
        }
        if (mbrList.size() != Constants.EVNumber.one) { // 数据库存在多个电话号码
            return R.error("异常情况，请联系客服处理！");
        }
        // 通过用户名生成token
        Map<String, Object> tokenMap = getFindPwdToken(mbrList.get(0).getLoginName());
        FindPwEntity findPwEntity = new FindPwEntity();
        findPwEntity.setExpire(jwtUtils.getExpire());
        findPwEntity.setLoginName(mbrList.get(0).getLoginName());
        findPwEntity.setVaildTimes(0);
        findPwEntity.setVaildType(new Byte("1"));   // validType 会员找回密码方式,0 邮箱,1短信
        findPwEntity.setApplyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        findPwEntity.setMobileAreaCode(dto.getMobileAreaCode());
        apiUserService.saveSmsCode(findPwEntity, language);
        return R.ok(tokenMap);
    }

}
