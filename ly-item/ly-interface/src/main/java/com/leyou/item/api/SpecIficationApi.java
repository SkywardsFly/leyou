package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SpecIficationApi {

    @GetMapping("spec/params")
    List<SpecParam> querySpecParamList(
            @RequestParam(name = "gid", required = false)Long gid,
            @RequestParam(name = "cid", required = false)Long cid,
            @RequestParam(name = "searching", required = false)Boolean searching
    );

    @GetMapping("spec/group")
    List<SpecGroup> querySpecGroupByCid(@RequestParam("cid") Long cid);

}
