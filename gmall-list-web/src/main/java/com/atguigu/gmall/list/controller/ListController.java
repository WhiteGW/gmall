package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    //@ResponseBody
    public String getList(SkuLsParams skuLsParams, HttpServletRequest request){
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        //获取所有的商品信息
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();

        //获取平台属性值id显示属性值
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        //根据平台属性值Id查询平台属性集合
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrInfoList(attrValueIdList);
        //制作urlParam参数
        String urlParam = makeUrlParam(skuLsParams);

        //放面包屑的集合
        List<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();

        if(baseAttrInfoList != null && baseAttrInfoList.size() > 0){
            //迭代器itco
            for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo =  iterator.next();
                //获取平台属性集合
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
                        for (String valueId : skuLsParams.getValueId()) {
                            if(baseAttrValue.getId().equals(valueId)){
                                iterator.remove();

                                //组装面包屑
                                BaseAttrValue baseAttrValueed = new BaseAttrValue();
                                baseAttrValueed.setValueName(baseAttrInfo.getAttrName() +":"+ baseAttrValue.getValueName());
                                //赋值最新的参数列表
                                String newUrlParam = makeUrlParam(skuLsParams, valueId);
                                baseAttrValueed.setUrlParam(newUrlParam);
                                baseAttrValueArrayList.add(baseAttrValueed);
                            }
                        }
                    }
                }
            }
        }


        //设置每页显示条数
        skuLsParams.setPageSize(3);
        //保存数据
        request.setAttribute("totalPages",skuLsResult.getTotalPages());
        request.setAttribute("pageNo",skuLsParams.getPageNo());
        request.setAttribute("keyword",skuLsParams.getKeyword());
        request.setAttribute("baseAttrValueArrayList",baseAttrValueArrayList);
        System.out.println(urlParam);
        request.setAttribute("urlParam",urlParam);
        request.setAttribute("baseAttrInfoList",baseAttrInfoList);
        request.setAttribute("skuLsInfoList",skuLsInfoList);
//        return JSON.toJSONString(search);
        return "list";
    }

    //制作查询参数
    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {
        String urlParam ="";
        if(skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0){
            urlParam += "keyword=" + skuLsParams.getKeyword();
        }
        if(skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0){
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }
        if(skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0){
            for(String valueId : skuLsParams.getValueId()){
                if(excludeValueIds != null && excludeValueIds.length > 0){
                    //获取对象中第一个数据
                    String excludeValueId = excludeValueIds[0];
                    if(excludeValueId.equals(valueId)){
                        //停止当前循环
                        continue;
                    }
                }
//                System.out.println(valueId);
                urlParam += "&valueId=" + valueId;
            }
        }
        return urlParam;
    }

}
