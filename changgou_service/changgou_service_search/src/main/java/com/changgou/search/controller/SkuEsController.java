package com.changgou.search.controller;

import com.changgou.search.service.SkuEsService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.search.controller
 * @date 2020-2-8
 */
@RestController
@CrossOrigin
@RequestMapping("search")
public class SkuEsController {

    @Autowired
    private SkuEsService skuEsService;

    @GetMapping("/import")
    public Result importSku(){
        skuEsService.importSku();
        return new Result(true, StatusCode.OK, "导入数据成功");
    }


    /***
     * 搜索商品
     * @param searchMap 搜索条件
     * @return 结果集
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String, String> searchMap) {
        Map result = skuEsService.search(searchMap);
        return result;
    }

}
