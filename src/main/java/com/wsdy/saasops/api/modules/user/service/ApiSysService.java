package com.wsdy.saasops.api.modules.user.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApiprefix;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.api.modules.transfer.dto.DepotFailDtosDto;
import com.wsdy.saasops.api.modules.transferNew.service.DepotService;
import com.wsdy.saasops.api.modules.transferNew.service.GatewayDepotService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.modules.member.dao.MbrBillManageMapper;
import com.wsdy.saasops.modules.member.dao.MbrDepotWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.dao.TGameLogoMapper;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.TGameLogo;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import com.wsdy.saasops.modules.operate.entity.TRecentlyGame;
import com.wsdy.saasops.modules.operate.mapper.OperateMapper;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import com.wsdy.saasops.modules.operate.service.TGmGameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ApiSysService {
    @Autowired
    private TGmGameService gmGameService;
    @Autowired
    MbrAccountService userService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private DepotService depotService;
    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    CommonService commonService;
    @Autowired
    private OperateMapper operateMapper;
    @Autowired
    private TGmDepotMapper tGmDepotMapper;
    @Autowired
    private TGameLogoMapper tGameLogoMapper;
    @Autowired
    private MbrBillManageMapper mbrBillManageMapper;
    @Autowired
    private GatewayDepotService gatewayDepotService;
    @Autowired
    private MbrDepotWalletMapper depotWalletMapper;
    @Autowired
    private TGmDepotService tGmDepotService;
    /**
     *  游戏跳转
     * @param cpSite
     * @param requestDto    参数dto
     * @param domain        域名，用户游戏平台超时回调等
     * @return
     */
    public R transit(TCpSite cpSite, BillRequestDto requestDto, String domain) {
        // 一. 校验线路和游戏
        MbrAccount user = userService.queryObject(requestDto.getAccountId(), cpSite.getSiteCode());
        if (user.getAvailable() == MbrAccount.Status.LOCKED) {
            throw new R200Exception("账户余额已冻结，不支持此操作，请联系管理员");
        }
        // 校验1.根据游戏id，获取游戏TGmGame对象(获取depotId)：缓存
        TGmGame tGmGame = gmGameService.queryObjectOne(requestDto.getGameId(), cpSite.getSiteCode());
        if (Objects.isNull(tGmGame)) {
            throw new R200Exception("平台无此游戏!");
        }
        // 校验2.根据平台depotid和站点，获取线路：缓存
        TGmApi gmApi = gmApiService.queryApiObject(tGmGame.getDepotId(), cpSite.getSiteCode());
        if (Objects.isNull(gmApi)) {
            throw new R200Exception("对不起，暂无此游戏线路");
        }

        // 添加用户最近游戏信息
        if ("H5".equals(requestDto.getDev())) {
            insertRecentlyGame(requestDto.getAccountId(), requestDto.getLoginName(), tGmGame.getDepotId(), tGmGame, requestDto.getGameId());
        }

        requestDto.setDepotId(tGmGame.getDepotId());
        requestDto.setDepotCode(gmApi.getDepotCode());  // 增加depotCode,便于后续进行处理

        //判断是否开启免转, 默认免转
        if(user.getFreeWalletSwitch() == null || user.getFreeWalletSwitch() == Constants.EVNumber.one){
            // 二. 回收上一次转出平台的余额
            // 1. 获取会员转出记录：转入平台
            MbrBillManage mbrBillManage = new MbrBillManage();
            mbrBillManage.setAccountId(requestDto.getAccountId());
            mbrBillManage.setOpType(0);
            List<MbrBillManage> mbrBillManages = mbrBillManageMapper.select(mbrBillManage);

            if (Collections3.isNotEmpty(mbrBillManages)) {  // 存在转出记录
                // 2. 获取最近的转账记录
                MbrBillManage mbrBillManage1 = mbrBillManages.get(mbrBillManages.size() - 1);
                // 3. 最近一次跳转的平台与此次跳转的平台不是同一平台，则回收余额
                if (!mbrBillManage1.getDepotId().equals(tGmGame.getDepotId())) {
                    // 4. 判断上一次转账的平台是否维护，维护则不进行余额回收：apigateway已经修改为维护状态也可以调用接口，此处需要v2处理
                    // 查询该站点该平台开启的线路
                    List<TGmApiprefix> prefixList = tGmDepotService.getTGmApiprefix(mbrBillManage1.getDepotId(),cpSite.getSiteCode());
                    // 线路开启状态，进行回收
                    if(Objects.nonNull(prefixList) && prefixList.size()>0 ){
                        // 回收的平台的钱包对象
                        MbrDepotWallet wallet = new MbrDepotWallet();
                        wallet.setAccountId(requestDto.getAccountId());
                        wallet.setDepotId(mbrBillManage1.getDepotId());
                        List<MbrDepotWallet> depotWallets = depotWalletMapper.select(wallet);

                        DepotFailDtosDto depotFailDtosDto = new DepotFailDtosDto();
                        depotFailDtosDto.setIp(requestDto.getIp());
                        depotFailDtosDto.setDev(requestDto.getDev());
                        depotFailDtosDto.setDepotWallets(depotWallets);             // 回收的平台的钱包对象
                        depotFailDtosDto.setUserId(requestDto.getAccountId());
                        depotFailDtosDto.setLoginName(requestDto.getLoginName());
                        depotFailDtosDto.setTransferSource(requestDto.getTransferSource());
                        depotFailDtosDto.setSiteCode(CommonUtil.getSiteCode());

                        // 5. 平台余额回收
                        mbrWalletService.getDepotFailDtos(depotFailDtosDto);
                    }
                }
            }
            // 三. 中心钱包转入跳转平台
            depotService.accountTransferOut(requestDto, gmApi.getSiteCode());
        }

        // 四. 获取游戏连接
        String url = getAllDepotUrl(requestDto.getAccountId(), requestDto.getLoginName(), requestDto, tGmGame, gmApi, domain);
        return R.ok(url);
    }

    private void insertRecentlyGame(Integer userId, String loginName, Integer depotId, TGmGame tGmGame, Integer gameId) {
        List<TRecentlyGame> recentlyGames = operateMapper.queryTRecentlyGameList(gameId.toString(), userId);
        TRecentlyGame info = new TRecentlyGame();
        info.setUserId(userId);
        info.setUserName(loginName);
        info.setDepotId(depotId);
        if (StringUtil.isEmpty(tGmGame.getDepotName())) {
            info.setDepotName(tGmDepotMapper.selectByPrimaryKey(depotId).getDepotName());
        } else {
            info.setDepotName(tGmGame.getDepotName());
        }
        info.setGameId(gameId + "");
        info.setGameName(tGmGame.getGameName());
        info.setGameNameEn(tGmGame.getGameNameEn());
        info.setGameCode(tGmGame.getGameCode());
        if (tGmGame.getCatId() != Constants.EVNumber.five) {
            TGameLogo tGameLogo = new TGameLogo();
            tGameLogo.setDepotId(depotId);
            tGameLogo.setCatId(tGmGame.getCatId());
            info.setGameLogo(tGameLogoMapper.selectOne(tGameLogo).getLogoMb());
        } else {
            info.setGameLogo(tGmGame.getLogo());
        }
        info.setEntryTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (Collections3.isEmpty(recentlyGames)) {
            operateMapper.insertRecentlyRecord(info);
        } else {
            operateMapper.updateRecentlyRecord(info.getEntryTime(), gameId, userId);
        }
    }

    private String getAllDepotUrl(Integer userId, String loginName, BillRequestDto requestDto, TGmGame tGmGame, TGmApi gmApi, String domain) {
        String url = gatewayDepotService.generateUrl(gmApi, tGmGame, loginName, requestDto.getDev(), userId, requestDto.getIp(), domain);
        if (StringUtil.isNotEmpty(url)){
            Map resultMaps = (Map) JSON.parse(url);
            if (Objects.nonNull(resultMaps.get("message"))){
                url = resultMaps.get("message").toString();
            }

        }

        return url;
    }

}
