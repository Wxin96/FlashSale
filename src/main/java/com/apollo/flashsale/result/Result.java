package com.apollo.flashsale.result;

import lombok.Getter;

@Getter
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    // 默认成功构造器
    private Result(T data) {
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    // 失败构造器
    private Result(CodeMsg codeMsg) {
        // 避免空指针异常
        if (codeMsg == null) {
            return;
        }
        this.code = codeMsg.getCode();
        this.msg = codeMsg.getMsg();
    }

    /**
     * 成功时, 调用
     * @param data 数据
     * @param <T> data的泛型
     * @return 结果信息封装
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(data);
    }

    /**
     * 失败时, 调用
     * @param codeMsg 错误类型
     * @param <T>   数据泛型
     * @return  结果信息封装
     */
    public static <T> Result<T> error(CodeMsg codeMsg) {
        return new Result<>(codeMsg);
    }

}
