package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.user.feign
 * @date 2020-2-15
 */
@FeignClient(name = "user")
@RequestMapping("user")
public interface UserFeign {

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping("/load/{id}")
    public Result<User> findById(@PathVariable String id);

    /***
     * 添加用户积分
     * @param points
     * @return
     */
    @RequestMapping("/points/add")
    public Result addUserPoints(@RequestParam Integer points);
}
