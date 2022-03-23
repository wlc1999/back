package com.xxxx.server.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxxx.server.pojo.Employee;
import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.pojo.RespPageBean;
import com.xxxx.server.pojo.Salary;
import com.xxxx.server.service.IEmployeeService;
import com.xxxx.server.service.ISalaryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工账套
 */
@RestController
@RequestMapping("/salary/sobcfg")

public class SalarySobCfgController {

    @Autowired
    private IEmployeeService employeeService;
    @Autowired
    private ISalaryService salaryService;

    @ApiOperation(value = "获取所有员工账套")
    @GetMapping("/")
    public RespPageBean/*因为要进行分页，所以要是用这种返回类型*/ getEmployeeWithSalary(
                                                @RequestParam(defaultValue = "1") Integer currentPage,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return employeeService.getEmployeeWithSalary(currentPage, size);
    }

    @ApiOperation(value = "获取所有工资账套")
    @GetMapping("/salaries")
    public List<Salary> getAllSalaries() {
        return salaryService.list();
    }

    @ApiOperation(value = "更新员工账套")//通过员工id和账套id，修改员工对应的工资账套
    @PutMapping("/")
    public RespBean updateEmployeeSalary(Integer eid/*员工id*/, Integer sid/*账套id*/) {
        if (employeeService.update(new UpdateWrapper<Employee>().set("salaryId", sid).eq("id", eid))) {
            return RespBean.success("更新成功!");
        }
        return RespBean.error("更新失败!");
    }



}
