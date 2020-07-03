package com.apollo.flashsale.controller;

import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.domain.OrderInfo;
import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.result.Result;
import com.apollo.flashsale.service.GoodsService;
import com.apollo.flashsale.service.OrderService;
import com.apollo.flashsale.vo.GoodsVo;
import com.apollo.flashsale.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 订单控制器
 */
@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @GetMapping("/details")
    @ResponseBody
    public Result<OrderDetailVo> info(FlashSaleUser user, @RequestParam("orderId") long orderId) {
        // 1.登录拦截(可添加过滤器)
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 2.查询订单信息
        OrderInfo order = orderService.getOrderById(orderId);
        if (order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        // 3.查询物品信息
        GoodsVo goods = goodsService.getGoodsVoGoodsId(order.getGoodsId());
        // 4.封装订单信息
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoods(goods);
        orderDetailVo.setOrder(order);

        // 5.返回
        return Result.success(orderDetailVo);
    }

}
