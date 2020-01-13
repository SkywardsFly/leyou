package com.leyou.page.service;

import com.leyou.item.pojo.*;

import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {

        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);

        // 查询spu详情
        SpuDetail detail = goodsClient.queryDetailById(spuId);

        // 查询sku
        List<Sku> skus = goodsClient.querySkuBySpuId(spuId);

        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());

        // 查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        // 查询规格参数
        List<SpecGroup> specs = specificationClient.querySpecGroupByCid(spu.getCid3());

        Map<String, Object> map = new HashMap<>();
        map.put("title", spu.getTitle());
        map.put("spuTitle", spu.getSubTitle());
        map.put("skus", skus);
        map.put("detail", detail);
        map.put("brand", brand);
        map.put("categories", categories);
        map.put("specs", specs);
        return map;
    }


    /**
     * 创建html页面
     *
     * @param spuId
     * @throws Exception
     */
    public void createHtml(Long spuId) {

        PrintWriter writer = null;
        try {
            // 创建thymeleaf上下文对象
            Context context = new Context();
            // 把数据放入上下文对象
            context.setVariables(loadModel(spuId));
            // 创建输出流
            File file = new File("E:\\www\\tmplates\\html\\" + spuId + ".html");
            if (file.exists()) {
                file.delete();
            }
            writer = new PrintWriter(file,"UTF-8");
            // 执行页面静态化方法
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            log.error("页面静态化出错：{}，"+ e, spuId);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void deleteHtml(Long spuId) {
        File file = new File("E:\\www\\tmplates\\html\\" + spuId + ".html");
        if (file.exists()){
            file.delete();
        }
    }
}
