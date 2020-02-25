package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.pay.service.impl
 * @date 2020-2-17
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${weixin.appid}")
    private String appid;   //公众号id
    @Value("${weixin.partner}")
    private String partner;  //商户号
    @Value("${weixin.notifyurl}")
    private String notifyurl;  //回调地址
    @Value("${weixin.partnerkey}")
    private String partnerkey;  //支付密钥


    @Override
    public Map createNative(Map<String,String> inParamMap) {
        Map map = new HashMap();
        try {
            //1、组装微信请求需要的参数-Map
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", this.appid);  //公众号id
            paramMap.put("mch_id", this.partner);  //商户号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字符串
            paramMap.put("body", "畅购");  //商品描述
            paramMap.put("out_trade_no", inParamMap.get("out_trade_no"));  //商户订单号
            paramMap.put("total_fee", inParamMap.get("total_fee"));  //支付金额（分）
            paramMap.put("spbill_create_ip", "127.0.0.1");  //终端ip-客户端ip(request可以取到)
            paramMap.put("notify_url", this.notifyurl);  //通知地址（回调地址）
            paramMap.put("trade_type", "NATIVE");  //支付类型:有好几种，我们使用扫码支付

            //附加参数{交换机：名字,路由key:名称}
            Map<String, String> attachMap = new HashMap<String,String>();
            attachMap.put("exchange", inParamMap.get("exchange"));
            attachMap.put("routingKey", inParamMap.get("routingKey"));
            attachMap.put("username", inParamMap.get("username"));
            paramMap.put("attach", JSON.toJSONString(attachMap));  //附加参数
            //签名待设定.......................
            String paramXml = WXPayUtil.generateSignedXml(paramMap, this.partnerkey);
            System.out.println("正在发起统一下单请求，参数为：" + paramXml);
            //2、构建HttpClient对象-发起http请求
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";  //请求地址
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3、获取请求结果-xml
            String resultXml = httpClient.getContent();
            System.out.println("发起统一下单请求成功，响应结果为：" + resultXml);
            //4、解析请求结果-Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
            //5、提取需要参数，包装并返回
            map.put("out_trade_no", inParamMap.get("out_trade_no"));
            map.put("total_fee", inParamMap.get("total_fee"));
            map.put("code_url", resultMap.get("code_url"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {
            //1、组装微信请求需要的参数-Map
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", this.appid);  //公众号id
            paramMap.put("mch_id", this.partner);  //商户号
            paramMap.put("out_trade_no", out_trade_no);  //商户订单号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());  //随机字符串
            //签名待设定.......................
            String paramXml = WXPayUtil.generateSignedXml(paramMap, this.partnerkey);
            System.out.println("正在发起查询订单请求，参数为：" + paramXml);
            //2、构建HttpClient对象-发起http请求
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";  //请求地址
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();
            //3、获取请求结果-xml
            String resultXml = httpClient.getContent();
            System.out.println("发起查询订单请求成功，响应结果为：" + resultXml);
            //4、解析请求结果-Map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
