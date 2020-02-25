package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.order.controller
 * @date 2020-2-15
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    /***
     * 添加购物车
     * @param num:购买商品数量
     * @param skuId：购买商品的skuId
     * @return
     */
    @RequestMapping("/add")
    public Result add(Integer num, Long skuId) {
        //获取登录名--从当前登录令牌中获取
        //String username = "zhangsan";  //测试用户名
        String username = TokenDecode.getUserInfo().get("username");
        cartService.add(num, skuId, username);
        return new Result(true, StatusCode.OK, "添加购物车成功！");
    }

    /***
     * 查询用户的购物车数据
     * @return
     */
    @RequestMapping("/list")
    public Result<List<OrderItem>> list() {
        //获取登录名--从当前登录令牌中获取
        //String username = "zhangsan";  //测试用户名
        //从SpringSecurity中获取用户信息
       /* Map<String, String> userInfo = TokenDecode.getUserInfo();
        System.out.println(userInfo);*/
       //从SpringSecurity中获取用户信息-->解密令牌-->获取用户名属性
        String username = TokenDecode.getUserInfo().get("username");
        List<OrderItem> orderItemList = cartService.list(username);
        return new Result<List<OrderItem>>(true,StatusCode.OK,"购物查询成功",orderItemList);
    }
}
