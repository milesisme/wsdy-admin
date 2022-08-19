package com.wsdy.saasops.agapi.modules.controller;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.wsdy.saasops.agapi.annotation.AgentLogin;
import com.wsdy.saasops.agapi.modules.service.AgentSafeyInfoService;
import com.wsdy.saasops.api.modules.user.dto.VfyMailOrMobDto;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.agent.entity.AgentCryptoCurrencies;
import com.wsdy.saasops.modules.agent.entity.AgentSubAccount;
import com.wsdy.saasops.modules.agent.entity.AgyBankcard;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.agent.service.AgentCryptoCurrenciesService;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.base.service.BaseBankService;
import com.wsdy.saasops.modules.system.pay.service.CryptoCurrenciesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

@RestController
@Slf4j
@RequestMapping("/agapi/n2/safety")
@Api(tags = "安全信息")
public class AgentSafetyInfoController {

    @Autowired
    private AgentSafeyInfoService safeyInfoService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private AgentAccountService accountService;
    @Autowired
    private BaseBankService baseBankService;
    @Autowired
    private AgentCryptoCurrenciesService cryptoCurrenciesService;
    @Autowired
    private CryptoCurrenciesService currenciesService;
    @Autowired
    private CaptchaService captchaService;

    @AgentLogin
    @GetMapping("agentInfo")
    @ApiOperation(value = "查询个人信息", notes = "查询个人信息")
    public R agentInfo(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        return R.ok().put(safeyInfoService.agentInfo(account));
    }

    @AgentLogin
    @PostMapping("updateContact")
    @ApiOperation(value = "修改个人信息", notes = "修改个人信息")
    public R updateContact(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        safeyInfoService.updateContact(account, agentAccount);
        return R.ok();
    }

    @AgentLogin
    @PostMapping("securePwdMobCodeReg")
    @ApiOperation(value = "会员修改安全密码手机验证", notes = "会员修改安全密码手机验证")
    public R sendMobCodeReg(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        String language = request.getHeader("language");
        Assert.isPhoneAll(vfyDto.getMobile(), vfyDto.getMobileAreaCode());
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        safeyInfoService.checkoutAgentMobile(account.getId(), vfyDto.getMobile());

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
        } else {
            Assert.isNull(vfyDto.getCodeSign(), "error1!");
            Assert.isNull(vfyDto.getKaptcha(), "error2!");
            String kaptcha = redisService.getKeyAndDel(vfyDto.getCodeSign());
            if (!vfyDto.getKaptcha().equals(kaptcha)) {
                return R.error("图形验证码错误！");
            }
        }

        if (isNull(vfyDto.getMobile()) || vfyDto.getMobile().contains("*")){
            throw new R200Exception("手机号码错误，请联系管理员");
        }

