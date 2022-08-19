package com.wsdy.saasops.modules.agent.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 	 渠道分组对象
 * </p>
 *
 * @author daimon
 * @since 2021-11-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "agy_channel_group")
public class AgyChannelGroup implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 自增id
     */
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
    private Integer id;

    /**
     * 分组名称
     */
    @ApiModelProperty(value = "分组名称")
    private String name;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    /**
     * 操作人
     */
    @ApiModelProperty(value = "操作人")
    private String udapyeBy;
    
}
