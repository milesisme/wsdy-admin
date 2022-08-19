package com.wsdy.saasops.api.resolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.wsdy.saasops.api.annotation.LoginUser;
import com.wsdy.saasops.api.constants.ApiConstants;
import com.wsdy.saasops.modules.member.entity.MbrAccount;
import com.wsdy.saasops.modules.member.service.MbrAccountService;

/**
 * 有@LoginUser注解的方法参数，注入当前登录用户
 */
@Component
public class LoginUserHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
	@Autowired
	private MbrAccountService userService;
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().isAssignableFrom(MbrAccount.class)
				&& parameter.hasParameterAnnotation(LoginUser.class);
	}
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container, NativeWebRequest request,
			WebDataBinderFactory factory){
		// 获取用户ID
		Integer object = (Integer) request.getAttribute(ApiConstants.USER_ID, RequestAttributes.SCOPE_REQUEST);
		//String loginName = (String) request.getAttribute(ApiConstants.USER_NAME, RequestAttributes.SCOPE_REQUEST);
		
		String prefix = (String) request.getAttribute(ApiConstants.WEB_SITE_PREFIX, RequestAttributes.SCOPE_REQUEST);
		if (object == null) {
			return null;
		}
		//加入缓存中
		MbrAccount user = userService.queryObject(object, prefix);
		return user;
	}
}
