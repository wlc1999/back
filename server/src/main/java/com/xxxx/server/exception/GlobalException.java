package com.xxxx.server.exception;

import com.xxxx.server.pojo.RespBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@RestControllerAdvice  //表示是一个控制器的增强类  如果发生异常或符合自定义的拦截异常就会被拦截
public class GlobalException {
    @ExceptionHandler(SQLException.class)
    public RespBean mySqlException(SQLException e){
        if (e instanceof SQLIntegrityConstraintViolationException){  //如果异常属于SQLIntegrityConstraintViolationException异常
            return RespBean.error("改数据有关联数据，操作失败");
        }
        return RespBean.error("数据库错误，操作失败");
    }
}
