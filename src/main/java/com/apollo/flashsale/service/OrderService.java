package com.apollo.flashsale.service;

import com.apollo.flashsale.dao.OrderDao;
import com.apollo.flashsale.domain.FlashSaleOrder;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.domain.OrderInfo;
import com.apollo.flashsale.vo.GoodsVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class OrderService {

    @Resource
    OrderDao orderDao;

    public FlashSaleOrder getFlashSaleOrderByUserIdGoodsId(Long id, long goodsId) {
        return orderDao.getFlashSaleOrderByUserIdGoodsId(id, goodsId);
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
        long orderId = orderDao.insert(orderInfo);

        // 3.生成秒杀订单, 并存入数据库
        FlashSaleOrder fsOrder = new FlashSaleOrder();
        fsOrder.setUserId(fsUser.getId());
        fsOrder.setOrderId(orderId);
        fsOrder.setGoodsId(goods.getId());

        orderDao.insertFlashSaleOrder(fsOrder);

        return orderInfo;
    }
}
