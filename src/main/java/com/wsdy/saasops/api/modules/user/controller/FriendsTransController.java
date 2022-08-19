package com.wsdy.saasops.api.modules.user.controller;

import com.wsdy.saasops.api.annotation.Login;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.user.service.ApiUserService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.HttpsRequestUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.dto.MbrFriendTransDetailDto;
import com.wsdy.saasops.modules.member.dto.MbrFriendTransDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrFriendTransDetail;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrFriendTransDetailService;
import com.wsdy.saasops.modules.system.systemsetting.dto.PaySet;
import com.wsdy.saasops.modules.system.systemsetting.dto.RegisterSet;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/friendTrans")
@Api(value = "api", tags = "好友转账")
public class FriendsTransController {

    @Autowired
    private MbrFriendTransDetailService mbrFreindTransDetailService;
    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private MbrAccountService mbrAccountService;

    @GetMapping("/checkReceiptAccount")
    @ApiOperation(value="检查输入账号是否存在",notes="检查输入账号是否存在")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R checkReceiptAccount(@RequestParam("loginName") String loginName, HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        MbrAccount account = mbrAccountService.getAccountInfo(loginName);
        if (account == null) {
            return R.error("输入的账号不存在");
        }
        MbrAccount resultAccount = new MbrAccount();
        resultAccount.setId(account.getId());
        resultAccount.setLoginName(account.getLoginName());
        return R.ok().put("receiptAccount", resultAccount);
    }

    @GetMapping("/checkFriendTrans")
    @ApiOperation(value="好友转账检查",notes="好友转账检查")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R checkFriendTrans(HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        MbrFriendTransDto mbrFriendTransDto = new MbrFriendTransDto();
        mbrFriendTransDto.setTransAccountId(userId);
        mbrFriendTransDto.setTransLoginName(loginName);
        mbrFreindTransDetailService.checkFriendsTransInfo(mbrFriendTransDto);
        return R.ok();
    }

    @PostMapping("/saveFriendTrans")
    @ApiOperation(value="好友转账保存",notes="好友转账保存")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R saveFriendTrans(@RequestBody MbrFriendTransDto mbrFriendTransDto, HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        String dev = request.getHeader("dev");
        Byte pcSource = HttpsRequestUtil.getHeaderOfDev(dev);
        String siteCode= CommonUtil.getSiteCode();
        Assert.isNull(mbrFriendTransDto.getReceiptAccountId(), "转账参数不全，无法转账");
        Assert.isNull(mbrFriendTransDto.getReceiptLoginName(), "转账人未找到，无法转账");
        Assert.isNull(mbrFriendTransDto.getTransAmount(), "转账金额不正确，无法转账");
        /*if(StringUtils.isNotEmpty(mbrFriendTransDto.getPassword())) {
            MbrAccount entityName = new MbrAccount();
            entityName.setLoginName(loginName);
            entityName = mbrAccountService.queryObjectCond(entityName);
            if (!entityName.getLoginPwd().equals(new Sha256Hash(mbrFriendTransDto.getPassword(), entityName.getSalt()).toHex())) {
                return R.error("密码不正确，请重新输入！");
            }
        }*/
        if(StringUtils.isNotEmpty(mbrFriendTransDto.getCode())) {
            String key = Constants.PROJECT_NAME+Constants.REDIS_SPACE_SPACING+RedisConstants.FRIENDS_TRANS_KEY+Constants.REDIS_SPACE_SPACING+siteCode+loginName;
            Object obj = redisService.getRedisValus(key);
            if (obj == null || !mbrFriendTransDto.getCode().equals(obj.toString())) {
                return R.error("短信验证码有误！");
            }
        }
        mbrFriendTransDto.setTransAccountId(userId);
        mbrFriendTransDto.setTransLoginName(loginName);
        if (mbrFriendTransDto.getTransLoginName().equals(mbrFriendTransDto.getReceiptLoginName())) {
            return R.error("转账错误，无法给自身转账！");
        }
        String redisKey = RedisConstants.FRIENDS_TRANS_ADD_KEY +siteCode+ userId;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(redisKey, userId, 4, TimeUnit.MINUTES);
        String key = Constants.PROJECT_NAME+Constants.REDIS_SPACE_SPACING+RedisConstants.FRIENDS_TRANS_KEY+Constants.REDIS_SPACE_SPACING+siteCode+loginName;
        if (Boolean.TRUE.equals(isExpired)) {
            try {
                mbrFreindTransDetailService.saveFriendsTransInfo(mbrFriendTransDto,siteCode,pcSource);
            } finally {
                redisService.del(redisKey);
                redisService.del(key);  // 删除验证码
            }
            return R.ok();
        }else{
            return R.error("转账失败，请稍后重试！");
        }


    }

