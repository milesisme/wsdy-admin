package com.wsdy.saasops.common.exception;

import com.wsdy.saasops.common.utils.R;
import com.wsdy.saasops.modules.sys.service.SysI18nService;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理器
 */
@RestControllerAdvice
public class RRExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SysI18nService sysI18nService;

    /**
     * 处理自定义异常: 默认2000，可自定义其他code
     * 目前已使用：401 token相关  10086 账户锁定
     */
    @ExceptionHandler(R200Exception.class)
    public R handleR200Exception(R200Exception e) {
        logger.info(e.getMsg());
        R r = new R();
        r.put("code", e.getCode());
        r.put("msg", e.getMessage());
        // 异步收集多语言
//        sysI18nService.i18nCollect(e.getMessage());
        return r;
    }

    /**
     * 处理自定义异常:默认500
     */
    @ExceptionHandler(RRException.class)
    public R handleRRException(RRException e) {
        logger.error(e.getMessage(), e);
        R r = new R();
        r.put("code", e.getCode());
        r.put("msg", e.getMessage());
        // 异步收集多语言
//        sysI18nService.i18nCollect(e.getMessage());
        return r;
    }

    /**
     * 主键重复异常:500
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public R handleDuplicateKeyException(DuplicateKeyException e) {
        logger.error(e.getMessage(), e);
        return R.error("数据库中已存在该记录");
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        logger.error("缺少请求参数", e);
        return R.error(e.getMessage());
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logger.error("参数解析失败", e);
        return R.error(e.getMessage());
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.error("参数验证失败", e);
        BindingResult result = e.getBindingResult();
        FieldError error = result.getFieldError();
        String field = error.getField();
        String code = error.getDefaultMessage();
        String message = String.format("%s:%s", field, code);
        return R.error(message);
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public R handleBindException(BindException e) {
        logger.error("参数绑定失败", e);
        BindingResult result = e.getBindingResult();
        FieldError error = result.getFieldError();
        String field = error.getField();
        String code = error.getDefaultMessage();
        String message = String.format("%s:%s", field, code);
        return R.error(message);
    }

    /**
     * 400 - Bad Request
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingPathVariableException.class)
    public R handleBindException(MissingPathVariableException e) {
        logger.error("参数绑定失败", e);
        String message = e.getVariableName() + "非法请求";
        return R.error(message);
    }

    /**
     *  权限异常：2001
     */
    @ExceptionHandler(AuthorizationException.class)
    public R handleAuthorizationException(AuthorizationException e) {
        logger.error(e.getMessage(), e);
        return R.error(2001, "没有权限，请联系管理员授权");
    }

    /**
     * 默认异常：500
     */
    @ExceptionHandler(Exception.class)
    public R handleException(Exception e, HttpServletRequest request) {
        logger.error(request.getRequestURI() + "--接口异常"+ e.getMessage(), e);
        // 异步收集多语言
//        sysI18nService.i18nCollect(e.getMessage());
        return R.error();
    }
}