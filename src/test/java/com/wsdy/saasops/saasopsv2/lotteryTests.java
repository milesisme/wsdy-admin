package com.wsdy.saasops.saasopsv2;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.wsdy.saasops.modules.lottery.dto.LotteryActivityDto;
import com.wsdy.saasops.modules.lottery.dto.LotteryAreaDto;
import com.wsdy.saasops.modules.lottery.dto.LotteryDepositDto;
import com.wsdy.saasops.modules.lottery.dto.LotteryPrizeAreaDto;
import com.wsdy.saasops.modules.lottery.serivce.LotteryActivityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class lotteryTests {

    @Autowired
    private LotteryActivityService lotteryActivityService;



    @Test
    public void accountSignInfo() {



        lotteryActivityService.lotteryInfo(8515,"","dcs");

      /*  lotteryActivityService.accountLottery(501,
                "wsdy.net",2,"dcs","127.0.0.1");*/
    }


   /* public static void main(String[] args) {
        List<LotteryAreaDto> lotteryAreaDtos = Lists.newArrayList();

        lotteryAreaDtos.add(test(1));
        lotteryAreaDtos.add(test(2));
        LotteryActivityDto lotteryActivityDto = new LotteryActivityDto();
        lotteryActivityDto.setLotteryAreaDtos(lotteryAreaDtos);
        System.out.println(JSON.toJSON(lotteryActivityDto));
    }*/

    public static LotteryAreaDto test(int i){
        LotteryAreaDto lotteryAreaDto = new LotteryAreaDto();
        //奖区
        lotteryAreaDto.setPrizeArea(i);

        List<LotteryPrizeAreaDto> prizeAreaDtos = Lists.newArrayList();
        LotteryPrizeAreaDto prizeAreaDto = new LotteryPrizeAreaDto();
        prizeAreaDto.setPrizeName("谢谢参与");
        prizeAreaDto.setPrizeType(1);
        prizeAreaDto.setProbability(50);
        prizeAreaDtos.add(prizeAreaDto);

        LotteryPrizeAreaDto prizeAreaDto1 = new LotteryPrizeAreaDto();
        prizeAreaDto1.setPrizeName("588元彩金");
        prizeAreaDto1.setPrizeType(2);
        prizeAreaDto1.setProbability(30);
        prizeAreaDto1.setDonateAmount(new BigDecimal(588));
        prizeAreaDto1.setMultipleWater(12.0);
        prizeAreaDtos.add(prizeAreaDto1);

        LotteryPrizeAreaDto prizeAreaDto2 = new LotteryPrizeAreaDto();
        prizeAreaDto2.setPrizeName("苹果12pro");
        prizeAreaDto2.setPrizeType(3);
        prizeAreaDto2.setProbability(20);
        prizeAreaDtos.add(prizeAreaDto2);

        lotteryAreaDto.setPrizeAreaDtos(prizeAreaDtos);
        lotteryAreaDto.setIsBank(true);
        lotteryAreaDto.setIsMail(false);
        lotteryAreaDto.setIsName(true);
        lotteryAreaDto.setIsMobile(false);
        lotteryAreaDto.setActLevelIds(Lists.newArrayList(1,2,3,4,5,6,7,8,9,10));
        lotteryAreaDto.setDomains("wsdy.net,www.wsdy.net,www.baidu.com");

        lotteryAreaDto.setRegisterCondition(0);
        lotteryAreaDto.setCycle(0);

        lotteryAreaDto.setDomainsCondition(Boolean.TRUE);
        lotteryAreaDto.setRegisterDomains("wsdy.net,www.wsdy.net");
        lotteryAreaDto.setNum(10);

        List<LotteryDepositDto> depositDtos = Lists.newArrayList();
        LotteryDepositDto lotteryDepositDto = new LotteryDepositDto();
        lotteryDepositDto.setSign(0);
        lotteryDepositDto.setAmountConditions(new BigDecimal(5000));
        lotteryDepositDto.setNum(5);
        depositDtos.add(lotteryDepositDto);

        LotteryDepositDto lotteryDepositDto1 = new LotteryDepositDto();
        lotteryDepositDto1.setSign(1);
        lotteryDepositDto1.setAmountConditions(new BigDecimal(2000));
        lotteryDepositDto1.setNum(6);
        depositDtos.add(lotteryDepositDto1);

        LotteryDepositDto lotteryDepositDto2 = new LotteryDepositDto();
        lotteryDepositDto2.setSign(2);
        lotteryDepositDto2.setAmountConditions(new BigDecimal(10000));
        lotteryDepositDto2.setNum(4);
        depositDtos.add(lotteryDepositDto2);

        lotteryAreaDto.setDepositDtos(depositDtos);
        return lotteryAreaDto;
    }
}
