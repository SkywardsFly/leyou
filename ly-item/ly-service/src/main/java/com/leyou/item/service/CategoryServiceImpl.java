package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.item.pojo.Category;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.service.impl.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public List<Category> queryListByPid(Long pid) {
        //查询条件，mapper会把对象中非空属性作为查询条件
        Category t= new Category();
        t.setParentId(pid);
        List<Category> list = categoryMapper.select(t);
        if (CollectionUtils.isEmpty(list)){
            throw new LyExcetion(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    @Override
    public List<Category> queryByIds(List<Long> ids) {
        List<Category> categoryList = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyExcetion(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categoryList;
    }
}
