package com.wsdy.saasops.api.modules.user.service;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.api.modules.transfer.dto.BillRequestDto;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.member.entity.MbrDepotWallet;
import com.wsdy.saasops.modules.member.service.MbrWalletService;
import com.wsdy.saasops.modules.operate.dao.TGmDepotMapper;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.wsdy.saasops.common.utils.DateUtil.*;

@Service
@Slf4j
public class DepositCommonService {

    @Autowired
    private MbrWalletService mbrWalletService;
    @Autowired
    private TGmDepotMapper depotMapper;

    //生成随机数字和字母,
    public String getStringRandom(int length) {

        String val = "";
        Random random = new Random();

        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {

            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            //输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                //输出是大写字母还是小写字母
                int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val += (char) (random.nextInt(26) + temp);
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }

    //生成随机数字,
    public String getStringRandomNum(int length) {
        String val = "";
        Random random = new Random();

        //参数length，表示生成几位随机数
        for (int i = 0; i < length; i++) {

            val += String.valueOf(random.nextInt(10));
        }
        return val;
    }

    public MbrBillManage setMbrBillManage(BillRequestDto requestDto) {
        MbrBillManage mbrBillManage = new MbrBillManage();
        mbrBillManage.setAccountId(requestDto.getAccountId());
        mbrBillManage.setDepotId(requestDto.getDepotId());
        mbrBillManage.setLoginName(requestDto.getLoginName());
        mbrBillManage.setAmount(requestDto.getAmount());
        // 此处根据depotId 获取depotCode, 之前用数据库id来区分的到底怎么想的
        // 因为是公共接口库调用的地方多，无法每个调用都修改获取depotCode
        TGmDepot tGmDepot = new TGmDepot();
        tGmDepot.setId(requestDto.getDepotId());
        tGmDepot = depotMapper.selectOne(tGmDepot);

        if (ApiConstants.DepotCode.GD.equals(tGmDepot.getDepotCode())) {
            //D180816134512Dn2D6  年月日+时分秒+5随机字符
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
            String dateStr = format.format(date); //转为字符串
            String orderNo = requestDto.getPrefix() +
                    dateStr +
                    getStringRandom(5);
            requestDto.setOrderNo(orderNo);
            mbrBillManage.setOrderNo(orderNo);
        } else if (ApiConstants.DepotCode.AB.equals(tGmDepot.getDepotCode())) {
            String orderNo = new SnowFlake().nextId() + "";
            String subOrderNo = orderNo.substring(0, 13);
            requestDto.setOrderNo(subOrderNo);
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        } else if (ApiConstants.DepotCode.OG.equals(tGmDepot.getDepotCode())) {
            String orderNo = new SnowFlake().nextId() + "";
            String subOrderNo = orderNo.substring(0, 16);
            requestDto.setOrderNo(subOrderNo);
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        } else if (ApiConstants.DepotCode.KY.equals(tGmDepot.getDepotCode())) {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String orderNo = format.format(date); //转为字符串
            requestDto.setOrderNo(orderNo);
            mbrBillManage.setOrderNo(orderNo);
        } else if (ApiConstants.DepotCode.AGIN.equals(tGmDepot.getDepotCode())) {
            String orderNo = new SnowFlake().nextId() + "";
            String subOrderNo = orderNo.substring(0, 16);
            requestDto.setOrderNo(subOrderNo);
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        } else if (ApiConstants.DepotCode.DS.equals(tGmDepot.getDepotCode())) {      // 用depotId不准确，之前的是用这个，新加的逻辑用depotCode
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String orderNo = format.format(date); //转为字符串
            requestDto.setOrderNo(orderNo);
            mbrBillManage.setOrderNo(orderNo);
        } else if (ApiConstants.DepotCode.DFW.equals(tGmDepot.getDepotCode())) {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            String orderNo = format.format(date); //转为字符串
            requestDto.setOrderNo(orderNo);
            mbrBillManage.setOrderNo(orderNo);
        } else if (ApiConstants.DepotCode.TM.equals(tGmDepot.getDepotCode())) {    // 天美单号要求18位数字
            String orderNo = new SnowFlake().nextId() + ""; // 17位
            orderNo = orderNo + getStringRandomNum(1); // 随机1位
            requestDto.setOrderNo(orderNo);
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        } else if (ApiConstants.DepotCode.OBES.equals(tGmDepot.getDepotCode())) {    // OBES单号要求20位数字
            String orderNo = new SnowFlake().nextId() + ""; // 17位
            orderNo = orderNo + getStringRandomNum(3); // 随机3位
            requestDto.setOrderNo(orderNo);
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        }else if (ApiConstants.DepotCode.OBDY.equals(tGmDepot.getDepotCode())) {   // 13位的时间戳（毫秒）
            String orderNo = String.valueOf(Calendar.getInstance().getTimeInMillis());
            requestDto.setOrderNo(orderNo);
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        } else if (ApiConstants.DepotCode.OBQP.equals(tGmDepot.getDepotCode())) {   // 13位的时间戳（毫秒）
            String orderNo = String.valueOf(Calendar.getInstance().getTimeInMillis());
            requestDto.setOrderNo(orderNo);
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        } else if (ApiConstants.DepotCode.AGST.equalsIgnoreCase(tGmDepot.getDepotCode())) {
            String orderNo = new SnowFlake().nextId() + "";
            String subOrderNo = orderNo.substring(0, 16);
            requestDto.setOrderNo(subOrderNo);
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        } else {
            requestDto.setOrderNo(new SnowFlake().nextId() + "");
            mbrBillManage.setOrderNo(requestDto.getOrderNo());
        }
        mbrBillManage.setStatus(Constants.manageStatus.freeze);     // 转账前，转账订单为冻结状态；
        mbrBillManage.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrBillManage.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        mbrBillManage.setModifyUser(requestDto.getLoginName());
        mbrBillManage.setLogId(requestDto.getId());
        return mbrBillManage;
    }
}
