package com.xxxx.server.controller;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.xxxx.server.pojo.*;
import com.xxxx.server.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Insert;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhoubin
 * @since 2022-01-26
 */
@RestController
@RequestMapping("/employee/basic")
public class EmployeeController {

    @Autowired
    private IEmployeeService employeeService;
    @Autowired
    private IPoliticsStatusService politicsStatusService;
    @Autowired
    private IJoblevelService joblevelService;
    @Autowired
    private INationService nationService;
    @Autowired
    private IPositionService positionService;
    @Autowired
    private IDepartmentService departmentService;

    @ApiOperation(value = "获取所有员工（分页查询每页十条记录）")
    @GetMapping("/")
    public RespPageBean getEmployee(@RequestParam(defaultValue = "1"/*默认为第一页*/) Integer currentPage,//当前页
                                    @RequestParam(defaultValue = "10"/*每页默认10条*/) Integer size,//每一页的大小
                                    Employee employee,/*完整的员工对象*/
                                LocalDate[] beginDateScope/*接收的日期范围*/){
        return employeeService.getEmployeeByPage(currentPage,size,employee,beginDateScope);
    }

    @ApiOperation(value = "获取所有政治面貌")
    @GetMapping("/politicsstatus")
    public List<PoliticsStatus> getAllPoliticsStatus(){
        return politicsStatusService.list();
    }

    @ApiOperation(value = "获取所有职称")
    @GetMapping("/joblevels")
    public List<Joblevel> getAllJoblevels(){
    return joblevelService.list();
    }

    @ApiOperation(value = "获取所有民族")
    @GetMapping("/nations")
    public List<Nation> getAllNations(){
        return nationService.list();
    }

    @ApiOperation(value = "获取所有职位")
    @GetMapping("/positions")
    public List<Position> getAllPositions(){
        return positionService.list();
    }
    @ApiOperation(value = "获取所有部门")
    @GetMapping("/deps")
    public List<Department> getAllDepartments(){
        return departmentService.getAllDepartments();
    }

    @ApiOperation(value = "获取工号（最大的）")
    @GetMapping("/maxWorkID")
    public RespBean maxWorkID(){
        return employeeService.maxWorkID();
    }

    @ApiOperation(value = "添加员工(添加时不需要写id)")
    @PostMapping("/")
    public RespBean addEmp(@RequestBody Employee employee){
        return employeeService.addEmp(employee);
    }

    @ApiOperation(value = "更新员工")
    @PutMapping("/")
    public RespBean updateEmp(@RequestBody Employee employee){
        if (employeeService.updateById(employee)){
            return RespBean.success("更新成功！");
        }
        return RespBean.error("更新失败！");
    }
    @ApiOperation(value = "删除员工")
    @DeleteMapping("/{id}")
    public RespBean deleteEmp(@PathVariable Integer id){
        if (employeeService.removeById(id)){
            return RespBean.success("删除成功");
        }
        return RespBean.error("删除失败");
    }

    //导出Excel表格，需要以流的形式导出表格
    @ApiOperation(value = "导出员工数据")
    @GetMapping(value = "/export",produces = "application/octet-stream")//表示用流的形式输出防止了乱码
    public void exportEmployee(HttpServletResponse response){
        //查询到所有的数据
        List<Employee> list = employeeService.getEmployee(null);//因为是要查询所有所以id是不用传的
        //准备导出表
        ExportParams params=new ExportParams("员工表"/*表名*/,"员工表"/*sheet名*/, ExcelType.HSSF);
        /*提供的一个导出工具类*/
        Workbook workbook = ExcelExportUtil.exportExcel(params/*参数*/, Employee.class/*对象*/, list/*数据*/);
        /*准备response的输出流*/
        ServletOutputStream outputStream=null;
        try {
            //流形式  可以在百度上搜 响应头如何实现流形式，如何防止中文乱码等等。
            response.setHeader("content-type","application/octet-stream");
            //防止中文乱码
            response.setHeader("content-disposition", "attachment;filename=" +
                    URLEncoder.encode("员工表.xls","UTF-8"));
            outputStream = response.getOutputStream();
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null!=outputStream){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @ApiOperation(value = "导入员工数据")
    @PostMapping("/import")
    public RespBean importEmployee(MultipartFile file){
        ImportParams params=new ImportParams();// 导入文件用ImportParam
        params.setTitleRows(1);//去掉第一行
        List<Nation> nationList =nationService.list();//拿到所有的民族
        List<PoliticsStatus> politicsStatusList = politicsStatusService.list();
        List<Department> departmentList = departmentService.list();
        List<Joblevel> joblevelList= joblevelService.list();
        List<Position> positionList = positionService.list();
        try {
            //拿到数据了
            List<Employee> list = ExcelImportUtil.importExcel(file.getInputStream()/*以流的形式*/, Employee.class/*字节码*/, params);

            //因为导入的时候表中显示的是实体类的名字，通过下面的方法获取实体类的id
            list.forEach(employee -> {
                //民族id  都是重写了hashcode和equals方法，但是当需要的数据经常变动的时候并不适合使用这个方法
                employee.setNationId(nationList.get/*拿到完整的Nation对象*/(nationList.indexOf/*这里使用到了hashcode的equals方法进行比较拿到索引下标*/( new Nation(employee.getNation().getName())/*重写了equals和hashcodeN安通里面必须有name*/)).getId());
                //政治面貌id
                employee.setPoliticId(politicsStatusList.get(politicsStatusList.indexOf(new PoliticsStatus(employee.getPoliticsStatus().getName()))).getId());
                //部门id
                employee.setDepartmentId(departmentList.get(departmentList.indexOf(new Department(employee.getDepartment().getName()))).getId());
                //职称id
                employee.setJobLevelId(joblevelList.get(joblevelList.indexOf(new Joblevel(employee.getJoblevel().getName()))).getId());
                //职位id
                employee.setPoliticId(positionList.get(positionList.indexOf(new Position(employee.getPosition().getName()))).getId());
            });
            if(employeeService.saveBatch(list)){
                return RespBean.success("导入成功！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RespBean.error("导入失败！");

    }


}
