package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice //控制器增强
public class LyExceptionHandler {

    @ExceptionHandler(LyExcetion.class)
    public ExceptionResult handlerException(LyExcetion e){
        ExceptionEnum em = e.getExceptionEnum();
        return new ExceptionResult(em);
    }
}
