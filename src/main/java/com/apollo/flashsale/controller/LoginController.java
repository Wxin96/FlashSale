package com.apollo.flashsale.controller;

import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.result.Result;
import com.apollo.flashsale.service.FlashSaleService;
import com.apollo.flashsale.util.ValidatorUtil;
import com.apollo.flashsale.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    FlashSaleService flashSaleService;

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<CodeMsg> doLogin(LoginVo loginVo) {
        logger.info(loginVo.toString());
        // 参数校验
        String mobile = loginVo.getMobile();
        String passInput = loginVo.getPassword();
        // 密码不能为空
        if (StringUtils.isEmpty(passInput)) {
            return Result.error(CodeMsg.PASSWORD_EMPTY);
        }
        // 手机号不能为空
        if (StringUtils.isEmpty(mobile)) {
            return Result.error(CodeMsg.MOBILE_EMPTY);
        }
        // 手机号格式是否正确
        if (!ValidatorUtil.isMobile(mobile)) {
            return Result.error(CodeMsg.MOBILE_ERROR);
        }
        // 登录
        CodeMsg codeMsg = flashSaleService.login(loginVo);
        // 判断
        if (codeMsg.getCode() == 0) {
            return Result.success(codeMsg);
        } else {
            return Result.error(codeMsg);
        }

    }
}
