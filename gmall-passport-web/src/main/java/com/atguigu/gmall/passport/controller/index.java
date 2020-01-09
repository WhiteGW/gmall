package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passport.config.JwtUtil;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class index {

    @Reference
    private UserService userService;
    @Value("${token.key}")
    String key;

    @RequestMapping("index")
    public String index(HttpServletRequest request) {
        String originUrl = request.getParameter("originUrl");
        // 保存
        request.setAttribute("originUrl", originUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){
        UserInfo info = userService.login(userInfo);
        if(info != null){
            Map map = new HashMap();
            map.put("userId",info.getId());
            map.put("nickName",info.getNickName());
            String salt = request.getHeader("X-forwarded-for");
            String token = JwtUtil.encode(key,map,salt);
            return token;
        }
        return "fail";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        // 从token 中获取userId  --- {解密token}  Map<String, Object> map1 = JwtUtil.decode(token, key, salt);
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");

        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        // 判断map
        if(map!=null && map.size()>0){
            // 从token 中解密 出来的userId
            String userId = (String) map.get("userId");
            // 调用服务层
            UserInfo userInfo = userService.verfiy(userId);

            if (userInfo!=null){
                return "success";
            }
        }
        return "fail";
    }
}
