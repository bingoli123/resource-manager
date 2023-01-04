package com.inspur.rms.controller;

import com.google.common.base.Preconditions;
import com.inspur.ivideo.common.exception.Res400Exception;
import com.inspur.ivideo.common.exception.ResException;
import com.inspur.rms.constant.RmsResStatusEnum;
import com.inspur.rms.rmspojo.DTO.EnableTreeDTO;
import com.inspur.rms.rmspojo.DTO.TreeSaveDTO;
import com.inspur.rms.rmspojo.DTO.TreeUpdateDTO;
import com.inspur.rms.rmspojo.PO.TreeList;
import com.inspur.rms.rmspojo.VO.TreeListVO;
import com.inspur.rms.rmspojo.VO.TreeSummaryVO;
import com.inspur.rms.service.TreeListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/8/13 10:33 上午
 * 虚拟资源目录管理
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping("/resource-manager/v3")
public class TreeListController {

    private final TreeListService treeListService;

    @Autowired
    public TreeListController(TreeListService treeListService) {
        this.treeListService = treeListService;
    }

    /**
     * @data 查询虚拟资源树列表
     */
    @GetMapping("/catelogs")
    public ResponseEntity<List<TreeListVO>> treeList(@RequestParam(value = "Type", required = false) String type) {
        List<TreeListVO> byAll = treeListService.findByAll(TreeList.builder().treeType(type).build());
        return ResponseEntity.ok(byAll);
    }


    /**
     * @data 查询虚拟资源详情
     */
    @GetMapping("/catelogs/{UID}")
    public ResponseEntity<TreeSummaryVO> queryTreeSummary(@PathVariable("UID") String treeUid) throws ResException, Res400Exception {
        TreeSummaryVO summaryR = treeListService.queryTreeSummary(treeUid);
        return ResponseEntity.status(HttpStatus.OK).body(summaryR);
    }

    /**
     * @data 新增虚拟资源树
     */
    @PostMapping("/catelogs")
    public ResponseEntity<Object> saveTree(@RequestBody @Valid TreeSaveDTO treeSaveDTO) throws Exception {
        Preconditions.checkNotNull(treeSaveDTO);
        Preconditions.checkNotNull(treeSaveDTO.getTreeName());
        TreeList treeList = new TreeList();
        BeanUtils.copyProperties(treeSaveDTO, treeList);
        treeListService.saveTree(treeList);
        return ResponseEntity.status(HttpStatus.CREATED).body(treeSaveDTO);

    }

    /**
     * @data 编辑虚拟资源树
     */
    @PutMapping("/catelogs/{UID}")
    public ResponseEntity<Object> updateTree(@PathVariable("UID") String treeUid, @RequestBody @Valid TreeUpdateDTO treeUpdateDTO) throws Exception {
        Preconditions.checkNotNull(treeUpdateDTO);
        Preconditions.checkNotNull(treeUpdateDTO.getTreeName());
        treeListService.updateTree(treeUid, treeUpdateDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    /**
     * @data 删除虚拟资源树
     */
    @DeleteMapping("/catelogs/{UID}")
    public ResponseEntity<Object> deleteTree(@PathVariable("UID") String treeUid) throws Res400Exception {
        treeListService.deleteTree(treeUid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    /**
     * @data 禁用启用禁用虚拟资源树 true启用 false禁用
     */
    @PutMapping("/catelogs/{UID}/status")
    public ResponseEntity<Object> treeStatus(@PathVariable("UID") String treeUid, @RequestBody @Valid EnableTreeDTO enableTreeDTO) throws Exception {
        if (enableTreeDTO.getValue() == null) {
            throw new Res400Exception(RmsResStatusEnum.VALUE_IS_NULL);
        }
        treeListService.treeStatus(treeUid, enableTreeDTO);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    /**
     * 校验节点名称是否可用
     */
    @GetMapping("/catelogs/name/validation")
    public ResponseEntity<Object> groupNameValidation(@RequestParam("Name") String groupName) throws Exception {
        HashMap hashMap = treeListService.catelogNameValidation(groupName);
        return ResponseEntity.ok(hashMap);
    }

}
