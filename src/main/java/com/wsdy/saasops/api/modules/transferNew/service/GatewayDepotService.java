package com.wsdy.saasops.api.modules.transferNew.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.dao.TCpSiteMapper;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.entity.TcpSiteurl;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transferNew.dto.GatewayRequestDto;
import com.wsdy.saasops.api.modules.transferNew.dto.GatewayResponseDto;
import com.wsdy.saasops.api.modules.unity.dto.LoginModel;
import com.wsdy.saasops.api.modules.unity.dto.PlayGameModel;
import com.wsdy.saasops.api.modules.unity.dto.RegisterModel;
import com.wsdy.saasops.api.modules.unity.dto.TransferModel;
import com.wsdy.saasops.api.utils.AuthUtils;
import com.wsdy.saasops.api.utils.GameTypeEnum;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.OkHttpUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.member.dao.MbrDepotWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import com.wsdy.saasops.modules.system.systemsetting.dao.StationSetMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.nonNull;

@Slf4j
@Service
public class GatewayDepotService {

    private static final String CREATE_MEMBER = "/createMember";
    private static final String DEPOSIT = "/deposit";
    private static final String QUERY_BALANCE = "/queryBalance";
    private static final String WITHDRAWAL = "/withdrawal";
    private static final String CHECK_TRANSFER = "/checkTransfer";
    private static final String LOGOUT = "/logout";

    @Autowired
    public TCpSiteMapper tCpSiteMapper;
    @Autowired
    private TGmApiService apiService;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private TransferNewService transferNewService;
    @Autowired
    private MbrDepotWalletMapper mbrDepotWalletMapper;
    @Value("${gateway.url}")
    private String gatewayUrl;
    @Autowired
    private StationSetMapper stationSetMapper;

    public static final List<String> passWordDepots = Lists.newArrayList("MG", "AGIN", "BBIN");

    public GatewayResponseDto createMember(RegisterModel registerModel) {
        String url = gatewayUrl + registerModel.getTGmApi().getDepotCode() + CREATE_MEMBER;
        GatewayRequestDto gatewayDto = new GatewayRequestDto();
        gatewayDto.setUserName(registerModel.getUserName());
        gatewayDto.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        gatewayDto.setSiteCode(registerModel.getSiteCode());
        gatewayDto.setPassword(passWordDepots.contains(registerModel.getTGmApi().getDepotCode()) ? registerModel.getPassword() : null);
        //TODO 获取私钥
        Map<String, String> stringMap = getStringMap(gatewayDto);
        log.info("userName==" + registerModel.getUserName() + "==depotCode==" + registerModel.getTGmApi().getDepotCode() + "==创建用户==stringMap==" + jsonUtil.toJson(gatewayDto));
        String result = OkHttpUtils.postGateWayJson(url, gatewayDto, stringMap);
        log.info("userName==" + registerModel.getUserName() + "==depotCode==" + registerModel.getTGmApi().getDepotCode() + "==创建用户==result==" + result);
        if (StringUtils.isNotEmpty(result)) {
            return jsonUtil.fromJson(result, GatewayResponseDto.class);
        }
        return null;
    }

    public Map<String, String> getStringMap(GatewayRequestDto gatewayDto) {
        //TODO 获取私钥
        TCpSite tCpSite = new TCpSite();
        tCpSite.setSiteCode(gatewayDto.getSiteCode());
        TCpSite tCpSiteBean = tCpSiteMapper.selectOne(tCpSite);
        String data = gatewayDto.getSiteCode() + gatewayDto.getUserName() + gatewayDto.getTimeStamp();
        String auth = null;
        try {
            PrivateKey privateKey = AuthUtils.getPrivateKey(tCpSiteBean.getPrivateKey());
            auth = AuthUtils.sign(data, privateKey);
        } catch (Exception e) {
            log.error("userName==" + gatewayDto.getUserName() + "==getStringMap==error==" + e);
        }
        Map<String, String> stringMap = new HashMap<>(2);
        stringMap.put("Authorization", "Bearer " + auth);
        return stringMap;
    }

