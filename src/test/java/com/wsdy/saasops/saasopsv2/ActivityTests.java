package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.modules.user.service.SdyActivityService;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.modules.analysis.service.AnalysisService;
import com.wsdy.saasops.modules.analysis.service.TurnoverRateService;
import com.wsdy.saasops.modules.fund.entity.FundAudit;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrActivityLevelMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevel;
import com.wsdy.saasops.modules.member.service.*;
import com.wsdy.saasops.modules.operate.dto.*;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.TGmDepot;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.modules.operate.service.OprActActivityService;
import com.wsdy.saasops.modules.operate.service.TGmDepotService;
import com.wsdy.saasops.modules.task.service.TaskAccountService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivityTests {

    @Autowired
    private OprActActivityService oprActBaseService;
    @Autowired
    private OprActActivityService actActivityService;
    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private AccountWaterSettlementService settlementService;
    @Autowired
    private SanGongRebateCastService sanGongRebateCastService;
    @Autowired
    private AnalysisService analysisService;
    @Autowired
    private TaskAccountService taskAccountService;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private SdyActivityService sdyActivityService;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private AccountVipRedService accountVipRedService;
    @Autowired
    private AccountAutoCastService accountAutoCastService;
    @Autowired
    private MbrActivityLevelMapper activityLevelMapper;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private TurnoverRateService rateService;


    @Test
    public void accountSignInfo() {
        rateService.castAccountRate("dcs");
        // MbrAccount account =accountMapper.selectByPrimaryKey(1300);
        //sdyActivityService.accountBirthday(8230);
      /*  MbrAccount account = accountMapper.selectByPrimaryKey(8215);
        MbrActivityLevel activityLevel1 = activityLevelMapper.selectByPrimaryKey(2);
        accountAutoCastService.accountUpgradeBonus(account, activityLevel1);*/
        //okHttpService.get("https://adminapitest.longxiaoqz.com/api/data/BetDetailList");
    }

    @Test
    public void sanGongRebateCast() {
        taskAccountService.getTask(7, 501, "ycs", "nathan", null);
    }

    @Test
    public void test6767() {
        BizEvent bizEvent = new BizEvent(this, "", null, null);
        FundAudit fundAudit = new FundAudit();

        //System.out.println(JSON.toJSONString(waterCastService.findAccountWaterRate(1912, "ycs")));
        //waterCastService.settlementWater(1912, "ycs");
        List<String> ids = Lists.newArrayList("");
      /*  for (int j = 0; j < 100; j++) {
            settlementService.esUpdateByQuery(settlementService.getUpdateWaterJson(j), "f68c1e8d6144441aa7d934021441d324");
        }*/
        settlementService.esUpdateWaterBybonusId("ycs", "testfl906", 99);
    }

    @Test
    public void isCastAccountAuto() {
//        dispatcherTaskService.dispatcherTask("ycs","accountWater");
    }

    @Test
    public void applyActivity() {
       // oprActBaseService.applyActivity(8228, 83, "dcs", "127.0.0.1");
    }

    @Test
    public void updateActivityState() {
        actActivityService.webActivityList(1, 100, 5, 21, null, 0, 1, null, Constants.EVNumber.one, null);
    }

    @Test
    public void findAccountBonusList() {
        actActivityService.findAccountBonusList(null, null, 525, 1, 3, null,null);
    }

    @Test
    public void AQ0000001() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(1);
        activity.setActivityName("首存送AA");

        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);

        activity.setContent("测试首存送AA活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JPreferentialDto dto = new JPreferentialDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        //dto.setScopeDto(scopeDto);
        dto.setScope(2);
        // dto.setIsAudit(true);


        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        auditCat1.setCatId(5);
        auditCats.add(auditCat1);
        AuditCat auditCat2 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat2);
        AuditCat auditCat3 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat3);
        //dto.setAuditCats(auditCats);


        List<ActivityRuleDto> ruleDtos = Lists.newArrayList();
        ActivityRuleDto activityRuleDto = new ActivityRuleDto();
        activityRuleDto.setAmountMin(new BigDecimal(100));
        activityRuleDto.setAmountMax(new BigDecimal(200));
        activityRuleDto.setDonateType(0);
        activityRuleDto.setDonateAmount(new BigDecimal(10));
        activityRuleDto.setDonateAmountMax(new BigDecimal(300));
        activityRuleDto.setMultipleWater(10.0);
        ruleDtos.add(activityRuleDto);
        ActivityRuleDto ruleDto = new ActivityRuleDto();
        ruleDto.setAmountMin(new BigDecimal(100));
        ruleDto.setAmountMax(new BigDecimal(200));
        ruleDto.setDonateType(1);
        ruleDto.setDonateAmount(new BigDecimal(20));
        ruleDto.setDonateAmountMax(new BigDecimal(300));
        ruleDto.setMultipleWater(10.0);
        ruleDtos.add(ruleDto);
        //dto.setActivityRuleDtos(ruleDtos);

        ActivityDto activityDto = new ActivityDto();

        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null, null, null);
    }


    @Test
    public void AQ0000002() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(2);
        activity.setActivityName("注册送活动");

        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);

        activity.setContent("测试首注册送活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JRegisterDto dto = new JRegisterDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);


        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat1);
        AuditCat auditCat2 = new AuditCat();
        auditCats.add(auditCat2);
        AuditCat auditCat3 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat3);
        dto.setAuditCats(auditCats);

        dto.setRegisterStartTime("2017-03-10 00:00:00");
        dto.setRegisterEndTime("2017-03-10 00:00:00");

        RegisterRuleDto registerRuleDto1 = new RegisterRuleDto();
        registerRuleDto1.setDonateAmount(new BigDecimal(800));
        registerRuleDto1.setMultipleWater(15.0);
        dto.setRuleDto(registerRuleDto1);

        ActivityDto activityDto = new ActivityDto();

        System.out.println(new Gson().toJson(dto));
        /* oprActBaseService.save(activityDto, "admin", null, null);*/
    }


    @Test
    public void AQ0000003() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(3);
        activity.setActivityName("存就送活动");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);

        activity.setContent("测试存就送活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JDepositSentDto dto = new JDepositSentDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        //dto.setScopeDto(scopeDto);
        //dto.setIsAudit(true);


        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat1);
        AuditCat auditCat2 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat2);
        AuditCat auditCat3 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat3);
        //dto.setAuditCats(auditCats);


        List<ActivityRuleDto> ruleDtos = Lists.newArrayList();
        ActivityRuleDto activityRuleDto = new ActivityRuleDto();
        activityRuleDto.setAmountMin(new BigDecimal(100));
        activityRuleDto.setAmountMax(new BigDecimal(200));
        activityRuleDto.setDonateType(0);
        activityRuleDto.setDonateAmountMax(new BigDecimal(300));
        activityRuleDto.setMultipleWater(10.0);
        ruleDtos.add(activityRuleDto);
        ActivityRuleDto ruleDto = new ActivityRuleDto();
        ruleDto.setAmountMin(new BigDecimal(100));
        ruleDto.setAmountMax(new BigDecimal(200));
        ruleDto.setDonateType(1);
        ruleDto.setDonateAmount(new BigDecimal(20));
        ruleDto.setDonateAmountMax(new BigDecimal(300));
        ruleDto.setMultipleWater(10.0);
        ruleDtos.add(ruleDto);
        //dto.setActivityRuleDtos(ruleDtos);

        ActivityDto activityDto = new ActivityDto();

        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null, null, null);
    }


    @Test
    public void AQ0000004() {
        // DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(4);
        activity.setActivityName("救援金活动");
        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);

        activity.setContent("测试存救援金啊啊啊啊啊啊啊啊啊啊啊啊");

    }

    @Test
    public void AQ0000005() {
        // DynamicDataSource.setDataSource("test");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(5);
        activity.setActivityName("返水优惠活动");

        activity.setUseStart("2018-12-28");
        activity.setUseEnd("2019-12-28");
        activity.setUseState(0);

        activity.setContent("测试返水优惠活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JWaterRebatesDto dto = new JWaterRebatesDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        //dto.setScopeDto(scopeDto);
        dto.setScope(2);
        //dto.setIsAudit(true);
        //dto.setDrawNumber(1);

        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        List<AuditDepot> depots2 = Lists.newArrayList();
        AuditDepot auditDepot2 = new AuditDepot();
        auditDepot2.setDepotId(0);
        auditDepot2.setGames(null);
        depots2.add(auditDepot2);
        auditCat1.setDepots(depots2);
        auditCat1.setCatId(5);
        auditCats.add(auditCat1);

        AuditCat auditCat2 = new AuditCat();
        List<AuditDepot> depots1 = Lists.newArrayList();
        AuditDepot auditDepot1 = new AuditDepot();
        auditDepot1.setDepotId(0);
        auditDepot1.setGames(null);
        depots1.add(auditDepot1);
        auditCat2.setDepots(depots1);
        auditCat2.setCatId(3);
        auditCats.add(auditCat2);

        AuditCat auditCat3 = new AuditCat();
        List<AuditDepot> depots = Lists.newArrayList();
        AuditDepot auditDepot = new AuditDepot();
        auditDepot.setDepotId(1);
        auditDepot.setGames(Lists.newArrayList(1, 2, 3, 88));
        depots.add(auditDepot);
        auditCat3.setCatId(12);
        auditCat3.setDepots(depots);
        auditCats.add(auditCat3);

        List<WaterRebatesRuleListDto> ruleListDtos = Lists.newArrayList();
        WaterRebatesRuleListDto ruleListDto = new WaterRebatesRuleListDto();
        ruleListDto.setValidAmountMin(new BigDecimal(1));
        ruleListDto.setValidAmountMax(new BigDecimal(2000));
        ruleListDto.setDonateRatio(new BigDecimal(10));
        ruleListDtos.add(ruleListDto);


        ActivityDto activityDto = new ActivityDto();

        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null, null, null);
    }


    @Test
    public void AQ0000006() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(6);
        activity.setActivityName("有效投注活动");

        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);

        activity.setContent("测试有效投注活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JValidDto dto = new JValidDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setScope(2);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);
        dto.setDrawType(0);
        dto.setDrawNumber(1);
        dto.setFormulaMode(0);

        List<AuditCat> auditCats = Lists.newArrayList();
        AuditCat auditCat = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat);
        AuditCat auditCat1 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat1);
        AuditCat auditCat2 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat2);
        AuditCat auditCat3 = new AuditCat();
        auditCat.setCatId(1);
        auditCats.add(auditCat3);
        dto.setAuditCats(auditCats);


        List<WaterRebatesRuleListDto> ruleListDtos = Lists.newArrayList();
        WaterRebatesRuleListDto ruleListDto = new WaterRebatesRuleListDto();
        ruleListDto.setValidAmountMin(new BigDecimal(1000));
        ruleListDto.setValidAmountMax(new BigDecimal(2000));
        ruleListDto.setDonateRatio(new BigDecimal(10));
        ruleListDtos.add(ruleListDto);

        WaterRebatesRuleListDto ruleListDto1 = new WaterRebatesRuleListDto();
        ruleListDto1.setValidAmountMin(new BigDecimal(10100));
        ruleListDto1.setValidAmountMax(new BigDecimal(20010));
        ruleListDto1.setDonateRatio(new BigDecimal(20));
        ruleListDtos.add(ruleListDto1);

        dto.setRuleDtos(ruleListDtos);

        ActivityDto activityDto = new ActivityDto();

        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null, null, null);
    }


    @Test
    public void AQ0000007() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(7);
        activity.setActivityName("推荐送活动");

        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);

        activity.setContent("测试推荐送活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JRecommendDto dto = new JRecommendDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setIsAudit(true);

        RecommendAwardDto awardDto = new RecommendAwardDto();
        awardDto.setIsAward(true);
        awardDto.setAwardType(1);
        awardDto.setAwardBasics(new BigDecimal(500));
        awardDto.setAwardMoney(new BigDecimal(30));
        awardDto.setMultipleWater(8);
        dto.setAward(awardDto);

        RecommendBonusDto bonus = new RecommendBonusDto();
        bonus.setIsBonus(true);
        bonus.setBetMoney(new BigDecimal(2000));
        bonus.setBonusMax(new BigDecimal(500));
        bonus.setMultipleWater(10);
        List<RecommendBonusListDto> bonusListDtos = Lists.newArrayList();
        RecommendBonusListDto recommendBonusListDto = new RecommendBonusListDto();
        recommendBonusListDto.setBetNumber(1);
        recommendBonusListDto.setBonusRatio(new BigDecimal(10));
        bonusListDtos.add(recommendBonusListDto);
        bonus.setBonusListDtos(bonusListDtos);
        dto.setBonus(bonus);

        ActivityDto activityDto = new ActivityDto();

        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null, null, null);
    }


    @Test
    public void AQ0000008() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(8);
        activity.setActivityName("签到活动");

        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);

        activity.setContent("测试签到活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JSignInDto dto = new JSignInDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setIsAudit(true);
        dto.setIsBank(true);
        dto.setIsMail(true);
        dto.setIsMobile(true);
        dto.setIsName(true);
        dto.setSignType(0);
        dto.setSignBenchmark(0);

        List<SignInRuleDto> ruleDtos = Lists.newArrayList();
        SignInRuleDto signInRuleDto = new SignInRuleDto();
        signInRuleDto.setValidAmountMin(new BigDecimal(100));
        signInRuleDto.setDonateAmountMax(new BigDecimal(300));
        signInRuleDto.setDonateType(1);
        signInRuleDto.setDonateAmountMax(new BigDecimal(300));
        signInRuleDto.setMultipleWaterType(0);
        signInRuleDto.setMultipleWater(10.0);
        ruleDtos.add(signInRuleDto);
        dto.setRuleDtos(ruleDtos);

        ActivityDto activityDto = new ActivityDto();

        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null, null, null);
    }

    @Test
    public void AQ0000009() {
        //DynamicDataSource.setDataSource("187");
        OprActActivity activity = new OprActActivity();
        activity.setActTmplId(9);
        activity.setActivityName("红包活动");

        activity.setUseStart("2017-12-28");
        activity.setUseEnd("2017-12-28");
        activity.setUseState(0);

        activity.setContent("测试红包活动啊啊啊啊啊啊啊啊啊啊啊啊");

        JRedPacketDto dto = new JRedPacketDto();
        ActivityScopeDto scopeDto = new ActivityScopeDto();
        scopeDto.setIsAccAll(false);
        scopeDto.setAccIds(Lists.newArrayList(1, 5, 15, 16));
        scopeDto.setIsAgyTopAll(false);
        scopeDto.setAgyTopIds(Lists.newArrayList(20, 21, 22));
        scopeDto.setIsAgyAll(true);
        dto.setScopeDto(scopeDto);
        dto.setIsAudit(true);


        ActivityDto activityDto = new ActivityDto();

        System.out.println(new Gson().toJson(dto));
        oprActBaseService.save(activityDto, "admin", null, null, null, null);
    }

    @Test
    public void smsTest() {
//        sendSmsSevice.sendSmsMass("008613645027795","123");
        MbrAccount account = new MbrAccount();
        account.setContent("123");
        List<Integer> accountIds = new ArrayList<>();
        accountIds.add(1481);
        account.setAccountIds(accountIds);
        mbrAccountService.accountMassTexting(account, "ycs");

    }
}
