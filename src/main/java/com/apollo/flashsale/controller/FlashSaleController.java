package com.apollo.flashsale.controller;

import com.apollo.flashsale.domain.FlashSaleOrder;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.rabbitmq.MQSender;
import com.apollo.flashsale.rabbitmq.message.FlashSaleMessage;
import com.apollo.flashsale.redis.key.impl.FlashSaleKey;
import com.apollo.flashsale.redis.key.impl.GoodsKey;
import com.apollo.flashsale.redis.key.impl.OrderKey;
import com.apollo.flashsale.result.CodeMsg;
import com.apollo.flashsale.result.Result;
import com.apollo.flashsale.service.*;
import com.apollo.flashsale.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/sell")
public class FlashSaleController implements InitializingBean {

    @Autowired
    FlashSaleUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    FlashSaleService flashSaleService;

    @Autowired
    MQSender mqSender;

    private final HashMap<Long, Boolean> localOverMap = new HashMap<>();

    /**
     * 系统初始化
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getFlashSaleGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }

    }

    @GetMapping("/reset")
    @ResponseBody
    public Result<Boolean> reset() {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for (GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            // 重置redis
            redisService.set(GoodsKey.getFlashSaleGoodsStock, "" + goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        // redis缓存删除
        redisService.delete(OrderKey.getFlashSaleOrderKey);
        redisService.delete(FlashSaleKey.isGoodsOver);
        // 重置数据库
        flashSaleService.reset(goodsList);
        return Result.success(true);
    }

    /**
     *  第 5 章 2054 /sec
     *
     *  第 6 章 4502 /sec
     *  电脑关闭其他软件 6453 /sec
     * @param user 秒杀用户
     * @param goodsId 货物Id
     * @return  秒杀信息
     */
    @PostMapping("/do_sell")
    @ResponseBody
    public Result<Integer> list(FlashSaleUser user, @RequestParam("goodsId") long goodsId) {
        // 1.判断用户是否登录
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 2.内存标记, 减少Redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.FLASH_SALE_OVER);
        }
        // 3.Redis 预减库存
        Long stock = redisService.decr(GoodsKey.getFlashSaleGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.FLASH_SALE_OVER);
        }
        // 4.判断是否已经秒杀到
        FlashSaleOrder fsOrder = orderService.getFlashSaleOrderByUserIdGoodsId(user.getId(), goodsId);
        if (fsOrder != null) {
            return Result.error(CodeMsg.REPEAT_FLASH_SALE);
        }
        // 5.入队
        FlashSaleMessage flashSaleMessage = new FlashSaleMessage();
        flashSaleMessage.setUser(user);
        flashSaleMessage.setGoodsId(goodsId);
        mqSender.sendFlashSaleMessage(flashSaleMessage);

        return Result.success(0);   // 排队中


/*        // 2.判断库存
        GoodsVo goods = goodsService.getGoodsVoGoodsId(goodsId);
        int stock = goods.getStockCount();
        if (stock <= 0) {
            return Result.error(CodeMsg.FLASH_SALE_OVER);
        }
        // 3.判断是否已经秒杀到
        FlashSaleOrder fsOrder = orderService.getFlashSaleOrderByUserIdGoodsId(user.getId(), goodsId);
        if (fsOrder != null) {
            return Result.error(CodeMsg.REPEAT_FLASH_SALE);
        }
        // 4.进行秒杀
        OrderInfo orderInfo = flashSaleService.flashSale(user, goods);

        return Result.success(orderInfo);
        */

    }

    /**
     *  查询订单情况
     * @param user 查询用户
     * @param goodsId 查询的秒杀商品
     * @return result = 0 等待, 下一次轮询
     *                = -1 秒杀事变
     *                > 0 订单id
     */
    @GetMapping("/result")
    @ResponseBody
    public Result<Long> flashSaleResult(FlashSaleUser user, @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = flashSaleService.getFlashSaleResult(user.getId(), goodsId);
        return Result.success(result);
    }
}
