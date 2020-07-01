package com.apollo.flashsale.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FlashSaleOrder {
    private Long id;
    private Long userId;
    private Long orderId;
    private Long goodsId;

}
