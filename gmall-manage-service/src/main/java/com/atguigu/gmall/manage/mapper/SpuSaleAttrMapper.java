package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    //销售属性
    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);

    //
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(String id, String spuId);
}
