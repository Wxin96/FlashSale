package com.apollo.flashsale.controller;

import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.redis.key.impl.GoodsKey;
import com.apollo.flashsale.result.Result;
import com.apollo.flashsale.service.FlashSaleUserService;
import com.apollo.flashsale.service.GoodsService;
import com.apollo.flashsale.service.RedisService;
import com.apollo.flashsale.vo.GoodsDetailVo;
import com.apollo.flashsale.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    ThymeleafViewResolver viewResolver;


    /**
     * 更新：页面缓存（Redis）
     * 页面缓存后测试, QTS: 2244/sec
     * @param request Request请求
     * @param response Response响应
     * @param model Model数据
     * @param user 用户
     * @return 静态化页面
     */
    @RequestMapping(path = "/to_list", produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, FlashSaleUser user) {
        /*---------------------------去缓存---------------------------*/
        // 1.取缓存
        String goodsListHtml = redisService.get(GoodsKey.getGoodsList, "wx", String.class);
        if (!StringUtils.isEmpty(goodsListHtml)) {
            log.debug("/to_list ==> 使用的是Redis中的缓存");
            return goodsListHtml;
        }
        /*---------------------------手动渲染---------------------------*/
        // 2. 从 [mysql数据库] 中, 查询商品列表
        List<GoodsVo> goodsList = goodsService.listGoodsVo();

        // 3.Model中放入[用户, 商品]信息
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsList);

        // 4.手动渲染模板
        // 细节: 提供model参数才能渲染上对应的动态页面
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        goodsListHtml = viewResolver.getTemplateEngine().process("goods_list", webContext);

        /*---------------------------结果输出---------------------------*/
        // 5.存入Redis
        if (!StringUtils.isEmpty(goodsListHtml)) {
            redisService.set(GoodsKey.getGoodsList, "wx", goodsListHtml);
        }

        log.debug("/to_list ==> 初次渲染, 查询了一次数据库");
        return goodsListHtml;
    }

    /**
     *  更新： URL缓存（Redis）
     *  问题: 秒杀倒计时不准(一分钟迟滞), 需要解决
     * @param request Request请求
     * @param response Response响应
     * @param model Model数据
     * @param user FlashSale用户
     * @param goodsId 货品的id
     * @return 静态页面
     */
    @RequestMapping(value = "/to_detail2/{goodsId}", produces = "text/html")
    @ResponseBody
    public String detail(HttpServletRequest request, HttpServletResponse response, Model model,
                         FlashSaleUser user, @PathVariable("goodsId") long goodsId) {
        log.info("URL缓存的Controller方法");
        /*---------------------------去缓存---------------------------*/
        // 1.取缓存
        String goodsDetailHtml = redisService.get(GoodsKey.getGoodsDetail, "wx" + goodsId, String.class);
        if (!StringUtils.isEmpty(goodsDetailHtml)) {
            log.info("使用的是Redis缓存");
            return goodsDetailHtml;
        }
        /*---------------------------手动渲染---------------------------*/
        // 2.查询 [mysql] 数据库, 获取秒杀商品信息并放入
        GoodsVo goods = goodsService.getGoodsVoGoodsId(goodsId);

        // 3.获取秒杀信息
        long startTime = goods.getStartDate().getTime(),
                endTime = goods.getEndDate().getTime(),
                curTime = System.currentTimeMillis();

        log.info("物品" + goodsId + "秒杀开始时间 : " + goods.getStartDate().toString());
        log.info("物品" + goodsId + "秒杀结束时间 : " + goods.getEndDate().toString());
        int flashSaleStatus = 0, remainSeconds = 0;
        if (curTime < startTime) {//秒杀还没开始，倒计时
            log.info("秒杀未开始");
            remainSeconds = (int) ((startTime - curTime) / 1000);
        } else if (curTime > endTime) {//秒杀已经结束
            log.info("秒杀结束");
            flashSaleStatus = 2;
            remainSeconds = -1;
        } else {//秒杀进行中
            log.info("秒杀进行中..");
            flashSaleStatus = 1;
        }

        // 4.Model中放入[用户, 商品, 秒杀]信息
        model.addAttribute("goods", goods);
        model.addAttribute("user", user);
        model.addAttribute("flashSaleStatus", flashSaleStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        // 5.手动渲染模板
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        goodsDetailHtml = viewResolver.getTemplateEngine().process("goods_detail", webContext);

        /*---------------------------结果输出---------------------------*/
        // 5.存入Redis
        if (!StringUtils.isEmpty(goodsDetailHtml)) {
            redisService.set(GoodsKey.getGoodsDetail, "wx" + goodsId, goodsDetailHtml);
        }

        log.info("查询数据库, 初次渲染, 未使用Redis缓存");
        return goodsDetailHtml;
    }

    /**
     *  页面静态化, 异步请求页面的动态数据
     * @param user 根据分布式Session获取页面的动态数据
     * @param goodsId 物品ID号
     * @return 返回动态数据的json数据
     */
    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(FlashSaleUser user, @PathVariable("goodsId") long goodsId) {
        // 0.日志记录
        log.trace("商品列表页 : 页面静态化, 异步请求页面的动态数据");
        // 1.获取秒杀商品信息
        GoodsVo goods = goodsService.getGoodsVoGoodsId(goodsId);
        // 2.计算秒杀信息
        long startTime = goods.getStartDate().getTime(),
                endTime = goods.getEndDate().getTime(),
                curTime = System.currentTimeMillis();
        log.debug("物品" + goodsId + "秒杀开始时间 : " + goods.getStartDate().toString());
        log.debug("物品" + goodsId + "秒杀结束时间 : " + goods.getEndDate().toString());

        int flashSaleStatus = 0, remainSeconds = 0;
        if (curTime < startTime) {//秒杀还没开始，倒计时
            remainSeconds = (int) ((startTime - curTime) / 1000);
            log.debug("秒杀未开始, 剩余时间为 : " + remainSeconds + " 秒");
        } else if (curTime > endTime) {//秒杀已经结束
            log.debug("秒杀结束");
            flashSaleStatus = 2;
            remainSeconds = -1;
        } else {//秒杀进行中
            log.debug("秒杀进行中..");
            flashSaleStatus = 1;
        }
        // 3.绑定数据
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setFlashSaleStatus(flashSaleStatus);
        vo.setRemainSeconds(remainSeconds);

        return Result.success(vo);
    }

}
