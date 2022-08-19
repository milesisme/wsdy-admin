package com.wsdy.saasops.modules.sys.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@ApiModel(value = "SysFileExportRecord", description = "异步文件下载记录")
@Table(name = "sys_fileexport_record")
public class SysFileExportRecord implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;
    @ApiModelProperty(value = "用户id")
    private Long userId;
    private String createTime;
    @ApiModelProperty(value = "文件服务器的文件id")
    private String fileName;
    @ApiModelProperty(value = "下载模块名称")
    private String module;
    @ApiModelProperty(value = "1成功 null处理中")
    private Integer status;
    @Transient
    @ApiModelProperty(value = "下载文件名")
    private String downloadFileName;
    @Transient
    @ApiModelProperty(value = "保存异步下载记录成功 success 成功 ， fail失败")
    private String saveFlag;
}
