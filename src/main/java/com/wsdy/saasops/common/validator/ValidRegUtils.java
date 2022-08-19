package com.wsdy.saasops.common.validator;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.dto.LoginUserDto;
import com.wsdy.saasops.api.modules.user.dto.UserDto;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting.SysValueConst;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class ValidRegUtils {
    @Autowired
    private CaptchaService captchaServiceEx;
    @Autowired
    private static CaptchaService captchaService;
    @Autowired
    private static RedisService redisService;

    @PostConstruct
    public void init() {
        captchaService = captchaServiceEx;
    }

    public static void validateReg(MbrAccount mbrAccount, List<SysSetting> list, String kaptcha, String kaptchaMb,int captchaType,int ipReg,int devicReg) {
        for (SysSetting seting : list) {
            switch (seting.getSyskey()) {
                case SystemConstants.MEMBER_ACCOUNT:
                    validloginName(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_LOGIN_PASSWORD:
                    validPwd(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_REAL_NAME:
                    validRealName(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_TELPHONE:
                    validPhone(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_TELPHONE_CODE:
                    phoneValidCaptchar(mbrAccount, seting.getSysvalue(), kaptchaMb);
                    break;
                case SystemConstants.MEMBER_EMAIL:
                    validEmail(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_QQ:
                    validQQ(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_WECHAT:
                    validWeChat(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_ADDRESS:
                    validAdress(mbrAccount, seting.getSysvalue());
                    break;
                case SystemConstants.MEMBER_IP:   //IP校验
                    validIp(seting.getSysvalue(),ipReg);
                    break;
                case SystemConstants.MEMBER_DEVICE: //设备号校验
                    validDevice(seting.getSysvalue(),devicReg);
                    break;
                default:
            }
        }
    }

    public static void validateRegEx(MbrAccount mbrAccount) {
        // 校验用户名
        validloginName(mbrAccount, SysValueConst.require);
        // 校验密码
        validPwd(mbrAccount, SysValueConst.require);
        // 校验手机号
        validPhone(mbrAccount, SysValueConst.require);
    }

    public static void validAgentName(String agyAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(agyAccount)) {
                    Assert.isAccount(agyAccount, "代理账号,长度为6~10位!");
                    Assert.isCharacter(agyAccount, "用户名以字母开头6~10位数字、字母组合组成!");
                }
                break;
            case SysValueConst.require:
                Assert.isBlank(agyAccount, "代理账号输入不能为空，请重新输入!");
                Assert.isAccount(agyAccount, "代理账号,长度为6~10位!");
                Assert.isCharacter(agyAccount, "代理账号为6~10 位数字、字母或组合");
                break;
            default:
        }
    }


    public static void validloginName(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setLoginName("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getLoginName())) {
                    Assert.isAccount(mbrAccount.getLoginName(), "会员账号,长度为6~10位!");
                    Assert.isCharacter(mbrAccount.getLoginName(), "用户名以字母开头6~10位数字、字母组合组成!");
                }
                break;
            case SysValueConst.require:
                Assert.isBlank(mbrAccount.getLoginName(), "用户名输入不能为空，请重新输入!");
                Assert.isAccount(mbrAccount.getLoginName(), "会员账号,长度为6~10位!");
                Assert.isCharacter(mbrAccount.getLoginName(), "用户名以字母开头6~10位数字、字母组合组成!");
                break;
            default:
        }
    }

    public static void validloginNameEx(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setLoginName("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getLoginName())) {
                    Assert.isAccountEx(mbrAccount.getLoginName(), "会员账号,长度为6~16位!");
                    Assert.isCharacter(mbrAccount.getLoginName(), "用户名以字母开头6~16位数字、字母组合组成!");
                }
                break;
            case SysValueConst.require:
                Assert.isBlank(mbrAccount.getLoginName(), "用户名输入不能为空，请重新输入!");
                Assert.isAccountEx(mbrAccount.getLoginName(), "会员账号,长度为6~16位!");
                Assert.isCharacter(mbrAccount.getLoginName(), "用户名以字母开头6~16位数字、字母组合组成!");
                break;
            default:
        }
    }

    public static void validPwd(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setLoginPwd("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getLoginPwd())) {
                    Assert.isLenght(mbrAccount.getLoginPwd(), "密码 | 由6~18位数字与字母组合组成!", 6, 18);
                    Assert.accEqualPwd(mbrAccount.getLoginName(), mbrAccount.getLoginPwd(), "会员密码不能与账号相同!");
                }

                break;
            case SysValueConst.require:
                Assert.isBlank(mbrAccount.getLoginPwd(), "密码输入不能为空，请重新输入!");
                Assert.isLenght(mbrAccount.getLoginPwd(), "密码 | 由6~18位数字与字母组合组成!", 6, 18);
                Assert.isCharacters(mbrAccount.getLoginPwd(), "密码格式输入不正确，请重新输入!");
                Assert.accEqualPwd(mbrAccount.getLoginName(), mbrAccount.getLoginPwd(), "会员密码不能与账号相同!");
                Assert.isContainChinaChar(mbrAccount.getLoginPwd(), "不支持中文密码");  // 由control移到这里校验
                break;
            default:
        }
    }

    public static void validCaptchar(MbrAccount mbrAccount, String validType, String kaptcha, int captchaType) {
        switch (validType) {
            case SysValueConst.none:
            case SysValueConst.visible:
                mbrAccount.setCaptchareg("");
                mbrAccount.setCaptchaVerification("");
                break;
            case SysValueConst.require:
                if(captchaType == Constants.EVNumber.two){ // 行为验证码
                    CaptchaVO captchaVO = new CaptchaVO();
                    captchaVO.setCaptchaVerification(kaptcha);
                    ResponseModel response = captchaService.verification(captchaVO);
                    if(response.isSuccess() == false){
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
                    if (StringUtils.isEmpty(mbrAccount.getCaptchareg())){
                        throw new R200Exception("网络异常验证码为空，请刷新页面重试");
                    }
                    if (!mbrAccount.getCaptchareg().equalsIgnoreCase(kaptcha)) {
                        throw new R200Exception("图形验证码错误，请重新输入!");
                    }
                }

                break;
            default:
        }
    }

    private static void phoneValidCaptchar(MbrAccount mbrAccount, String validType, String kaptchaMb) {
        switch (validType) {
            case SysValueConst.none:
            case SysValueConst.visible:
                mbrAccount.setCaptchareg("");
                break;
            case SysValueConst.require:
                if (!mbrAccount.getPhoneCaptchareg().equalsIgnoreCase(kaptchaMb)) {
                    throw new R200Exception("短信验证码不正确!");
                }
                break;
            default:
        }
    }

    public static void validUserRealName(String realName, String validType) {
        switch (validType) {
            case SysValueConst.none:
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(realName)) {
                    Assert.isLenght(realName, "会员真实姓名不能为空,最大长度为20位!", 2, 20);
                    Assert.userRealNameCharacter(realName, "会员真实姓名为2~20位数字、字母、汉字或组合");
                }
                break;
            case SysValueConst.require:
                Assert.isLenght(realName, "会员真实姓名不能为空,最大长度为20位!", 2, 20);
                Assert.userRealNameCharacter(realName, "会员真实姓名为2~20位数字、字母、汉字或组合");
                break;
            default:
        }
    }

    public static void validRealName(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setRealName("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getRealName())) {
                    Assert.isLenght(mbrAccount.getRealName(), "会员真实姓名长度为2-30位!", 2, 30);
                    Assert.realNameCharacter(mbrAccount.getRealName(), "会员真实姓名只能为汉字(及·)或者英文(及空格符)");
                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getRealName(), "会员真实姓名不能为空,长度为2-30位!", 2, 30);
                Assert.realNameCharacter(mbrAccount.getRealName(), "会员真实姓名只能为汉字(及·)或者英文(及空格符)");
                break;
            default:
        }
    }

    public static void validPhone(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setMobile("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getMobile())) {
                    Assert.isPhoneAll(mbrAccount.getMobile(),mbrAccount.getMobileAreaCode());
                }
                break;
            case SysValueConst.require:
                Assert.isPhoneAll(mbrAccount.getMobile(),mbrAccount.getMobileAreaCode());
                break;
            default:
        }
    }

    public static void validEmail(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setEmail("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getEmail())) {
                    Assert.isLenght(mbrAccount.getEmail(), "邮箱最长为30位,可选!", 0, 30);
                    Assert.checkEmail(mbrAccount.getEmail(), "请输入正确的邮箱格式!");

                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getEmail(), "邮箱最长为30位,可选!", 4, 30);
                Assert.checkEmail(mbrAccount.getEmail(), "请输入正确的邮箱格式!");
                break;
            default:
        }
    }

    public static void validQQ(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setQq("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getQq())) {
                    Assert.isLenght(mbrAccount.getQq(), "qq最长为 15位!", 0, 15);
                    Assert.isQq(mbrAccount.getQq(), "qq只能为数字!");
                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getQq(), "qq最长为 15位!", 4, 15);
                Assert.isQq(mbrAccount.getQq(), "qq只能为数字!");
                break;
            default:
        }
    }

    public static void validWeChat(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setWeChat("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getWeChat())) {
                    Assert.isLenght(mbrAccount.getWeChat(), "微信最长为 20位!", 0, 20);
                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getWeChat(), "微信最长为 20位,并且不能为空!", 1, 20);
                break;
            default:
        }
    }

    private static void validAdress(MbrAccount mbrAccount, String validType) {
        switch (validType) {
            case SysValueConst.none:
                mbrAccount.setAddress("");
                break;
            case SysValueConst.visible:
                if (!StringUtils.isEmpty(mbrAccount.getAddress())) {
                    Assert.isLenght(mbrAccount.getAddress(), "地址最长为50位!", 0, 50);
                }
                break;
            case SysValueConst.require:
                Assert.isLenght(mbrAccount.getAddress(), "地址最长为 50位,并且不能为空!", 1, 50);
                break;
            default:
        }
    }


    public static void loginVerify(LoginUserDto model, Integer ltdNo, String kaptcha) {
        if (nonNull(model.getLoginName())){
            Assert.isBlank(model.getLoginName(), "用户名不能为空");
            Assert.isLenght(model.getPassword(), "会员密码,长度为6~18位!", 6, 18);
//            //登陆连续三次出错,需要验证码
//            if (ltdNo > 2) {
//                Assert.isBlank(model.getCaptcha(), "图形验证码不能为空");
//                if (!model.getCaptcha().equalsIgnoreCase(kaptcha)) {
//                    Assert.message("图形验证码不正确");
//                }
//            }
        }else if(nonNull(model.getMobile())){
            Assert.isBlank(model.getMobile(), "手机号不能为空");
            Assert.isLenght(model.getPassword(), "会员密码,长度为6~18位!", 6, 18);
        }
    }

    public static void registerVerify(UserDto entity, MbrAccount mbrAccount, List<SysSetting> list, String kaptcha, String kaptchaMb, String registerMethod,int captchaType,int ipReg,int devicReg) {
        entity.setLoginName(entity.getLoginName().toLowerCase());
        BeanUtils.copyProperties(entity,mbrAccount);
        if("0".equals(registerMethod)){ // 普通注册校验
            ValidRegUtils.validateReg(mbrAccount, list, kaptcha, kaptchaMb,captchaType,ipReg,devicReg);
        }else if("1".equals(registerMethod)){ // SDY废弃：快捷方式注册校验
            ValidRegUtils.validateRegEx(mbrAccount);
        }else { // 默认：普通注册校验
            ValidRegUtils.validateReg(mbrAccount, list, kaptcha, kaptchaMb,captchaType,ipReg,devicReg);
        }
    }

    public static void checkFpValid(HttpServletRequest request, JwtUtils jwtUtils) {
        // 获取用户凭证
        String token = request.getHeader(jwtUtils.getHeader());
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(jwtUtils.getHeader());
        }
        // 凭证为空
        if (StringUtils.isBlank(token)) {
            throw new R200Exception("token不能为空", HttpStatus.UNAUTHORIZED.value());
        }
        Claims claims = jwtUtils.getClaimByfindPwdToken(token);
        if (claims == null || jwtUtils.isTokenExpired(claims.getExpiration())) {
            throw new R200Exception("已失效,请重新申请找回密码!", HttpStatus.UNAUTHORIZED.value());
        }
        String loginName = claims.getSubject();
        // 设置userId到request里，后续根据userId，获取用户信息
        request.setAttribute(ApiConstants.USER_NAME, loginName);
    }
    
    public static void checkAgyFpValid(HttpServletRequest request, JwtUtils jwtUtils) {
    	// 获取用户凭证
    	String token = request.getHeader(jwtUtils.getHeader());
    	if (StringUtils.isBlank(token)) {
    		token = request.getParameter(jwtUtils.getHeader());
    	}
    	// 凭证为空
    	if (StringUtils.isBlank(token)) {
    		throw new R200Exception("token不能为空", HttpStatus.UNAUTHORIZED.value());
    	}
    	Claims claims = jwtUtils.getClaimByfindPwdToken(token);
    	if (claims == null || jwtUtils.isTokenExpired(claims.getExpiration())) {
    		throw new R200Exception("已失效,请重新申请找回密码!", HttpStatus.UNAUTHORIZED.value());
    	}
    	// 设置代理到request里，后续根据userId，获取用户信息
    	request.setAttribute(Constants.AGENT_ID, claims.getSubject());
    }
