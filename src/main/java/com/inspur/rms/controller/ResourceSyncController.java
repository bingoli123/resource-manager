package com.inspur.rms.controller;

import com.inspur.rms.rmspojo.DTO.BatchParamDTO;
import com.inspur.rms.rmspojo.DTO.BatchSyncDTO;
import com.inspur.rms.rmspojo.DTO.SyncDTO;
import com.inspur.rms.service.MonitorService;
import com.inspur.rms.service.ResourceSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/11/9 3:09 下午
 * 设备 媒体 平台 名称 经纬度等信息同步
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@RestController
@Slf4j
@RequestMapping("/resource-manager/v3")
public class ResourceSyncController {

    private final ResourceSyncService resourceSyncService;
    private final MonitorService monitorService;

    @Autowired
    public ResourceSyncController(ResourceSyncService resourceSyncService, MonitorService monitorService) {
        this.resourceSyncService = resourceSyncService;
        this.monitorService = monitorService;
    }

    /**
     * 设备列表界面一键同步监控点信息
     */
    @PostMapping("/devices/sync")
    public ResponseEntity<Object> syncDeviceName(@RequestBody @Validated BatchParamDTO batchParamDTO) throws Exception {
        resourceSyncService.deviceSync(batchParamDTO.getUidList());
        return ResponseEntity.status(HttpStatus.OK).body(batchParamDTO);
    }

    /**
     * 媒体列表界面一键同步监控点信息
     */
    @PostMapping("/media/sync")
    public ResponseEntity<Object> syncMediaName(@RequestBody @Validated BatchParamDTO batchParamDTO) throws Exception {
        resourceSyncService.mediaSync(batchParamDTO.getUidList());
        return ResponseEntity.status(HttpStatus.OK).body(batchParamDTO);
    }

    /**
     * 平台详情页面一键同步监控点信息
     */
    @PostMapping("/platforms/sync")
    public ResponseEntity<Object> syncPlatformName(@RequestBody @Validated List<String> platformUids) throws Exception {
        resourceSyncService.platformSync(platformUids.get(0));
        return ResponseEntity.ok(platformUids);
    }

    /**
     * 资源管理中一键同步监控点信息
     */
    @PutMapping("/monitors/sync")
    public ResponseEntity<Object> sync(@RequestBody @Valid BatchSyncDTO batchSyncDTO) throws Exception {
        if (batchSyncDTO.getIsForce() == null) {
            batchSyncDTO.setIsForce(false);
        }
        monitorService.sync(batchSyncDTO, batchSyncDTO.getIsForce());
        return ResponseEntity.ok(batchSyncDTO);
    }

    /**
     * 资源管理中一键同步 目录下所有监控点信息
     */
    @PutMapping("/catelogs/{CatelogUID}/groups/{GroupUID}/monitors/sync")
    public ResponseEntity<Object> syncResourceGroup(@PathVariable("CatelogUID") String catelogUid, @PathVariable("GroupUID") String groupUid, @RequestBody SyncDTO syncDTO) throws Exception {
        if (syncDTO.getIsForce() == null) {
            syncDTO.setIsForce(false);
        }
        resourceSyncService.catelogSync(catelogUid, groupUid, syncDTO.getIsForce());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
