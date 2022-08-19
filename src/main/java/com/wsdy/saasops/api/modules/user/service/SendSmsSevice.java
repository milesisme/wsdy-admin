package com.wsdy.saasops.api.modules.user.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.wsdy.saasops.api.modules.user.dto.SmsRes;
import com.wsdy.saasops.api.modules.user.dto.SmsTeleSignResDto;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.api.utils.OkHttpUtils;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.BeanUtil;
import com.wsdy.saasops.common.utils.CommonUtil;
import com.wsdy.saasops.common.utils.DateUtil;
import com.wsdy.saasops.common.utils.PageUtils;
import com.wsdy.saasops.common.utils.StringUtil;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.system.systemsetting.dao.SmsConfigMapper;
import com.wsdy.saasops.modules.system.systemsetting.dao.SmsLogMapper;
import com.wsdy.saasops.modules.system.systemsetting.dto.SmsResultDto;
import com.wsdy.saasops.modules.system.systemsetting.entity.SmsConfig;
import com.wsdy.saasops.modules.system.systemsetting.entity.SmsLog;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.telesign.MessagingClient;
import com.telesign.RestClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;
import static java.util.Objects.isNull;

@Slf4j
@Service
@Transactional
public class SendSmsSevice {
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private SmsConfigMapper smsConfigMapper;
    @Autowired
    private JsonUtil jsonUtil;
    @Autowired
    private SmsLogMapper smsLogMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private SendMailSevice sendMailSevice;
    @Value("${email.open}")
    private boolean open;
    @Value("${email.sms.plateform.errornum}")
    private Integer errornum;


    @Value("${ipgo.sms.id}")
    private String IPGO_id;
    @Value("${ipgo.sms.url}")
    private String IPGO_url;
    /**
     *  测试短信发送接口
     * @param mobiles   需要发送的手机号
     * @param content   发送的内容
     * @param smsConfig 平台参数
     */
    public SmsResultDto testReceiveSms(String mobiles,String content, SmsConfig smsConfig) {
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==testReceiveSms==" + jsonUtil.toJson(smsConfig));
        // 发送结果
        SmsResultDto smsResultDto = new SmsResultDto();
        smsResultDto.setSuccess(false);

        // 发送短信
        switch (smsConfig.getPlatformId()) {
            case Constants.EVNumber.one:    // 1梦网云
                return sendSmsMWY(mobiles, content, smsConfig, false);
            case Constants.EVNumber.two:    //  2Telesign
                return sendSmsTelSign(mobiles, content, smsConfig,false,smsConfig.getMobileAreaCode());
            case Constants.EVNumber.three:  //  3启瑞云
                return sendSmsQRY(mobiles, content, smsConfig, false);
            case Constants.EVNumber.four:   // 4互亿无线
                return sendSmsHYWX(mobiles, content, smsConfig, false);
            default:
        }
        return smsResultDto;
    }

    /**
     * 短信发送接口
     * @param mobiles 需要发送的手机号
     * @param content  发送的内容
     * @param codeFlag 是否需要处理验证码
     * @param mobileAreaCode 手机号码国际区号 886台湾 86中国
     */
    public void sendSms(String mobiles,String content, boolean codeFlag, String mobileAreaCode, Integer module) {
        log.info("sendSmsApi==mobiles" + mobiles + "==content==" + content);
        // 查询可用短信配置, 按排序号排序
        SmsConfig smsConfig = new SmsConfig();
        smsConfig.setAvailable(Constants.EVNumber.one);
        if(StringUtil.isEmpty(mobileAreaCode)){
            mobileAreaCode = "86";
        }
        smsConfig.setMobileAreaCode("*" + mobileAreaCode);
        List<SmsConfig> smsConfigList = smsConfigMapper.querySmsConfig(smsConfig);
        if(Objects.isNull(smsConfigList) || smsConfigList.size() == 0){
            throw new RRException("未配置或未启用短信平台！");
        }

        // 异步发送短信
        sendSmsAsync(mobiles, content, smsConfigList, codeFlag, mobileAreaCode, module);
    }

