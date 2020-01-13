package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import com.leyou.item.service.impl.IBrandService;
import com.leyou.item.service.impl.ICategoryService;
import com.leyou.item.service.impl.IGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GoodsServiceImpl implements IGoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper detailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IBrandService brandService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 查询
     * @param page
     * @param rows
     * @param saleable
     * @param key
     * @return
     */
    @Override
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 1、查询SPU
        // 分页,最多允许查100条
        PageHelper.startPage(page, Math.min(rows, 100));
        // 创建查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 是否过滤上下架
        if (saleable != null) {
            criteria.orEqualTo("saleable", saleable);
        }
        // 是否模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        if (saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }
        example.setOrderByClause("last_update_time DESC");
        List<Spu> spus = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)){
            throw new LyExcetion(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //解析品牌分类和商品分类名称
        loadCategoryAndBrandName(spus);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);

        return new PageResult<>(pageInfo.getTotal(), spus);
    }

    /**
     * 新增
     * @param spu
     */

    @Transactional
    @Override
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if (count != 1){
            throw new LyExcetion(ExceptionEnum.GOODS_SAVE_BAD);
        }
        //新增detail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        detailMapper.insert(spuDetail);
        //新增sku和库存
        saveSkuAndStock(spu);
        //发送mq消息
        try {
            amqpTemplate.convertAndSend("item-insert",spu.getId());
        } catch (AmqpException e) {
            log.error("商品消息发送异常，商品id：{}",spu.getId(), e);
        }

    }

    @Transactional
    public void updateGoods(Spu spu){

        if (spu.getId() == null){
            throw new LyExcetion(ExceptionEnum.GOODS_ID_CANNOT_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skuList = skuMapper.select(sku);
        // 如果以前存在，则删除
        if(!CollectionUtils.isEmpty(skuList)) {
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            // 删除以前库存
            stockMapper.deleteByIdList(ids);
            // 删除以前的sku
            skuMapper.delete(sku);
        }

        // 更新spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1){
            throw new LyExcetion(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        //修改detail
        count = detailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1){
            throw new LyExcetion(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        // 新增spu和stock
        saveSkuAndStock(spu);
        //发送mq消息
        try {
            amqpTemplate.convertAndSend("item-update",spu.getId());
        } catch (AmqpException e) {
            log.error("商品消息发送异常，商品id：{}",spu.getId(), e);
        }

    }

    @Override
    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail spuDetail = detailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null){
            throw new LyExcetion(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    @Override
    public List<Sku> querySkuBySpuId(Long spuId) {
        //查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> list = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(list)){
            throw new LyExcetion(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //查询库存
//        for (Sku s : list) {
//            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
//            if (stock == null){
//                throw new LyExcetion(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
//            }
//            s.setStock(stock.getStock());
//        }
        List<Long> ids = list.stream().map(Sku::getId).collect(Collectors.toList());
        loadStockInSku(list, ids);
        return list;
    }

    /**
     * 查询spu sku detail
     * @param id
     * @return
     */
    @Override
    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu == null){
            throw new LyExcetion(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询sku
        spu.setSkus(querySkuBySpuId(id));
        //detail
        spu.setSpuDetail(queryDetailById(id));
        return spu;
    }

    @Override
    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)){
            throw new LyExcetion(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        loadStockInSku(skus, ids);
        return skus;
    }


    private void saveSkuAndStock(Spu spu) {
        int count;//定义库存集合
        List<Stock> stocks = new ArrayList<>();
        //新增sku
        List<Sku> skuList = spu.getSkus();
        for (Sku sku : skuList){
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);
            if (count != 1){
                throw new LyExcetion(ExceptionEnum.GOODS_SAVE_BAD);
            }

            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stocks.add(stock);
        }
//        stockMapper.insert(stocks);
        //批量新增
        count = stockMapper.insertList(stocks);
        if (count != 1){
            throw new LyExcetion(ExceptionEnum.GOODS_SAVE_BAD);
        }
    }

    /**
     * 减库存
     * @param carts
     */
    @Transactional
    @Override
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cartDto : carts) {
            int count = stockMapper.decreaseStock(cartDto.getSkuId(), cartDto.getNum());
            if (count != 1) {
                throw new LyExcetion(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus){
            //处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            //处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    private void loadStockInSku(List<Sku> list, List<Long> ids) {
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)){
            throw new LyExcetion(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }

        //把stock变为一个map，其key是：sku的id，只是库存值
        Map<Long,Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId,Stock::getStock));
        list.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }

}
