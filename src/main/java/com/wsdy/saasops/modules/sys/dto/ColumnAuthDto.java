package com.wsdy.saasops.modules.sys.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Setter
@Getter
public class ColumnAuthDto {

	private Long menuId;
	
	private Long parentId;
	
	private String name;
	
	private String url;
	
	private String perms;
	
	private Long type;
	
	private Long orderNum;
	
	private String columnName;
	
	private String columnKey;
	
	private Long operate;
	
	private List<ColumnAuthDto> childList = new ArrayList<>();
	
	private Long roleId;
	
	private Set<Long> paramList;
}