    @GetMapping("/findFrendisList")
    @ApiOperation(value="获取好友列表",notes="获取好友列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R findFirendsList( @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize,HttpServletRequest request){
        Integer userId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        return R.ok().put("page",mbrFreindTransDetailService.findMbrFriendsList(userId,pageNo,pageSize));
    }

    @PostMapping("/sendMobileCode")
    @ApiOperation(value="好友转账短信验证码",notes="好友转账短信验证码")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R sendMobileCode(@RequestBody MbrFriendTransDto mbrFriendTransDto, HttpServletRequest request){
        String language = request.getHeader("language");
        Assert.isNull(mbrFriendTransDto, "未设置手机号码");
        Assert.isBlank(mbrFriendTransDto.getMobile(), "未设置手机号码");
        Assert.isPhone(mbrFriendTransDto.getMobile(), "手机号码格式错误!");
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        String siteCode= CommonUtil.getSiteCode();

        String code = null;
        String key = Constants.PROJECT_NAME+Constants.REDIS_SPACE_SPACING+RedisConstants.FRIENDS_TRANS_KEY+Constants.REDIS_SPACE_SPACING+siteCode+loginName;
        if(Objects.nonNull(redisService.getRedisValus(key))){   // 是否存在旧的验证码
            code = redisService.getRedisValus(key).toString();
        }
        code = apiUserService.sendVfySmsOneCode(mbrFriendTransDto.getMobile(), code, null, Constants.EVNumber.six, language);
        if(StringUtils.isNotEmpty(code)){
            redisService.setRedisExpiredTime(key,code,3, TimeUnit.MINUTES); // 旧的验证码存在3分钟
            return R.ok();
        }else{
            return R.error("验证码发送失败！");
        }

    }

    @GetMapping("/transList")
    @ApiOperation(value="好友转账列表", notes="好友转账列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R list(MbrFriendTransDetailDto mbrFreindTransDetail, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, HttpServletRequest request) {
        String loginName = (String) request.getAttribute(ApiConstants.USER_NAME);
        mbrFreindTransDetail.setLoginName(loginName);
        R r = R.ok();
        r.put("page", mbrFreindTransDetailService.queryListPageForUser(mbrFreindTransDetail,pageNo,pageSize));
        MbrFriendTransDetail todayCount = mbrFreindTransDetailService.queryTodayCount(mbrFreindTransDetail);
        r.put("todayTransInAmount", todayCount.getTransInAmount());
        r.put("todayTransOutAmount", todayCount.getTransOutAmount());
        return r;
    }

    @GetMapping("/decideUsePwOrSmc")
    @ApiOperation(value="判断使用密码还是短信验证", notes="判断使用密码还是短信验证")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")
            , @ApiImplicitParam(name = "SToken", value = "SToken", required = true, dataType = "String", paramType = "header")})
    @Login
    public R decidePwOrSmCode(HttpServletRequest request){
        Integer accountId = (Integer) request.getAttribute(ApiConstants.USER_ID);
        RegisterSet  registerSet =  sysSettingService.queryRegisterSet();
        Map<String,Object> resultMap = new HashMap<>(8);
        if(registerSet !=null && registerSet.getMobileCaptchareg() != null && registerSet.getMobileCaptchareg()!=0){
            resultMap.put("0","使用短信验证码校验");
        }else{
            resultMap.put("1","不用使用短信验证码校验");
        }
        PaySet paySet  = sysSettingService.queryFriendTransSet();
        if(paySet !=null && paySet.getFriendsTransAntomatic() !=null && paySet.getFriendsTransAntomatic() ==1){
            resultMap.put("2","好友转账功能开启");
        }else{
            resultMap.put("3","好友转账功能禁用");
        }
        if(paySet !=null ){
            resultMap.put("4",paySet.getFriensTransMaxAmount());
        }
        int count = mbrFreindTransDetailService.findAuditAccountBounsCount(accountId);
        if (count > 0){
            resultMap.put("5","先满足优惠流水，方可转账");
        }
        return R.ok().put("message",resultMap);
    }


}
