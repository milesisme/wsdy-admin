package com.wsdy.saasops.listener;

import com.wsdy.saasops.modules.system.msgtemple.service.MsgModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


/**
 *	监听器，publishEvent BizEvent
 */
@Slf4j
@Component
public class MessageSenderListener implements ApplicationListener<BizEvent> {

    @Autowired
    private MsgModelService msgModelService;

    @Override
    public void onApplicationEvent(BizEvent event) {
        log.info("消息正在发送");
        msgModelService.sendMsg(event);
    }
}



