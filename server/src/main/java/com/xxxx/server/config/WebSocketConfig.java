package com.xxxx.server.config;

import com.xxxx.server.config.security.component.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.net.http.WebSocket;

/**
 * WebSocket的配置类   客户端发起一次会话，服务器端响应一次会话，就建立了一次连接
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {  //打开选择继承的快捷键是ctrl+o

    @Value("${jwt.tokenHead}")//引入yaml里面的tokenhead
    private String tokenHead;
    @Autowired
    private JwtTokenUtils jwtTokenUtil;
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 网页可以通过websocket连接上服务
     * 配置websocket的服务地址，指定是否使用socketJS
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        /**为了让客户端连接我们  /ws/ep是前端连接我们的端点
         * 1.将ws/ep路径注册为stomp的端点，用户连接了这个端点就可以进行websocket通讯了，同时支持socketJS
         * 2.setAllowedOrigins("*")  允许跨域
         * 3.withSockJS()使用sockJS去连接
         */
        registry.addEndpoint("/ws/ep").setAllowedOrigins("*")/*设置跨域允许*/.withSockJS()/*允许SockJS*/; //接下来运行下面的重写
    }

    /**
     * 这个方法在没有用jwt令牌的时候是不需要配置的
     * 输入通道参数配置
     * 主要视为了获取jwt令牌防止被拦截
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {//相当于一个拦截器的效果
            @Override
            public Message<?> preSend/*预发送*/(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor/*拿这个主要是为了判断一下这个是不是链接*/ = MessageHeaderAccessor.getAccessor(message/*转进来的message*/, StompHeaderAccessor.class);
                //判断是否为连接，如果是，需要获取token，并且设置用户对象
                if (StompCommand.CONNECT.equals(accessor.getCommand())){
                    //拿到token
                    String token = accessor.getFirstNativeHeader("Auth-Token"/*这个参数是前端发过来的*/);
                    if (!StringUtils.isEmpty(token)){
                        String authToken =
                                token.substring(tokenHead.length());//拿到完整的令牌
                        String username =
                                jwtTokenUtil.getUserNameFromToken(authToken);//从token里面拿到用户名
                            //token中存在用户名
                        if (!StringUtils.isEmpty(username)){
                            //登录
                            UserDetails userDetails =
                                    userDetailsService.loadUserByUsername(username);
                            //验证token是否有效，如果有效重新设置用户对象
                            if (jwtTokenUtil.validataToken(authToken,userDetails)){
                                UsernamePasswordAuthenticationToken authenticationToken =
                                        new UsernamePasswordAuthenticationToken(userDetails, null,
                                                userDetails.getAuthorities()/*权限列表*/);
                                SecurityContextHolder.getContext()/*获取springSec的全局上下文*/.setAuthentication(authenticationToken);//spring的全局对象里面设置
                                accessor.setUser(authenticationToken);//z在access里面设置
                            }
                        }
                    }
                }
                return message;
            }
        });

                        }

    /**主动给客户端推送消息
     * 配置消息代理
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //配置代理域，可以配置多个，配置代理目的地前缀为/queue，可以在配置域上向客户端推送消息
        registry.enableSimpleBroker("/queue"/*指明前缀是queue*/);
    }
}
