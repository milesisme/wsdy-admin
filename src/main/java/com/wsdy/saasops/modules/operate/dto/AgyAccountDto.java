package com.wsdy.saasops.modules.operate.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "AgyAccountDto")
public class AgyAccountDto {
	
	//总代ids
	private List<Integer> genAgtIds;
	//用户名
	private String agyAccount;
	//总代id
	private Integer genAgtId;
	//代理ids
	private List<Integer> agtIds;
}