        String code = apiUserService.sendAgentSmsRegCode(vfyDto.getMobile(), vfyDto.getMobileAreaCode(), Constants.EVNumber.three, language);
        if (!StringUtils.isEmpty(code)) {
            log.info("代理修改安全密码短信验证码" + code + "," + vfyDto.getMobile());
            String key = RedisConstants.AGENT_REDIS_MOBILE_SECURE_CODE + CommonUtil.getSiteCode() + vfyDto.getMobile();
            redisService.setRedisExpiredTime(key, code, 10, TimeUnit.MINUTES);
            return R.ok();
        }
        return R.error();
    }

    @AgentLogin
    @PostMapping("checkoutMobileCode")
    @ApiOperation(value = "下一步验证短信验证码", notes = "下一步验证短信验证码")
    public R checkoutMobileCode(@RequestBody AgentAccount agentAccount) {
        Assert.isPhone(agentAccount.getMobile(), "手机号码格式错误!");
        Assert.isBlank(agentAccount.getMobileCaptchareg(), "短信验证码不能为空");
        String key = RedisConstants.AGENT_REDIS_MOBILE_SECURE_CODE + CommonUtil.getSiteCode() + agentAccount.getMobile();
        Object obj = redisService.getRedisValus(key);
        if (isNull(obj)) {
            throw new R200Exception("确认时间超过10分钟，请重新注册！");
        }
        if (!agentAccount.getMobileCaptchareg().equalsIgnoreCase(obj.toString())) {
            throw new R200Exception("验证码不正确!");
        }
        return R.ok();
    }

    @AgentLogin
    @PostMapping("updateSecurePwd")
    @ApiOperation(value = "修改安全密码", notes = "修改安全密码")
    public R updateSecurePwd(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isPhone(agentAccount.getMobile(), "手机号码格式错误!");
        Assert.isBlank(agentAccount.getMobileCaptchareg(), "短信验证码不能为空");
        Assert.isBlank(agentAccount.getSecurePwd(), "安全密码不能为空");

        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        String key = RedisConstants.AGENT_REDIS_MOBILE_SECURE_CODE + CommonUtil.getSiteCode() + agentAccount.getMobile();
        Object obj = redisService.getRedisValus(key);
        if (isNull(obj)) {
            throw new R200Exception("确认时间超过10分钟，请重新注册！");
        }
        if (!agentAccount.getMobileCaptchareg().equalsIgnoreCase(obj.toString())) {
            throw new R200Exception("验证码不正确!");
        }
        safeyInfoService.updateSecurePwd(account, agentAccount);
        return R.ok();
    }

    @AgentLogin
    @PostMapping("updateAgyPwd")
    @ApiOperation(value = "修改登录密码", notes = "修改登录密码")
    public R updateAgyPwd(@RequestBody AgentAccount agentAccount, HttpServletRequest request) {
        Assert.isBlank(agentAccount.getAgyPwd(), "旧密码不能为空");
        Assert.isBlank(agentAccount.getNewAgyPwd(), "新密码不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        safeyInfoService.updateAgyPwd(account, agentAccount);
        return R.ok();
    }

    @AgentLogin
    @GetMapping("agyBankList")
    @ApiOperation(value = "代理银行查看代理id", notes = "代理银行查看代理id")
    public R agyBankList(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        return R.ok().put(accountService.agyBankList(account.getId()));
    }
    
    @AgentLogin
    @GetMapping("agyAlipayList")
    @ApiOperation(value = "代理支付宝查看代理id", notes = "代理支付宝查看代理id")
    public R agyAlipayList(HttpServletRequest request) {
    	AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
    	if (account.getAttributes() == Constants.EVNumber.four) {
    		throw new R200Exception("无此权限");
    	}
    	return R.ok().put(accountService.agyAlipayList(account.getId()));
    }

    @AgentLogin
    @PostMapping("agyBankSave")
    @ApiOperation(value = "代理银行卡新增", notes = "代理银行卡新增")
    public R agyBankSave(@RequestBody AgyBankcard bankcard, HttpServletRequest request) {
        Assert.isNull(bankcard.getBankCardId(), "银行卡id不能为空");
        Assert.isBlank(bankcard.getCardNo(), "银行卡号不能为空");
        Assert.isBlank(bankcard.getSecurePwd(), "安全密码不能为空");
        Assert.isBlank(bankcard.getRealName(), "姓名不能为空");
        Assert.isBlank(bankcard.getMobileCaptchareg(), "手机验证码不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        String key = RedisConstants.AGENT_REDIS_MOBILE_CARD_CODE + CommonUtil.getSiteCode() + account.getMobile();
        Object obj = redisService.getRedisValus(key);
        if (isNull(obj)) {
            throw new R200Exception("确认时间超过10分钟，请重新注册！");
        }
        if (!bankcard.getMobileCaptchareg().equalsIgnoreCase(obj.toString())) {
            throw new R200Exception("验证码不正确!");
        }
        String csPassword = new Sha256Hash(bankcard.getSecurePwd(), account.getSalt()).toHex();
        if (!csPassword.equals(account.getSecurePwd())) {
            throw new R200Exception("支付密码错误");
        }
        //   bankcard.setRealName(account.getRealName());
        bankcard.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bankcard.setCreateUser(account.getAgyAccount());
        bankcard.setAccountId(account.getId());
        accountService.agyBankSave(bankcard);
        return R.ok();
    }
    

    @AgentLogin
    @PostMapping("/agyAlipaySave")
    @ApiOperation(value = "代理新增支付宝账号", notes = "代理新增支付宝账号")
    public R saveAlipayAccount(@RequestBody AgyBankcard agyBankcard, HttpServletRequest request) {
    	AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        String key = RedisConstants.AGENT_REDIS_MOBILE_CARD_CODE + CommonUtil.getSiteCode() + account.getMobile();
        Object obj = redisService.getRedisValus(key);
        if (isNull(obj)) {
            throw new R200Exception("未获取到相关验证码信息，请重新操作！");
        }
        if (!agyBankcard.getMobileCaptchareg().equalsIgnoreCase(obj.toString())) {
            throw new R200Exception("验证码不正确!");
        }
        String csPassword = new Sha256Hash(agyBankcard.getSecurePwd(), account.getSalt()).toHex();
        if (!csPassword.equals(account.getSecurePwd())) {
            throw new R200Exception("支付密码错误");
        }
        Assert.isBlank(agyBankcard.getCardNo(), "开户账号不能为空!");
        Assert.isBankCardNo(agyBankcard.getCardNo(), "长度只能为10~32位!", 10, 32);
        agyBankcard.setAccountId(account.getId());
        agyBankcard.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        agyBankcard.setCreateUser(account.getAgyAccount());
        accountService.saveBankCard(agyBankcard);
        return R.ok();
    }
    
    
    @AgentLogin
    @PostMapping("/agyAlipayUpdate")
    @ApiOperation(value = "代理支付宝修改", notes = "代理支付宝修改")
    public R agyAlipayUpdate(@RequestBody AgyBankcard bankcard, HttpServletRequest request) {
        Assert.isNull(bankcard.getId(), "id不能为空");
        Assert.isNull(bankcard.getBankCardId(), "支付宝id不能为空");
        Assert.isBlank(bankcard.getCardNo(), "支付宝号不能为空");
        Assert.isBlank(bankcard.getSecurePwd(), "安全密码不能为空");
        Assert.isBlank(bankcard.getRealName(), "姓名不能为空");
        Assert.isBlank(bankcard.getMobileCaptchareg(), "手机验证码不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        String key = RedisConstants.AGENT_REDIS_MOBILE_CARD_CODE + CommonUtil.getSiteCode() + account.getMobile();
        Object obj = redisService.getRedisValus(key);
        if (isNull(obj)) {
            throw new R200Exception("确认时间超过10分钟，请重新注册！");
        }
        if (!bankcard.getMobileCaptchareg().equalsIgnoreCase(obj.toString())) {
            throw new R200Exception("验证码不正确!");
        }
        bankcard.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bankcard.setModifyUser(account.getAgyAccount());
        bankcard.setAccountId(account.getId());
        accountService.agyBankUpdate(bankcard, bankcard.getRealName());
        return R.ok();
    }

    @AgentLogin
    @PostMapping("bankcardMobCodeReg")
    @ApiOperation(value = "会员银行卡手机验证", notes = "会员银行卡手机验证")
    public R bankcardMobCodeReg(@RequestBody VfyMailOrMobDto vfyDto, HttpServletRequest request) {
        String language = request.getHeader("language");
        Assert.isPhoneAll(vfyDto.getMobile(), vfyDto.getMobileAreaCode());
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        safeyInfoService.checkoutAgentMobile(account.getId(), vfyDto.getMobile());
        if (!StringUtil.isEmpty(vfyDto.getCaptchaVerification())) {   // 行为验证码
            CaptchaVO captchaVO = new CaptchaVO();
            captchaVO.setCaptchaVerification(vfyDto.getCaptchaVerification());
            ResponseModel response = captchaService.verification(captchaVO);
            if (response.isSuccess() == false) {
                throw new R200Exception("银行卡验证码校验失败,repCode=" + response.getRepCode());
            }
        } else {
            Assert.isNull(vfyDto.getCodeSign(), "error1!");
            Assert.isNull(vfyDto.getKaptcha(), "error2!");
            String kaptcha = redisService.getKeyAndDel(vfyDto.getCodeSign());
            if (!vfyDto.getKaptcha().equals(kaptcha)) {
                return R.error("图形验证码错误！");
            }
        }
        if (isNull(vfyDto.getMobile()) || vfyDto.getMobile().contains("*")){
            throw new R200Exception("手机号码错误，请联系管理员");
        }

        String code = apiUserService.sendAgentSmsRegCode(vfyDto.getMobile(), vfyDto.getMobileAreaCode(), Constants.EVNumber.zero, language);
        if (!StringUtils.isEmpty(code)) {
            log.info("代理银行卡验证码" + code + "," + vfyDto.getMobile());
            String key = RedisConstants.AGENT_REDIS_MOBILE_CARD_CODE + CommonUtil.getSiteCode() + vfyDto.getMobile();
            redisService.setRedisExpiredTime(key, code, 10, TimeUnit.MINUTES);
            return R.ok();
        }
        return R.error();
    }

    @AgentLogin
    @PostMapping("agyBankUpdate")
    @ApiOperation(value = "代理银行卡修改", notes = "代理银行卡修改")
    public R agyBankUpdate(@RequestBody AgyBankcard bankcard, HttpServletRequest request) {
        Assert.isNull(bankcard.getId(), "id不能为空");
        Assert.isNull(bankcard.getBankCardId(), "银行卡id不能为空");
        Assert.isBlank(bankcard.getCardNo(), "银行卡号不能为空");
        Assert.isBlank(bankcard.getSecurePwd(), "安全密码不能为空");
        Assert.isBlank(bankcard.getRealName(), "姓名不能为空");
        Assert.isBlank(bankcard.getMobileCaptchareg(), "手机验证码不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        String key = RedisConstants.AGENT_REDIS_MOBILE_CARD_CODE + CommonUtil.getSiteCode() + account.getMobile();
        Object obj = redisService.getRedisValus(key);
        if (isNull(obj)) {
            throw new R200Exception("确认时间超过10分钟，请重新注册！");
        }
        if (!bankcard.getMobileCaptchareg().equalsIgnoreCase(obj.toString())) {
            throw new R200Exception("验证码不正确!");
        }
   /*     String csPassword = new Sha256Hash(bankcard.getSecurePwd(), account.getSalt()).toHex();
        if (!csPassword.equals(account.getSecurePwd())) {
            throw new R200Exception("支付密码错误");
        }*/
        bankcard.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        bankcard.setModifyUser(account.getAgyAccount());
        bankcard.setAccountId(account.getId());
        accountService.agyBankUpdate(bankcard, bankcard.getRealName());
        return R.ok();
    }


    @AgentLogin
    @GetMapping("fundSubAccountList")
    @ApiOperation(value = "子账号列表", notes = "子账号列表")
    public R fundSubAccountList(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        return R.ok().putPage(safeyInfoService.fundSubAccountList(account));
    }

    @AgentLogin
    @GetMapping("fundSubAccountMenu")
    @ApiOperation(value = "子账号菜单查询", notes = "子账号菜单查询")
    public R fundSubAccountMenu(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        return R.ok().put(safeyInfoService.fundSubAccountMenu());
    }

    @AgentLogin
    @PostMapping("addSubAccount")
    @ApiOperation(value = "子账号保存", notes = "子账号保存")
    public R addSubAccount(@RequestBody AgentSubAccount subAccount,
                           HttpServletRequest request) {
        Assert.isBlank(subAccount.getAgyAccount(), "子账号名称不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        subAccount.setModifyUser(account.getAgyAccount());
        subAccount.setAgentId(account.getId());
        subAccount.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        safeyInfoService.addSubAccount(subAccount);
        return R.ok();
    }

    @AgentLogin
    @PostMapping("saveCryptoCurrencies")
    @ApiOperation(value = "新增代理加密货币钱包", notes = "新增代理加密货币钱包")
    public R saveCryptoCurrencies(@RequestBody AgentCryptoCurrencies mbrCryptoCurrencies, HttpServletRequest request) {
        Assert.isBlank(mbrCryptoCurrencies.getSecurePwd(), "安全密码不能为空");
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        if (account.getAttributes() == Constants.EVNumber.four) {
            throw new R200Exception("无此权限");
        }
        String csPassword = new Sha256Hash(mbrCryptoCurrencies.getSecurePwd(), account.getSalt()).toHex();
        if (!csPassword.equals(account.getSecurePwd())) {
            throw new R200Exception("支付密码错误");
        }
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
        mbrCryptoCurrencies.setAccountId(account.getId());
        return cryptoCurrenciesService.saveCryptoCurrencies(mbrCryptoCurrencies);
    }
    
    @AgentLogin
    @PostMapping("/deleteCryptoCurrencies")
    @ApiOperation(value = "删除代理加密货币钱包", notes = "删除代理加密货币钱包")
    public R deleteCryptoCurrencies(@RequestBody AgentCryptoCurrencies mbrCryptoCurrencies, HttpServletRequest request) {
    	AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
		Assert.isNull(mbrCryptoCurrencies.getId(), "id不能为空!");
		String csPassword = new Sha256Hash(mbrCryptoCurrencies.getSecurePwd(), account.getSalt()).toHex();
        if (!csPassword.equals(account.getSecurePwd())) {
            throw new R200Exception("支付密码错误");
        }
    	return cryptoCurrenciesService.unbindWalletList(account.getId(), mbrCryptoCurrencies.getId());
    }

    @AgentLogin
    @GetMapping("/cryptoCurrenciesList")
    @ApiOperation(value = "会员查询加密货币钱包列表", notes = "会员查询加密货币钱包列表")
    public R cryptoCurrenciesList(HttpServletRequest request) {
        AgentAccount account = (AgentAccount) request.getAttribute(Constants.AGENT_COOUNT_ID);
        return R.ok().put("cryptoCurrencies", cryptoCurrenciesService.listCondCryptoCurrencies(account.getId()));
    }

    @AgentLogin
    @GetMapping("/getCrLogo")
    @ApiOperation(value = "获取数字货币钱包/平台logo", notes = "获取数字货币钱包/平台logo")
    public R getCrLogo(HttpServletRequest request) {
        return R.ok().put(currenciesService.getCrLogo());
    }
}
