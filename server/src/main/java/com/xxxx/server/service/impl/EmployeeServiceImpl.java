package com.xxxx.server.service.impl;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xxxx.server.mapper.EmployeeMapper;
import com.xxxx.server.mapper.MailLogMapper;
import com.xxxx.server.pojo.*;
import com.xxxx.server.service.IEmployeeService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhoubin
 * @since 2022-01-26
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MailLogMapper mailLogMapper;
    /**
     * 获取所有员工（分页）
     * @param currentPage
     * @param size
     * @param employee
     * @param beginDateScope
     * @return
     */
    @Override
    public RespPageBean getEmployeeByPage(Integer currentPage, Integer size, Employee employee, LocalDate[] beginDateScope) {
        //开启分页
        Page<Employee> page = new Page<>(currentPage,size);//Page使用 com.baomidou下的
        IPage<Employee> employeeByPage = employeeMapper.getEmployeeByPage(page/*将上面已经开启的分页传入*/, employee, beginDateScope);
        RespPageBean respPageBean =new RespPageBean(employeeByPage.getTotal(),employeeByPage.getRecords());
        return respPageBean;
    }

    /**
     * 获取工号（最大的）
     * @return
     */
    @Override
    public RespBean maxWorkID() {
        /*获取字段的最大值  SELECT max(workID) FROM t_employee*/
        List<Map<String, Object>> maps = employeeMapper.selectMaps(new QueryWrapper<Employee>().select("max(workID)"));
        return RespBean.success(null,String.format("%8d",Integer.parseInt(maps.get(0)/*拿到Map*/.get("max(workID)")/*拿到值*/.toString()/*转为字符串类型（因为是Object类型所以要先转成字符串在转成Integer）*/)+1));
    }

    /**
     * 添加员工
     * @param employee
     * @return
     */
    @Override
    public RespBean addEmp(Employee employee) {

        //处理合同期限保留两位小数
        LocalDate beginContract = employee.getBeginContract();//计算合同时间，先获取合同的开始时间和合同的结束时间
        LocalDate endContract = employee.getEndContract();
        long days=beginContract.until(endContract, ChronoUnit.DAYS);//使用下面的utils  按照天计算
        DecimalFormat decimalFormat=new DecimalFormat("##.00");

        //将计算的结果保存到employee中
        employee.setContractTerm(Double.parseDouble(decimalFormat.format(days/365.00)));
        if (1==employeeMapper.insert(employee)){
            //获取员工对象
            Employee emp=employeeMapper.getWmployee(employee.getId()).get(0);

            //在发送消息之间首先要进行消息落库
            //数据库记录发送的消息
            String msgID = UUID.randomUUID().toString();//这就是消息ID
            MailLog mailLog=new MailLog();  //这里一块是自动生成的 GenerateAllSetter插件  使用alt+enter快捷键
            mailLog.setMsgId(msgID);
            mailLog.setEid(employee.getId());//员工的id
            mailLog.setStatus(0); //状态是0  只有当消息确认会掉成功的时候才会被改成1 在RabbitMQConfig的消息确认回调中修改
            mailLog.setRouteKey("");//路由key
            mailLog.setExchange("");//交换机
            mailLog.setCount(0);//重试次数，默认第一次是0
            mailLog.setTryTime(LocalDateTime.now().plusMinutes(MailConstants.MSG_TIMEOUT)/*设置当前时间加一分钟*/);//重试时间  因为是重试时间应该是当前时间往后推一分钟
            mailLog.setCreateTime(LocalDateTime.now());//创建时间
            mailLog.setUpdateTime(LocalDateTime.now());//更新时间
            mailLogMapper.insert(mailLog); //将我们设置的maillog放进去 ，到此消息的落户完成

            //发送信息
            //rabbitTemplate.convertAndSend("mail.welcome"/*这里是单纯的用路由键控制*/,emp);
            //这是第一次的发送任务，将这里的交换机改成常量会发送失败，走重试的交换机正确会发送成功（用这个来尝试重新发送的逻辑是不是正确）
            rabbitTemplate.convertAndSend(MailConstants.MAIL_EXCHANGE_NAME,MailConstants.MAIL_ROUTING_KEY_NAME,emp,new CorrelationData(msgID)/*将id放进去，不放的话发送的消息里面是没有id的*/);
            return RespBean.success("添加成功！");
        }
        return RespBean.error("添加失败！");
    }

    /**
     * 查询员工
     * @param id
     * @return
     */
    @Override
    public List<Employee> getEmployee(Integer id) {
        return employeeMapper.getWmployee(id);
    }

    @Override
    public RespPageBean getEmployeeWithSalary(Integer currentPage, Integer size) {
        Page<Employee> page=new Page<>(currentPage,size);//开启分页
        IPage<Employee> employeeIPage = employeeMapper.getEmployeeWithSalary(page);
        RespPageBean respPageBean=new RespPageBean(employeeIPage.getTotal()/*获取总条数*/,employeeIPage.getRecords()/*获取具体的一条数据*/);
        return respPageBean;
    }
}
