package com.wsdy.saasops.modules.system.pay.dao;

import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicQrCodeBank;
import com.wsdy.saasops.modules.system.pay.entity.SetBasicQrCodeGroup;
import com.wsdy.saasops.modules.system.pay.entity.SysQrCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysQrCodeMapper extends MyMapper<SysQrCode> {

    List<SysQrCode> queryList(SysQrCode sysQrCode);

    int batchInsertQrCodeBank(@Param("qrCodeBanks") List<SetBasicQrCodeBank> qrCodeBanks);

    int deleteQrCodeBank(@Param("qrCodeId") Integer qrCodeId);

    int batchInsertQrCodeGroup(@Param("qrCodeGroups") List<SetBasicQrCodeGroup> qrCodeGroups);

    int deleteQrCodeGroup(SetBasicQrCodeGroup qrCodeGroup);

    List<SetBasicQrCodeGroup> getQrCodeGroupIsQueue();

    List<SysQrCode> findQrCodeList(@Param("groupId") Integer groupId);

    List<BaseBank> findBankList(@Param("qrCodeId") Integer qrCodeId);

    List<SetBasicQrCodeGroup> getQrCodeGroup(SetBasicQrCodeGroup qrCodeGroup);

    List<SysQrCode> findQrCodeListWithSelected(SysQrCode qrCode);

    List<MbrGroup> findGroupById(
            @Param("qrCodeId") Integer fastPayId);

}
