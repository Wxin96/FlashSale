package com.apollo.flashsale.controller;

import com.apollo.flashsale.access.AccessLimit;
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

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/sell")
public class FlashSaleController implements InitializingBean {

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
            redisService.set(FlashSaleKey.isGoodsOver, "" + goods.getId(), false);
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
            log.info("商品" + goods.getId() + "在缓存中库存重置为10");
            redisService.set(GoodsKey.getFlashSaleGoodsStock, "" + goods.getId(), 10);
            log.info("商品" + goods.getId() + "在缓存中超卖标志重置为false");
            localOverMap.put(goods.getId(), false);
        }
        // redis缓存删除
        log.info("删除Redis中的秒杀订单OrderKeyfs缓存~");
        redisService.delete(OrderKey.getFlashSaleOrderKey);
        log.info("删除Redis中的秒杀结束标志FlashSalego缓存~");
        redisService.delete(FlashSaleKey.isGoodsOver);
        // 重置数据库
        log.info("重置数据库中秒杀产品库存~");
        flashSaleService.reset(goodsList);
        return Result.success(true);
    }

    /**
     *  秒杀接口
     *
     *  秒杀接口测试记录:
     *  - 第 5 章 2054 /sec
     *  - 第 6 章 4502 /sec
     *  - 第 6 章 电脑关闭其他软件 6453 /sec
     * @param user 秒杀用户
     * @param goodsId 货物Id
     * @return  秒杀信息
     */
    @PostMapping("/{inputFSPath}/do_sell")
    @ResponseBody
    public Result<Integer> list(FlashSaleUser user, @RequestParam("goodsId") long goodsId,
                                @PathVariable("inputFSPath") String inputFSPath) {
        // 1.判断用户是否登录
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 2.验证Path
        if (!flashSaleService.checkPath(user, goodsId, inputFSPath)) {
            log.warn("用户秒杀请求路径不合法!");
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 3.内存标记, 减少Redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            log.trace("根据over判断已经卖完~");
            return Result.error(CodeMsg.FLASH_SALE_OVER);
        }
        // 4.判断是否已经秒杀到
        FlashSaleOrder fsOrder = orderService.getFlashSaleOrderByUserIdGoodsId(user.getId(), goodsId);
        if (fsOrder != null) {
            log.trace("入队前, 通过访问redis缓存判断已经秒杀到物品" + goodsId);
            return Result.error(CodeMsg.REPEAT_FLASH_SALE);
        }
        // 5.Redis 预减库存
        Long stock = redisService.decr(GoodsKey.getFlashSaleGoodsStock, "" + goodsId);
        log.trace("Redis预减库存, 物品" + goodsId + "剩余数量为 : " + stock);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.FLASH_SALE_OVER);
        }
        // 5.入队
        FlashSaleMessage flashSaleMessage = new FlashSaleMessage();
        flashSaleMessage.setUser(user);
        flashSaleMessage.setGoodsId(goodsId);
        mqSender.sendFlashSaleMessage(flashSaleMessage);
        log.trace("秒杀信息入队," + flashSaleMessage);

        return Result.success(0);   // 排队中

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
        // 0.用户登陆验证
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = flashSaleService.getFlashSaleResult(user.getId(), goodsId);
        return Result.success(result);
    }

    @AccessLimit(seconds = 60, maxCount = 20, needLogin = true)
    @GetMapping("/path")
    @ResponseBody
    public Result<String> getFlashSalePath(HttpServletRequest request, FlashSaleUser user,
                                           @RequestParam("goodsId") long goodsId,
                                           @RequestParam("inputVerifyCode") Integer inputVerifyCode) {
        // 1.登录验证, 分布式Session
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 2.判断验证码是否合法
        if (!flashSaleService.checkVerifyCode(user, goodsId, inputVerifyCode)) {
            log.warn("验证码输入错误!");
            return Result.error(CodeMsg.REQUEST_ILLEGAL_VERIFY_CODE);
        }
        // 3.获取秒杀路径
        String fsPath = flashSaleService.createFlashSalePath(user, goodsId);

        return Result.success(fsPath);
    }

    /**
     *  返回图片验证码
     * @param response response响应, 此处使用response传输图片数据流. 有效时间为60秒.
     * @param user 秒杀用户
     * @param goodsId 秒杀商品di
     * @return Result结果封装的信息
     */
    @GetMapping("/verifyCode")
    @ResponseBody
    public Result<String> getFlashSaleVerifyCode(HttpServletResponse response, FlashSaleUser user,
                                                 @RequestParam("goodsId") long goodsId) {
        // 1.登录验证, 分布式Session
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        // 2.输出图片验证码, 利用response的输出流
        OutputStream outputStream = null;
        try {
            BufferedImage image = flashSaleService.createVerifyCode(user, goodsId);
            outputStream = response.getOutputStream();
            ImageIO.write(image, "JPEG", outputStream);
            outputStream.flush();
            return Result.success("图片写入成功~");
        } catch (Exception e) {
            log.error("传递图片验证码时出错!", e);
            return Result.error(CodeMsg.FLASH_SALE_FAIL);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



}
