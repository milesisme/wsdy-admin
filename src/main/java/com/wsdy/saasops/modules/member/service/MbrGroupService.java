package com.wsdy.saasops.modules.member.service;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.Constants.Available;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.dao.MbrRebateMapper;
import com.wsdy.saasops.modules.member.dto.RebateCatDto;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.member.entity.MbrRebate;
import com.wsdy.saasops.modules.member.mapper.MbrMapper;
import com.wsdy.saasops.modules.sys.dao.SysUserMbrgrouprelationMapper;
import com.wsdy.saasops.modules.sys.entity.SysUserMbrgrouprelation;
import com.wsdy.saasops.modules.system.systemsetting.entity.SysSetting;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;


@Service
@Transactional
public class MbrGroupService extends BaseService<MbrGroupMapper, MbrGroup> {

    @Autowired
    private MbrMapper mbrMapper;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private MbrWithdrawalCondService mbrWithdrawalCondService;
    @Autowired
    private SysUserMbrgrouprelationMapper mbrgrouprelationMapper;
    @Autowired
    private MbrRebateMapper rebateMapper;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private MbrGroupMapper mbrGroupMapper;

    public PageUtils queryListPage(MbrGroup mbrGroup, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<MbrGroup> list = mbrMapper.findGroupList(mbrGroup);
        if (null!=list&&list.size()>0){
            for (MbrGroup g:list) {
                if (StringUtils.isEmpty(g.getWLowUsdt())&&null!=g.getWLowQuota()){
                    g.setWLowUsdt(g.getWLowQuota());
                }
                if (StringUtils.isEmpty(g.getWTopUsdt())&&null!=g.getWTopQuota()){
                    g.setWTopUsdt(g.getWTopQuota());
                }
            }
        }
        return BeanUtil.toPagedResult(list);
    }

    public List<Integer> getAllMbrGroupIds() {
        return mbrMapper.getAllMbrGroupIds();
    }

    public void deleteBatch(Long[] idArr) {
        Arrays.stream(idArr).forEach(id -> {
            checkAgent(id.intValue(),Available.disable);
        });
        if (mbrMapper.selectGroupCount(idArr) == 0) {
            mbrMapper.deleteGroupBatch(idArr);
        }
        SysUserMbrgrouprelation mbrgrouprelation = new SysUserMbrgrouprelation();
        mbrgrouprelation.setMbrGroupId(idArr[0].intValue());
        mbrgrouprelationMapper.delete(mbrgrouprelation);

        MbrRebate rebate = new MbrRebate();
        rebate.setGroupId(idArr[0].intValue());
        rebateMapper.delete(rebate);
    }

    public MbrGroup selectMbrGroupById(Long[] idArr) {
        return mbrMapper.selectMbrGroupById(idArr[0]);
    }
    
    /**
     * 	根据id查询会员组
     * @param id
     * @return
     */
    public MbrGroup selectById(Integer id) {
    	return mbrGroupMapper.selectByPrimaryKey(id);
    }

    public int updateGroupAvil(Integer id, Byte available) {
        int mod = 0;
        MbrGroup mbrGroup = new MbrGroup();
        mbrGroup.setId(id);
        mbrGroup.setAvailable(available);
        mbrGroup.setIsDef(Available.disable);
        if (mbrGroup.getAvailable() == Available.enable) {
            int deposit = mbrDepositCondService.selectCountNo(mbrGroup.getId());
            int withDrawl = mbrWithdrawalCondService.selectCountNo(mbrGroup.getId());
            if (deposit > 0 && withDrawl > 0) {
                checkMember(id, available);
                checkAgent(id, available);
                mod = mbrMapper.updateGroupAvil(mbrGroup);
            }
        } else {
            checkMember(id, available);
            checkAgent(id, available);
            mod = mbrMapper.updateGroupAvil(mbrGroup);
        }
        return mod;
    }

    @Override
    public int save(MbrGroup group) {
        if (super.selectCount(group) > 0) {
            throw new R200Exception("会员组名称已存在,请不要增加相同名称会员组!");
        }
        return super.save(group);
    }

    public void checkMember(Integer groupId, Byte available) {
        if (available == Available.disable && (mbrMapper.countGroupMem(groupId) > 0)) {
            throw new R200Exception("请移出该会员组下会员后,方可使用禁用功能!");
        }
    }

    public void checkAgent(Integer groupId, Byte available){
        if (available == Available.disable && (mbrMapper.countGroupAgent(groupId) > 0)) {
            throw new R200Exception("请修改代理与会员组的关联,方可使用此功能!");
        }
    }

