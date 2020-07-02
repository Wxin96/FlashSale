package com.apollo.flashsale.service;

import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.domain.OrderInfo;
import com.apollo.flashsale.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlashSaleService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Transactional
    public OrderInfo flashSale(FlashSaleUser user, GoodsVo goods) {
        // 减库存, 下订单, 写入秒杀订单
        goodsService.reduceStock(goods);

        return orderService.createOrder(user, goods);
    }
}