package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mappeer.UserAddressMapper;
import com.atguigu.gmall.user.mappeer.UserInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;
import java.util.List;


@Service
public class UserServiceImpl implements UserService{

    @Autowired(required = false)
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24*7;


    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> findUserAddressByUserId(String userId) {
//        Example example = new Example(UserAddress.class);
//        example.createCriteria().andEqualTo("userId",userId);
//        return userAddressMapper.selectByExample(example);

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        //密码加密
        String passwd = userInfo.getPasswd();
        String newPasswd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPasswd);
        UserInfo info = userInfoMapper.selectOne(userInfo);
        if (info != null) {
            Jedis jedis = redisUtil.getJedis();
            //定义key
            String userKey = userKey_prefix + info.getId() + userinfoKey_suffix;
            //设置用户的过期时间
            jedis.setex(userKey,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verfiy(String userId) {
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();
        // 组成userKey
        String userKey = userKey_prefix+userId+userinfoKey_suffix;
        // 获取缓存中的数据
        String userJson = jedis.get(userKey);
        if (!StringUtils.isEmpty(userJson)){
            // userJson 转换成对象
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }

        return null;
    }
}
