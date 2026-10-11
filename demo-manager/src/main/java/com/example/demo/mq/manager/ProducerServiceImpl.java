package com.example.demo.mq.manager;

import com.example.demo.mq.api.ProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * @author JiakunXu
 */
@Slf4j
@Service("com.example.demo.mq.manager.producerService")
public class ProducerServiceImpl implements ProducerService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public String syncSend(String topic, String tags, byte[] body, String keys) {
        try {
            return rocketMQTemplate
                .syncSend(topic + ":" + tags,
                    MessageBuilder.withPayload(body).setHeader(RocketMQHeaders.KEYS, keys).build())
                .getMsgId();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public String syncSend(String topic, String tags, byte[] body, String keys, long delayTime) {
        try {
            return rocketMQTemplate.syncSendDelayTimeSeconds(topic + ":" + tags,
                MessageBuilder.withPayload(body).setHeader(RocketMQHeaders.KEYS, keys).build(),
                delayTime).getMsgId();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void send(String topic, String tags, byte[] body, String keys) {
        try {
            rocketMQTemplate.send(topic + ":" + tags,
                MessageBuilder.withPayload(body).setHeader(RocketMQHeaders.KEYS, keys).build());
        } catch (Exception e) {
            log.error("{},{},{},{}", topic, tags, body, keys, e);
        }
    }

}
