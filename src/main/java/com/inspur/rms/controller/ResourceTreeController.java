package com.inspur.rms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.ivideo.common.constant.TrueFalse;
import com.inspur.ivideo.common.entity.PageData;
import com.inspur.rms.rmspojo.DTO.MonitorBatchMoveDTO;
import com.inspur.rms.rmspojo.DTO.MonitorNodeCopyDTO;
import com.inspur.rms.rmspojo.DTO.ResourceTreeSaveDTO;
import com.inspur.rms.rmspojo.DTO.ResourceTreeUpdateDTO;
import com.inspur.rms.rmspojo.VO.*;
import com.inspur.rms.rmspojo.cmspojo.BatchDeleteDTO;
import com.inspur.rms.service.ResourceTreeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/8/17 10:42 上午
 * 资源树controller
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping("/resource-manager/v3")
public class ResourceTreeController {

    private final ResourceTreeService resourceTreeService;

    @Autowired
    public ResourceTreeController(ResourceTreeService resourceTreeService) {
        this.resourceTreeService = resourceTreeService;
    }


    /**
     * 查询资源目录树
     */
    @GetMapping("catelogs/{UID}/groups")
    public ResponseEntity<List<ResourceTreeCatelogVO>> queryList(@PathVariable("UID") String treeUid) {
        Preconditions.checkNotNull(treeUid);
        List<ResourceTreeCatelogVO> resourceTreeListDTOs = resourceTreeService.getCatelog(treeUid);
        return ResponseEntity.ok(resourceTreeListDTOs);
    }

    /**
     * 创建资源目录节点
     */
    @PostMapping("/catelogs/{CatelogUID}/groups")
    public ResponseEntity<Object> createCatelog(
            @PathVariable("CatelogUID") String ca,
            @RequestBody @Validated ResourceTreeSaveDTO resourceTreeSaveDTO) throws Exception {
        ResourceTreeSaveDTO resourceTreeSaveDTO1 = resourceTreeService.saveResourceTreeCatelog(resourceTreeSaveDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceTreeSaveDTO1);
    }


    /**
     * 查询节点详情
     */
    @GetMapping("/groups/{UID}")
    public ResponseEntity<ResourceTreeGroupSummaryVO> getResourceSummary(@PathVariable("UID") String groupUid) throws Exception {
        ResourceTreeGroupSummaryVO groupSummary = resourceTreeService.getGroupSummary(groupUid);
        return ResponseEntity.ok(groupSummary);
    }

    /**
     * 修改资源目录节点
     */
    @PatchMapping("/catelogs/{CatelogUID}/groups/{UID}")
    public ResponseEntity<Object> updateResource(@PathVariable("UID") String groupUid, @PathVariable("CatelogUID") String catelogUid, @RequestBody ResourceTreeUpdateDTO resourceTreeUpdateDTO) throws Exception {
        Preconditions.checkNotNull(groupUid);
        resourceTreeService.updateResourceTreeCatelog(resourceTreeUpdateDTO, groupUid);
        return ResponseEntity.ok(resourceTreeUpdateDTO);
    }

    /**
     * 删除资源目录节点
     */

    @DeleteMapping("/catelogs/{CatelogUID}/groups/{UID}")
    public ResponseEntity<Object> deleteResource(@PathVariable("UID") @NotBlank(message = "节点不能为空") String groupUid,
                                                 @PathVariable("CatelogUID") @NotBlank(message = "分组不能为空") String catelogUid) throws Exception {
        Preconditions.checkNotNull(groupUid);
        Preconditions.checkNotNull(catelogUid);
        resourceTreeService.deleteResourceTree(catelogUid, groupUid);
        return ResponseEntity.ok(catelogUid);
    }

    /**
     * 校验节点名称是否可用
     */
    @GetMapping("/groups/name/validation")
    public ResponseEntity<Object> groupNameValidation(@RequestParam("Name") String groupName,
                                                      @RequestParam(value = "ParentUID", required = false) String groupUid) throws Exception {
        if (StringUtils.isBlank(groupUid)) {
            HashMap<Object, Object> hashMap = Maps.newHashMap();
            hashMap.put("Available", TrueFalse.TRUE.isValue());
            return ResponseEntity.ok(hashMap);
        }
        HashMap hashMap = resourceTreeService.groupNameValidation(groupName, groupUid);
        return ResponseEntity.ok(hashMap);
    }

