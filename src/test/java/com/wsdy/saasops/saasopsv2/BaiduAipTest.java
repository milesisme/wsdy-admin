package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.common.utils.BaiduAipUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BaiduAipTest {
	@Autowired
	private BaiduAipUtil baiduAipUtil;

	@Test
	public void getAccessToken() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
		baiduAipUtil.getAuth();

		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void bankCardOCR() {
		System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");

		System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

}
