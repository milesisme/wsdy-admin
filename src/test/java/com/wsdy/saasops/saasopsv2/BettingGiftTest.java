package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.modules.analysis.entity.GameReportQueryModel;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.member.service.AuditCastService;
import com.wsdy.saasops.modules.operate.dto.AuditCat;
import com.wsdy.saasops.modules.operate.dto.BettingGiftDto;
import com.wsdy.saasops.modules.operate.dto.BettingGiftRuleDto;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.operate.service.OprBettingGiftService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BettingGiftTest {

    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private OprBettingGiftService bettingGiftService;
    @Autowired
    private AuditCastService auditCastService;


    @Test
    public void testBettingGiftJson(){

        BettingGiftDto giftDto = new BettingGiftDto();
        String auditCatsStr = "{\"catId\":3,\"depots\":[{\"depotId\":5},{\"depotId\":21},{\"depotId\":18},{\"depotId\":34},{\"depotId\":27},{\"depotId\":8}]}";
        AuditCat cat = new Gson().fromJson(auditCatsStr,AuditCat.class);
        List<AuditCat> auditCats = Lists.newArrayList(cat);
        giftDto.setAuditCats(auditCats);

        giftDto.setIsName(true);
        giftDto.setIsBank(true);
        giftDto.setIsMobile(true);
        giftDto.setDrawType(1);

        BettingGiftRuleDto ruleDto = new BettingGiftRuleDto();
        ruleDto.setValidBetMin(new BigDecimal(100));
        ruleDto.setValidBetMax(new BigDecimal(1000));
        ruleDto.setDonateAmount(new BigDecimal(20));
        ruleDto.setDepositAmountType(1);
        ruleDto.setDepositMin(new BigDecimal(300));
        ruleDto.setDepositMax(new BigDecimal(1000));
        ruleDto.setMultipleWater(3d);
        List<BettingGiftRuleDto> ruleDtos = Lists.newArrayList(ruleDto);
        giftDto.setBettingGiftRuleDtos(ruleDtos);
        String ruleStr = new Gson().toJson(giftDto);
        System.out.println(ruleStr);

    }

    @Test
    public void getMbrValidBetTotal(){

        List<String> siteCode = Lists.newArrayList();
        siteCode.add("ycs");
        siteCode.add("af8ycs");
        siteCode.add("tybh");
        Long start = System.currentTimeMillis();
        Map result = bettingGiftService.getValidBet(siteCode,"testzsj1",
                "2019-11-04 00:00:00","2019-11-10 23:59:59");
        System.out.println(new Gson().toJson(result));
        System.out.println(System.currentTimeMillis() - start);

        GameReportQueryModel model = new GameReportQueryModel();
        model.setLoginName("testzsj1");
        model.setPayOutStrTime("2019-11-04 00:00:00");
        model.setPayOutEndTime("2019-11-10 23:59:59");
        model.setSiteCode("ycs");
        Map result1 = analysisService.getRptBetListReport(model);
        System.out.println(new Gson().toJson(result1));
        System.out.println(System.currentTimeMillis() - start);

    }

    @Before
    public void initMocks() {
// 初始化当前测试类所有@Mock注解模拟对象
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAudit(){
        String siteCode = "ycs";
        Integer id = 1895;
        List<String> siteCodes = Lists.newArrayList();
        siteCodes.add("ycs");
        siteCodes.add("af8ycs");
        siteCodes.add("tybh");
        Boolean isCastAudit = true;
        auditCastService.doingCronAuditAccount(siteCode,id,siteCodes,isCastAudit);
    }
}