    /**
     * 异地登录短信发送
     * @param mobiles 需要发送的手机号
     * @param content  发送的内容
     * @param codeFlag 是否需要处理验证码
     * @param mobileAreaCode 手机号码国际区号 886台湾 86中国
     */
    public void sendYidiLoginSms(String mobiles, String content, boolean codeFlag, String mobileAreaCode, Integer module) {
        log.info("sendSmsApi==mobiles" + mobiles + "==content==" + content);
        // 查询可用短信配置, 按排序号排序
        SmsConfig smsConfig = new SmsConfig();
        smsConfig.setAvailable(Constants.EVNumber.one);
        if(StringUtil.isEmpty(mobileAreaCode)){
            mobileAreaCode = "86";
        }
        smsConfig.setMobileAreaCode("*" + mobileAreaCode);
        List<SmsConfig> smsConfigList = smsConfigMapper.querySmsConfig(smsConfig);
        if(Objects.isNull(smsConfigList) || smsConfigList.size() == 0){
            throw new RRException("未配置或未启用短信平台！");
        }

        // 异步发送短信
        sendYidiSmsAsync(mobiles, content, smsConfigList, codeFlag, mobileAreaCode, module);
    }

    /**
     * 越南站短信发送接口
     * @param mobiles 需要发送的手机号
     * @param content  发送的内容
     * @param codeFlag 是否需要处理验证码
     * @param mobileAreaCode 手机号码国际区号 886台湾 86中国
     */
    public void vietnameSendSms(String mobiles, String content, boolean codeFlag, String mobileAreaCode, Integer module) {
        log.info("sendSmsApi==mobiles" + mobiles + "==content==" + content);
        // 查询可用短信配置, 按排序号排序
        SmsConfig smsConfig = new SmsConfig();
        smsConfig.setAvailable(Constants.EVNumber.one);
        if(StringUtil.isEmpty(mobileAreaCode)){
            mobileAreaCode = "84";
        }
        smsConfig.setMobileAreaCode("*" + mobileAreaCode);
        List<SmsConfig> smsConfigList = smsConfigMapper.querySmsConfig(smsConfig);
        if(Objects.isNull(smsConfigList) || smsConfigList.size() == 0){
            throw new RRException("未配置或未启用短信平台！");
        }

        // 异步发送短信
        sendSmsAsync(mobiles, content, smsConfigList, codeFlag, mobileAreaCode, module);
    }

