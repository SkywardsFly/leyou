package com.leyou.auth.client;

import com.leyou.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author
 * @date 2018/10/1
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {
}
