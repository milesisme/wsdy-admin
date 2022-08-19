package com.wsdy.saasops.modules.member.entity;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户历史最高等级记录表
 * </p>
 *
 * @author ${author}
 * @since 2022-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "mbr_activity_level_highest")
public class MbrActivityLevelHighest implements Serializable {

    private static final long serialVersionUID=1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")    private Integer id;

    /**
     * 会员达到过的最高等级
     */
    private Integer accountlevel;

    /**
     * 会员账号
     */
    private String loginname;
    
    
    /**
     * 	会员最后一次等级恢复时间
     */
    private Date recoverTime;
    
    public MbrActivityLevelHighest() {
    	super();
    }
    
    public MbrActivityLevelHighest(String loginname) {
    	super();
    	this.loginname = loginname;
    }

}
