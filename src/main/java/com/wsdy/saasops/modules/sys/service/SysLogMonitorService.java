package com.wsdy.saasops.modules.sys.service;

import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.sys.dao.SysLogMonitorDao;
import com.wsdy.saasops.modules.sys.entity.SysLogMonitorEntity;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 系统用户
 */
@Slf4j
@Service("SysLogMonitorService")
public class SysLogMonitorService {

    @Autowired
    private SysLogMonitorDao logMonitorDao;

    public PageUtils queryList(SysLogMonitorEntity sysLogMonitorEntity) {
        PageHelper.startPage(sysLogMonitorEntity.getPageNo(), sysLogMonitorEntity.getPageSize());
        List<SysLogMonitorEntity> list = logMonitorDao.queryList(sysLogMonitorEntity);
        PageUtils p = BeanUtil.toPagedResult(list);
        return p;
    }

}
