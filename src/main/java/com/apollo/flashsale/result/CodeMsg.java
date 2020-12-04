package com.apollo.flashsale.result;

import lombok.Getter;

/**
 *  两部分构成, 状态码, 状态描述
 *
 */
@Getter
public class CodeMsg {
    // 代码
    private int code;
    // 信息
    private String msg;

    private CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    // 通用异常
    public static final CodeMsg SUCCESS = new CodeMsg(0, "success");
    public static final CodeMsg SERVER_ERROR = new CodeMsg(5001000, "服务器异常");
    public static final CodeMsg BIND_ERROR = new CodeMsg(500101, "参数校验异常,%s");
    public static final CodeMsg REQUEST_ILLEGAL_VERIFY_CODE = new CodeMsg(500102, "图片验证码输入错误!");
    public static final CodeMsg REQUEST_ILLEGAL = new CodeMsg(500103, "请求非法");
    public static final CodeMsg ACCESS_LIMIT_REACHED = new CodeMsg(500104, "访问太频繁");
    //登录模块 5002XX
    public static final CodeMsg SESSION_ERROR = new CodeMsg(500210, "Session不存在或者已经失效");
    public static final CodeMsg PASSWORD_EMPTY = new CodeMsg(500211, "登陆密码不能为空");
    public static final CodeMsg MOBILE_EMPTY = new CodeMsg(500212, "手机号不能为空");
    public static final CodeMsg MOBILE_ERROR = new CodeMsg(500213, "手机号格式错误");
    public static final CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500214, "手机号不存在");
    public static final CodeMsg PASSWORD_ERROR = new CodeMsg(500215, "密码错误");
    //商品模块 5003XX

    //订单模块 5004XX
    public static final CodeMsg ORDER_NOT_EXIST = new CodeMsg(500400, "订单不存在");

    //秒杀模块 5005XX
    public static final CodeMsg FLASH_SALE_OVER = new CodeMsg(500500, "商品已经秒杀完毕");
    public static final CodeMsg REPEAT_FLASH_SALE = new CodeMsg(500501, "不能重复秒杀");
    public static final CodeMsg FLASH_SALE_FAIL = new CodeMsg(500502, "秒杀失败");


    // 自定义异常


    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        String message = String.format(this.msg, args);
        return new CodeMsg(code, message);
    }
}
