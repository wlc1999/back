package com.xxxx.server.config.security.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * jwt的登陆授权过滤器
 */
public class JwtAuthencationTokenFilter extends OncePerRequestFilter {

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Autowired
    private JwtTokenUtils jwtTokenUtils;
    @Autowired
    private UserDetailsService userDetailsService;



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader=request.getHeader(tokenHeader);
        //token存在，并且是以tokenHead开头的
        if (authHeader!=null && authHeader.startsWith(tokenHead)){
            String authToken = authHeader.substring(tokenHead.length());  //字符串的截取
            String username=jwtTokenUtils.getUserNameFromToken(authToken);//从token里面拿到用户名
            //token存在用户名，但是未登录
            if (username!=null && SecurityContextHolder.getContext().getAuthentication()==null){
                //登录
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);//通过userDetailsService进行登录
                //验证token是否有效，重新设置用户对象
                if (jwtTokenUtils.validataToken(authToken,userDetails)){
                    UsernamePasswordAuthenticationToken authenticationToken=new
                            UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));//重新设置到用户对象里面
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

        }
        filterChain.doFilter(request,response);
    }
}
