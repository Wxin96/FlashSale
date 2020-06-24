package com.apollo.flashsale.controller;

import com.apollo.flashsale.domain.User;
import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.result.Result;
import com.apollo.flashsale.result.key.impl.UserKey;
import com.apollo.flashsale.service.RedisService;
import com.apollo.flashsale.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return "Hello";
    }

    //1.rest.api json输出 2.页面
    @GetMapping("/hello")
    @ResponseBody
    public Result<String> hello() {
        return Result.success("hello,imooc");
    }

    @RequestMapping("/helloError")
    public @ResponseBody Result<String> helloError() {
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model) {
        model.addAttribute("name", "Apollo");
        return "hello";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User user = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User u1 = new User();
        u1.setId(1);
        u1.setName("apollo");
        boolean flag = redisService.set(UserKey.getById, "" + 1, u1);
        return Result.success(flag);
    }
}
