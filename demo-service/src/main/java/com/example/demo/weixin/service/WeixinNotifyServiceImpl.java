package com.example.demo.weixin.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.weixin.api.MessageService;
import com.example.demo.weixin.api.WeixinNotifyService;
import com.example.demo.weixin.dao.dataobject.WeixinNotifyDO;
import com.example.demo.weixin.dao.mapper.WeixinNotifyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author JiakunXu
 */
@Service
public class WeixinNotifyServiceImpl extends ServiceImpl<WeixinNotifyMapper, WeixinNotifyDO>
                                     implements WeixinNotifyService {

    @Autowired
    private MessageService messageService;

    @Override
    public String verify(String signature, String timestamp, String nonce, String echoStr) {
        return messageService.verify(signature, timestamp, nonce, echoStr);
    }

    @Override
    public String notify(String signature, String timestamp, String nonce, String data) {
        messageService.notify(signature, timestamp, nonce, data);

        return "";
    }

}
