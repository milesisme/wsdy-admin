package com.wsdy.saasops.api.modules.user.service;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.apisys.entity.TCpSite;
import com.wsdy.saasops.api.modules.apisys.entity.TGmApi;
import com.wsdy.saasops.api.modules.apisys.service.TGmApiService;
import com.wsdy.saasops.api.modules.transfer.mapper.AccountDepotMapper;
import com.wsdy.saasops.api.modules.transferNew.service.DepotService;
import com.wsdy.saasops.api.modules.user.dto.*;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class DepotWalletService {

    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private AccountDepotMapper depotMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TGmApiService gmApiService;
    @Autowired
    private TGmDepotService tGmDepotService;
    @Autowired
    private DepotService depotService;

    /**
     * 查询平台余额（加缓存）
     *
     * @param userId
     * @param gmApi
     * @return
     */
    public UserBalanceResponseDto findDepotBalance(Integer userId, TGmApi gmApi) {
        String depotBalance = RedisConstants.REDIS_DEPOT_BALANCE + gmApi.getDepotId() +
                gmApi.getDepotCode() + userId + "_" + gmApi.getSiteCode();
        if (redisService.booleanRedis(depotBalance)) {
            redisService.setRedisExpiredTime(depotBalance, 3, 30, TimeUnit.SECONDS);
            return queryDepotBalance(userId, gmApi);
        }
        Integer num = Integer.parseInt(redisService.getRedisValus(depotBalance).toString());
        int redisExpire = (int) redisService.getExpire(depotBalance, TimeUnit.SECONDS);
        if (num > 0) {
            redisService.setRedisExpiredTime(depotBalance, num - 1, redisExpire, TimeUnit.SECONDS);
            return queryDepotBalance(userId, gmApi);
        }
        throw new R200Exception("查询余额太频繁，请稍候再试！");
    }

    public List<UserBalanceResponseDto> findDepotAllBalance(Integer userId, Byte terminal, TCpSite cpSite) {
        List<TGmDepot> depotList = tGmDepotService.findDepotList(userId, terminal, cpSite.getSiteCode(), Constants.EVNumber.zero);
        List<UserBalanceResponseDto> userBalanceResponseDtoList = new ArrayList<>();
        depotList.stream().forEach(ls -> {
            TGmApi gmApi = gmApiService.queryApiObject(ls.getId(), cpSite.getSiteCode());
            if (Objects.isNull(gmApi)) {
                throw new R200Exception("对不起，暂无此游戏线路");
            }
            UserBalanceResponseDto userBalanceResponseDto = new UserBalanceResponseDto();
            //TODO 有异常继续执行
            try {
                userBalanceResponseDto = findDepotBalance(userId, gmApi);
            } catch (Exception e) {
                //TODO 如果查询第三方返回false，则把平台余额默认为0
                userBalanceResponseDto.setDepotId(ls.getId());
                userBalanceResponseDto.setCurrency("RMB");
                userBalanceResponseDto.setBalance(BigDecimal.ZERO);
                log.error("error:" + e);
            }
            userBalanceResponseDtoList.add(userBalanceResponseDto);
        });
        return userBalanceResponseDtoList;
    }

    public UserBalanceResponseDto queryDepotBalance(Integer userId, TGmApi gmApi) {
        MbrAccount user = mbrAccountMapper.selectByPrimaryKey(userId);
        if (Objects.isNull(user)) {
            throw new RRException("会员信息不能为空!");
        }
        UserBalanceResponseDto balanceDto = new UserBalanceResponseDto();
        balanceDto.setBalance(new BigDecimal(0.00));
        balanceDto = getAllDepotBalance(gmApi, user, balanceDto);
        return balanceDto;
    }

    public UserBalanceResponseDto getBalance(PtUserInfo info) {
        UserBalanceResponseDto balanceDto = new UserBalanceResponseDto();
        balanceDto.setBalance(new BigDecimal(info.getBALANCE()));
        balanceDto.setCurrency(ApiConstants.CURRENCY_TYPE);
        return balanceDto;
    }

    public TransferListDto findTransferList(TransferRequestDto requestDto, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TransferResponseDto> list = depotMapper.findTransferList(requestDto);
        PageUtils pageUtils = BeanUtil.toPagedResult(list);
        TransferListDto dto = new TransferListDto();
        dto.setList(pageUtils.getList());
        dto.setCurrPage(pageUtils.getCurrPage());
        dto.setPageSize(pageUtils.getPageSize());
        dto.setTotalCount(pageUtils.getTotalCount());
        dto.setTotalPage(pageUtils.getTotalPage());
        return dto;
    }

    /**
     * 获取各个平台余额
     *
     * @param gmApi
     * @param user
     * @param balanceDto
     * @return
     */
    private UserBalanceResponseDto getAllDepotBalance(TGmApi gmApi, MbrAccount user, UserBalanceResponseDto balanceDto) {
        if (Objects.nonNull(gmApi)) {
            balanceDto = depotService.queryDepotBalanceNew(user, gmApi);
        }
        return balanceDto;
    }
}
