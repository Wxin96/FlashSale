package com.apollo.flashsale;

import com.apollo.flashsale.result.key.impl.FlashSaleUserKey;
import com.apollo.flashsale.util.MD5Util;
import com.apollo.flashsale.util.UUIDUtil;
import com.apollo.flashsale.util.ValidatorUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FlashSaleApplicationTests {

    @Test
    void contextLoads() {

    }

    @Test
    void testMD5Util() {
        String formPass = MD5Util.inputPassToFormPass("123456");
        System.out.println(formPass);
    }

    @Test
    void testMD5Util02() {
        String formPass = MD5Util.inputPassToFormPass("123456");
        String dBPass = MD5Util.formPassToDBPass(formPass, "1a2b3c4d");
        System.out.println(dBPass);
    }

    @Test
    void testValidatorUtil() {
        System.out.println(ValidatorUtil.isMobile("13476489762"));
        System.out.println(ValidatorUtil.isMobile("23476489762"));
    }

    @Test
    void testUUIDUtil() {
        System.out.println(UUIDUtil.uuid());
    }

    @Test
    void testFlashUserKey() {
        System.out.println(FlashSaleUserKey.token.getPrefix());
    }
}
