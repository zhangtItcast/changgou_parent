package com.changgou.gateway.filter;

import com.changgou.gateway.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.gateway.filter
 * @date 2020-2-12
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //用户传令牌key
    private static final String AUTHORIZATION = "Authorization";
    //登录url
    private static final String USER_LOGIN_URL = "http://localhost:9001/oauth/login";


    /**
     * 过滤拦截逻辑实现
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1、获取Request、Response对象-exchange.get...
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //2、获取请求的URI-request.getURI().getPath()
        String uri = request.getURI().getPath();
        //3、如果是登录请求-uri.startsWith，放行-chain.filter
        if (URLFilter.hasAuthorize(uri)) {
            return chain.filter(exchange);
        }else{ //4、如果是非登录请求
            //4.1 获取前端传入的令牌-从请求头中获取-request.getHeaders().getFirst
            String token = request.getHeaders().getFirst(AUTHORIZATION);
            //4.2 如果头信息中没有，从请求参数中获取-request.getQueryParams().getFirst
            if (StringUtils.isBlank(token)) {
                token = request.getQueryParams().getFirst(AUTHORIZATION);
            }
            //4.3 如果请求参数中没有，从cookie中获取-request.getCookies()
            // -取值前先判断不为空-getFirst
            if (StringUtils.isBlank(token)) {
                MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                //有cookie
                if (cookies != null) {
                    HttpCookie cookie = cookies.getFirst(AUTHORIZATION);
                    if (cookie != null) {
                        token = cookie.getValue();
                    }
                }
            }
            //4.4 如果以上方式都取不到令牌-返回405错误-response.setStatusCode(405)-return response.setComplete
            if (StringUtils.isBlank(token)) {
                //返回405错误，标识没有权限操作
                //response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);

                //获取当前请求路径，传递到后续逻辑中
                String url = USER_LOGIN_URL + "?FROM=" + request.getURI();
                //跳转登录页
                response.setStatusCode(HttpStatus.SEE_OTHER);
                //跳转的地址
                response.getHeaders().set("Location",url);

                return response.setComplete();
            }else{
                try {
                    // 4.5 如果获取到了令牌，解析令牌-JwtUtil.parseJWT，放行-chain.filter(exchange)
                    //Claims claims = JwtUtil.parseJWT(token);
                    //System.out.println("认证成功：" + claims);

                    //把令牌传递到后续的微服务中
                    //4.5.1解析成功-把令牌返回-request.mutate().header(key,value)
                    request.mutate().header(AUTHORIZATION, "bearer " + token);
                } catch (Exception e) {  //解析令牌失败，无效令牌
                    e.printStackTrace();
                    //返回401错误，表示认证过程失败了
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
                //放行
                return chain.filter(exchange);
            }
        }
    }


    /**
     * 用于设置过滤器执行顺序，数据越小，越先执行
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
