package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.pay.controller
 * @date 2020-2-17
 */
@RestController
@CrossOrigin
@RequestMapping("/weixin/pay")
public class WeixinPayController {
    @Autowired
    private WeixinPayService weixinPayService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${mq.pay.exchange.order}")
    private String exchange;
    @Value("${mq.pay.queue.order}")
    private String queue;
    @Value("${mq.pay.routing.key}")
    private String routing;


    /**
     * 生成微信支付二维码
     * @param paramMap {
     *                 out_trade_no 订单号,
     *                 total_fee 金额(分),
     *                 exchange 交换机,
     *                 routingKey 路由Key,
     *                 username 用户名
     *                 }
     * @return
     */
    @RequestMapping("/create/native")
    public Result<Map> createNative(@RequestParam Map<String,String> paramMap) {
        //String username = TokenDecode.getUserInfo().get("username");
        String username = "zhangsan";
        paramMap.put("username", username);
        Map map = weixinPayService.createNative(paramMap);
        return new Result<Map>(true, StatusCode.OK, "生成二维码成功",map);
    }

    @RequestMapping("/status/query")
    public Result<Map> queryPayStatus(String out_trade_no) {
        Map map = weixinPayService.queryPayStatus(out_trade_no);
        return new Result<Map>(true, StatusCode.OK, "查询订单成功",map);
    }

    /***
     * 支付回调
     * 支付完成后，微信会把相关支付结果及用户信息通过数据流的形式发送给商户，
     * 商户需要接收处理，并按文档规范返回应答
     * @param request
     * @return
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request) {
        try {
            //1、读取微信传入的输入流--InputStream
            InputStream inputStream = request.getInputStream();
            //2、把输入流转换成字符串
            String xmlResult = IOUtils.toString(inputStream, "UTF-8");
            System.out.println("微信回调接口，传入参数为：" + xmlResult);
            //3、把字符串xml转换成Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlResult);

            //获取附加参数
            Map<String,String> attachMap = JSON.parseObject(resultMap.get("attach"), Map.class);
            exchange = attachMap.get("exchange");
            routing = attachMap.get("routingKey");

            //4、识别支付结果--发送MQ.......
            rabbitTemplate.convertAndSend(exchange, routing, JSON.toJSONString(resultMap));

            //5、按照微信回调接口的规范响应微信
            Map respMap = new HashMap();
            respMap.put("return_code","SUCCESS");
            respMap.put("return_msg","OK");
            return WXPayUtil.mapToXml(respMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("/test/queue")
    public String testQueue() {
        //普通订单测试
        //rabbitTemplate.convertAndSend(exchange, routing, "{'return_code':'FAIL'}");
        //秒杀订单测试
        rabbitTemplate.convertAndSend(exchange, "queue.seckillorder", "{'return_code':'FAIL'}");
        return "msg is ok....";
    }
}
