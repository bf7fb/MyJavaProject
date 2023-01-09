package com.wangzhen.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author wz
 * @ClassName GlobalExceptionHandler
 * @date 2023/1/5 19:09
 * @Description 自定义异常类 controlleradvice处理restcontroller和controller注解类的所有异常
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 捕获重复错误
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleException(SQLIntegrityConstraintViolationException ex){
        if (ex.getMessage().contains("Duplicate entry")){
            String[] s = ex.getMessage().split(" ");
            String msg = s[2] + "已存在~请重试";
            return Result.error(msg);
        }
        return Result.error("未知错误");
    }

    /**
     * 捕获菜品或套餐删除错误
     * @param ex
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public Result<String> handleException(CustomException ex){
        return Result.error(ex.getMessage());
    }
}
