package com.leyou.item.service.impl;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface ISpecIficationService {
    /**
     *
     * @param cid
     * @return
     */
    List<SpecGroup> querySpecGroupByCId(Long cid);

    /**
     *
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    List<SpecParam> querySpecParamByList(Long gid,Long cid,Boolean searching);

    List<SpecGroup> queryListByCId(Long cid);
}
