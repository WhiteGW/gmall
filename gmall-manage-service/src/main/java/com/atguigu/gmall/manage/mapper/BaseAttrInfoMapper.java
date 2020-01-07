package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseCatalog3;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    //通过三级分类Id查询
    List<BaseAttrInfo> selectBaseAttrInfoListByCatalog3Id(String catalog3Id);

    /**
     * 根据平台属性值Id查询平台属性集合
     * @param valueIds
     * @return
     */
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds") String valueIds);
}
