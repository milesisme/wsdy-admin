package com.wsdy.saasops.saasopsv2;

import lombok.extern.log4j.Log4j2;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.xml.transform.Result;


@Log4j2
@SpringBootTest
public class HttpOk {

    /**
     * 以get方式调用第三方接口
     * @param url
     * @return
     */
    public static String doGet(String url) {
        //创建HttpClient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
        httpGet.addHeader("X-Key","MTI5OTc6WmFSMFFVMzREcHFta2NOSENkWVFLZHRiTmFrclFqTXI=");
        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //返回json格式
                String res = EntityUtils.toString(response.getEntity());
                return res;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void Tests01(){
        String result = doGet("http://v2.api.iphub.info/ip/8.8.8.8");
        JSONObject jo = JSONObject.parseObject(new String(result));
        System.out.println("this  : " + jo.getString("asn"));
        System.out.println("this  : " + jo.getString("block"));

    }
}
