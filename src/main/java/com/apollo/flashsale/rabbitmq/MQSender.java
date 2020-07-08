package com.apollo.flashsale.rabbitmq;

import com.apollo.flashsale.config.MQConfig;
import com.apollo.flashsale.rabbitmq.message.FlashSaleMessage;
import com.apollo.flashsale.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MQSender {

    @Autowired
    AmqpTemplate amqpTemplate;

    public void sendFlashSaleMessage(FlashSaleMessage fsMsg) {
        String msg = RedisService.beanToString(fsMsg);
        log.trace("MQSender send flashSaleMessage : " + msg);
        amqpTemplate.convertAndSend(MQConfig.FLASH_SALE_QUEUE, msg);
    }


    // /**
    //  *  向队列中添加信息
    //  * @param message 添加对象
    //  */
    /*
    public void send(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("MQSender => send message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
    }

    public void sendTopic(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("MQSender => send topic message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key1", msg + 1);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key2", msg + 2);
    }

    public void sendFanout(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("MQSender => send fanout message:" + msg);
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "", msg);
    }

    public void sendHeader(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("MQSender => send header message:" + msg);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("header1", "value1");
        properties.setHeader("header2", "value2");
        Message obj = new Message(msg.getBytes(), properties);
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
    }
    */

}
