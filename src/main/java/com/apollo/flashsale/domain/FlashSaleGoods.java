package com.apollo.flashsale.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class FlashSaleGoods {
    private Long id;
    private Long goodsId;
    private Double flashSalePrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
