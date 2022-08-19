package com.wsdy.saasops.common.utils.jpush;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.file.FileClient;
import cn.jpush.api.file.model.FileModelPage;
import cn.jpush.api.file.model.FileType;
import cn.jpush.api.file.model.FileUploadResult;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.*;
import com.google.common.collect.Lists;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.Collections3;
import com.wsdy.saasops.modules.sys.dto.SysPushDto;
import com.wsdy.saasops.modules.sys.service.SysPushService;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

import static com.wsdy.saasops.common.utils.DateUtil.FORMAT_18_DATE_TIME;
import static com.wsdy.saasops.common.utils.DateUtil.getCurrentDate;

@Slf4j
@Component
public class JPushUtil {

    @Autowired
    private SysPushService sysPushService;
   // @Value("${jpush.APP_KEY}")
   // private String APP_KEY;
   // @Value("${jpush.MASTER_SECRET}")
   // private String MASTER_SECRET;
    @Value("${jpush.apnsProduction}")
    private boolean apnsProduction;
//    @Value("${jpush.readTimeout}")
//    private int readTimeout;
    @ApiModelProperty(value = "临时文件目录名")
    private static String pushDir = "jpushTemp";

    private static String AUDIENCE_ALL = "all";     // 推送对象：全部
    private static String AUDIENCE_ALIAS = "alias"; // 别名
    private static String AUDIENCE_FILE = "file";   // 文件

    /**
     * 发送给所有设备
     */
    public void pushToAll(JPushNotificationDto dto){
        // 创建pushPayload
        PushPayload pushPayload = buildPushPayload(dto,AUDIENCE_ALL);
        // 发送
        sendPush(pushPayload);
    }

    /**
     * 推送到alias列表
     * 向所有平台单个或多个指定别名用户推送消息
     */
    public void pushToAliasList(JPushNotificationDto dto) {
        log.info("messageSend==JPush==pushToAliasList==size==" + dto.getAlias().size());

        // 按个数分组
        List<List<String> > groupByLength = Lists.partition(dto.getAlias(),999);
        int count = 0;
        for(List<String> alias : groupByLength){
            dto.setAlias(alias);
            PushPayload pushPayload = buildPushPayload(dto,AUDIENCE_ALIAS);
            // 发送
            sendPush(pushPayload);
            log.info("messageSend==JPush==pushToAliasList==size==" + dto.getAlias().size() + "==count==" + count++);
        }
    }

    /**
     *  推送到alias列表-文件推送
     * @param dto
     */
    public void pushToAliasListByFile(JPushNotificationDto dto){
        // 入参校验
        if(Collections3.isEmpty(dto.getAlias())){
            return;
        }

        // 0.创建目录，获取文件路径
        String fileNamePath = getFileNamePath();
        String file_id = null;
        try{
            // 1.生成别名推送文件
            createAliasFile(dto.getAlias(),fileNamePath);
            dto.getAlias().clear();  // 显示清理
            // 2.查询当下的有效的文件，超过20个则删除文件 TODO

            // 3.上传别名推送文件
            FileUploadResult fileUploadResult = uploadPushFile(fileNamePath,FileType.ALIAS);
            if(Objects.isNull(fileUploadResult)){
                log.info("messageSend==JPush==pushToAliasListByFile==上传文件返回null");
                return;
            }

            file_id = fileUploadResult.getFile_id();

            // 4.文件推送
            // 创建pushPayload
            dto.setFile_id(fileUploadResult.getFile_id());  // 文件id
            PushPayload filePushPayload = buildPushPayload(dto,AUDIENCE_FILE);
            // 推送
            sendFilePush(filePushPayload);

        }catch(Exception e){
            log.error("messageSend==JPush==pushToAliasListByFile==",e);
        }finally {
            // 删除远程推送文件
            if(Objects.nonNull(file_id)){
                deletePushFile(file_id);
            }
            // 删除本地该文件
            File file = new File(fileNamePath);
            if(file.exists()) {
                file.delete();
            }

        }
    }

