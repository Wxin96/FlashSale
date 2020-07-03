package com.apollo.flashsale.vo;

import com.apollo.flashsale.domain.OrderInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单信息包装类
 */
@Setter
@Getter
public class OrderDetailVo {
    private GoodsVo goods;
    private OrderInfo order;

}
