package com.leyou.search.repository;


import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void createIndex(){
        // 创建索引
        template.createIndex(Goods.class);
        // 配置映射
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        // 创建索引
        template.createIndex(Goods.class);
        // 配置映射
        template.putMapping(Goods.class);
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            // 查询分页数据
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);
            List<Spu> spus = result.getItems();
            if (CollectionUtils.isEmpty(spus)){
                break;
            }
            // 创建Goods集合
            List<Goods> goodsList = spus.stream().map(searchService::buildGoods).collect(Collectors.toList());

            goodsRepository.saveAll(goodsList);
            page++;
            size = spus.size();
        } while (size == 100);
    }
}
