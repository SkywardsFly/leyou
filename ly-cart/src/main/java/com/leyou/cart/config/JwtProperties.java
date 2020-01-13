package com.leyou.cart.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {


    private String pubKeyPath;

    private String cookieName;

    private PublicKey publicKey; //公钥


    @PostConstruct
    public void init() {
        try {
            //公钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("读取公钥失败",e);
            throw new RuntimeException();
        }
    }
}
