package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class PageController {


    @Autowired
    private PageService pageService;
    /**
     * 跳转到商品详情页
     * @param id
     * @param model
     * @return
     */
    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id")Long id, Model model){
        //准备模型数据
        // 加载所需的数据
        Map<String, Object> modelMap = pageService.loadModel(id);
        // 放入模型
        model.addAllAttributes(modelMap);
        return "item";
    }
}
