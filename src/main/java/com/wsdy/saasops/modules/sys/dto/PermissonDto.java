package com.wsdy.saasops.modules.sys.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PermissonDto {	
	
	Long id;
	
	String label;
	
	List<PermissionDetailDto> children = new ArrayList<>();
}
