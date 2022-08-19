package com.wsdy.saasops.modules.member.service;

import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.validator.Assert;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrMemoMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrMemo;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class MbrMemoService extends BaseService<MbrMemoMapper, MbrMemo> {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrMemoMapper mbrMemoMapper;
    @Autowired
    private MbrAccountLogService accountLogService;
    @Autowired
    private MbrAccountService accountService;

    public List<MbrMemo> queryListPage(MbrMemo mbrMemo) {
        return mbrMapper.queryAccountMemoList(mbrMemo);
    }

    public PageUtils queryListPageAll(MbrMemo mbrMemo, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrMemo> list = mbrMapper.queryAccountMemoListAll(mbrMemo);
        return BeanUtil.toPagedResult(list);
    }

    public Map sortList(Integer accountId, Integer roleId, Integer pageNo, Integer pageSize) {
        Map<String, Object> parmar = new HashMap<>(2);
        List<MbrMemo> memoList = mbrMapper.queryAccountSortMemo(accountId);
        parmar.put("sort", memoList);
        MbrMemo mbrMemo = new MbrMemo();
        mbrMemo.setRoleId(roleId);
        mbrMemo.setAccountId(accountId);
        PageHelper.startPage(pageNo, pageSize);
        List<MbrMemo> mbrMemos = mbrMemoMapper.select(mbrMemo);
        parmar.put("sortPage", BeanUtil.toPagedResult(mbrMemos));
        return parmar;
    }

    public void deleteBatch(List<Integer> ids) {
        mbrMapper.deleteMemoBatch(ids);
    }

    public void saveMbrMemo(MbrMemo mbrMemo, SysUserEntity user,String ip){
        mbrMemo.setMarkName(user.getUsername());
        verifyMemo(mbrMemo);
        mbrMemo.setRoleId(user.getRoleId());
        save(mbrMemo);
        // add modifyTime
        MbrAccount mbrAccount = new MbrAccount();
        mbrAccount.setId(mbrMemo.getAccountId());
        mbrAccount.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        accountService.updateModifyTime(mbrAccount);
        accountLogService.addAccountMemo(getUser().getUsername(), mbrMemo,ip);
    }

    public void updateMbrMemo(MbrMemo mbrMemo){
        verifyMemo(mbrMemo);
        update(mbrMemo);
    }

    private void verifyMemo(MbrMemo mbrMemo) {
        Assert.isNull(mbrMemo.getAccountId(), "会员ID不能为空!");
        Assert.isBlank(mbrMemo.getMemoTime(), "备注时间不能为空!");
        Assert.isBlank(mbrMemo.getOprUserName(), "备注操作员不能为空!");
        Assert.isBlank(mbrMemo.getMemo(), "备注内容不能为空!");
        Assert.isLenght(mbrMemo.getMemo(), "备注内容长度最大为200字符!", 1, 200);
    }
}
