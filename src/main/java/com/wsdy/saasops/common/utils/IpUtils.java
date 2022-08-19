package com.wsdy.saasops.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class IpUtils {

	// 获取ip地址
	public static String getIp() {
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes sra = (ServletRequestAttributes) ra;
		HttpServletRequest request = sra.getRequest();

		String ip = request.getHeader("x-forwraded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}




	// 获取当前操作系统名称
	public static String getOsName() {
		return System.getProperty("os.name");
	}

	public static String getUrl(HttpServletRequest request) {
		String domain = request.getHeader("domain");
		log.info("获取域名domain"+domain);
		if (StringUtils.isNotEmpty(domain)){
			return domain;
		}
		log.info("获取域名"+request.getHeader("Referer"));
		try {
			if(StringUtils.isEmpty(request.getHeader("Referer"))){
				return null;
			}
			String[] strArr = request.getHeader("Referer").split("//");
			String[] urlArr = strArr[1].split("/");
			return urlArr[0];
		}catch (Exception e){
			log.info("getUrl==e=="+e);
			return null;
		}
	}
}