    /**
     * 根据树id查询该树的跟节点
     */
    @GetMapping("/groups/{UID}/root")
    public ResponseEntity<ResourceTreeCatelogVO> getTreeRoot(@PathVariable("UID") String treeUid) {
        ResourceTreeCatelogVO treeRoot = resourceTreeService.getTreeRoot(treeUid);
        return ResponseEntity.ok(treeRoot);
    }

    /**
     * 查询下级资源 监控点
     *
     * @param recursion          是否递归查询 False不递归 True递归
     * @param ablityType         查询的能力类型，如
     *                           空值表示不返回任何能力相关状态，比如资源管理查询，
     *                           Video 表示查询监控点录像状态
     *                           Picture 表示查询监控点图片状态
     * @param ablityConfigStatus 能力配置状态
     * @param ablityWorkStatus   能力工作状态
     */
    @GetMapping("/monitors")
    public ResponseEntity<PageData<ResourceTreeMonitorVO>> getChilds(
            @RequestParam("GroupUID") String groupUid,
            @RequestParam("CatelogUID") String catelogUid,
            @RequestParam(value = "Name", required = false) String monitorName,
            @RequestParam(value = "OnlineCode", required = false) Integer online,
            @RequestParam("Recursion") Boolean recursion,
            @RequestParam(value = "AblityType", required = false) String ablityType,
            @RequestParam(value = "AblityConfigStatus", required = false) Boolean ablityConfigStatus,
            @RequestParam(value = "AblityWorkStatus", required = false) String ablityWorkStatus,
            @RequestParam("Page") Integer page,
            @RequestParam("PerPage") Integer perpage) throws Exception {
        if (StringUtils.isNotBlank(monitorName)) {
            monitorName = URLDecoder.decode(monitorName.replace("%", "%25"), "utf-8").replace("%", "\\%").replace("_", "\\_").replace("\\", "\\\\");
        }
        PageData<ResourceTreeMonitorVO> childs = resourceTreeService.getChilds(page, perpage, groupUid, monitorName, recursion, catelogUid, online, ablityType, ablityConfigStatus, ablityWorkStatus);
        return ResponseEntity.ok(childs);
    }

    /**
     * 监控点节点复制 从基本资源树复制设备到虚拟资源树
     */
    @PutMapping("/monitornodes/duplication")
    public ResponseEntity<Object> duplication(@RequestBody @Valid MonitorNodeCopyDTO monitorNodeCopyDTO) throws Exception {
        resourceTreeService.resourceCopy(monitorNodeCopyDTO);
        return ResponseEntity.ok(monitorNodeCopyDTO);
    }

