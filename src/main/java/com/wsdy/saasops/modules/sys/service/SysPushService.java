package com.wsdy.saasops.modules.sys.service;


import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.Constant;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.sys.dao.SysPushMapper;
import com.wsdy.saasops.modules.sys.dto.SysPushDto;
import com.wsdy.saasops.modules.sys.entity.SysPush;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Objects;

@Slf4j
@Service
public class SysPushService {

    @Autowired
    private SysPushMapper sysPushMapper;

    @Value("${jpush.APP_KEY}")
    private String APP_KEY;
    @Value("${jpush.MASTER_SECRET}")
    private String MASTER_SECRET;

    public SysPushDto getByType(Integer type){


        Example example = new Example(SysPush.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("type",type);

        SysPush sp = sysPushMapper.selectOneByExample(example);
        SysPushDto sysPushDto = new SysPushDto();

        if(Objects.isNull(sp)){

            // 初始化配置
            sysPushDto.setPushKey(APP_KEY);
            sysPushDto.setSecret(MASTER_SECRET);
            sysPushDto.setType(Constants.EVNumber.one);
            sp = save(sysPushDto);
        }
        BeanUtils.copyProperties(sp, sysPushDto);
        return sysPushDto;
    }

    public SysPush save(SysPushDto sysPushDto){

        Example example = new Example(SysPush.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("type",sysPushDto.getType());
        SysPush sp = sysPushMapper.selectOneByExample(example);
        if(Objects.nonNull(sp)){
          throw new R200Exception("该类型已经存在不能重复添加");
        }
        SysPush sysPush = new SysPush();
        sysPush.setSecret(sysPushDto.getSecret());
        sysPush.setType(Constants.EVNumber.one);
        sysPush.setPushKey(sysPushDto.getPushKey());
        sysPush.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        sysPush.setCreator(sysPushDto.getCreator());
        sysPushMapper.insert(sysPush);
        return sysPush;
    }

    public SysPush update(SysPushDto sysPushDto){
        SysPush sysPush = new SysPush();
        sysPush.setSecret(sysPushDto.getSecret());
        sysPush.setPushKey(sysPushDto.getPushKey());
        sysPush.setId(sysPushDto.getId());
        sysPush.setType(sysPushDto.getType());
        sysPush.setCreator(sysPushDto.getCreator());
        sysPush.setCreateTime(sysPushDto.getCreateTime());
        sysPush.setUpdateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        sysPush.setUpdater(sysPushDto.getCreator());
        sysPushMapper.updateByPrimaryKey(sysPush);
       return sysPush;
    }
}
