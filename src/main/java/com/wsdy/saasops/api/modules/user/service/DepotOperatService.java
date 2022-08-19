package com.wsdy.saasops.api.modules.user.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transferNew.dto.GatewayResponseDto;
import com.wsdy.saasops.api.modules.transferNew.service.DepotService;
import com.wsdy.saasops.api.modules.user.dto.DepotManageDto;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.member.dao.MbrDepotWalletMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrDepotWalletService;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.nonNull;

/**
 * @Author: Miracle
 * @Description: 平台操作服务 登陆、登出、锁定等
 **/
@Service
@Slf4j
public class DepotOperatService {

    @Autowired
    private MbrAccountService userService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private PtService ptService;
    @Autowired
    private DepotWalletService depotWalletService;
    @Autowired
    private MbrDepotWalletService mbrDepotWalletService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DepotService depotService;
    @Autowired
    private MbrDepotWalletMapper depotWalletMapper;
    @Autowired
    private TGmDepotMapper tGmDepotMapper;


    /**
     * 登出：调用平台接口的方式
     *
     * @param depotId
     * @param sitePrefix
     * @return
     */
    public R LoginOut(Integer depotId, Integer accountId, String sitePrefix) {
        Boolean isTransfer = true;
        MbrAccount user = userService.queryObject(accountId, sitePrefix);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, sitePrefix);
        if (gmApi == null) {
            throw new R200Exception("对不起，暂无此游戏线路");
        }
        switch (gmApi.getDepotCode()) {
            case ApiConstants.DepotCode.AGIN:
                break;
            case ApiConstants.DepotCode.PT:
                isTransfer = ptService.logOut(gmApi, user.getLoginName());
                break;
            case ApiConstants.DepotCode.PT2:
                break;
            case ApiConstants.DepotCode.NT:
                //todo 需要用到登陆session才能登出
                break;
            case ApiConstants.DepotCode.MG:
                break;
            case ApiConstants.DepotCode.PNG:
                break;
            case ApiConstants.DepotCode.T188:
                break;
            default:
                isTransfer = commonService.logOut(gmApi, user.getLoginName());
                break;
        }
        if (isTransfer) {
            return R.ok();
        } else {
            return R.error("登出失败!");
        }
    }

    /**
     * 登出：调用Gateway接口的方式
     *
     * @param depotId
     * @param sitePrefix
     * @return
     */
    public R LoginOutGateway(Integer depotId, Integer accountId, String sitePrefix) {
        MbrDepotWallet depotWallet = new MbrDepotWallet();
        depotWallet.setAccountId(accountId);
        depotWallet.setDepotId(depotId);
        int count = depotWalletMapper.selectCount(depotWallet);
        if (count == 0) {
            return R.ok();
        }
        MbrAccount user = userService.queryObject(accountId, sitePrefix);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, sitePrefix);
        if (gmApi == null) {
            throw new R200Exception("对不起，暂无此游戏线路");
        }
        // 调用gateway登出接口
        GatewayResponseDto responseDto = depotService.LoginOutGateway(gmApi, user.getLoginName());
        if (nonNull(responseDto)) {
            if (Boolean.TRUE.equals(responseDto.getStatus())) {
                return R.ok("用户登出成功！");
            } else {
                log.info("登出平台失败，返回信息:" + JSON.toJSONString(responseDto));
                // TODO 失败原因处理返回
                return R.error("用户已登出！");
            }
        } else {
            return R.error("用户已登出！");
        }
    }

    public TGmDepot findTGmDepotByCode(String depotCode) {
        TGmDepot tGmDepot = new TGmDepot();
        tGmDepot.setDepotCode(depotCode);
        tGmDepot.setAvailable((byte) 1);
        return tGmDepotMapper.selectOne(tGmDepot);
    }


    /***
     * 查询第三方平台列表
     * @param pageNo
     * @param pageSize
     * @param accountId
     * @return
     */
    public R getDepotList(Integer pageNo, Integer pageSize, Integer accountId) {
        String siteCode = CommonUtil.getSiteCode();
        PageHelper.startPage(pageNo, pageSize);
        List<MbrDepotWallet> depots = mbrDepotWalletService.findDepots(accountId);
        PageUtils page = BeanUtil.toPagedResult(depots);
        MbrAccount user = userService.queryObject(accountId, siteCode);
        List<DepotManageDto> depotManageDtos = new ArrayList<>();
        for (MbrDepotWallet depot : depots) {
            CompletableFuture<DepotManageDto> completableFuture = getAsyncBalance(depot, user, siteCode);
            CompletableFuture.allOf(completableFuture).join();
            try {
                depotManageDtos.add(completableFuture.get());
            } catch (Exception e) {
                throw new R200Exception("查询平台余额异常");
            }
        }
        page.setList(depotManageDtos);

        return R.ok().put("data", page);
    }

    /***
     * 查询第三方平台列表  不分页
     * @param accountId
     * @return
     */
    public List<DepotManageDto> getDepotList(Integer accountId) {
        String siteCode = CommonUtil.getSiteCode();
        List<MbrDepotWallet> depots = mbrDepotWalletService.findDepots(accountId);
        MbrAccount user = userService.queryObject(accountId, siteCode);

        List<DepotManageDto> depotManageDtos = new ArrayList<>(depots.size());

        // 根据平台ID号查询各个平台的余额
        for (MbrDepotWallet depot : depots) {
            CompletableFuture<DepotManageDto> completableFuture = getAsyncBalance(depot, user, siteCode);
            CompletableFuture.allOf(completableFuture).join();
            try {
            	DepotManageDto depotManageDto = completableFuture.get();
            	if (depotManageDto != null) {
            		depotManageDtos.add(depotManageDto);
            	}
            } catch (Exception e) {
                throw new R200Exception("查询平台余额异常");
            }
        }
        // AG电子：AGST AG真人：AGIN
        long count = depotManageDtos.stream().filter(t -> ApiConstants.DepotCode.AGST.equalsIgnoreCase(t.getDepotCode())
				|| ApiConstants.DepotCode.AGIN.equalsIgnoreCase(t.getDepotCode())).count();
        // 如果有两个，去除AG电子
        if (count > 1) {
        	depotManageDtos.removeIf(t -> t.getDepotCode().equalsIgnoreCase(ApiConstants.DepotCode.AGST));
        }
        
        // IGGF IGSS
        long countIg = depotManageDtos.stream().filter(t -> ApiConstants.DepotCode.IGGF.equalsIgnoreCase(t.getDepotCode())
        		|| ApiConstants.DepotCode.IGSS.equalsIgnoreCase(t.getDepotCode())).count();
        // 如果有两个，去除 IGSS
        if (countIg > 1) {
        	depotManageDtos.removeIf(t -> t.getDepotCode().equalsIgnoreCase(ApiConstants.DepotCode.IGSS));
        }
        return depotManageDtos;
    }

    /**
     * 获取当前站点下玩家各个平台的余额
     *
     * @param depot
     * @param user
     * @param siteCode
     * @return
     */
    public CompletableFuture<DepotManageDto> getAsyncBalance(MbrDepotWallet depot, MbrAccount user, String siteCode) {
        return CompletableFuture.supplyAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            TGmApi gmApi = gmApiService.queryApiObject(depot.getDepotId(), siteCode);
            if (nonNull(gmApi)) {
                DepotManageDto depotManageDto = new DepotManageDto();
                depotManageDto.setBalance(BigDecimal.ZERO);
                depotManageDto.setPlayer(gmApi.getPrefix() + user.getLoginName());
                depotManageDto.setPlatform(depot.getLastDepotName());
                depotManageDto.getOpterate().setEnableLoggingOut(getLoginOutAuth(gmApi));
                depotManageDto.getOpterate().setEnableLock(getLockAuth(depot.getDepotId()));
                depotManageDto.setPlatformId(depot.getDepotId());
                depotManageDto.setDepotCode(gmApi.getDepotCode());
                try {
                    depotManageDto.setApiId(gmApi.getId());
                    depotManageDto.setBalance(depotWalletService.queryDepotBalance(user.getId(), gmApi).getBalance());
                    log.info("【" + user.getLoginName() + "】获取用户余余额:" + depotManageDto.getBalance());
                } catch (Exception e) {
                    log.error("用户:" + user.getLoginName() + "异余额查询出错" + JSON.toJSONString(gmApi), e);
                }
                return depotManageDto;
            }
            return null;    // 此处直接返回null前端的列表会无法显示，让前端处理了；
        });

    }

    // 支持登出功能的平台
    private Boolean getLoginOutAuth(TGmApi gmApi) {
        switch (gmApi.getDepotCode()) {
            case ApiConstants.DepotCode.BBIN:
                return true;
            default:
                return false;
        }
    }

    // 支持锁定功能的平台  锁定功能去掉，api-gateway无锁定接口
    private Boolean getLockAuth(int depotId) {
        switch (depotId) {
            default:
                return false;
        }
    }

    public BigDecimal flushBalance(Integer accountId, Integer platformId, String siteCode) {
        try {
            TGmApi gmApi = gmApiService.queryApiObject(platformId, siteCode);
            BigDecimal balance = depotWalletService.queryDepotBalance(accountId, gmApi).getBalance();
            log.info("【" + accountId + "】获取用户余余额:" + balance);
            return balance;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public R lockPlayer(Integer accountId, Integer depotId, String sitePrefix) {
        MbrAccount user = userService.queryObject(accountId, sitePrefix);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, sitePrefix);
        String result = ptService.lockPlayer(gmApi, user.getLoginName());
        return R.ok().put(result);
    }

    public R unlockPlayer(Integer accountId, Integer depotId, String sitePrefix) {
        MbrAccount user = userService.queryObject(accountId, sitePrefix);
        TGmApi gmApi = gmApiService.queryApiObject(depotId, sitePrefix);
        return R.ok().put(ptService.unlockPlayer(gmApi, user.getLoginName()));
    }
}
