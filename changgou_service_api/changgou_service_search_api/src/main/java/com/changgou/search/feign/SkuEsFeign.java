package com.changgou.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.search.feign
 * @date 2020-2-11
 */
@FeignClient(name = "search")
@RequestMapping("search")
public interface SkuEsFeign {
    /***
     * 搜索商品
     * @param searchMap 搜索条件
     * @return 结果集
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String, String> searchMap);
}
