package com.apollo.flashsale.rabbitmq;

import com.apollo.flashsale.config.MQConfig;
import com.apollo.flashsale.domain.FlashSaleOrder;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.rabbitmq.message.FlashSaleMessage;
import com.apollo.flashsale.service.FlashSaleService;
import com.apollo.flashsale.service.GoodsService;
import com.apollo.flashsale.service.OrderService;
import com.apollo.flashsale.service.RedisService;
import com.apollo.flashsale.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MQReceiver {

    @Autowired
    FlashSaleService flashSaleService;

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RabbitListener(queues = MQConfig.FLASH_SALE_QUEUE)
    public void receiveFlashSaleMsg(String message) {
        // 1.从队列中获取信息
        log.trace("MQReceive => receive  message:" + message);
        FlashSaleMessage fsMsg = RedisService.stringToBean(message, FlashSaleMessage.class);
        FlashSaleUser user = fsMsg.getUser();
        long goodsId = fsMsg.getGoodsId();

        // 2.查看库存, 判断是否进行秒杀
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        if (goods.getStockCount() <= 0) {
            log.debug("出队后, 判断秒杀商品" + goodsId + "为0, 已经秒杀完毕");
            return;
        }

        // 3.判断是否已经秒杀到
        FlashSaleOrder order = orderService.getFlashSaleOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            log.debug("出队后, 判断用户" +user.getId() + "已经秒杀到商品" + goodsId);
            return;
        }

        // 4.减库存, 下订单, 写入秒杀订单
        flashSaleService.flashSale(user, goods);
    }

    /*
    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message) {
        log.info("MQReceive => receive message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        log.info("MQReceive => receive topicQueue1 message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        log.info("MQReceive => receive topicQueue2 message:" + message);
    }

    @RabbitListener(queues = MQConfig.HEADER_QUEUE)
    public void receiveHeaderQueue(byte[] message) {
        log.info("MQReceive => receive header message:" + new String(message));
    }
    */

}
