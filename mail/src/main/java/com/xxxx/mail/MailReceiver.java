package com.xxxx.mail;

import com.xxxx.server.pojo.Employee;
import com.xxxx.server.pojo.MailConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;


/**
 * 消息接受者
 */
@Component
public class MailReceiver {
    private static final Logger LOGGER= LoggerFactory.getLogger(MailReceiver.class);

    @Autowired  //邮件发送类
    private JavaMailSender javaMailSender;
    @Autowired  //邮件配置类
    private MailProperties mailProperties;
    @Autowired  //tamplate引擎
    private TemplateEngine templateEngine;

    @RabbitListener(queues = MailConstants.MAIL_QUEUE_NAME)//接收者邮件监听  监听的是主方法中的queue
    public void handler(Employee employee){  //发送邮件
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper =new MimeMessageHelper(mimeMessage);
        try {
            helper.setFrom(mailProperties.getUsername());//发件人
            helper.setTo(employee.getEmail());//收件人
            helper.setSubject("入职欢迎邮件");//主题
            helper.setSentDate(new Date());//发送日期
            Context context =new Context();//右键内容
            context.setVariable("name",employee.getName());
            context.setVariable("posname",employee.getPosition().getName());
            context.setVariable("joblevelName",employee.getJoblevel().getName());
            context.setVariable("departmentname",employee.getDepartment().getName());
            String mail=templateEngine.process("mail",context);//拿到mail
            helper.setText(mail,true); //发送mail
            javaMailSender.send(mimeMessage);  //发送邮件
        } catch (MessagingException e) {
            LOGGER.error("邮件发送失败========>{}",e.getMessage());
        }


    }
}
