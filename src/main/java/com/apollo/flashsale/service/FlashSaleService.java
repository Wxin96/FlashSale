package com.apollo.flashsale.service;

import com.apollo.flashsale.dao.FlashSaleUserDao;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.exception.GlobalException;
import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.result.key.impl.FlashSaleUserKey;
import com.apollo.flashsale.util.MD5Util;
import com.apollo.flashsale.util.UUIDUtil;
import com.apollo.flashsale.vo.LoginVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
@Slf4j
public class FlashSaleService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Resource
    FlashSaleUserDao flashSaleUserDao;

    @Autowired
    RedisService redisService;

    public FlashSaleUser getById(long id) {
        return flashSaleUserDao.getById(id);
    }

    // 根据Cookie中的token信息, 获取FlashSaleUser对象
    public FlashSaleUser getByToken(HttpServletResponse response, String token) {
        // 0.预处理
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        // 1.查询
        FlashSaleUser user = redisService.get(FlashSaleUserKey.token, token, FlashSaleUser.class);

        // 2.延长有效期
        if (user != null) {
            addCookie(response, token, user);
            log.info(user.toString());
        } else {
            log.info("user为空");
        }


        return user;
    }


    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPassword = loginVo.getPassword();
        // 判断手机号是否存在
        FlashSaleUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        // 验证密码
        String dbPassword = user.getPassword();
        String saltDB = user.getSalt();
        String calcPassword = MD5Util.formPassToDBPass(formPassword, saltDB);
        if (!calcPassword.equals(dbPassword)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        // 生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);

        return true;
    }


    // 生成Cookie
    private void addCookie(HttpServletResponse response, String token, FlashSaleUser user) {
        // 1.将User内容存入redis缓存
        redisService.set(FlashSaleUserKey.token, token, user);
        // 2.生成Cookie
        // key: token, value: uuid
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        // 设置Cookie存储时间
        cookie.setMaxAge(FlashSaleUserKey.token.expireSeconds());
        // 设置同一服务器, 不同项目之间皆可共享
        // 默认目录: 虚拟/项目 目录
        cookie.setPath("/");

        response.addCookie(cookie);
    }

}

