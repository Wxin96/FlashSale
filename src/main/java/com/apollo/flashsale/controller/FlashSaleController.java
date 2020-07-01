package com.apollo.flashsale.controller;

import com.apollo.flashsale.domain.FlashSaleOrder;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.domain.OrderInfo;
import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.service.*;
import com.apollo.flashsale.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @RequestMapping("/do_sell")
    public String list(Model model, FlashSaleUser user,
                       @RequestParam("goodsId") long goodsId) {
        // 1.判断用户是否登录
        model.addAttribute("user", user);
        if (user == null) {
            return "login";
        }
        // 2.判断库存
        GoodsVo goods = goodsService.getGoodsVoGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            model.addAttribute("errMsg", CodeMsg.FLASH_SALE_OVER.getMsg());
            return "flashSale_fail";
        }

        // 3.判断是否已经秒杀到
        FlashSaleOrder fsOrder = orderService.getFlashSaleOrderByUserIdGoodsId(user.getId(), goodsId);
        if (fsOrder != null) {
            model.addAttribute("errMsg", CodeMsg.REPEAT_FLASH_SALE.getMsg());
            return "flashSale_fail";
        }

        // 4.进行秒杀
        OrderInfo orderInfo = flashSaleService.flashSale(user, goods);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);

        return "order_detail";
    }


}
