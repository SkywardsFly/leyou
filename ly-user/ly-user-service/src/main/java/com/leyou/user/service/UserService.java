package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private static final String KEY_PREFIX = "sms:verify:phone";

    public Boolean checkData(String data, Integer type) {
        User record = new User();
        switch (type) {
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
               throw new LyExcetion(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return userMapper.selectCount(record) == 0;
    }

    public void sendVerifyCode(String phone) {
        //生成key
        String key = KEY_PREFIX + phone;
        //生成验证码
        String code = NumberUtils.generateCode(6);
        Map<String, String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",msg);
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //保存验证码
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
    }

    public void register(User user, String code) {

        String key = KEY_PREFIX + user.getPhone();
        //从数据库取出验证码
        String getcode = redisTemplate.opsForValue().get(key);
        //校验验证码是否正确
        if (!StringUtils.equals(code, getcode)){
            //验证码错误
            throw new LyExcetion(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        user.setId(null);
        user.setCreated(new Date());
        //生成盐
        String salt = CodeUtils.generateSalt();
        user.setSalt(salt);
        //生成密码
        String md5pwd = CodeUtils.md5Hex(user.getPassword(),user.getSalt());
        user.setPassword(md5pwd);
        //数据添加完成

        //保存
        int count = userMapper.insert(user);
        if (count != 1){
            throw new LyExcetion(ExceptionEnum.INVALID_PARAM);
        }

        redisTemplate.delete(key);
    }

    public User queryUser(String username, String password) {

        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);

        //校验用户名
        if (user == null){
            throw new LyExcetion(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        //校验密码
        if (StringUtils.equals(user.getPassword(),CodeUtils.md5Hex(password,user.getSalt()))){
            throw new LyExcetion(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //用户名密码正确
        return user;
    }
}