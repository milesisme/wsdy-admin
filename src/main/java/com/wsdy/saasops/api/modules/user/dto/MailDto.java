package com.wsdy.saasops.api.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MailDto  {
	private String toUsers;
	private String subject;
	private String content;
}