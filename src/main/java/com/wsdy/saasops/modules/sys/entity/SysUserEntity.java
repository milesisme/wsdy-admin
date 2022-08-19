package com.wsdy.saasops.modules.sys.entity;

import com.wsdy.saasops.common.validator.group.AddGroup;
import com.wsdy.saasops.common.validator.group.UpdateGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 系统用户
 */
@Setter
@Getter
@ToString
public class SysUserEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 用户名
	 */
	@NotBlank(message = "用户名不能为空", groups = { AddGroup.class, UpdateGroup.class })
	private String username;

	/**
	 * 密码
	 */
	@NotBlank(message = "密码不能为空", groups = AddGroup.class)
	//@JsonIgnore
	private String password;
	
	//@NotBlank(message = "新密码不能为空", groups = AddGroup.class)
	//@JsonIgnore
	/**
	 * 新密码
	 */
	private String newPassword;

	/**
	 * 安全密码
	 */
	@NotBlank(message = "安全密码不能为空", groups = AddGroup.class)
	//@JsonIgnore
	private String securepwd;

	/**
	 * 盐
	 */
	@JsonIgnore
	private String salt;

	/**
	 * 邮箱
	 */
	//@NotBlank(message = "邮箱不能为空", groups = { AddGroup.class, UpdateGroup.class })
	@Email(message = "邮箱格式不正确", groups = { AddGroup.class, UpdateGroup.class })
	private String email;

	/**
	 * 手机号
	 */
	private String mobile;

	/**
	 * 状态 0：禁用 1：正常
	 */
	private Integer status;

	/**
	 * 角色ID列表
	 */
	private List<Long> roleIdList;
    /**
     * 角色ID
     */
    private Integer roleId;
    
    private Integer deptId ;

	/**
	 * 创建者ID
	 */
	private Long createUserId;

	/**
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 修改者id
	 */
	private Long modifyUserId;
	
	/**
	 * 修改时间
	 */
	private Date modifyTime;
	
	/**
	 * 站点信息
	 */
	private String websiteTile;

	private String authenticatorKey;

	private Integer authenticatorLogin;

	private String ip;

	public interface ErrorCode
	{
		//密码与原密码相同
		int code_01=100;
		//密码与安全密码相同
		int code_02=200;
		//安全码与原安全密码相同
		int code_03=300;
		//安全码与密码相同
		int code_04=400;
		//原密码错误
		int code_05=0;
		//安全密码错误
		int code_06=600;
		//成功
		int code_07=700;
	}

	private String expireTime;
	/**
	 *真实姓名
	 */
	private String realName;

	// 用户对应会员组权限 1: 全部 2:自定义
	private Integer userMbrGroupAuth;

	// 用户对应代理权限 1: 全部 2:自定义
	private Integer userAgyAccountAuth;

	// 1: 未删除 2:已删除
	private Integer isDelete;


	// 用户绑定的分机号
	private String telExtNo;

	@Transient
    private String roleName;
    @Transient
	private List<SysUserMbrgrouprelation> mbrGroups; //会员组权限
    @Transient
    private List<SysUserAgyaccountrelation> agyAccounts; //代理权限
    @Transient
    private String token;
	@Transient
    private String domainUrl;
	@Transient
	private String merchantId;
	@Transient
	private List<Integer> roleNameList;
	@Transient
	private List<Integer> statusList;
	@Transient
	private Integer offset;
	@Transient
	private String sidx;
	@Transient
	private String order;
	@Transient
	private Integer pageNo;
	@Transient
	private Integer pageSize;

	private Date lastLoginTime;

}
