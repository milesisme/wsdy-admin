package com.wsdy.saasops.common.utils;

import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.modules.sys.entity.SysUserEntity;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

/**
 * Shiro工具类
 *
 */
public class ShiroUtils {

	public static Session getSession() {
		return SecurityUtils.getSubject().getSession();
	}

	public static Subject getSubject() {
		return SecurityUtils.getSubject();
	}

	public static SysUserEntity getUserEntity() {
		return (SysUserEntity) SecurityUtils.getSubject().getPrincipal();
	}

	public static Long getUserId() {
		return getUserEntity().getUserId();
	}

	public static void setSessionAttribute(Object key, Object value) {
		getSession().setAttribute(key, value);
	}

	public static Object getSessionAttribute(Object key) {
		return getSession().getAttribute(key);
	}

	public static boolean isLogin() {
		return SecurityUtils.getSubject().getPrincipal() != null;
	}

	public static String getKaptcha(String key) {
		Object kaptcha = getSessionAttribute(key);
		if (kaptcha == null) {
			throw new RRException("验证码已失效");
		}
		getSession().removeAttribute(key);
		return kaptcha.toString();
	}

	public static void setCookie(HttpServletRequest request, HttpServletResponse response) {
		String sessionId = request.getSession().getId();
        Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			Cookie cookie = new Cookie(ApiConstants.COOKIE_JSESSIONID, sessionId);
			cookie.setPath("/");
			response.addCookie(cookie);
		}

	}

	public static String getSessionId(HttpServletRequest request) {
		return request.getSession().getId();
	}
}
