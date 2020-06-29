package com.apollo.flashsale.service;

import com.apollo.flashsale.dao.FlashSaleUserDao;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.util.MD5Util;
import com.apollo.flashsale.vo.LoginVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FlashSaleService {

    @Resource
    FlashSaleUserDao flashSaleUserDao;

    public FlashSaleUser getById(long id) {
        return flashSaleUserDao.getById(id);
    }

    public CodeMsg login(LoginVo loginVo) {
        if (loginVo == null) {
            return CodeMsg.SERVER_ERROR;
        }
        String mobile = loginVo.getMobile();
        String formPassword = loginVo.getPassword();
        // 判断手机号是否存在
        FlashSaleUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            return CodeMsg.MOBILE_NOT_EXIST;
        }
        // 验证密码
        String dbPassword = user.getPassword();
        String saltDB = user.getSalt();
        String calcPassword = MD5Util.formPassToDBPass(formPassword, saltDB);
        if (!calcPassword.equals(dbPassword)) {
            return CodeMsg.PASSWORD_ERROR;
        }

        return CodeMsg.SUCCESS;
    }

}
