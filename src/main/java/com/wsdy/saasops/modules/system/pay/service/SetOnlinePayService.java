package com.wsdy.saasops.modules.system.pay.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.BigDecimalMath;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.system.pay.dao.*;
import com.wsdy.saasops.modules.system.pay.dto.*;
import com.wsdy.saasops.modules.system.pay.entity.*;
import com.wsdy.saasops.modules.system.pay.mapper.PayMapper;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Service
@Transactional
public class SetOnlinePayService{

    @Autowired
    private SetBacicOnlinepayMapper onlinepayMapper;
    @Autowired
    private SysDepositMapper depositMapper;
    @Autowired
    private PayMapper payMapper;
    @Autowired
    private MbrGroupMapper groupMapper;
    @Autowired
    private SetBasicSysDepMbrMapper sysDepMbrMapper;
    @Autowired
    private SetBasicPaymbrGroupRelationMapper onlineGroupMapper;
    @Autowired
    private SetBacicFastPayGroupMapper fastPayGroupMapper;
    @Autowired
    private SysDepositService sysDepositService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private SetBacicFastPayMapper setBacicFastPayMapper;
    @Autowired
    private SetBacicOnlinepayMapper setBacicOnlinepayMapper;
    @Autowired
    private SysDepositMapper sysDepositMapper;
    @Autowired
    private SysQrCodeMapper sysQrCodeMapper;
    @Autowired
    private SetBasicSysCryptoCurrenciesMapper setBasicSysCryptoCurrenciesMapper;


    /**
     * 	线上支付List
     * 
     * @param onlinepay
     * @return
     */
    public List<SetBacicOnlinepay> queryList(SetBacicOnlinepay onlinepay) {
        onlinepay.setIsDelete(Constants.EVNumber.zero);
        List<SetBacicOnlinepay> onlinepays = payMapper.findOnlinePayList(onlinepay);
        if (Collections3.isNotEmpty(onlinepays)) {
            onlinepays.forEach(d -> d.setGroupList(payMapper.findOnlineGroupByDepositId(d.getId())));
        }
        return onlinepays;
    }

    /**
     * 	在线支付更新状态
     * 
     * @param onlinepay
     * @param userName
     * @param ip
     */
    public void updateStatus(SetBacicOnlinepay onlinepay, String userName, String ip) {
        SetBacicOnlinepay bacicOnlinepay = onlinepayMapper.selectByPrimaryKey(onlinepay.getId());
        if (bacicOnlinepay == null) {
        	throw new R200Exception("当前数据不存在，请重新刷新页面");
        }
        
    	// isHot，isRecommend 只有一个为true
		if (onlinepay.getIsHot() != null && onlinepay.getIsRecommend() != null && onlinepay.getIsHot()
				&& onlinepay.getIsRecommend()) {
			throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
		}
		
		bacicOnlinepay.setIsHot(onlinepay.getIsHot());
		bacicOnlinepay.setIsRecommend(onlinepay.getIsRecommend());
        bacicOnlinepay.setAvailable(onlinepay.getAvailable());
        bacicOnlinepay.setCreateUser(userName);
        bacicOnlinepay.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        onlinepayMapper.updateByPrimaryKey(bacicOnlinepay);

        //添加操作日志
        mbrAccountLogService.updateOnlinePayStatusLog(bacicOnlinepay, userName, ip);
    }

    /**
     * 	在线支付更新跳转方式
     *
     * @param onlinepay
     * @param userName
     * @param ip
     */
    public void updateJump(SetBacicOnlinepay onlinepay, String userName, String ip) {
        SetBacicOnlinepay basicOnlinepay = onlinepayMapper.selectByPrimaryKey(onlinepay.getId());
        if (basicOnlinepay == null) {
            throw new R200Exception("当前数据不存在，请重新刷新页面");
        }
        basicOnlinepay.setIsJump(onlinepay.getIsJump());
        onlinepayMapper.updateByPrimaryKey(basicOnlinepay);

        //添加操作日志
        mbrAccountLogService.updateOnlinePayStatusLog(basicOnlinepay, userName, ip);
    }

