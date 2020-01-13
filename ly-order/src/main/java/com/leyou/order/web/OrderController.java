package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    @GetMapping("{id}")
    public ResponseEntity<Order> queryById(@PathVariable("id") Long orderId){
        return ResponseEntity.ok(orderService.queryById(orderId));
    }

    @GetMapping("/url/{id}")
    public ResponseEntity<String> createUrl(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.createUrl(id));
    }

    @GetMapping("/state/{id}")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("id")Long orderId){
        return ResponseEntity.ok(orderService.queryOrderState(orderId).getValue());
    }
}
