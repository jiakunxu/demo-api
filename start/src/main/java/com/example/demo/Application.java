package com.example.demo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableDubbo
@SpringBootApplication
@MapperScan(basePackages = "com.example.demo.*.dao.mapper")
public class Application {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
