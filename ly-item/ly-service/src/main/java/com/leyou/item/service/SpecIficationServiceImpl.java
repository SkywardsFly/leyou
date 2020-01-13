package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyExcetion;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.impl.ISpecIficationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpecIficationServiceImpl implements ISpecIficationService {

    @Autowired
    private SpecGroupMapper groupMapper;
    @Autowired
    private SpecParamMapper paramMapper;
    @Override
    public List<SpecGroup> querySpecGroupByCId(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> specGroupList = groupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(specGroupList)){
            throw new LyExcetion(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return specGroupList;
    }

    @Override
    public List<SpecParam> querySpecParamByList(Long gid,Long cid,Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> paramList = paramMapper.select(specParam);
        if (CollectionUtils.isEmpty(paramList)){
            throw new LyExcetion(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return paramList;
    }

    @Override
    public List<SpecGroup> queryListByCId(Long cid) {
        //查询规格组
        List<SpecGroup> specGroups = querySpecGroupByCId(cid);
        //查询组内参数
        List<SpecParam> specParams = querySpecParamByList(null, cid, null);
        //先把规格参数变成map,key作为规格组id，map是组下所有参数
        Map<Long,List<SpecParam>> map =  new HashMap<>();
        for (SpecParam param : specParams) {
            if (!map.containsKey(param.getGroupId())){
                //组id在map不存在就新增list
                map.put(param.getGroupId(),new ArrayList<>());
            }
            map.get(param.getGroupId()).add(param);
        }
        //填充param到group内
        for (SpecGroup specGroup : specGroups){
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
