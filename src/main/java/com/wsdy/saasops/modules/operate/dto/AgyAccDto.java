package com.wsdy.saasops.modules.operate.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "AgyAccountDto")
public class AgyAccDto {

	//是否全选总代
	private Boolean isAllGen;
	
	//是否全选分代
	private Boolean isAllAgt;
	
	//总代ids
	private List<Integer> genIds;
	
	//分代ids
	private List<Integer> agtIds;

	//用户名
	private String loginName;
	
}
