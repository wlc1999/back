package com.xxxx.server.config;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.rabbitmq.client.AMQP;
import com.xxxx.server.pojo.MailConstants;
import com.xxxx.server.pojo.MailLog;
import com.xxxx.server.service.IMailLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;




/**
 * RabbitMQ的配置类
 */

@Configuration
public class RabbitMQConfig {
    //RabbitMQ的消息确认回调和失败回调
    private static final Logger LOGGER= LoggerFactory.getLogger(RabbitMQConfig.class);//打印下日志

    @Autowired  //注入连接方法
    private CachingConnectionFactory cachingConnectionFactory;  //连接工厂
    @Autowired  //注入数据库
    private IMailLogService mailLogService;

    @Bean
    public RabbitTemplate rabbitTemplate(){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);

        /**
         *消息确认回调，确认参数是否到达broker
         * data:消息唯一标识
         * ack：确认结果
         * cause：失败原因
         */
        rabbitTemplate.setConfirmCallback((data,ack,cause)->{
            String msgId=data.getId();
            if (ack){
                LOGGER.info("{}===>消息发送成功",msgId);//打印日志
                mailLogService.update(new UpdateWrapper<MailLog>().set("status",1).eq("msgId",msgId));//根据msgId更新状态成功
            }else {
                LOGGER.error("{}===>消息发送失败",msgId);
            }
        });

        /**
         * 消息失败回调，比如router不到queue时回调  (写完两个配置不要忘记消息回调publisher-confirm-type: correlated;publisher-returns: true)
         * msg:消息主题
         * repCode:响应码
         * repText:响应描述
         * exchange:交换机
         * routingKey:路由键
         */
        rabbitTemplate.setReturnCallback((msg,repCode,repText,exchange,routingKey
        )->{
            LOGGER.info("{}=====>消息发送到queue时失败",msg.getBody());
        });
        return rabbitTemplate;
    }

    @Bean //队列
    public Queue queue(){
        return new Queue(MailConstants.MAIL_QUEUE_NAME);
    }
    @Bean  //路由模式  交换机
    public DirectExchange directExchange(){
        return new DirectExchange(MailConstants.MAIL_EXCHANGE_NAME);
    }
    @Bean  //将队列和我们的路由键进行绑定
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(directExchange()).with(MailConstants.MAIL_ROUTING_KEY_NAME);
    }


}
