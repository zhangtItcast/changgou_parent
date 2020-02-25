package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.order.service.impl
 * @date 2020-2-15
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;

    @Override
    public void add(Integer num, Long skuId, String username) {
        //如果购物数量小于1
        if (num < 1) {
            redisTemplate.boundHashOps("Cart_" + username).delete(skuId);
            return;
        }
        //1、跟据skuId查询sku信息
        Sku sku = skuFeign.findById(skuId).getData();
        if (sku == null) {
            throw new RuntimeException("当前商品信息不存在");
        }else {
            //2、从sku中获取spuId查询spu信息
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();
            //3、组装购物车对象OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setCategoryId1(spu.getCategory1Id());  //一级分类
            orderItem.setCategoryId2(spu.getCategory2Id());  //2级分类
            orderItem.setCategoryId3(spu.getCategory3Id());  //3级分类
            orderItem.setSpuId(spu.getId());
            orderItem.setSkuId(skuId);
            orderItem.setName(sku.getName());  //商品名称
            orderItem.setPrice(sku.getPrice());  //单价
            orderItem.setNum(num);  //数量
            int money = orderItem.getPrice() * orderItem.getNum();  //总金额
            orderItem.setMoney(money);
            orderItem.setPayMoney(orderItem.getMoney());
            orderItem.setImage(sku.getImage());
            orderItem.setWeight(sku.getWeight());
            orderItem.setPostFee(0);  //运费-暂时不做
            orderItem.setIsReturn("0");

            //4、把购物车保存在Redis中
            redisTemplate.boundHashOps("Cart_" + username).put(skuId,orderItem);
        }

    }

    @Override
    public List<OrderItem> list(String username) {
        List orderItemList = redisTemplate.boundHashOps("Cart_" + username).values();
        return orderItemList;
    }
}
