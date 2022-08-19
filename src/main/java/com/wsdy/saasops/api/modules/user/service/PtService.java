package com.wsdy.saasops.api.modules.user.service;

import com.wsdy.saasops.api.annotation.CacheDuration;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.constants.ApiConstants.TransferStates;
import com.wsdy.saasops.api.constants.PtConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.user.dto.*;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class PtService {
    @Autowired
    MbrWalletService mbrWalletService;
    @Autowired
    private OkHttpService okHttpService;


    /**
     * 锁定用户
     *
     * @param gmApi
     * @param loginName
     */
    public String lockPlayer(TGmApi gmApi, String loginName) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        userPtDto.setFrozen(PtConstants.mod.frozen);
        setEntity(userPtDto, gmApi);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>余额查询 [会员账号{}提交参数{}返回结果{}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            Map<String, Object> list = JsonUtil.json2map(result);
            if (list.get("result") != null) {
                Map<String, Object> rs = (Map<String, Object>) list.get("result");
                return rs.get("result").toString();
            } else {
                throw new RRException(gmApi.getPrefix() + loginName + "该用户不存在");
            }
        } else {
            throw new RRException("PT会员通信异常!");
        }
    }

    /**
     * 锁定用户
     *
     * @param gmApi
     * @param loginName
     */
    public String unlockPlayer(TGmApi gmApi, String loginName) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        userPtDto.setUnFrozen(PtConstants.mod.unFrozen);
        setEntity(userPtDto, gmApi);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>余额查询 [会员账号{}提交参数{}返回结果{}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            Map<String, Object> list = JsonUtil.json2map(result);
            if (list.get("result") != null) {
                Map<String, Object> rs = (Map<String, Object>) list.get("result");
                return rs.get("result").toString();
            } else {
                throw new RRException(gmApi.getPrefix() + loginName + "该用户不存在");
            }
        } else {
            throw new RRException("PT会员通信异常!");
        }
    }

    /**
     * 获取奖池奖金
     *
     * @return
     */
    @Cacheable(cacheNames = ApiConstants.ACCOUNT_SITE, key = "#siteCode")
    @CacheDuration(duration = 2 * 60 * 60)
    public R getJackPot(String siteCode) {
//        String result = okHttpService.get(ptConfig.getLuckyJackpotUrl());
//        log.debug(" PT—>奖池查询 [提交参数{}返回结果{}]", ptConfig.getLuckyJackpotUrl(), result);
//        PtJackPotResDto ptJackPotResDto = XmlUtil.ptJackPotResDto(result);
//        if (Objects.isNull(ptJackPotResDto.getAmounts())) {
//            Random r = new Random();
//            r.nextInt(200000000);
//            ptJackPotResDto.setAmounts("0.00");
//        }
        return R.ok().put("amount", getRandom());
    }

    /**
     * 余额
     *
     * @param gmApi
     * @param loginName
     */
    @SuppressWarnings("rawtypes")
    public PtUserInfo getBalance(TGmApi gmApi, String loginName) {
        UserPtDto userPtDto = getUserPtDto(gmApi, loginName);
        userPtDto.setMod(PtConstants.mod.info);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>余额查询 [会员账号{}提交参数{}返回结果{}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        PtUserInfo ptUserInfo = new PtUserInfo();
        if (!StringUtils.isEmpty(result)) {
            Map<String, Object> list = JsonUtil.json2map(result);
            if (list.get("result") != null) {
                ptUserInfo.setBALANCE(String.valueOf(((Map) list.get("result")).get("BALANCE")));
                ptUserInfo.setCURRENCY(String.valueOf(((Map) list.get("result")).get("CURRENCY")));
            } else {
                throw new RRException("PT查询会员余额失败!", gmApi.getDepotId());
            }
        } else {
            throw new RRException("PT查询会员通信异常!", gmApi.getDepotId());
        }
        return ptUserInfo;
    }

    /**
     * 存款
     *
     * @param gmApi
     * @param loginName
     * @param amount
     * @param tranid
     * @return
     */
    public Integer deposit(TGmApi gmApi, String loginName, double amount, String tranid) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setAdminname(gmApi.getWebName());
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        setEntity(userPtDto, gmApi);
        userPtDto.setAmount(amount);
        userPtDto.setExternaltranid(tranid);
        userPtDto.setMod(PtConstants.mod.deposit);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>存款 [会员账号{}提交参数 {}返回结果{}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            if (result.toLowerCase().indexOf(PtConstants.DEPOSIT_OK) != -1) {
                return TransferStates.suc;
            }
        }
        return TransferStates.fail;
    }

    /**
     * 取款
     *
     * @param gmApi
     * @param loginName
     * @param amount
     * @param tranid
     * @return
     */
    public Integer withdraw(TGmApi gmApi, String loginName, double amount, String tranid) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setAdminname(gmApi.getWebName());
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        setEntity(userPtDto, gmApi);
        userPtDto.setAmount(amount);
        userPtDto.setExternaltranid(tranid);
        userPtDto.setIsForce(UserPtDto.IsForce.is);
        userPtDto.setMod(PtConstants.mod.withdraw);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>取款 [会员账号{}提交参数{}返回结果 {}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        // PtUserInfo ptUserInfo = new PtUserInfo();
        if (!StringUtils.isEmpty(result)) {
            if (result.toLowerCase().indexOf(PtConstants.WITHDRAW_OK) != -1) {
                return TransferStates.suc;
            }
        }
        return TransferStates.fail;
    }



    /**
     * 登出
     *
     * @param gmApi
     * @param loginName
     * @return
     */

    public boolean logOut(TGmApi gmApi, String loginName) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setLoginname(gmApi.getPrefix() + loginName);
        userPtDto.setMod(PtConstants.mod.logout);
        setEntity(userPtDto, gmApi);
        String result = okHttpService.send(gmApi.getPcUrl() + userPtDto.toString(), userPtDto.getPtEntity());
        log.debug(" PT—>登出 [会员账号{}提交参数 {}返回结果 {}]", loginName, gmApi.getPcUrl() + userPtDto.toString(), result);
        if (!StringUtils.isEmpty(result)) {
            if (result.toLowerCase().indexOf(PtConstants.LOGOUT) != -1) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }


    private UserPtDto getUserPtDto(TGmApi gmApi, String loginName) {
        MbrDepotWallet mbrWallet = new MbrDepotWallet();
        mbrWallet.setLoginName(loginName);
        return getUserPtDto(gmApi, mbrWallet);
    }

    private UserPtDto getUserPtDto(TGmApi gmApi, MbrDepotWallet mbrWallet) {
        UserPtDto userPtDto = new UserPtDto();
        userPtDto.setAdminname(gmApi.getWebName());
        userPtDto.setKioskname(gmApi.getAgyAcc());
        userPtDto.setPassword(mbrWallet.getPwd());
        userPtDto.setLoginname(gmApi.getPrefix() + mbrWallet.getLoginName());
        setEntity(userPtDto, gmApi);
        return userPtDto;
    }

    private void setEntity(UserPtDto userPtDto, TGmApi gmApi) {
        PtEntity ptEntity = new PtEntity();
        ptEntity.setEntityKey(PtConstants.JsonKey.ENTITY_KEY_NAME);
        ptEntity.setEntityContext(gmApi.getSecureCodes().get(PtConstants.JsonKey.ENTITY_KEY_NAME));
        userPtDto.setPtEntity(ptEntity);
    }

    private BigDecimal getRandom(){
        Long i = Math.round(Math.random()*(250000000-180000000))+180000000;
        Random r2 = new Random();
        Integer i2 = (r2.nextInt(100));
        return new BigDecimal(i.toString()+"."+i2.toString()).setScale(2);
    }


}
