package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

//@RestController
@Controller
public class OrderController {

    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;

    /**
     * 根据用户Id查询收货地址列表
     */
    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");
        List<UserAddress> userAddressList = userService.findUserAddressByUserId(userId);
        //获取购物车中选中数据
        List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);
        //订单信息集合
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
//      计算总金额
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        //保存数据
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
//        request.setAttribute("cartInfoList",cartInfoList);
        request.setAttribute("orderDetailList", orderDetailList);
        request.setAttribute("userAddressesList",userAddressList);
        return "trade";
    }

    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request){

        //用户Id
        String userId = (String) request.getAttribute("userId");
        orderInfo.setUserId(userId);
        //第三方交易编号
        String outTradNo = "ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradNo);
//        System.out.println(orderInfo.getOrderDetailList().toArray());
        String orderId = orderService.saveOrder(orderInfo);
        //到支付
        return "redirect://payment.gmall.com/index?orderId=\"+orderId";
    }
}
