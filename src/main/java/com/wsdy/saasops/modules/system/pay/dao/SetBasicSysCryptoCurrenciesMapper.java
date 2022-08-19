package com.wsdy.saasops.modules.system.pay.dao;

import com.wsdy.saasops.api.modules.pay.dto.CrPayLogoDto;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicCryptoCurrenciesBank;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicCryptoCurrenciesGroup;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicSysCryptoCurrencies;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SetBasicSysCryptoCurrenciesMapper extends MyMapper<SetBasicSysCryptoCurrencies> {
    int batchInsertCrGroup(@Param("crGroups") List<SetBasicCryptoCurrenciesGroup> crGroups);
    List<SetBasicCryptoCurrenciesGroup> getCrGroupIsQueue();
    int batchInsertCrBank(@Param("crBanks") List<SetBasicCryptoCurrenciesBank> crBanks);
    List<SetBasicSysCryptoCurrencies> queryList(SetBasicSysCryptoCurrencies setBasicSysCryptoCurrencies);
    List<MbrGroup> findGroupById(@Param("currenciesId") Integer currenciesId);
    int deleteCrBank(@Param("crId") Integer crId);
    int deleteCrGroup(SetBasicCryptoCurrenciesGroup crGroup);
    int deleteCrGroupEx(SetBasicCryptoCurrenciesGroup crGroup);
    List<BaseBank> findBankList(@Param("currenciesId") Integer crId);
    List<SetBasicSysCryptoCurrencies> findCrListWithSelected(SetBasicSysCryptoCurrencies cr);
    List<SetBasicCryptoCurrenciesGroup> getCrGroup(SetBasicCryptoCurrenciesGroup qrCodeGroup);
    List<SetBasicSysCryptoCurrencies> findCrList(@Param("groupId") Integer groupId);

    List<CrPayLogoDto> getCrLogo();

    SetBasicSysCryptoCurrencies getCrByCodeAndProtocol(@Param("currencyCode") String currencyCode, @Param("currencyProtocol") String currencyProtocol);

    SetBasicSysCryptoCurrencies getCrByCodeAndProtocolFromManage(@Param("currencyCode") String currencyCode, @Param("currencyProtocol") String currencyProtocol);

    SetBasicSysCryptoCurrencies queryById(@Param("id") Integer id);

    Integer qryBankAndWalletSumById(@Param("id") Integer id);
}
