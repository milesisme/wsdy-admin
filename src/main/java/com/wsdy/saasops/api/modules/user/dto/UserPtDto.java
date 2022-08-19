package com.wsdy.saasops.api.modules.user.dto;

import org.springframework.util.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="PT会员基本信息")
public class UserPtDto {

	@ApiModelProperty(value="会员登陆名")
	private String loginname;
	@ApiModelProperty(value="kioskname 名")
	private String kioskname;
	@ApiModelProperty(value="adminname 名")
	private String adminname;
	@ApiModelProperty(value="会员密码")
	private String password;
	@ApiModelProperty(value="语言")
	private String language;
	@ApiModelProperty(value="游戏代码")
	private String game;
	@ApiModelProperty(value="密钥")
	private PtEntity ptEntity;
	@ApiModelProperty(value="方法名称")
	private String mod;
	@ApiModelProperty(value="金额")
	private Double amount;
	@ApiModelProperty(value="订单号")
	private String externaltranid;
	@ApiModelProperty(value="仅做查询订单号")
	private String externaltransactionid;
	@ApiModelProperty(value="锁用户")
	private String[] frozen;
	@ApiModelProperty(value="1|0 即：是否强制提款")
	private String isForce;
	@ApiModelProperty(value="解锁用户")
	private String unFrozen;

	@Override
    public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		if (!StringUtils.isEmpty(mod)) {
            buffer.append(mod);
        }
		if (!StringUtils.isEmpty(loginname) && StringUtils.isEmpty(frozen) && StringUtils.isEmpty(unFrozen)) {
            buffer.append("/playername/").append(loginname.toUpperCase());
        }
		
		if (!StringUtils.isEmpty(kioskname)) {
            buffer.append("/kioskname/").append(kioskname);
        }
		
		if (!StringUtils.isEmpty(adminname)) {
            buffer.append("/adminname/").append(adminname);
        }
		
		if (!StringUtils.isEmpty(password)) {
            buffer.append("/password/").append(password);
        }
		
		if (!StringUtils.isEmpty(amount)) {
            buffer.append("/amount/").append(amount);
        }
		
		if (!StringUtils.isEmpty(externaltranid)) {
            buffer.append("/externaltranid/").append(externaltranid);
        }
		
		if (!StringUtils.isEmpty(isForce)) {
            buffer.append("/isForce/").append(isForce);
        }
		
		if (!StringUtils.isEmpty(externaltransactionid)) {
            buffer.append("/externaltransactionid/").append(externaltransactionid);
        }

		if (!StringUtils.isEmpty(frozen)) {
            buffer.append(frozen[0]).append("/"+loginname).append("/frozen/").append(frozen[1]);
        }

		if (!StringUtils.isEmpty(unFrozen)) {
            buffer.append(unFrozen).append("/"+loginname);
        }
/*		if (!StringUtils.isEmpty(adminname))
			buffer.append("/adminname/").append(adminname);
		if (!StringUtils.isEmpty(kioskname))
			buffer.append("/password/").append(password);*/
		return buffer.toString();
	}
	public interface IsForce
	{
		String is="1";//是强制提款
		String no="0";//否强制提款
	}
}
