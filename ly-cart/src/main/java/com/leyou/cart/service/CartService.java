package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:uid";

    //已登录，加入购物车
    public void addCart(Cart cart) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        // Redis的key
        String key = KEY_PREFIX + user.getId();
        //记录num
        Integer num = cart.getNum();
        String hashkey = cart.getSkuId().toString();
        // 获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        //判断当前购物车商品是否存在
        if (hashOps.hasKey(hashkey)){
            // 存在，获取购物车数据
            String json = hashOps.get(hashkey).toString();
            cart = JsonUtils.parse(json, Cart.class);
            // 修改购物车数量
            cart.setNum(cart.getNum() + num);
        }
        // 将购物车数据写入redis
        hashOps.put(hashkey,JsonUtils.serialize(cart));
    }

    public List<Cart> queryCartList() {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();

        // 判断是否存在购物车
        String key = KEY_PREFIX + user.getId();
        if(!redisTemplate.hasKey(key)){
            // 不存在，直接返回
            throw new LyExcetion(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        List<Cart> carts = hashOps.values().stream().map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
        // 判断是否有数据
        if(CollectionUtils.isEmpty(carts)){
            return null;
        }
        // 查询购物车数据
        return carts;
    }

    public void updateNum(Long skuId, Integer num) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())){
            throw new LyExcetion(ExceptionEnum.CART_NOT_FOUND);
        }
        // 获取购物车
        String json = hashOps.get(skuId.toString()).toString();
        Cart cart = JsonUtils.parse(json, Cart.class);
        cart.setNum(num);
        // 写入购物车
        hashOps.put(skuId.toString(), JsonUtils.serialize(cart));
    }

    public void deleteCart(String skuId) {
        // 获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = KEY_PREFIX + user.getId();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        hashOps.delete(skuId);
    }
}
