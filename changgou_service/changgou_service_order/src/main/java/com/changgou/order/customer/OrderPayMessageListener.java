package com.changgou.order.customer;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.order.customer
 * @date 2020-2-17
 */
@Component
public class OrderPayMessageListener {
    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "${mq.pay.queue.order}")
    public void payListener(String msg) {
        //1、解析消息内容-Map
        Map<String,String> map = JSON.parseObject(msg, Map.class);
        System.out.println("mq收到消息：" + map);
        //2、判断处理结果调用相应逻辑
        if (map != null && "success".equalsIgnoreCase(map.get("return_code"))) {
            String result_code = map.get("result_code");
            String out_trade_no = map.get("out_trade_no");
            String transaction_id = map.get("transaction_id");
            //如果支付成功
            if ("success".equalsIgnoreCase(result_code)) {
                //支付成功相关逻辑-修改订单支付状态
                System.out.println(out_trade_no + "，已支付成功");
                orderService.updateStatus(out_trade_no, transaction_id);
            } else {
                //支付失败相关逻辑-取消订单
                System.out.println(out_trade_no + "，支付失败");
                orderService.deleteOrder(out_trade_no);
            }
        }
    }
}
