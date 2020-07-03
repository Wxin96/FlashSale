package com.apollo.flashsale.exception;

import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *  异常处理类
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e) {
        // e.printStackTrace();
        log.error("出现异常", e);
        if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            log.error(ex.getCodeMsg().getMsg());
            return Result.error(ex.getCodeMsg());
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);
            String message = error.getDefaultMessage();
            CodeMsg bindError = CodeMsg.BIND_ERROR.fillArgs(message);
            log.error(bindError.getMsg());
            return Result.error(bindError);
        } else {
            CodeMsg serverError = CodeMsg.SERVER_ERROR;
            log.error(serverError.getMsg());
            return Result.error(serverError);
        }
    }

}