    /**
     * 推送
     * @param payload
     */
    private void sendPush(PushPayload payload) {
        ClientConfig clientConfig = ClientConfig.getInstance();

        SysPushDto sysPushDto = sysPushService.getByType(Constants.EVNumber.one);
        String MASTER_SECRET = sysPushDto.getSecret();
        String APP_KEY = sysPushDto.getPushKey();
        JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, clientConfig);

        // 支持3种httpClient， setHttpClient方法设置使用的httpClient 默认使用 NativeHttpClient.
//        String authCode = ServiceHelper.getBasicAuthorization(APP_KEY, MASTER_SECRET);
        // ApacheHttpClient设置
//        ApacheHttpClient httpClient = new ApacheHttpClient(authCode, null, clientConfig);
        // NettyHttpClient设置
//        NettyHttpClient httpClient =new NettyHttpClient(authCode, null, clientConfig);
        // 设置setHttpClient
//        jpushClient.getPushClient().setHttpClient(httpClient);

//        // For push, all you need do is to build PushPayload object.
//        PushPayload payload = buildPushObject_all_alias_alert();

        try {
            PushResult result = jpushClient.sendPush(payload);  // push api 接口 免费版本 600次/分钟
            log.info("messageSend==JPush==sendPush==responseCode==" + result.getResponseCode() + "==sendno==" + result.sendno+ "==msg_id==" + result.msg_id );
            // 如果使用 NettyHttpClient，需要手动调用 close 方法退出进程
            // jpushClient.close();
        } catch (APIConnectionException e) {
            log.error("messageSend==JPush==sendPush==APIConnectionException==" + e);
        } catch (APIRequestException e) {
            log.error("messageSend==JPush==sendFilePush==APIRequestException==status=="
                    + e.getStatus() + "==Error Code==" +e.getErrorCode() + "==Error Message==" + e.getErrorMessage()
                    + "==Msg ID==" + e.getMsgId() + "==Sendno==" + payload.getSendno());
        }
    }

    /**
     * 推送-文件
     * @param payload
     */
    private void sendFilePush(PushPayload payload) {
        ClientConfig clientConfig = ClientConfig.getInstance();
        SysPushDto sysPushDto = sysPushService.getByType(Constants.EVNumber.one);
        String MASTER_SECRET = sysPushDto.getSecret();
        String APP_KEY = sysPushDto.getPushKey();
        JPushClient jPushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, clientConfig);

        try {
            PushResult result = jPushClient.sendFilePush(payload);
            log.info("messageSend==JPush==sendFilePush==responseCode==" + result.getResponseCode() + "==sendno==" + result.sendno+ "==msg_id==" + result.msg_id );
        } catch (APIConnectionException e) {
            log.error("messageSend==JPush==sendFilePush==APIConnectionException==" + e);
        } catch (APIRequestException e) {
            log.error("messageSend==JPush==sendFilePush==APIRequestException==status=="
                    + e.getStatus() + "==Error Code==" +e.getErrorCode() + "==Error Message==" + e.getErrorMessage()
                    + "==Msg ID==" + e.getMsgId() + "==Sendno==" + payload.getSendno());
        }
    }

    /**
     * 创建 PushPayload对象
     * @param dto           消息传参dto
     * @param audienceType  推送目标 all 所有设备   alias别名  file 文件
     * @return
     */
    private PushPayload buildPushPayload(JPushNotificationDto dto,String audienceType){
        // 此字段为透传字段，不会显示在通知栏。用户可以通过此字段来做一些定制需求，如特定的key传要指定跳转的页面（value）
        Map<String, String> extras = dto.getExtras();

        // 安卓平台通知栏样式
        String title = dto.getMsgTitle();
        PlatformNotification platformNotificationAndroid = AndroidNotification.newBuilder()
                .setTitle(title)
                .setAlert(dto.getMsgContent())
                .addExtras(extras)
                .build();

        // iOS 平台上 APNs 通知结构
        int badge = 1; // 应用角标 角标数字： 不填-不改变角标数字  0-清除 1-表示+1
        // 创建一个IosAlert对象，可指定APNs的alert、title等字段
        IosAlert iosAlert = IosAlert.newBuilder().setTitleAndBody(dto.getMsgTitle(), null,dto.getMsgContent()).build();
        PlatformNotification platformNotificationIos = IosNotification.newBuilder()
                .setAlert(iosAlert)
                .incrBadge(badge)
                .addExtras(extras)
                .build();

        // 通知对象
        // alert信息:通知内容 上级统一信息，如果各平台有指定，则各平台覆盖上级
        String alert = dto.getMsgContent();
        Notification notification = Notification.newBuilder()
                .setAlert(alert)
                .addPlatformNotification(platformNotificationAndroid)
                .addPlatformNotification(platformNotificationIos)
                .build();

        // 推送目标 all:发广播（全部设备) 免费10次/天 错误码2008;  registration_id: 注册id
        Audience audience = null;
        if(AUDIENCE_ALL.equals(audienceType)){
            audience = Audience.all();
        }
        if(AUDIENCE_ALIAS.equals(audienceType)){
            audience =  Audience.alias(dto.getAlias());
        }
        if(AUDIENCE_FILE.equals(audienceType)){
            audience =  Audience.file(dto.getFile_id());
        }

        // 推送平台 "android", "ios", "quickapp","winphone"  all:全部平台  android_ios: 安卓和ios
        Platform platform = Platform.all();
        // 推送参数
        Options options = Options.newBuilder()
                .setTimeToLive(3600*dto.getTimeToLive())     // 离线消息保留时长(秒) 0 保留 默认1天86400，最长10天  ios无效 3600秒=1小时
                .setApnsProduction(apnsProduction)   // APNs 是否生产环境 True 表示推送生产环境，False 表示要推送开发环境；如果不指定则为推送生产环境。但注意，JPush 服务端 SDK 默认设置为推送 “开发环境”。
                .build();

        // 构建PushPayload
        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(platform)              // 平台
                .setAudience(audience)              // 目标
                .setNotification(notification)      // 通知对象
                .setOptions(options)                // 推送参数
                .build();
        return payload;
    }




    // 上传推送文件
    public FileUploadResult uploadPushFile(String fileName,FileType type) {
//        ClientConfig conf = ClientConfig.getInstance();
//        conf.setReadTimeout(readTimeout);     // 设置超时
        SysPushDto sysPushDto = sysPushService.getByType(Constants.EVNumber.one);
       String MASTER_SECRET = sysPushDto.getSecret();
        String APP_KEY = sysPushDto.getPushKey();
        FileClient fileClient = new FileClient(MASTER_SECRET, APP_KEY);
        try {
            // 上传文件
            FileUploadResult result = fileClient.uploadFile(type, fileName);
            log.info("messageSend==JPush==uploadPushFile==file_id==" + result.getFile_id());
            return result;
        } catch (APIConnectionException e) {
            log.error("messageSend==JPush==uploadPushFile==APIConnectionException==" + e);
        } catch (APIRequestException e) {
            log.error("messageSend==JPush==uploadPushFile==APIRequestException==status=="
                    + e.getStatus() + "==Error Code==" +e.getErrorCode() + "==Error Message==" + e.getErrorMessage() + "==Msg ID==" + e.getMsgId());
        }
        return null;
    }

    // 查询有效文件
    public FileModelPage testQueryEffFiles() {
        SysPushDto sysPushDto = sysPushService.getByType(Constants.EVNumber.one);
        String MASTER_SECRET = sysPushDto.getSecret();
        String APP_KEY = sysPushDto.getPushKey();
        FileClient fileClient = new FileClient(MASTER_SECRET, APP_KEY);
        try {
            FileModelPage result = fileClient.queryEffectFiles();
            log.info("queryEffFiles:{}", result);
        } catch (APIConnectionException e) {
            log.error("Connection error. Should retry later. ", e);
        } catch (APIRequestException e) {
            log.error("Error response from JPush server. Should review and fix it. ", e);
            log.info("HTTP Status: " + e.getStatus());
            log.info("Error Code: " + e.getErrorCode());
            log.info("Error Message: " + e.getErrorMessage());
            log.info("Msg ID: " + e.getMsgId());
        }
        return null;
    }

    // 删除文件
    public void deletePushFile(String fileId) {
        SysPushDto sysPushDto = sysPushService.getByType(Constants.EVNumber.one);
        String MASTER_SECRET = sysPushDto.getSecret();
        String APP_KEY = sysPushDto.getPushKey();
        FileClient fileClient = new FileClient(MASTER_SECRET, APP_KEY);
        try {
            fileClient.deleteFile(fileId);
            log.info("messageSend==JPush==deletePushFile==file_id==" + fileId);
        } catch (APIConnectionException e) {
            log.error("messageSend==JPush==deletePushFile==APIConnectionException==" + e);
        } catch (APIRequestException e) {
            log.error("messageSend==JPush==deletePushFile==APIRequestException==status=="
                    + e.getStatus() + "==Error Code==" +e.getErrorCode() + "==Error Message==" + e.getErrorMessage() + "==Msg ID==" + e.getMsgId());
        }
    }
    

    /**
     * 创建目录，获取文件路径
     * @return
     */
    public static String getFileNamePath(){
        String basePath = getResourceBasePath();                // 项目根路径
        String fileName = System.currentTimeMillis() + ".txt";  // jpushTemp/时间戳.txt
        fileName = pushDir + File.separator  + fileName;
        String fileNamePath =  new File(basePath, fileName).getAbsolutePath();
        // 目录不存在，则创建目录，保证目录一定存在
        ensureDirectory(fileNamePath);
        return fileNamePath;
    }

    /**
     * 获取项目根路径
     * @return
     */
    public static String getResourceBasePath() {
        // 获取跟目录
        File path = null;
        try {
            path = new File(ResourceUtils.getURL("classpath:").getPath());
        } catch (FileNotFoundException e) {
            log.error("messageSend==JPush==getResourceBasePath==" + e);
        }
        if (path == null || !path.exists()) {
            path = new File("");    // linux的根目录
        }

        String pathStr = path.getAbsolutePath();
        // 如果是在eclipse中运行，则和target同级目录,如果是jar部署到服务器，则默认和jar包同级
        pathStr = pathStr.replace("\\target\\classes", "");

        return pathStr;
    }

    /**
     * 目录不存在，则创建目录，保证目录一定存在
     * @param filePath 文件路径
     */
    public static void ensureDirectory(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return;
        }
        filePath = replaceSeparator(filePath);
        if (filePath.indexOf("/") != -1) {
            filePath = filePath.substring(0, filePath.lastIndexOf("/"));
            File file = new File(filePath);
            if (!file.exists()) {
                // 不存在目录则创建目录
                file.mkdirs();
            }
        }
    }

    /**
     * 将符号“\\”和“\”替换成“/”,有时候便于统一的处理路径的分隔符,避免同一个路径出现两个或三种不同的分隔符
     *
     * @param str
     * @return
     */
    public static String replaceSeparator(String str) {
        return str.replace("\\", "/").replace("\\\\", "/");
    }

    /**
     *  生成别名推送文件
     * @param alias
     * @param fileNamePath
     * @throws Exception
     */
    private static void createAliasFile(List<String> alias,String fileNamePath) throws Exception {
        BufferedWriter bufferedWriter = null;
        try {
            FileWriter fileWriter = new FileWriter(fileNamePath);
            bufferedWriter = new BufferedWriter(fileWriter);
            // 别名写入文件
            for(String as : alias){
                bufferedWriter.write(as);  // System.lineSeparator() 系统换行符
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } catch (Exception e) {
            log.error("messageSend==JPush==createAliasFile==" + e);
            throw e;
        }finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (Exception e) {
                    log.error("messageSend==JPush==createAliasFile==bufferedWriter.close()==" + e);
                    throw e;
                }
            }
        }
    }

    public static void main(String[] args) {
        JPushUtil jPushUtil = new JPushUtil();
        JPushNotificationDto dto = new JPushNotificationDto();
        dto.setMsgTitle("标题");
        dto.setMsgContent("内容");
//        jPushUtil.pushToAll(dto);
        List<String> alias = new ArrayList<>();
        // list写入文件
        alias.add("sdytest");
        alias.add("kane");
        dto.setAlias(alias);
        jPushUtil.pushToAliasListByFile(dto);
//        jPushUtil.testQueryEffFiles();
//        System.out.println(JPushUtil.getResourceBasePath());
    }
}

