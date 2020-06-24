package com.apollo.flashsale.result;

import lombok.Getter;

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
    public static CodeMsg SUCCESS = new CodeMsg(0, "success");
    public static CodeMsg SERVER_ERROR = new CodeMsg(5001000, "服务器异常");

    //登录模块 5002XX

    //商品模块 5003XX

    //订单模块 5004XX

    //秒杀模块 5005XX

}
