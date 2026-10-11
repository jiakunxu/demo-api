package com.example.demo.mqtt.manager;

import com.alibaba.mqtt.server.ServerProducer;
import com.example.demo.mqtt.api.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("com.example.demo.mqtt.manager.producerService")
public class ProducerServiceImpl implements ProducerService {

    @Autowired
    private ServerProducer producer;

    @Override
    public String sendMessage(String mqttTopic, byte[] payload) {
        try {
            return producer.sendMessage(mqttTopic, payload).getMsgId();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
