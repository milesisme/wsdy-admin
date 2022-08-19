package com.wsdy.saasops.modules.system.msgtemple.service;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.wsdy.saasops.api.modules.user.service.SendMailSevice;
import com.wsdy.saasops.api.modules.user.service.SendSmsSevice;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.config.SiteCodeThreadLocal;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.listener.BizEvent;
import com.wsdy.saasops.modules.agent.service.AgentAccountService;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.entity.MbrMessageInfo;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import com.wsdy.saasops.modules.member.service.MbrMessageService;
import com.wsdy.saasops.modules.operate.service.OprRecMbrService;
import com.wsdy.saasops.modules.system.msgtemple.dao.MsgModelMapper;
import com.wsdy.saasops.modules.system.msgtemple.entity.MsgModel;
import com.wsdy.saasops.modules.system.msgtemple.mapper.myMsgModelMapper;
import com.wsdy.saasops.modules.system.systemsetting.dto.MailSet;
import com.wsdy.saasops.modules.system.systemsetting.service.SysSettingService;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MsgModelService {
    @Autowired
    private MsgModelMapper msgModelMapper;
    @Autowired
    private myMsgModelMapper modelMapper;
    @Value("${temple.msg.excel.path}")
    private String templeMsgExcelPath;
    @Autowired
    private MbrAccountService mbrAccountService;
    @Autowired
    private SysSettingService sysSettingService;
    @Autowired
    private SendMailSevice sendMailSevice;
    @Autowired
    private OprRecMbrService oprRecMbrService;
    @Autowired
    private AgentAccountService agentAccountService;
    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private MbrMessageService mbrMessageService;
    @Autowired
    private SendSmsSevice sendSmsSevice;

    public MsgModel queryObject(Integer id) {
        return msgModelMapper.selectByPrimaryKey(id);
    }

    public PageUtils queryListPage(MsgModel msgModel, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<MsgModel> list = msgModelMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }

    public R save(MsgModel msgModel, String userName, String ip) {
        msgModel.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        msgModel.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        msgModel.setState(Constants.EVNumber.one);
        msgModel.setCreater(userName);
        if (StringUtil.isNullOrEmpty(msgModel.getName())) {
            return R.error(2000, "必须填写模板名称");
        }
        msgModelMapper.insert(msgModel);

        //添加操作日志
        mbrAccountLogService.addMsgModelLog(msgModel, userName, ip);
        return R.ok();
    }

    public void update(MsgModel msgModel, String userName, String ip) {
        List<MsgModel> oldMsgList = modelMapper.selectListByIds(msgModel.getId()+"");
        msgModel.setModifyTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        msgModelMapper.updateByPrimaryKeySelective(msgModel);

        //添加操作日志
        MsgModel oldMsgModel = oldMsgList.get(0);
        if(oldMsgModel.getState().equals(msgModel.getState())){
            mbrAccountLogService.editMsgModelLog(msgModel, userName, ip);
        } else {
            mbrAccountLogService.updateMsgModelStatusLog(msgModel, userName, ip);
        }

    }

    public void delete(Integer id) {
        msgModelMapper.deleteByPrimaryKey(id);
    }

    public void deleteBatch(String ids, String userName, String ip) {
        List<MsgModel> msgList = modelMapper.selectListByIds(ids);
        modelMapper.deleteByIds(ids);

        //添加操作日志
        for(MsgModel msgModel:msgList){
            mbrAccountLogService.deleteMsgModelLog(msgModel, userName, ip);
        }
    }

    public PageUtils queryByConditions(MsgModel msgModel, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        /*String states = msgModel.getStates();
        if (!StringUtils.isEmpty(states)) {
            msgModel.setState(MsgModel.getStateByStates(states));
        }*/
        List<MsgModel> list = modelMapper.queryByConditions(msgModel);
        return BeanUtil.toPagedResult(list);
    }

    public void exportExcel(MsgModel msgModel, HttpServletResponse response) {
        String fileName = "信息模板" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        List<Map<String, Object>> list = Lists.newLinkedList();
        modelMapper.queryByConditions(msgModel).stream().forEach(
                cs -> {
                    Map<String, Object> param = new HashMap<>(8);
                    param.put("name", cs.getName());
                    param.put("msgName", cs.getMsgName());
                    param.put("inMail", cs.getInMail());
                    param.put("email", cs.getEmail());
                    param.put("phoneMail", cs.getPhoneMail());
                    param.put("state", cs.getState() == 1 ? "启用 " : "禁用");
                    param.put("creater", cs.getCreater());
                    param.put("createTime", cs.getCreateTime());
                    list.add(param);
                }
        );
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", templeMsgExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            log.error("error:" + e);
        }
    }

    public void sendMsg(BizEvent bizEvent) {
        SiteCodeThreadLocal siteCodeThreadLocal = new SiteCodeThreadLocal();
        siteCodeThreadLocal.setSiteCode(bizEvent.getSiteCode());
        ThreadLocalCache.siteCodeThreadLocal.set(siteCodeThreadLocal);

        MbrAccount mbrAccount = mbrAccountService.getAccountInfo(bizEvent.getUserId());
        //TODO 简化成一个，不要列表
        MsgModel msgModel = new MsgModel();
        msgModel.setMsgType(bizEvent.getEventType().getEventCode());
        msgModel.setState(1);
        msgModel = msgModelMapper.selectOne(msgModel);
        if (msgModel != null) {
            if (msgModel.getInMailDef() == 1) {
                sendInMail(mbrAccount, msgModel, bizEvent);
            }
            if (msgModel.getEmailDef() == 1) {
                MailSet mailSet = sysSettingService.getMailSet(bizEvent.getSiteCode());
                sendEmail(mailSet, mbrAccount, sendMailSevice, msgModel, bizEvent);
            }
            if (msgModel.getPhoneMailDef() == 1) {
                sendPhoneMsg(mbrAccount, msgModel, bizEvent.getSiteCode(), bizEvent);
            }
        }
    }

    /**
     * 处理信息模板,替换模板信息
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String msgContentHandler(String msg, BizEvent bizEvent) {
        switch (bizEvent.getEventType().getEventCode()) {
            case 1:
                return memberRegister(msg, bizEvent);
            case 2:
                return memberLevelUp(msg, bizEvent);
            case 3:
                return updateMemberInfo(msg, bizEvent);
            case 4:
                return forceLogout(msg, bizEvent);
            case 5:
                return memberAccountFreeze(msg, bizEvent);
            case 6:
                return onlinePay(msg, bizEvent);
            case 7:
                return depositVerifySuccess(msg, bizEvent);
            case 8:
                return depositVerifyFailed(msg, bizEvent);
            case 9:
                return promoteVerifySuccess(msg, bizEvent);
            case 10:
                return promoteVerifyFailed(msg, bizEvent);
            case 11:
                return memberCommissionSuccess(msg, bizEvent);
            case 12:
                return memberCommissionRefuse(msg, bizEvent);
            case 13:
                return memberWithdrawalPrimaryVerifyFailed(msg, bizEvent);
            case 14:
                return memberWithdrawalReviewVerifyFailed(msg, bizEvent);
            case 15:
                return memberWithdrawalReviewVerifySuccess(msg, bizEvent);
            case 16:
                return memberWithdrawalRefuse(msg, bizEvent);
            case 17:
                return agencyRegisterSuccess(msg, bizEvent);
            case 18:
                return agencyWithdrawalVerifySuccess(msg, bizEvent);
            case 19:
                return agencyWithdrawalVerifyFailed(msg, bizEvent);
            case 20:
                return agencySalarySuccess(msg, bizEvent);
            case 21:
                return agencyAccountFreeze(msg, bizEvent);
            case 22:
                return agencyWithdrawalRefuse(msg, bizEvent);
            case 23:
                return agencySalaryRefuse(msg, bizEvent);
            case 24:
                return accountRebate(msg, bizEvent);
            case 25:
                return accountManualAdd(msg, bizEvent);
            case 26:
                return accountManualReduce(msg, bizEvent);
            case 27:
                return friendsTransInfo(msg,bizEvent);
            case 28:
                return bindBankCardSuccess(msg, bizEvent);
            case 29:
                return unBindBankCardSuccess(msg, bizEvent);
            case 30:
                return memberAccountStart(msg, bizEvent);
            case 31:
                return memberModifyRealName(msg, bizEvent);
            case 32:
                return memberModifyEmail(msg, bizEvent);
            case 33:
                return memberModifyMobile(msg, bizEvent);
            case 34:
                return memberModifyPwd(msg, bizEvent);
            case 35:
                return bindCRSuccess(msg, bizEvent);
            case 36:
                return unBindCRSuccess(msg, bizEvent);
            case 37:
                return memberWithdrawalPrimaryVerifyFailed1(msg, bizEvent);
            case 38:
                return memberWithdrawalPrimaryVerifyFailed2(msg, bizEvent);
            case 39:
                return memberWithdrawalPrimaryVerifyFailed3(msg, bizEvent);
            case 40:
                return memberWithdrawalPrimaryVerifyFailed4(msg, bizEvent);
            case 41:
                return redEnvelopeMsg(msg, bizEvent);
            default:
        }
        return null;
    }

    /**
     * 拒绝返佣
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencySalaryRefuse(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 拒绝代理取款
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyWithdrawalRefuse(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 、人工增加
     * @param msg
     * @param bizEvent
     * @return
     */
    private String accountManualAdd(String msg, BizEvent bizEvent) {
        msg = msg.replace("#{despoitMoney}", bizEvent.getDespoitMoney().toString());
        return msg;
    }

    /**
     * 人工减少
     * @param msg
     * @param bizEvent
     * @return
     */
    private String accountManualReduce(String msg, BizEvent bizEvent) {
        msg = msg.replace("#{despoitMoney}", bizEvent.getDespoitMoney().toString());
        return msg;
    }

    /**
     * 代理账户冻结
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyAccountFreeze(String msg, BizEvent bizEvent) {
        return msg.replace("#{loginName}", agentAccountService.findAccountInfo(bizEvent.getAgencyId()).getAgyAccount()).replace("#{date}", LocalTime.now().toString());
    }

    /**
     * 代理返佣成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencySalarySuccess(String msg, BizEvent bizEvent) {

        return msg.replace("#{term}", bizEvent.getTerm()).replace("#{commssion}", bizEvent.getCommssion().toString());
    }

    /**
     * 代理取款审核失败
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyWithdrawalVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}", bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 代理取款审核成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyWithdrawalVerifySuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}", bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 代理注册成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String agencyRegisterSuccess(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 拒绝会员取款
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalRefuse(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 会员取款审核成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalReviewVerifySuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}", bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 会员提款复审拒绝
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalReviewVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}", bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 会员提款初审拒绝
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalPrimaryVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{withdrawMoney}", bizEvent.getWithdrawMoney().toString());
    }

    /**
     * 会员提款初审拒绝(流水不足)
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalPrimaryVerifyFailed1(String msg, BizEvent bizEvent) {
        return msg;
    }
    /**
     * 会员提款初审拒绝(违规下注)
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalPrimaryVerifyFailed2(String msg, BizEvent bizEvent) {
        return msg;
    }
    /**
     * 会员提款初审拒绝(优惠套利)
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalPrimaryVerifyFailed3(String msg, BizEvent bizEvent) {
        return msg;
    }
    /**
     * 会员提款初审拒绝(注单审核)
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberWithdrawalPrimaryVerifyFailed4(String msg, BizEvent bizEvent) {
        return msg;
    }



    /**
     * 拒绝会员返水
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberCommissionRefuse(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 会员返点
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String accountRebate(String msg, BizEvent bizEvent) {
        return msg.replace("#{acvitityMoney}", bizEvent.getDespoitMoney().toString());
    }

    /**
     * 会员返水成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberCommissionSuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{acvitityMoney}", bizEvent.getAcvitityMoney().toString());
    }

    /**
     * 优惠审核失败
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String promoteVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{acvitityMoney}", bizEvent.getAcvitityMoney().toString()).replace("#{acvitityName}", bizEvent.getAcvitityName());
    }

    /**
     * 优惠审核成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String promoteVerifySuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{acvitityMoney}", bizEvent.getAcvitityMoney().toString()).replace("#{acvitityName}", bizEvent.getAcvitityName());
    }

    /**
     * 存款审核失败
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String depositVerifyFailed(String msg, BizEvent bizEvent) {
        return msg.replace("#{despoitMoney}", bizEvent.getDespoitMoney().toString());
    }

    /**
     * 存款审核成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String depositVerifySuccess(String msg, BizEvent bizEvent) {
        return msg.replace("#{despoitMoney}", bizEvent.getDespoitMoney().toString());
    }

    /**
     * 会员注册成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberRegister(String msg, BizEvent bizEvent) {
        return msg.replace("#{loginName}", mbrAccountService.getAccountInfo(bizEvent.getUserId()).getLoginName());
    }

    /**
     * 玩家升级成功
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberLevelUp(String msg, BizEvent bizEvent) {
        return msg;
    }

    /**
     * 修改会员资料
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String updateMemberInfo(String msg, BizEvent bizEvent) {
        return msg.replace("#{oldPassword}", bizEvent.getOldPassword()).replace("#{newPassword}", bizEvent.getNewPassword());
    }

    /**
     * 会员账户冻结
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberAccountFreeze(String msg, BizEvent bizEvent) {
        return msg.replace("#{loginName}", mbrAccountService.getAccountInfo(bizEvent.getUserId()).getLoginName()).replace("#{date}", LocalTime.now().toString());
    }

    /**
     * 好友转账
     * @param msg
     * @param bizEvent
     * @return
     */
    private String friendsTransInfo(String msg,BizEvent bizEvent){
        return msg.replace("#{loginName}",bizEvent.getLoginName()).replace("#{transAmount}",bizEvent.getTransAmount().toString());
    }

    /**
     * 绑定会员银行卡成功
     * @param msg
     * @param bizEvent
     * @return
     */
    private String bindBankCardSuccess(String msg,BizEvent bizEvent){
        return msg.replace("#{cardNo}",bizEvent.getCardNo());
    }

    /**
     * 解绑会员银行卡成功
     * @param msg
     * @param bizEvent
     * @return
     */
    private String unBindBankCardSuccess(String msg,BizEvent bizEvent){
        return msg.replace("#{cardNo}",bizEvent.getCardNo());
    }

    /**
     * 绑定会员钱包成功
     * @param msg
     * @param bizEvent
     * @return
     */
    private String bindCRSuccess(String msg,BizEvent bizEvent){
        return msg.replace("#{walletAddress}",bizEvent.getCardNo());
    }
    /**
     * 解绑会员钱包成功
     * @param msg
     * @param bizEvent
     * @return
     */
    private String unBindCRSuccess(String msg,BizEvent bizEvent){
        return msg.replace("#{walletAddress}",bizEvent.getCardNo());
    }

    /**
     * 会员账户启用
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberAccountStart(String msg, BizEvent bizEvent) {
        return msg.replace("#{loginName}", mbrAccountService.getAccountInfo(bizEvent.getUserId()).getLoginName()).replace("#{date}", LocalTime.now().toString());
    }

    /**
     * 修改会员真实姓名
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberModifyRealName(String msg, BizEvent bizEvent) {
        return msg.replace("#{realName}", mbrAccountService.getAccountInfo(bizEvent.getUserId()).getRealName());
    }

    /**
     * 修改会员邮箱
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberModifyEmail(String msg, BizEvent bizEvent) {
        return msg.replace("#{email}", mbrAccountService.getAccountInfo(bizEvent.getUserId()).getEmail());
    }

    /**
     * 修改会员电话
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberModifyMobile(String msg, BizEvent bizEvent) {
        return msg.replace("#{mobile}", mbrAccountService.getAccountInfo(bizEvent.getUserId()).getMobile());
    }

    /**
     * 修改会员登录密码
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String memberModifyPwd(String msg, BizEvent bizEvent) {
        return msg;
    }




    /**
     * 强制踢出
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String forceLogout(String msg, BizEvent bizEvent) {
        return msg;
    }

    private String onlinePay(String msg, BizEvent bizEvent) {
        return msg.replace("#{despoitMoney}", bizEvent.getDespoitMoney().toString()).replace("#{orderNum}", bizEvent.getOrderNum());
    }


    private void sendEmail(MailSet mailSet, MbrAccount mbrAccount, SendMailSevice sendMailSevice, MsgModel msgModel, BizEvent bizEvent) {
        sendMailSevice.sendMail(mailSet, mbrAccount.getEmail(), msgModel.getName(), msgContentHandler(msgModel.getInMail(), bizEvent),null);
    }

    /**
     * 来自publishEvent的消息，属于用户被动接受的消息
     * 
     * @param mbrAccount
     * @param msgModel
     * @param siteCode
     * @param bizEvent
     */
    private void sendPhoneMsg(MbrAccount mbrAccount, MsgModel msgModel, String siteCode, BizEvent bizEvent) {
        String mobile = String.valueOf(mbrAccount.getMobile());
        String content = msgContentHandler(msgModel.getPhoneMail(), bizEvent);
        // 用户被动接受的消息 module = 0
        sendSmsSevice.sendSms(mobile, content, false, mbrAccount.getMobileAreaCode(), 0);
    }

    /**
     * 包赔优惠
     *
     * @param msg
     * @param bizEvent
     * @return
     */
    private String redEnvelopeMsg(String msg, BizEvent bizEvent) {
        return msg.replace("#{redMoney}", bizEvent.getTransAmount().toString());
    }

    private void sendInMail(MbrAccount mbrAccount, MsgModel msgModel, BizEvent bizEvent) {
        /*OprRecMbr oprRecMbr = new OprRecMbr();
        oprRecMbr.setMbrId(mbrAccount.getId());
        oprRecMbr.setIsRead(0);
        oprRecMbr.setMbrName(mbrAccount.getLoginName());
        oprRecMbr.setTitle(msgModel.getName());
        List<MbrAccount> mbrAccounts = new ArrayList<>();
        mbrAccounts.add(mbrAccount);
        oprRecMbr.setMbrList(mbrAccounts);
        oprRecMbr.setContext(msgContentHandler(msgModel.getInMail(), bizEvent));
        String sender = "系统消息";
        oprRecMbrService.sendInMail(oprRecMbr, sender);*/

        MbrMessageInfo mbrMessageInfo = new MbrMessageInfo();
        mbrMessageInfo.setCreateUser("系统消息");
        List<String> loginNameList = new ArrayList<>();
        loginNameList.add(mbrAccount.getLoginName());
        mbrMessageInfo.setLoginNameList(loginNameList);
        mbrMessageInfo.setIsSign(2);
        // 替换消息模板，获取消息内容
        mbrMessageInfo.setTextContent(msgContentHandler(msgModel.getInMail(), bizEvent));
        mbrMessageService.messageSend(mbrMessageInfo, null);
    }

    public void sendInMailForDepositLock(MbrAccount mbrAccount, MsgModel msgModel, BizEvent bizEvent) {

        MbrMessageInfo mbrMessageInfo = new MbrMessageInfo();
        mbrMessageInfo.setCreateUser("系统消息");
        List<String> loginNameList = new ArrayList<>();
        loginNameList.add(mbrAccount.getLoginName());
        mbrMessageInfo.setLoginNameList(loginNameList);
        mbrMessageInfo.setIsSign(2);
        // 替换消息模板，获取消息内容
        mbrMessageInfo.setTextContent(msgContentHandler(msgModel.getInMail(), bizEvent));
        mbrMessageService.messageSend(mbrMessageInfo, null);
    }
}
