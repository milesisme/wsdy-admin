package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.AccountWaterCastService;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.member.service.AuditCastService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Objects;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AuditTests {

    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private AuditCastService auditCastService;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private MbrAccountMapper accountMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private AccountWaterCastService accountWaterCastService;


    @Test
    public void accountAutoCastService(){
       /*accountWaterCastService.getValidBetGB(null,
               "yoxi1993",null,null,null, null);*/
       // accountWaterCastService.esUpdateByQuery(accountWaterCastService.getUpdateBounsJson(0000000000));
      // List<WaterDepotDto>  waterDepotDtos =  accountWaterCastService.findAccountWaterRate(1105,"ycs");
        OprActBonus oprActBonus = new OprActBonus();
        oprActBonus.setAccountId(1105);

        Integer accountId = 1105;
        oprActBonus.setAccountId(accountId);
        //accountWaterCastService.waterDetailList(oprActBonus, 1, 8);
    }



    @Test
    @Rollback(false)
    public void test12() {
        //auditCastService.doingCronAuditAccount("ybh", 488);
        auditAccountService.auditDetail(2);
    }

    @Test
    public void test1() {
        auditAccountService.isBounsOut(8, 1);
    }

    @Test
    public void getDepotAuditDto() {
        auditAccountService.getDepotAuditDto(8, 1);
    }


    @Test
    public void accountUseBonus() {
        OprActBonus actBonus = actBonusMapper.selectByPrimaryKey(637);
        MbrAccount account = accountMapper.selectByPrimaryKey(431);
        actBonus.setBonusAmount(new BigDecimal(8));
        actBonus.setDiscountAudit(new BigDecimal(2));
        auditAccountService.accountUseBonus(actBonus, account, new BigDecimal(80), 222, 5, 14);
    }

    @Test
    public void test00(){
        String transferInCont = RedisConstants.SEESION_TRANSFERIN + 1 + 12;
        if (Objects.nonNull(redisService.getRedisValus(transferInCont))){
            System.out.println("11111");
        }
        redisService.setRedisValue(transferInCont,1);
        redisService.del(transferInCont);
    }
}
