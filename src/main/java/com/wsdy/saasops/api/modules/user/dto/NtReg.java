package com.wsdy.saasops.api.modules.user.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class NtReg {
	@Value("zhang")
	String firstName;
	@Value("shang")
	String lastName;
	@Value("china")
	String address;
	@Value("beijin")
	String city;
	@Value("cn")
	String country;
	@Value("0086")
	String postalCode;
	@Value("1990-08-08")
	String birthDate;
	@Value("ZH")
	String language;
	@Value("CNY")
	String iso3CurrencyCode;

}