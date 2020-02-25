package com.changgou.user.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.user.pojo.User;
import com.changgou.user.service.UserService;
import com.github.pagehelper.PageInfo;
import entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/****
 * @Author:shenkunlin
 * @Description:
 * @Date 2019/6/14 0:18
 *****/

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    /***
     * User分页条件搜索实现
     * @param user
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false)  User user, @PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页条件查询User
        PageInfo<User> pageInfo = userService.findPage(user, page, size);
        return new Result(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * User分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用UserService实现分页查询User
        PageInfo<User> pageInfo = userService.findPage(page, size);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param user
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<User>> findList(@RequestBody(required = false)  User user){
        //调用UserService实现条件查询User
        List<User> list = userService.findList(user);
        return new Result<List<User>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    //当前方法，只允许admin角色访问
    @PreAuthorize("hasAnyAuthority('admin')")
    public Result delete(@PathVariable String id){
        //调用UserService实现根据主键删除
        userService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改User数据
     * @param user
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody  User user,@PathVariable String id){
        //设置主键值
        user.setUsername(id);
        //调用UserService实现修改User
        userService.update(user);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 新增User数据
     * @param user
     * @return
     */
    @PostMapping
    public Result add(@RequestBody   User user){
        //调用UserService实现添加User
        userService.add(user);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping({"/{id}","/load/{id}"})
    public Result<User> findById(@PathVariable String id){
        //调用UserService实现根据主键查询User
        User user = userService.findById(id);
        return new Result<User>(true,StatusCode.OK,"查询成功",user);
    }

    /***
     * 查询User全部数据
     * @return
     */
    @GetMapping
    public Result<List<User>> findAll(){
        //调用UserService实现查询所有User
        List<User> list = userService.findAll();
        return new Result<List<User>>(true, StatusCode.OK,"查询成功",list) ;
    }

    //用户登录
    @RequestMapping("login")
    public Result<User> login(String username, String password, HttpServletResponse response) {
        //1、查询用户是否存在
        User user = userService.findById(username);
        //2、不存在返回错误
        if (user == null) {
            return new Result(false, StatusCode.ERROR, "用户名不存在！");
        }else {
            //3、用户存在匹配密码
            //checkpw(明文，密文)  返回对比结果
            if (BCrypt.checkpw(password, user.getPassword())) {
                //存储用户信息在redis中
                //把信息存在cookie中--加密方案

                //封装令牌信息
                Map<String, Object> map = new HashMap<>();
                map.put("role","USER");
                map.put("flag",true);
                map.put("user",user);

                //生成令牌
                String token = JwtUtil.createJWT(UUID.randomUUID().toString(), JSON.toJSONString(map), null);


                //响应用户两种方案：
                //1、设置到响应头中
                response.setHeader("Authorization", token);
                //2、保存在cookie中
                Cookie cookie = new Cookie("Authorization",token);

                //设置cookie生效范围
                cookie.setPath("/");

                response.addCookie(cookie);

                return new Result<User>(true, StatusCode.OK, "登录成功！", user);
            } else {
                return new Result(false, StatusCode.ERROR, "密码不正确！");
            }
        }
    }

    /***
     * 添加用户积分
     * @param points
     * @return
     */
    @RequestMapping("/points/add")
    public Result addUserPoints(@RequestParam Integer points) {
        String username = TokenDecode.getUserInfo().get("username");

        userService.addUserPoints(username, points);

        return new Result(true, StatusCode.OK, "积分添加成功！");
    }
}
