package com.apollo.flashsale.service;

import com.apollo.flashsale.dao.GoodsDao;
import com.apollo.flashsale.domain.FlashSaleGoods;
import com.apollo.flashsale.vo.GoodsVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GoodsService {

    @Resource
    GoodsDao goodsDao;

    // 秒杀商品列表
    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }

    // 获取特定秒杀商品
    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public boolean reduceStock(GoodsVo goodsVo) {
        // GoodsVo中id就是FlashSaleGoods的goodsId
        FlashSaleGoods flashSaleGoods = new FlashSaleGoods();
        flashSaleGoods.setGoodsId(goodsVo.getId());
        int change = goodsDao.reduceStock(flashSaleGoods);
        return change >= 1;
    }


    public void resetStock(List<GoodsVo> goodsList) {
        for(GoodsVo goods : goodsList ) {
            FlashSaleGoods g = new FlashSaleGoods();
            g.setGoodsId(goods.getId());
            g.setStockCount(goods.getStockCount());
            goodsDao.resetStock(g);
        }

    }
}
