package com.wsdy.saasops.api.modules.user.controller;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.pay.dto.BankResponseDto;
import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.service.PayInfoService;
import com.wsdy.saasops.api.modules.user.dto.ActApplyDto;
import com.wsdy.saasops.api.modules.user.dto.BetDataDto;
import com.wsdy.saasops.api.modules.user.dto.LoginUserDto;
import com.wsdy.saasops.api.modules.user.dto.LoginVerifyDto;
import com.wsdy.saasops.api.modules.user.dto.MbrWdApplyDto;
import com.wsdy.saasops.api.modules.user.dto.MsgIds;
import com.wsdy.saasops.api.modules.user.dto.PwdDto;
import com.wsdy.saasops.api.modules.user.dto.RealNameDto;
import com.wsdy.saasops.api.modules.user.dto.TransferRequestDto;
import com.wsdy.saasops.api.modules.user.dto.UserDto;
import com.wsdy.saasops.api.modules.user.dto.VfyMailOrMobDto;
import com.wsdy.saasops.api.modules.user.dto.WithdrawalLimitDto;
import com.wsdy.saasops.api.modules.user.service.ApiPromotionService;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.JwtUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.constants.OprConstants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BaiduAipUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.IpUtils;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.SPTVUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.common.validator.ValidRegUtils;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.analysis.dto.WinLostReportModelDto;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.analysis.service.GameWinLoseService;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.base.service.BaseBankService;
import com.wsdy.saasops.modules.fund.dao.FundMerchantPayMapper;
import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.fund.service.FundDepositService;
import com.wsdy.saasops.modules.fund.service.FundWithdrawService;
import com.wsdy.saasops.modules.log.entity.LogMbrRegister;
import com.wsdy.saasops.modules.member.dao.MbrAccountOtherMapper;
import com.wsdy.saasops.modules.member.dto.WaterDepotDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAccountDevice;
import com.wsdy.saasops.modules.member.entity.MbrAccountOther;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import com.wsdy.saasops.modules.member.entity.MbrCryptoCurrencies;
import com.wsdy.saasops.modules.member.entity.MbrDepositCond;
import com.wsdy.saasops.modules.member.entity.MbrDepositCount;
import com.wsdy.saasops.modules.member.entity.MbrMessageInfo;
import com.wsdy.saasops.modules.member.entity.MbrUseDevice;
import com.wsdy.saasops.modules.member.entity.MbrWithdrawalCond;
import com.wsdy.saasops.modules.member.service.AccountWaterSettlementService;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.MbrAccountDeviceService;
import com.wsdy.saasops.modules.member.service.MbrAccountMobileService;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrActivityLevelCastService;
import com.wsdy.saasops.modules.member.service.MbrBankcardService;
import com.wsdy.saasops.modules.member.service.MbrCryptoCurrenciesService;
import com.wsdy.saasops.modules.member.service.MbrDepositCondService;
import com.wsdy.saasops.modules.member.service.MbrMessageService;
import com.wsdy.saasops.modules.member.service.MbrWithdrawalCondService;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.AdvBanner;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.OprHelpCategory;
import com.wsdy.saasops.modules.operate.entity.OprRecMbr;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.operate.service.OprAdvService;
import com.wsdy.saasops.modules.operate.service.OprMixActivityService;
import com.wsdy.saasops.modules.operate.service.OprNoticeService;
import com.wsdy.saasops.modules.operate.service.OprRecMbrService;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import com.wsdy.saasops.modules.system.pay.service.SetSmallAmountLineService;
import com.wsdy.saasops.modules.system.systemsetting.dto.WithdrawLimitTimeDto;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/user")
@Api(value = "api", tags = "前端会员接口")
public class ApiUserController {

    @Autowired
    MbrAccountService mbrAccountService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private MbrBankcardService mbrBankcardService;
    @Autowired
    private FundWithdrawService fundWithdrawService;
    @Autowired
    private OprRecMbrService oprRecMbrService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private OprActActivityService oprActActivityService;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private MbrWithdrawalCondService withdrawalCond;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private OprActActivityCastService oprActActivityCastService;
    @Autowired
    private BaseBankService baseBankService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OprNoticeService oprNoticeService;
    @Autowired
    private MbrMessageService messageService;
    @Autowired
    private GameWinLoseService winLoseService;
    @Autowired
    private MbrActivityLevelCastService activityLevelCastService;
    @Autowired
    private AccountWaterSettlementService waterSettlementService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private BaiduAipUtil baiduAipUtil;
    @Autowired
    private MbrAccountOtherMapper mbrAccountOtherMapper;
    @Autowired
    private MbrCryptoCurrenciesService mbrCryptoCurrenciesService;
    @Autowired
    private TGmDepotMapper gmDepotMapper;
    @Autowired
    private MbrAccountDeviceService mbrAccountDeviceService;
    @Autowired
    private FundMerchantPayMapper merchantPayMapper;
    @Autowired
    private CryptoCurrenciesService cryptoCurrenciesService;
    @Autowired
    private MbrAccountMobileService accountMobileService;
    @Autowired
    private SPTVUtils sPTVUtils;
    @Autowired
    private OprMixActivityService oprMixActivityService;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private OprAdvService oprAdvService;
    @Autowired
    private ApiPromotionService apiPromotionService;
    @Autowired
    private PayInfoService offlinePayService;
    @Autowired
    private FundDepositService fundDepositService;
	@Autowired
	private SetSmallAmountLineService setSmallAmountLineService;


    @PostMapping("/generateRegInfo")
    @ApiOperation(value = "生成注册账密", notes = "生成注册账密")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R generateRegInfo(@RequestBody UserDto userDto) {
        // 验证码校验
        if (isNull(userDto.getMobileCaptchareg())) {
            throw new R200Exception("请输入验证码！");
        }
        Object kaptchaLog = redisService.getRedisValus(RedisConstants.REDIS_MOBILE_REGISTE_CODE + CommonUtil.getSiteCode() + userDto.getMobile());
        if (isNull(kaptchaLog)) {
            return R.error("验证码已经失效！");
        }
        String kaptchaMb = kaptchaLog.toString();

        if (StringUtil.isEmpty(userDto.getMobileCaptchareg())) {
            throw new R200Exception("请重新获取验证码！");
        }
        if (!userDto.getMobileCaptchareg().equalsIgnoreCase(kaptchaMb)) {
            throw new R200Exception("验证码不正确!");
        }
        // 生成账号密码
        Map<String, String> map = new HashMap<>();
        map.put("loginName", "VIP" + CommonUtil.genRandom(7, 7));
        map.put("loginPwd", CommonUtil.genRandom(6, 6));

        map = validGenerateLoginName(map);

        // 保存redis数据
        redisService.setRedisExpiredTime(RedisConstants.MEMBER_REGISTER_LOGINNAME_ONLI + "_" + map.get("loginName"),
                map.get("loginName"), 10, TimeUnit.MINUTES);

        // 设置账密有效时间
        String siteCode = CommonUtil.getSiteCode();
        String mobile = userDto.getMobile();
        String key = RedisConstants.MEMBER_REGISTER_GENERATE_INFO + "_" + siteCode + "_" + mobile;
        redisService.setRedisExpiredTime(key, key, 10, TimeUnit.MINUTES);
        log.info("generateRegInfo==" + jsonUtil.toJson(map));
        return R.ok().put(map);
    }

    public Map<String, String> validGenerateLoginName(Map<String, String> map) {
        // 账号唯一性的校验
        Object obj = redisService.getRedisValus(RedisConstants.MEMBER_REGISTER_LOGINNAME_ONLI + "_" + map.get("loginName"));
        if (Objects.nonNull(obj)) {   // 生成账户存在相同，则重新再生成一次
            map.put("loginName", "VIP" + CommonUtil.genRandom(7, 7));
            map.put("loginPwd", CommonUtil.genRandom(6, 6));
            validGenerateLoginName(map);
        }
        // 校验数据库是否存在
        int count = mbrAccountService.findAccountOrAgentByName(map.get("loginName"));
        if (count > 0) {
            map.put("loginName", "VIP" + CommonUtil.genRandom(7, 7));
            map.put("loginPwd", CommonUtil.genRandom(6, 6));
            validGenerateLoginName(map);
        }
        return map;
    }

    @PostMapping("register")
    @ApiOperation(value = "会员接口-注册", notes = "会员接口-注册")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @ApiImplicitParam(name = "dev", value = "登录来源  dev：(PC:0、H5:4(APP)、WAP：3 wap)", required = true, dataType = "String", paramType = "header")
    public R register(@RequestBody UserDto userDto, HttpServletRequest request) {
        // 注册方式
        String registerMethod = userDto.getRegisterMethod();
        if (StringUtils.isEmpty(registerMethod)) {
            log.info("register==registerMethod is null");
//            throw new R200Exception("请选择注册方式");
        }

        // SDY废弃： 手机验证码注册，需要判断是否超时10分钟
        if ("1".equals(registerMethod)) { // 快捷方式注册校验
            String siteCode = CommonUtil.getSiteCode();
            String mobile = userDto.getMobile();
            String key = RedisConstants.MEMBER_REGISTER_GENERATE_INFO + "_" + siteCode + "_" + mobile;
            Object obj = redisService.getRedisValus(key);
            if (Objects.isNull(obj)) {
                throw new R200Exception("确认时间超过10分钟，请重新注册！");
            }
        }
        Boolean isMobile = Boolean.FALSE;           // 是否为手机号码的标识
        Boolean isMobileCaptchareg = Boolean.FALSE; // 是否为手机验证码的标识

        // 手机号码是否重复校验
        if (nonNull(userDto.getMobile())) {
            isMobile = Boolean.TRUE;
            List<MbrAccount> mbrAccountList = mbrAccountService.getAccountInfoByMobile(userDto.getMobile(), null);
            if (mbrAccountList.size() > 0) {
                throw new R200Exception("手机号不支持重复注册,请更换手机号码！");
            }
            accountMobileService.checkAccountMobile(userDto.getMobile());
        }
        // 如有手机校验码校验，获取注册验证码
        String kaptchaMb = null;
        if (nonNull(userDto.getMobileCaptchareg())) {
            isMobileCaptchareg = Boolean.TRUE;
            kaptchaMb = apiUserService.getKaptchaMb(userDto);
        }
        // 如有校验码校验，获取验证码验证
        String kaptcha = null;
        int captchaType = 1;    // 兼容旧的图形验证码 1 图形验证  2 滑动验证
        if (!StringUtil.isEmpty(userDto.getCaptchareg())) {
            if (StringUtils.isNotEmpty(userDto.getCodeSign())) {
                kaptcha = redisService.getKeyAndDel(userDto.getCodeSign());
            }
        } else if (!StringUtil.isEmpty(userDto.getCaptchaVerification())) { // 行为验证码
            kaptcha = userDto.getCaptchaVerification();
            captchaType = 2;
        }

        // 进行数据校验(包括验证码)
        MbrAccount mbrAccount = new MbrAccount();
        String dev = request.getHeader("dev");
        mbrAccount.setLoginSource(HttpsRequestUtil.getHeaderOfDevEx(dev));
        mbrAccount.setPhoneCaptchareg(userDto.getMobileCaptchareg());
        if (StringUtil.isEmpty(userDto.getMobileAreaCode())) {
            userDto.setMobileAreaCode("86");    // 默认中国区号
        }
        mbrAccount.setMobileAreaCode(userDto.getMobileAreaCode());  // 手机国际区号
        if (Objects.isNull(CommonUtil.getIpAddress(request))) {
            throw new R200Exception("获取注册IP为空");
        }
        if (Objects.isNull(userDto.getRegisterDevice())) {
            throw new R200Exception("获取注册设备为空");
        }
        //获取注册ip已注册人数
        Integer ipReg = mbrAccountService.getIpAccNum(CommonUtil.getIpAddress(request));

        //获取注册设备已注册人数
        Integer deviceReg = mbrAccountService.getDeviceAccNum(userDto.getRegisterDevice());
        List<SysSetting> list = sysSettingService.getRegisterInfoList();
        //把图形验证码单独拿出来
        for (SysSetting seting : list) {
            if (seting.getSyskey().equals(SystemConstants.MEMBER_VERIFICATION_CODE)) {
                String key = RedisConstants.REDIS_MOBILE_LOGIN_CODE + CommonUtil.getSiteCode() + mbrAccount.getLoginName();
                Object kaptchaLog = redisService.getRedisValus(key);
                if (isNull(kaptchaLog)) {
                    ValidRegUtils.validCaptchar(mbrAccount, seting.getSysvalue(), kaptcha, captchaType);
                }
            }

        }
        ValidRegUtils.registerVerify(userDto, mbrAccount, list, kaptcha, kaptchaMb, registerMethod, captchaType, ipReg, deviceReg);

        SysSetting sysSetting =  sysSettingService.getSysSetting(SystemConstants.MEMBER_REAL_NAME_REPEAT);
        if(sysSetting!= null && "0".equals(sysSetting.getSysvalue()) && userDto.getRealName()!= null && userDto.getRealName().length() > 0){
                Integer num = mbrAccountService.getRealNameNum(userDto.getRealName());
                if(num > 0){
                    throw new R200Exception("该姓名已被注册，请联系客服解决!");
                }
        }

        SysSetting sysSetting2 =  sysSettingService.getSysSetting(SystemConstants.MEMBER_PROMOTION);
        if(sysSetting2!= null && "2".equals(sysSetting2.getSysvalue())){
            if(userDto.getCodeId()== null) {
                throw new R200Exception("邀请码不能为空!");
            }
            Integer num = mbrAccountService.getCodeNum(userDto.getCodeId());
            if(num == 0){
                throw new R200Exception("邀请码输入不正确，请核对后输入!");
            }
        }

        String keyLogin = RedisConstants.REDIS_MOBILE_LOGIN_CODE + CommonUtil.getSiteCode() + mbrAccount.getLoginName();
        redisService.setRedisExpiredTime(keyLogin, kaptcha, 180, TimeUnit.SECONDS);      // 登录注册是否需要图形验证码
        // 保存会员
        MbrAccount account = mbrAccountService.webSave(mbrAccount, userDto);

        // 发送注册成功信息
        applicationEventPublisher.publishEvent(new BizEvent(this,
                CommonUtil.getSiteCode(), account.getId(), BizEventType.MEMBER_REGISTER_SUCCESS));

        //注册成功后登陆
        Map<String, Object> map = getToken(account);

        // 同步校验SPTV创建用户
        try {
            sPTVUtils.createUser(CommonUtil.getSiteCode(), (String) map.get("token"), account.getLoginName(), true);
        } catch (Exception e) {
            // 但未登陆成功
            return R.ok().put("loginFlag", false);
        }

        // 处理登录数据
        account.setLoginIp(CommonUtil.getIpAddress(request));
        account.setRegisterUrl(IpUtils.getUrl(request));
        account.setLoginType(userDto.getLoginType());
        account.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        mbrAccountService.asyncLogoInfo(account, CommonUtil.getSiteCode(), Boolean.TRUE, isMobile, isMobileCaptchareg);
        apiUserService.updateLoginTokenCache(CommonUtil.getSiteCode(), account.getLoginName(), (String) map.get("token"));
        apiUserService.updateLoginTokenCacheListener(CommonUtil.getSiteCode(), account.getLoginName());

        return R.ok(map).put("userInfo", mbrAccountService.webUserInfo(account.getLoginName())).put("loginFlag", true);
    }

