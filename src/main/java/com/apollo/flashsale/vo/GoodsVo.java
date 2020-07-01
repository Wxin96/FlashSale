package com.apollo.flashsale.vo;

import com.apollo.flashsale.domain.Goods;
import lombok.Data;

import java.util.Date;

@Data
public class GoodsVo extends Goods {
    private Double flashSalePrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
