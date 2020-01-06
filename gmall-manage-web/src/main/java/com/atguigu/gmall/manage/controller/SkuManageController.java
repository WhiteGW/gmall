package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @RequestMapping("spuImageList")
    public List<SpuImage> getSpuImageList(String spuId,SpuImage spuImage){
        return manageService.getSpuImageList(spuImage);
    }

    // 通过spuId 查询销售属性-销售属性值
    // http://localhost:8082/spuSaleAttrList?spuId=6
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId){
        return manageService.getSpuSaleAttrList(spuId);
    }

    @RequestMapping("saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
    }

    //商品上架
    @RequestMapping("onSale")
    public void onSale(String skuId){
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        BeanUtils.copyProperties(skuInfo,skuLsInfo);
        listService.saveSkuInfo(skuLsInfo);
    }
}
