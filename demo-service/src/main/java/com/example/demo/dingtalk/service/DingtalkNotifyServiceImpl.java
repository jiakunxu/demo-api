package com.example.demo.dingtalk.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dingtalk.api.DingtalkNotifyService;
import com.example.demo.dingtalk.api.MessageService;
import com.example.demo.dingtalk.dao.dataobject.DingtalkNotifyDO;
import com.example.demo.dingtalk.dao.mapper.DingtalkNotifyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class DingtalkNotifyServiceImpl extends ServiceImpl<DingtalkNotifyMapper, DingtalkNotifyDO>
                                       implements DingtalkNotifyService {

    @Autowired
    private MessageService messageService;

    @Override
    public Map<String, String> notify(String signature, String timeStamp, String nonce,
                                      String encrypt) {
        return Map.of();
    }

}
