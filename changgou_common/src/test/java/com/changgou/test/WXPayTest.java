package com.changgou.test;

import com.github.wxpay.sdk.WXPayUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.test
 * @date 2020-2-17
 */
public class WXPayTest {
    public static void main(String[] args)  throws Exception{
        //生成微信支付使用的随机字符串
        System.out.println(WXPayUtil.generateNonceStr());

        Map map = new HashMap();
        map.put("no", "001");
        map.put("name", "风清扬");
        //把Map直接转成XML字符串
        String xml = WXPayUtil.mapToXml(map);
        System.out.println(xml);
        //生成带签名的xml(把map转成xml)
        String signedXml = WXPayUtil.generateSignedXml(map, "ali-007");
        System.out.println(signedXml);

        //把xml字符串转换成Map
        Map<String, String> xmlToMap = WXPayUtil.xmlToMap(signedXml);
        xmlToMap.put("sex", "1");
        System.out.println(xmlToMap);
    }
}
