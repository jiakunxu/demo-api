package com.example.demo.bytedance.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.bytedance.api.BytedanceNotifyService;
import com.example.demo.bytedance.api.MessageService;
import com.example.demo.bytedance.api.bo.message.Message;
import com.example.demo.bytedance.dao.dataobject.BytedanceNotifyDO;
import com.example.demo.bytedance.dao.mapper.BytedanceNotifyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author JiakunXu
 */
@Service
public class BytedanceNotifyServiceImpl extends
                                        ServiceImpl<BytedanceNotifyMapper, BytedanceNotifyDO>
                                        implements BytedanceNotifyService {

    @Autowired
    private MessageService messageService;

    @Override
    public String verify(String signature, String timestamp, String nonce, String echostr) {
        return "";
    }

    @Override
    public Message notify(String signature, String timestamp, String nonce, String data) {
        return null;
    }

}
