package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.service.PaymentService;
import com.wsdy.saasops.api.modules.user.dto.RebateReportDto;
import com.wsdy.saasops.api.modules.user.service.ApiPromotionService;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.log.service.LogSystemService;
import com.wsdy.saasops.modules.member.entity.MbrRebateReportNew;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.system.pay.entity.SetBacicOnlinepay;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;


@RunWith(SpringRunner.class)
@SpringBootTest
public class applyActivityTests {

    @Autowired
    private OprActActivityCastService actActivityCastService;

    @Autowired
    LogSystemService logSystemService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    ApiPromotionService promotionService;

    @Autowired
    OprActActivityService oprActActivityService;

    @Autowired
    RedisService redisService;

    @Test
    public void addAccount() {
       /* MbrAccount account = new MbrAccount();
        account.setLoginName("qwe098137");
        account.setLoginPwd("123123");
        promotionService.addAccount(21,account);*/
        MbrRebateReportNew rebateReport = new MbrRebateReportNew();
        rebateReport.setAccountId(721);
        rebateReport.setStartTime("2018-11-13");
        rebateReport.setEndTime("2018-11-14");
        List<RebateReportDto> reportDtos =  promotionService.rebateReport(rebateReport);
        System.out.println("2222222222222");
    }


    @Test
    public void optionPayment() {
        PayParams payParams = new PayParams();
        payParams.setOutTradeNo(new SnowFlake().nextId());
        payParams.setIp("121.96.26.189");
        payParams.setSiteCode("ycs");
        payParams.setTerminal(0);
        payParams.setFee(new BigDecimal(300));
        payParams.setOnlinePayId(103);
        payParams.setAccountId(1445);
        payParams.setOutTradeNo(new SnowFlake().nextId());
        paymentService.optionPayment(payParams);
    }

    @Test
    public void updateDongdongPay() {
        SetBacicOnlinepay onlinepay = new SetBacicOnlinepay();
        onlinepay.setPayUrl("http://www.epay666.com/");
        onlinepay.setMerNo("100334");
        onlinepay.setPassword("FC86EAE69E25E33F703AF9E59C5AC212");

        FundDeposit deposit = new FundDeposit();
        deposit.setOrderNo("22461668854333440");
        deposit.setDepositAmount(new BigDecimal(200));
        paymentService.updateDongdongPay(onlinepay,deposit,"ycs");
    }
    @Test
    public void updateCfbPay(){
        SetBacicOnlinepay onlinepay  = new SetBacicOnlinepay();
        FundDeposit deposit = new FundDeposit();
        String siteCode = "ycs";
        onlinepay.setMerNo("cfb0738");
        onlinepay.setPassword("14627385d368d0b6888fc95fd5a86e25");
        deposit.setOrderNo("22145692004777984");
        onlinepay.setPayUrl("https://api.cfbpay.info/");
        paymentService.updateCfbPay(onlinepay,deposit,siteCode);

    }


    @Test
    public void queryLog() {
        System.out.println(new Gson().toJson(logSystemService.queryLog(1,10,null)));
    }

    @Test
    public void test(){
        actActivityCastService.accountBonusList(445,null,1,100);
    }

    @Test
    public void updateActivityState(){
        oprActActivityService.updateActivityState();
    }

    @Test
    public void redisTest(){
        for(int i = 0 ;i<5;i++) {
            Boolean flg = redisService.setRedisExpiredTimeBo(RedisConstants.ACTIVITY_AUDIT_ACCOUNT + "ycs", "ycs", 200, TimeUnit.SECONDS);
            System.out.println(flg);
        }
    }

}
