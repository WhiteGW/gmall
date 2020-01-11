package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@RestController
@Controller
public class OrderController {

    @Reference
    private UserService userService;

    /**
     * 根据用户Id查询收货地址列表
     */
    @RequestMapping("trade")
    public String trade(String userId){

        return "trade";
    }
}