    public void onlinepayDelete(Integer id, String userName, String ip) {
        SetBacicOnlinepay onlinepay = onlinepayMapper.selectByPrimaryKey(id);
        onlinepay.setIsDelete(Constants.EVNumber.one);
        onlinepay.setAvailable(Constants.EVNumber.zero);
        onlinepay.setModifyUser(userName);
        onlinepay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        onlinepayMapper.updateByPrimaryKeySelective(onlinepay);

        SetBasicPaymbrGroupRelation relation = new SetBasicPaymbrGroupRelation();
        relation.setOnlinePayId(onlinepay.getId());
        onlineGroupMapper.delete(relation);

        //添加操作日志
        mbrAccountLogService.deleteOnlinePayLog(onlinepay, userName, ip);
    }

    public SetBacicOnlinepay onlinepayInfo(Integer id) {
        SetBacicOnlinepay onlinepay = new SetBacicOnlinepay();
        onlinepay.setId(id);
        onlinepay.setIsDelete(Constants.EVNumber.zero);
        List<SetBacicOnlinepay> onlinePayList = payMapper.findOnlinePayList(onlinepay);
        if (Collections3.isNotEmpty(onlinePayList)) {
            SetBacicOnlinepay bacicOnlinepay = onlinePayList.get(0);
            bacicOnlinepay.setGroupIds(getOnlinepayGroupIds(bacicOnlinepay.getId()));
            return bacicOnlinepay;
        }
        return null;
    }

