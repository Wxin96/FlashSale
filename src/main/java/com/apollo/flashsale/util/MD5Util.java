package com.apollo.flashsale.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {

    private static final String salt = "1a2b3c4d";

    public static String md5(String src) {
        return DigestUtils.md5Hex(src);
    }

    /**
     * 将明文密码第一次MD5处理, 转换为表单密码
     * @param inputPass 明文密码
     * @return  表单密码
     */
    public static String inputPassToFormPass(String inputPass) {
        // 1.重组字符串
        // 开头""+细节问题
        // d3b1294a61a07da9b49b6e22b2cbd7f9
        String str = "" + salt.charAt(0) + salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);

        return md5(str);
    }

    /**
     *  将表单密码第二次MD5处理, 转换为数据库密码
     * @param formPass 表单密码
     * @param salt  组合数
     * @return  数据库密码
     */
    public static String formPassToDBPass(String formPass, String salt) {
        //b7797cce01b4b131b433b6acf4add449
        String str = "" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     * 将明文密码进行两次MD处理, 转化为数据库密码
     * @param inputPass 明文密码
     * @param salt  组合数
     * @return  数据库密码
     */
    public static String inputPassToDBPass(String inputPass, String salt) {
        String formPass = inputPassToFormPass(inputPass);
        return formPassToDBPass(formPass, salt);
    }

}
