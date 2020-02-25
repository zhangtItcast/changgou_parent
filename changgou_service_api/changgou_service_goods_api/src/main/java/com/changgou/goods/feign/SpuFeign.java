package com.changgou.goods.feign;

import com.changgou.goods.pojo.Spu;
import entity.Result;
import entity.StatusCode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.goods.feign
 * @date 2020-2-15
 */
@FeignClient("goods")
@RequestMapping("/spu")
public interface SpuFeign {

    /***
     * 根据ID查询Spu数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable Long id);

}
