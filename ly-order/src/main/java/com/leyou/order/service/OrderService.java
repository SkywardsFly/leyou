package com.leyou.order.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //1.新增订单
        Order order = new Order();
             //1.1 订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());

        //1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getName());
        order.setBuyerRate(false);

        //1.3 收货人地址
        AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());
        //1.4 金额
        //将cartdto 转为一个map,key是sku id val是num
        Map<Long, Integer> numMap = orderDTO.getCarts().stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //获取sku所有id
        Set<Long> ids = numMap.keySet();
        //根据id查询sku
        List<Sku> skuList = goodsClient.querySkuByIds(new ArrayList<>(ids));
        //准备OrderDetail集合
        List<OrderDetail> details = new ArrayList<>();

        long totalPay = 0L;
        for (Sku sku : skuList) {
            totalPay += sku.getPrice() * numMap.get(sku.getId());

            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());
            details.add(detail);
        }
        order.setTotalPay(totalPay);
        //实付金额 总金额+邮费-优惠
        order.setActualPay(totalPay - order.getPostFee() - 0);
        //1.5 order写入数据库、
        int count = orderMapper.insertSelective(order);
        if (count != 1){
            log.error("【创建订单】创建订单失败 OrderId",orderId);
            throw new LyExcetion(ExceptionEnum.ORDER_CREATE_BAD);
        }
        //2.新增订单详情
        count = orderDetailMapper.insertList(details);
        if (count != details.size()){
            log.error("【创建订单】创建订单失败 OrderId",orderId);
            throw new LyExcetion(ExceptionEnum.ORDER_CREATE_BAD);
        }
        //3.新增订单状态
        //填充orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(order.getCreateTime());

        //保存orderStatus
        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != details.size()){
            log.error("【创建订单】创建订单失败 OrderId",orderId);
            throw new LyExcetion(ExceptionEnum.ORDER_CREATE_BAD);
        }
        //减库存
        goodsClient.decreaseStock(orderDTO.getCarts());

        return orderId;
    }

    public Order queryById(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyExcetion(ExceptionEnum.ORDER_NOT_FOUND);
        }
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        if (CollectionUtils.isEmpty(orderDetails)){
            throw new LyExcetion(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(orderDetails);
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        if (orderStatus == null){
            throw new LyExcetion(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createUrl(Long id) {
        //查询订单
        Order order = queryById(id);
        //判断支付状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.INIT.value()){
            throw new LyExcetion(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //支付金额
        Long actualPay = order.getActualPay();
        //商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();
        return payHelper.createPayUrl(id,actualPay,desc);
    }

    public void handleNotify(Map<String, String> result) {
        payHelper.handleNotify(result);
    }

    public PayState queryOrderState(Long orderId) {
        return payHelper.queryPayState(orderId);
    }
}
