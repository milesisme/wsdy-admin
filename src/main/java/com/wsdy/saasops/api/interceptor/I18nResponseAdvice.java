package com.wsdy.saasops.api.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.internal.Lists;
import com.google.gson.JsonArray;
import com.wsdy.saasops.api.modules.apisys.dao.TI18nMapper;
import com.wsdy.saasops.api.modules.apisys.entity.TI18n;
import com.wsdy.saasops.api.modules.user.service.RedisService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.RedisConstants;
import com.wsdy.saasops.common.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@ControllerAdvice
public class I18nResponseAdvice implements ResponseBodyAdvice<Object> {
    @Autowired
    private RedisService redisService;
    @Autowired
    private TI18nMapper t18nMapper;
    @Autowired
    private JsonUtil jsonUtil;

    // 中文正则判断
    private static final Pattern chinesePattern = Pattern.compile("[\u4e00-\u9fa5]");;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 是否请求多语言过滤
        List<String> i18nList = request.getHeaders().get("i18n");
        // 语种
        List<String> languageList = request.getHeaders().get("language");
        if(Objects.isNull(languageList) || languageList.size() == 0){
            return body;
        }
        // uri过滤
        String requestUri = ((ServletServerHttpRequest) request).getServletRequest().getRequestURI();
        if(!filterUri(requestUri)){
            return body;
        }
        log.info("==ti18n==请求URL=={}==start", requestUri);

        // 多语言标志
        String language = languageList.get(0);

        // 判断redis中是否存在缓存，存在则取redis缓存
        Object obj = redisService.getRedisValus(RedisConstants.I18N + language);
        if (Objects.nonNull(obj) && !"[]".equals(obj.toString())) {
            log.info("==ti18n==请求URL=={}==取redis进行翻译==start", requestUri);
            // 多语言翻译
            JSONObject jsonObj= JSON.parseObject(jsonUtil.toJson(body));
            recursiveTransJson(jsonObj, JSON.parseArray(obj.toString()), language);
            log.info("==ti18n==请求URL=={}==取redis进行翻译==end", requestUri);
            return jsonObj;
        }

        log.info("==ti18n==请求URL=={}==取数据库进行翻译==start", requestUri);
        // 查询库中是否有该多语言版本
        TI18n record = new TI18n();
        record.setI18nflag(language);
        List<TI18n> recordList = t18nMapper.select(record);
        if(Objects.isNull(recordList)){
            return body;
        }

        String value = jsonUtil.toJson(recordList);
        // 多语言翻译
        JSONObject jsonObj = JSON.parseObject(jsonUtil.toJson(body));
        recursiveTransJson(jsonObj, JSON.parseArray(value), language);
        log.info("==ti18n==请求URL=={}==取数据库进行翻译==end", requestUri);

        // 保存多语言redis
        redisService.setRedisExpiredTimeBo(RedisConstants.I18N + language, value, 10, TimeUnit.MINUTES);

        return jsonObj;

