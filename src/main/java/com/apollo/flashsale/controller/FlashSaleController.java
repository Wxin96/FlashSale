package com.apollo.flashsale.controller;

import com.apollo.flashsale.domain.FlashSaleOrder;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.domain.OrderInfo;
import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.result.Result;
import com.apollo.flashsale.service.*;
import com.apollo.flashsale.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/sell")
public class FlashSaleController {

    @Autowired
    FlashSaleUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    FlashSaleService flashSaleService;

    /**
     *  第 5 章 2054 /sec
     * @param user 秒杀用户
     * @param goodsId 货物Id
     * @return  秒杀信息
     */
    @PostMapping("/do_sell")
    @ResponseBody
    public Result<OrderInfo> list(FlashSaleUser user, @RequestParam("goodsId") long goodsId) {
        // 1.判断用户是否登录
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 2.判断库存
        GoodsVo goods = goodsService.getGoodsVoGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return Result.error(CodeMsg.FLASH_SALE_OVER);
        }
        // 3.判断是否已经秒杀到
        FlashSaleOrder fsOrder = orderService.getFlashSaleOrderByUserIdGoodsId(user.getId(), goodsId);
        if (fsOrder != null) {
            return Result.error(CodeMsg.REPEAT_FLASH_SALE);
        }
        // 4.进行秒杀
        OrderInfo orderInfo = flashSaleService.flashSale(user, goods);

        return Result.success(orderInfo);
    }


}
