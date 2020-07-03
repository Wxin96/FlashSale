package com.apollo.flashsale.vo;

import com.apollo.flashsale.domain.FlashSaleUser;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GoodsDetailVo {

    private int flashSaleStatus;

    private int remainSeconds;

    private GoodsVo goods;

    private FlashSaleUser user;

}