    @PostMapping("login")
    @ApiOperation(value = "会员接口-登陆", notes = "会员接口-登陆")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R login(@RequestBody LoginUserDto model, HttpServletRequest request) {
        // 日志
        log.info("login==loginname==" + model.getLoginName());
        log.info("login==mobile==" + model.getMobile());
        log.info("login==model==" + jsonUtil.toJson(model));

        String siteCode = CommonUtil.getSiteCode();
        String keyLogin = RedisConstants.REDIS_MOBILE_LOGIN_CODE + CommonUtil.getSiteCode() + model.getLoginName();
        Object kaptchaLogin = redisService.getRedisValus(keyLogin);
        // 校验图形验证码
        if (("goc".equals(siteCode)) && nonNull(model.getLoginName())&&isNull(kaptchaLogin)) {
            if (!StringUtil.isEmpty(model.getCaptchaVerification())) {   // 行为验证码
                CaptchaVO captchaVO = new CaptchaVO();
                captchaVO.setCaptchaVerification(model.getCaptchaVerification());
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
                redisService.setRedisExpiredTime(keyLogin, model.getCaptchaVerification(), 180, TimeUnit.SECONDS);      // 登录注册是否需要图形验证码
            } else {
                if (StringUtil.isEmpty(model.getCaptcha()) || StringUtil.isEmpty(model.getCodeSign())) {
                    log.info(model.getLoginName() + "图形验证码为空");
                    return R.error("验证码错误！");
                }
                String kaptcha = redisService.getKeyAndDel(model.getCodeSign());
                if (!model.getCaptcha().equalsIgnoreCase(kaptcha)) {
                    log.info(model.getLoginName() + "图形验证码错误");
                    return R.error("验证码错误！");
                }
            }
        }

        MbrAccount entityName = new MbrAccount();
        MbrAccount entityMb = new MbrAccount();
        LoginVerifyDto verifyDto = new LoginVerifyDto();
        String userLogin = null;
        String userLoginFail = null;
        String mobileLogin = null;
        String mobileLoginFail = null;
        // 手机登录还是会员名登录 会员名登录true, 手机登录false
        Boolean isMobileOrUserName = Boolean.FALSE;

        // 登录错误次数 计数器，初始化5后，错误-1，手机和会员名登录分开
        Integer num = 0;
        if (nonNull(model.getLoginName())) {
            Assert.isAccountEx(model.getLoginName(), "会员账号,长度为6~16位!"); // 登录校验16位，兼容老站会员
            model.setLoginName(model.getLoginName().toLowerCase());
            verifyDto = apiUserService.queryPassLtdNoCache(siteCode, model.getLoginName());
            entityName.setLoginName(model.getLoginName());
            entityName = mbrAccountService.queryObjectCond(entityName);
            if (isNull(entityName)) {
                throw new R200Exception("请确认账号或密码是否正确！");
            }

            // 旧站点导入数据密码为空的情况
            if (StringUtils.isEmpty(entityName.getLoginPwd())) {
                return R.error("请使用手机登录或联系客服重置密码！");
            }

            // 此处增加外围系统前端校验： 投注状态
            if (StringUtils.isNotEmpty(model.getAgentV2Sign()) && model.getAgentV2Sign().equals("1")) {
                MbrAccountOther mbrAccountOther = new MbrAccountOther();
                mbrAccountOther.setAccountId(entityName.getId());
                mbrAccountOther = mbrAccountOtherMapper.selectOne(mbrAccountOther);
                if (Objects.isNull(mbrAccountOther)
                        || Integer.valueOf(Constants.EVNumber.zero).equals(mbrAccountOther.getBettingStatus())) {
                    throw new RRException("账号投注状态为关闭！");
                }
            }
            // 会员名登录错误计数器 key
            userLogin = RedisConstants.REDIS_USER_LOGIN + model.getLoginName().toLowerCase() + "_" + siteCode;
            // 会员名登录错误提示次数计数器 key，初始化1，用于1和0时不同提示
            userLoginFail = RedisConstants.REDIS_USER_LOGIN + model.getLoginName().toLowerCase() + "_" + siteCode + "_" + "FAIL";
            if (redisService.booleanRedis(userLogin)) {
                redisService.setRedisExpiredTime(userLogin, 5, 900, TimeUnit.SECONDS);      // 会员名登录错误次数 计数器 初始化5
            }
            if (redisService.booleanRedis(userLoginFail)) {
                redisService.setRedisExpiredTime(userLoginFail, 1, 900, TimeUnit.SECONDS);  // 会员名登录错误提示次数 计数器 初始化1，提示一次-1
            }

            isMobileOrUserName = Boolean.TRUE;
            num = Integer.parseInt(redisService.getRedisValus(userLogin).toString());
        } else if (nonNull(model.getMobile())) {
            Assert.isPhoneAll(model.getMobile(), model.getMobileAreaCode());

            verifyDto = apiUserService.queryPassMptdNoCache(siteCode, model.getMobile());
            entityMb = mbrAccountService.getAccountInfoByMobileOne(model.getMobile(), Constants.EVNumber.one);
            if (isNull(entityMb)) {
//                throw new R200Exception("手机号码输入有误或手机号码未绑定！");
                throw new R200Exception("请确认账号或密码是否正确！！");
            }
            // 手机号登录错误计数器 key
            mobileLogin = RedisConstants.REDIS_MOBILE_LOGIN + model.getMobile() + "_" + siteCode;
            // 手机号登录错误提示次数计数器 key，初始化1，用于1和0时不同提示
            mobileLoginFail = RedisConstants.REDIS_MOBILE_LOGIN + model.getMobile() + "_" + siteCode + "_" + "FAIL";
            if (redisService.booleanRedis(mobileLogin)) {
                redisService.setRedisExpiredTime(mobileLogin, 5, 900, TimeUnit.SECONDS);        // 手机号登录错误次数 计数器 初始化5
            }
            if (redisService.booleanRedis(mobileLoginFail)) {
                redisService.setRedisExpiredTime(mobileLoginFail, 1, 900, TimeUnit.SECONDS);    // 手机号登录错误提示次数 计数器 初始化1，提示一次-1
            }
            num = Integer.parseInt(redisService.getRedisValus(mobileLogin).toString());
        }

        // 错误次数小于5，正常登录逻辑
        if (num > 0) {
            String kaptcha = "";
//            if (StringUtils.isNotEmpty(model.getCodeSign())) {
//                kaptcha = redisService.getKeyAndDel(model.getCodeSign());
//            } else {
//                kaptcha = (verifyDto.getNo() > 2) ? apiUserService.getKaptcha(session, ApiConstants.KAPTCHA_LOGIN_SESSION_KEY) : "";
//            }
            if (nonNull(model.getLoginName())) {
                if (!entityName.getLoginPwd().equals(new Sha256Hash(model.getPassword(), entityName.getSalt()).toHex())) {
                    redisService.setRedisExpiredTime(userLogin, num - 1, 900, TimeUnit.SECONDS);
                    return R.error("请确认账号或密码是否正确！");
                }
                ValidRegUtils.loginVerify(model, verifyDto.getNo(), kaptcha);
                return getR(model, request, siteCode, entityName);
            } else if (entityMb.getIsVerifyMoblie() == Available.enable && nonNull(model.getMobile())) {
                if (nonNull(model.getPassword()) && !entityMb.getLoginPwd().equals(new Sha256Hash(model.getPassword(), entityMb.getSalt()).toHex())) {
                    redisService.setRedisExpiredTime(mobileLogin, num - 1, 900, TimeUnit.SECONDS);
//                    return R.error("错误的手机号或密码，请核对您的手机号及密码或使用会员账号+密码登录！");
                    return R.error("请确认账号或密码是否正确！");

                }

                // 用手机登录则密码或手机验证码必要验证一个
                if (StringUtils.isEmpty(model.getPassword()) && StringUtil.isEmpty(model.getMobileCaptcha())) {
                    return R.error("请确认账号或密码是否正确！");
                }

                if (nonNull(model.getMobileCaptcha())) {
                    //TODO 验证码登陆
                    String key = RedisConstants.REDIS_MOBILE_LOGIN_CODE + CommonUtil.getSiteCode() + model.getMobile();
                    Object kaptchaLog = redisService.getRedisValus(key);
                    if (isNull(kaptchaLog)) {
                        return R.error("验证码已经失效！");
                    }
                    if (!model.getMobileCaptcha().equals(kaptchaLog.toString())) {
                        redisService.setRedisExpiredTime(mobileLogin, num - 1, 900, TimeUnit.SECONDS);
                        return R.error("验证码不正确！");
                    }
                }
            }

//            ValidRegUtils.loginVerify(model, verifyDto.getNo(), kaptcha);
            return getR(model, request, siteCode, entityMb);
        } else {
            Integer num1 = 0;   // 会员名登录错误提示 1 第一次提示  0 第二次提示
            Integer num2 = 0;   // 手机号登录错误提示 1 第一次提示  0 第二次提示
            if (isMobileOrUserName.equals(Boolean.TRUE)) {
                num1 = Integer.parseInt(redisService.getRedisValus(userLoginFail).toString());
            } else {
                num2 = Integer.parseInt(redisService.getRedisValus(mobileLoginFail).toString());
            }
            if (num1 > 0) {
                int redisExpire = (int) redisService.getExpire(userLogin, TimeUnit.SECONDS);
                redisService.setRedisExpiredTime(userLoginFail, num1 - 1, redisExpire, TimeUnit.SECONDS);
                throw new R200Exception("对不起，您的账号已被锁定，15分钟后，再来尝试吧！", 10086);
            }
            if (num2 > 0) {
                int redisExpire = (int) redisService.getExpire(mobileLogin, TimeUnit.SECONDS);
                redisService.setRedisExpiredTime(mobileLoginFail, num2 - 1, redisExpire, TimeUnit.SECONDS);
                throw new R200Exception("对不起，您的账号已被锁定，15分钟后，再来尝试吧！", 10086);
            }
            throw new R200Exception("您的账号已被锁定，请稍后再次尝试！", 10086);
        }
    }

