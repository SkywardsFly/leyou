package com.leyou.item.web;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.impl.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @RequestMapping("/list")
    public ResponseEntity<List<Category>> queryByPid(@RequestParam("pid") Long pid){
       return ResponseEntity.ok(categoryService.queryListByPid(pid));
    }

    /**
     *
     * @param ids
     * @return
     */
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }
}
