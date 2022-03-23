package com.xxxx.server.pojo;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhoubin
 * @since 2022-01-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_employee")
@ApiModel(value="Employee对象", description="")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "员工编号")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "员工姓名")
    @Excel(name = "员工姓名")  //Easy POI的注释 说明导出的excel表格这一列的名字
    private String name;

    @ApiModelProperty(value = "性别")
    @Excel(name = "性别")
    private String gender;

    @ApiModelProperty(value = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "Asia/Shanghai")
    @Excel(name = "出生日期",width = 20/*设置这一列的宽度*/,format = "yyyy-MM-dd"/*格式化*/)
    private LocalDate birthday;

    @ApiModelProperty(value = "身份证号")
    @Excel(name = "身份证号",width = 30)
    private String idCard;

    @ApiModelProperty(value = "婚姻状况")
    @Excel(name = "婚姻状况")
    private String wedlock;

    @ApiModelProperty(value = "民族")
    @Excel(name = "民族")
    private Integer nationId;

    @ApiModelProperty(value = "籍贯")
    @Excel(name = "籍贯")
    private String nativePlace;

    @ApiModelProperty(value = "政治面貌")
    @Excel(name = "政治面貌")
    private Integer politicId;

    @ApiModelProperty(value = "邮箱")
    @Excel(name = "邮箱")
    private String email;

    @ApiModelProperty(value = "电话号码")
    @Excel(name = "电话号码")
    private String phone;

    @ApiModelProperty(value = "联系地址")
    @Excel(name = "联系地址")
    private String address;

    @ApiModelProperty(value = "所属部门")
    @Excel(name = "所属部门")
    private Integer departmentId;

    @ApiModelProperty(value = "职称ID")
    @Excel(name = "职称ID")
    private Integer jobLevelId;

    @ApiModelProperty(value = "职位ID")
    @Excel(name = "职位ID")
    private Integer posId;

    @ApiModelProperty(value = "聘用形式")
    @Excel(name = "聘用形式")
    private String engageForm;

    @ApiModelProperty(value = "最高学历")
    @Excel(name = "最高学历")
    private String tiptopDegree;

    @ApiModelProperty(value = "所属专业")
    @Excel(name = "所属专业")
    private String specialty;

    @ApiModelProperty(value = "毕业院校")
    @Excel(name = "毕业院校")
    private String school;

    @ApiModelProperty(value = "入职日期")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "Asia/Shanghai")
    @Excel(name = "入职日期",width = 20)
    private LocalDate beginDate;

    @ApiModelProperty(value = "在职状态")
    @Excel(name = "在职状态")
    private String workState;

    @ApiModelProperty(value = "工号")
    @Excel(name = "工号")
    private String workID;

    @ApiModelProperty(value = "合同期限")
    @Excel(name = "合同期限",suffix = "年"/*给这一列的每个单元格中加单位*/)
    private Double contractTerm;

    @ApiModelProperty(value = "转正日期")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "Asia/Shanghai")  //所有都是返回给前端的一个格式化
    @Excel(name = "转正日期")
    private LocalDate conversionTime;

    @ApiModelProperty(value = "离职日期")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "Asia/Shanghai")
    @Excel(name = "离职日期")
    private LocalDate notWorkDate;

    @ApiModelProperty(value = "合同起始日期")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "Asia/Shanghai")
    @Excel(name = "合同起始日期")
    private LocalDate beginContract;

    @ApiModelProperty(value = "合同终止日期")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "Asia/Shanghai")
    @Excel(name = "合同终止日期")
    private LocalDate endContract;

    @ApiModelProperty(value = "工龄")
    @Excel(name = "工龄")
    private Integer workAge;

    @ApiModelProperty(value = "工资账套ID")
    @Excel(name = "工资账号ID")
    private Integer salaryId;


    //以下都是多表查询的内容
    @ApiModelProperty(value = "民族")
    @TableField(exist = false)
    @ExcelEntity(name = "民族")/*因为这里是id所以要在Nation中也加上@Excel 下面的都是*/
    private Nation nation;

    @ApiModelProperty(value = "政治面貌")
    @TableField(exist = false)
    @ExcelEntity(name = "政治面貌") //这些都是表示有实体类对象作为一个主体
    private PoliticsStatus politicsStatus;

    @ApiModelProperty(value = "部门")
    @TableField(exist = false)
    @ExcelEntity(name = "部门")
    private Department department;

    @ApiModelProperty(value = "职称")
    @TableField(exist = false)
    @ExcelEntity(name = "职称")
    private Joblevel joblevel;

    @ApiModelProperty(value = "职位")
    @TableField(exist = false)
    @ExcelEntity(name = "职位")
    private Position position;

    //因为在获取所有员工账套的敌后需要查询工资账套所以在这里要引入，工资账套
    @ApiModelProperty(value = "工资账套")
    @TableField(exist = false)
    private Salary salary;

}
