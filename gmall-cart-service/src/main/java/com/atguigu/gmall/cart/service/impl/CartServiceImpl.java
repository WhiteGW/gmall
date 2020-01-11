package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Reference
    private ManageService manageService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {

        Jedis jedis = redisUtil.getJedis();
        //定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        // 调用查询数据库并加入缓存
        if(!jedis.exists(cartKey)){
            loadCartCache(userId);
        }

        //判断购物车中是否又要添加的商品
//        CartInfo cartInfo = new CartInfo();
//        cartInfo.setUserId(userId);
//        cartInfo.setSkuId(skuId);
//        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userId).andEqualTo("skuId",skuId);
        CartInfo cartInfoExist = cartInfoMapper.selectOneByExample(example);

        if(cartInfoExist != null){
            //有,加
            cartInfoExist.setSkuNum(skuNum + cartInfoExist.getSkuNum());
            //初始化skuPrice
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            //更新数据
            cartInfoMapper.updateByPrimaryKey(cartInfoExist);
        }else{
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();

            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setSkuId(skuId);
            cartInfo1.setUserId(userId);
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuName(skuInfo.getSkuName());

            cartInfoMapper.insertSelective(cartInfo1);
            cartInfoExist = cartInfo1;
        }

        setCartExpire(skuId, userId, jedis, cartKey, cartInfoExist);
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        List<CartInfo> cartInfos = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        //定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        List<String> stringList = jedis.hvals(cartKey);
        if(stringList != null && stringList.size() > 0){
            for (String cart : stringList) {
                cartInfos.add(JSON.parseObject(cart,CartInfo.class));
            }
            cartInfos.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });

            return cartInfos;
        }else{
            cartInfos = loadCartCache(userId);
            return cartInfos;
        }
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartInfoNoLoginList, String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        List<CartInfo> cartInfoLoginList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if(cartInfoLoginList != null && cartInfoLoginList.size() > 0){
            for (CartInfo cartInfoNoLogin : cartInfoNoLoginList) {
                boolean flag = false;
                for (CartInfo cartInfoLogin : cartInfoLoginList) {
                    if(cartInfoLogin.getSkuId().equals(cartInfoNoLogin.getSkuId())){
                        cartInfoLogin.setSkuNum(cartInfoNoLogin.getSkuNum() + cartInfoLogin.getSkuNum());
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoLogin);
                        flag = true;
                    }
                }
                if(!flag){
                    //自增
                    cartInfoNoLogin.setId(null);
                    cartInfoNoLogin.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoNoLogin);
                }
            }
        }else{
            //数据库中没有数据
            for (CartInfo cartInfoNoLogin : cartInfoNoLoginList) {
                cartInfoNoLogin.setId(null);
                cartInfoNoLogin.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoNoLogin);
            }
        }

        cartInfoList = loadCartCache(userId);
        //判断状态合并数据
        if(cartInfoList != null && cartInfoList.size() > 0){
            for (CartInfo cartInfoLogin : cartInfoLoginList) {
                for (CartInfo cartInfo : cartInfoNoLoginList) {
                    if(cartInfo.getSkuId().equals(cartInfoLogin.getSkuId())){
                        if("1".equals(cartInfo.getIsChecked())){
                            cartInfoLogin.setIsChecked("1");
                            checkCart(cartInfo.getSkuId(),userId,"1");
                        }
                    }
                }
            }
        }
        return cartInfoList;
    }

    @Override
    public void deleteCartList(String userTempId) {
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", userTempId);
        cartInfoMapper.deleteByExample(example);

        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userTempId + CartConst.USER_CART_KEY_SUFFIX;
        jedis.del(cartKey);

        jedis.close();
    }

    @Override
    public void checkCart(String skuId, String userId, String isChecked) {
        //修改数据库
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userId).andEqualTo("skuId",skuId);
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        cartInfoMapper.updateByExampleSelective(cartInfo, example);

        //修改缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(cartKey, skuId);
        CartInfo cartInfoJson = JSON.parseObject(cartJson, CartInfo.class);
        cartInfoJson.setIsChecked(isChecked);
        jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfoJson));

        jedis.close();
    }

    private List<CartInfo> loadCartCache(String userId) {
        // 使用实时价格：将skuInfo.price 价格赋值 cartInfo.skuPrice
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

        if (cartInfoList==null || cartInfoList.size()==0){
            return  null;
        }
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();
        // 定义key：user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        HashMap<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
            //jedis.hset(cartKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(cartKey,map);

        jedis.close();
        return cartInfoList;

    }

    private void setCartExpire(String skuId, String userId, Jedis jedis, String cartKey, CartInfo cartInfoExist) {
        jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist)) ;
        String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;

        if(jedis.exists(userKey)){
            //获取用户过期时间
            Long ttl = jedis.ttl(userKey);
            //将用户过期时间给购物车过期时间
            jedis.expire(cartKey,ttl.intValue());
        }else{
            jedis.expire(cartKey,7*24*60*60);
        }
    }
}