    public GatewayResponseDto deposit(TransferModel transferModel) {
        String url = gatewayUrl + transferModel.getTGmApi().getDepotCode() + DEPOSIT;
        GatewayRequestDto gatewayDto = new GatewayRequestDto();
        gatewayDto.setUserName(transferModel.getUserName());
        gatewayDto.setSiteCode(transferModel.getSiteCode());
        gatewayDto.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        gatewayDto.setOrderNo(transferModel.getOrderNo());
        gatewayDto.setPassword(passWordDepots.contains(transferModel.getTGmApi().getDepotCode()) ? transferModel.getPassword() : null);
        gatewayDto.setAmount(transferModel.getAmount().toString());
        //TODO 获取私钥
        Map<String, String> stringMap = getStringMap(gatewayDto);
        log.info("userName==" + transferModel.getUserName() + "==depotCode==" + transferModel.getTGmApi().getDepotCode() + "==转账转入平台==stringMap==" + jsonUtil.toJson(gatewayDto));
        String result = OkHttpUtils.postGateWayJson(url, gatewayDto, stringMap);
        log.info("userName==" + transferModel.getUserName() + "==depotCode==" + transferModel.getTGmApi().getDepotCode() + "==转账转入平台==result==" + result);
        if (StringUtils.isNotEmpty(result)) {
            return jsonUtil.fromJson(result, GatewayResponseDto.class);
        }
        return null;
    }

    public GatewayResponseDto queryBalance(LoginModel loginModel) {
        if (TransferNewService.noTransferDepotCodes.contains(loginModel.getTGmApi().getDepotCode())) {
            return null;
        }
        String url = gatewayUrl + loginModel.getTGmApi().getDepotCode() + QUERY_BALANCE;
        GatewayRequestDto gatewayDto = new GatewayRequestDto();
        gatewayDto.setUserName(loginModel.getUserName());
        gatewayDto.setSiteCode(loginModel.getSiteCode());
        gatewayDto.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        gatewayDto.setPassword(passWordDepots.contains(loginModel.getTGmApi().getDepotCode()) ? loginModel.getPassword() : null);
        //TODO 获取私钥
        Map<String, String> stringMap = getStringMap(gatewayDto);
        log.info("userName==" + loginModel.getUserName() + "==depotCode==" + loginModel.getTGmApi().getDepotCode() + "==查询余额==stringMap==" + jsonUtil.toJson(gatewayDto));
        String result = OkHttpUtils.postGateWayJson(url, gatewayDto, stringMap);
        log.info("userName==" + loginModel.getUserName() + "==depotCode==" + loginModel.getTGmApi().getDepotCode() + "==查询余额==result==" + result);
        if (StringUtils.isNotEmpty(result)) {
            return jsonUtil.fromJson(result, GatewayResponseDto.class);
        }
        return null;
    }

    public GatewayResponseDto withdrawal(TransferModel transferModel) {
        String url = gatewayUrl + transferModel.getTGmApi().getDepotCode() + WITHDRAWAL;
        GatewayRequestDto gatewayDto = new GatewayRequestDto();
        gatewayDto.setUserName(transferModel.getUserName());
        gatewayDto.setSiteCode(transferModel.getSiteCode());
        gatewayDto.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        gatewayDto.setOrderNo(transferModel.getOrderNo());
        gatewayDto.setPassword(passWordDepots.contains(transferModel.getTGmApi().getDepotCode()) ? transferModel.getPassword() : null);
        gatewayDto.setAmount(transferModel.getAmount().toString());
        //TODO 获取私钥
        Map<String, String> stringMap = getStringMap(gatewayDto);
        log.info("userName==" + transferModel.getUserName() + "==depotCode==" + transferModel.getTGmApi().getDepotCode() + "==转账转出平台==stringMap==" + jsonUtil.toJson(gatewayDto));
        String result = OkHttpUtils.postGateWayJson(url, gatewayDto, stringMap);
        log.info("userName==" + transferModel.getUserName() + "==depotCode==" + transferModel.getTGmApi().getDepotCode() + "==转账转出平台==result==" + result);
        if (StringUtils.isNotEmpty(result)) {
            return jsonUtil.fromJson(result, GatewayResponseDto.class);
        }
        return null;
    }

