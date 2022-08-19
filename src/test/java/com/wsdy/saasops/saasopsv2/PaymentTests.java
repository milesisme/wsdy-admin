package com.wsdy.saasops.saasopsv2;

import com.wsdy.saasops.api.constants.PayConstants;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.utils.JsonUtil;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.utils.pay.PaymentPaySignUtil;
import com.wsdy.saasops.modules.fund.dto.PaymentPayBalanceResponseDto;
import com.wsdy.saasops.modules.fund.dto.PaymentPayBaseResponseDto;
import com.wsdy.saasops.modules.fund.dto.PaymentPayExecuteResponseDto;
import com.wsdy.saasops.modules.fund.dto.PaymentPayRequestDto;
import com.wsdy.saasops.modules.fund.entity.FundMerchantPay;
import com.wsdy.saasops.modules.fund.service.PaymentPayService;
import com.wsdy.saasops.modules.member.entity.MbrBankcard;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.wsdy.saasops.modules.fund.service.PaymentPayService.debit_balance;
import static java.util.Objects.isNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class PaymentTests {

    @Autowired
    private PaymentPayService paymentPayService;
    @Autowired
    private OkHttpService okHttpService;
    @Autowired
    private JsonUtil jsonUtil;
    @Test
    public void balanceSign() {
        FundMerchantPay merchantPay = new FundMerchantPay();
        merchantPay.setUrl("https://tyapi.miaopays.com/");
        String webSite = merchantPay.getUrl();
        merchantPay.setMerchantNo("SHP92OHZH7J0");
        merchantPay.setMerchantKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDMRSq0kfOazlh+dtBIokhsnqPG4aQItuFceeFyKAyoAF+P1go6Y2uT3hdx6N0+ZlnNySEYtuhWaUKDCIRuYZCUDP9ajRcO2gKvWMsCs8fuVxLoApqex/1pxBI4DwSoTI8DFJ4PuhI/qE3qgKXEFzQfpsNBUeyIOVKxuOFDN+s2eyZVam6WRDsEGnYhbszMnZqs/rD+pYAk4NsmKnAgfllGpkZb2uhVzPDg/ctdgvUFQ/r1KFgfSm5zxEASgBoeGDAaKsH5GMOqxph8YrVRvMinBLbp9bRgmVaXTYBNDeZWvCj3Uhe61IhcVcA7YvxXJoNwsJQmT4+W0WQFe5hVUDvAgMBAAECggEAAm+p45YuES46jZA3OxlqFH8c+YVYVVrgWrUHyJs2JlqETJTejTvxDpiZMpyfc2+jIqCKhA7V50Qj5XDePRgF3ztgC8eqG0BbKT/lFI9K01r81MavCtUofcZRbhkD5JCrvyO3cFYyuBBKbB8zFx0NrC19sZs0fqFPwLi6xuC7UpleNLeU2ElFNZsOGwig8DeGDu4EVcoHYBsSZyw7jxIHWauchbt8Z44ZeboRiqSgcmWXDYI6Wdjcv82Sx874foZw7WEpbwa59+4JFEgDBQ2Wou7BdEqf4NNt6xp6lkKja0EFvyiX0njYjS2ItawWNiBiUuTC4mDAzkxkCR+vWh/FAQKBgQD/kpJdn2J/FECcKWmRcD7TvQlxowK9Xb8vnrRK0I9MaaSrAWFaHNKk+X/Edo8tlvAiA5HVmiPjRv3ZEcfUUEKWlLw3MpsOcWjCGaqQ9gpzpk0JuTgL4XpmmbfdYn2gsU7VSpylz+GFdEatjq/XCp3aZPFnV1+/woKEcLm6ymjRgQKBgQCDaUDCj+U3assGq8zdpWexTsft7caWxGRP92iHeMOtrTef+wZBrg/OJFqYy+bZZVBgvUowGPkEraX5KoLzMeMGlGktvvSPKjHvdbHFsNSmFLt/If/ZW3UDDtBSyPJltNatS7fozZVCQkoHOG/LkxwNAtTEiqeuOR+SvI2I0qNqbwKBgG4wY6q57V2c/b5WnI14N55TAp9cCPJcWPHPGyymfyIS6kxQbAjOJIO8jrnF5DVjvmTr5RSTQKXdFdVhNSb088C4n+2sg+3WN7R4HHIjop36NXzbvq5gqMVTwVJSBJgj2jlb0e4rkrBXUaHCw02T5jsjCnHj2gRzeJJhh7JhUpABAoGAWXKOSyDpM/R0J6FPPFt3HmByhAiCUWNyqF/Ofc/82b37C+ExHAnf5kAmjrTT+IwWzxJpah3FeqK8Q8LlCaaeUn84rJO7Iavl+4nQLLE1vaqFCX9aCFhRaHhv4rm3DPWv3puYnlU5ULWQcCPBLP+Aa3bO3s1GX7ewPxArAsPlye8CgYEA9Ztlw2KWuQE8N+/Qh5sCn4HGJ0GAzFpGM18Ka0cg6joQtX8TbO+J543A/EJNqxo3wKyhZC/a/dOJLfk9VOjXJL7N9ABC6GzI45K0qpyUeGpS7eNWLTKzSDZB27fl3EeL1hHAg+vbG3rvorR9EKlxYvuHjW5dnSJ17s9jxXIwlsU=");

        if(!webSite.endsWith("/")) {
            webSite+="/";
        }
        String url = webSite+ debit_balance;
        try {
            Map<String,String> paramsMap = new HashMap<>();
            paramsMap.put("merchantNumber",merchantPay.getMerchantNo() );  // 商户号
            String sign = PaymentPaySignUtil.sign(merchantPay.getMerchantKey().getBytes("UTF-8"), PaymentPaySignUtil.createLinkString(paramsMap));
            paramsMap.put("sign",sign );
            System.out.println("sign=" + sign);

        } catch (Exception e) {
            System.out.println("payment代付余额查询异常"+e);
        }
    }

    @Test
    public void balance() {
        FundMerchantPay merchantPay = new FundMerchantPay();
        merchantPay.setUrl("https://tyapi.miaopays.com/");
        merchantPay.setMerchantNo("SHP92OHZH7J0");
        merchantPay.setMerchantKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDMRSq0kfOazlh+dtBIokhsnqPG4aQItuFceeFyKAyoAF+P1go6Y2uT3hdx6N0+ZlnNySEYtuhWaUKDCIRuYZCUDP9ajRcO2gKvWMsCs8fuVxLoApqex/1pxBI4DwSoTI8DFJ4PuhI/qE3qgKXEFzQfpsNBUeyIOVKxuOFDN+s2eyZVam6WRDsEGnYhbszMnZqs/rD+pYAk4NsmKnAgfllGpkZb2uhVzPDg/ctdgvUFQ/r1KFgfSm5zxEASgBoeGDAaKsH5GMOqxph8YrVRvMinBLbp9bRgmVaXTYBNDeZWvCj3Uhe61IhcVcA7YvxXJoNwsJQmT4+W0WQFe5hVUDvAgMBAAECggEAAm+p45YuES46jZA3OxlqFH8c+YVYVVrgWrUHyJs2JlqETJTejTvxDpiZMpyfc2+jIqCKhA7V50Qj5XDePRgF3ztgC8eqG0BbKT/lFI9K01r81MavCtUofcZRbhkD5JCrvyO3cFYyuBBKbB8zFx0NrC19sZs0fqFPwLi6xuC7UpleNLeU2ElFNZsOGwig8DeGDu4EVcoHYBsSZyw7jxIHWauchbt8Z44ZeboRiqSgcmWXDYI6Wdjcv82Sx874foZw7WEpbwa59+4JFEgDBQ2Wou7BdEqf4NNt6xp6lkKja0EFvyiX0njYjS2ItawWNiBiUuTC4mDAzkxkCR+vWh/FAQKBgQD/kpJdn2J/FECcKWmRcD7TvQlxowK9Xb8vnrRK0I9MaaSrAWFaHNKk+X/Edo8tlvAiA5HVmiPjRv3ZEcfUUEKWlLw3MpsOcWjCGaqQ9gpzpk0JuTgL4XpmmbfdYn2gsU7VSpylz+GFdEatjq/XCp3aZPFnV1+/woKEcLm6ymjRgQKBgQCDaUDCj+U3assGq8zdpWexTsft7caWxGRP92iHeMOtrTef+wZBrg/OJFqYy+bZZVBgvUowGPkEraX5KoLzMeMGlGktvvSPKjHvdbHFsNSmFLt/If/ZW3UDDtBSyPJltNatS7fozZVCQkoHOG/LkxwNAtTEiqeuOR+SvI2I0qNqbwKBgG4wY6q57V2c/b5WnI14N55TAp9cCPJcWPHPGyymfyIS6kxQbAjOJIO8jrnF5DVjvmTr5RSTQKXdFdVhNSb088C4n+2sg+3WN7R4HHIjop36NXzbvq5gqMVTwVJSBJgj2jlb0e4rkrBXUaHCw02T5jsjCnHj2gRzeJJhh7JhUpABAoGAWXKOSyDpM/R0J6FPPFt3HmByhAiCUWNyqF/Ofc/82b37C+ExHAnf5kAmjrTT+IwWzxJpah3FeqK8Q8LlCaaeUn84rJO7Iavl+4nQLLE1vaqFCX9aCFhRaHhv4rm3DPWv3puYnlU5ULWQcCPBLP+Aa3bO3s1GX7ewPxArAsPlye8CgYEA9Ztlw2KWuQE8N+/Qh5sCn4HGJ0GAzFpGM18Ka0cg6joQtX8TbO+J543A/EJNqxo3wKyhZC/a/dOJLfk9VOjXJL7N9ABC6GzI45K0qpyUeGpS7eNWLTKzSDZB27fl3EeL1hHAg+vbG3rvorR9EKlxYvuHjW5dnSJ17s9jxXIwlsU=");

        String result = paymentPayService.balance(merchantPay);

        if (StringUtils.isEmpty(result)) {
            return;
        }
        PaymentPayBaseResponseDto response =  jsonUtil.fromJson(result, PaymentPayBaseResponseDto.class);
        if (isNull(response) || isNull(response.getStatus()) ) {
            return;
        }
        if (!Integer.valueOf(Constants.EVNumber.one).equals(response.getStatus())) {
            return;
        }
        PaymentPayBalanceResponseDto paymentPayBalanceResponseDto = jsonUtil.fromJson(jsonUtil.toJson(response.getData()), PaymentPayBalanceResponseDto.class);
        if (isNull(paymentPayBalanceResponseDto) || isNull(paymentPayBalanceResponseDto.getBalance())) {
            return;
        }
    }

    @Test
    public void debitPayment(){
        FundMerchantPay merchantPay = new FundMerchantPay();
        merchantPay.setUrl("https://tyapi.miaopays.com/");
        merchantPay.setMerchantNo("SHP92OHZH7J0");
        merchantPay.setMerchantKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDMRSq0kfOazlh+dtBIokhsnqPG4aQItuFceeFyKAyoAF+P1go6Y2uT3hdx6N0+ZlnNySEYtuhWaUKDCIRuYZCUDP9ajRcO2gKvWMsCs8fuVxLoApqex/1pxBI4DwSoTI8DFJ4PuhI/qE3qgKXEFzQfpsNBUeyIOVKxuOFDN+s2eyZVam6WRDsEGnYhbszMnZqs/rD+pYAk4NsmKnAgfllGpkZb2uhVzPDg/ctdgvUFQ/r1KFgfSm5zxEASgBoeGDAaKsH5GMOqxph8YrVRvMinBLbp9bRgmVaXTYBNDeZWvCj3Uhe61IhcVcA7YvxXJoNwsJQmT4+W0WQFe5hVUDvAgMBAAECggEAAm+p45YuES46jZA3OxlqFH8c+YVYVVrgWrUHyJs2JlqETJTejTvxDpiZMpyfc2+jIqCKhA7V50Qj5XDePRgF3ztgC8eqG0BbKT/lFI9K01r81MavCtUofcZRbhkD5JCrvyO3cFYyuBBKbB8zFx0NrC19sZs0fqFPwLi6xuC7UpleNLeU2ElFNZsOGwig8DeGDu4EVcoHYBsSZyw7jxIHWauchbt8Z44ZeboRiqSgcmWXDYI6Wdjcv82Sx874foZw7WEpbwa59+4JFEgDBQ2Wou7BdEqf4NNt6xp6lkKja0EFvyiX0njYjS2ItawWNiBiUuTC4mDAzkxkCR+vWh/FAQKBgQD/kpJdn2J/FECcKWmRcD7TvQlxowK9Xb8vnrRK0I9MaaSrAWFaHNKk+X/Edo8tlvAiA5HVmiPjRv3ZEcfUUEKWlLw3MpsOcWjCGaqQ9gpzpk0JuTgL4XpmmbfdYn2gsU7VSpylz+GFdEatjq/XCp3aZPFnV1+/woKEcLm6ymjRgQKBgQCDaUDCj+U3assGq8zdpWexTsft7caWxGRP92iHeMOtrTef+wZBrg/OJFqYy+bZZVBgvUowGPkEraX5KoLzMeMGlGktvvSPKjHvdbHFsNSmFLt/If/ZW3UDDtBSyPJltNatS7fozZVCQkoHOG/LkxwNAtTEiqeuOR+SvI2I0qNqbwKBgG4wY6q57V2c/b5WnI14N55TAp9cCPJcWPHPGyymfyIS6kxQbAjOJIO8jrnF5DVjvmTr5RSTQKXdFdVhNSb088C4n+2sg+3WN7R4HHIjop36NXzbvq5gqMVTwVJSBJgj2jlb0e4rkrBXUaHCw02T5jsjCnHj2gRzeJJhh7JhUpABAoGAWXKOSyDpM/R0J6FPPFt3HmByhAiCUWNyqF/Ofc/82b37C+ExHAnf5kAmjrTT+IwWzxJpah3FeqK8Q8LlCaaeUn84rJO7Iavl+4nQLLE1vaqFCX9aCFhRaHhv4rm3DPWv3puYnlU5ULWQcCPBLP+Aa3bO3s1GX7ewPxArAsPlye8CgYEA9Ztlw2KWuQE8N+/Qh5sCn4HGJ0GAzFpGM18Ka0cg6joQtX8TbO+J543A/EJNqxo3wKyhZC/a/dOJLfk9VOjXJL7N9ABC6GzI45K0qpyUeGpS7eNWLTKzSDZB27fl3EeL1hHAg+vbG3rvorR9EKlxYvuHjW5dnSJ17s9jxXIwlsU=");
        MbrBankcard bankcard = new MbrBankcard();
        bankcard.setCardNo("6666666666666666");
        bankcard.setRealName("测试");

        String result = paymentPayService.debitPayment("202006111151008", bankcard, new BigDecimal(1), merchantPay,"ycs");
        if (StringUtils.isEmpty(result)) {
            return;
        }
        PaymentPayBaseResponseDto response =  jsonUtil.fromJson(result, PaymentPayBaseResponseDto.class);
        if (isNull(response) || isNull(response.getStatus()) ) {
            return;
        }
        if (!Integer.valueOf(Constants.EVNumber.one).equals(response.getStatus())) {
            return;
        }else{  // 提单成功
            PaymentPayExecuteResponseDto paymentPayExecuteResponseDto = jsonUtil.fromJson(jsonUtil.toJson(response.getData()), PaymentPayExecuteResponseDto.class);
        }
    }

    @Test
    public void debitPaymentSign(){
        FundMerchantPay merchantPay = new FundMerchantPay();
        merchantPay.setUrl("https://tyapi.miaopays.com/");
        merchantPay.setMerchantNo("SHP92OHZH7J0");
        merchantPay.setMerchantKey("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCDMRSq0kfOazlh+dtBIokhsnqPG4aQItuFceeFyKAyoAF+P1go6Y2uT3hdx6N0+ZlnNySEYtuhWaUKDCIRuYZCUDP9ajRcO2gKvWMsCs8fuVxLoApqex/1pxBI4DwSoTI8DFJ4PuhI/qE3qgKXEFzQfpsNBUeyIOVKxuOFDN+s2eyZVam6WRDsEGnYhbszMnZqs/rD+pYAk4NsmKnAgfllGpkZb2uhVzPDg/ctdgvUFQ/r1KFgfSm5zxEASgBoeGDAaKsH5GMOqxph8YrVRvMinBLbp9bRgmVaXTYBNDeZWvCj3Uhe61IhcVcA7YvxXJoNwsJQmT4+W0WQFe5hVUDvAgMBAAECggEAAm+p45YuES46jZA3OxlqFH8c+YVYVVrgWrUHyJs2JlqETJTejTvxDpiZMpyfc2+jIqCKhA7V50Qj5XDePRgF3ztgC8eqG0BbKT/lFI9K01r81MavCtUofcZRbhkD5JCrvyO3cFYyuBBKbB8zFx0NrC19sZs0fqFPwLi6xuC7UpleNLeU2ElFNZsOGwig8DeGDu4EVcoHYBsSZyw7jxIHWauchbt8Z44ZeboRiqSgcmWXDYI6Wdjcv82Sx874foZw7WEpbwa59+4JFEgDBQ2Wou7BdEqf4NNt6xp6lkKja0EFvyiX0njYjS2ItawWNiBiUuTC4mDAzkxkCR+vWh/FAQKBgQD/kpJdn2J/FECcKWmRcD7TvQlxowK9Xb8vnrRK0I9MaaSrAWFaHNKk+X/Edo8tlvAiA5HVmiPjRv3ZEcfUUEKWlLw3MpsOcWjCGaqQ9gpzpk0JuTgL4XpmmbfdYn2gsU7VSpylz+GFdEatjq/XCp3aZPFnV1+/woKEcLm6ymjRgQKBgQCDaUDCj+U3assGq8zdpWexTsft7caWxGRP92iHeMOtrTef+wZBrg/OJFqYy+bZZVBgvUowGPkEraX5KoLzMeMGlGktvvSPKjHvdbHFsNSmFLt/If/ZW3UDDtBSyPJltNatS7fozZVCQkoHOG/LkxwNAtTEiqeuOR+SvI2I0qNqbwKBgG4wY6q57V2c/b5WnI14N55TAp9cCPJcWPHPGyymfyIS6kxQbAjOJIO8jrnF5DVjvmTr5RSTQKXdFdVhNSb088C4n+2sg+3WN7R4HHIjop36NXzbvq5gqMVTwVJSBJgj2jlb0e4rkrBXUaHCw02T5jsjCnHj2gRzeJJhh7JhUpABAoGAWXKOSyDpM/R0J6FPPFt3HmByhAiCUWNyqF/Ofc/82b37C+ExHAnf5kAmjrTT+IwWzxJpah3FeqK8Q8LlCaaeUn84rJO7Iavl+4nQLLE1vaqFCX9aCFhRaHhv4rm3DPWv3puYnlU5ULWQcCPBLP+Aa3bO3s1GX7ewPxArAsPlye8CgYEA9Ztlw2KWuQE8N+/Qh5sCn4HGJ0GAzFpGM18Ka0cg6joQtX8TbO+J543A/EJNqxo3wKyhZC/a/dOJLfk9VOjXJL7N9ABC6GzI45K0qpyUeGpS7eNWLTKzSDZB27fl3EeL1hHAg+vbG3rvorR9EKlxYvuHjW5dnSJ17s9jxXIwlsU=");

        PaymentPayRequestDto paymentPayRequestDto = new PaymentPayRequestDto();
        paymentPayRequestDto.setMerchantNumber(merchantPay.getMerchantNo());    // 商户号
        paymentPayRequestDto.setMerchantOrderNumber("202006111151007");                   // 订单号
        paymentPayRequestDto.setReceiveName("测试");            // 帐户名
        paymentPayRequestDto.setReceiveCard("6666666666666666");              // 银行卡号
        paymentPayRequestDto.setAmount(new BigDecimal(1));                               // 转账金额
        paymentPayRequestDto.setCallBackUrl("http://admin.wy24.com/" + PayConstants.PAYMENT_PAYURL +"ycs" ); // 回调地址

        try{
            Map<String, String>  paramsMap = new JsonUtil().toStringMap(paymentPayRequestDto);
            String sign = PaymentPaySignUtil.sign(merchantPay.getMerchantKey().getBytes("UTF-8"), PaymentPaySignUtil.createLinkString(paramsMap));
            paramsMap.put("sign",sign );
            System.out.println("sign=" + sign);

        }catch (Exception e){
            System.out.println("payment代付提单异常"+e);
        }
    }
}
