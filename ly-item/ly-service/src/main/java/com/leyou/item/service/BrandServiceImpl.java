package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.impl.IBrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@Service
public class BrandServiceImpl implements IBrandService {

    @Autowired
    private BrandMapper brandMapper;
    @Override
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {

        //使用分页助手分页
        PageHelper.startPage(page,rows);

        Example example = new Example(Brand.class);
        //过滤
        if (StringUtils.isNotBlank(key)){
            //过滤条件
            example.createCriteria().orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if (StringUtils.isNotBlank(sortBy)){
            String orderByClauses = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClauses);
        }

        List<Brand> lists = brandMapper.selectByExample(example);
        //解析分页结果
        PageInfo<Brand> pageInfo = new PageInfo<>(lists);
        return new PageResult<>(pageInfo.getTotal(),lists);
    }

    @Transactional
    @Override
    public void saveBrand(Brand brand, List<Long> cids) {
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if (count != 1){
            throw new LyExcetion(ExceptionEnum.BRAND_BAD_CREATED);
        }
        for (Long cid : cids){
            brandMapper.insertCategoryBrand(cid,brand.getId());
        }
    }

    @Override
    public Brand queryById(Long Id) {
        Brand brand = brandMapper.selectByPrimaryKey(Id);
        if (brand == null){
            throw new LyExcetion(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = brandMapper.queryBrandByCid(cid);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyExcetion(ExceptionEnum.BRAND_BAD_CREATED);
        }
        return brands;
    }

    @Override
    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyExcetion(ExceptionEnum.BRAND_BAD_CREATED);
        }
        return brands;
    }


}
