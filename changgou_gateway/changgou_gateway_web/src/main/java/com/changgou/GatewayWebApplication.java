package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableEurekaClient
public class GatewayWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayWebApplication.class,args);
    }

    /**
     * 创建KeyResolver，用于告诉网关我们限流的方式
     * @return
     */
    @Bean(name = "ipKeyResolver")
    public KeyResolver getKeyResolver(){
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                String ip = exchange.getRequest().getRemoteAddress().getHostString();
                //以ip限流
                return Mono.just(ip);
            }
        };
    }
}
