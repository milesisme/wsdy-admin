package com.wsdy.saasops.modules.operate.entity;

import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name = "msgAccTemp")
public class OprMsgAccTemp {
	//会员账号或代理账号
	private String loginName;
	//会员Id
	private Integer memberId;
}
