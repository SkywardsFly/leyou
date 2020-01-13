package com.leyou.search.mq;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {

    @Autowired
    private SearchService searchService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.leyou.item.queue", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item-update","item-insert"}
    ))
    public void listenCreate(Long spuId){
        if (spuId == null){
            return;
        }
        //处理消息 对索引库新增和修改
        searchService.createIndex(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.leyou.delete.exchange", durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item-delete"}
    ))
    public void listenDelete(Long spuId){
        if (spuId == null){
            return;
        }
        //处理消息 对索引库删除
        searchService.deleteIndex(spuId);
    }
}
