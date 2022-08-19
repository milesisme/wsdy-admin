package com.wsdy.saasops.modules.base.service;

import java.util.List;

import com.wsdy.saasops.modules.base.mapper.BaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wsdy.saasops.modules.base.dao.BaseBankMapper;
import com.wsdy.saasops.modules.base.entity.BaseBank;

@Service
public class BaseBankService {

    @Autowired
    private BaseBankMapper baseBankMapper;

    @Autowired
    private BaseMapper baseMapper;

    public BaseBank queryObject(Integer id) {
        return baseBankMapper.selectByPrimaryKey(id);
    }

    public List<BaseBank> selectAll() {
        BaseBank baseBank = new BaseBank();
        baseBank.setWDEnable((byte) 1);
        return baseBankMapper.select(baseBank);
    }

    public List<BaseBank> payBankList(Integer payId) {
        List<BaseBank> bankList = baseMapper.findBankList(payId);
        return bankList;
    }

    public List<BaseBank> select(BaseBank b) {
        return baseBankMapper.select(b);
    }

    public BaseBank selectOne(BaseBank b) {
        return baseBankMapper.selectOne(b);
    }

    public List<BaseBank> qrList(){
        BaseBank baseBank = new BaseBank();
        baseBank.setWDEnable((byte) 8);
        return baseBankMapper.select(baseBank);
    }
}
