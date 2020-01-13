package com.leyou.order.web;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("notify")
@Slf4j
public class NotifyController {

    @Autowired
    private OrderService orderService;

    @PostMapping(value = "pay",produces = "application/xml")
    public ResponseEntity<String> payNotify(@RequestBody Map<String, String> result) {
        //处理回调结果
        orderService.handleNotify(result);
        // 没有异常，则返回成功
        String msg = "<xml>\n" +
                "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                "</xml>";
        return ResponseEntity.ok(msg);

    }
}