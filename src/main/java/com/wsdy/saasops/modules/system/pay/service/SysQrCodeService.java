package com.wsdy.saasops.modules.system.pay.service;

import com.wsdy.saasops.api.modules.pay.dto.PayParams;
import com.wsdy.saasops.api.modules.pay.dto.QrCodePayDto;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.base.entity.BaseBank;
import com.wsdy.saasops.modules.fund.dao.FundDepositMapper;
import com.wsdy.saasops.modules.fund.entity.FundDeposit;
import com.wsdy.saasops.modules.member.dao.MbrAccountMapper;
import com.wsdy.saasops.modules.member.dao.MbrGroupMapper;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrDepositCond;
import com.wsdy.saasops.modules.member.entity.MbrGroup;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrDepositCondService;
import com.wsdy.saasops.modules.member.service.MbrDepositLockLogService;
import com.wsdy.saasops.modules.member.service.MbrVerifyService;
import com.wsdy.saasops.modules.system.pay.dao.SetBasicQrCodeGroupMapper;
import com.wsdy.saasops.modules.system.pay.dao.SysQrCodeMapper;
import com.wsdy.saasops.modules.system.pay.dto.AllotDto;
import com.wsdy.saasops.modules.system.pay.entity.*;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.nonNull;

@Service
@Transactional
public class SysQrCodeService {

    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;
    @Autowired
    private MbrAccountLogService logService;
    @Autowired
    private SysQrCodeMapper sysQrCodeMapper;
    @Autowired
    private SetBasicQrCodeGroupMapper setBasicQrCodeGroupMapper;
    @Autowired
    private MbrAccountMapper mbrAccountMapper;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private MbrDepositCondService mbrDepositCondService;
    @Autowired
    private FundDepositMapper fundDepositMapper;
    @Autowired
    private MbrGroupMapper groupMapper;
    @Autowired
    private MbrVerifyService verifyService;
    @Autowired
    private MbrDepositLockLogService mbrDepositLockLogService;

    /**
     * 	扫码支付列表
     * 
     * @param qrCode
     * @return
     */
    public List<SysQrCode> qrCodePayList(SysQrCode qrCode){
        qrCode.setIsDelete(Constants.EVNumber.zero);
        List<SysQrCode> qrCodeList = sysQrCodeMapper.queryList(qrCode);
        if(Collections3.isNotEmpty(qrCodeList)){
            qrCodeList.forEach(q -> {
                q.setGroupList(sysQrCodeMapper.findGroupById(q.getId()));
                List<Integer> groupIds = q.getGroupList().stream().map(MbrGroup::getId).collect(Collectors.toList());
                q.setGroupIds(groupIds);
            });
        }
        return qrCodeList;
    }

    /**
     * 	扫码支付新增
     * 
     * @param qrCode
     * @param userName
     * @param ip
     */
    public void qrCodeSave(SysQrCode qrCode ,String userName,String ip){
    	// isHot，isRecommend 只有一个为true
		if (qrCode.getIsHot() != null && qrCode.getIsRecommend() != null && qrCode.getIsHot()
				&& qrCode.getIsRecommend()) {
			throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
		}
        qrCode.setIsDelete(Constants.EVNumber.zero);
        qrCode.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
        qrCode.setCreateUser(userName);
        qrCode.setQrImgFileName(qrCode.getQrImgUrl().substring(qrCode.getQrImgUrl().lastIndexOf("/")+1));
        qrCode.setDepositAmount(BigDecimal.ZERO);
        sysQrCodeMapper.insert(qrCode);
        insertQrCodeBank(qrCode);
        insertQrCodeGroup(qrCode);
        logService.addSysQrCodeLog(qrCode,userName,ip);
    }

