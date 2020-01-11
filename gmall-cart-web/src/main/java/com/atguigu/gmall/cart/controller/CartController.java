package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    private CartService cartService;
    @Reference
    private ManageService manageService;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){
        //得到前台传递过来的数据
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");
        String userId = (String) request.getAttribute("userId");
        //判断用户是否登录
        if(userId == null){
            //用户未登录 储存一个临时用户，在Cookies中
            userId = CookieUtil.getCookieValue(request, "user-key", false);
            //未登录情况下，没有添加过一件商品
            if(userId == null){
                String tempUserId = UUID.randomUUID().toString().replace("-","");
                CookieUtil.setCookie(request, response, "user-key", tempUserId, 7*24*60*60, false);
            }
//            return "";
        }
        cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request){
//        request.getParameter("");//获取浏览器url后的参数，或表单提交的
        String userId = (String) request.getAttribute("userId");//获取作用域中的数据
        List<CartInfo> cartInfoList = new ArrayList<>();
        List<CartInfo> cartInfoNoLoginList = new ArrayList<>();;
        if(userId != null){
            String userTempId = CookieUtil.getCookieValue(request, "user-key", false);
            if (!StringUtils.isEmpty(userTempId)){
                //得到未登录的购物车数据
                cartInfoNoLoginList = cartService.getCartList(userTempId);
                if(cartInfoNoLoginList != null && cartInfoNoLoginList.size() > 0){
                    // 合并购物车
                    cartInfoList = cartService.mergeToCartList(cartInfoNoLoginList,userId);
                    //  删除未登录购物车
                    cartService.deleteCartList(userTempId);
                }
            }
            //直接查询登录数据
            if(StringUtils.isEmpty(userTempId) || cartInfoNoLoginList == null || cartInfoNoLoginList.size() == 1){
                cartInfoList = cartService.getCartList(userId);
            }

        }else {
            //获取未登录用户信息
            // 获取cookie 中的my-userId
            String userTempId = CookieUtil.getCookieValue(request, "user-key", false);
            if (userTempId!=null){
                cartInfoList = cartService.getCartList(userTempId);
            }
        }
        request.setAttribute("cartInfoList",cartInfoList);
        return "cartList";

    }

    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request){
        String userId = (String) request.getAttribute("userId");//获取作用域中的数据
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");

        if(userId == null){
            //未登录
            userId = CookieUtil.getCookieValue(request, "user-key", false);
        }
        cartService.checkCart(skuId, userId, isChecked);
    }

    @RequestMapping("toTrade")
    @LoginRequire
    public String toTrade(HttpServletRequest request, HttpServletResponse response){
        //获取userId
        String userId = (String) request.getAttribute("userId");
        //先获取未登录的数据
        String userTempId = CookieUtil.getCookieValue(request, "user-key", false);
        if (!StringUtils.isEmpty(userTempId)){
            //得到未登录的购物车数据
            List<CartInfo> cartInfoNoLoginList = cartService.getCartList(userTempId);
            if(cartInfoNoLoginList != null && cartInfoNoLoginList.size() > 0){
                // 合并购物车
                cartService.mergeToCartList(cartInfoNoLoginList,userId);
                //  删除未登录购物车
                cartService.deleteCartList(userTempId);
            }
        }
        return "redirect://trade.gmall.com/trade";
    }
}
