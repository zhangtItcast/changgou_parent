package com.changgou;

import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author Steven
 * @description com.changgou.goods
 */
@SpringBootApplication
@EnableEurekaClient  //开启Eureka客户端服务发现
//启动通用Mapper,设置扫描的根包
@MapperScan(basePackages = "com.changgou.goods.dao")
public class GoodsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class, args);
    }

    @Bean
    public IdWorker getIdWorker() {
        return new IdWorker(0,0);
    }
}
