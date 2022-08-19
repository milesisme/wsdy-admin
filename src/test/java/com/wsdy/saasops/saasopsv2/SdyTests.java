package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.aff.dto.AccountDepositRequestDto;
import com.wsdy.saasops.aff.dto.AccountListRequestDto;
import com.wsdy.saasops.aff.dto.AddBalanceDto;
import com.wsdy.saasops.aff.dto.CreateUserDto;
import com.wsdy.saasops.aff.service.SdyDataService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;


@RunWith(SpringRunner.class)
@SpringBootTest
public class SdyTests {

    @Autowired
    private SdyDataService sdyDataService;

    @Test
    public void findAdjustBonus() {
        sdyDataService.findAdjustBonus(new AccountDepositRequestDto(),1,100);
    }

    @Test
    public void createAffiliate() {
        CreateUserDto dto = new CreateUserDto();
        dto.setPass("123123");
        dto.setMembercode("test2356");
        dto.setDomain("127.0.0.1");
        dto.setAffiliatecode("HUJU90");
        sdyDataService.createAffiliate(dto);
    }

    @Test
    public void accountList() {

        AccountListRequestDto dto = new AccountListRequestDto();
        dto.setAgentAccount("egagent");
        sdyDataService.accountList(1,10,dto);
    }


    @Test
    public void depositAndWithdrawalList() {
        AccountDepositRequestDto dto = new AccountDepositRequestDto();
        dto.setMembercode("test2366");
        sdyDataService.depositAndWithdrawalList(1,30,dto);
    }

    @Test
    public void auditAndBonusList() {
        AccountDepositRequestDto dto = new AccountDepositRequestDto();
        dto.setMembercode("test5588");
        sdyDataService.auditAndBonusList(1,30,dto);
    }

    @Test
    public void addBalance() {
        AddBalanceDto dto = new AddBalanceDto();
        dto.setAmount(new BigDecimal(66));
        dto.setMembercode("jim1111");
        sdyDataService.addBalance(dto);
    }
}
