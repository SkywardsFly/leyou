package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class PayHelper {

    private WXPay wxPay;

    private String notifyUrl;

    @Autowired
    private PayConfig payConfig;

    @Autowired
    private RestTemplate restTemplate;


//    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper statusMapper;


//    private PayLogMapper payLogMapper;

    public PayHelper(PayConfig payConfig) {
        wxPay = new WXPay(payConfig);
        this.notifyUrl = payConfig.getNotifyUrl();
    }

    public String createPayUrl(Long orderId, Long totalPay, String desc) {

        //从缓存中取出支付连接
//        String key = "order:pay:url:" + orderId;
//        try {
//            String url = redisTemplate.opsForValue().get(key);
//            if (StringUtils.isNotBlank(url)) {
//                return url;
//            }
//        } catch (Exception e) {
//            log.error("查询缓存付款链接异常，订单号：{}", orderId, e);
//        }

        try {
            Map<String, String> data = new HashMap<>();
            //描述
            data.put("body", desc);
            //订单号
            data.put("out_trade_no", orderId.toString());
            //货币（默认就是人民币）
            //data.put("fee_type", "CNY");
            //总金额
            data.put("total_fee", totalPay.toString());
            //调用微信支付的终端ip
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址
            data.put("notify_url", notifyUrl);
            //交易类型为扫码支付
            data.put("trade_type", "NATIVE");

            Map<String, String> result = wxPay.unifiedOrder(data);
//            //填充请求参数并签名，并把请求参数处理成字节
//            byte[] request = WXPayUtil.mapToXml(wxPay.fillRequestData(data)).getBytes("UTF-8");
//
//            //发起请求并获取响应结果
//            String xml = restTemplate.postForObject(WXPayConstants.UNIFIEDORDER_URL, request, String.class);
//
//            //将结果处理成map
//            Map<String, String> result = WXPayUtil.xmlToMap(xml);
            //判断通信标识
            isSuccess(result);
            //检验签名
            isSignatureValid(result);

            //下单成功，获取支付连接
            String url = result.get("code_url");

//            //将连接缓存到Redis中，失效时间2小时
//            try {
//                this.redisTemplate.opsForValue().set(key, url, 2, TimeUnit.HOURS);
//            } catch (Exception e) {
//                log.error("【微信下单】缓存付款链接异常,订单编号：{}", orderId, e);
//            }
            return url;
        } catch (Exception e) {
            log.error("【微信下单】创建预交易订单异常", e);
            return null;
        }
    }

    /**
     *  //判断通信标识
     * @param result
     */
    private void isSuccess(Map<String, String> result) {
        //通信失败
        if (WXPayConstants.FAIL.equals(result.get("return_code"))) {
            log.error("【微信下单】与微信通信失败，失败信息：{}", result.get("return_msg"));
            throw new LyExcetion(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }

        //下单失败
        if (WXPayConstants.FAIL.equals(result.get("result_code"))) {
            log.error("【微信下单】创建预交易订单失败，错误码：{}，错误信息：{}",
                    result.get("err_code"), result.get("err_code_des"));
            throw new LyExcetion(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }

    /**
     * 检验签名
     *
     * @param result
     */
    private void isSignatureValid(Map<String, String> result) {
        try {
            boolean boo1 = WXPayUtil.isSignatureValid(result, payConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
            boolean boo2 = WXPayUtil.isSignatureValid(result, payConfig.getKey(), WXPayConstants.SignType.MD5);

            if (!boo1 && !boo2) {
                throw new LyExcetion(ExceptionEnum.WX_PAY_SIGN_INVALID);
            }
        } catch (Exception e) {
            log.error("【微信支付】检验签名失败，数据：{}", result);
            throw new LyExcetion(ExceptionEnum.WX_PAY_SIGN_INVALID);
        }
    }

    /**
     * 处理回调结果
     *
     * @param result
     */
    public void handleNotify(Map<String, String> result) {
        isSuccess(result);
        //检验签名
        isSignatureValid(result);

        //检验金额
        //解析数据
        String totalFeeStr = result.get("total_fee");  //订单金额
        String outTradeNo = result.get("out_trade_no");  //订单编号
//        String transactionId = result.get("transaction_id");  //商户订单号
//        String bankType = result.get("bank_type");  //银行类型
        if (StringUtils.isBlank(totalFeeStr) || StringUtils.isBlank(outTradeNo)) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyExcetion(ExceptionEnum.WX_PAY_NOTIFY_PARAM_ERROR);
        }

        //查询订单
        Long totalFee = Long.valueOf(totalFeeStr);
        Long orderId = Long.valueOf(outTradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        //验证回调数据时，支付金额使用1分进行验证，后续使用实际支付金额验证
        if (/*order.getActualPay()*/1 != totalFee) {
            log.error("【微信支付回调】支付回调返回数据不正确");
            throw new LyExcetion(ExceptionEnum.WX_PAY_NOTIFY_PARAM_ERROR);

        }

        //修改订单状态
        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAYED.value());
        status.setOrderId(orderId);
        status.setPaymentTime(new Date());
        int count = statusMapper.updateByPrimaryKey(status);
        if (count != 1){
            throw new LyExcetion(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
        log.info("【订单回调】订单支付成功！ 订单编号：{}",orderId);

    }


    /**
     * 查询订单支付状态
     *
     * @param orderId
     * @return
     */
    public PayState queryPayState(Long orderId) {
        Map<String, String> data = new HashMap<>();
        data.put("out_trade_no", orderId.toString());
        try {
            Map<String, String> result = wxPay.orderQuery(data);
            if (CollectionUtils.isEmpty(result) || WXPayConstants.FAIL.equals(result.get("return_code"))) {
                //未查询到结果，或连接失败
                log.error("【支付状态查询】链接微信服务失败，订单编号：{}", orderId);
                return PayState.NOT_PAY;
            }

            //查询失败
            if (WXPayConstants.FAIL.equals(result.get("result_code"))) {
                log.error("【支付状态查询】查询微信订单支付结果失败，错误码：{}, 订单编号：{}", result.get("result_code"), orderId);
                return PayState.NOT_PAY;
            }

            //检验签名
            isSignatureValid(result);

            //查询支付状态
            String state = result.get("trade_state");
            if (StringUtils.equals("SUCCESS", state)) {
                //支付成功, 修改支付状态等信息
                handleNotify(result);
                return PayState.SUCCESS;
            } else if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
                //未支付成功
                return PayState.NOT_PAY;
            } else {
                //其他返回付款失败
                return PayState.FAIL;
            }
        } catch (Exception e) {
            log.error("查询订单支付状态异常", e);
            return PayState.NOT_PAY;
        }

    }
}