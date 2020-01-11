package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {

    // 添加购物车 用户Id，商品Id，商品数量。
    void  addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 根据用户Id查询购物车列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 合并购物车
     * @param cartInfoNoLoginList
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList, String userId);

    /**
     * 删除未登录购物车
     * @param userTempId
     */
    void deleteCartList(String userTempId);

    /**
     * 修改购物车状态
     * @param skuId
     * @param userId
     * @param isChecked
     */
    void checkCart(String skuId, String userId, String isChecked);

}
