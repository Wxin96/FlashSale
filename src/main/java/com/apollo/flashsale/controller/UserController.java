package com.apollo.flashsale.controller;

import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.result.Result;
import com.apollo.flashsale.service.FlashSaleUserService;
import com.apollo.flashsale.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    FlashSaleUserService userService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/info")
    @ResponseBody
    public Result<FlashSaleUser> info(FlashSaleUser user) {
        // log.info(user.toString());
        return Result.success(user);
    }

}
