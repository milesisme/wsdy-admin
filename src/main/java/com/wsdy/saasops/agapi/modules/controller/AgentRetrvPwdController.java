package com.wsdy.saasops.agapi.modules.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.google.common.collect.ImmutableMap;
import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentService;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.dto.FindPwdDto;
import com.wsdy.saasops.api.modules.user.dto.ModPwdDto;
import com.wsdy.saasops.api.modules.user.entity.FindPwEntity;
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
import com.wsdy.saasops.modules.agent.entity.AgentAccount;

import io.jsonwebtoken.lang.Collections;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Staff
 *
 */
@Slf4j
@RestController
@RequestMapping("/agapi/n2/retrvpwd")
@Api(value = "agy", tags = "代理找回密码接口")
public class AgentRetrvPwdController {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private CaptchaService captchaService;
    
    @GetMapping("/retrvwayEx")
    @ApiOperation(value = "第一步使用手机号码，获取验证码，返回token", notes = "retrvwayEx->validCode->modpwd")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R retrvwayEx(@ModelAttribute FindPwdDto dto) {
		log.info("retrvwayEx接收参数 --- CaptchaVerificatio:{}, codeSign:{}, Mobile:{}", dto.getCaptchaVerification(),
				dto.getCodeSign(), dto.getMobile());
        Assert.isPhoneAll(dto.getMobile(), dto.getMobileAreaCode());

        // 通过手机号，找到代理
        List<AgentAccount> agyInfoByMobile = agentService.getAgyInfoByMobile(dto.getMobile());
        if(Collections.isEmpty(agyInfoByMobile)){
            return R.error("该手机号未注册！");
        }
        if (agyInfoByMobile.size() > Constants.EVNumber.one) { // 数据库存在多个电话号码
            return R.error("异常情况，请联系客服处理！");
        }
        // 通过代理名生成token
        Map<String, Object> tokenMap = getFindPwdToken(agyInfoByMobile.get(0).getId());
        FindPwEntity findPwEntity = new FindPwEntity();
        findPwEntity.setExpire(jwtUtils.getExpire());
        findPwEntity.setLoginName(agyInfoByMobile.get(0).getAgyAccount());
        findPwEntity.setVaildTimes(0);
        findPwEntity.setVaildType(new Byte("1"));   // validType 会员找回密码方式,0 邮箱,1短信
        findPwEntity.setApplyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        findPwEntity.setMobileAreaCode(dto.getMobileAreaCode());
        agentService.saveSmsCode(findPwEntity);
        return R.ok(tokenMap);
    }


    @GetMapping("/validCode")
    @ApiOperation(value = "第二步：发邮短信或邮箱后，验证code", notes = "第二步：发邮短信或邮箱后，验证code,特别注意此处的TOKEN与第一步产生的TOKEN是不一样的!")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R validCode(@RequestParam(value = "code") String code, HttpServletRequest request) {
        Assert.isBlank(code, "验证码不能为空!");
        ValidRegUtils.checkAgyFpValid(request, jwtUtils);
        
        Integer id = Integer.parseInt(request.getAttribute(Constants.AGENT_ID).toString());
        AgentAccount queryObject = agentService.queryObject(id);
        boolean flag = agentService.validCode(code, queryObject.getAgyAccount());
        if (!flag) {
            throw new RRException("错误验证码,请重试!");
        }
        return R.ok(getPassValidToken(id, code));
    }

    @GetMapping("/modpwd")
    @ApiOperation(value = "第三步：代理密码修改", notes = "第三步：代理密码修改!")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R modpwd(@RequestParam("password") String password, HttpServletRequest request) {
        Assert.isLenght(password, "代理密码,长度为6~18位!", 6, 18);
        
        ValidRegUtils.checkAgyFpValid(request, jwtUtils);        
        String str = (String) request.getAttribute(Constants.AGENT_ID);
        String[] urerInfos = str.split(ApiConstants.USER_TOKEN_SPLIT);
        if (urerInfos == null || urerInfos.length != 2) {
            throw new R200Exception("错误的Token!");
        }
        AgentAccount account = agentService.queryObject(Integer.parseInt(urerInfos[1]));
        
        ModPwdDto modPwdDto = new ModPwdDto();
        modPwdDto.setCode(urerInfos[0]);
        modPwdDto.setLoginName(account.getAgyAccount());
        modPwdDto.setPassword(password);
        Assert.accEqualPwd(modPwdDto.getLoginName(), modPwdDto.getPassword(), "代理密码不能与账号相同!");
        boolean flag = agentService.modPwd(modPwdDto, CommonUtil.getSiteCode());
        if (!flag) {
            throw new R200Exception("错误验证码,请重试!");
        }
        return R.ok();
    }

    /**
     * 	根据代理id生成token
     * @param id
     * @return
     */
    public Map<String, Object> getFindPwdToken(Integer id) {
    	String agentToken = jwtUtils.agentGeneratefindPwdToken(String.valueOf(id));
        return ImmutableMap.of("token", agentToken, "expire", jwtUtils.getExpireFindPwd());
    }

    /**
     * 	根据代理id + code 生成token
     * @param id
     * @param code
     * @return
     */
    public Map<String, Object> getPassValidToken(Integer id, String code) {
        String agentToken = jwtUtils.agentGeneratefindPwdToken(String.valueOf(id), code);
        return ImmutableMap.of("token", agentToken, "expire", jwtUtils.getExpireFindPwd());
    }


}