    public SysQrCode qrCodePayInfo(Integer id){
        SysQrCode qrCode = sysQrCodeMapper.selectByPrimaryKey(id);
        qrCode.setGroupList(sysQrCodeMapper.findGroupById(id));
        List<Integer> groupIds = qrCode.getGroupList().stream().map(MbrGroup::getId).collect(Collectors.toList());
        qrCode.setGroupIds(groupIds);
        List<Integer> bankIds = sysQrCodeMapper.findBankList(id).stream().map(BaseBank :: getId).collect(Collectors.toList());
        qrCode.setBankIds(bankIds);
        return qrCode;
    }

    private void insertQrCodeBank(SysQrCode qrCode){
        List<Integer> bankIds = qrCode.getBankIds();
        List<SetBasicQrCodeBank> qrCodeBanks = bankIds.stream().map(id -> {
            SetBasicQrCodeBank qrCodeBank = new SetBasicQrCodeBank();
            qrCodeBank.setQrCodeId(qrCode.getId());
            qrCodeBank.setBankId(id);
            return qrCodeBank;
        }).collect(Collectors.toList());
        sysQrCodeMapper.batchInsertQrCodeBank(qrCodeBanks);
    }

    private void insertQrCodeGroup(SysQrCode qrCode){
        List<Integer> groupIds= qrCode.getGroupIds();
        List<SetBasicQrCodeGroup> qrCodeGroupsIsQueue = sysQrCodeMapper.getQrCodeGroupIsQueue();
        List<SetBasicQrCodeGroup> qrCodeGroups = groupIds.stream().map(id -> {
            SetBasicQrCodeGroup qrCodeGroup = new SetBasicQrCodeGroup();
            qrCodeGroup.setQrCodeId(qrCode.getId());
            qrCodeGroup.setGroupId(id);
            qrCodeGroup.setSort(Constants.EVNumber.zero);
            qrCodeGroup.setIsQueue(getIsQueue(id,qrCodeGroupsIsQueue));
            return qrCodeGroup;
        }).collect(Collectors.toList());
        sysQrCodeMapper.batchInsertQrCodeGroup(qrCodeGroups);
    }

    private Integer getIsQueue(Integer groupId,List<SetBasicQrCodeGroup> qrCodeGroupsIsQueue){
        //设置排队/平铺
        Optional<SetBasicQrCodeGroup> groupRelationOptional = qrCodeGroupsIsQueue.stream().filter(groupIsQueue ->
                groupId.equals(groupIsQueue.getGroupId()))
                .findAny();
        if(groupRelationOptional.isPresent()){
            return groupRelationOptional.get().getIsQueue();
        }else{//默认排队
            return Constants.EVNumber.one;
        }
    }

    /**
     * 	二维码编辑
     * 
     * @param qrCode
     * @param userName
     * @param ip
     */
    public void qrCodeUpdate(SysQrCode qrCode ,String userName,String ip){
    	
    	// isHot，isRecommend 只有一个为true
		if (qrCode.getIsHot() != null && qrCode.getIsRecommend() != null && qrCode.getIsHot()
				&& qrCode.getIsRecommend()) {
			throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
		}
			
    	// 删除原有的银行
    	sysQrCodeMapper.deleteQrCodeBank(qrCode.getId());
    	
        SetBasicQrCodeGroup qrCodeGroup = new SetBasicQrCodeGroup();
        qrCodeGroup.setQrCodeId(qrCode.getId());
        // 删除对应的code 组
        sysQrCodeMapper.deleteQrCodeGroup(qrCodeGroup);
        insertQrCodeBank(qrCode);
        insertQrCodeGroup(qrCode);
        
        SysQrCode qrCodeOld = sysQrCodeMapper.selectByPrimaryKey(qrCode.getId());
        // 删除对应的图片
        if(!qrCodeOld.getQrImgUrl().equals(qrCode.getQrImgUrl())){
            qrCode.setQrImgFileName(qrCode.getQrImgUrl().substring(qrCode.getQrImgUrl().lastIndexOf("/")+1));
            qiNiuYunUtil.deleteFile(qrCodeOld.getQrImgFileName());
        }
        
        qrCode.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        qrCode.setModifyUser(userName);
        sysQrCodeMapper.updateByPrimaryKeySelective(qrCode);
        logService.updateSysQrCodeLog(qrCode,userName,ip);
    }

