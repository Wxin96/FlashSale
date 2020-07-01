package com.apollo.flashsale.controller;

import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.service.FlashSaleUserService;
import com.apollo.flashsale.service.GoodsService;
import com.apollo.flashsale.service.RedisService;
import com.apollo.flashsale.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    FlashSaleUserService flashSaleUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/to_list")
    public String list(Model model, FlashSaleUser user) {
        model.addAttribute("user", user);
        // 查询商品列表
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);

        return "goods_list";
    }

    @RequestMapping("/to_detail/{goodsId}")
    public String detail(Model model, FlashSaleUser user, @PathVariable("goodsId") long goodsId) {
        // 1.将用户信息放入
        model.addAttribute("user", user);
        // 2.获取秒杀商品信息并放入
        GoodsVo goods = goodsService.getGoodsVoGoodsId(goodsId);
        model.addAttribute("goods", goods);
        // 3.获取秒杀时间
        long startTime = goods.getStartDate().getTime();
        long endTime = goods.getEndDate().getTime();
        long curTime = System.currentTimeMillis();
        log.info(goods.getStartDate().toString());
        log.info(goods.getEndDate().toString());

        int flashSaleStatus = 0;
        int remainSeconds = 0;
        if(curTime < startTime ) {//秒杀还没开始，倒计时
            // flashSaleStatus = 0;
            log.info("秒杀未开始");
            remainSeconds = (int) ((startTime - curTime) / 1000);
        }else  if(curTime > endTime){//秒杀已经结束
            log.info("秒杀结束");
            flashSaleStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            log.info("秒杀进行中..");
            flashSaleStatus = 1;
            // remainSeconds = 0;
        }
        model.addAttribute("flashSaleStatus", flashSaleStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        return "goods_detail";
    }
}
