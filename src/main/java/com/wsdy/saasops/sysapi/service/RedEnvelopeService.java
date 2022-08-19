package com.wsdy.saasops.sysapi.service;

import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.OrderConstants;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.listener.BizEventType;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrMessageInfo;
import com.wsdy.saasops.modules.member.service.AuditAccountService;
import com.wsdy.saasops.modules.operate.dao.OprActBonusMapper;
import com.wsdy.saasops.modules.operate.entity.OprActActivity;
import com.wsdy.saasops.modules.operate.entity.OprActBonus;
import com.wsdy.saasops.modules.operate.entity.TOpActtmpl;
import com.wsdy.saasops.modules.operate.mapper.OperateActivityMapper;
import com.wsdy.saasops.modules.operate.service.OprActActivityCastService;
import com.wsdy.saasops.sysapi.dto.SptvBonusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Service
@Transactional
public class RedEnvelopeService {

    @Autowired
    private OprActActivityCastService actActivityCastService;
    @Autowired
    private OperateActivityMapper activityMapper;
    @Autowired
    private OprActBonusMapper actBonusMapper;
    @Autowired
    private AuditAccountService auditAccountService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;



    public void addM8Envelope(SptvBonusDto dto, MbrAccount account) {
       OprActActivity activity = new OprActActivity();
        activity.setTmplCode(TOpActtmpl.m8EnvelopeCode);
        OprActActivity actActivity = activityMapper.findAffActivity(activity);
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(),
                actActivity.getId(), null, null, actActivity.getRuleId());
        bonus.setDiscountAudit(dto.getAuditMultiple());
        bonus.setBonusAmount(dto.getBonusAmount());
        bonus.setSource(1);
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setCreateUser(Constants.SYSTEM_USER);
        bonus.setStatus(Constants.EVNumber.one);
        bonus.setAuditTime(bonus.getApplicationTime());
        bonus.setAuditUser(Constants.SYSTEM_USER);

        actBonusMapper.insert(bonus);
        bonus.setAuditUser(Constants.SYSTEM_USER);
        bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        actActivityCastService.auditOprActBonus(bonus, OrderConstants.TYJ_PAYOUT_FINANCIAL_CODE, actActivity.getActivityName(), Boolean.FALSE);

    }

    public void addRedEnvelope(SptvBonusDto dto, MbrAccount account) {
   /*     OprActActivity activity = new OprActActivity();
        activity.setTmplCode(TOpActtmpl.redEnvelopeActivityCode);
        OprActActivity actActivity = activityMapper.findAffActivity(activity);
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(),
                actActivity.getId(), null, null, actActivity.getRuleId());
        bonus.setDiscountAudit(dto.getAuditMultiple());
        bonus.setBonusAmount(dto.getBonusAmount());
        bonus.setSource(1);
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(bonus.getDiscountAudit(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setCreateUser(Constants.SYSTEM_USER);
        bonus.setStatus(Constants.EVNumber.one);
        bonus.setOrderNo(dto.getBetNumber());
        bonus.setAuditTime(bonus.getApplicationTime());
        bonus.setAuditUser(Constants.SYSTEM_USER);
        actBonusMapper.insert(bonus);*/

        OprActActivity activity = new OprActActivity();
        activity.setTmplCode(TOpActtmpl.redEnvelopeActivityCode);
        OprActActivity actActivity = activityMapper.findAffActivity(activity);
        OprActBonus bonus = actActivityCastService.setOprActBonus(account.getId(), account.getLoginName(),
                actActivity.getId(), null, null, actActivity.getRuleId());
        bonus.setModifyAmountMemo(null);
        bonus.setMemo(null);
        bonus.setDiscountAudit(dto.getAuditMultiple());
        bonus.setBonusAmount(dto.getBonusAmount());
        bonus.setSource(1);
        bonus.setAuditAmount(auditAccountService.getAddAuditAmount(dto.getAuditMultiple(), bonus.getDepositedAmount(), bonus.getBonusAmount()));
        bonus.setCreateUser(Constants.SYSTEM_USER);
        bonus.setOrderNo(dto.getBetNumber());
        actBonusMapper.insert(bonus);
        bonus.setAuditUser(Constants.SYSTEM_USER);
        bonus.setAuditTime(getCurrentDate(FORMAT_18_DATE_TIME));
        actActivityCastService.auditOprActBonus(bonus, OrderConstants.ACTIVITY_BPYH, actActivity.getActivityName(), Boolean.FALSE);

        String siteCode= dto.getSiteCode();
        BizEvent bizEvent = new BizEvent(this, siteCode, account.getId(),
                BizEventType.RED_ENVELOP_RED);
        bizEvent.setLoginName(account.getLoginName());
        bizEvent.setTransAmount(dto.getBonusAmount());
        applicationEventPublisher.publishEvent(bizEvent);
    }
}