    @PostMapping("loginByPhoneNumber")
    @ApiOperation(value = "会员接口-手机号登陆", notes = "会员接口-手机号登陆")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R loginByPhoneNumber(@RequestBody LoginUserDto model, HttpServletRequest request) {
        // 日志
        log.info("login==mobile==" + model.getMobile());
        log.info("login==model==" + jsonUtil.toJson(model));

        String siteCode = CommonUtil.getSiteCode();

        MbrAccount entityMb = new MbrAccount();
        String userLogin = null;
        String userLoginFail = null;
        String mobileLogin = null;
        String mobileLoginFail = null;
        // 手机登录还是会员名登录 会员名登录true, 手机登录false
        Boolean isMobileOrUserName = Boolean.FALSE;

        // 登录错误次数 计数器，初始化5后，错误-1，手机和会员名登录分开
        Integer num = 0;
        if (nonNull(model.getMobile())) {
            Assert.isPhoneAll(model.getMobile(), model.getMobileAreaCode());

            // 手机号登录错误计数器 key
            mobileLogin = RedisConstants.REDIS_MOBILE_LOGIN + model.getMobile() + "_" + siteCode;
            // 手机号登录错误提示次数计数器 key，初始化1，用于1和0时不同提示
            mobileLoginFail = RedisConstants.REDIS_MOBILE_LOGIN + model.getMobile() + "_" + siteCode + "_" + "FAIL";
            if (redisService.booleanRedis(mobileLogin)) {
                redisService.setRedisExpiredTime(mobileLogin, 5, 900, TimeUnit.SECONDS);        // 手机号登录错误次数 计数器 初始化5
            }
            if (redisService.booleanRedis(mobileLoginFail)) {
                redisService.setRedisExpiredTime(mobileLoginFail, 1, 900, TimeUnit.SECONDS);    // 手机号登录错误提示次数 计数器 初始化1，提示一次-1
            }
            num = Integer.parseInt(redisService.getRedisValus(mobileLogin).toString());
        } else {
            throw new R200Exception("请输入正确的手机号码进行登录！！");
        }

        // 错误次数小于5，正常登录逻辑
        if (num > 0) {
            String kaptchaMb = "";

            // 手机验证码必要验证
            if (StringUtil.isEmpty(model.getMobileCaptcha())) {
                return R.error("请确认验证码是否正确！");
            }

            if (nonNull(model.getMobileCaptcha())) {
                //TODO 验证码登陆
                String key = RedisConstants.REDIS_MOBILE_LOGIN_CODE + CommonUtil.getSiteCode() + model.getMobile();
                Object kaptchaLog = redisService.getRedisValus(key);
                if (isNull(kaptchaLog)) {
                    return R.error("验证码已经失效！");
                }
                if (!model.getMobileCaptcha().equals(kaptchaLog.toString())) {
                    redisService.setRedisExpiredTime(mobileLogin, num - 1, 900, TimeUnit.SECONDS);
                    return R.error("验证码不正确！");
                }
                kaptchaMb = kaptchaLog.toString();
            }

            // 获取手机号码绑定的用户
            entityMb = mbrAccountService.getAccountInfoByMobileOne(model.getMobile(), Constants.EVNumber.one);
            if (isNull(entityMb)) {
                // 如果不存在该用户，则新增一名用户
                // 进行数据校验(包括验证码)
                MbrAccount mbrAccount = new MbrAccount();
                String dev = request.getHeader("dev");
                mbrAccount.setLoginSource(HttpsRequestUtil.getHeaderOfDevEx(dev));
                //mbrAccount.setPhoneCaptchareg(userDto.getMobileCaptchareg());
                // 手机国际区号
                mbrAccount.setMobileAreaCode(model.getMobileAreaCode());
                if (Objects.isNull(CommonUtil.getIpAddress(request))) {
                    throw new R200Exception("获取您的IP为空");
                }
                if (Objects.isNull(model.getRegisterDevice())) {
                    throw new R200Exception("获取注册设备为空");
                }

                // 保存会员
                UserDto userDto = new UserDto();
                // 生成用户名与密码 用户名sitecode+50000开始递增,密码UUID取15位
                // 由于用户名要求最长10位，当ID超过7位数时，使用7位随机字符串
                // Integer maxId = mbrAccountService.getAccountMaxId();
                // 使用domaincode逻辑生成7位随机数
                String extendLoginName = apiPromotionService.getExtendLoginNameCode(siteCode);
                userDto.setLoginName(extendLoginName);
                userDto.setLoginPwd(UUID.randomUUID().toString().substring(0, 15));
                userDto.setMobile(model.getMobile());
                userDto.setMainDomain(model.getMainDomain());
                userDto.setRegisterDevice(model.getRegisterDevice());
                userDto.setCaptchareg(model.getMobileCaptcha());

                //获取注册ip已注册人数
                Integer ipReg = mbrAccountService.getIpAccNum(CommonUtil.getIpAddress(request));
                //获取注册设备已注册人数
                Integer deviceReg = mbrAccountService.getDeviceAccNum(model.getRegisterDevice());
                List<SysSetting> list = sysSettingService.getRegisterInfoList();
                //手机登录时，自动注册账号，同样需要注册效验
                //移除掉手机号码登录自动注册时没有的验证项目
                SysSetting removeVerifCode = list.stream().filter(rvc -> rvc.getSyskey().equals(SystemConstants.MEMBER_VERIFICATION_CODE)).findFirst().get();
                list.remove(removeVerifCode);
                // 保证验证时需要的phoneCaptchareg验证通过
                mbrAccount.setPhoneCaptchareg(model.getMobileCaptcha());
                ValidRegUtils.registerVerify(userDto, mbrAccount, list, "", kaptchaMb, Constants.EVNumber.zero + "", 0, ipReg, deviceReg);

                BeanUtils.copyProperties(userDto, mbrAccount);
                MbrAccount account = mbrAccountService.webSave(mbrAccount, userDto);

                entityMb = account;

                // 发送注册成功信息
                applicationEventPublisher.publishEvent(new BizEvent(this,
                        CommonUtil.getSiteCode(), account.getId(), BizEventType.MEMBER_REGISTER_SUCCESS));
            }

            // ValidRegUtils.loginVerify(model, verifyDto.getNo(), kaptcha);
            return getR(model, request, siteCode, entityMb);
        } else {
            Integer num1 = 0;   // 会员名登录错误提示 1 第一次提示  0 第二次提示
            Integer num2 = 0;   // 手机号登录错误提示 1 第一次提示  0 第二次提示
            if (isMobileOrUserName.equals(Boolean.TRUE)) {
                num1 = Integer.parseInt(redisService.getRedisValus(userLoginFail).toString());
            } else {
                num2 = Integer.parseInt(redisService.getRedisValus(mobileLoginFail).toString());
            }
            if (num1 > 0) {
                int redisExpire = (int) redisService.getExpire(userLogin, TimeUnit.SECONDS);
                redisService.setRedisExpiredTime(userLoginFail, num1 - 1, redisExpire, TimeUnit.SECONDS);
                throw new R200Exception("对不起，您的账号已被锁定，15分钟后，再来尝试吧！", 10086);
            }
            if (num2 > 0) {
                int redisExpire = (int) redisService.getExpire(mobileLogin, TimeUnit.SECONDS);
                redisService.setRedisExpiredTime(mobileLoginFail, num2 - 1, redisExpire, TimeUnit.SECONDS);
                throw new R200Exception("对不起，您的账号已被锁定，15分钟后，再来尝试吧！", 10086);
            }
            throw new R200Exception("您的账号已被锁定，请稍后再次尝试！", 10086);
        }
    }

    /**
     * 登录
     *
     * @param model
     * @param request
     * @param siteCode
     * @param entityName
     * @return
     */
    public R getR(@RequestBody LoginUserDto model, HttpServletRequest request, String siteCode, MbrAccount entityName) {
        if (entityName.getIsLock().equals(Available.enable)) {
            return R.error("账号已被锁定,请联系在线客服!");
        } else if (entityName.getAvailable().equals(Available.disable)) {
            return R.error("账号已被禁用,请联系在线客服!");
        } else {
            // 生成token
            Map<String, Object> map = getToken(entityName);
            // 同步 校验SPTV创建用户
            sPTVUtils.createUser(siteCode, (String) map.get("token"), entityName.getLoginName(), true);
            // 保存mbr
            String dev = request.getHeader("dev");
            Byte loginSource = HttpsRequestUtil.getHeaderOfDev(dev);
            entityName.setLoginSource(loginSource);
            entityName.setLoginIp(CommonUtil.getIpAddress(request));
            entityName.setLoginType(model.getLoginType());
            entityName.setRegisterUrl(IpUtils.getUrl(request));
            entityName.setLoginTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
            mbrAccountService.update(entityName);

//            map.put("score",model.getScore());
            apiUserService.rmPassLtdNoCache(siteCode, model.getPassword());
            apiUserService.updateLoginTokenCache(siteCode, entityName.getLoginName(), (String) map.get("token"));
            apiUserService.updateLoginTokenCacheListener(siteCode, entityName.getLoginName());
            //保存登录日志的同时，判断是否异地登录，是则发短信通知，暂未上线
            mbrAccountService.asyncLogoInfo(entityName, siteCode, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
            return R.ok(map).put("userInfo", mbrAccountService.webUserInfo(entityName.getLoginName()));
        }
    }

    @GetMapping("/chkUser")
    @ApiOperation(value = "会员接口-账号检测", notes = "根据会员账号检测账号是否存在，存在msg为真，不存在msg为假!")
    public R chkUser(@ApiParam("会员账号") @RequestParam("username") String username) {
        Assert.isBlank(username, "用户名不能为空");
        int count = mbrAccountService.findAccountOrAgentByName(username);
        return R.ok(count > 0 ? Boolean.TRUE : Boolean.FALSE);
    }

    @GetMapping("/chkMobile")
    @ApiOperation(value = "会员接口-手机号检测", notes = "根据会员账号检测手机号是否存在，存在msg为真，不存在msg为假!")
    public R chkMobile(@ApiParam("手机号") @RequestParam("mobile") String mobile) {
        Assert.isBlank(mobile, "手机号不能为空！");
        List<MbrAccount> accountList = mbrAccountService.getAccountInfoByMobile(mobile, null);
        return R.ok(accountList.size() > 0 ? Boolean.TRUE : Boolean.FALSE);
    }

    @Login
    @GetMapping("/chkUserOnline")
    @ApiOperation(value = "会员接口-账号检测是否在线", notes = "会员接口-账号检测是否在线!")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R chkUserOnline() {
        return R.ok(Boolean.TRUE);
    }

    @GetMapping("/getRegistSetting")
    @ApiOperation(value = "获取会员注册参数设置", notes = "获取会员注册参数设置")
    public R getRegistSetting() {
        return R.ok().put("regSetting", sysSettingService.getRegisterInfoMap());
    }

    @PostMapping("/loginOut")
    @ApiOperation(value = "会员接口-登出", notes = "根据当前TOKEN 登出此账号!")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R loginOut(HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        mbrAccountService.updateOffline(loginName);
        apiUserService.rmLoginTokenCache(cpSite.getSiteCode(), loginName);
        return R.ok(Boolean.TRUE);
    }

    @GetMapping("/getUserInfo")
    @ApiOperation(value = "查找会员信息", notes = "查找会员信息!")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R getUserInfo(HttpServletRequest request) {
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        LinkedHashMap<String, Object> userInfo = mbrAccountService.webUserInfo(loginName);
        return R.ok().put("userInfo", userInfo);
    }

    @GetMapping("/getLtdNo") // limited number
    @ApiOperation(value = "查找会员登陆是否需要显示验证码及锁定时倒计时", notes = "no>=5时 锁定 180秒(second),no>=3应该出现验证码")
    public R getLtdNo(@RequestParam("loginName") String loginName, HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        LoginVerifyDto verifyDto = apiUserService.queryPassLtdNoCache(cpSite.getSiteCode(), loginName);
        return R.ok().put("no", verifyDto.getNo()).put("second", DateUtil.subtractTime(verifyDto.getExpireTime()));
    }

    @PostMapping("/modRealName")
    @ApiOperation(value = "修改会员真实姓名", notes = "修改会员真实姓名")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R modRealName(@RequestBody RealNameDto realNameDto, HttpServletRequest request) {
        Assert.isBlank(realNameDto.getRealName(), "会员真实姓名不能为空!");
        Assert.isLenght(realNameDto.getRealName(), "会员真实姓名长度为2-30位!", 2, 30);
        Assert.realNameCharacter(realNameDto.getRealName(), "会员真实姓名只能为汉字(及·)或者英文(及空格符)");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);

        SysSetting sysSetting =  sysSettingService.getSysSetting(SystemConstants.MEMBER_REAL_NAME_REPEAT);
        if(sysSetting!= null && "0".equals(sysSetting.getSysvalue()) && realNameDto.getRealName()!= null && realNameDto.getRealName().length() > 0){
            Integer num = mbrAccountService.getRealNameNum(realNameDto.getRealName());
            if(num > 0){
                throw new R200Exception("该姓名已被注册，请联系客服解决!");
            }
        }



        MbrAccount mbrAccount = mbrAccountService.updateRealName(userId, realNameDto.getRealName(),
                cpSite.getSiteCode(), CommonUtil.getIpAddress(request));
        if (mbrAccount.getRealName().equals(realNameDto.getRealName())) {
            return R.ok();
        } else {
            return R.error("修改真实姓名失败!");
        }
    }

