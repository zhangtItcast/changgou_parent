package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.seckill.task
 * @date 2020-2-19
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Component
public class SeckillGoodsPushTask {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    //配置定时器
    @Scheduled(cron = "0/15 * * * * *")
    public void loadGoodsPushRedis() {
        System.out.println("定时任务被调用，当前时间为：" + new Date());
        //完成商品同步到Redis
        //1、获取当前时间菜单
        List<Date> dateMenus = DateUtil.getDateMenus();
        //2、循环匹配当前时间段的商品列表，保存在Redis中
        for (Date now : dateMenus) {
            //2020021910-当前时间段
            String dataStr = DateUtil.data2str(now, DateUtil.PATTERN_YYYYMMDDHH);
            //构建商品查询条件
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //审核通过的商品
            criteria.andEqualTo("status", "1");
            //有货商品,剩余库存数 > 0
            criteria.andGreaterThan("stockCount", 0);
            //时间在匹配范围   开始时间 <= now <= 结束时间
            criteria.andGreaterThanOrEqualTo("startTime", now);
            //now<=2020021910 + 2小时
            criteria.andLessThanOrEqualTo("endTime", DateUtil.addDateHour(now,2));
            //排除Redis中已有的商品列表.............
            Set ids = redisTemplate.boundHashOps("SeckillGoods_" + dataStr).keys();
            // AND id not in (2,3,,3,3,34)
            if (ids != null && ids.size() > 0) {
                criteria.andNotIn("id", ids);
            }

            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            System.out.println(dataStr + " 时间段，导入商品个数为：" + seckillGoodsList.size());
            //把商品信息同步到Redis中
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                //SeckillGoods_2020021910
                redisTemplate.boundHashOps("SeckillGoods_" + dataStr).put(seckillGoods.getId(), seckillGoods);

                //超卖解决方案一：分布式队列:SeckillGoodsCountList_商品id
                //以一个库存为一个队列
                for (int i = 0; i < seckillGoods.getStockCount(); i++) {
                    redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGoods.getId()).leftPush(i);
                }

                //超卖解决方案二：Redis中的自减,Hash<商品id,库存数>
                //increment(商品id,库存数)
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGoods.getId(), seckillGoods.getStockCount());
            }
        }
    }
}
