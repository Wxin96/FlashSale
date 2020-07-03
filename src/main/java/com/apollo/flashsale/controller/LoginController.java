package com.apollo.flashsale.controller;

import com.apollo.flashsale.result.Result;
import com.apollo.flashsale.service.FlashSaleUserService;
import com.apollo.flashsale.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    FlashSaleUserService flashSaleUserService;

    @RequestMapping("/to_login")
    public String toLogin() {
        logger.trace("用户登录.");
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        logger.trace("用户登录提交信息:" + loginVo.toString());
        // 登录(如果有异常会走异常拦截的线)
        String token = flashSaleUserService.login(response, loginVo);
        // 判断
        return Result.success(token);
    }
}