    // 屏蔽代码 转入SPTV同步
//    @PostMapping("/modNickName")
//    @ApiOperation(value = "修改会员昵称", notes = "修改会员昵称")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
//            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
//    @Login
//    public R modNickName(@RequestBody NickNameDto nickNameDto, HttpServletRequest request) {
//        Assert.isBlank(nickNameDto.getNickName(), "昵称不能为空!");
//        Assert.isLenght(nickNameDto.getNickName(), "昵称长度为1-10位!", 1, 10);
//        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
//        return mbrAccountService.modNickName(userId, nickNameDto);
//    }

    @PostMapping("/modPwd")
    @ApiOperation(value = "修改会员密码", notes = "修改会员密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R modPwd(@RequestBody PwdDto pwdDto, HttpServletRequest request) {
        Assert.isBlank(pwdDto.getLastPwd(), "旧密码不能为空!");
        Assert.isBlank(pwdDto.getPwd(), "密码不能为空!");
        Assert.isContainChinaChar(pwdDto.getPwd(), "升级账号安全，不支持中文密码！");
        Assert.isLenght(pwdDto.getPwd(), "会员密码,长度为6~18位!", 6, 18);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        mbrAccountService.updatePwd(userId, pwdDto);
        applicationEventPublisher.publishEvent(new BizEvent(this, CommonUtil.getSiteCode(), userId,
                BizEventType.UPDATE_MEMBER_INFO, pwdDto.getLastPwd(), pwdDto.getPwd()));
        return R.ok();
    }

    @PostMapping("/verifyPwd")
    @ApiOperation(value = "原密码校验", notes = "原密码校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R verifyPwd(@RequestBody PwdDto pwdDto, HttpServletRequest request) {
        Assert.isBlank(pwdDto.getLastPwd(), "旧密码不能为空!");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok(mbrAccountService.verifyPwd(userId, pwdDto));
    }

    @PostMapping("/modScPwd")
    @ApiOperation(value = "修改会员提款密码", notes = "修改会员提款密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R modScPwd(@RequestBody PwdDto pwdDto, HttpServletRequest request) {
        Assert.isBlank(pwdDto.getLastPwd(), "旧密码不能为空!");
        Assert.isBlank(pwdDto.getPwd(), "提款密码不能为空!");
        Assert.isLenght(pwdDto.getPwd(), "会员提款密码,长度为6~18位!", 6, 18);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(userId);
        if (StringUtils.isEmpty(mbrAccount.getSecurePwd())) {
            throw new R200Exception("请在提款页面设置提款密码");
        }
        mbrAccountService.updateScPwd(userId, pwdDto);
        return R.ok();
    }

    @PostMapping("/sendMailCode")
    @ApiOperation(value = "会员邮箱验证", notes = "会员邮箱验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R sendMailCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        Assert.checkEmail(vfyDto.getEmail(), "邮箱格式不正确!");
        Assert.isLenght(vfyDto.getEmail(), "邮箱长度不能大于30位", 5, 30);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        String code = apiUserService.sendVfyMailCode(vfyDto, cpSite.getSiteCode(), userId);
        if (!StringUtils.isEmpty(code)) {
            vfyDto.setCode(code);
            apiUserService.updateVfyMailOrMobCodeCache(cpSite.getSiteCode(), loginName, vfyDto);
            return R.ok();
        } else {
            return R.error("邮件发送失败,请联系在线客服!");
        }
    }

    @PostMapping("/vfyMailCode")
    @ApiOperation(value = "会员邮箱验证", notes = "会员邮箱验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R vfyMailCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        Assert.checkEmail(vfyDto.getEmail(), "邮箱格式不正确!");
        Assert.isLenght(vfyDto.getEmail(), "邮箱长度不能大于30位", 5, 30);
        Assert.isBlank(vfyDto.getCode(), "验证码不能为空!");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        VfyMailOrMobDto vfyDotCahce = apiUserService.queryVfyMailOrMobCodeCache(cpSite.getSiteCode(), loginName);
        if (!vfyDto.getEmail().equals(vfyDotCahce.getEmail())) {
            throw new R200Exception("验证邮箱与申请邮箱不匹配!");
        }
        if (!vfyDto.getCode().equals(vfyDotCahce.getCode())) {
            throw new R200Exception("验证码不正确!");
        }
        if (mbrAccountService.updateMail(userId, vfyDto.getEmail(), CommonUtil.getIpAddress(request)) > 0) {
            return R.ok();
        } else {
            return R.error();
        }
    }

    @PostMapping("/sendMobCode")
    @ApiOperation(value = "会员手机验证", notes = "会员手机验证(个人资料)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R sendMobCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
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

        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        List<MbrAccount> accountList = mbrAccountService.getAccountInfoByMobile(vfyDto.getMobile(), Constants.EVNumber.one);
        if (accountList.size() > 0) {
            throw new R200Exception("手机号码已绑定，请更换手机号码!");
        }
        String code = apiUserService.sendVfySmsCode(vfyDto, userId, language);
        if (!StringUtils.isEmpty(code)) {
            vfyDto.setCode(code);
            apiUserService.updateVfyMailOrMobCodeCache(cpSite.getSiteCode(), loginName, vfyDto);
            return R.ok();
        } else {
            return R.error();
        }
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

        // 控制已绑定的手机不能发验证码
        List<MbrAccount> mbrAccountList = mbrAccountService.getAccountInfoByMobile(vfyDto.getMobile(), null);
        if (mbrAccountList.size() > 0) {
            throw new R200Exception("手机号不支持重复注册,请使用其他手机号码！");
        }
        accountMobileService.checkAccountMobile(vfyDto.getMobile());
        String code = apiUserService.sendVfySmsRegCode(vfyDto.getMobile(), vfyDto.getMobileAreaCode(), language);
        if (!StringUtils.isEmpty(code)) {
            // 改为使用redis控制，避免iphone x session问题
            log.info("短信注册验证码" + code + "," + vfyDto.getMobile());
            String key = RedisConstants.REDIS_MOBILE_REGISTE_CODE + CommonUtil.getSiteCode() + vfyDto.getMobile();
            redisService.setRedisExpiredTime(key, code, 15, TimeUnit.MINUTES);

            return R.ok();
        }
        return R.error();
    }

    @PostMapping("/sendMobCodeLog")
    @ApiOperation(value = "会员手机验证(登陆)", notes = "会员手机验证")
    public R sendMobCodeLog(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
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

        String code = apiUserService.sendVfySmsOneCode(vfyDto.getMobile(), null, vfyDto.getMobileAreaCode(), Constants.EVNumber.two, language);
        if (!StringUtils.isEmpty(code)) {
            log.info("短信登陆验证码" + code + "," + vfyDto.getMobile());
            String key = RedisConstants.REDIS_MOBILE_LOGIN_CODE + CommonUtil.getSiteCode() + vfyDto.getMobile();
            redisService.setRedisExpiredTime(key, code, 10, TimeUnit.MINUTES);
            return R.ok();
        }
        return R.error();
    }

    @PostMapping("/sendMobCodeForLbpn")
    @ApiOperation(value = "会员手机验证(登陆)", notes = "会员手机验证")
    public R sendMobCodeForLoginByPhoneNumber(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
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

        String code = apiUserService.sendVfySmsOneCodForLbpn(vfyDto.getMobile(), null, vfyDto.getMobileAreaCode(), language);
        if (!StringUtils.isEmpty(code)) {
            log.info("短信登陆验证码" + code + "," + vfyDto.getMobile());
            String key = RedisConstants.REDIS_MOBILE_LOGIN_CODE + CommonUtil.getSiteCode() + vfyDto.getMobile();
            redisService.setRedisExpiredTime(key, code, 5, TimeUnit.MINUTES);
            return R.ok();
        }
        return R.error();
    }

