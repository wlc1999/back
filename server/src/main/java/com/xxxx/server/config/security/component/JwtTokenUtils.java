package com.xxxx.server.config.security.component;



import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JwtToken的工具类
 */
@Component
public class JwtTokenUtils {

    //根据用户名生成我们的Token   也根据Token拿到我们需要的用户名
    private static final String CLAIM_KEY_USERNAME="sub";
    private static final String CLAIM_KEY_CREATED="created";  //jwt的创建时间
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;


    /**
     * 根据用户信息生成Token
     * @param userDetails
     * @return
     */
    //获取前台用户的登录信息
    public String generateToken(UserDetails userDetails/*这是spring Sec中的一个属性*/){
        Map<String,Object> claims=new HashMap<>();//荷载
        claims.put(CLAIM_KEY_USERNAME,userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED,new Date());
        return generateToken(claims);
    }

    /**
     * 从token中获取登录用户名
     * @param token
     * @return
     */
    public String getUserNameFromToken(String token){
        String username;
        try {
            Claims claims = getClaimsFromToken(token);//在token中拿到荷载
            username=claims.getSubject();
        } catch (Exception e) {
            username=null;
        }
        return username;
    }

    /**
     * 验证token是否有效
     * @param token
     * @param userDetails
     * @return
     */

    public boolean validataToken(String token,UserDetails userDetails){
        String username=getUserNameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);  //判断token中的用户名和userDetails的用户名是否相同   isTokenExpired判断是否已经失效
    }

    /**
     * 判断token是否可以被刷新
     * @param token
     * @return
     */
    public boolean canRefresh(String token){
        return !isTokenExpired(token);
    }

    /**
     * 刷新Token
     * @param token
     * @return
     */
    public String refreshToken(String token){
        Claims claims=getClaimsFromToken(token);
        claims.put(CLAIM_KEY_CREATED,new Date());
        return generateToken(claims);
    }



    /**
     * 判断token是否失效
     * @param token
     * @return
     */
    private boolean isTokenExpired(String token) {
        Date expireDate =getExpireDateFromToken(token);
        return expireDate.before(new Date());//判断如果在当前时间前面就是失效的  expireDate是创建时的时间减伤失效时长
    }

    /**
     * 从token中获取失效时间
     * @param token
     * @return
     */
    private Date getExpireDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();  //获取过期时间
    }

    /**
     * 从token中获取荷载
     * @param token
     * @return
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims=null;

        try {
            claims= Jwts.parser()//转荷载
                    .setSigningKey(secret)//放入转荷载需要的签名
                    .parseClaimsJws(token)
                    .getBody(); //获取荷载
        } catch (Exception e) {
            e.printStackTrace();
        }
        return claims;
    }


    /**
     * 根据荷载生成JWT TOKEN
     * @param claims
     * @return
     */
    private String generateToken(Map<String,Object> claims) {
        return Jwts.builder()       //生成
                .setClaims(claims)  //放入荷载
                .signWith(SignatureAlgorithm.HS512,secret)//签名  后边的是秘钥
                .setExpiration(generateExpirationDate())  //失效时间  里面是失效时间的转换方法
                .compact();
    }


    /**
     * 生成token失效时间
     * @return
     */
    private Date generateExpirationDate() {
        Long time=System.currentTimeMillis();
        return new Date(time+expiration*1000);//失效时间是当前时间加上我们配置的一个时间
    }


}
