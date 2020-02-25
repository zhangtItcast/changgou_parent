package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.utils.SeckillStatus;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.seckill.task
 * @date 2020-2-19
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Async  //标识当前方法为多线程调用-异步调用
    public void createOrder(){
        /*try {
            System.out.println("多线程方法被调用....begin....");
            Thread.sleep(20000);
            System.out.println("多线程方法被调用....end....");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //便于测试我们这里的参数先写死
        /*//时间区间
        String time = "2020021914";
        //用户登录名
        String username="zhangsan";
        //用户抢购商品
        Long id = 1131816140985733120L;*/

        //右出
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
        //取到队列，执行下单逻辑
        if (seckillStatus != null) {
            //时间区间
            String time = seckillStatus.getTime();
            //用户登录名
            String username = seckillStatus.getUsername();
            //用户抢购商品
            Long id = seckillStatus.getGoodsId();

            //1、根据id查询商品信息
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);
            //2、如果商品不存在或者库存不足，提示用户，卖完了
            /*if (seckillGoods == null || seckillGoods.getStockCount() < 1) {
                throw new RuntimeException("抱歉，你来晚了一步，当前商品已被抢购一空！");
            }*/

            //先从队列中获取个数-库存
            //超卖方式一取法
            //Long size = redisTemplate.boundListOps("SeckillGoodsCountList_" + id).size();
            //超卖方式二取法
            Long size = redisTemplate.boundHashOps("SeckillGoodsCount").increment(id, 0);
            if (size == null || size < 0) {
                size = 0L;
            }
            //3、扣减库存
            seckillGoods.setStockCount(size.intValue());
            redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, seckillGoods);
            //4、扣减库存后，库存数量不足1，把商品信息同步回Mysql
            if (seckillGoods.getStockCount() == 0) {
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //删除Redis的商品信息....(可以不做)
            }
            //5、创建订单并保存Redis订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());  //订单id
            seckillOrder.setSeckillId(seckillGoods.getId());  //购买商品
            seckillOrder.setMoney(seckillGoods.getCostPrice()); //支付金额
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");  //未支付
            redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

            //更新排队信息
            seckillStatus.setOrderId(seckillOrder.getId());  //记录订单号
            seckillStatus.setStatus(2);  //等待支付
            seckillStatus.setMoney(new Float(seckillOrder.getMoney()));  //应付金额
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
        }
    }
}
