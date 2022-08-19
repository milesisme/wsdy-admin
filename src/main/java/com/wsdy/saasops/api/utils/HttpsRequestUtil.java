package com.wsdy.saasops.api.utils;


import com.wsdy.saasops.modules.log.entity.LogMbrRegister.RegIpValue;


public class HttpsRequestUtil {
	//将请求头中的dev=PC/H5转换为byte
	public static Byte getHeaderOfDev(String dev) {
		Byte Source=null;
		if (dev != null && !"".equals(dev)) {
			switch (dev.toUpperCase()) {
			case "PC": {
				Source = RegIpValue.pcClient;
				break;
			}
			case "H5": {
				Source = RegIpValue.H5Client;
				break;
			}
			default: {
				Source = RegIpValue.pcClient;
				break;
			}
			}
		}
		return Source;
	}

	//将请求头中的dev=PC/H5转换为byte
	public static Byte getHeaderOfDevEx(String dev) {
		Byte Source=null;
		if (dev != null && !"".equals(dev)) {
			switch (dev.toUpperCase()) {
				case "PC": {
					Source = RegIpValue.pcClient;
					break;
				}
				case "WAP": {	// 注册时WAP送'WAP'，复用原H5
					Source = RegIpValue.H5Client;
					break;
				}
				case "H5": {	// 注册时APP送'H5'
					Source = RegIpValue.appClient;
					break;
				}
				default: {
					Source = RegIpValue.pcClient;
					break;
				}
			}
		}
		return Source;
	}
}