    public GatewayResponseDto LoginOutGateway(TransferModel transferModel) {
        String url = gatewayUrl + transferModel.getTGmApi().getDepotCode() + LOGOUT;
        GatewayRequestDto gatewayDto = new GatewayRequestDto();
        gatewayDto.setUserName(transferModel.getUserName());
        gatewayDto.setSiteCode(transferModel.getSiteCode());
        gatewayDto.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        gatewayDto.setPassword(passWordDepots.contains(transferModel.getTGmApi().getDepotCode()) ? transferModel.getPassword() : null);
        //TODO 获取私钥
        Map<String, String> stringMap = getStringMap(gatewayDto);
        log.info("userName==" + transferModel.getUserName() + "==depotCode==" + transferModel.getTGmApi().getDepotCode() + "==登出平台==stringMap==" + jsonUtil.toJson(gatewayDto));
        String result = OkHttpUtils.postGateWayJson(url, gatewayDto, stringMap);
        log.info("userName==" + transferModel.getUserName() + "==depotCode==" + transferModel.getTGmApi().getDepotCode() + "==登出平台==result==" + result);
        if (StringUtils.isNotEmpty(result)) {
            return jsonUtil.fromJson(result, GatewayResponseDto.class);
        }
        return null;
    }


    public String login(LoginModel loginModel) {
        TGmApi api = apiService.queryApiObject(loginModel.getDepotId(), loginModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/login");

        Map<String, String> map = new HashMap(8);
        map.put("siteCode", loginModel.getSiteCode());
        map.put("userName", loginModel.getUserName());
        map.put("password", passWordDepots.contains(loginModel.getTGmApi().getDepotCode()) ? loginModel.getPassword() : null);
        map.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        String resultString;
        try {
            Map<String, String> stringMap = getStringMapSign(map);
            resultString = OkHttpUtils.postGateWayJson(url.toString(), map, stringMap);
            log.error("会员登录返回值========" + resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }

    public Map<String, String> getStringMapSign(Map<String, String> map) {
        //TODO 获取私钥
        TCpSite tCpSite = new TCpSite();
        tCpSite.setSiteCode(map.get("siteCode"));
        TCpSite tCpSiteBean = tCpSiteMapper.selectOne(tCpSite);
        String data = map.get("siteCode") + map.get("userName") + map.get("timeStamp");
        String auth = null;
        try {
            PrivateKey privateKey = AuthUtils.getPrivateKey(tCpSiteBean.getPrivateKey());
            auth = AuthUtils.sign(data, privateKey);
        } catch (Exception e) {
            log.error("userName==" + map.get("userName") + "==getStringMapSign==error==" + e);
        }
        Map<String, String> stringMap = new HashMap<>(2);
        stringMap.put("Authorization", "Bearer " + auth);
        return stringMap;
    }

    public String logout(LoginModel loginModel) {
        TGmApi api = apiService.queryApiObject(loginModel.getDepotId(), loginModel.getSiteCode());
        Assert.isNull(api, "api线路不存在");
        StringBuffer url = new StringBuffer();
        url.append(api.getApiUrl());
        url.append(api.getDepotCode());
        url.append("/logout");

        Map<String, String> map = new HashMap(4);
        map.put("siteCode", loginModel.getSiteCode());
        map.put("userName", loginModel.getUserName());
        map.put("password", passWordDepots.contains(loginModel.getTGmApi().getDepotCode()) ? loginModel.getPassword() : null);
        map.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        String resultString;
        try {
            Map<String, String> stringMap = getStringMapSign(map);
            resultString = OkHttpUtils.postGateWayJson(url.toString(), map, stringMap);
            log.error("会员登出返回值========" + resultString);
        } catch (Exception e) {
            log.error("调用接口异常");
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }


    public GatewayResponseDto checkTransfer(TransferModel transferModel) {
        String url = gatewayUrl + transferModel.getTGmApi().getDepotCode() + CHECK_TRANSFER;
        GatewayRequestDto gatewayDto = new GatewayRequestDto();
        gatewayDto.setSiteCode(transferModel.getSiteCode());
        gatewayDto.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        gatewayDto.setOrderNo(transferModel.getOrderNo());
        gatewayDto.setPassword(passWordDepots.contains(transferModel.getTGmApi().getDepotCode()) ? transferModel.getPassword() : null);
        gatewayDto.setAmount(transferModel.getAmount().toString());
        gatewayDto.setUserName(transferModel.getUserName());
        Map<String, String> stringMap = getStringMap(gatewayDto);
        log.info("userName==" + transferModel.getUserName() + "==depotCode==" + transferModel.getTGmApi().getDepotCode() + "==确认转账查询==stringMap==" + jsonUtil.toJson(gatewayDto));
        String result = OkHttpUtils.postGateWayJson(url, gatewayDto, stringMap);
        log.info("userName==" + transferModel.getUserName() + "==depotCode==" + transferModel.getTGmApi().getDepotCode() + "==确认转账查询==result==" + result);
        if (StringUtils.isNotEmpty(result)) {
            return jsonUtil.fromJson(result, GatewayResponseDto.class);
        }
        return null;
    }


    public String playGame(PlayGameModel playGameModel) {
        StringBuffer url = new StringBuffer();
        url.append(gatewayUrl);
        url.append(playGameModel.getTGmApi().getDepotCode());
        url.append("/playGame");

        Map<String, String> map = new HashMap(8);
        map.put("siteCode", playGameModel.getSiteCode());
        map.put("userName", playGameModel.getUserName());
        map.put("gameType", playGameModel.getGameType());
        map.put("gameId", playGameModel.getGameId());
        map.put("gameCode", playGameModel.getGamecode());
        map.put("origin", playGameModel.getOrigin());
        map.put("password", passWordDepots.contains(playGameModel.getTGmApi().getDepotCode()) ? playGameModel.getPassword() : null);
        map.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        // 增加参数domain
        // 当domain为空的时候，查询该站点的推广域名
        if (StringUtils.isEmpty(playGameModel.getDomain())) {
            TcpSiteurl siteurl = stationSetMapper.getPromotionUrl();
            if (Objects.nonNull(siteurl)) {
                map.put("domain", "https://" + siteurl.getSiteUrl());
            }
        } else {
            map.put("domain", "https://" + playGameModel.getDomain());
        }
        String resultString;
        try {
            Map<String, String> stringMap = getStringMapSign(map);
            log.info("userName==" + playGameModel.getUserName() + "==depotCode==" + playGameModel.getTGmApi().getDepotCode() + "==游戏请求-游戏==stringMap==" + jsonUtil.toJson(map));
            resultString = OkHttpUtils.postGateWayJson(url.toString(), map, stringMap);
            log.info("userName==" + playGameModel.getUserName() + "==depotCode==" + playGameModel.getTGmApi().getDepotCode() + "==游戏请求-游戏==result==" + resultString);
            // EG错误处理
            if (ApiConstants.DepotCode.EG.equals(playGameModel.getDepotCode())) {
                Map resultMaps = (Map) JSON.parse(resultString);
                if (Objects.isNull(resultMaps)) {
                    log.error("调用EG跳转接口返回null.");
                    throw new R200Exception("游戏维护中，请稍后再试！");
                }
                if ("600".equals(resultMaps.get("msgCode"))) {
                    Map messageMaps = (Map) JSON.parse(resultMaps.get("message").toString());
                    if (Objects.nonNull(messageMaps.get("status")) && messageMaps.get("status").equals(4401)) {
                        log.error("调用EG跳转接口返回：游戏维护.");
                        throw new R200Exception("游戏维护中，请稍后再试！");
                    }
                }
            }
        } catch (R200Exception e) {
            throw e;
        } catch (Exception e) {
            log.info("userName==" + playGameModel.getUserName() + "==depotCode==" + playGameModel.getTGmApi().getDepotCode() + "==playGame调用接口异常==" + e);
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }


    public String openHall(PlayGameModel playGameModel) {
        StringBuffer url = new StringBuffer();
        url.append(gatewayUrl);
        url.append(playGameModel.getTGmApi().getDepotCode());
        url.append("/openHall");

        Map<String, String> map = new HashMap(8);
        map.put("origin", playGameModel.getOrigin());
        map.put("siteCode", playGameModel.getSiteCode());
        map.put("userName", playGameModel.getUserName());
        map.put("gameType", playGameModel.getGameType());
        map.put("password", passWordDepots.contains(playGameModel.getTGmApi().getDepotCode()) ? playGameModel.getPassword() : null);
        map.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        // 增加参数domain
        // 当domain为空的时候，查询该站点的推广域名
        if (StringUtils.isEmpty(playGameModel.getDomain())) {
            TcpSiteurl siteurl = stationSetMapper.getPromotionUrl();
            if (Objects.nonNull(siteurl)) {
                map.put("domain", "https://" + siteurl.getSiteUrl());
            }
        } else {
            map.put("domain", "https://" + playGameModel.getDomain());
        }
        String resultString;
        try {
            Map<String, String> stringMap = getStringMapSign(map);
            log.info("userName==" + playGameModel.getUserName() + "==depotCode==" + playGameModel.getTGmApi().getDepotCode() + "==游戏请求-大厅==stringMap==" + jsonUtil.toJson(map));
            resultString = OkHttpUtils.postGateWayJson(url.toString(), map, stringMap);
            log.info("userName==" + playGameModel.getUserName() + "==depotCode==" + playGameModel.getTGmApi().getDepotCode() + "==游戏请求-大厅==result==" + resultString);
            // EG错误处理
            if (ApiConstants.DepotCode.EG.equals(playGameModel.getDepotCode())) {
                Map resultMaps = (Map) JSON.parse(resultString);
                if (Objects.isNull(resultMaps)) {
                    log.error("调用EG跳转接口返回null.");
                    throw new R200Exception("游戏维护中，请稍后再试！");
                }
                if ("600".equals(resultMaps.get("msgCode"))) {
                    Map messageMaps = (Map) JSON.parse(resultMaps.get("message").toString());
                    if (Objects.nonNull(messageMaps.get("status")) && messageMaps.get("status").equals(4401)) {
                        log.error("调用EG跳转接口返回：游戏维护.");
                        throw new R200Exception("游戏维护中，请稍后再试！");
                    }
                }
            }
        } catch (R200Exception e) {
            log.error("openHall:" + e);
            throw e;
        } catch (Exception e) {
            log.info("userName==" + playGameModel.getUserName() + "==depotCode==" + playGameModel.getTGmApi().getDepotCode() + "==openHall调用接口异常==" + e);
            throw new R200Exception("调用接口异常");
        }
        return resultString;
    }

    /**
     * 跳转游戏（调用）
     *
     * @param gmApi
     * @param tGmGame
     * @param loginName
     * @param dev
     * @return
     */
    public String generateUrl(TGmApi gmApi, TGmGame tGmGame, String loginName, String dev, Integer accountId, String ip, String domain) {
        transferNewService.createMember(accountId, tGmGame.getDepotId(), loginName, gmApi);
        PlayGameModel playGameModel = new PlayGameModel();
        playGameModel.setOrigin(dev);
        playGameModel.setUserName(loginName);
        playGameModel.setDepotId(gmApi.getDepotId());
        playGameModel.setDepotCode(gmApi.getDepotCode());   // 增加平台代码
        playGameModel.setSiteCode(gmApi.getSiteCode());
        playGameModel.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        playGameModel.setTGmApi(gmApi);
        playGameModel.setIp(ip);
        playGameModel.setDomain(domain);
        MbrDepotWallet mbrDepotWallet = new MbrDepotWallet();
        mbrDepotWallet.setLoginName(loginName);
        mbrDepotWallet.setDepotId(gmApi.getDepotId());
        MbrDepotWallet depotWallet = mbrDepotWalletMapper.selectOne(mbrDepotWallet);
        if (nonNull(depotWallet)) {
            playGameModel.setPassword(depotWallet.getPwd());
        }
        if ("PC".equals(dev)) {
            playGameModel.setGameId(tGmGame.getGameCode());
        } else {
            playGameModel.setGameId(tGmGame.getMbGameCode());
        }
        switch (tGmGame.getCatId()) {
            case Constants.EVNumber.one:
                if (ApiConstants.DepotCode.IM.equals(playGameModel.getDepotCode())) {   // IM电竞处理为送esports
                    playGameModel.setGameType(GameTypeEnum.ENUM_NINE_2.getValue());
                } else {
                    playGameModel.setGameType(GameTypeEnum.ENUM_ONES.getValue());
                }
                break;
            case Constants.EVNumber.three:
                playGameModel.setGameType(GameTypeEnum.ENUM_THREE.getValue());
                break;
            case Constants.EVNumber.five:
                playGameModel.setGameType(GameTypeEnum.ENUM_FIVE.getValue());
                break;
            case Constants.EVNumber.eight:
                playGameModel.setGameType(GameTypeEnum.ENUM_EIGHT.getValue());
                break;
            case Constants.EVNumber.twelve:
                playGameModel.setGameType(GameTypeEnum.ENUM_TWELVE.getValue());
                break;
            case Constants.EVNumber.six:
                playGameModel.setGameType(GameTypeEnum.ENUM_SIX.getValue());
                break;
            case Constants.EVNumber.nine:
                playGameModel.setGameType(GameTypeEnum.ENUM_NINE_2.getValue());
                break;
            default:
        }
        if (tGmGame.getTopLink() == Constants.EVNumber.zero) {
            return playGame(playGameModel);
        } else {
            return openHall(playGameModel);
        }
    }

    // 试玩服务
    public String tryPlayGame(PlayGameModel playGameModel) {
        StringBuffer url = new StringBuffer();
        url.append(gatewayUrl);
        url.append(playGameModel.getDepotCode());
        url.append("/tryPlayGame");

        Map<String, String> map = new HashMap(16);
        map.put("gameId", playGameModel.getGameId());
        map.put("gameType", playGameModel.getGameType());
        map.put("gameCode", playGameModel.getGamecode());
        map.put("origin", playGameModel.getOrigin());
        map.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        map.put("siteCode", playGameModel.getSiteCode());
        map.put("userName", playGameModel.getUserName());
        map.put("password", playGameModel.getPassword());
        // 当domain为空的时候，查询该站点的推广域名
        if (StringUtils.isEmpty(playGameModel.getDomain())) {
            TcpSiteurl siteurl = stationSetMapper.getPromotionUrl();
            if (Objects.nonNull(siteurl)) {
                map.put("domain", "https://" + siteurl.getSiteUrl());
            }
        } else {
            map.put("domain", "https://" + playGameModel.getDomain());
        }
        String resultString;
        try {
            Map<String, String> stringMap = getStringMapSign(map);
            log.info("userName==" + playGameModel.getUserName() + "==depotCode==" + playGameModel.getDepotCode() + "==试玩==stringMap==" + jsonUtil.toJson(map));
            resultString = OkHttpUtils.postGateWayJson(url.toString(), map, stringMap);
            log.info("userName==" + playGameModel.getUserName() + "==depotCode==" + playGameModel.getDepotCode() + "==试玩==result==" + resultString);
        } catch (Exception e) {
            log.info("userName==" + playGameModel.getUserName() + "==depotCode==" + playGameModel.getDepotCode() + "==试玩==tryPlayGame调用接口异");
            return null;
        }
        return resultString;
    }
}
