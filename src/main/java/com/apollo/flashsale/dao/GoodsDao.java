package com.apollo.flashsale.dao;

import com.apollo.flashsale.domain.FlashSaleGoods;
import com.apollo.flashsale.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {
    /**
     *  获取秒杀商品信息
     * @return 秒杀商品列表
     */
    @Select("select g.*, fsg.stock_count, fsg.start_date, fsg.end_date, fsg.flash_sale_price from flash_sale_goods fsg left join goods g on fsg.goods_id = g.id")
    List<GoodsVo> listGoodsVo();

    /**
     *  根据物品id返回特定的秒杀商品
     * @param goodsId 物品id
     * @return 秒杀商品信息
     */
    @Select("select g.*, fsg.stock_count, fsg.start_date, fsg.end_date, fsg.flash_sale_price from flash_sale_goods fsg left join goods g on fsg.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    /**
     *  秒杀商品减库存
     * @param flashSaleGoods 对应的秒杀商品
     * @return 返回更改的行数
     */
    @Update("update flash_sale_goods set stock_count = stock_count - 1 where goods_Id = #{goodsId}")
    int reduceStock(FlashSaleGoods flashSaleGoods);

}
