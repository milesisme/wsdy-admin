package com.wsdy.saasops.sysapi.service;

import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.modules.user.service.SendSmsSevice;
import com.wsdy.saasops.modules.activity.service.HuPengOprRebateService;
import com.wsdy.saasops.modules.agent.service.CommissionCastService;
import com.wsdy.saasops.modules.analysis.service.TurnoverRateService;
import com.wsdy.saasops.modules.mbrRebateAgent.service.MbrRebateAgentCastService;
import com.wsdy.saasops.modules.member.service.AccountAutoCastService;
import com.wsdy.saasops.modules.member.service.AccountVipRedService;
import com.wsdy.saasops.modules.member.service.AccountWaterCastService;
import com.wsdy.saasops.modules.member.service.SanGongRebateCastService;
import com.wsdy.saasops.modules.member.service.*;
import com.wsdy.saasops.modules.sys.service.SysEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class DispatcherTaskService {

	/**
	 * 会员组自动升级
	 */
	private static final String MBR_GROUP_AUTO_UPDATE = "mbrGroupAutoUpdate";
    private static final String ACCOUNT_AUTO_UPDATE = "accountAutoUpdate";
    private static final String ACCOUNT_WATER = "accountWater";
    private static final String ACCOUNT_OFFLINE = "accountOffline";
    private static final String ACCOUNT_SANGONG = "sanGongCast";
    private static final String ACCOUNT_ENCRYPT = "accountEncrypt";
    private static final String SMS_ALARM = "smsAlarm"; // 短信告警
    private static final String ACCOUNT_VIPREDENVELOPE = "accountVipRedenvelope";
    private static final String ACCOUNT_TRANSFER = "accountTransfer";
    private static final String ACCOUNT_REBATE_AGENT = "accountRebateAgent";            // 全民代理-日表计算
    private static final String ACCOUNT_REBATE_AGENT_MONTH = "accountRebateAgentMonth"; // 全民代理-月表计算

    private static final String MESSAGE_DELETE = "messageDelete"; // 过期消息清理
    private static final String AGENT_COMMISSION = "agentCommission"; //代理佣金
    private static final String ACCOUNT_DEVICE_BLACKLIST = "accountDeviceBlackList";   // 同设备拉黑名单组

    private static final String ACCOUNT_VERIFY = "accountVerify";

    private static final String ACCOUNT_VALIDBET_RATE = "accountValidbetRate"; //平台流水费率

    private static final String MBR_FUND_REPORT_COUNT = "mbrFundReportCount"; //统计会员每日存取款与优惠总额

    private static final String MBR_BET_POINT = "mbrBetPoint"; //统计会员投注比

    private static final String FRIEND_REBATE = "friendRebate"; // 好友返利

    private static final String HUPENG_REBATE = "hupengRebate"; // 呼朋换友

    private static final String MBR_WARNING = "mbrWarning"; // 会员预警
    @Autowired
    private AccountAutoCastService autoUpdateCastService;
    @Autowired
    private MbrGroupAutoUpdateService mbrGroupAutoUpdateService;
    @Autowired
    private AccountWaterCastService waterCastService;
    @Autowired
    private SanGongRebateCastService sanGongRebateCastService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SysEncryptionService encryptionService;
    @Autowired
    private SendSmsSevice sendSmsSevice;
    @Autowired
    private AccountVipRedService vipRedService;
    @Autowired
    private BatchTransferService batchTransferService;
    @Autowired
    private MbrRebateAgentCastService mbrRebateAgentCastService;
    @Autowired
    private MbrMessageService mbrMessageService;
    @Autowired
    private CommissionCastService commissionCastService;
    @Autowired
    private AccountDeviceBlackListService accountDeviceBlackListService;
    @Autowired
    private MbrVerifyService verifyService;
    @Autowired
    private TurnoverRateService turnoverRateService;
    @Autowired
    private MbrFundsReportService mbrFundsReportService;
    @Autowired
    private MbrBetPointService mbrBetPointService;
    @Autowired
    private AccountRebateCastNewService rebateCastNewService;
    @Autowired
    private HuPengOprRebateService huPengOprRebateService;
    @Autowired
    private MbrWarningService mbrWarningService;

    public void dispatcherTask(String siteCode, String taskSig, String key) {
        if (ACCOUNT_AUTO_UPDATE.equals(taskSig)) {
            autoUpdateCastService.isCastAccountAuto(siteCode);
        }
		if (taskSig.contains(MBR_GROUP_AUTO_UPDATE)) {
			Boolean queryRecent = false;
			String[] allMem = taskSig.split("_");
			if (allMem.length > 1) {
				queryRecent = true;
			}
			mbrGroupAutoUpdateService.mbrGroupAutoUpdate(siteCode, queryRecent);
		}
        if (ACCOUNT_WATER.equals(taskSig)) {
            waterCastService.castWaterActivity(siteCode);
        }
        if (ACCOUNT_OFFLINE.equals(taskSig)) {
            autoUpdateCastService.accountAutoOffline(siteCode);
        }
        if (ACCOUNT_SANGONG.equals(taskSig)) {
            sanGongRebateCastService.sanGongRebateCast(siteCode);
            redisService.del(key);
        }
        if (ACCOUNT_ENCRYPT.equals(taskSig)) {  // 数据库加解密
            encryptionService.castEncryption(siteCode);
            redisService.del(key);
        }
        if (SMS_ALARM.equals(taskSig)) {    // 短信告警
            sendSmsSevice.smsAlarm(siteCode);
            redisService.del(key);
        }
        if (ACCOUNT_VIPREDENVELOPE.equals(taskSig)) {
            vipRedService.castVipRedActivity(siteCode);
            redisService.del(key);
        }
        if (taskSig.contains(ACCOUNT_TRANSFER)) {
            String[] str = taskSig.split("_");
            batchTransferService.accountTransfer(siteCode, str[1]);
            redisService.del(key);
        }
        if (taskSig.contains(AGENT_COMMISSION)) {
            String[] str = taskSig.split("_");
            // 代理佣金
            commissionCastService.calculateCommission(siteCode, str[1]);
            redisService.del(key);
        }
        if (MESSAGE_DELETE.equals(taskSig)) {    // 过期消息清理
            mbrMessageService.messageDeleteExpiration(siteCode);
            redisService.del(key);
        }
        if (ACCOUNT_REBATE_AGENT.equals(taskSig)) {         // 全民代理-日表计算
            mbrRebateAgentCastService.mbrRebateAgentCast(siteCode);
            redisService.del(key);
        }
        if (ACCOUNT_REBATE_AGENT_MONTH.equals(taskSig)) {   // 全民代理-月表计算
            mbrRebateAgentCastService.mbrRebateAgentCastMonth(siteCode);
            redisService.del(key);
        }
        if (ACCOUNT_DEVICE_BLACKLIST.equals(taskSig)) {    // 同设备拉黑名单组
            accountDeviceBlackListService.accountBlackList(siteCode, key);
        }
        if (ACCOUNT_VERIFY.equals(taskSig)) {
            verifyService.castDatasecret(siteCode);
            redisService.del(key);
        }
        if (ACCOUNT_VALIDBET_RATE.equals(taskSig)) {
            turnoverRateService.castAccountRate(siteCode);
            redisService.del(key);
        }
        if (taskSig.contains(MBR_FUND_REPORT_COUNT)) {    // 统计每日会员资金报表（存取款，优惠）
            String date = "";
            if (taskSig.contains("_")) {
                String[] str = taskSig.split("_");
                date = str[1];
            }
            if ("MONTH".equals(date)) {
                mbrFundsReportService.countMonthlyMbrFundsReport(siteCode);
            } else {
                mbrFundsReportService.countDailyMbrFundsReport(siteCode, date);
            }
            redisService.del(key);
        }
        if (taskSig.contains(MBR_BET_POINT)) {    // 统计投注比
            Integer passDay = 0;
            if (taskSig.contains("_")) {
                String[] str = taskSig.split("_");
                passDay = Integer.parseInt(str[1]);
            }
            mbrBetPointService.countBetPoint(siteCode, passDay);
        }

        if(taskSig.contains(FRIEND_REBATE)){
            String date = "";
            if (taskSig.contains("_")) {
                String[] str = taskSig.split("_");
                date = str[1];
            }
            rebateCastNewService.friendRebate(siteCode, date);
            redisService.del(key);
        }

        if(taskSig.contains(HUPENG_REBATE)){
            String date = "";
            if (taskSig.contains("_")) {
                String[] str = taskSig.split("_");
                date = str[1];
            }
            huPengOprRebateService.accountHuPengRebate(siteCode, date);
            redisService.del(key);
        }

        if(taskSig.contains(MBR_WARNING)){
            String date = "";
            if (taskSig.contains("_")) {
                String[] str = taskSig.split("_");
                date = str[1];
            }
            mbrWarningService.mbrWarning(siteCode, date);
            redisService.del(key);
        }
    }
}
