package com.changgou;

import com.netflix.loadbalancer.RetryRule;
import entity.FeignInterceptor;
import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.changgou.order.dao"})
@EnableFeignClients(basePackages = {"com.changgou.goods.feign","com.changgou.user.feign"})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }

    @Bean
    public IdWorker getIdWorker() {
        return new IdWorker(0,1);
    }

    @Bean
    public FeignInterceptor getFeignInterceptor() {
        return new FeignInterceptor();
    }
}
