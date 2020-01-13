package com.leyou.item.service.impl;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;

import java.util.List;

public interface IBrandService {

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key);


    public void saveBrand(Brand brand, List<Long> cids);

    public Brand queryById(Long brandId);

    public List<Brand> queryBrandByCid(Long cid);

    public List<Brand> queryBrandByIds(List<Long> ids);
}
