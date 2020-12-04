package com.apollo.flashsale;

import com.apollo.flashsale.config.MQConfig;
import com.apollo.flashsale.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class RabbitMQTest {

    @Autowired
    AmqpTemplate amqpTemplate;

    @Test
    void testHeaders() {
        sendHeader("你好");
    }

    public void sendHeader(Object message) {
        String msg = RedisService.beanToString(message);
        log.info("MQSender => send header message:" + msg);
        MessageProperties properties = new MessageProperties();
        // properties.setHeader("header1", "value1");
        properties.setHeader("header2", "value2");
        properties.setHeader("header3", "value3");
        Message obj = new Message(msg.getBytes(), properties);
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
    }
}
