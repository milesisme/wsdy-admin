package com.wsdy.saasops.common.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 自定义异常
 */
@Setter
@Getter
public class R200Exception extends RuntimeException {
	private static final long serialVersionUID = 1L;

    private String msg;
    private int code = 2000;

    public R200Exception(String msg) {
		super(msg);
		this.msg = msg;
	}

	public R200Exception(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	public R200Exception(String msg, int code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public R200Exception(String msg, int code, Throwable e) {
		super(msg, e);
		this.msg = msg;
		this.code = code;
	}
}