/*private static String getFieldValueByName(String fieldName, Object o) {  
    try {    
        String firstLetter = fieldName.substring(0, 1).toUpperCase();    
        String getter = "get" + firstLetter + fieldName.substring(1);    
        Method method = o.getClass().getMethod(getter, new Class[] {});    
        Object value = method.invoke(o, new Object[] {});    
        return String.valueOf(value);    
    } catch (Exception e) {    
        //log.error(e.getMessage(),e);    
        return null;    
    }    
}*/
/*	private static String setFieldValueByName(String fieldName, Object o) {
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			String setter = "set" + firstLetter + fieldName.substring(1);
			Method method = o.getClass().getMethod(setter, new Class[] {});
			Object value = method.invoke(o, new Object[] {});
			return String.valueOf(value);
		} catch (Exception e) {
			// log.error(e.getMessage(),e);
			return null;
		}
	}*/
	
/*	public static Object getProperty(Object beanObj, String property)
			throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// 此处应该判断beanObj,property不为null
		PropertyDescriptor pd = new PropertyDescriptor(property, beanObj.getClass(),
				"set" +captureName(property),
				"get" + captureName(property));
		Method getMethod = pd.getReadMethod();
		return getMethod.invoke(beanObj);
	}

	 该方法用于传入某实例对象以及对象方法名、修改值，通过放射调用该对象的某个set方法设置修改值 
	public static Object setProperty(Object beanObj, String property, Object value)
			throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// 此处应该判断beanObj,property不为null
		PropertyDescriptor pd = new PropertyDescriptor(property, beanObj.getClass());
		Method setMethod = pd.getWriteMethod();
		if (setMethod == null) {

		}
		return setMethod.invoke(beanObj, value);
	}
	 public static String captureName(String name) {
	        name = name.substring(0, 1).toUpperCase() + name.substring(1);
	       return  name;
	      
	    }*/

    public static void validIp(String validType,int ipReg) {
        Integer tag = Integer.valueOf(validType);
        if (tag>0){
            if (ipReg>=tag){
                Assert.ipAndDeviceRegAssert("此IP注册数量已达最大限制");
            }
        }

    }
    public static void validDevice(String validType,int deviceReg) {
        Integer tag = Integer.valueOf(validType);
        if (tag>0){
            if (deviceReg>=tag){
                Assert.ipAndDeviceRegAssert("此设备注册数量已达最大限制");
            }
        }
    }
}
