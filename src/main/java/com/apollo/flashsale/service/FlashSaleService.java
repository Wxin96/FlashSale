package com.apollo.flashsale.service;

import com.apollo.flashsale.domain.FlashSaleOrder;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.domain.OrderInfo;
import com.apollo.flashsale.redis.key.impl.FlashSaleKey;
import com.apollo.flashsale.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class FlashSaleService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Transactional
    public OrderInfo flashSale(FlashSaleUser user, GoodsVo goods) {
        // 减库存, 下订单, 写入秒杀订单
        if (goodsService.reduceStock(goods)) {
            log.info("用户" + user.getId() + "秒杀到商品" + goods.getId());
            return orderService.createOrder(user, goods);
        } else {
            log.warn("商品已经秒杀完毕, 用户" + user.getId() + "秒杀失败!");
            setGoodsOver(goods.getId());
            return null;
            // throw new GlobalException(CodeMsg.FLASH_SALE_OVER);
        }
    }

    public long getFlashSaleResult(long userId, long goodsId) {
        // 1.获取秒杀订单
        FlashSaleOrder fsOrder = orderService.getFlashSaleOrderByUserIdGoodsId(userId, goodsId);
        // 2.判断秒杀情况
        // a.秒杀成功
        if (fsOrder != null) {
            return fsOrder.getOrderId();
        } else {
            // b.秒杀失败
            if (getGoodsOver(goodsId)) {
                return -1;
            } else {
                // c.等待结果
                return 0;
            }
        }
    }

    private void setGoodsOver(long goodsId) {
        redisService.set(FlashSaleKey.isGoodsOver, "" + goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(FlashSaleKey.isGoodsOver, "" + goodsId);
    }


    public void reset(List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }
}
