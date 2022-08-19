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
public class ColumnAuthTreeDto {

	/**
	 * 菜单id
	 */
	private Long menuId;
	
	/**
	 * 父id
	 */
	private Long parentId;
	
	/**
	 * 列权限名称
	 */
	private String name;
	
	/**
	 * url
	 */
	private String url;
	
	/**
	 * 权限参数
	 */
	private String perms;
	
	/**
	 * 类型 3：列权限 4：列表展示字段 5：搜索展示字段
	 */
	private Long type;
	
	/**
	 * 排序
	 */
	private Long orderNum;
	
	/**
	 * 列名称
	 */
	private String columnName;
	
	/**
	 * 字段key
	 */
	private String columnKey;
	
	/**
	 * 操作 1:新增 2：修改 3：删除
	 */
	private Long operate;
	
	private List<ColumnAuthTreeDto> childList = new ArrayList<>();
	
	/**
	 * 角色id
	 */
	private Integer roleId;
	
	private Set<Long> paramList;
}
