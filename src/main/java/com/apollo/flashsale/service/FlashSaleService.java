package com.apollo.flashsale.service;

import com.apollo.flashsale.domain.FlashSaleOrder;
import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.domain.OrderInfo;
import com.apollo.flashsale.redis.key.impl.FlashSaleKey;
import com.apollo.flashsale.util.MD5Util;
import com.apollo.flashsale.util.UUIDUtil;
import com.apollo.flashsale.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class FlashSaleService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Transactional
    public OrderInfo flashSale(FlashSaleUser user, GoodsVo goods) {
        // 减库存, 下订单, 写入秒杀订单
        if (goodsService.reduceStock(goods)) {
            log.info("用户" + user.getId() + "秒杀到商品" + goods.getId());
            return orderService.createOrder(user, goods);
        } else {
            log.warn("商品已经秒杀完毕, 用户" + user.getId() + "秒杀失败!");
            setGoodsOver(goods.getId());
            return null;
            // throw new GlobalException(CodeMsg.FLASH_SALE_OVER);
        }
    }

    public long getFlashSaleResult(long userId, long goodsId) {
        // 1.获取秒杀订单
        FlashSaleOrder fsOrder = orderService.getFlashSaleOrderByUserIdGoodsId(userId, goodsId);
        // 2.判断秒杀情况
        // a.秒杀成功
        if (fsOrder != null) {
            return fsOrder.getOrderId();
        } else {
            // b.秒杀失败
            if (getGoodsOver(goodsId)) {
                return -1;
            } else {
                // c.等待结果
                return 0;
            }
        }
    }

    private void setGoodsOver(long goodsId) {
        redisService.set(FlashSaleKey.isGoodsOver, "" + goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(FlashSaleKey.isGoodsOver, "" + goodsId);
    }


    public void reset(List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }

    /**
     *  利用MD5、UUID和Salt生成秒杀路径, 临时存到缓存中, 设置有效时间为 60 秒.
     * @param user 秒杀用户
     * @param goodsId 秒杀商品id
     * @return 秒杀路径字段
     */
    public String createFlashSalePath(FlashSaleUser user, long goodsId) {
        // 0.预处理
        if (user == null || goodsId <= 0) {
            return null;
        }
        // 1.利用MD5、UUID和Salt生成秒杀路径
        String fsPath = MD5Util.md5(UUIDUtil.uuid() + "12345");
        redisService.set(FlashSaleKey.getFlashSalePath, "" + user.getId() + "_" + goodsId, fsPath);

        return fsPath;
    }

    /**
     *  查询Redis缓存, 判断用户输入的路径字段和Redis缓存中的是否相等
     * @param user 秒杀用户
     * @param goodsId 秒杀商品id
     * @param inputFSPath 用户输入的秒杀路径字段
     * @return 如果, 用户输入的路径字段和Redis缓存中的相等, 返回true;否则, 返回false;
     */
    public boolean checkPath(FlashSaleUser user, long goodsId, String inputFSPath) {
        // 0.预处理
        if (user == null || goodsId <= 0) {
            return false;
        }
        // 1.查询Redis缓存
        String fsPath = redisService.get(FlashSaleKey.getFlashSalePath, "" + user.getId() + "_" + goodsId, String.class);
        // 2.判断fsPath路径是否存在
        if (fsPath == null) {
            return false;
        }

        return fsPath.equals(inputFSPath);
    }

    /**
     * 根据用户和秒杀商品生成 图片验证码, 生成的图片验证码答案放进Redis缓存
     *
     * @param user    秒杀用户
     * @param goodsId 物品id
     * @return 图片缓存
     */
    public BufferedImage createVerifyCode(FlashSaleUser user, long goodsId) {
        // 0.预处理
        if (user == null || goodsId <= 0) {
            return null;
        }
        // 1.构造图片
        int width = 80, height = 32;
        // a.create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // b.set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // c.draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // d.create a random instance to generate the codes
        Random rdm = new Random();
        // e.make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // f.generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        // 3.把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(FlashSaleKey.getFlashSaleVerifyCode, user.getId() + "_" + goodsId, rnd);
        // 4.输出图片
        return image;
    }

    /**
     *  根据 用户 和 秒杀物品id 从Redis中获取图片的 verifyCode, 和用户输入的验证码值进行对比
     * @param user 秒杀用户
     * @param goodsId 秒杀物品id
     * @param inputVerifyCode 用户输入的验证码
     * @return 如果输入正确, 返回true; 否则, 返回false.
     */
    public boolean checkVerifyCode(FlashSaleUser user, long goodsId, Integer inputVerifyCode) {
        // 0.预处理
        if (user == null || goodsId <= 0) {
            return false;
        }
        // 1.Redis中获取预处验证码的值
        Integer verifyCode = redisService.get(FlashSaleKey.getFlashSaleVerifyCode, user.getId() + "_" + goodsId, Integer.class);
        // 2.Redis中验证码的值失效 或者 和用户输入的不相等
        if (verifyCode == null || verifyCode != inputVerifyCode) {
            return false;
        }
        // 3.避免验证码重复使用, 删除缓存中的键值
        redisService.delete(FlashSaleKey.getFlashSaleVerifyCode, user.getId() + "_" + goodsId);

        return true;
    }

    private static final char[] OPS = {'+', '-', '*'};

    /**
     * 利用随机数种子生成字符串表达式
     *
     * @param rdm 随机数种子
     * @return 验证码字符串
     */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = OPS[rdm.nextInt(3)];
        char op2 = OPS[rdm.nextInt(3)];

        return "" + num1 + op1 + num2 + op2 + num3;
    }

    /**
     *  根据字符串表示式子计算表达式的值
     * @param verifyCode 字符串表达式
     * @return 表达式的值
     */
    private int calc(String verifyCode) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engineByExtension = manager.getEngineByName("JavaScript");
            return (int) engineByExtension.eval(verifyCode);
        } catch (Exception e) {
            log.error("字符串表达式解析出错!", e);
            return 0;
        }
    }
}
