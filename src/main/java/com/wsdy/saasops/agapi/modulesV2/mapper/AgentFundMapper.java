package com.wsdy.saasops.agapi.modulesV2.mapper;

import com.wsdy.saasops.agapi.modulesV2.dto.AgentFundDto;
import com.wsdy.saasops.agapi.modulesV2.dto.BillRecordListDto;
import com.wsdy.saasops.modules.agent.entity.AgentAccount;
import com.wsdy.saasops.modules.member.entity.MbrBillManage;
import com.wsdy.saasops.modules.operate.entity.TGmGame;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentFundMapper {

    List<AgentFundDto> agentFundList(AgentFundDto fundDto);

    List<AgentFundDto> accountFundList(AgentFundDto fundDto);

    List<MbrBillManage> findMbrBillManageList(MbrBillManage fundBillReport);

    String findChildnodeid(@Param("parentId") Integer parentId);

    AgentAccount findAgyAccount(@Param("agyAccount") String agyAccount,
                                @Param("parentId") Integer parentId);

    List<BillRecordListDto> billRecordList(BillRecordListDto recordListDto);

    List<TGmGame> egGameNameList(@Param("depotName") String depotName);
}
