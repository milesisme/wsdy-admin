package com.wsdy.saasops.saasopsv2;

import com.telesign.VoiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import com.telesign.MessagingClient;
import com.telesign.RestClient;
import com.telesign.Util;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TelesignTests {

    @Test
    public void sendSMS(){
        String customerId = "546BFE80-A2B4-4656-A797-4205E6AD30D4";
        String apiKey = "Y4A3fIi4zTMXA8fQ8qgneTHAMrXCCFVKXmXL8j2IShv24mMiOf0diMYYOckgDjVHhFAN/OrY9Tqs94G3/iS2dw==";
        String phoneNumber = "8618322519407";
        String verifyCode = Util.randomWithNDigits(5);
        String message = String.format("Your code is %s", verifyCode);
        String messageType = "OTP";

        try {
            MessagingClient messagingClient = new MessagingClient(customerId, apiKey);
            RestClient.TelesignResponse telesignResponse = messagingClient.message(phoneNumber, message, messageType, null);
            System.out.println(telesignResponse.body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sendVoice(){
        String customerId = "546BFE80-A2B4-4656-A797-4205E6AD30D4";
        String apiKey = "Y4A3fIi4zTMXA8fQ8qgneTHAMrXCCFVKXmXL8j2IShv24mMiOf0diMYYOckgDjVHhFAN/OrY9Tqs94G3/iS2dw==";
        String phoneNumber = "8618322519407";
        String verifyCode = Util.randomWithNDigits(5);
        String message = String.format("Hello, your code is %s. Once again, your code is %s. Goodbye.", verifyCode, verifyCode);
        String messageType = "OTP";

        try {
            VoiceClient voiceClient = new VoiceClient(customerId, apiKey);
            RestClient.TelesignResponse telesignResponse = voiceClient.call(phoneNumber, message, messageType, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
