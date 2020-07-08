package com.apollo.flashsale.service;

import com.apollo.flashsale.dao.OrderDao;
import com.apollo.flashsale.domain.FlashSaleOrder;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.domain.OrderInfo;
import com.apollo.flashsale.redis.key.impl.OrderKey;
import com.apollo.flashsale.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class OrderService {

    @Resource
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    public FlashSaleOrder getFlashSaleOrderByUserIdGoodsId(Long userId, long goodsId) {
        // return orderDao.getFlashSaleOrderByUserIdGoodsId(id, goodsId);
        return redisService.get(OrderKey.getFlashSaleOrderKey, "" + userId + "_" + goodsId, FlashSaleOrder.class);
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }


    @Transactional
    public OrderInfo createOrder(FlashSaleUser fsUser, GoodsVo goods) {
        // 1.生成订单, 填入信息
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(fsUser.getId());
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsPrice(goods.getFlashSalePrice());
        orderInfo.setStatus(0);
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setOrderChannel(1);

        // 2.存入数据库
        // [id自增赋值到哪里? => orderInfo.id中]
        orderDao.insert(orderInfo);

        // 3.生成秒杀订单, 并存入数据库
        FlashSaleOrder fsOrder = new FlashSaleOrder();
        fsOrder.setUserId(fsUser.getId());
        fsOrder.setOrderId(orderInfo.getId());
        fsOrder.setGoodsId(goods.getId());

        orderDao.insertFlashSaleOrder(fsOrder);
        redisService.set(OrderKey.getFlashSaleOrderKey, "" + fsUser.getId() + "_" + goods.getId(), fsOrder);

        return orderInfo;
    }

    public void deleteOrders() {
        orderDao.deleteOrders();
        orderDao.deleteFlashSaleOrders();
    }
}