    private List<Integer> getOnlinepayGroupIds(Integer onlinepayId) {
        SetBasicPaymbrGroupRelation groupRelation = new SetBasicPaymbrGroupRelation();
        groupRelation.setOnlinePayId(onlinepayId);
        List<SetBasicPaymbrGroupRelation> groupRelationList = onlineGroupMapper.select(groupRelation);
        if (Collections3.isNotEmpty(groupRelationList)) {
            return groupRelationList.stream().map(
                    st -> st.getGroupId()).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 	在线支付保存
     * 
     * @param onlinepay
     * @param userName
     * @param ip
     */
    public void onlinepaySave(SetBacicOnlinepay onlinepay, String userName, String ip) {
    	// isHot，isRecommend 只有一个为true
		if (onlinepay.getIsHot() != null && onlinepay.getIsRecommend() != null && onlinepay.getIsHot()
				&& onlinepay.getIsRecommend()) {
			throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
		}
        onlinepay.setIsDelete(Constants.EVNumber.zero);
        onlinepay.setCreateUser(userName);
        onlinepay.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        onlinepay.setDepositAmount(BigDecimal.ZERO);
        onlinepayMapper.insert(onlinepay);

        insertSetBasicPaymbrGroupRelationList(onlinepay);
        //添加操作日志
        mbrAccountLogService.addOnlinePayLog(onlinepay, userName, ip);
    }

    private void insertSetBasicPaymbrGroupRelationList(SetBacicOnlinepay onlinepay) {
        if (Collections3.isNotEmpty(onlinepay.getGroupIds())) {
            List<SetBasicPaymbrGroupRelation> groupRelations = Lists.newArrayList();
            List<SetBasicPaymbrGroupRelation> groupsPayIsQueue = payMapper.getOnLinePayGroupIsQueue(onlinepay.getPaymentType());
            for (int i = 0; i < onlinepay.getGroupIds().size(); i++) {
                SetBasicPaymbrGroupRelation groupRelation = new SetBasicPaymbrGroupRelation();
                Integer groupId = onlinepay.getGroupIds().get(i);
                groupRelation.setGroupId(groupId);
                groupRelation.setOnlinePayId(onlinepay.getId());
                groupRelation.setSort(i);
                groupRelation.setIsQueue(getIsQueue(groupId,groupsPayIsQueue));
                groupRelations.add(groupRelation);
            }
            onlineGroupMapper.insertList(groupRelations);
        }
    }

    private void deleteSetBasicPaymbrGroupRelation(Integer onlinePayId, Integer groupId) {
        SetBasicPaymbrGroupRelation groupRelation = new SetBasicPaymbrGroupRelation();
        groupRelation.setOnlinePayId(onlinePayId);
        groupRelation.setGroupId(groupId);
        onlineGroupMapper.delete(groupRelation);
    }

    /**
     * 	在线支付更新
     * 
     * @param onlinepay
     * @param userName
     * @param ip
     */
    public void onlinepayUpdate(SetBacicOnlinepay onlinepay, String userName, String ip) {
    	// isHot，isRecommend 只有一个为true
		if (onlinepay.getIsHot() != null && onlinepay.getIsRecommend() != null && onlinepay.getIsHot()
				&& onlinepay.getIsRecommend()) {
			throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
		}
        onlinepay.setModifyUser(userName);
        onlinepay.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        onlinepayMapper.updateByPrimaryKeySelective(onlinepay);

        List<Integer> onlinepayGroupIds = getOnlinepayGroupIds(onlinepay.getId());
        if (Objects.isNull(onlinepayGroupIds)) {
            insertSetBasicPaymbrGroupRelationList(onlinepay);
        } else {
            updateSetBasicPaymbrGroupRelationGroup(onlinepay, onlinepayGroupIds);
        }
        //添加操作日志
        mbrAccountLogService.updateOnlinePayLog(onlinepay, userName, ip);
    }

    private void updateSetBasicPaymbrGroupRelationGroup(SetBacicOnlinepay onlinepay, List<Integer> onlinepayGroupIds) {
        if (onlinepay.getGroupIds().size() == 0 && onlinepayGroupIds.size() > 0) {
            onlinepayGroupIds.stream().forEach(os -> deleteSetBasicPaymbrGroupRelation(onlinepay.getId(), os));
        }
        if (Collections3.isNotEmpty(onlinepay.getGroupIds())) {
            for (int i = 0; i < onlinepayGroupIds.size(); i++) {
                Boolean isDelete = Boolean.TRUE;
                for (int j = 0; j < onlinepay.getGroupIds().size(); j++) {
                    if (onlinepayGroupIds.get(i).equals(onlinepay.getGroupIds().get(j))) {
                        isDelete = Boolean.FALSE;
                        break;
                    }
                }
                if (Boolean.TRUE.equals(isDelete)) {
                    deleteSetBasicPaymbrGroupRelation(onlinepay.getId(), onlinepayGroupIds.get(i));
                }
            }
            List<SetBasicPaymbrGroupRelation> groupsPayIsQueue = payMapper.getOnLinePayGroupIsQueue(onlinepay.getPaymentType());
            for (int j = 0; j < onlinepay.getGroupIds().size(); j++) {
                Boolean isInsert = Boolean.TRUE;
                for (int i = 0; i < onlinepayGroupIds.size(); i++) {
                    if (onlinepay.getGroupIds().get(j).equals(onlinepayGroupIds.get(i))) {
                        isInsert = Boolean.FALSE;
                        break;
                    }
                }
                if (Boolean.TRUE.equals(isInsert)) {
                    SetBasicPaymbrGroupRelation groupRelation = new SetBasicPaymbrGroupRelation();
                    Integer groupId = onlinepay.getGroupIds().get(j);
                    groupRelation.setGroupId(groupId);
                    groupRelation.setOnlinePayId(onlinepay.getId());
                    groupRelation.setSort(payMapper.findPayMbrGroupRelationMaxSort(groupRelation.getGroupId()));
                    groupRelation.setIsQueue(getIsQueue(groupId,groupsPayIsQueue));
                    onlineGroupMapper.insert(groupRelation);
                }
            }
        }
    }

    private Integer getIsQueue(Integer groupId,List<SetBasicPaymbrGroupRelation> groupsPayIsQueue){
        //设置排队/平铺
        Optional<SetBasicPaymbrGroupRelation> groupRelationOptional = groupsPayIsQueue.stream().filter(groupIsQueue ->
                groupId.equals(groupIsQueue.getGroupId()))
                .findAny();
        if(groupRelationOptional.isPresent()){
            return groupRelationOptional.get().getIsQueue();
        }else{//默认排队
            return Constants.EVNumber.one;
        }
    }

    /**
     * 存就送活动获取支付方式
     */
    public Object findPayListForJDepositActivity(String siteCode) {
        List<TPay> payList = payMapper.findPayBySiteCode(siteCode, Lists.newArrayList(1, 2, 3, 4, 5, 7, 8));
        return payList;
    }

    public Object findPayList(String siteCode) {
        List<TPay> payList = payMapper.findPayBySiteCode(siteCode, Lists.newArrayList(1, 2, 3, 4, 5, 7, 8, 11,14,16,17,18, 19));
        if (Collections3.isNotEmpty(payList)) {
            payList.forEach(p -> p.setBaseBanks(payMapper.findBankByPayId(p.getId())));
            return payList.stream().collect(Collectors.groupingBy(TPay::getPaymentType));
        }
        return null;
    }

    public List<TPay> shortcutPayList(String siteCode) {
        return payMapper.findPayBySiteCode(siteCode, Lists.newArrayList(6, 9, 10, 13, 15));
    }

    public List<PayGroupListDto> findPayAllotList() {
        List<PayGroupListDto> dtoList = Lists.newArrayList();
        MbrGroup group = new MbrGroup();
        group.setAvailable((byte) Constants.EVNumber.one);
        List<MbrGroup> groupList = groupMapper.select(group);
        groupList.stream().forEach(p -> {
            PayGroupListDto groupListDto = new PayGroupListDto();
            groupListDto.setGroupId(p.getId());
            groupListDto.setGroupName(p.getGroupName());
            groupListDto.setPayListDto(getPayListDto(p.getId()));
            dtoList.add(groupListDto);
        });
        return dtoList;
    }

    public List<PayGroupListDto> findPayAllotListByGroupId(Integer groupId) {
        List<PayGroupListDto> dtoList = Lists.newArrayList();
        MbrGroup group = new MbrGroup();
        group.setAvailable((byte) Constants.EVNumber.one);
        List<MbrGroup> groupList = groupMapper.select(group);
        groupList.stream().forEach(p -> {
            PayGroupListDto groupListDto = new PayGroupListDto();
            groupListDto.setGroupId(p.getId());
            groupListDto.setGroupName(p.getGroupName());    // 前端需要获得所有的会员组id/name
            // 只查groupId的数据
            if(p.getId().equals(groupId)){
                groupListDto.setPayListDto(getPayListDto(p.getId()));
            }
            dtoList.add(groupListDto);
        });
        return dtoList;
    }

    private PayListDto getPayListDto(Integer groupId) {
        PayListDto dto = new PayListDto();
        List<SysDeposit> deposts = payMapper.findDepositAll();
        List<SysDeposit> groupDeposits = payMapper.findDepositByGroupId(groupId);
        dto.setLeftDeposits(groupDeposits);
        List<Integer> sysIds = deposts.stream().map(SysDeposit::getId).collect(Collectors.toList());
        List<Integer> groupIds = groupDeposits.stream().map(SysDeposit::getId).collect(Collectors.toList());
        List<Integer> subIds = Collections3.subtract(sysIds, groupIds);
        dto.setRightDeposits(deposts.stream().filter(x -> subIds.contains(x.getId())).collect(Collectors.toList()));

        List<SetBacicOnlinepay> onlinepays = payMapper.findOnlineList();
        List<SetBacicOnlinepay> groupOnlinepays = payMapper.findOnlineListByGroupId(groupId);
        dto.setLeftOnlinepays(groupOnlinepays);
        List<Integer> onlineIds = onlinepays.stream().map(SetBacicOnlinepay::getId).collect(Collectors.toList());
        List<Integer> onlineGroupIds = groupOnlinepays.stream().map(SetBacicOnlinepay::getId).collect(Collectors.toList());
        List<Integer> subOnlineIds = Collections3.subtract(onlineIds, onlineGroupIds);
        dto.setRightOnlinepays(onlinepays.stream().filter(x -> subOnlineIds.contains(x.getId())).collect(Collectors.toList()));

        SetBacicFastPay fastPay = new SetBacicFastPay();
        fastPay.setGroupId(groupId);
        fastPay.setIsSign(Boolean.FALSE);
        fastPay.setAvailable(Constants.EVNumber.one);
        fastPay.setIsAllocation(Boolean.FALSE);
        fastPay.setSortItem(3);
        fastPay.setSortBy("asc");
        dto.setLeftFastPays(sysDepositService.fastPayList(fastPay));
        fastPay.setIsSign(Boolean.TRUE);
        dto.setRightFastPay(sysDepositService.fastPayList(fastPay));

        SysQrCode qrCode = new SysQrCode();
        qrCode.setGroupId(groupId);
        List<SysQrCode> qrCodeList = sysQrCodeMapper.findQrCodeListWithSelected(qrCode);
        List<SysQrCode> leftList = qrCodeList.stream().filter(qr -> qr.getSelected() == 1).collect(Collectors.toList());
        List<SysQrCode> rightList = qrCodeList.stream().filter(qr -> qr.getSelected() == 0).collect(Collectors.toList());
        dto.setLeftQrCodes(leftList);
        dto.setRightQrCodes(rightList);

        // 数字货币
        SetBasicSysCryptoCurrencies cr = new SetBasicSysCryptoCurrencies();
        cr.setGroupId(groupId);
        List<SetBasicSysCryptoCurrencies> crList = setBasicSysCryptoCurrenciesMapper.findCrListWithSelected(cr);
        List<SetBasicSysCryptoCurrencies> leftListCr = crList.stream().filter(qr -> qr.getSelected() == 1).collect(Collectors.toList());
        List<SetBasicSysCryptoCurrencies> rightListCr = crList.stream().filter(qr -> qr.getSelected() == 0).collect(Collectors.toList());
        dto.setLeftCr(leftListCr);
        dto.setRightCr(rightListCr);

        return dto;
    }

    public void updateBankSort(AllotDto allotDto) {
        if (Collections3.isNotEmpty(allotDto.getSysDepMbrs())) {
            sysDepMbrMapper.insertList(allotDto.getSysDepMbrs());
        }
    }

    public void deleteSysDepMbr(AllotDto allotDto) {
        bankPayLog(allotDto);
        if (Collections3.isNotEmpty(allotDto.getSysDepMbrs())) {
            SetBasicSysDepMbr sysDepMbr = allotDto.getSysDepMbrs().get(0);
            MbrGroup group = groupMapper.selectByPrimaryKey(sysDepMbr.getGroupId());
            if (Objects.isNull(group)) {
                throw new R200Exception("会员组不存在");
            }
            deleteSysDepMbrEx(group.getId());
        } else {
            deleteSysDepMbrEx(allotDto.getGroupId());
        }
    }

    private void bankPayLog(AllotDto allotDto){
        Integer groupId = allotDto.getGroupId();
        MbrGroup group = groupMapper.selectByPrimaryKey(groupId);
        SetBasicSysDepMbr sysDepMbr = new SetBasicSysDepMbr();
        sysDepMbr.setGroupId(groupId);
        List<SetBasicSysDepMbr> sysDepMbrs = sysDepMbrMapper.select(sysDepMbr);
        List<Integer> depositIds = sysDepMbrs.stream().map(SetBasicSysDepMbr :: getDepositId).collect(Collectors.toList());
        List<Integer> diffList = null;
        Integer addOrDel = Constants.EVNumber.zero;
        if(CollectionUtils.isNotEmpty(allotDto.getSysDepMbrs()) &&
                (CollectionUtils.isEmpty(depositIds) || depositIds.size()<allotDto.getSysDepMbrs().size())){//添加 addOrDel = 1
            List<Integer> newBankIds = allotDto.getSysDepMbrs().stream().map(SetBasicSysDepMbr :: getDepositId).collect(Collectors.toList());
            diffList = getSubId(depositIds,newBankIds);
            addOrDel = Constants.EVNumber.one;
        }else if(CollectionUtils.isNotEmpty(depositIds) && (CollectionUtils.isEmpty(allotDto.getSysDepMbrs()) ||
                depositIds.size()>allotDto.getSysDepMbrs().size())){//删除 addOrDel = 0
            if(CollectionUtils.isEmpty(allotDto.getSysDepMbrs())){
                diffList = depositIds;
            }else{
                List<Integer> newDepositList = allotDto.getSysDepMbrs().stream().map(SetBasicSysDepMbr :: getDepositId).collect(Collectors.toList());
                diffList = getSubId(newDepositList,depositIds);
            }
            addOrDel = Constants.EVNumber.zero;
        }
        if(Collections3.isNotEmpty(diffList)) {
            SysDeposit deposit = sysDepositMapper.selectByPrimaryKey(diffList.get(0));
            mbrAccountLogService.updatePaySetLog(addOrDel, group.getGroupName(), deposit.getBankName() + deposit.getRealName());
        }else if(CollectionUtils.isNotEmpty(depositIds)){//调整顺序
            mbrAccountLogService.updatePaySetLog(Constants.EVNumber.three, group.getGroupName(), null);
        }
    }

    private void deleteSysDepMbrEx(Integer groupId) {
        SetBasicSysDepMbr depMbr = new SetBasicSysDepMbr();
        depMbr.setGroupId(groupId);
        payMapper.deleteSysDepMbrEx(depMbr);
    }

    public void updateOnlineSort(AllotDto allotDto) {
    	this.deleteOnlineSort(allotDto);
        if (Collections3.isNotEmpty(allotDto.getOnlineGroups())) {
            onlineGroupMapper.insertList(allotDto.getOnlineGroups());
        }
    }

    private List<Integer> getSubId(List<Integer> smallList,List<Integer> bigList){
        if(Collections3.isEmpty(smallList)) {
            return bigList;
        }
        return Collections3.subtract(bigList,smallList);
    }

    public void deleteOnlineSort(AllotDto allotDto) {
        onLinePayLog(allotDto);
        if (Collections3.isNotEmpty(allotDto.getOnlineGroups())) {
            SetBasicPaymbrGroupRelation groupRelation = allotDto.getOnlineGroups().get(0);
            MbrGroup group = groupMapper.selectByPrimaryKey(groupRelation.getGroupId());
            if (Objects.isNull(group)) {
                throw new R200Exception("会员组不存在");
            }
            deleteGroupRelationEx(group.getId());
        } else {
            deleteGroupRelationEx(allotDto.getGroupId());
        }
    }
    private void onLinePayLog(AllotDto allotDto){
        Integer groupId = allotDto.getGroupId();
        MbrGroup group = groupMapper.selectByPrimaryKey(groupId);
        SetBasicPaymbrGroupRelation depMbr = new SetBasicPaymbrGroupRelation();
        depMbr.setGroupId(groupId);
        List<SetBasicPaymbrGroupRelation> onlinePaySet = onlineGroupMapper.select(depMbr);
        List<Integer> onlinePayIds = onlinePaySet.stream().map(SetBasicPaymbrGroupRelation :: getOnlinePayId).collect(Collectors.toList());
        List<Integer> diffList = null;
        Integer addOrDel = Constants.EVNumber.zero;
        if(CollectionUtils.isNotEmpty(allotDto.getOnlineGroups()) && (CollectionUtils.isEmpty(onlinePayIds) ||
                onlinePayIds.size()<allotDto.getOnlineGroups().size())){//添加
            List<Integer> newOnlinePayList = allotDto.getOnlineGroups().stream().map(SetBasicPaymbrGroupRelation :: getOnlinePayId).collect(Collectors.toList());
            diffList = getSubId(onlinePayIds,newOnlinePayList);
            addOrDel = Constants.EVNumber.one;
        }else if(CollectionUtils.isNotEmpty(onlinePayIds) && (CollectionUtils.isEmpty(allotDto.getOnlineGroups()) ||
                onlinePayIds.size()>allotDto.getOnlineGroups().size())){
            if(CollectionUtils.isEmpty(allotDto.getOnlineGroups())){
                diffList = onlinePayIds;
            }else{
                List<Integer> newFastPayList = allotDto.getOnlineGroups().stream().map(SetBasicPaymbrGroupRelation :: getOnlinePayId).collect(Collectors.toList());
                diffList = getSubId(newFastPayList,onlinePayIds);
            }
        }
        if(Collections3.isNotEmpty(diffList)) {
            SetBacicOnlinepay pay = setBacicOnlinepayMapper.selectByPrimaryKey(diffList.get(0));
            mbrAccountLogService.updatePaySetLog(addOrDel, group.getGroupName(), pay.getName());
        }else if(CollectionUtils.isNotEmpty(onlinePayIds)){//调整顺序
            mbrAccountLogService.updatePaySetLog(Constants.EVNumber.three, group.getGroupName(), null);
        }
    }

    private void deleteGroupRelationEx(Integer groupId) {
        if(groupId == null){
            throw new R200Exception("会员组参数错误");
        }
        SetBasicPaymbrGroupRelation depMbr = new SetBasicPaymbrGroupRelation();
        depMbr.setGroupId(groupId);
        payMapper.deleteGroupRelationEx(depMbr);
    }

    public void updateQuota(PayQuotaDto quotaDto, String userName, String ip) {
        if (quotaDto.getQuotaType() == Constants.EVNumber.one
                || quotaDto.getQuotaType() == Constants.EVNumber.two) {
            SysDeposit deposit = depositMapper.selectByPrimaryKey(quotaDto.getId());
            deposit.setDayMaxAmout(quotaDto.getDayMaxAmout());
            deposit.setMinAmout(quotaDto.getMinAmout());
            deposit.setMaxAmout(quotaDto.getMaxAmout());
            depositMapper.updateByPrimaryKeySelective(deposit);
            if (quotaDto.getQuotaType() == Constants.EVNumber.two
                    && quotaDto.getIsShow() == Constants.EVNumber.one) {
                deposit.setIsShow(quotaDto.getIsShow());
            }
            depositMapper.updateByPrimaryKeySelective(deposit);

            //添加操作日志
            mbrAccountLogService.updateCompanyDepositLog2(deposit, userName, ip);
        }
        if (quotaDto.getQuotaType() == Constants.EVNumber.three) {
            SetBacicOnlinepay onlinepay = onlinepayMapper.selectByPrimaryKey(quotaDto.getId());
            onlinepay.setDayMaxAmout(quotaDto.getDayMaxAmout());
            onlinepay.setMinAmout(quotaDto.getMinAmout());
            onlinepay.setMaxAmout(quotaDto.getMaxAmout());
            onlinepayMapper.updateByPrimaryKeySelective(onlinepay);

            //添加操作日志
            mbrAccountLogService.updateOnlinePayLog2(onlinepay, userName, ip);
        }

        // 二维码限额修改
        if (quotaDto.getQuotaType() == Constants.EVNumber.four) {
            SysQrCode sysQrCode = sysQrCodeMapper.selectByPrimaryKey(quotaDto.getId());
            sysQrCode.setDayMaxAmout(quotaDto.getDayMaxAmout());
            sysQrCode.setMinAmout(quotaDto.getMinAmout());
            sysQrCode.setMaxAmout(quotaDto.getMaxAmout());
            sysQrCodeMapper.updateByPrimaryKeySelective(sysQrCode);

            //添加操作日志
            mbrAccountLogService.updateSysQrCodeLog2(sysQrCode, userName, ip);
        }

        // 数字货币限额修改
        if (quotaDto.getQuotaType() == Constants.EVNumber.five) {
            SetBasicSysCryptoCurrencies cr = setBasicSysCryptoCurrenciesMapper.selectByPrimaryKey(quotaDto.getId());
            cr.setMinAmout(quotaDto.getMinAmout());
            setBasicSysCryptoCurrenciesMapper.updateByPrimaryKeySelective(cr);

            //添加操作日志
            mbrAccountLogService.updateSysQrCodeLog3(cr);
        }

    }


    public List<SetBacicOnlinepay> querySetBacicOnlinepayList() {
        return payMapper.querySetBacicOnlinepayList();
    }

    public void updateFastPaySort(AllotDto allotDto) {
    	this.deleteFastPay(allotDto);
        if (Collections3.isNotEmpty(allotDto.getFastPayGroups())) {
            fastPayGroupMapper.insertList(allotDto.getFastPayGroups());
        }
    }

    public void deleteFastPay(AllotDto allotDto) {
        fastPayLog(allotDto);
        if (Collections3.isNotEmpty(allotDto.getSysDepMbrs())) {
            SetBasicFastPayGroup fastPayGroup = allotDto.getFastPayGroups().get(0);
            MbrGroup group = groupMapper.selectByPrimaryKey(fastPayGroup.getGroupId());
            if (Objects.isNull(group)) {
                throw new R200Exception("会员组不存在");
            }
            deletFastPayByIdEx(group.getId());
        } else {    // 按照会员组删除该会员组下的该自动入款
            deletFastPayByIdEx(allotDto.getGroupId());
        }
    }

    private void fastPayLog(AllotDto allotDto){
        Integer groupId = allotDto.getGroupId();
        MbrGroup group = groupMapper.selectByPrimaryKey(groupId);
        SetBasicFastPayGroup fastPayGroup = new SetBasicFastPayGroup();
        fastPayGroup.setGroupId(groupId);
        List<SetBasicFastPayGroup> fastPayList = fastPayGroupMapper.select(fastPayGroup);
        List<Integer> fastPayIds = fastPayList.stream().map(SetBasicFastPayGroup :: getFastPayId).collect(Collectors.toList());
        List<Integer> diffList = null;
        Integer addOrDel = Constants.EVNumber.zero;
        if(CollectionUtils.isNotEmpty(allotDto.getFastPayGroups()) &&
                (CollectionUtils.isEmpty(fastPayIds) || fastPayIds.size()<allotDto.getFastPayGroups().size())){//添加
            List<Integer> newFastPayList = allotDto.getFastPayGroups().stream().map(SetBasicFastPayGroup :: getFastPayId).collect(Collectors.toList());
            diffList = getSubId(fastPayIds,newFastPayList);
            addOrDel = Constants.EVNumber.one;
        }else if(CollectionUtils.isNotEmpty(fastPayIds) && (CollectionUtils.isEmpty(allotDto.getFastPayGroups()) ||
                fastPayIds.size()>allotDto.getFastPayGroups().size())){//减少
            if(CollectionUtils.isEmpty(allotDto.getFastPayGroups())){
                diffList = fastPayIds;
            }else{
                List<Integer> newFastPayList = allotDto.getFastPayGroups().stream().map(SetBasicFastPayGroup :: getFastPayId).collect(Collectors.toList());
                diffList = getSubId(newFastPayList,fastPayIds);
            }
        }
        if(Collections3.isNotEmpty(diffList)) {
            SetBacicFastPay pay = setBacicFastPayMapper.selectByPrimaryKey(diffList.get(0));
            mbrAccountLogService.updatePaySetLog(addOrDel, group.getGroupName(), pay.getName());
        }else if(CollectionUtils.isNotEmpty(fastPayIds)){//调整顺序
            mbrAccountLogService.updatePaySetLog(Constants.EVNumber.three, group.getGroupName(), null);
        }
    }

    // 删除会员组所分配的对应的自动入款平台：不含禁用的支付方式
    private void deletFastPayByIdEx(Integer groupId) {
        if(groupId == null){
            throw new R200Exception("会员组参数错误");
        }
        SetBasicFastPayGroup fastPayGroup = new SetBasicFastPayGroup();
        fastPayGroup.setGroupId(groupId);
        payMapper.deletFastPayByIdEx(fastPayGroup);
    }

    /**
     * 入款管理--成功率统计
     */
    public List<StatisticsSucRateRespDto> statisticSucRate(StatisticsSucRateDto statisticsSucRateDto) {
        List<StatisticsSucRateDto> list = payMapper.statisticSucRate(statisticsSucRateDto);
        // 分组统计
        Map<String, List<StatisticsSucRateDto>> depositGroupingBy =
                list.stream().collect(
                        Collectors.groupingBy(
                                StatisticsSucRateDto::getType));
        List<StatisticsSucRateRespDto> respList = new ArrayList<>();
        for (String type : depositGroupingBy.keySet()) {
            StatisticsSucRateRespDto resp = new StatisticsSucRateRespDto();

            List<StatisticsSucRateDto> deposits = depositGroupingBy.get(type);
            // 计算总笔数
            Optional<BigDecimal>  totalNumSum = deposits.stream().map(StatisticsSucRateDto::getTotalNum).reduce(BigDecimal::add);
            // 计算总成功笔数
            Optional<BigDecimal>  sucNumSum = deposits.stream().map(StatisticsSucRateDto::getSucNum).reduce(BigDecimal::add);
            // 计算总成功率
            BigDecimal sucRateSum = BigDecimal.ZERO.equals(totalNumSum.get()) ? BigDecimal.ZERO:(BigDecimalMath.div(BigDecimalMath.mul(sucNumSum.get(), new BigDecimal("100")),totalNumSum.get(), 2));
            // 按成功率降序
            List<StatisticsSucRateDto>  orderList = depositGroupingBy.get(type);
            orderList = orderList.stream().sorted(
                    Comparator.comparing(StatisticsSucRateDto::getSucRate).reversed()
            ).collect(Collectors.toList());

            resp.setType(type);
            resp.setTotalNumSum(totalNumSum.get());
            resp.setSucNumSum(sucNumSum.get());
            resp.setSucRateSum(sucRateSum);
            resp.setList(orderList);
            respList.add(resp);
        }

        return respList;
    }
}
