package com.xxxx.server.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxxx.server.pojo.Employee;
import com.xxxx.server.pojo.MailConstants;
import com.xxxx.server.pojo.MailLog;
import com.xxxx.server.service.IEmployeeService;
import com.xxxx.server.service.IMailLogService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮件发送服务定时任务
 */
@Component
public class MailTask {
    @Autowired
    private IMailLogService mailLogService;
    @Autowired
    private IEmployeeService employeeService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

/**
 * 右键发送定时任务，十秒发送一次
 */
    @Scheduled(fixedRate=10 * 1000)
    public void mailTask(){
        List<MailLog> list = mailLogService.list(new QueryWrapper<MailLog>().eq("status", 0).lt("tryTime", LocalDateTime.now())/*拿到重试时间*/);//获取投递中的信息
        //将获取的所有为0的进行循环发送
        list.forEach(mailLog -> {
            //如果重试次数为3次，则更新状态为发送失败，
            if (3<=mailLog.getCount()){
                mailLogService.update(new UpdateWrapper<MailLog>().set("statue",2).eq("msgId",mailLog.getMsgId()));
            }else {//否则重试次数+1，更新时间,重试时间
                mailLogService.update(new UpdateWrapper<MailLog>().set("count",mailLog.getCount()+1)
                        .set("updateTime",LocalDateTime.now()).set("tryTime",LocalDateTime.now().plusMinutes(MailConstants.MSG_TIMEOUT)).eq("msgId",mailLog.getMsgId())/*不加eq就变成了全局修改*/);
                //根据id获取员工的信息
                Employee emp=employeeService.getEmployee(mailLog.getEid()).get(0);
                //发送消息
                rabbitTemplate.convertAndSend(MailConstants.MAIL_EXCHANGE_NAME,
                        MailConstants.MAIL_ROUTING_KEY_NAME, emp,
                        new CorrelationData(mailLog.getMsgId()));


            }
        });

    }

}
