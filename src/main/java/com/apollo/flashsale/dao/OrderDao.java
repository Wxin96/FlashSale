package com.apollo.flashsale.dao;

import com.apollo.flashsale.domain.FlashSaleOrder;
import com.apollo.flashsale.domain.OrderInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {

    @Select("select * from flash_sale_order where user_id = #{userId} and goods_id = #{goodsId}")
    FlashSaleOrder getFlashSaleOrderByUserIdGoodsId(@Param("userId") long userId, @Param("goodsId") long goodsId);

    @Insert("insert into order_info(user_id, goods_id, delivery_add_id, goods_name, goods_count, goods_price, order_channel, status, create_date, pay_date)" +
            "values(#{userId}, #{goodsId}, #{deliveryAddrId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel}, #{status}, #{createDate}, #{payDate})")
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = long.class, before = false, statement = "select last_insert_id();")
    long insert(OrderInfo orderInfo);

    @Insert("insert into flash_sale_order(user_id, order_id, goods_id) values(#{userId}, #{orderId}, #{goodsId});")
    int insertFlashSaleOrder(FlashSaleOrder flashSaleOrder);

}
