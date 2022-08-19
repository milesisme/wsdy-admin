package com.wsdy.saasops.modules.sys.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TreeMenuDto {

	private Long id;
	
	private String label;

	List<PermissonDto> children = new ArrayList<>();
}
