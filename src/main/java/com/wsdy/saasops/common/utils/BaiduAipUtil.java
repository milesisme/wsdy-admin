package com.wsdy.saasops.common.utils;

import com.baidu.aip.util.Base64Util;
import com.wsdy.saasops.api.modules.user.dto.BaiduResDto;
import com.wsdy.saasops.api.modules.user.dto.BaiduResResultDto;
import com.wsdy.saasops.api.modules.user.service.OkHttpService;
import com.wsdy.saasops.api.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

/**
 *
 * 百度AIP工具类
 */
@Slf4j
@Component
public class BaiduAipUtil {
	@Value("${baidu.apikey}")
	private String apiKey;
	@Value("${baidu.secretkey}")
	private String secretKey;
	@Value("${baidu.auth.host}")
	private String authHost;
	@Value("${baidu.bankcard}")
	private String bankCard;

	@Autowired
	private OkHttpService okHttpService;
	@Autowired
	private JsonUtil jsonUtil;

	/**
	 * 获取权限token
	 * @return 返回示例：
	 * {
	 * "access_token": "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-1234567",
	 * "expires_in": 2592000
	 * }
	 */
	public String getAuth() {
		// 官网获取的 API Key
		String clientId = apiKey;
		// 官网获取的 Secret Key
		String clientSecret = secretKey;
		return getAuth(clientId, clientSecret);
	}

	/**
	 * 获取API访问token
	 * 该token有一定的有效期，需要自行管理，当失效时需重新获取.
	 * @param ak - 百度云官网获取的 API Key
	 * @param sk - 百度云官网获取的 Securet Key
	 * @return assess_token 示例：
	 * "24.460da4889caad24cccdb1fea17221975.2592000.1491995545.282335-12pr34567"
	 */
	public  String getAuth(String ak, String sk) {
		Map<String, String> stringMap = new HashMap<>(2);
		stringMap.put("Accept", OkHttpService.APPLICATION_JSON);
		String getAccessTokenUrl = authHost
				// 1. grant_type为固定参数
				+ "grant_type=client_credentials"
				// 2. 官网获取的 API Key
				+ "&client_id=" + ak
				// 3. 官网获取的 Secret Key
				+ "&client_secret=" + sk;
		String jsonMessage = okHttpService.get(okHttpService.getPayHttpsClient(), getAccessTokenUrl, stringMap);
		log.info("getAuth-jsonMessage" + jsonMessage);
		if (StringUtil.isEmpty(jsonMessage)) {
			return null;
		}

		JSONObject jsonObject = new JSONObject(jsonMessage);
		String access_token = jsonObject.getString("access_token");
		log.info("getAuth--access_token:" + access_token);
		return access_token;
	}

	public BaiduResResultDto bankCardOCR(MultipartFile bankCardPic) {
		// 请求url
		try {
			String access_token = getAuth();
			if(StringUtil.isEmpty(access_token)){
				return null;
			}
			String url = bankCard + "?access_token=" + access_token;
			if (Objects.nonNull(bankCardPic)) {
				byte[] fileBuff = IOUtils.toByteArray(bankCardPic.getInputStream());
				String imgStr = Base64Util.encode(fileBuff);
				String imgParam = URLEncoder.encode(imgStr, "UTF-8");
				String param = "image=" + imgParam;
				String result = BaiduHttpUtil.post(url, access_token, param);
//				Map<String, String> bodyParams = new HashMap<>();
//				bodyParams.put("image",imgParam);
//				Map<String, String> headParams = new HashMap<>();
//				headParams.put("Content-Type","application/x-www-form-urlencoded");
//				headParams.put("Connection","Keep-Alive");
//				String result = okHttpService.post(url,bodyParams,headParams);
				log.info("bankCardOCR-result-" + result);
				if(StringUtil.isEmpty(result)){
					return null;
				}
				BaiduResDto baiduResDto = jsonUtil.fromJson(result,  BaiduResDto.class);
				if (isNull(baiduResDto) || isNull(baiduResDto.getResult())) {
					return null;
				}
				BaiduResResultDto baiduResResultDto = jsonUtil.fromJson(
						jsonUtil.toJson(baiduResDto.getResult()), BaiduResResultDto.class);
				if (isNull(baiduResResultDto) ) {
					return null;
				}
				// 去除空格符
				baiduResResultDto.setBank_card_number(StringUtils.deleteWhitespace(baiduResResultDto.getBank_card_number()));
				return baiduResResultDto;
			}
		} catch (Exception e) {
			log.error("bankCardOCR-error",e);
		}
		return null;
	}
}
