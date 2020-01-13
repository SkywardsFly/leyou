package com.leyou.item.service.impl;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface ICategoryService {

    public List<Category> queryListByPid(Long pid);

    List<Category> queryByIds(List<Long> ids);
}
