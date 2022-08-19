package com.wsdy.saasops.api.utils;

import com.wsdy.saasops.modules.fund.entity.AccWithdraw;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by William on 2017/12/8.
 */
@Component
@Slf4j
public class JsonUtil {
    /**
     * 对象转换成json字符串
     * @Description: TODO
     * @param @param obj
     * @param @return
     * @return String
     * @throws
     * @author william
     * @date 2017年12月8日
     */
    public String toJson(Object obj) {
        String result = "";
        Gson gson = new GsonBuilder().serializeNulls().create();
        try{
            result = gson.toJson(obj);
        }catch(Exception e){
            log.error("error:" + e);
        }

        return result;
    }

    /**
     * json字符串转成对象
     * @Description: TODO
     * @param @param str
     * @param @param type
     * @param @return
     * @return T
     * @throws
     * @author william
     * @date 2017年12月8日
     */
    public <T> T fromJson(String str, Type type) {
        Gson gson = new Gson();
        try{
            T t = gson.fromJson(str, type);
            return t;
        }catch(Exception e){
            log.error("error:" + e);
        }
        return null;
    }

    /**
     * json字符串转成对象
     * @Description: TODO
     * @param @param str
     * @param @param type
     * @param @return
     * @return T
     * @throws
     * @author william
     * @date 2017年12月8日
     */
    public <T> T fromJson(String str, Class<T> type) {
        Gson gson = new Gson();
        try{
            T t = gson.fromJson(str, type);
            return t;
        }catch(Exception e){
            log.error("error:" + e);
        }
        return null;
    }

    /**
     * @创建人: william
     * @Method描述: 字符串返回成Map
     * @创建时间: 2017年12月8日
     * @param str Json字符串
     * @return Map
     */
    public Map<String, Object> toMap(String str) {
        Gson gson = new Gson();
        Map<String, Object> gsonMap = gson.fromJson(str, new TypeToken<Map<String, Object>>() {
        }.getType());
        return gsonMap;
    }

    /**
     * Json字符串转JsonArray对象
     * @Description: TODO
     * @param @param strJson
     * @param @return
     * @return JsonObject
     * @throws
     * @author william
     * @date 2017年12月8日
     */
    public JsonArray String2JsonArray(String strJson) {
        JsonParser jsonParser=new JsonParser();
        return jsonParser.parse(strJson).getAsJsonArray();
    }

    /**
     * Json字符串转JsonObject对象
     * @Description: TODO
     * @param @param strJson
     * @param @return
     * @return JsonObject
     * @throws
     * @author william
     * @date 2017年12月8日
     */
    public JsonObject String2JsonObject(String strJson){
        JsonParser jsonParser=new JsonParser();
        strJson = strJson.replace("[", "");
        strJson = strJson.replace("]", "");
        return jsonParser.parse(strJson).getAsJsonObject();
    }

    private <T> Map<String,Object> EntToMap(Object model, Class<T> t, Map<String,Object> map){
        try{
            Field[] fields =  t.getDeclaredFields();
            if(fields.length>0 && map==null) {
                map = new HashMap<String,Object>(16);
            }
            for(Field f:fields){
                String name = f.getName();
                if("serialVersionUID".equals(name)) {
                    continue;
                }
                name = name.substring(0,1).toUpperCase()+name.substring(1); //将属性的首字符大写，方便构造get，set方法
                Method m = model.getClass().getMethod("get"+name);
                Object value = m.invoke(model);
                if(map!=null && value!=null) {
                    map.put(f.getName(), String.valueOf(value));
                } else {
                    map.put(f.getName(), "");
                }
            }
            if(t.getSuperclass()!=null){
                EntToMap(model, t.getSuperclass(), map);
            }
        }catch(Exception e){
            log.error("error:" + e);
        }
        return map;
    }

    /**
     * 实体类转成Map对象
     * @param model
     * @return
     */
    public <T> Map<String, Object> Entity2Map(Object model){
        Map<String, Object> map = null;
        try{
            map = EntToMap(model,model.getClass(),map);
        }catch(Exception e){
            log.error("error:" + e);
        }
        return map;
    }


    private <T> JsonObject EntToObj(Object model, Class<T> t, JsonObject jsonObj){
        try{
            Field[] fields =  t.getDeclaredFields();
            if(fields.length>0 && jsonObj==null) {
                jsonObj = new JsonObject();
            }
            for(Field f:fields){
                String name = f.getName();
                name = name.substring(0,1).toUpperCase()+name.substring(1); //将属性的首字符大写，方便构造get，set方法
                Method m = model.getClass().getMethod("get"+name);
                String value = String.valueOf(m.invoke(model));
                if(jsonObj!=null && value!=null) {
                    jsonObj.addProperty(f.getName(), value);
                } else {
                    jsonObj.addProperty(f.getName(), "");
                }
            }
            if(t.getSuperclass()!=null){
                EntToObj(model, t.getSuperclass(), jsonObj);
            }
        }catch(Exception e){
            log.error("error:" + e);
        }
        return jsonObj;
    }

    /**
     * 实体转为Json对象
     * @param model
     * @return
     */
    public <T> JsonObject Entity2JsonObject(Object model){
        JsonObject jsonObject = null;
        try{
            jsonObject = EntToObj(model, model.getClass(), jsonObject);
        }catch(Exception e){
            log.error("error:" + e);
        }
        return jsonObject;
    }

    public static Map<String, Object> json2map(String str_json) {
        Map<String, Object> res = null;
        try {
            Gson gson = new Gson();
            res = gson.fromJson(str_json, new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
        }
        return res;
    }

    public Map<String, String> toStringMap(Object object) {
        Gson gson = new Gson();
        Map<String, String> gsonMap = gson.fromJson(gson.toJson(object), new TypeToken<Map<String, String>>() {
        }.getType());
        return gsonMap;
    }

    public Map<String, Object> toStringMapObject(Object object) {
        Gson gson = new Gson();
        Map<String, Object> gsonMap = gson.fromJson(gson.toJson(object), new TypeToken<Map<String, Object>>() {
        }.getType());
        return gsonMap;
    }

    public static void main(String[] args){

        AccWithdraw withdraw = new AccWithdraw();
        withdraw.setWithdrawCount(23);
        withdraw.setAccountId(22);
        withdraw.setBankCardId(12);
        withdraw.setLoginSysUserName("dfgg");

        Map map = new JsonUtil().Entity2Map(withdraw);

        System.out.println(new Gson().toJson(map));
    }
}
