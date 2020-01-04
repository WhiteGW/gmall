package com.atguigu.gmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration //相当于spring3.0版本的xml
public class RedisConfig{

    /*
     * 获取到配置文件中的host，port，timeOut等参数
     * 将RedisUtil放入到spring容器中管理
     */

    //获取配置文件中的host，如果没有数据，给默认值：disabled
    @Value("${spring.redis.host:disabled}")
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;


    @Value("${spring.redis.timeOut:10000}")
    private int timeOut;

    /*<bean ></bean>*/
    @Bean
    public RedisUtil getRedisUtil(){
        if("disabled".equals(host)){
            return null;
        }

        RedisUtil redisUtil = new RedisUtil();
        //初始化连接池工厂
        redisUtil.initJedisPool(host, port ,timeOut);

        return redisUtil;
    }

}
