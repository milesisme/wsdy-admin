package com.wsdy.saasops.api.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.wsdy.saasops.api.modules.user.dto.RedisKey;
import com.wsdy.saasops.config.ThreadLocalCache;
import com.wsdy.saasops.modules.member.service.MbrAccountService;

import static com.wsdy.saasops.api.constants.ApiConstants.REIDS_LOGIN_TOKEN_LISTENER_KEY;

@Component
public class RedisExpiredListener implements MessageListener {


    @Override
    public void onMessage(Message message, byte[] bytes) {
        RedisKey keys = getLoingName(message);
        if (!StringUtils.isEmpty(keys)) {
            MbrAccountService mbrAccountService = SpringContextHolder.getBean("mbrAccountService");
            ThreadLocalCache.setSiteCodeAsny(keys.getSiteCode());
            mbrAccountService.updateOffline(keys.getLoginName());
        }
    }

    private RedisKey getLoingName(Message message) {
        String body = new String(message.getBody());
        if (!StringUtils.isEmpty(body)) {
            String[] keyArr = body.split("_");
            if (REIDS_LOGIN_TOKEN_LISTENER_KEY.equals(keyArr[0])) {
                RedisKey keyContent = new RedisKey();
                keyContent.setSiteCode(keyArr[1]);
                keyContent.setLoginName(keyArr[2]);
                return keyContent;
            }
        }
        return null;
    }
}
