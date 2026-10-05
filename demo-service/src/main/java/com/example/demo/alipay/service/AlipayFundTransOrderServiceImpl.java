package com.example.demo.alipay.service;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.demo.alipay.api.AlipayFundTransOrderService;
import com.example.demo.alipay.api.bo.fund.AlipayFundTransOrder;
import com.example.demo.alipay.dao.dataobject.AlipayFundTransOrderDO;
import com.example.demo.alipay.dao.mapper.AlipayFundTransOrderMapper;
import com.example.demo.framework.annotation.NotNull;
import com.example.demo.framework.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlipayFundTransOrderServiceImpl extends
                                             ServiceImpl<AlipayFundTransOrderMapper, AlipayFundTransOrderDO>
                                             implements AlipayFundTransOrderService {

    @Override
    public AlipayFundTransOrder changed(@NotNull AlipayFundTransOrder alipayFundTransOrder) {
        AlipayFundTransOrderDO alipayFundTransOrderDO = BeanUtil.copy(alipayFundTransOrder,
            AlipayFundTransOrderDO::new);

        this.save(alipayFundTransOrderDO);

        alipayFundTransOrder.setId(alipayFundTransOrderDO.getId());

        return alipayFundTransOrder;
    }

}
