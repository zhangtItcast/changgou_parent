package com.changgou.controller;

import com.changgou.search.feign.SkuEsFeign;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.controller
 * @date 2020-2-11
 */
@Controller
@RequestMapping("/search")
public class SkuEsController {
    @Autowired
    private SkuEsFeign skuEsFeign;

    @GetMapping("/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) {
        //1、查询数据
        Map result = skuEsFeign.search(searchMap);
        //2、返回数据
        model.addAttribute("result", result);
        //返回查询参数到页面
        if (searchMap.get("pageNum") == null) {
            searchMap.put("pageNum", "1");
        }
        model.addAttribute("searchMap", searchMap);

        //拼接请求url
        String url = getUrl(searchMap);
        model.addAttribute("url", url);


        //构建分页对象Page并返回到页面
        Page page = new Page(
                new Long(result.get("total").toString()),
                new Integer(result.get("pageNum").toString()),
                new Integer(result.get("pageSize").toString())
        );
        model.addAttribute("page", page);

        //3、响应视图
        return "search";
    }

    /**
     * 拼接get请求的url与参数
     * @param searchMap
     * @return
     */
    private String getUrl(@RequestParam(required = false) Map<String, String> searchMap) {
        String url = "/search/list";
        if (searchMap != null && searchMap.size() > 0) {
            // "/search/list?keywords=华为&category=笔记本"
            url += "?";
            for (String key : searchMap.keySet()) {
                //排除排序条件，每次排序条件都是新的
                if (key.indexOf("sort") > -1 || key.equals("pageNum")) {
                    continue;
                }
                url = url + key + "=" + searchMap.get(key) + "&";
            }
            //去除最后一个&
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