    public void rebateSave(MbrRebate rebate, String userName, String ip) {
        MbrRebate mbrRebate = new MbrRebate();
        mbrRebate.setGroupId(rebate.getGroupId());
        int count = rebateMapper.selectCount(mbrRebate);
        if (count > 0) {
            MbrRebate mbrRebate1 = new MbrRebate();
            mbrRebate1.setGroupId(rebate.getGroupId());
            mbrRebate1.setIsShow(Constants.EVNumber.zero);
            rebateMapper.delete(mbrRebate1);
        }
        rebate.setId(null);
        rebate.setIsShow(Constants.EVNumber.zero);
        rebate.setIsCast(Constants.EVNumber.zero);
        rebateInsert(rebate, userName);

        //添加操作日志
        mbrAccountLogService.updateAccountGroupRebate(rebate, userName, ip);
    }

    private void rebateInsert(MbrRebate rebate, String userName) {
        rebate.setCreateUser(userName);
        rebate.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        rebate.setRebateText(JSON.toJSONString(rebate.getRebateCatDtos()));
        rebateMapper.insert(rebate);
    }

    public MbrRebate rebateInfo(Integer groupId) {
        MbrRebate mbrRebate1 = new MbrRebate();
        mbrRebate1.setGroupId(groupId);
        List<MbrRebate> rebates = rebateMapper.select(mbrRebate1);
        MbrRebate rebate = new MbrRebate();
        rebate.setGroupId(groupId);
        if (rebates.size() == 1) {
            MbrRebate rebate1 = rebates.get(0);
            rebate.setIsShow(rebate1.getIsShow());
        } else {
            rebate.setIsShow(Constants.EVNumber.zero);
        }
        MbrRebate mbrRebate = rebateMapper.selectOne(rebate);
        if (nonNull(mbrRebate)) {
            mbrRebate.setRebateCatDtos(JSON.parseArray(mbrRebate.getRebateText(), RebateCatDto.class));
        }
        return mbrRebate;
    }

    public void updateRebateShow() {
        List<MbrRebate> mbrRebateList = rebateMapper.selectAll();
        if (Collections3.isNotEmpty(mbrRebateList)) {
            Map<Integer, List<MbrRebate>> rebateGroupingBy =
                    mbrRebateList.stream().collect(
                            Collectors.groupingBy(MbrRebate::getGroupId));
            for (Integer groupIdKey : rebateGroupingBy.keySet()) {
                List<MbrRebate> rebateList = rebateGroupingBy.get(groupIdKey);
                if (rebateList.size() > 1) {
                    for (MbrRebate rebate : rebateList) {
                        if (rebate.getIsCast() == Constants.EVNumber.zero) {
                            rebate.setIsShow(Constants.EVNumber.one);
                            rebate.setIsCast(Constants.EVNumber.one);
                            rebateMapper.updateByPrimaryKey(rebate);
                        } else {
                            rebateMapper.deleteByPrimaryKey(rebate.getId());
                        }
                    }
                } else {
                    MbrRebate rebate = rebateList.get(0);
                    rebate.setIsShow(Constants.EVNumber.one);
                    rebate.setIsCast(Constants.EVNumber.one);
                    rebateMapper.updateByPrimaryKey(rebate);
                }
            }
        }
    }


    public void bankDifferentName(MbrGroup group) {
        SysSetting enableSet = new SysSetting();
        enableSet.setSyskey(SystemConstants.BANK_DIFFERENT_NAME_ENABLE);
        enableSet.setSysvalue(group.getBankDifferentName()+"");
        sysSettingService.update(enableSet);
        SysSetting numberSet = new SysSetting();
        numberSet.setSyskey(SystemConstants.BANK_DIFFERENT_NAME_NUMBER);
        numberSet.setSysvalue(group.getBankDifferentNumber()+"");
        sysSettingService.update(numberSet);

        if (nonNull(group.getId())) {
            // 将之前设置的所有会员组允许不同名全部关闭
            mbrGroupMapper.updateDifferentNameOff();
            mbrGroupMapper.updateByPrimaryKeySelective(group);
        }
    }

	/**
	 * 	根据用户id获取当前会员的会员组
	 * 
	 * @param userId
	 * @return
	 */
	public MbrGroup getGroupByUserId(Integer userId) {
		return mbrGroupMapper.getGroupByUserId(userId);
	}
}
