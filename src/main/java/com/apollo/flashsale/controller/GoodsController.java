package com.apollo.flashsale.controller;

import com.apollo.flashsale.domain.FlashSaleUser;
import com.apollo.flashsale.service.FlashSaleService;
import com.apollo.flashsale.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    FlashSaleService flashSaleService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/to_list")
    public String list(Model model, FlashSaleUser user) {
        model.addAttribute("user", user);

        return "goods_list";
    }
}