    /**
     * 	修改状态
     * @param qrCode
     * @param userName
     * @param ip
     */
    public void qrCodeUpdateStatus(SysQrCode qrCode ,String userName,String ip){
    	// isHot，isRecommend 只有一个为true
    	if (qrCode.getIsHot() != null && qrCode.getIsRecommend() != null && qrCode.getIsHot()
    			&& qrCode.getIsRecommend()) {
    		throw new R200Exception("操作失败，推荐和热门按钮只允许同时勾选一个！");
    	}
        SysQrCode qrCodeOld = sysQrCodeMapper.selectByPrimaryKey(qrCode.getId());
        if (qrCodeOld == null) {
			throw new R200Exception("当前数据不存在，请重新刷新页面");
		}

        qrCodeOld.setIsHot(qrCode.getIsHot());
		qrCodeOld.setIsRecommend(qrCode.getIsRecommend());
        qrCodeOld.setAvailable(qrCode.getAvailable());
        qrCodeOld.setModifyTime(getCurrentDate(FORMAT_18_DATE_TIME));
        qrCodeOld.setModifyUser(userName);
        sysQrCodeMapper.updateByPrimaryKeySelective(qrCodeOld);
        logService.updateSysQrCodeStatusLog(qrCodeOld,userName,ip);
    }

    public void qrCodeDelete(Integer id, String userName,String ip){
        SysQrCode qrCodeOld = sysQrCodeMapper.selectByPrimaryKey(id);
        if (qrCodeOld.getAvailable() == Constants.EVNumber.one) {
            throw new R200Exception("禁用状态才能删除");
        }
        SysQrCode qrCodeNew = new SysQrCode();
        qrCodeNew.setId(id);
        qrCodeNew.setIsDelete(Constants.EVNumber.one);
        qrCodeNew.setModifyUser(userName);
        qrCodeNew.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        sysQrCodeMapper.updateByPrimaryKeySelective(qrCodeNew);
        SetBasicQrCodeGroup qrCodeGroup = new SetBasicQrCodeGroup();
        qrCodeGroup.setQrCodeId(id);
        sysQrCodeMapper.deleteQrCodeGroup(qrCodeGroup);
        logService.deleteSysQrCodeLog(qrCodeOld,userName,ip);
    }

    public List<SysQrCode> findQrCodeList(Integer accountId){
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(accountId);
        if (!nonNull(account)) {
            return new ArrayList<>();
        }
        List<SysQrCode> qrCodeList = sysQrCodeMapper.findQrCodeList(account.getGroupId());
        return qrCodeList;
    }

    public QrCodePayDto qrCodePay(PayParams params){
        params.setOutTradeNo(new SnowFlake().nextId());
        MbrAccount account = mbrAccountMapper.selectByPrimaryKey(params.getAccountId());
        params.setUserName(account.getRealName());

        // 效验存款锁定规则，是否应该锁定会员
        boolean lock = mbrDepositLockLogService.companyUnpayLock(account);
        if (lock) {
            throw new R200Exception("您已被限制存款，请联系客服.！");
        }

        SysQrCode qrCode = checkoutQrcodePay(params);

        sysSettingService.checkPayCondition(account, SystemConstants.DEPOSIT_CONDITION);
        MbrDepositCond mbrDepositCond = mbrDepositCondService.getMbrDeposit(params.getAccountId());
        FundDeposit deposit = saveFundDeposit(params, qrCode, mbrDepositCond, account);

        QrCodePayDto qrCodePayDto = new QrCodePayDto();
        qrCodePayDto.setCreateTime(deposit.getCreateTime());
        qrCodePayDto.setDepositPostscript(deposit.getDepositPostscript());
        qrCodePayDto.setOrderNo(deposit.getOrderNo());
        qrCodePayDto.setUrl(qrCode.getQrImgUrl());
        qrCodePayDto.setUrlMethod(Constants.EVNumber.two);
        qrCodePayDto.setDepositAmount(deposit.getDepositAmount());
        return qrCodePayDto;
    }

