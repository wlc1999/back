package com.xxxx.server.controller;

import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.ChatMsg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 前端会接收服务器通过 /user/queue/chat 发送来的消息
 */
@Controller
public class WsController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/ws/chat") //这是前端发消息调用的一个接口
    public void handleMsg(Authentication authentication/*要获取用户，是在config的87行放入的*/, ChatMsg chatMsg){

        //这些是处理前端发送过来的消息
        Admin admin = (Admin) authentication.getPrincipal();//根据Authenication获取当前用户对象 ，也可以直接注入Principal在/admin/info中
        chatMsg.setFrom(admin.getUsername());//用户名
        chatMsg.setFromNickName(admin.getName());//昵称
        chatMsg.setDate(LocalDateTime.now());//时间
/**
 * 上面处理完成了发给对应的用户 通过/queue/chat进行中转中转到config的configureMessageBroker
 * 发送消息
 * 1.消息接收者
 * 2.消息队列
 * 3.消息对象
 */
        simpMessagingTemplate.convertAndSendToUser(chatMsg.getTo()/*发送给谁*/,"/queue/chat"/*队列queue（给前端接收？）*/, chatMsg/*发送的一个消息*/);

    }

}
