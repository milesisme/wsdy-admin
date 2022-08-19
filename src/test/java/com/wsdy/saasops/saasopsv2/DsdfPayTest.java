package com.wsdy.saasops.saasopsv2;

import com.alibaba.fastjson.JSONObject;
import com.wsdy.saasops.api.modules.pay.service.PaymentService;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.common.utils.QiNiuYunUtil;
import com.wsdy.saasops.common.utils.SnowFlake;
import com.wsdy.saasops.modules.member.service.MbrAccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DsdfPayTest {
    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;

    @Autowired
    private OkHttpService okHttpService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MbrAccountService mbrAccountService;

    // 查询七牛云上所有的文件名
    @Test
    public void payUrl() throws Exception, Exception {
        System.out.println("begin>>>>>>>>>>>>>>>>>>>>>>>>>>");
        JSONObject data = new JSONObject();
        data.put("cid", "934");  //cid
        data.put("uid", "player123");  //uid
        data.put("time", Calendar.getInstance().getTimeInMillis() + "");  //unix时间戳
        data.put("amount", "1.23");   //充值金额
        data.put("order_id", new SnowFlake().nextId() + "");   //充值记录唯一标识
        data.put("category", "qrcode");   //存款方式，remit: 银行卡转账  qrcode: 二维码存款 后台模式无法表单跳转，只能支持银行卡转账和部分二维码
        data.put("from_bank_flag", "ALIPAY");
        //data.put("from_username", "张三");
        //data.put("", "");
        String key = "wErm47W5aa58PEJW4DA7BiGwkoZCDM7TpazPDhAcZKkcNjYdS0iWmxjDTCA4MnEM";
        String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(data.toString().getBytes());
        String sign = Base64.getEncoder().encodeToString(rawHmac.toString().getBytes("utf-8"));
        //String sign = CharacterEncoder.encode(rawHmac);
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("Content-Hmac", sign);
        String result = okHttpService.postJson(okHttpService.getPayHttpsClient(), "https://www.dsdfpay.com/dsdf/api/place_order", data, stringMap);
        System.out.println(result);

   /*     CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://www.dsdfpay.com/dsdf/api/place_order");
		httpPost.addHeader("Content-Hmac", sign);
		httpPost.addHeader("content-type", "application/json");
		httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);*/
        System.out.println("end<<<<<<<<<<<<<<<<<<<<<<<<<<");
    }

    @Test
    public void payBtpUrl() {
        paymentService.updateBtpPay(null,null,"ycs");
    }

    @Test
    public void getDomainCode() {
        String domainCode = mbrAccountService.getDomainCode();
    }


}
