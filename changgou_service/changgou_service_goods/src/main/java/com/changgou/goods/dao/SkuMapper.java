package com.changgou.goods.dao;
import com.changgou.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:sz.itheima
 * @Description:Sku的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SkuMapper extends Mapper<Sku> {
    /**
     * 扣减库存
     * @param num  扣减数量
     * @param skuId
     * @return 数据库受影响的行数
     */
    @Update("UPDATE tb_sku SET num = num - #{num},sale_num = sale_num + #{num} WHERE id = #{skuId} AND num > #{num}")
    int decrCount(@Param("num") Integer num, @Param("skuId") Long skuId);
}
