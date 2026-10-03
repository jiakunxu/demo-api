package com.example.demo.test.task;

import com.example.demo.framework.util.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class TestTask {

    @XxlJob("testJobHandler")
    public void testJobHandler() {
        System.out.println(this.getClass().getName() + "：" + DateUtil.getNowDateTime());
    }

}