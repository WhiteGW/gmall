package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {

    /**
     * 查询所有用户信息
     * @return
     */
    List<UserInfo> findAll();

    /**
     * 根据用户Id查询地址
     * @param userId
     * @return
     */
    List<UserAddress> findUserAddressByUserId(String userId);

    /**
     * 登录
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

    /**
     * 认证
     * @param userId
     * @return
     */
    UserInfo verfiy(String userId);
}