    @PostMapping("/vfyMobCode")
    @ApiOperation(value = "会员手机验证", notes = "会员手机验证：个人资料")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R vfyMobCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);

        VfyMailOrMobDto vfyDotCahce = apiUserService.queryVfyMailOrMobCodeCache(cpSite.getSiteCode(), loginName);
        if (Objects.isNull(vfyDotCahce) || StringUtil.isEmpty(vfyDotCahce.getMobile())) {
            throw new R200Exception("请先获取手机验证码！");
        }

        if (!vfyDto.getMobile().equals(vfyDotCahce.getMobile())) {
            throw new R200Exception("验证手机号与申请手机号不匹配!");
        }
        if (!vfyDto.getCode().equals(vfyDotCahce.getCode())) {
            throw new R200Exception("验证码不正确!");
        }

        if (mbrAccountService.updateMobile(userId, vfyDto.getMobile(), CommonUtil.getIpAddress(request), loginName) > 0) {
            return R.ok();
        } else {
            throw new R200Exception("验证码不正确!");
        }
    }

    @PostMapping("/vfydevice")
    @ApiOperation(value = "验证是否常用设备", notes = "验证是否常用设备")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R vfydevice(@RequestBody LoginUserDto loginUserDto, HttpServletRequest request) {
        Assert.isNull(loginUserDto.getLoginDevice(), "设备校验,设备为空");
        MbrAccount account = new MbrAccount();
        //检查是否绑定手机,没绑定手机的不校验设备
        if (StringUtils.isNotEmpty(loginUserDto.getLoginName())) {
            loginUserDto.setLoginName(loginUserDto.getLoginName().toLowerCase());
            account = mbrAccountService.getAccountInfo(loginUserDto.getLoginName());
            if (!nonNull(account)) {
                throw new R200Exception("请确认账号或密码是否正确");
            }
            if (StringUtils.isEmpty(loginUserDto.getPassword()) || !account.getLoginPwd().equals(new Sha256Hash(loginUserDto.getPassword(), account.getSalt()).toHex())) {
                throw new R200Exception("请确认账号或密码是否正确");
            }
        }
        if (StringUtils.isNotEmpty(loginUserDto.getMobile())) { //假如是手机号登录
            account = mbrAccountService.getAccountInfoByMobileOne(loginUserDto.getMobile(), null);
            if (StringUtils.isEmpty(account.getMobile()) || !account.getMobile().equals(loginUserDto.getMobile())) {
                throw new R200Exception("请确认手机号是否正确");
            }
        }
        if (StringUtils.isEmpty(account.getMobile())) { //没绑定手机的不校验设备
            return R.ok("0");
        }
        //查看玩家是否已经有常用设备,假如没有也不验证
        int num = mbrAccountService.queryMbrDeviceNum(loginUserDto.getLoginName());
        if (num < 1) {
            return R.ok("0");
        }
        //检查当前设备是否是  有效期间的常用设备
        MbrUseDevice device = mbrAccountService.getDeviceByUuid(loginUserDto.getLoginName(), DateUtil.getLastMonthDD(), loginUserDto.getLoginDevice());
        if (!nonNull(device)) {
            return R.ok("1").put(account.getMobile());
        }
        return R.ok("0");
    }

    @PostMapping("/saveBankCard")
    @ApiOperation(value = "新增会员银行卡号", notes = "新增会员银行卡号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R saveBankCard(@RequestBody MbrBankcard mbrBankcard, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String ip = (String) request.getAttribute(ApiConstants.ip);
        //会员与银行卡关联表增加id字段进行关联，加该校验是因为北京前端无法同步修改
        if (mbrBankcard.getBankCardId() != null) {
            BaseBank baseBank = baseBankService.queryObject(mbrBankcard.getBankCardId());
            mbrBankcard.setBankName(baseBank.getBankName());
        } else {
            BaseBank baseBank = new BaseBank();
            baseBank.setBankName(mbrBankcard.getBankName());
            List<BaseBank> baseBanks = baseBankService.select(baseBank);
            if (baseBanks.size() == 0) {
                throw new R200Exception("不支持该银行,请更换银行卡号");
            }
            mbrBankcard.setBankCardId(baseBanks.get(0).getId());
        }
        Assert.isBlank(mbrBankcard.getBankName(), "开户银行不能为空!");
        Assert.isBlank(mbrBankcard.getCardNo(), "开户账号不能为空!");
        Assert.isNumeric(mbrBankcard.getCardNo(), "开户账号只能为数字!");
        Assert.isBankCardNo(mbrBankcard.getCardNo(), "长度只能为16~19位!", 16, 19);
        Assert.isBlank(mbrBankcard.getProvince() + mbrBankcard.getCity() + mbrBankcard.getAddress(), "开户支行不能为空!");
        Assert.isChina(mbrBankcard.getAddress(), "支行名称只允许填写中文!");
        mbrBankcard.setAccountId(userId);
        return mbrBankcardService.saveBankCard(mbrBankcard, Constants.EVNumber.one, null, ip);
    }

    @PostMapping("/saveAlipayAccount")
    @ApiOperation(value = "新增会员支付宝账号", notes = "新增会员支付宝账号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R saveAlipayAccount(@RequestBody MbrBankcard mbrBankcard, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String ip = (String) request.getAttribute(ApiConstants.ip);
        //会员与银行卡关联表增加id字段进行关联，加该校验是因为北京前端无法同步修改
        //支付宝无法开启wdenable，估绑定支付宝时，通过name和code获取支付宝id
        if (mbrBankcard.getBankCardId() != null) {
            BaseBank baseBank = baseBankService.queryObject(mbrBankcard.getBankCardId());
            mbrBankcard.setBankName(baseBank.getBankName());
        } else {
            BaseBank search = new BaseBank();
            search.setBankName("支付宝");
            search.setBankCode("ZFB");
            BaseBank baseBank = baseBankService.selectOne(search);
            if (isNull(baseBank)) {
                throw new R200Exception("不支持该钱包");
            }
            mbrBankcard.setBankCardId(baseBank.getId());
            mbrBankcard.setBankName(baseBank.getBankName());
        }
        Assert.isBlank(mbrBankcard.getBankName(), "开户银行不能为空!");
        Assert.isBlank(mbrBankcard.getCardNo(), "开户账号不能为空!");
        Assert.isBankCardNo(mbrBankcard.getCardNo(), "长度只能为10~32位!", 10, 32);
        mbrBankcard.setAccountId(userId);
        return mbrBankcardService.saveBankCard(mbrBankcard, Constants.EVNumber.one, null, ip);
    }

    @PostMapping("/saveOtherPayAccount")
    @ApiOperation(value = "新增会员其他钱包账号", notes = "新增会员其他钱包账号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R saveOtherPayAccount(@RequestBody MbrBankcard mbrBankcard, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String ip = (String) request.getAttribute(ApiConstants.ip);
        BaseBank search = new BaseBank();
        //search.setBankName("其他钱包");
        search.setBankCode(mbrBankcard.getAddress());
        BaseBank baseBank = baseBankService.selectOne(search);
        if (isNull(baseBank)) {
            throw new R200Exception("不支持该钱包");
        }
        mbrBankcard.setBankCardId(baseBank.getId());
        mbrBankcard.setBankName(baseBank.getBankName());
        mbrBankcard.setAddress("其他钱包");

        Assert.isBlank(mbrBankcard.getBankName(), "开户银行不能为空!");
        Assert.isBlank(mbrBankcard.getCardNo(), "钱包地址不能为空!");
        //Assert.isBankCardNo(mbrBankcard.getCardNo(), "长度只能为10~32位!", 10, 32);
        if (mbrBankcard.getCardNo().length() < 16 || mbrBankcard.getCardNo().length() > 64) {
            throw new R200Exception("钱包地址只能在16 ~ 64个字符之间");
        }

        mbrBankcard.setAccountId(userId);
        return mbrBankcardService.saveBankCard(mbrBankcard, Constants.EVNumber.one, null, ip);
    }

    @PostMapping("/getRedisKeyValueAndTTL")
    @ApiOperation(value = "根据key获取redis值以及TTL", notes = "根据key获取redis值以及TTL")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R getRedisKeyValueAndTTL(@RequestParam("keyName") String keyName, HttpServletRequest request) {
        /*redisService.setRedisExpiredTime(ApiConstants.REIDS_LOGIN_TOKEN_LISTENER_KEY + "_"
                + siteCode + "_" + loginName, loginName, 120, TimeUnit.SECONDS);*/
        Map<String, Object> getResult = new HashMap<>();
        Long leftSeconds = redisService.getExpire(keyName, TimeUnit.SECONDS);
        getResult.put("leftSeconds", leftSeconds);
        Object keyValue = redisService.getRedisValus(keyName);
        getResult.put("keyValue", keyValue);
        String tokenKey = "SaasopsV2:" + ApiConstants.REIDS_LOGIN_TOKEN_KEY + ":" + CommonUtil.getSiteCode() + "_" + request.getAttribute(ApiConstants.USER_NAME);
        Object loginToken = redisService.getRedisValus(tokenKey);
        getResult.put("loginToken", loginToken);
        getResult.put("tokenKey", tokenKey);
        Long loginTokenLeftSeconds = redisService.getExpire(tokenKey, TimeUnit.SECONDS);
        getResult.put("loginTokenLeftSeconds", loginTokenLeftSeconds);
        return new R().put(getResult);
    }

    @PostMapping("/saveCryptoCurrencies")
    @ApiOperation(value = "新增会员加密货币钱包", notes = "新增会员加密货币钱包")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R saveCryptoCurrencies(@RequestBody MbrCryptoCurrencies mbrCryptoCurrencies, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        //会员与t_bs_bank 关联表增加id字段进行关联
        if (mbrCryptoCurrencies.getBankCardId() != null) {
            BaseBank baseBank = baseBankService.queryObject(mbrCryptoCurrencies.getBankCardId());
            mbrCryptoCurrencies.setCurrencyCode(baseBank.getBankCode());
            mbrCryptoCurrencies.setCurrencyProtocol(baseBank.getCategory());
        } else {
            BaseBank baseBank = new BaseBank();
            baseBank.setBankCode(mbrCryptoCurrencies.getCurrencyCode());
            baseBank.setCategory(mbrCryptoCurrencies.getCurrencyProtocol());
            List<BaseBank> baseBanks = baseBankService.select(baseBank);
            mbrCryptoCurrencies.setBankCardId(baseBanks.get(0).getId());
        }
        Assert.isBlank(mbrCryptoCurrencies.getWalletName(), "钱包类型/名称不能为空!");
        Assert.isBlank(mbrCryptoCurrencies.getWalletAddress(), "钱包地址不能为空!");
        Assert.isBlank(mbrCryptoCurrencies.getCurrencyCode(), "currencyCode不能为空!");
        Assert.isBlank(mbrCryptoCurrencies.getCurrencyProtocol(), "currencyProtocol不能为空!");

        // ERC20 地址校验
        if (Constants.TYPE_ERC20.equals(mbrCryptoCurrencies.getCurrencyProtocol())) {
            Assert.isERC20Address(mbrCryptoCurrencies.getWalletAddress(), "钱包地址(ERC20协议)格式错误，请确认后再提交！");
        }
        // TRC20 地址校验
        if (Constants.TYPE_TRC20.equals(mbrCryptoCurrencies.getCurrencyProtocol())) {
            Assert.isTRC20Address(mbrCryptoCurrencies.getWalletAddress(), "钱包地址(TRC20协议)格式错误，请确认后再提交！");
        }

        mbrCryptoCurrencies.setAccountId(userId);
        return mbrCryptoCurrenciesService.saveCryptoCurrencies(mbrCryptoCurrencies, Constants.EVNumber.one);
    }

    @GetMapping("/unbindCardList")
    @ApiOperation(value = "会员解绑银行卡", notes = "会员解绑的银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "Stoken", value = "Stoken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R unbindCardList(@RequestParam("bankCardId") Integer bankCardId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return mbrBankcardService.unbindCondBankCard(userId, bankCardId);
    }


    @GetMapping("/unbindWalletList")
    @ApiOperation(value = "会员解绑钱包", notes = "会员解绑钱包")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "Stoken", value = "Stoken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R unbindWalletList(@RequestParam("walletId") Integer walletId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return mbrCryptoCurrenciesService.unbindWalletList(userId, walletId);
    }

    @PostMapping("/uservfyInfo")
    @ApiOperation(value = "查询会员密码手机等是否填写", notes = "查询会员密码手机等是否填写")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R uservfyInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("uservfyInfos", mbrAccountService.webUserVfyInfo(userId));
    }

    @PostMapping("/bankCardList")
    @ApiOperation(value = "会员查询银行卡列表", notes = "会员查询银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "Stoken", value = "Stoken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R userBankCards(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        R r = R.ok();
        r.put("bankCards", mbrBankcardService.listCondBankCard(userId));
        r.put("bankDifferentName", mbrBankcardService.bankDifferentName(userId));
        return r;
    }

    @PostMapping("/ailpayAccountList")
    @ApiOperation(value = "会员查询银行卡列表", notes = "会员查询银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "Stoken", value = "Stoken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R ailpayAccountList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("bankCards", mbrBankcardService.listCondAlipayAccount(userId));
    }

    @PostMapping("/otherPayAccountList")
    @ApiOperation(value = "会员查询银行卡列表", notes = "会员查询银行卡列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "Stoken", value = "Stoken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R otherPayAccountList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("otherPay", mbrBankcardService.listCondOtherPayAccount(userId));
    }

    @GetMapping("/cryptoCurrenciesList")
    @ApiOperation(value = "会员查询加密货币钱包列表", notes = "会员查询加密货币钱包列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "Stoken", value = "Stoken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R cryptoCurrenciesList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("cryptoCurrencies", mbrCryptoCurrenciesService.listCondCryptoCurrencies(userId));
    }

    @PostMapping("/withdrawal")
    @ApiOperation(value = "会员取款申请", notes = "会员取款申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R withdrawal(@RequestBody MbrWdApplyDto wdApply, HttpServletRequest request) {
//        Assert.isNull(wdApply.getMethodType(), "methodType不能为空!");    // 天翊
        Assert.isNumeric(wdApply.getDrawingAmount(), "申请取款金额只能为正数!");

        if (Objects.nonNull(wdApply.getMethodType()) && Constants.EVNumber.one == wdApply.getMethodType().intValue()) {  // 加密货币
            Assert.isNull(wdApply.getCryptoCurrenciesId(), "取款钱包不能为空!");
        } else {  // 银行卡
            Assert.isNull(wdApply.getBankCardId(), "取款银行不能为空!");
        }

        // 检查提款显示
        fundWithdrawService.checkWithdwarwLimitTime();

        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        log.info("withdrawal=={}==会员{}==开始取款", CommonUtil.getSiteCode(), loginName);

        AccWithdraw withDraw = new AccWithdraw();
        withDraw.setAccountId(userId);
        withDraw.setLoginName(loginName);
        withDraw.setCreateUser(loginName);
        withDraw.setIp(CommonUtil.getIpAddress(request));
        withDraw.setDrawingAmount(wdApply.getDrawingAmount());

        withDraw.setMethodType(wdApply.getMethodType());

        if (Objects.nonNull(wdApply.getMethodType()) && Constants.EVNumber.one == wdApply.getMethodType().intValue()) {  // 加密货币
            log.info("withdrawal=={}==会员{}==USDT取款", CommonUtil.getSiteCode(), loginName);
            Assert.isNull(wdApply.getCryptoCurrenciesId(), "cryptoCurrenciesId不为null!");
            withDraw.setCryptoCurrenciesId(wdApply.getCryptoCurrenciesId());
            // 此处使用后端查询的费率，不用前端的
            String rate = cryptoCurrenciesService.getExchangeRate("Withdrawal");
            withDraw.setExchangeRate(new BigDecimal(rate));    // 参考汇率
            log.info("withdrawal=={}==会员{}==USDT取款==rate=={}", CommonUtil.getSiteCode(), loginName, withDraw.getExchangeRate());
        } else if (Objects.nonNull(wdApply.getMethodType()) && Constants.EVNumber.two == wdApply.getMethodType().intValue()) {  // 支付宝
            log.info("withdrawal=={}==会员{}==支付宝取款", CommonUtil.getSiteCode(), loginName);
            Assert.isNull(wdApply.getBankCardId(), "bankcardId不为null!");
            withDraw.setMethodType(Constants.EVNumber.two);
            withDraw.setBankCardId(wdApply.getBankCardId());
        } else if (Objects.nonNull(wdApply.getMethodType()) && Constants.EVNumber.three == wdApply.getMethodType().intValue()) { // 其他钱包
            log.info("withdrawal=={}==会员{}==其他钱包取款", CommonUtil.getSiteCode(), loginName);
            Assert.isNull(wdApply.getBankCardId(), "bankcardId不为null!");
            withDraw.setMethodType(Constants.EVNumber.three);
            withDraw.setBankCardId(wdApply.getBankCardId());
        } else {  // 银行卡
            log.info("withdrawal=={}==会员{}==银行卡取款", CommonUtil.getSiteCode(), loginName);
            Assert.isNull(wdApply.getBankCardId(), "bankcardId不为null!");
            withDraw.setBankCardId(wdApply.getBankCardId());
            withDraw.setMethodType(Constants.EVNumber.zero);
        }

        //获取取款的客户端来源
        String dev = request.getHeader("dev");
        Byte withdrawSource = HttpsRequestUtil.getHeaderOfDev(dev);
        withDraw.setWithdrawSource(withdrawSource == null ? LogMbrRegister.RegIpValue.pcClient : withdrawSource);
        // 获取siteCode
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        // 提款申请
        fundWithdrawService.saveApply(withDraw, wdApply.getPwd(), cpSite.getSiteCode(), request);
        return R.ok("申请取款成功!");
    }

    @GetMapping("/getExchangeRate")
    @ApiOperation(value = "获取取款参考汇率", notes = "获取取款参考汇率")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getExchangeRate(@ModelAttribute PayParams params, HttpServletRequest request) {
        return R.ok(cryptoCurrenciesService.getExchangeRate("Withdrawal"));
    }

    @GetMapping("/wdApplyList")
    @ApiOperation(value = "查询取款记录", notes = "查询取款记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R wdApplyList(@RequestParam("startTime") String startTime, @RequestParam("entTime") String entTime,
                         @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,
                         HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        PageUtils page = fundWithdrawService.queryAccListPage(startTime, entTime, userId, pageNo, pageSize);
        Integer time = sysSettingService.queryCuiDanSet();
        return R.ok().put("page", page).put("totalActuals",
                fundWithdrawService.totalActualArrival(startTime, entTime, userId)).put("time", time);
    }

    @GetMapping("/msgList")
    @ApiOperation(value = "会员消息", notes = "会员消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R msgList(@RequestParam("startTime") String startTime, @RequestParam("entTime") String entTime,
                     @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,
                     HttpServletRequest request) {
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        OprRecMbr oprRecMbr = new OprRecMbr();
        oprRecMbr.setMbrName(loginName);
        oprRecMbr.setSendTimeFrom(startTime);
        oprRecMbr.setSendTimeTo(entTime);
        PageUtils page = oprRecMbrService.queryListPage(oprRecMbr, pageNo, pageSize, "", null);
        return R.ok().put("page", page);
    }

    @PostMapping("/readMsg")
    @ApiOperation(value = "会员消息", notes = "会员消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R readMsg(@RequestBody MsgIds msgIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        List<OprRecMbr> ormList = new ArrayList<OprRecMbr>();
        for (int i = 0; i < msgIds.getIds().length; i++) {
            OprRecMbr oprRecMbr = new OprRecMbr();
            oprRecMbr.setMsgId(msgIds.getIds()[i]);
            oprRecMbr.setMbrId(userId);
            oprRecMbr.setReadDate(getCurrentDate(FORMAT_18_DATE_TIME));
            oprRecMbr.setIsRead(OprConstants.READED);
            ormList.add(oprRecMbr);

        }
        oprRecMbrService.readBatch(ormList);
        return R.ok();
    }

    @PostMapping("/delMsg")
    @ApiOperation(value = "会员消息", notes = "会员消息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R delMsg(@RequestBody MsgIds msgIds, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        List<OprRecMbr> ormList = new ArrayList<OprRecMbr>();
        for (int i = 0; i < msgIds.getIds().length; i++) {
            OprRecMbr oprRecMbr = new OprRecMbr();
            oprRecMbr.setMsgId(msgIds.getIds()[i]);
            oprRecMbr.setMbrId(userId);
            ormList.add(oprRecMbr);
        }
        oprRecMbrService.modifyOrm(ormList, null, CommonUtil.getIpAddress(request), Boolean.FALSE);
        return R.ok();
    }

    @GetMapping("/unReadMsgNo")
    @ApiOperation(value = "未读消条数", notes = "未读消息条数")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R unReadMsg(HttpServletRequest request) {
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        return R.ok().put("msgNo", oprRecMbrService.getUnreadMsgCount(loginName));
    }

    @GetMapping("/siteDataReport")
    @ApiOperation(value = "站点运营报表", notes = "站点运营报表")
    public R siteDataReport(@ModelAttribute BetDataDto betDataDto,
                            @RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize) {
        log.info("站点运营报表起始：");
        return R.ok().put("total", analysisService.getRptBetListReport(betDataDto, pageNo, pageSize));
    }

    /**
     * 查询游戏代码
     *
     * @return
     */
    @GetMapping("finalGameCode")
    @ApiOperation(value = "游戏代码", notes = "游戏代码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R finalGameCodByType(@RequestParam("codetype") String platFormId) {
        if (platFormId != null && !"".equals(platFormId.trim())) {
            return R.ok().put("page", analysisService.getGameType(platFormId, 0, CommonUtil.getSiteCode()));
        }
        return R.ok().put("page", analysisService.getPlatFormWithOrder(CommonUtil.getSiteCode()));
    }

    @GetMapping("/BetDetailList")
    @ApiOperation(value = "投注记录", notes = "全部游戏")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "Integer", paramType = "header")})
    @Login
    public R betDetailsData(@RequestParam("pageNo") @NotNull Integer pageNo,
                            @RequestParam("pageSize") @NotNull Integer pageSize,
                            @RequestParam(value = "depotName", required = false) String depotName,
                            @RequestParam(value = "gameCatId", required = false) Integer gameCatId,
                            @RequestParam(value = "betStrTime", required = false) String betStrTime,
                            @RequestParam(value = "betEndTime", required = false) String betEndTime,
                            @RequestParam(value = "status", required = false) String status,
                            HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        return R.ok()
                .put("page",
                        analysisService.getRptBetListPage(pageNo, pageSize, cpSite.getSiteCode(), loginName, depotName, gameCatId,
                                betStrTime, betEndTime, status))
                .put("total", analysisService.getRptBetListReport(cpSite.getSiteCode(), loginName, depotName, gameCatId,
                        betStrTime, betEndTime, status));
    }

    @GetMapping("/bonusList")
    @ApiOperation(value = "红利记录", notes = "查询抽奖活动记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R bonusList(@ModelAttribute TransferRequestDto requestDto,
                       @RequestParam("pageNo") @NotNull Integer pageNo,
                       @RequestParam("pageSize") @NotNull Integer pageSize,
                       @RequestParam("activityId") Integer activityId,
                       HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        // 查询成功状态的红利：1
        PageUtils page = oprActActivityService.findAccountBonusList(requestDto.getStartTime(),
                requestDto.getEntTime(), userId, pageNo, pageSize, Constants.EVNumber.one, activityId);
        return R.ok().put("page", page);
    }

    @Login
    @GetMapping("/checkAccountBank")
    @ApiOperation(value = "验证初次绑定银行卡", notes = "验证初次绑定银行卡")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R checkAccountBank(@ModelAttribute MbrBankcard mbrBankcard, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        mbrBankcard.setAccountId(userId);
        return R.ok().put(mbrBankcardService.checkAccountBank(mbrBankcard));
    }

    @Login
    @GetMapping("/secPwdUpdate")
    @ApiOperation(value = "修改会员提款密码", notes = "修改会员提款密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R checkAccountBank(@ModelAttribute MbrAccount mbraccModel, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount info = mbrAccountService.getAccountInfo(accountId);
        String salt = info.getSalt();
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setSecurePwd(new Sha256Hash(mbraccModel.getSecurePwd(), salt).toHex());
        mbrAccount.setId(accountId);
        return R.ok().put(mbrAccountService.update(mbrAccount));
    }

    @GetMapping("/noticeList")
    @ApiOperation(value = "查询弹窗广播列表", notes = "查询弹窗广播列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header")})
    public R checkAccountBank(HttpServletRequest request) {
        return R.ok().put(oprNoticeService.queryValidListPage());
    }

    @GetMapping("/ActivityList")
    @ApiOperation(value = "活动记录", notes = "活动记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R ActivityList(@RequestParam("pageNo") @NotNull Integer pageNo,
                          @RequestParam("pageSize") @NotNull Integer pageSize,
                          @RequestParam(value = "actCatId", required = false) Integer actCatId,
                          @RequestParam(value = "discount", required = false) Integer discount,
                          @RequestParam(value = "terminal", required = false) Byte terminal,
                          @RequestParam(value = "buttonShow", required = false) Integer buttonShow,
                          HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String ip = CommonUtil.getIpAddress(request);
        R result = R.ok();
        PageUtils page = oprActActivityService.webActivityList(pageNo, pageSize, actCatId, userId, terminal, discount, buttonShow, null, Constants.EVNumber.one, ip);
        result.put("page", page);
        List<AdvBanner> youhuiBanners = new ArrayList<>();
        // 获取活动页面banner
        if (terminal == null || terminal == ApiConstants.Terminal.pc) {
            youhuiBanners = oprAdvService.queryYouhuiBannerList();
        }
        JSONObject data = new JSONObject();
        data.put("youhuiBanners", youhuiBanners);
        result.put("data", data);
        return result;
    }

    @Login
    @GetMapping("/getMixActivity")
    @ApiOperation(value = "获取混合活动子规则数据", notes = "我的优惠-全部-获取混合活动的子规则数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getMixActivity(@RequestParam("activityId") @NotNull Integer activityId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(oprMixActivityService.getMixActivity(activityId, userId));
    }

    @Login
    @GetMapping("/getMixActivityClaimeAll")
    @ApiOperation(value = "获取混合活动子规则数据", notes = "我的优惠-已领取-获取混合活动的子规则数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getMixActivityClaimeAll(@RequestParam("activityId") @NotNull Integer activityId, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(oprMixActivityService.getMixActivityClaimeAll(activityId, userId));
    }


    @GetMapping("/claimedActivities")
    @ApiOperation(value = "优惠券已领取记录", notes = "优惠券已领取记录")
    @Login
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R claimedActivities(@RequestParam("pageNo") @NotNull Integer pageNo,
                               @RequestParam("pageSize") @NotNull Integer pageSize,
                               HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(oprActActivityService.claimedActivities(pageNo, pageSize, userId));
    }

    @GetMapping("/getWithdrawalLimit")
    @ApiOperation(value = "获取提款限制时间", notes = "获取提款限制时间")
    @Login
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getWithdrawalLimit(HttpServletRequest request) {
        SysSetting sysSetting = sysSettingService.getSysSetting(SystemConstants.IS_WITHDRAW_LIMIT_TIME_OPEN);
        WithdrawalLimitDto withdrawalLimitDto = new WithdrawalLimitDto();
           if( sysSetting.getSysvalue()!=null && sysSetting.getSysvalue().length()>0){
               withdrawalLimitDto.setIsWithdrawLimitTimeOpen(Integer.parseInt(sysSetting.getSysvalue()));
           }else {
               withdrawalLimitDto.setIsWithdrawLimitTimeOpen(Constants.EVNumber.zero);
           }

        SysSetting sysSetting2 = sysSettingService.getSysSetting(SystemConstants.WITHDRAW_LIMIT_TIME_LIST);
        if(sysSetting2.getSysvalue()!= null &&  sysSetting2.getSysvalue().length() > 0) {
            Type jsonType = new com.google.common.reflect.TypeToken<List<WithdrawLimitTimeDto>>() {
            }.getType();
            List<WithdrawLimitTimeDto> withdrawLimitTimeDtoList = jsonUtil.fromJson(sysSetting2.getSysvalue(), jsonType);
            withdrawalLimitDto.setWithdrawLimitTimeDtoList(withdrawLimitTimeDtoList);
        }else {
            withdrawalLimitDto.setWithdrawLimitTimeDtoList(new ArrayList<>());
        }

        withdrawalLimitDto.setCurrentTime(DateUtil.getCurrentDate(DateUtil.FORMAT_26_DATE_TIME));
        return R.ok().put(withdrawalLimitDto);

    }

    @GetMapping("/ptLogOut")
    @ApiOperation(value = "PT登出", notes = "PT登出")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R ptLogOut(HttpServletRequest request) {
        TGmDepot depot = new TGmDepot();
        depot.setDepotCode(ApiConstants.DepotCode.PT);
        List<TGmDepot> list = gmDepotMapper.select(depot);
        if (Objects.nonNull(list) && list.size() > 0) {
            //ptService.logOut(list.get(0).getId(), loginName, cpSite.getSiteCode());
        }

        return R.ok();
    }

    @Deprecated
    @Login
    @GetMapping("/auditDetail")
    @ApiOperation(value = "提款稽核明细-废弃", notes = "稽核明细-废弃")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R auditList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(auditAccountService.auditDetail(userId));
    }

    @Login
    @GetMapping("/isWithdrawal")
    @ApiOperation(value = "提款稽核验证及即时稽核数据", notes = "提款稽核验证及即时稽核数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R isWithdrawal(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(auditAccountService.isWithdrawal(accountId));
    }

    @GetMapping("/withdrawalCond")
    @ApiOperation(value = "取款条件", notes = "取款条件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R withdrawalCond(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        // 获取取款条件
        MbrWithdrawalCond cond = withdrawalCond.getMbrWithDrawal(userId);

        // 获取会员已取款次数和金额
        AccWithdraw withdraw = withdrawalCond.sumWithDraw(userId);
        if (Objects.nonNull(cond) && Objects.nonNull(withdraw)) {
            cond.setWithDrawalQuotaMbr(withdraw.getDrawingAmount());
            cond.setWithDrawalTimesMbr(withdraw.getWithdrawCount());
        }

        // 获取是否未通过稽核出款开关
        Integer isMultipleOpen = sysSettingService.queryPaySet().getIsMultipleOpen();
        if (isMultipleOpen != null && isMultipleOpen == 1) {
            // 如果有稽核未通过的，
            boolean checkoutAudit = fundWithdrawService.checkoutAudit(userId);
            // 稽核有未完成的
            if (!checkoutAudit) {
                // 是否还有免费提款次数
                Byte freeFee = fundWithdrawService.isFreeFee(cond.getFeeTimes(), cond.getFeeHours(), userId);
                // 免提款次数不足，前端展示手续费，预计到账金额
                if (freeFee != Available.enable) {
                    cond.setIsShowFee(true);
                }
            }
        }

        return R.ok().put("withdrawalCond", cond);
    }

    @GetMapping("/fastWithdrawList")
    @ApiOperation(value = "获取极速取款方式", notes = "获取极速取款方式")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R fastWithdrawList(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        //MbrAccount mbr = mbrAccountService.getAccountInfo(accountId);
        List<BankResponseDto> brDtoList = offlinePayService.findFastWithdrawList(accountId);
        return R.ok().put("fastDW", brDtoList);
    }

    @GetMapping("/isCrWithraw")
    @ApiOperation(value = "获取是否存在加密货币取款方式", notes = "获取是否存在加密货币取款方式")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R isCrWithraw(HttpServletRequest request) {
        FundMerchantPay pay = new FundMerchantPay();
        pay.setAvailable(Constants.EVNumber.one);
        pay.setMethodType(Constants.EVNumber.one);
        List<FundMerchantPay> list = merchantPayMapper.select(pay);
        boolean crFlag = false;
        if (Objects.nonNull(list) && list.size() > 0) {     // 存在USDT代付
            crFlag = true;
        }
        return R.ok().put("crFlag", crFlag);
    }

    @GetMapping("/depotCond")
    @ApiOperation(value = "线下存款条件", notes = "线下存款条件(lowQuota最低,topQuota最高 )")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R depotCond(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(userId);
        return R.ok().put("lowQuota", mbrDepositCond.getLowQuota()).put("topQuota", mbrDepositCond.getTopQuota());
    }

    public Map<String, Object> getToken(MbrAccount entity) {
        String token = jwtUtils.generateToken(entity.getId(), entity.getLoginName());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expire", jwtUtils.getExpire());
        return map;
    }

    @PostMapping("/actApply")
    @ApiOperation(value = "会员活动申请", notes = "会员活动申请")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R actApply(@RequestBody ActApplyDto actApplyDto, HttpServletRequest request) {
        Assert.isNull(actApplyDto.getActivityId(), "活动不能为空!");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String ip = CommonUtil.getIpAddress(request);
        return R.ok().put(oprActActivityService.applyActivity(userId, actApplyDto, CommonUtil.getSiteCode(), ip));
    }

    @GetMapping("/sendMsg")
    @ApiOperation(value = "发送消息", notes = "发送消息()")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "siteCode", value = "siteCode", required = true, dataType = "String", paramType = "header")})
    @Login
    public R sendMsg(HttpServletRequest request) {
        String schemaName = CommonUtil.getSiteCode() == null ? "test" : CommonUtil.getSiteCode();
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        applicationEventPublisher
                .publishEvent(new BizEvent(this, schemaName, userId, BizEventType.MEMBER_REGISTER_SUCCESS));// 在这里发布事件
        return R.ok().put("code", 200);
    }

    @GetMapping("/depotBalanceList")
    @ApiOperation(value = "平台信息列表", notes = "平台信息列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R depotList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("tGmDepots", tGmDepotService.findDepotBalanceList(userId));
    }

    @GetMapping("/setAccQQ")
    @ApiOperation(value = "更新QQ号码", notes = "更新QQ号码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R setAccQQ(MbrAccount mbrAccount, HttpServletRequest request) {
        Assert.isQq(mbrAccount.getQq(), "未通过验证，请输入正确的QQ号码");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setQq(mbrAccount.getQq());
        mbrAccountService.saveQQOrWeChat(account);
        return R.ok();
    }

    @Login
    @PostMapping("/setGender")
    @ApiOperation(value = "修改性别", notes = "修改性别")
    public R setGender(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setGender(mbrAccount.getGender());
        mbrAccountService.saveQQOrWeChat(account);
        return R.ok();
    }

    @Login
    @PostMapping("/setBirthday")
    @ApiOperation(value = "修改生日", notes = "修改生日")
    public R setBirthday(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount mbrAccount1 = mbrAccountService.getAccountInfo(userId);
        if (StringUtils.isNotEmpty(mbrAccount1.getBirthday())) {
            throw new R200Exception("生日不可变更");
        }
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setBirthday(mbrAccount.getBirthday());
        mbrAccountService.saveQQOrWeChat(account);
        return R.ok();
    }

    @Login
    @GetMapping("/setAccWeChat")
    @ApiOperation(value = "更新微信号码", notes = "更新微信号码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R setAccWeChat(MbrAccount mbrAccount, HttpServletRequest request) {
        Assert.isWeChat(mbrAccount.getWeChat(), "微信号格式不正确");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setWeChat(mbrAccount.getWeChat());
        mbrAccountService.saveQQOrWeChat(account);
        return R.ok();
    }

    @Login
    @PostMapping("/setAccMail")
    @ApiOperation(value = "更新电子邮箱", notes = "更新电子邮箱")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R setAccMail(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        Assert.checkEmail(mbrAccount.getEmail(), "邮箱格式不正确!");
        Assert.isLenght(mbrAccount.getEmail(), "邮箱长度不能大于30位", 5, 30);
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        if (mbrAccountService.updateMail(userId, mbrAccount.getEmail(), CommonUtil.getIpAddress(request)) > 0) {
            return R.ok();
        } else {
            throw new R200Exception("电子邮箱修改失败！");
        }
    }

    @Login
    @GetMapping("/accountBonusList")
    @ApiOperation(value = "会员优惠券管理", notes = "会员优惠券管理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R accountBonusList(@RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize,
                              @RequestParam(value = "status", required = false) Integer status,
                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().putPage(oprActActivityCastService.accountBonusList(userId, status, pageNo, pageSize));
    }

    @Login
    @GetMapping("/accountBonusOne")
    @ApiOperation(value = "会员优惠券管理ID查询", notes = "会员优惠券管理ID查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R accountBonusOne(@RequestParam("id") Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().putPage(oprActActivityCastService.accountBonusOne(userId, id));
    }

    @Login
    @GetMapping("/availableAccountBonusList")
    @ApiOperation(value = "可用的会员优惠券管理", notes = "可用的会员优惠券管理")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R availableAccountBonusList(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().putPage(oprActActivityCastService.availableAccountBonusList(userId));
    }

    @Login
    @PostMapping("/setFreeWalletSwitch")
    @ApiOperation(value = "免转钱包开关", notes = "免转钱包开关")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R setFreeWalletSwitch(@RequestBody MbrAccount mbrAccount, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount account = new MbrAccount();
        account.setId(userId);
        account.setFreeWalletSwitch(mbrAccount.getFreeWalletSwitch());
        mbrAccountService.update(account);
        return R.ok();
    }

    @Login
    @GetMapping("/setSecurePwdOfFirst")
    @ApiOperation(value = "首次设置提款密码", notes = "首次设置提款密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R setSecurePwdOfFirst(MbrAccount mbrAccount, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        mbrAccount.setId(userId);
        mbrAccountService.setSecurePwdOfFirst(mbrAccount);
        return R.ok();
    }

    @Login
    @GetMapping("/isDepotAudit")
    @ApiOperation(value = "查询会员是否存在平台优惠稽核 false稽核不过 true通过", notes = "查询会员是否存在平台优惠稽核 false稽核不过 true通过")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R isDepotAudit(@RequestParam("depotId") Integer depotId, HttpServletRequest request) {
        Assert.isNull(depotId, "平台ID不能为空");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(auditAccountService.isBounsOut(userId, depotId));
    }

    @Login
    @GetMapping("/depotAudit")
    @ApiOperation(value = "查询会员平台优惠稽核明细", notes = "查询会员平台优惠稽核明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R depotAudit(@RequestParam("depotId") Integer depotId, HttpServletRequest request) {
        Assert.isNull(depotId, "平台ID不能为空");
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(auditAccountService.getDepotAuditDto(userId, depotId));
    }

    @Login
    @GetMapping("messageInfo")
    @ApiOperation(value = "查询会员消息/留言(收件箱和通知)", notes = "查询会员消息/留言(收件箱和通知)")
    public R messageList(@ModelAttribute MbrMessageInfo dto,
                         @RequestParam("pageNo") @NotNull Integer pageNo,
                         @RequestParam("pageSize") @NotNull Integer pageSize,
                         HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(messageService.accountMessageList(pageNo, pageSize, accountId, dto));
    }

    @Login
    @PostMapping("deleteMessageInfoById")
    @ApiOperation(value = "删除会员消息/留言(收件箱和通知)", notes = "删除会员消息/留言(收件箱和通知)")
    public R deleteMessageInfoById(@ModelAttribute MbrMessageInfo dto,
                                   HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(messageService.deleteMessageInfoById(accountId, dto));
    }


    @Login
    @GetMapping("setMessageMbrRead")
    @ApiOperation(value = "设置通知为会员已读", notes = "设置通知为会员已读")
    public R setMessageMbrRead(@ModelAttribute MbrMessageInfo info,
                               HttpServletRequest request) {
//        Assert.isNull(info.getSetReadType(),"setReadType不能为空");
        // 0 单条通知置为已读 id不能为空
        if (Integer.valueOf(Constants.EVNumber.zero).equals(info.getSetReadType()) || Objects.isNull(info.getSetReadType())) {
            Assert.isNull(info.getId(), "id不能为空");
        }

        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        info.setAccountId(accountId);
        messageService.setMessageMbrRead(info);
        return R.ok();
    }

    @Login
    @PostMapping(value = "messageSend")
    @ApiOperation(value = "会员留言板发送", notes = "会员留言板发送")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R messageSend(HttpServletRequest request,
                         @RequestParam(value = "textContent", required = false) String textContent,
                         @RequestParam(value = "uploadMessageFile", required = false) MultipartFile uploadMessageFile) {
        if (StringUtils.isEmpty(textContent) && Objects.isNull(uploadMessageFile)) {
            throw new R200Exception("发送内容不能为空");
        }
        if (StringUtils.isNotEmpty(textContent)) {
            // 屏蔽emoj表情
            if (StringUtil.isHasEmoji(textContent)) {
                throw new R200Exception("不支持发送表情！");
            }
            Assert.isLenght(textContent, "发送内容最大长度为1000", 0, 1000);
        }
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        String resut = messageService.accountMessageSend(accountId, loginName, uploadMessageFile, textContent);
        return R.ok().put(resut);
    }

    @Login
    @GetMapping("messageUnread")
    @ApiOperation(value = "会员是否存在未读消息", notes = "含收件箱和通知")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R messageUnread(@ModelAttribute MbrMessageInfo info, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        int count = messageService.messageUnread(accountId, info);
        return R.ok().put(count > 0 ? Boolean.TRUE : Boolean.FALSE).put("count", count);
    }

    @Login
    @GetMapping("messageDelete")
    @ApiOperation(value = "删除会员留言", notes = "删除会员留言")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R messageDelete(@RequestParam("id") @NotNull Integer id) {
        messageService.messageDelete(id);
        return R.ok();
    }

    @Login
    @GetMapping("findWinLostList")
    @ApiOperation(value = "会员查询输赢报表", notes = "会员查询输赢报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R findWinLostList(@ModelAttribute WinLostReportModelDto reportModelDto, @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize, HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(accountId);
        reportModelDto.setLoginName(mbrAccount.getLoginName());
        return R.ok().put(winLoseService.findWinLostAccountPage(reportModelDto, pageNo, pageSize));
    }


    @GetMapping("getLevelRule")
    @ApiOperation(value = "获得活动等级晋升规则", notes = "获得活动等级晋升规则")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R mbrActivityLevelList() {
        return R.ok().put("levelRule", sysSettingService.findActLevelStaticsRuleAndDescript()).put("staticsRule", activityLevelCastService.mbrActivityLevelList());
    }

    @Login
    @GetMapping("accountWaterRate")
    @ApiOperation(value = "查询返水比率", notes = "查询返水比率")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R findRuleRate(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        List<WaterDepotDto> waterDepotDtoList = waterSettlementService.findAccountWaterRate(accountId, CommonUtil.getSiteCode());
        BigDecimal minLimitAmount = waterSettlementService.findRuleRateLimit();

        Optional<BigDecimal> sumAmount = waterDepotDtoList.stream()
                .filter(p -> nonNull(p.getAmount()))
                .map(WaterDepotDto::getAmount).reduce(BigDecimal::add);

        return R.ok().put(waterDepotDtoList).put("sumAmount", sumAmount.isPresent() ? sumAmount.get() : BigDecimal.ZERO)
                .put("minLimitAmount", minLimitAmount);
    }

    @Login
    @GetMapping("settlementWater")
    @ApiOperation(value = "一键洗码", notes = "一键洗码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R settlementWater(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        String siteCode = CommonUtil.getSiteCode();
        String key = RedisConstants.ACCOUNT_SETTLEMENT_WATER + siteCode + loginName;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, accountId, 10, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                waterSettlementService.settlementWater(accountId, CommonUtil.getSiteCode());
            } finally {
                redisService.del(key);
            }
        }
        return R.ok();
    }

    @Login
    @GetMapping("waterDetailList")
    @ApiOperation(value = "查询返水交易记录", notes = "查询返水交易记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R waterDetailList(HttpServletRequest request,
                             @ModelAttribute OprActBonus oprActBonus,
                             @RequestParam("pageNo") @NotNull Integer pageNo,
                             @RequestParam("pageSize") @NotNull Integer pageSize) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        oprActBonus.setAccountId(accountId);
        return R.ok().put(waterSettlementService.waterDetailList(oprActBonus, pageNo, pageSize));
    }

    @Login
    @GetMapping("depotWaterDetailList")
    @ApiOperation(value = "查询返水交易记录", notes = "查询返水交易记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R depotWaterDetailList(HttpServletRequest request,
                                  @ModelAttribute OprActBonus oprActBonus,
                                  @RequestParam("pageNo") @NotNull Integer pageNo,
                                  @RequestParam("pageSize") @NotNull Integer pageSize) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        oprActBonus.setAccountId(accountId);
        return R.ok().put(waterSettlementService.depotWaterDetailList(oprActBonus, pageNo, pageSize));
    }

    @Login
    @GetMapping("getLevelPromoteData")
    @ApiOperation(value = "获得会员活动等级晋升数据", notes = "获得会员活动等级晋升数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getLevelPromoteData(HttpServletRequest request) {
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(activityLevelCastService.getLevelPromoteData(accountId));
    }

    //银行卡识别
    @Login
    @PostMapping("bankCardOCR")
    @ApiOperation(value = "银行卡识别", notes = "银行卡识别")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R bankCardOCR(@RequestParam(value = "bankCardPic", required = true) MultipartFile bankCardPic) {
        Assert.isNull(bankCardPic, "bankCardPic不为空！");
        return R.ok().put("data", baiduAipUtil.bankCardOCR(bankCardPic));
    }

    @PostMapping("/sendSecurityMobCode")
    @ApiOperation(value = "获取安全校验短信验证码", notes = "获取安全校验短信验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R sendSecurityMobCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        String language = request.getHeader("language");
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

        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        // 发送验证码
        String code = apiUserService.sendSecurityMobCode(vfyDto, userId, Constants.EVNumber.four, language);
        if (!StringUtils.isEmpty(code)) {
            vfyDto.setCode(code);
            apiUserService.updateSecurityMobCode(cpSite.getSiteCode(), loginName, vfyDto);
            return R.ok();
        } else {
            return R.error();
        }
    }

    @PostMapping("/sendSecurityMobCodeTwo")
    @ApiOperation(value = "获取安全校验短信验证码", notes = "获取安全校验短信验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R sendSecurityMobCodeTwo(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        String language = request.getHeader("language");
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
        vfyDto.setLoginName(vfyDto.getLoginName().toLowerCase());
        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(vfyDto.getLoginName());
        if (!nonNull(mbrAccount)) {
            throw new R200Exception("发送短信用户名不存在");
        }
        //Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        //String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        // 发送验证码
        String code = apiUserService.sendSecurityMobCode(vfyDto, mbrAccount.getId(), Constants.EVNumber.two, language);
        if (!StringUtils.isEmpty(code)) {
            vfyDto.setCode(code);
            apiUserService.updateSecurityMobCode(cpSite.getSiteCode(), mbrAccount.getLoginName().toLowerCase(), vfyDto);
            return R.ok();
        } else {
            return R.error();
        }
    }

    @PostMapping("/vfySecurityMobCode")
    @ApiOperation(value = "验证安全验证码", notes = "验证安全验证码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R vfySecurityMobCode(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        VfyMailOrMobDto vfyDotCahce = apiUserService.querySecurityMobCodeCache(cpSite.getSiteCode(), loginName);
        if (Objects.isNull(vfyDotCahce)) {
            throw new RRException("请先获取验证码!");
        }
        if (!vfyDto.getMobile().equals(vfyDotCahce.getMobile())) {
            throw new RRException("验证手机号与发送短信的手机号不匹配!");
        }
        if (!vfyDto.getCode().equals(vfyDotCahce.getCode())) {
            throw new RRException("验证码不正确!");
        }
        return R.ok();
    }

    @PostMapping("/vfySecurityMobCodeTwo")
    @ApiOperation(value = "验证安全验证码(登录设备校验)", notes = "验证安全验证码(登录设备校验)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R vfySecurityMobCodeTwo(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        TCpSite cpSite = (TCpSite) request.getAttribute(ApiConstants.WEB_SITE_OBJECT);
        VfyMailOrMobDto vfyDotCahce = apiUserService.querySecurityMobCodeCache(cpSite.getSiteCode(), vfyDto.getLoginName().toLowerCase());
        if (Objects.isNull(vfyDotCahce)) {
            throw new RRException("请先获取验证码!");
        }
        if (!vfyDto.getMobile().equals(vfyDotCahce.getMobile())) {
            throw new RRException("验证手机号与发送短信的手机号不匹配!");
        }
        if (!vfyDto.getCode().equals(vfyDotCahce.getCode())) {
            throw new RRException("验证码不正确!");
        }
        return R.ok();
    }

    @PostMapping("/deviceBind")
    @ApiOperation(value = "会员设备绑定", notes = "会员设备绑定")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R deviceBind(@RequestBody MbrAccountDevice deviceDto, HttpServletRequest request) {
        Assert.isBlank(deviceDto.getDeviceUuid(), "设备不为空！");
        Assert.isNull(deviceDto.getDeviceType(), "类型不为空！");

        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        deviceDto.setAccountId(userId);
        deviceDto.setLoginName(loginName.toLowerCase());

        mbrAccountDeviceService.deviceBind(deviceDto);
        //新增或者更新常用设备
        mbrAccountService.saveOrUpdateDevice(deviceDto);
        return R.ok();
    }

    @GetMapping("/bonusAndTaskList")
    @ApiOperation(value = "优惠+任务", notes = "优惠+任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R bonusAndTaskList(@ModelAttribute TransferRequestDto requestDto,
                              @RequestParam("pageNo") @NotNull Integer pageNo,
                              @RequestParam("pageSize") @NotNull Integer pageSize,
                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);

        PageUtils page = oprActActivityService.bonusAndTaskList(requestDto.getStartTime(),
                requestDto.getEntTime(), userId, pageNo, pageSize);
        return R.ok().put("page", page);
    }

    @GetMapping("/depositLockStatus")
    @ApiOperation(value = "查询存款锁定状态", notes = "查询存款锁定状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R depositLockStatus(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrDepositCount ret = mbrAccountService.depositLockStatus(userId);
        return R.ok().put(ret);
    }

    @GetMapping("helpCategoryList")
    @ApiOperation(value = "获得站点帮助中心分类列表", notes = "获得站点帮助中心分类列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R helpCategoryList() {
        OprHelpCategory oprHelpCategory = new OprHelpCategory();
        return R.ok().put("categoryList", oprAdvService.findCategory(oprHelpCategory));
    }

    @GetMapping("findTitleAndContent")
    @ApiOperation(value = "获得站点帮助中心分类列表", notes = "获得站点帮助中心分类列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R findTitleAndContent(@RequestParam Integer id) {
        OprHelpCategory oprHelpCategory = new OprHelpCategory();
        oprHelpCategory.setId(id);
        return R.ok().put("categoryDetail", oprAdvService.findTitleAndContent(oprHelpCategory));
    }

    @PostMapping("/vfyRegisteAndLogin")
    @ApiOperation(value = "登录注册校验是否需要弹出验证", notes = "登录注册校验是否需要弹出验证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R vfyRegisteAndLogin(@RequestBody LoginUserDto loginUserDto, HttpServletRequest request) {
        String key = RedisConstants.REDIS_MOBILE_LOGIN_CODE + CommonUtil.getSiteCode() + loginUserDto.getLoginName();
        Object kaptchaLog = redisService.getRedisValus(key);
        if (isNull(kaptchaLog)) {
            return R.ok(false);
        }
        return R.ok(true);
    }

    @Login
    @GetMapping("/aiRecommendSeven")
    @ApiOperation(value = "ai推荐7日游戏列表", notes = "ai推荐7日游戏列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R aiRecommendSeven(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put(tGmDepotService.getAiRecommendSeven(userId));
    }

    @Login
    @PostMapping("/uploadImage")
    @ApiOperation(value = "上传图片", notes = "上传图片")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R uploadImage(@RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile) {
        Assert.isNull(uploadFile, "不能为空");
        return R.ok().put("path", oprAdvService.getSingleImageUrl(uploadFile));
    }


    @Login
    @GetMapping("/chkUserDeposit")
    @ApiOperation(value = "会员接口-账号检测是否在线", notes = "会员接口-账号检测是否在线!")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R chkUserDeposit(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        Integer count = fundDepositService.findTotalsDepositCount(userId);
        Boolean rt = Boolean.FALSE;
        if (count > 0) {
            rt = Boolean.TRUE;
        }
        return R.ok(rt);
    }

    @Login
    @GetMapping("/getActActivity")
    @ApiOperation(value = "获取单个活动内容", notes = "我的优惠-全部-获取混合活动的子规则数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R getActActivity(@RequestParam("tmplCode") @NotNull String tmplCode, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        OprActActivity actActivity = oprActActivityCastService.getActActivity(tmplCode, userId);
        return R.ok().put(actActivity);
    }
    
    @Login
    @GetMapping("/lineService")
    @ApiOperation(value = "小额客服线", notes = "小额客服线")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
    	@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R lineService(HttpServletRequest request) {
    	Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
    	return R.ok().put(setSmallAmountLineService.checkUser(userId));
    }
    
    @Login
    @GetMapping("/lineServiceCount")
    @ApiOperation(value = "小额客服线当日点击次数统计", notes = "小额客服线当日点击次数统计")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
    	@ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    public R lineServiceCount(HttpServletRequest request) {
    	Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
    	setSmallAmountLineService.lineServiceCount(userId);
    	return R.ok();
    }


    @GetMapping("/cuiDanWithdraw")
    @ApiOperation(value = "查询取款记录", notes = "查询取款记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R cuiDanWithdraw(@RequestParam("orderId") Integer orderId,
                         HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        AccWithdraw accWithdraw = fundWithdrawService.findCuiDanAccWithdraw(userId, orderId);

        if(accWithdraw == null){
            throw  new R200Exception("订单不存在");
        }
        if(accWithdraw.getStatus() == Constants.EVNumber.zero || accWithdraw.getStatus() == Constants.EVNumber.one){
            throw  new R200Exception("订单状不正确");
        }

        if(accWithdraw.getCuiCount() > 0){
            throw  new R200Exception("该订单已经催单");
        }

        Integer time = sysSettingService.queryCuiDanSet();
        if(time == 0){
            throw  new R200Exception("不允许催单");
        }
        fundWithdrawService.updateCuiDanAccWithdraw(orderId);
        return R.ok();
    }


    @GetMapping("/withdrawalConfirm")
    @ApiOperation(value = "会员取款确认", notes = "会员取款确认")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", required = true, dataType = "Integer", paramType = "header"),
            @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R withdrawalConfirm(@RequestParam("orderId") Integer orderId, HttpServletRequest request) {

        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        AccWithdraw accWithdraw = fundWithdrawService.findCuiDanAccWithdraw(userId, orderId);

        if(accWithdraw == null){
            throw  new R200Exception("订单不存在");
        }
        if(accWithdraw.getStatus() == Constants.EVNumber.zero || accWithdraw.getStatus() == Constants.EVNumber.one){
            throw  new R200Exception("订单状不正确");
        }
        return R.ok();
    }
}
