package com.wsdy.saasops.modules.sys.service;


import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.WarningConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.member.dto.WarningLogDto;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrSysWarningService;
import com.wsdy.saasops.modules.sys.dao.SysWarningMapper;
import com.wsdy.saasops.modules.sys.dto.SysWarningDealWithDto;
import com.wsdy.saasops.modules.sys.dto.SysWarningDto;
import com.wsdy.saasops.modules.sys.dto.SysWarningQueryDto;
import com.wsdy.saasops.modules.sys.entity.SysWarning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SysWarningService {
    @Autowired
    private SysWarningMapper sysWarningMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;

    public PageUtils pageList(SysWarningQueryDto sysWarningQueryDto){
        PageHelper.startPage(sysWarningQueryDto.getPageNo(), sysWarningQueryDto.getPageSize());
        List<SysWarningDto> list = sysWarningMapper.list(sysWarningQueryDto);
        return BeanUtil.toPagedResult(list);
    }


    public int dealWith(SysWarningDealWithDto sysWarningQueryDto, String dealUser){
        SysWarning s = new SysWarning();
        s.setId(sysWarningQueryDto.getId());
        SysWarning sysWarning = sysWarningMapper.selectOne(s);

        if(Objects.isNull(sysWarning)){
                throw new R200Exception("该预警信息不存在!");
        }

        if(sysWarning.getStatus() == Constants.EVNumber.one){
            throw new R200Exception("带信息已处理，无法重复处理!");
        }
        sysWarning.setDealTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        sysWarning.setDealUser(dealUser);
        sysWarning.setStatus(Constants.EVNumber.one);
        sysWarning.setMemo(sysWarningQueryDto.getMemo());
        return sysWarningMapper.updateByPrimaryKey(sysWarning);
    }

    public void bindWarning(MbrAccount mbrAccount, String typeContent){
        int days = DateUtil.daysBetween(mbrAccount.getRegisterTime(), DateUtil.getCurrentDate(DateUtil.FORMAT_10_DATE));
        if(days > WarningConstants.REGISTER_DAY){
            String content = String.format(WarningConstants.BIND_TMP,WarningConstants.REGISTER_DAY, typeContent);
            mbrAccountLogService.addWarningLog(new WarningLogDto(mbrAccount.getLoginName(), null, content, Constants.EVNumber.six));
        }
    }

    public SysWarningDto getSysWarningByLoginNameAndType(String loginName, Integer type){
        return sysWarningMapper.getSysWarningByLoginNameAndType( loginName,  type);
    }
}
