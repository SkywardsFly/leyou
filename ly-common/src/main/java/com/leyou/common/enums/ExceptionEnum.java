package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter //get方法
@AllArgsConstructor // 带参构造方法
@NoArgsConstructor  //空参构造方法
public enum ExceptionEnum {

    CATEGORY_NOT_FOUND(404,"商品分类没找到"),
    SPEC_GROUP_NOT_FOUND(404,"商品规格组没找到"),
    SPEC_PARAM_NOT_FOUND(404,"商品规格参数没找到"),
    GOODS_DETAIL_NOT_FOUND(404,"商品详情不存在"),
    GOODS_SKU_NOT_FOUND(404,"商品sku不存在"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存不存在"),
    GOODS_UPDATE_ERROR(500,"商品更新失败"),
    GOODS_ID_CANNOT_NULL(500,"商品id不能为空"),
    GOODS_SAVE_BAD(500,"添加商品失败"),
    BRAND_NOT_FOUND(404,"品牌没找到"),
    GOODS_NOT_FOUND(404,"商品没找到"),
    BRAND_BAD_CREATED(500,"品牌添加失败"),
    UPLOAD_BAD_FILE(500,"上传文件失败"),
    INVALID_FILE_TYPE(400,"不支持的文件类型"),
    INVALID_USER_DATA_TYPE(400,"用户数据类型无效"),
    INVALID_VERIFY_CODE(404,"验证码校验错误"),
    INVALID_PARAM(400,"无效数据类型"),
    INVALID_USERNAME_PASSWORD(400,"无效用户名或密码"),
    USERNAME_OR_PASSWORD_ERROR(400,"用户名或密码错误"),
    CART_NOT_FOUND(404,"购物车找不到"),
    ORDER_CREATE_BAD(500,"订单创建失败"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    ORDER_NOT_FOUND(404,"订单不存在"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情不存在"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态不存在"),
    WX_PAY_SIGN_INVALID(500,"签名无效"),
    ORDER_STATUS_ERROR(500,"订单状态异常"),
    WX_PAY_ORDER_FAIL(500,"微信支付异常下单失败"),
    WX_PAY_NOTIFY_PARAM_ERROR(500,"微信支付返回数据错误"),
    UPDATE_ORDER_STATUS_ERROR(500,"更新订单状态错误"),
    ;
    private int code;
    private String msg;

}
