package com.xxxx.server.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)

public class ChatMsg {

    private String from;//哪个人发的
    private String to;//要发送到哪里去
    private String content;//对应的内容
    private LocalDateTime date;//时间
    private String fromNickName;//昵称是什么

}