    /**
     * 异步发送短信
     */
    private void sendSmsAsync(String mobiles, String content, List<SmsConfig> smsConfigList, boolean codeFlag,String mobileAreaCode, Integer module) {
        String siteCode = CommonUtil.getSiteCode();
        CompletableFuture.runAsync(() -> {
            // 异步执行
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            // 顺序执行平台短信发送
            // 发送结果
            SmsResultDto smsResultDto = new SmsResultDto();
            smsResultDto.setSuccess(false);
            // 循环配置的短信服务商
            for(SmsConfig smsConfig : smsConfigList){
                switch (smsConfig.getPlatformId()) {
                    case Constants.EVNumber.one:    // 1梦网云
                        smsResultDto = sendSmsMWY(mobiles, content, smsConfig, codeFlag);
                        break;
                    case Constants.EVNumber.two:    //  2Telesign
                        smsResultDto = sendSmsTelSign(mobiles, content, smsConfig, codeFlag, mobileAreaCode);
                        break;
                    case Constants.EVNumber.three:  //  3启瑞云
                        smsResultDto = sendSmsQRY(mobiles, content, smsConfig, codeFlag);
                        break;
                    case Constants.EVNumber.four:   // 4互亿无线
                        smsResultDto = sendSmsHYWX(mobiles, content, smsConfig, codeFlag);
                        break;
                    case Constants.EVNumber.ten:   // twilio
                        smsResultDto = sendSmsTwilio(mobiles, content, smsConfig, codeFlag, mobileAreaCode);
                        break;
                    default:
                }
                // 发送成功结束循环，记录成功数据
                if(smsResultDto.isSuccess()){
                    SmsLog smslog = new SmsLog();
                    smslog.setType(Constants.EVNumber.one);
                    smslog.setStatus(Constants.EVNumber.zero);
                    smslog.setPlatformId(smsConfig.getPlatformId());
                    smslog.setPlatformName(smsConfig.getPlatformName());
                    smslog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                    smslog.setMobile(mobiles);
                    smslog.setContent(content);
                    smslog.setMsg(smsResultDto.getMessage());
                    smslog.setModule(module);
                    smslog.setIsSuccess(true);
                    smsLogMapper.insert(smslog);
                    break;
                }
                // 发送失败，记录平台单次发送失败
                SmsLog smslog = new SmsLog();
                smslog.setType(Constants.EVNumber.one);
                smslog.setStatus(Constants.EVNumber.zero);
                smslog.setPlatformId(smsConfig.getPlatformId());
                smslog.setPlatformName(smsConfig.getPlatformName());
                smslog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                smslog.setMobile(mobiles);
                smslog.setContent(content);
                smslog.setMsg(smsResultDto.getMessage());
                smsLogMapper.insert(smslog);
            }
            if(!smsResultDto.isSuccess()){   // 全部发送失败，记录单次发送失败
                // 保存
                SmsLog smslog = new SmsLog();
                smslog.setType(Constants.EVNumber.zero);
                smslog.setStatus(Constants.EVNumber.zero);
                smslog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                smslog.setMobile(mobiles);
                smslog.setContent(content);
                smslog.setMsg(smsResultDto.getMessage());
                smsLogMapper.insert(smslog);

                // 会员短信发送皆失败，发送告警邮件
                String format = DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME);
                String subject = "【报警】短信发送情况告警--会员发送失败(" + format + ")"; // 标题
                String text = "手机号码：" + mobiles + "所有短信平台均发送失败！请确认短信平台状况！";       // 短信内容
                boolean result = sendMailSevice.sendAlarmEmail(subject,text);
            }
        });
    }

    /**
     * 异步发送短信
     */
    private void sendYidiSmsAsync(String mobiles, String content, List<SmsConfig> smsConfigList, boolean codeFlag,String mobileAreaCode, Integer module) {
        String siteCode = CommonUtil.getSiteCode();
        CompletableFuture.runAsync(() -> {
            // 异步执行
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            // 顺序执行平台短信发送
            // 发送结果
            SmsResultDto smsResultDto = new SmsResultDto();
            smsResultDto.setSuccess(false);
            // 循环配置的短信服务商
            for(SmsConfig smsConfig : smsConfigList){
                switch (smsConfig.getPlatformId()) {
                    case Constants.EVNumber.two:    //  2Telesign
                        smsResultDto = sendSmsTelSign(mobiles, content, smsConfig, codeFlag, mobileAreaCode);
                        break;
                    default:
                }
            }
            if(!smsResultDto.isSuccess()){   // 全部发送失败，记录单次发送失败
                // 保存
                SmsLog smslog = new SmsLog();
                smslog.setType(Constants.EVNumber.zero);
                smslog.setStatus(Constants.EVNumber.zero);
                smslog.setCreateTime(getCurrentDate(FORMAT_18_DATE_TIME));
                smslog.setMobile(mobiles);
                smslog.setContent(content);
                smslog.setMsg(smsResultDto.getMessage());
                smsLogMapper.insert(smslog);

                // 会员短信发送皆失败，发送告警邮件
                String format = DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME);
                String subject = "【报警】短信发送情况告警--会员发送失败(" + format + ")"; // 标题
                String text = "手机号码：" + mobiles + "所有短信平台均发送失败！请确认短信平台状况！";       // 短信内容
                boolean result = sendMailSevice.sendAlarmEmail(subject,text);
            }
        });
    }


    @Async("smsAlarmTaskAsyncExecutor")
    @Transactional
    public void smsAlarm(String siteCode){
        // 告警关闭
        if(!open){
            return;
        }
        log.info("【"+siteCode + "】smsAlarm==" +siteCode + "==start" );
        String key = RedisConstants.BATCH_SMS_ALARM + siteCode ;
        Boolean isExpired = redisService.setRedisExpiredTimeBo(key, siteCode, 20, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(isExpired)) {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            SmsLog smsLog = new SmsLog();

            // 查询平台失败记录 TODO
            smsLog.setType(Constants.EVNumber.one);
            smsLog.setStatus(Constants.EVNumber.zero);
            List<SmsLog> logList = smsLogMapper.queryFailLog(smsLog);
            Map<Integer, List<SmsLog>> logGroupingBy =
                    logList.stream().collect(
                            Collectors.groupingBy(
                                    SmsLog::getPlatformId));

            for (Integer tierIdKey : logGroupingBy.keySet()) {
                List<SmsLog> logs = logGroupingBy.get(tierIdKey);
                if(logs.size()>= errornum ){ // 平台大于errornum 次就要报警
                    // 发送短信
                    String format = DateUtil.format(new Date(), DateUtil.FORMAT_18_DATE_TIME);
                    String subject = "【报警】短信发送情况告警--平台["  + logs.get(0).getPlatformName() +"]发送失败次数超阈值"; // 标题
                    String text = "平台["  + logs.get(0).getPlatformName() +"]发送失败次数大于等于"+ errornum+ "次(" + format + ")"; // 标题;       // 短信内容
                    boolean result = sendMailSevice.sendAlarmEmail(subject,text);
                    if(result){ // 邮件发送成功，处理为已处理
                        List<Long> ids = logs.stream().map(ls -> {
                            return ls.getId();
                        }).collect(Collectors.toList());
                        SmsLog smsLog1 = new SmsLog();
                        smsLog1.setStatus(Constants.EVNumber.one);
                        smsLog1.setIds(ids);
                        smsLogMapper.updateStatus(smsLog1);
                    }
                }
            }
            redisService.del(key);
        }
        log.info("【"+siteCode + "】smsAlarm==" +siteCode + "==end" );
    }



    /**
     *  梦网云短信发送
     * @param mobiles   需要发送的手机号
     * @param content   发送的内容
     */
    private SmsResultDto sendSmsMWY(String mobiles, String content, SmsConfig smsConfig, boolean codeFlag) {
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY==start");
        // 发送结果
        SmsResultDto smsResultDto = new SmsResultDto();
        smsResultDto.setSuccess(false);

        try{
            // 拼接参数
            Map<String, String> params = new HashMap<>(16);
            params.put("apikey", smsConfig.getInterfacePassword());     // 用户唯一标识
            params.put("mobile", mobiles);                              // 手机号码
            // 处理验证码
            if(codeFlag){
                if (StringUtils.isEmpty(smsConfig.getTemplate())) {
                    log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY==" + "未配置验证码模板!" );
                    smsResultDto.setMessage("未配置验证码模板!");
                    return smsResultDto;
                }
                content = smsConfig.getTemplate().replace("{0}", content).toString();
            }
            if(StringUtil.isNotEmpty(smsConfig.getSendName())){
                content = "【" + smsConfig.getSendName() + "】" + content;  // 短信签名+短信内容 (例:【YHX】您的验证码是：8888)
            }
            content = URLEncoder.encode(content, "GBK");          // 编码方法：urlencode（GBK明文）
            params.put("content",content );                             // 短信内容：最大支持350个字，一个字母或一个汉字都视为一个字。
            params.put("timestamp", DateUtil.getCurrentDate("MMddHHmmss"));               // 时间戳：采用24小时制格式MMDDHHMMSS

            // 发送请求
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY" + "==params==" + params);
            String url = smsConfig.getGetwayAddress();
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY" + "==url==" + url);
            String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), url,params,null);
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY"+"==result==" + result);

            if (StringUtil.isEmpty(result)) {
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY" + "==result返回为空!");
                smsResultDto.setMessage("result返回为空!");
                return smsResultDto;
            }
            if (StringUtil.isNotEmpty(result)) {
                SmsRes smsRes = new Gson().fromJson(result, SmsRes.class);
                if(!"0".equals(smsRes.getResult())){ // 0成功 非零失败
                    log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY" + "==短信发送失败，失败码==" + smsRes.getResult());
                    smsResultDto.setMessage("短信发送失败，失败码==" + smsRes.getResult());
                    return smsResultDto;
                }
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY==success");
                smsResultDto.setSuccess(true);
                return smsResultDto;
            }
        }catch(Exception e){
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY==" + "error==" + e);
            smsResultDto.setMessage( "error==" + e);
            return smsResultDto;
        }
        return smsResultDto;
    }
    /**
     *  Telesign短信发送
     * @param mobiles   需要发送的手机号
     * @param content   发送的内容
     */
    private SmsResultDto sendSmsTelSign(String mobiles, String content, SmsConfig smsConfig, boolean codeFlag, String mobileAreaCode) {
        // telesign需要加区号
        if(StringUtil.isEmpty(mobileAreaCode)){
             mobileAreaCode = "86";
        }
        mobiles = mobileAreaCode + mobiles;
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign==start");
        // 发送结果
        SmsResultDto smsResultDto = new SmsResultDto();
        smsResultDto.setSuccess(false);

        // 处理验证码
        if(codeFlag){
            if (StringUtils.isEmpty(smsConfig.getTemplate())) {
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign==" + "未配置验证码模板!" );
                smsResultDto.setMessage(  "未配置验证码模板!");
                return smsResultDto;
            }
            content = smsConfig.getTemplate().replace("{0}", content).toString();
        }
        if(StringUtil.isNotEmpty(smsConfig.getSendName())){
            content = "【" + smsConfig.getSendName() + "】" + content;  // 短信签名+短信内容 (例:【YHX】您的验证码是：8888)
        }
        String customerId = smsConfig.getInterfaceName();   // customerId
        String apiKey = smsConfig.getInterfacePassword();   // apiKey
        String messageType = "OTP";
        try {
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign" + "==customerId==" + customerId + "==apiKey==" + apiKey);
            MessagingClient messagingClient = new MessagingClient(customerId, apiKey);
            RestClient.TelesignResponse result = messagingClient.message(mobiles, content, messageType, null);
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign" + "==body==" + result.body);
            if (Objects.isNull(result) || StringUtil.isEmpty(result.body)) {
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign" + "==result返回为空!");
                smsResultDto.setMessage( "result返回为空!");
                return smsResultDto;
            }
            if (StringUtil.isNotEmpty(result.body)) {
                SmsTeleSignResDto smsRes = new Gson().fromJson(result.body, SmsTeleSignResDto.class);
                if(Objects.isNull(smsRes.getStatus())){
                    log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign" + "==staus is null");
                    smsResultDto.setMessage( "staus is null");
                    return smsResultDto;
                }
                if(!"290".equals(String.valueOf(smsRes.getStatus().getCode())) &&
                        !"200".equals(String.valueOf(smsRes.getStatus().getCode()))){ // 290 发往短信网关成功,200deliver成功 其他失败
                    log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign==短信发送失败，失败原因==code==" + smsRes.getStatus().getCode() + "==description" +smsRes.getStatus().getDescription());
                    smsResultDto.setMessage( "短信发送失败，失败原因==code==" + smsRes.getStatus().getCode() + "==description" +smsRes.getStatus().getDescription());
                    return smsResultDto;
                }
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign==success");
                smsResultDto.setSuccess(true);
                return smsResultDto;
            }
        }catch(Exception e){
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign==" + "error==" + e);
            smsResultDto.setMessage("error==" + e);
            return smsResultDto;
        }
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign==end");
        return smsResultDto;
    }

    /**
     *  启瑞云短信发送
     * @param mobiles   需要发送的手机号
     * @param content   发送的内容
     */
    private SmsResultDto sendSmsQRY(String mobiles, String content, SmsConfig smsConfig, boolean codeFlag) {
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsQRY" );
        // 发送结果
        SmsResultDto smsResultDto = new SmsResultDto();
        smsResultDto.setSuccess(false);
        try{
            // 拼接参数
            Map<String, String> params = new HashMap<>(16);
            // 处理验证码
            if(codeFlag){
                if (StringUtils.isEmpty(smsConfig.getTemplate())) {
                    log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsQRY==" + "未配置验证码模板!" );
                    smsResultDto.setMessage( "未配置验证码模板!");
                    return smsResultDto;
                }
                content = smsConfig.getTemplate().replace("{0}", content).toString();
            }
            if(StringUtil.isNotEmpty(smsConfig.getSendName())){
                content = "【" + smsConfig.getSendName() + "】" + content;  // 短信签名+短信内容 (例:【YHX】您的验证码是：8888)
            }
            content = URLEncoder.encode(content, "utf8");
            params.put("sm",content );                          // 短信签名+短信内容 (例:【启瑞云】您的验证码是：8888)
            params.put("da", mobiles);                          // 手机号码
            params.put("tf", "3");               // 短信内容的编码，2: GBK，3: UTF-8
            params.put("un", smsConfig.getInterfaceName());     // 接口短信账号(apiKey)
            params.put("pw", smsConfig.getInterfacePassword()); // 密码 (apiSecret)
            params.put("rf", "2");               // 控制返回格式，1:XML，2:JSON
            params.put("dc", "15");              // 指定内容类型 8:UCS2，15:中文
            params.put("rd", "0");               // 是否需要状态报告。0:不需要，1:需要
//            params.put("ts", DateUtil.getTodayStart(DateUtil.FORMAT_18_DATE_TIME2));    // 当前时间(yyyyMMddHHmmss)，有值时决定pw是签名，不是密码明文

            // 签名
//            String encript = smsSet.getSmsInterfaceName() + "+" + smsSet.getSmsInterfacePassword()
//                    +  "+" +  DateUtil.getTodayStart(DateUtil.FORMAT_18_DATE_TIME2) +  "+" + content;
//            log.info("sendSmsQRY==encriptBefore" + encript);
//            encript = Base64.getEncoder().encodeToString((MD5.getMD5Array(encript))); // Base64(MD5(un+pw+ts+sm))
//            log.info("sendSmsQRY==encriptAfter" + encript);
//            params.put("pw", encript); // 密码

            // 发送请求
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsQRY" + "==params==" + params);
            String url = smsConfig.getGetwayAddress();
            String result = OkHttpUtils.get(url,params);
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsQRY"+"==result==" + result);

            if (StringUtil.isEmpty(result)) {
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY" + "==result返回为空!");
                smsResultDto.setMessage( "result返回为空!");
                return smsResultDto;
            }
            if (StringUtil.isNotEmpty(result)) {
                SmsRes smsRes = new Gson().fromJson(result, SmsRes.class);
                if (!smsRes.isSuccess()) {
                    smsResultDto.setMessage( "短信发送失败==" + smsRes.getR());
                    return smsResultDto;
                }
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsQRY==success");
                smsResultDto.setSuccess(true);
                return smsResultDto;
            }
        }catch(Exception e){
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsMWY" + "==error==" + e);
            smsResultDto.setMessage("error==" + e);
            return smsResultDto;
        }
        return smsResultDto;
    }

    /**
     *  互亿无线短信发送
     * @param mobiles   需要发送的手机号
     * @param content   发送的内容
     */
    private SmsResultDto sendSmsHYWX(String mobiles, String content, SmsConfig smsConfig, boolean codeFlag) {
        // 发送结果
        SmsResultDto smsResultDto = new SmsResultDto();
        smsResultDto.setSuccess(false);

        // 拼接参数
        Map<String, String> params = new HashMap<>(8);
        params.put("mobile", mobiles);                              // 手机号码
        // 处理验证码
        if(codeFlag){
            if (StringUtils.isEmpty(smsConfig.getTemplate())) {
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsHYWX==" + "未配置验证码模板!" );
                smsResultDto.setMessage( "未配置验证码模板!");
                return smsResultDto;
            }
            content = smsConfig.getTemplate().replace("{0}", content).toString();
        }
        if(StringUtil.isNotEmpty(smsConfig.getSendName())){
            content = "【" + smsConfig.getSendName() + "】" + content;  // 短信签名+短信内容 (例:【YHX】您的验证码是：8888)
        }
        params.put("content", content);                             // 短信内容
        params.put("account", smsConfig.getInterfaceName());        // 账户
        params.put("password", smsConfig.getInterfacePassword());   // 密码
        params.put("format", "json");
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsHYWX" + "==params==" + params);
        // 发送请求
        String url = smsConfig.getGetwayAddress();
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsHYWX" + "==url==" + url);
        String result = OkHttpUtils.postForm(url, params);
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsQRY"+"==result==" + result);
        if (StringUtil.isEmpty(result)) {
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsHYWX" + "==result返回为空!");
            smsResultDto.setMessage( "result返回为空!");
            return smsResultDto;
        }
        if (StringUtil.isNotEmpty(result)) {
            SmsRes smsRes = new Gson().fromJson(result, SmsRes.class);
            if (!"2".equals(smsRes.getCode())) {
                smsResultDto.setMessage( "短信发送失败==" + smsRes.getMsg() + "," + smsRes.getCode());
                return smsResultDto;
            }
            smsResultDto.setSuccess(true);
            return smsResultDto;
        }
        return smsResultDto;
    }

    /**
     * twilio 越南站短信发送
     * @param mobiles   需要发送的手机号
     * @param content   发送的内容
     */
    private SmsResultDto sendSmsTwilio(String mobiles, String content, SmsConfig smsConfig, boolean codeFlag, String mobileAreaCode) {
        // 加区号
        if(StringUtil.isEmpty(mobileAreaCode)){
            mobileAreaCode = "+84";
        }
        mobiles = "+" + mobileAreaCode + mobiles;
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTwilio==start");
        // 发送结果
        SmsResultDto smsResultDto = new SmsResultDto();
        smsResultDto.setSuccess(false);


        // 处理验证码
        if(codeFlag){
            if (StringUtils.isEmpty(smsConfig.getTemplate())) {
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTwilio==" + "未配置验证码模板!" );
                smsResultDto.setMessage("未配置验证码模板!");
                return smsResultDto;
            }
            content = smsConfig.getTemplate().replace("{0}", content).toString();
        }
        if(StringUtil.isNotEmpty(smsConfig.getSendName())){
            content = "【" + smsConfig.getSendName() + "】" + content;  // 短信签名+短信内容 (例:【YHX】您的验证码是：8888)
        }
        String customerId = smsConfig.getInterfaceName();   // customerId
        String apiKey = smsConfig.getInterfacePassword();   // apiKey
        String messageType = "OTP";
        try {
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTwilio" + "==customerId==" + customerId + "==apiKey==" + apiKey);
            Twilio.init(customerId, apiKey);
            Message message = Message.creator(new PhoneNumber(mobiles), new PhoneNumber("+19896327175"),
                    content).create();
            //MessagingClient messagingClient = new MessagingClient(customerId, apiKey);
            //RestClient.TelesignResponse result = messagingClient.message(mobiles, content, messageType, null);
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTwilio" + "==body==" + message.getSid());
            if (Objects.isNull(message.getSid()) || StringUtil.isEmpty(message.getSid())) {
                log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTwilio" + "==result返回为空!");
                smsResultDto.setMessage( "result返回为空!");
                return smsResultDto;
            }
            if (StringUtil.isNotEmpty(message.getSid())) {
                smsResultDto.setSuccess(true);
                return smsResultDto;
            }
        }catch(Exception e){
            log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign==", e);
            smsResultDto.setMessage("error==" + e);
            return smsResultDto;
        }
        log.info("sendSmsApi==mobiles==" + mobiles + "==content==" + content + "==sendSmsTelSign==end");
        return smsResultDto;
    }


    /**
     * 异步短信群发接口 IPGO
     * @param to       待发送的手机号，以|分割
     * @param content  短信内容，不超过200字符
     */
    public void sendSmsMass(String to,String content, String siteCode) {
        CompletableFuture.runAsync(() -> {
            ThreadLocalCache.setSiteCodeAsny(siteCode);
            // 请求的地址
            String res = IPGO_url + "?id=" +  IPGO_id + "&msg=" + content + "&to=" + to ;
            log.info("短信群发请求地址url:" +res);
            String jsonMessage;
            Map<String, String> stringMap = new HashMap<>(2);
            try {
                String startTime = getCurrentDate(FORMAT_18_DATE_TIME);
                jsonMessage = okHttpService.get(okHttpService.getPayHttpsClient(), res, stringMap);
                log.info("短信群发时间，开始时间【" + startTime + "】,结束时间:" + getCurrentDate(FORMAT_18_DATE_TIME));
                log.info("短信群发返回信息【" + jsonMessage + "】");
            } catch (Exception e) {
                log.error("短信群发报错【" + e + "】");
                throw new RRException("短信群发错误！请联系管理员！");
            }
            if (isNull(jsonMessage)) {
                throw new RRException("短信群发平台返回空");
            }

            if(!("1".equals(jsonMessage))){    // 非1失败
                log.info("群发短信错误：" + jsonMessage + "==to==" + to);
            }
        });
    }

	/**
	 * 后台条件查询
	 * 
	 * @param smsLog
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public PageUtils list(SmsLog smsLog) {
		PageHelper.startPage(smsLog.getPageNo(), smsLog.getPageSize());
		List<SmsLog> list = smsLogMapper.selectList(smsLog);
		for (SmsLog target : list) {
			target.setMobile(StringUtil.phone(target.getMobile()));
		}
		return BeanUtil.toPagedResult(list);
	}

    /*public static void main(String[] args) {
        Twilio.init("AC8bbff7b1850692b8b60347c116963ab5", "91a4c7b8cc59a9e394961b375dfca32a");
        //Message message = Message.creator(new PhoneNumber("+84241057831"), new PhoneNumber("+19896327175"), "test message").create();
        Message message = Message.creator(new PhoneNumber("+84789148049"), new PhoneNumber("+19896327175"), "test message").create();
        System.out.println(message.getSid());
    }*/
}
