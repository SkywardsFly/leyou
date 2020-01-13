package com.leyou.sms.utils;


import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsUtils {

    @Autowired
    private SmsProperties prop;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "sms:phone";
    private static final long SMS_MIN_INTERVAL_IN_MILLIS = 60000;

    public void sendSms(String phoneNumer,String signName,String templateCode,String templateParam){
        String key = KEY_PREFIX + phoneNumer;
        //读取时间
        String lastTime = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(lastTime)){
            Long aLong = Long.valueOf(lastTime);
            if (System.currentTimeMillis() - aLong < SMS_MIN_INTERVAL_IN_MILLIS){
                log.info("【短信服务】发送短信频率过高，被拦截，手机号码: {}", phoneNumer);
                return;
            }
        }

        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", prop.getAccessKeyId(), prop.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phoneNumer);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", templateParam);
        CommonResponse response = null;
        try {
            response = client.getCommonResponse(request);
            //发送短信，存入Redis，指定生命周期为1分钟
            redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()),1, TimeUnit.MINUTES);
            //记录日志
            log.info("[短信服务]手机号：{}",phoneNumer);
        } catch (Exception e) {
            log.error("【短信服务】发送短信异常，手机号码: {}", phoneNumer,response.getData());
            e.printStackTrace();
        }
    }
}
