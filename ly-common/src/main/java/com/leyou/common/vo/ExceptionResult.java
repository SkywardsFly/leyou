package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;

@Data
public class ExceptionResult {

    private int status;
    private String messge;
    private Long timestamp;

    public ExceptionResult(ExceptionEnum em){
        this.status = em.getCode();
        this.messge = em.getMsg();
        this.timestamp = System.currentTimeMillis();
    }
}