    private SysQrCode checkoutQrcodePay(PayParams params) {
        SysQrCode qrCode = sysQrCodeMapper.selectByPrimaryKey(params.getDepositId());
        if (Objects.isNull(qrCode) || Constants.EVNumber.zero == qrCode.getAvailable() || Constants.EVNumber.one == qrCode.getIsDelete()) {
            throw new R200Exception("此支付方式不接受会员充值");
        }
        if (qrCode.getAmountType() == 0) {
            if (qrCode.getMinAmout().compareTo(params.getFee()) == 1) {
                throw new R200Exception("小于单笔最小充值额度");
            }
            if (params.getFee().compareTo(qrCode.getMaxAmout()) == 1) {
                throw new R200Exception("大于单笔最大充值额度");
            }
        }
        if (qrCode.getAmountType() == 1) {
            if (!qrCode.getFixedAmount().contains(String.valueOf(params.getFee().intValue()))) {
                throw new R200Exception("存款金额不在可选金额中");
            }
        }
        if (qrCode.getDepositAmount().compareTo(qrCode.getDayMaxAmout()) == 1) {
            throw new R200Exception("此支付方式已经达到单日最大限额，请选择其他银行支付");
        }
        return qrCode;
    }

    private FundDeposit saveFundDeposit(PayParams params, SysQrCode qrCode, MbrDepositCond depositCond, MbrAccount account) {
        FundDeposit deposit = new FundDeposit();
        deposit.setOrderNo(params.getOutTradeNo().toString());
        deposit.setMark(FundDeposit.Mark.qrCodePay);
        deposit.setStatus(FundDeposit.Status.apply);
        deposit.setIsPayment(FundDeposit.PaymentStatus.unPay);
        deposit.setQrCodeId(params.getDepositId());
        Random random = new Random();
        //设置金额随机两位小数
        Integer d = random.nextInt(50);
        BigDecimal ran = new BigDecimal(d/100f);
        deposit.setDepositAmount(params.getFee().add(ran.setScale(2,BigDecimal.ROUND_DOWN)));

        Byte feeEnable = nonNull(depositCond) && nonNull(depositCond.getFeeEnable()) ? depositCond.getFeeEnable() : 0;
        deposit.setHandlingCharge(getActualArrival(params.getFee(), qrCode, feeEnable));
        deposit.setHandingback(Constants.Available.disable);
        deposit.setActualArrival(deposit.getDepositAmount().add(deposit.getHandlingCharge()));
        deposit.setIp(params.getIp());
        deposit.setOrderPrefix(OrderConstants.FUND_ORDER_COMPANYDEPOSIT);
        deposit.setDepositUser(params.getUserName());
        deposit.setCreateUser(account.getLoginName());
        deposit.setAccountId(params.getAccountId());
        deposit.setCreateTime(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setModifyTime(DateUtil.getCurrentDate(FORMAT_18_DATE_TIME));
        deposit.setFundSource(params.getFundSource());
        deposit.setDepositPostscript(CommonUtil.genRandom(6, 6));
        deposit.setLoginName(account.getLoginName());   // 增加会员名
        fundDepositMapper.insert(deposit);
        verifyService.addMbrVerifyDeposit(deposit, CommonUtil.getSiteCode());
        return deposit;
    }

    public BigDecimal getActualArrival(BigDecimal fee, SysQrCode qrCode, Byte feeEnable) {
        if (feeEnable == Constants.EVNumber.zero) {
            return BigDecimal.ZERO;
        }
        BigDecimal bigDecimal = BigDecimal.ZERO;
        if (qrCode.getFeeWay() == Constants.EVNumber.one) {
            bigDecimal = qrCode.getFeeFixed();
        }
        if (qrCode.getFeeWay() == Constants.EVNumber.zero) {
            bigDecimal = CommonUtil.adjustScale(qrCode.getFeeScale().divide(
                    new BigDecimal(Constants.ONE_HUNDRED)).multiply(fee));
            if (nonNull(qrCode.getFeeTop()) && bigDecimal.compareTo(qrCode.getFeeTop()) == 1) {
                bigDecimal = qrCode.getFeeTop();
            }
        }
        return bigDecimal;
    }

    private void deleteQrCodeGroup(AllotDto allotDto){
        qrCodePayLog(allotDto);
        MbrGroup group = groupMapper.selectByPrimaryKey(allotDto.getGroupId());
        if (Objects.isNull(group)) {
            throw new R200Exception("会员组不存在");
        }
        SetBasicQrCodeGroup qrCodeGroup = new SetBasicQrCodeGroup();
        qrCodeGroup.setGroupId(group.getId());
        sysQrCodeMapper.deleteQrCodeGroup(qrCodeGroup);

    }

    public void updateQrCodeSort(AllotDto allotDto){
        deleteQrCodeGroup(allotDto);
        if (Collections3.isNotEmpty(allotDto.getQrCodePayGroups())) {
            setBasicQrCodeGroupMapper.insertList(allotDto.getQrCodePayGroups());
        }
    }

    private void qrCodePayLog(AllotDto allotDto){
        Integer groupId = allotDto.getGroupId();
        MbrGroup group = groupMapper.selectByPrimaryKey(groupId);
        SetBasicQrCodeGroup qrCodeGroup = new SetBasicQrCodeGroup();
        qrCodeGroup.setGroupId(groupId);
        List<SetBasicQrCodeGroup> qrCodeGroups = sysQrCodeMapper.getQrCodeGroup(qrCodeGroup);
        List<Integer> qrCodeIds = qrCodeGroups.stream().map(SetBasicQrCodeGroup :: getQrCodeId).collect(Collectors.toList());
        List<Integer> diffList = null;
        Integer addOrDel = Constants.EVNumber.zero;
        if(CollectionUtils.isNotEmpty(allotDto.getQrCodePayGroups()) &&
                (CollectionUtils.isEmpty(qrCodeIds) || qrCodeIds.size()<allotDto.getQrCodePayGroups().size())){//添加 addOrDel = 1
            List<Integer> newQrCodeIds = allotDto.getQrCodePayGroups().stream().map(SetBasicQrCodeGroup :: getQrCodeId).collect(Collectors.toList());
            diffList = getSubtractId(qrCodeIds,newQrCodeIds);
            addOrDel = Constants.EVNumber.one;
        }else if(CollectionUtils.isNotEmpty(qrCodeIds) && (CollectionUtils.isEmpty(allotDto.getSysDepMbrs()) ||
                qrCodeIds.size()>allotDto.getQrCodePayGroups().size())){//删除 addOrDel = 0
            if(CollectionUtils.isEmpty(allotDto.getQrCodePayGroups())){
                diffList = qrCodeIds;
            }else{
                List<Integer> newDepositList = allotDto.getQrCodePayGroups().stream().map(SetBasicQrCodeGroup :: getQrCodeId).collect(Collectors.toList());
                diffList = getSubtractId(newDepositList,qrCodeIds);
            }
            addOrDel = Constants.EVNumber.zero;
        }
        if(Collections3.isNotEmpty(diffList)) {
            SysQrCode qrCode = sysQrCodeMapper.selectByPrimaryKey(diffList.get(0));
            logService.updatePaySetLog(addOrDel, group.getGroupName(), qrCode.getName());
        }else if(CollectionUtils.isNotEmpty(qrCodeIds)){//调整顺序
            logService.updatePaySetLog(Constants.EVNumber.three, group.getGroupName(), null);
        }
    }

    private List<Integer> getSubtractId(List<Integer> smallList,List<Integer> bigList){
        if(Collections3.isEmpty(smallList)) {
            return bigList;
        }
        return Collections3.subtract(bigList,smallList);
    }

    public List<SysQrCode> queryQrCodeList() {
        return sysQrCodeMapper.selectAll();
    }
}
