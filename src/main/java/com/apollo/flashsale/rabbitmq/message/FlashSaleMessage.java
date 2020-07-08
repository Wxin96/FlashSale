package com.apollo.flashsale.rabbitmq.message;

import com.apollo.flashsale.domain.FlashSaleUser;
import lombok.Data;

@Data
public class FlashSaleMessage {
    private FlashSaleUser user;
    private long goodsId;
}
