package com.changgou.test;

import io.jsonwebtoken.*;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.test
 * @date 2020-2-12
 */
public class JwtTest {

    //数据加密-生成令牌
    @Test
    public void testCreateJWT(){
        //1、创建Jwt构建器-jwtBuilder = Jwts.builder()
        JwtBuilder jwtBuilder = Jwts.builder();
        //2、设置唯一编号-setId
        jwtBuilder.setId("hm-007");
        //3、设置主题，可以是JSON数据-setSubject()
        jwtBuilder.setSubject("测试-001");
        //4、设置签发日期-setIssuedAt
        jwtBuilder.setIssuedAt(new Date());
        //5、设置签发人-setIssuer
        jwtBuilder.setIssuer("sz-itheima");

        //设置令牌有效时间-setExpiration(令牌生效最后时间)
        //有效30秒
        //new Date().getTime()  == System.currentTimeMillis()
        //Date exp = new Date(System.currentTimeMillis() + 30000);
        //jwtBuilder.setExpiration(exp);

        //自定义claims
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Steven");
        user.put("age", "18");
        user.put("address", "深圳市.黑马程序员");
        jwtBuilder.addClaims(user);


        //6、设置签证-signWith(使用的加密算法，密文密钥[base64加密])
        jwtBuilder.signWith(SignatureAlgorithm.HS256, "itheima-steven");
        //7、生成令牌-compact()
        String token = jwtBuilder.compact();
        //8、输出结果
        System.out.println(token);
    }

    @Test
    public void testParseJwt() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJobS0wMDciLCJzdWIiOiLmtYvor5UtMDAxIiwiaWF0IjoxNTgxNDg4NjE4LCJpc3MiOiJzei1pdGhlaW1hIiwiYWRkcmVzcyI6Iua3seWcs-W4gi7pu5HpqaznqIvluo_lkZgiLCJuYW1lIjoiU3RldmVuIiwiYWdlIjoiMTgifQ.Jq9pzX5urfQCmp9CgrqLlhCgMme5AyIkKGbRJyfTgIE";
        //1、创建解析器
        JwtParser parser = Jwts.parser();
        //2、设置密钥
        parser.setSigningKey("itheima-steven");
        //3、解析数据
        Claims claims = parser.parseClaimsJws(token).getBody();

        System.out.println(claims);
    }
}