        /* 旧逻辑
        // 多语言标志
        String i18n = i18nList.get(0);
        // 查询库中是否有该多语言版本
        TI18n record = new TI18n();
        record.setI18nflag(i18n);
        List<TI18n> recordList = t18nMapper.select(record);
        if(Objects.isNull(recordList)){
            return body;
        }

        // 判断redis中是否存在缓存，不存在则新增redis缓存
        Object obj = redisService.getRedisValus(RedisConstants.I18N + i18n);
        if (Objects.isNull(obj) || "[]".equals(obj.toString())) {

            // 保存多语言redis
            String value = jsonUtil.toJson(recordList);
            redisService.setRedisValue(RedisConstants.I18N + i18n, value);

            // 多语言翻译
            JSONObject jsonObj= JSON.parseObject(jsonUtil.toJson(body));
            recursiveTransJson(jsonObj, value);

            return jsonObj;
        }

        // 多语言翻译
        JSONObject jsonObj = JSON.parseObject(jsonUtil.toJson(body));
        recursiveTransJson(jsonObj, obj.toString());

        return jsonObj;
        */
    }

    /**
     * 递归替换json中的value值
     * @param objJson
     * @param redisJson
     */
    public void recursiveTransJson(Object objJson, JSONArray redisJson, String language){
        // 如果是json数组
        if(objJson instanceof JSONArray){
            JSONArray objArray = (JSONArray)objJson;
            objArray.stream().forEach( ob ->{
                    recursiveTransJson(ob,redisJson, language);
            });
        }
        //如果为json对象
        else if(objJson instanceof JSONObject){
            JSONObject jsonObject = (JSONObject)objJson;
            for(String key:jsonObject.keySet()){
                Object object = jsonObject.get(key);
                //如果key中是数组
                if(object instanceof JSONArray){
                    JSONArray objArray = (JSONArray)object;
                    recursiveTransJson(objArray,redisJson, language);
                }
                //如果key中是json对象
                else if(object instanceof JSONObject){
                    recursiveTransJson((JSONObject)object,redisJson, language);
                }
                //如果key中是其他
                else if(Objects.nonNull(object)){
                    // 需要翻译的字段必须包含中文才进行翻译
                    Matcher m = chinesePattern.matcher(object.toString());
                    if (!m.find()) {
                        continue;
                    }
                    //log.info("==ti18n进行JSON转换==当前时间秒数=={}", System.currentTimeMillis() / 1000);
                    Boolean iftrans = false;
                    for (Object ob : redisJson) {
                        JSONObject transJson = (JSONObject)ob;
                        if(transJson.get("source").equals(object.toString())){
                            if (StringUtil.isNotEmpty(transJson.getString("translate"))) {
                                jsonObject.put(key, transJson.get("translate"));
                            } else {
                                /*TI18n getnew = new TI18n();
                                getnew.setSource(object.toString());
                                getnew.setI18nflag(language);
                                getnew.setLanguage(language);
                                TI18n nowData = t18nMapper.selectOne(getnew);
                                if (StringUtil.isNotEmpty(nowData.getTranslate())) {
                                    jsonObject.put(key, transJson.get("translate"));
                                }*/
                            }
                            iftrans = true;
                        }
                    }
                    if (!iftrans) {
                        TI18n notMatch = new TI18n();
                        notMatch.setSource(object.toString());
                        notMatch.setI18nflag(language);
                        notMatch.setLanguage(language);
                        List<TI18n> exist = t18nMapper.select(notMatch);
                        if (exist == null || exist.isEmpty()) {
                            if (object.toString().length() > 1024) {
                                log.info("==发现超长i18n转换字符串，不进行记录source=={}", object.toString().substring(0, 1024));
                            } else {
                                log.info("==发现i18n中不存在的需要翻译的中文source=={}==进行记录", object.toString());
                                t18nMapper.insert(notMatch);
                            }
                        }
                    }
                    /*redisJsonObj.stream().forEach( ob ->{
                        JSONObject transJson = (JSONObject)ob;
                        if(transJson.get("source").equals(object.toString())){
                            jsonObject.put(key, transJson.get("translate"));
                        }
                    });*/
                }
            }
        }
    }

    // 判断是否翻译
    public static boolean  filterUri(String requestUri){
        List<String> enableUri = enableUri();
        List<String> disableUri = disableUri();
        for(String uri : disableUri){
            if(requestUri.contains(uri)){
                return false;
            }
        }
        for(String uri : enableUri){
            if(requestUri.contains(uri)){
                return true;
            }
        }
        return false;
    }

    // 翻译rest列表
    public static List<String> enableUri() {
        return Lists.newArrayList("/api/","/bkapi/");
    }

    // 禁止翻译rest列表
    public static List<String> disableUri() {
        return Lists.newArrayList("/api/user/captcha","/verifyEgDepot","/api/sys/getSiteCode","/api/sys/findSiteCode",
                "/api/OnlinePay/pzPay/paiZiCallback", "/tuolyCallback","/api/callback/",
                "/bkapi/sys/login","/bkapi/sys/getSiteCode","/bkapi/sys/googleAvailable","/bkapi/sys/authenticatorLogin","/bkapi/sys/getI18n",
                "/bkapi/sys/getEgSanGongFlg","/bkapi/file/export");
    }
}
