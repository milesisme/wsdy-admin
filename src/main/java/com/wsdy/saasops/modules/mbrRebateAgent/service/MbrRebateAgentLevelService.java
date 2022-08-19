package com.wsdy.saasops.modules.mbrRebateAgent.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.modules.mbrRebateAgent.entity.MbrRebateAgentLevel;
import com.wsdy.saasops.modules.mbrRebateAgent.mapper.MbrRebateAgentLevelMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrActivityLevel;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class MbrRebateAgentLevelService {
    @Autowired
    private MbrRebateAgentLevelMapper mbrRebateAgentLevelMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;

    public List<MbrRebateAgentLevel> getMbrAgentLevelList(){
        List<MbrRebateAgentLevel> ret =  mbrRebateAgentLevelMapper.getMbrAgentLevelList();
        return ret;
    }

    public void insertMbrAgentLevel(MbrRebateAgentLevel dto){
        // 等级校验
        checkoutAgentLevel(dto);
        mbrRebateAgentLevelMapper.insert(dto);
    }

    public void deleteMbrAgentLevel(MbrRebateAgentLevel dto){
        MbrAccount account = new MbrAccount();
        account.setAgyLevelId(dto.getId());
        int count = mbrAccountMapper.selectCount(account);
        if (count > 0) {
            throw new R200Exception("该级别下有会员存在，不允许删除，如要删除，请把该级别的会员移到别的级别下");
        }
        // 0级代理不删除
        MbrRebateAgentLevel qryDto = mbrRebateAgentLevelMapper.selectByPrimaryKey(dto.getId());
        if(Objects.isNull(qryDto)){
            return;
        }
        if(qryDto.getAccountLevel() == Constants.EVNumber.zero){
            throw new R200Exception("0级会员代理不可删除");
        }
        mbrRebateAgentLevelMapper.deleteByPrimaryKey(dto.getId());
    }

    public void updateMbrAgentLevel(MbrAccount dto){
        // 查询旧的等级
        MbrAccount old = mbrAccountMapper.selectByPrimaryKey(dto.getId());
        // 更新新的等级
        MbrAccount updateMa = new MbrAccount();
        updateMa.setId(dto.getId());
        updateMa.setAgyLevelId(dto.getAgyLevelId());
        mbrAccountMapper.updateByPrimaryKeySelective(updateMa);
        // 记录日志
        mbrAccountLogService.updateMbrAgentLevel(updateMa, old);
    }

    private void checkoutAgentLevel(MbrRebateAgentLevel dto) {
        MbrRebateAgentLevel qryDto = new MbrRebateAgentLevel();
        qryDto.setAccountLevel(dto.getAccountLevel());
        int count = mbrRebateAgentLevelMapper.selectCount(qryDto);
        if (count > 0) {
            throw new R200Exception("等级已经存在");
        }
    }
}
