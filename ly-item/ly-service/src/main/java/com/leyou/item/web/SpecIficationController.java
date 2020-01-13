package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.impl.ISpecIficationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecIficationController {

    @Autowired
    private ISpecIficationService specIficationService;

    /**
     *
     * @param cid
     * @return
     */
    @GetMapping("group/{id}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupByCId(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(specIficationService.querySpecGroupByCId(cid));
    }

    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParamByGid(@RequestParam(name = "gid", required = false)Long gid,
                                                               @RequestParam(name = "cid", required = false)Long cid,
                                                               @RequestParam(name = "searching", required = false)Boolean searching){
        return ResponseEntity.ok(specIficationService.querySpecParamByList(gid,cid,searching));
    }



    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specIficationService.queryListByCId(cid));
    }

}
