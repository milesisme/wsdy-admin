package com.wsdy.saasops.common.utils;

import com.wsdy.saasops.common.constants.SystemConstants;
import com.wsdy.saasops.common.exception.RRException;
import com.google.common.net.InternetDomainName;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class CommonUtil {

    public static final String ALLCHAR = "0123456789";
    private static Pattern HTTP_PATTERN = Pattern.compile("((?<=http://|\\.)[^.]*?\\.((com)$|(vip)$|(cn)$|(net)$|(org)$|(biz)$|(info)$|(cc)$|(tv)$|(hk)$|(co)$))", Pattern.CASE_INSENSITIVE);
    private static Pattern HTTP_IP_PATTERN = Pattern.compile("((?:(?:25[0-5]|2[0-4]\\d|(?:1\\d{2}|[1-9]?\\d))\\.){3}(?:25[0-5]|2[0-4]\\d|(?:1\\d{2}|[1-9]?\\d)))", Pattern.CASE_INSENSITIVE);
    /**
     * bigDecimal 保留上数位 系统固定2位 不进行四舍五入
     *
     * @param bigDecimal
     * @return
     */
    public static BigDecimal adjustScale(BigDecimal bigDecimal) {
        return bigDecimal.setScale(2, BigDecimal.ROUND_DOWN);
    }

    public static BigDecimal adjustScale(BigDecimal bigDecimal,int scale) {
        return bigDecimal.setScale(scale, BigDecimal.ROUND_DOWN);
    }

    public static BigDecimal adjustScaleUp(BigDecimal bigDecimal) {
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取Ip
     *
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = null;
        try {
          /*  ip = request.getHeader("remote_addr");
            log.info("获取用户remote_addr:" + request.getHeader("remote_addr"));
            if (StringUtils.isNotEmpty(ip)) {
                return ip;
            }*/
          /*  log.info("X-Real-IP:" + request.getHeader("X-Real-IP")
                    + ",x-forwarded-for:" + request.getHeader("x-forwarded-for")
                    + ",Proxy-Client-IP:" + request.getHeader("Proxy-Client-IP")
                    + ",remote_addr:" + request.getHeader("remote_addr")
                    + ",WL-Proxy-Client-IP:" + request.getHeader("WL-Proxy-Client-IP")
                    + ",HTTP_CLIENT_IP:" + request.getHeader("HTTP_CLIENT_IP")
                    + ",HTTP_X_FORWARDED_FOR:" + request.getHeader("HTTP_X_FORWARDED_FOR")
                    + ",X-Forwarded-For:" + request.getHeader("X-Forwarded-For")
                    + ",域名:" + IpUtils.getUrl(request)
                    + ",完整路径:" + request.getRequestURL()
            );*/
            ip = request.getHeader("X-Forwarded-For");
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("remote_addr");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.error("获取IP异常", e);
        }
        //使用代理，则获取第一个IP地址
        if (StringUtils.isNotEmpty(ip) && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
                String[] iparr = ip.split(":");
                ip = iparr.length > 2 ? ip : iparr[0];
            }
        }
        return ip;
    }


    public static String requestUrl(String weburl) {
        java.net.URL url = null;
        try {
            url = new java.net.URL(weburl);
        } catch (MalformedURLException e) {
            log.error("error:" + e);
        }
        String host = url.getHost();// 获取主机名
        return host;

    }

    /**
     * 去除空行和空格
     *
     * @param item
     * @return
     */
    public static String remKong(String item) {

        return item != null ? item.replaceAll("\\n", "").replaceAll("\\s", "") : null;
    }

    public static String genRandomNum(int min, int max) {
        int maxNum = 36;
        int i;
        int count = 0;
        char[] str = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
                'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
                'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        StringBuffer pwd = new StringBuffer("");
        Random r = new Random();
        int s = r.nextInt(max) % (max - min + 1) + min;
        while (count < s) {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }
        return pwd.toString();
    }

    public static String genRandom(int min, int max) {
        int maxNum = 36;
        int i;
        int count = 0;
        char[] str = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        StringBuffer pwd = new StringBuffer("");
        Random r = new Random();
        int s = r.nextInt(max) % (max - min + 1) + min;
        while (count < s) {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length) {
                pwd.append(str[i]);
                count++;
            }
        }
        return pwd.toString();
    }

    public static String genRandomNum(int number) {
        return genRandomNum(number, number);
    }

    public static String getRandomNum() {
        return genRandomNum(12);
    }

    public static String getRandomCode() {
        return genRandom(5, 5);
    }

    public static String getSiteCode() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            if (request != null) {
                return AESUtil.decrypt(request.getHeader(SystemConstants.STOKEN));
            }
            return null;
        }
        return null;
    }

    /**
     * 根据URL获取一级 domain
     *
     * @param url
     * @return
     */
    public static String getDomainForUrl(String url) {
        String domainUrl = null;
        if (url == null) {
            return null;
        } else if (url.contains("localhost")) {
            return "localhost";
        } else {
            Pattern p = HTTP_PATTERN;
            Matcher matcher = p.matcher(url);
            while (matcher.find()) {
                domainUrl = matcher.group();
            }
            //增加对ip地址的支持
            if (!matcher.find()) {
                p = HTTP_IP_PATTERN;
                matcher = p.matcher(url);
                while (matcher.find()) {

                    domainUrl = requestUrl(url.startsWith("http://") ? url : "http://" + url);
                }
            }
            return StringUtil.isNotEmpty(domainUrl) ? domainUrl : url;
        }
    }

    /**
     * 获取顶级域名处理
     * @param url
     * @return
     */
    public static String getDomainForUrlEx(String url) {
        // 去掉端口
        url = getUrlWithoutPort(url);
        if(StringUtil.isEmpty(url)){
            return null;
        }
        String domainUrl = null;
        if (url.contains("localhost")) {
            return "localhost";
        } else {
            // 先处理ip
            Pattern p = HTTP_IP_PATTERN;
            Matcher matcher = p.matcher(url);
            while (matcher.find()) {
                domainUrl = requestUrl(url.startsWith("http://") ? url : "http://" + url);
                return StringUtil.isNotEmpty(domainUrl) ? domainUrl : url;
            }
            // 再处理非IP
            try{
                InternetDomainName domainName = InternetDomainName.from(url);
                domainUrl = domainName.topPrivateDomain().toString();
            }catch(Exception e){
                log.error("getDomainForUrlEx==InternetDomainName解析错误==非法的url==" + url);
            }
            return StringUtil.isNotEmpty(domainUrl) ? domainUrl : url;
        }
    }

    public static String getUrlWithoutPort(String url) {
        if(StringUtil.isEmpty(url)){
            return "";
        }
        String[] urlArr = null;
        if(url.startsWith("http://") && url.split("http://").length > 1){
            urlArr = url.split("http://")[1].split(":");
        }else if(url.startsWith("https://") && url.split("https://").length > 1){
            urlArr = url.split("https://")[1].split(":");
        }else{
            urlArr = url.split(":");
        }
        return urlArr[0];
    }

    public static byte[] getImagesByte(String base64Data) {
        String dataPrix;
        String data;
        if (org.apache.commons.lang.StringUtils.isEmpty(base64Data)) {
            throw new RRException("上传失败，上传图片数据为空");
        } else {
            String[] d = base64Data.split("base64,");
            if (Objects.nonNull(d) && d.length == 2) {
                dataPrix = d[0];
                data = d[1];
            } else {
                throw new RRException("上传失败，数据不合法");
            }
        }
        if ("data:image/jpeg;".equalsIgnoreCase(dataPrix) || "data:image/x-icon;".equalsIgnoreCase(dataPrix)
                || "data:image/gif;".equalsIgnoreCase(dataPrix)
                || "data:image/png;".equalsIgnoreCase(dataPrix)) {//data:image/jpeg;base64,base64编码的jpeg图片数据
            return Base64Utils.decodeFromString(data);
        }
        throw new RRException("上传图片格式不合法");
    }

    public static String getBase64FromInputStream(BufferedImage in) {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(in, "jpg", outputStream);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            log.error("error:" + e);
        }
        // 对字节数组Base64编码
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(outputStream.toByteArray());
    }

    public static Object getKey(Map map, Object value) {
        for (Object key : map.keySet()) {
            if (map.get(key).equals(value)) {
                return key;
            }
        }
        return null;
    }

    /**
     * <获取参数map>
     *
     * @return 参数map
     * @throws Exception
     */
    public static Map<String, Object> getParameterMap(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, String[]> tempMap = request.getParameterMap();
        Set<String> keys = tempMap.keySet();
        for (String key : keys) {
            resultMap.put(key, request.getParameter(key));
        }
        return resultMap;
    }

    public static String generateString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
        }
        return sb.toString().toLowerCase();
    }


    /**
     * map按value排序
     * flag = 1 正序
     * flag = 0 倒序
     * @param map
     * @param flag
     * @return
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> mapSortByValue(Map<K, V> map, int flag) {

        if (flag == 1) {
            return map.entrySet().stream().sorted((o1, o2) -> o1.getValue().compareTo(o2.getValue())).map(entry -> {
                Map<K, V> result = new LinkedHashMap<>();
                result.put(entry.getKey(), entry.getValue());
                return result;
            }).reduce((map1, map2) -> {
                map2.entrySet().forEach(entry -> map1.put(entry.getKey(), entry.getValue()));
                return map1;
            }).get();
        } else {
            return map.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).map(entry -> {
                Map<K, V> result = new LinkedHashMap<>();
                result.put(entry.getKey(), entry.getValue());
                return result;
            }).reduce((map1, map2) -> {
                map2.entrySet().forEach(entry -> map1.put(entry.getKey(), entry.getValue()));
                return map1;
            }).get();
        }
    }
}
