package com.wsdy.saasops.sysapi.service;

import com.alibaba.fastjson.JSON;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.member.dao.*;
import com.wsdy.saasops.modules.member.entity.*;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.sysapi.dto.TradeRequestDto;
import com.wsdy.saasops.sysapi.dto.TradeResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static com.wsdy.saasops.sysapi.dto.TradeResponseDto.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@Transactional
public class DepotTradeService {

    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private MbrWalletMapper walletMapper;
    @Autowired
    private MbrDepotTradeLogMapper tradeLogMapper;
    @Autowired
    private MbrWalletService walletService;
    @Autowired
    private MbrDepotTradeMapper tradeMapper;
    @Autowired
    private TGmDepotMapper gmDepotMapper;
    @Autowired
    private MbrBillDetailMapper mbrBillDetailMapper;
    @Autowired
    private MbrMapper mbrMapper;


    public TradeResponseDto findAccountBalance(String loginName) {
        TradeResponseDto responseDto = new TradeResponseDto();
        MbrAccount account = new MbrAccount();
        account.setLoginName(loginName);
        MbrAccount account1 = accountMapper.selectOne(account);
        if (isNull(account1)) {
            responseDto.setMsg("会员不存在");
            responseDto.setCode(ERROR_CODE_11);
            return responseDto;
        }
        Map<String, Object> stringMap = new HashMap<>();
        if (account1.getAvailable() != 1) {
            stringMap.put("balance", BigDecimal.ZERO);
        } else {
            MbrWallet wallet = new MbrWallet();
            wallet.setAccountId(account1.getId());
            MbrWallet wallet1 = walletMapper.selectOne(wallet);
            stringMap.put("balance", wallet1.getBalance());
        }
        responseDto.setData(stringMap);
        return responseDto;
    }

    public MbrDepotTradeLog addMbrDepotTradeLog(TradeRequestDto requestDto) {
        MbrDepotTradeLog tradeLog = new MbrDepotTradeLog();
        tradeLog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        tradeLog.setOutTradeno(requestDto.getOutTradeno());
        tradeLog.setParam(JSON.toJSONString(requestDto));
        tradeLogMapper.insert(tradeLog);
        return tradeLog;
    }


    public TradeResponseDto operatingBalance(TradeRequestDto requestDto, MbrDepotTradeLog tradeLog) {
        TradeResponseDto responseDto1 = findAccountBalance(requestDto.getLoginName());
        if (!"00".equals(responseDto1.getCode())) {
            return responseDto1;
        }
        MbrDepotTrade depotTrade1 = new MbrDepotTrade();
        depotTrade1.setOutTradeno(requestDto.getOutTradeno());
        int count = tradeMapper.selectCount(depotTrade1);
        if (count > 0) {
            responseDto1.setCode(ERROR_CODE_96);
            responseDto1.setMsg("交易号已存在");
            return responseDto1;
        }

        TradeResponseDto responseDto = new TradeResponseDto();
        BigDecimal bigDecimal = (BigDecimal) responseDto1.getData().get("balance");
        if (requestDto.getAmount().compareTo(bigDecimal) == 1 && requestDto.getOpType() == 0) {
            responseDto.setMsg("余额不足，扣款失败");
            responseDto.setCode(ERROR_CODE_98);
            updateMbrDepotTradeLog(tradeLog, responseDto.getMsg(), responseDto.getCode());
            return responseDto;
        }
        String orderNo = new SnowFlake().nextId() + CommonUtil.genRandom(4, 4);
        MbrAccount account = new MbrAccount();
        account.setLoginName(requestDto.getLoginName());
        MbrAccount account1 = accountMapper.selectOne(account);

        MbrWallet mbrWallet = mbrMapper.findWalletForUpdate(account1.getId());

        Boolean sign = requestDto.getOpType() == Constants.EVNumber.one ? Boolean.TRUE : Boolean.FALSE;
        MbrBillDetail mbrBillDetail = walletService.castWalletAndBillDetail(account1.getLoginName(),
                account1.getId(), requestDto.getFinancialcode(), requestDto.getAmount(),
                orderNo, sign, null, account1.getLoginName());
        if (isNull(mbrBillDetail)) {
            responseDto.setMsg("余额操作失败");
            responseDto.setCode(ERROR_CODE_98);
            updateMbrDepotTradeLog(tradeLog, responseDto.getMsg(), responseDto.getCode());
            return responseDto;
        }
        MbrDepotTrade depotTrade = new MbrDepotTrade();
        depotTrade.setAccountId(account1.getId());
        depotTrade.setAmount(requestDto.getAmount());
        depotTrade.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        depotTrade.setDepotCode(requestDto.getDepotCode());
        depotTrade.setFinancialcode(requestDto.getFinancialcode());
        depotTrade.setOpType(requestDto.getOpType());
        depotTrade.setOutTradeno(requestDto.getOutTradeno());
        depotTrade.setOrderNo(orderNo);
        depotTrade.setLoginName(account1.getLoginName());
        depotTrade.setAfterBalance(mbrBillDetail.getAfterBalance());
        depotTrade.setBeforeBalance(mbrBillDetail.getBeforeBalance());
        tradeMapper.insert(depotTrade);

        TGmDepot gmDepot = new TGmDepot();
        gmDepot.setDepotCode(requestDto.getDepotCode());
        TGmDepot gmDepot1 = gmDepotMapper.selectOne(gmDepot);
        if (nonNull(gmDepot1)) {
            mbrBillDetail.setDepotId(gmDepot.getId());
            mbrBillDetailMapper.updateByPrimaryKeySelective(mbrBillDetail);
        }
        updateMbrDepotTradeLog(tradeLog, null, responseDto.getCode());
        return responseDto;
    }

    private void updateMbrDepotTradeLog(MbrDepotTradeLog tradeLog, String msg, String code) {
        tradeLog.setMemo(msg);
        tradeLog.setCode(code);
        tradeLogMapper.updateByPrimaryKeySelective(tradeLog);
    }


    public TradeResponseDto findOrderNo(TradeRequestDto requestDto) {
        MbrDepotTrade depotTrade = new MbrDepotTrade();
        depotTrade.setOutTradeno(requestDto.getOutTradeno());
        int count = tradeMapper.selectCount(depotTrade);
        TradeResponseDto responseDto = new TradeResponseDto();
        if (count > 0) {
            responseDto.setCode(ERROR_CODE_00);
            responseDto.setMsg("成功");
        } else {
            MbrDepotTradeLog tradeLog = new MbrDepotTradeLog();
            tradeLog.setOutTradeno(requestDto.getOutTradeno());
            MbrDepotTradeLog tradeLog1 = tradeLogMapper.selectOne(tradeLog);
            if (isNull(tradeLog1)) {
                responseDto.setCode(ERROR_CODE_97);
                responseDto.setMsg("订单不存在");
            } else {
                responseDto.setCode(tradeLog1.getCode());
                responseDto.setMsg(tradeLog1.getMemo());
            }
        }
        return responseDto;
    }

}
