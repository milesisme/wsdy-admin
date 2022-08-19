package com.wsdy.saasops.modules.member.service;

import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrAccountMobileMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrAccountMobile;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Service
@Transactional
public class MbrAccountMobileService {

    @Autowired
    private MbrAccountMobileMapper accountMobileMapper;
    @Autowired
    private MbrMapper mbrMapper;

    public void addMbrAccountMobile(MbrAccount account,String userName) {
       // if (StringUtils.isNotEmpty(account.getMobile())) {
            //绑定电话卡之前先判断会员之前有没有绑定,假如有则将最近一条加解绑时间

            MbrAccountMobile old = mbrMapper.queryLastOneByAccount(account.getId());
            if (Objects.nonNull(old)){
                old.setUpdater(userName);
                old.setUpdateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                accountMobileMapper.updateByPrimaryKeySelective(old);
            }
            MbrAccountMobile mobile = mbrMapper.queryByAccountAndUpdateIsNull(account.getId(),account.getMobile());
            if (null==mobile||!account.getMobile().equals("")) {
                MbrAccountMobile accountMobile = new MbrAccountMobile();
                accountMobile.setAccountId(account.getId());
                accountMobile.setLoginName(account.getLoginName());
                accountMobile.setMobile(account.getMobile());
                accountMobile.setUpdater(userName);
                accountMobile.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                accountMobileMapper.insert(accountMobile);
            }
      //  }
    }

    public void checkAccountMobile(String mobile) {
        if (StringUtils.isNotEmpty(mobile)) {
            MbrAccountMobile accountMobile = new MbrAccountMobile();
            accountMobile.setMobile(mobile);
            int count = accountMobileMapper.selectCount(accountMobile);
            if (count > 0) {
                throw new R200Exception("手机号已存在,请更换手机号码！");
            }
        }
    }

}
