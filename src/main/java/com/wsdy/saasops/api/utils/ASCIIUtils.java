package com.wsdy.saasops.api.utils;

import org.apache.commons.lang.StringUtils;

import java.net.URLEncoder;
import java.util.*;

/**
 *  ASCII 码递排序
 * Created by William on 2017/12/7.
 */
public class ASCIIUtils {

    /**
     * 递增排序
     * @param paramsStr 请求参数
     * @return
     */
    public String Increment(String paramsStr){
        char[] ch = new char[3];

        return null;
    }
    /**
     *
     * 方法用途: 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序），并且生成url参数串<br>
     * 实现步骤: <br>
     *
     * @param paraMap   要排序的Map对象
     * @param urlEncode   是否需要URLENCODE
     * @param keyToLower    是否需要将Key转换为全小写
     *            true:key转化成小写，false:不转化
     * @return
     */
    public static String formatUrlMap(Map<String, Object> paraMap, boolean urlEncode, boolean keyToLower)
    {
        String buff = "";
        Map<String, Object> tmpMap = paraMap;
        try
        {
            List<Map.Entry<String, Object>> infoIds = new ArrayList<>(tmpMap.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            Collections.sort(infoIds, (o1, o2) -> (o1.getKey()).toString().compareTo(o2.getKey()));
            // 构造URL 键值对的格式
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, Object> item : infoIds)
            {
                if (StringUtils.isNotBlank(item.getKey())&& !"subject".equalsIgnoreCase(item.getKey()) &&!"extra".equalsIgnoreCase(item.getKey()) && item.getValue() != null && StringUtils.isNotBlank(item.getValue().toString())&& !"null".equals(item.getValue()) )
                {
                    String key = item.getKey();
                    String val = item.getValue().toString();
                    if (urlEncode)
                    {
                        val = URLEncoder.encode(val, "utf-8");
                    }
                    if (keyToLower)
                    {
                        buf.append(key.toLowerCase() + "=" + val);
                    } else
                    {
                        buf.append(key + "=" + val);
                    }
                    buf.append("&");
                }

            }
            buff = buf.toString();
            if (buff.isEmpty() == false)
            {
                buff = buff.substring(0, buff.length() - 1);
            }
        } catch (Exception e)
        {
            return null;
        }
        return buff;
    }


    public static String formatUrlMap2(Map<String, String> paraMap, boolean urlEncode, boolean keyToLower)
    {
        String buff = "";
        Map<String, String> tmpMap = paraMap;
        try
        {
            List<Map.Entry<String, String>> infoIds = new ArrayList<>(tmpMap.entrySet());
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
            Collections.sort(infoIds, (o1, o2) -> (o1.getKey()).toString().compareTo(o2.getKey()));
            // 构造URL 键值对的格式
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, String> item : infoIds)
            {
                if (StringUtils.isNotBlank(item.getKey())&& !"subject".equalsIgnoreCase(item.getKey()) &&!"extra".equalsIgnoreCase(item.getKey()) && item.getValue() != null && StringUtils.isNotBlank(item.getValue().toString())&& !"null".equals(item.getValue()) )
                {
                    String key = item.getKey();
                    String val = item.getValue().toString();
                    if (urlEncode)
                    {
                        val = URLEncoder.encode(val, "utf-8");
                    }
                    if (keyToLower)
                    {
                        buf.append(key.toLowerCase() + "=" + val);
                    } else
                    {
                        buf.append(key + "=" + val);
                    }
                    buf.append("&");
                }

            }
            buff = buf.toString();
            if (buff.isEmpty() == false)
            {
                buff = buff.substring(0, buff.length() - 1);
            }
        } catch (Exception e)
        {
            return null;
        }
        return buff;
    }

    public static String getFormatUrl(Map<String, Object> paraMap, String key) {
        String formatUrl = formatUrlMap(paraMap, false, false);
        return formatUrl + "&key=" + key;
    }
}
