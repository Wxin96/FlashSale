package com.apollo.flashsale.service;

import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.domain.OrderInfo;
import com.apollo.flashsale.exception.GlobalException;
import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class FlashSaleService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Transactional
    public OrderInfo flashSale(FlashSaleUser user, GoodsVo goods) {
        // 减库存, 下订单, 写入秒杀订单
        if (goodsService.reduceStock(goods)) {
            return orderService.createOrder(user, goods);
        } else {
            throw new GlobalException(CodeMsg.FLASH_SALE_OVER);
        }
    }
}