    /**
     * 监控点节点删除
     *
     * @param monitorNodeUid 监控点节点UID resourceTree表中的groupUID
     */
    @DeleteMapping("/monitornodes/{UID}")
    public ResponseEntity<Object> deleteMonitorNode(@PathVariable("UID") String monitorNodeUid) throws Exception {
        resourceTreeService.deleteMonitorNode(monitorNodeUid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    /**
     * 监控点节点批量删除
     *
     * @param batchDeleteDTO 监控点节点UID resourceTree表中的groupUID
     */
    @PostMapping("/monitornodes/batch/delete")
    public ResponseEntity<Object> deleteMonitorNode(@RequestBody @Valid BatchDeleteDTO batchDeleteDTO) throws Exception {
        Preconditions.checkNotNull(batchDeleteDTO);
        Preconditions.checkNotNull(batchDeleteDTO.getUidList());
        resourceTreeService.deleteMonitorNode(batchDeleteDTO.getUidList());
        return ResponseEntity.ok(null);
    }

    /**
     * 批量监控点移动
     *
     * @param
     */
    @PostMapping("/monitornodes/parentuid/batch/update")
    public ResponseEntity<Object> monitorMove(@RequestBody MonitorBatchMoveDTO monitorBatchMoveDTO) throws Exception {
        resourceTreeService.monitorMove(monitorBatchMoveDTO);
        return ResponseEntity.status(HttpStatus.OK).body(monitorBatchMoveDTO);
    }

    /**
     * 根据角色id查询用户资源权限uid
     */
    @GetMapping("/roles/{RoleUID}/groups")
    public List<String> rolesGroups(@PathVariable("RoleUID") String roleUid) throws Exception {
        return resourceTreeService.rolesGroups(roleUid);
    }

    @PostMapping("/monitornodes/search")
    public PageData<MonitorOnlineGroupNameVO> getMonitorOnlineAndGroupName(@RequestBody List<String> monitorUids) throws Exception {
        if (null == monitorUids || monitorUids.isEmpty()) {
            return new PageData<>(Lists.newArrayList());
        }
        List<MonitorOnlineGroupNameVO> monitorOnlineAndGroupName = resourceTreeService.getMonitorOnlineAndGroupName(monitorUids);
        if (monitorOnlineAndGroupName != null) {
            monitorOnlineAndGroupName.stream().forEach(s -> {
                OnlineVO onlineVO = OnlineVO.builder().code(s.getOnline2()).message(s.getMessage()).build();
                s.setOnline(onlineVO);
            });
            return new PageData<>(monitorOnlineAndGroupName);
        } else {
            return new PageData<>(Lists.newArrayList());
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        MonitorOnlineGroupNameVO2 monitorOnlineGroupNameVO2 = new MonitorOnlineGroupNameVO2();
        monitorOnlineGroupNameVO2.setOnline(OnlineVO.builder().code("1").message("13213132").build());
        monitorOnlineGroupNameVO2.setMonitorUid("12332");
        monitorOnlineGroupNameVO2.setGroupName("132321");
        monitorOnlineGroupNameVO2.setOnline2("2");
        monitorOnlineGroupNameVO2.setMessage("12313123");
        monitorOnlineGroupNameVO2.setDate(new Date());
        String s = new ObjectMapper().writeValueAsString(monitorOnlineGroupNameVO2);
        System.out.println(s);
        String a = "{\"UID\":\"12332\",\"GroupName\":\"132321\",\"Message\":\"12313123\",\"Date\":\"2022-05-27 09:28:59\",\"sadasda\":\"12313123\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MonitorOnlineGroupNameVO2 monitorOnlineGroupNameVO21 = objectMapper.readValue(a, MonitorOnlineGroupNameVO2.class);
        System.out.println("12313123");

    }

    @GetMapping("catelogs/{UID}/groups/httppull")
    public ResponseEntity queryList2(@PathVariable("UID") String treeUid) {
        Preconditions.checkNotNull(treeUid);
        List<ResourceTreeCatelogVO> resourceTreeListDTOs = resourceTreeService.getCatelog(treeUid);
        HashMap map = new HashMap();
        map.put("ID", Lists.newArrayList(5, 6, 7, 8, 14));
        map.put("ParentID", Lists.newArrayList(1, 2, 3, 3, 4));
        map.put("Name", Lists.newArrayList("name1", "name2", "name3", "name4", "name5"));
        map.put("Path", Lists.newArrayList("name1", "name2", "name3", "name4", "name5"));
        map.put("UID", Lists.newArrayList("name1", "name2", "name3", "name4", "name5"));
        map.put("MonitorOnlineNum", Lists.newArrayList(1, 2, 3, 3, 4));
        map.put("MonitorNum", Lists.newArrayList(1, 2, 3, 3, 4));
        //map.put("meta", resourceTreeListDTOs);
        return ResponseEntity.ok(map);
    }

    @GetMapping("catelogs/{UID}/groups2/httppull")
    public ResponseEntity<ResourceTreeCatelogVO> queryList3(@PathVariable("UID") String treeUid) {
        Preconditions.checkNotNull(treeUid);
        List<ResourceTreeCatelogVO> resourceTreeListDTOs = resourceTreeService.getCatelog(treeUid);
        return ResponseEntity.ok(resourceTreeListDTOs.get(1));
    }

}
