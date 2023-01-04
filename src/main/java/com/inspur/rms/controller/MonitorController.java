package com.inspur.rms.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.inspur.ivideo.common.constant.DeviceProtocolEnum;
import com.inspur.ivideo.common.constant.ResourceTypeEnum;
import com.inspur.ivideo.common.entity.PageData;
import com.inspur.rms.rmspojo.DTO.*;
import com.inspur.rms.rmspojo.PO.Monitor;
import com.inspur.rms.rmspojo.VO.*;
import com.inspur.rms.rmspojo.cmspojo.Media;
import com.inspur.rms.service.MonitorService;
import com.inspur.rms.service.ResourceTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : lidongbin
 * @date : 2021/8/31 5:03 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@RestController
@RequestMapping("/resource-manager/v3")
public class MonitorController {

    private final ResourceTreeService resourceTreeService;
    private final MonitorService monitorService;

    @Autowired
    public MonitorController(ResourceTreeService resourceTreeService, MonitorService monitorService) {
        this.resourceTreeService = resourceTreeService;
        this.monitorService = monitorService;
    }

    /**
     * 监控点详情
     */
    @GetMapping("/monitors/{UID}")
    public ResponseEntity<MonitorSummaryVO> monitorSummary(@PathVariable("UID") String monitorUid) throws Exception {
        Preconditions.checkNotNull(monitorUid);
        MonitorSummaryVO monitorSummaryVO = monitorService.monitorSummary(monitorUid);
        return ResponseEntity.ok(monitorSummaryVO);
    }

    /**
     * 监控点详情
     */
    @PostMapping("/monitors/batch/query")
    public ResponseEntity<List<MonitorSummaryVO>> monitorSummary(@RequestBody MonitorUnInManageDTO monitorUnInManageDTO) throws Exception {
        List<MonitorSummaryVO> monitorSummarys = monitorService.monitorSummary(monitorUnInManageDTO.getUidList());
        return ResponseEntity.ok(monitorSummarys);
    }

    @PostMapping("/monitors/search")
    public ResponseEntity<List<MonitorSummaryVO>> monitorSummary2(@RequestBody MonitorUnInManageDTO monitorUnInManageDTO) throws Exception {
        List<MonitorSummaryVO> monitorSummarys = monitorService.monitorSummary(monitorUnInManageDTO.getUidList());
        return ResponseEntity.ok(monitorSummarys);
    }


    /**
     * 监控点修改
     */
    @PutMapping("/monitors/{UID}")
    public ResponseEntity<Object> updateMonitor(@PathVariable("UID") String monitorUid,
                                                @RequestBody MonitorUpdateDTO monitorUpdateDTO) throws Exception {
        Preconditions.checkNotNull(monitorUid);
        monitorService.updateMonitor(monitorUid, monitorUpdateDTO);
        return ResponseEntity.ok(monitorUpdateDTO);
    }

    /**
     * 资源纳管
     */
    @PutMapping("/resources/inmanage")
    public ResponseEntity<PageData<ResourceUidVO>> resourceInManage(@RequestBody @Valid ResourceInManageDTO resourceInManageDTO) throws Exception {
        Preconditions.checkNotNull(resourceInManageDTO);
        List<Monitor> monitors = Lists.newArrayList();
        if (resourceInManageDTO.getResourceType().equals(ResourceTypeEnum.DEVICE.getResourceType())) {
            DeviceInManageDTO deviceInManageDTO = DeviceInManageDTO.builder()
                    .targetGroupUid(resourceInManageDTO.getParentGroupUid())
                    .importGroup(resourceInManageDTO.isImportGroup())
                    .uidList(resourceInManageDTO.getResourceUidList()).build();
            monitors = monitorService.deviceInManage(deviceInManageDTO);
        } else if (resourceInManageDTO.getResourceType().equals(ResourceTypeEnum.MEDIA.getResourceType())) {
            MediaInManageDTO mediaInManageDTO = MediaInManageDTO.builder().targetGroupUid(resourceInManageDTO.getParentGroupUid())
                    .importGroup(resourceInManageDTO.isImportGroup())
                    .uidList(resourceInManageDTO.getResourceUidList()).build();
            monitors = monitorService.mediaInManage(mediaInManageDTO);
        }
        List<ResourceUidVO> res = Lists.newArrayList();
        for (Monitor s : monitors) {
            ResourceUidVO resourceUidVO = ResourceUidVO.builder().resourceUid(s.getMonitorUid()).build();
            res.add(resourceUidVO);
        }
        return ResponseEntity.ok(new PageData<ResourceUidVO>(res));
    }


    @GetMapping("/inmanage")
    public void inmanage() throws Exception {
        long pageSize = 1000;
        //设备协议
        String deviceProtocol = DeviceProtocolEnum.HIKVISIONSDK.getProtocol();
        //父节点uid
        String resourceParentUid = "4391e16645f3423291e16645f352321c";
        String groupName = "媒体";

        //List<Device> devices = monitorService.queryDevice(deviceProtocol);
        List<Media> medias = monitorService.queryMedia();
        double a = (double) medias.size() / pageSize;
        int b = (int) Math.ceil(a);
        for (int i = 0; i < b; i++) {
            //先创建分组
            ResourceTreeSaveDTO resourceTreeSaveDTO = ResourceTreeSaveDTO.builder().groupName(groupName + i).parentUid(resourceParentUid).build();
            ResourceTreeSaveDTO resourceTreeSaveDTO1 = resourceTreeService.saveResourceTreeCatelog(resourceTreeSaveDTO);
            //再纳管设备
            //List<Device> list1 = devices.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList());
            //List<String> collect = list1.stream().map(Device::getUid).collect(Collectors.toList());
            //DeviceInManageDTO deviceInManageDTO = DeviceInManageDTO.builder().targetGroupUid(resourceTreeSaveDTO1.getGroupUid()).importGroup(false).uidList(collect).build();
            //monitorService.deviceInManage(deviceInManageDTO);
            //纳管媒体
            List<Media> list1 = medias.stream().skip(i * pageSize).limit(pageSize).collect(Collectors.toList());
            List<String> collect = list1.stream().map(Media::getUid).collect(Collectors.toList());
            MediaInManageDTO mediaInManageDTO = MediaInManageDTO.builder().targetGroupUid(resourceTreeSaveDTO1.getGroupUid())
                    .importGroup(false)
                    .uidList(collect).build();
            monitorService.mediaInManage(mediaInManageDTO);
        }
    }

    /**
     * 纳管子资源 设备下的通道或者平台下的设备
     */
    @PutMapping("/subresources/inmanage")
    public ResponseEntity<PageData<SubResourceUidVO>> subresourceInmanage(@RequestBody @Valid SubresourceInManageDTO subresourceInManageDTO) throws Exception {
        List<Monitor> monitors = Lists.newArrayList();
        if (subresourceInManageDTO.getResourceType().equals(ResourceTypeEnum.DEVICE.getResourceType())) {
            DeviceSubresourceInManageDTO deviceSubresourceInManageDTO = DeviceSubresourceInManageDTO.builder()
                    .importGroup(subresourceInManageDTO.isImportGroup())
                    .targetGroupUid(subresourceInManageDTO.getParentGroupUid())
                    .uidList(subresourceInManageDTO.getSubresourceUidList()).build();
            monitors = monitorService.deviceSubresourceInManage(deviceSubresourceInManageDTO, subresourceInManageDTO.getResourceUid());
        } else if (subresourceInManageDTO.getResourceType().equals(ResourceTypeEnum.PLATFORM.getResourceType())) {
            PlatformDeviceInManageDTO platformDeviceInManageDTO = PlatformDeviceInManageDTO.builder()
                    .targetGroupUid(subresourceInManageDTO.getParentGroupUid())
                    .uidList(subresourceInManageDTO.getSubresourceUidList()).build();
            monitors = monitorService.platformDeviceInManage(platformDeviceInManageDTO);
        } else if (subresourceInManageDTO.getResourceType().equals(ResourceTypeEnum.EDGE.getResourceType())) {

        }
        List<SubResourceUidVO> res = Lists.newArrayList();
        for (Monitor s : monitors) {
            SubResourceUidVO resourceUidVO = SubResourceUidVO.builder().subresourceUid(s.getMonitorUid()).build();
            res.add(resourceUidVO);
        }
        return ResponseEntity.ok(new PageData<SubResourceUidVO>(res));
    }


    /**
     * 纳管平台下的目录
     */
    @PutMapping("/groups/inmanage")
    public ResponseEntity<PageData<GroupUidVO>> test(@RequestBody @Valid PlatformFoldInManageDTO platformFoldInManageDTO) throws Exception {
        List<Monitor> monitors = Lists.newArrayList();
        if (platformFoldInManageDTO.isImportGroup()) {
            platformFoldInManageDTO.setUidList(Lists.newArrayList(platformFoldInManageDTO.getGroupUid()));
            monitors = monitorService.platformFoldInManage(platformFoldInManageDTO);
        } else {
//            不导入分组
            monitors = monitorService.platformFoldInManageNotImportGroup(platformFoldInManageDTO);
        }
        List<GroupUidVO> res = Lists.newArrayList();
        for (Monitor s : monitors) {
            GroupUidVO resourceUidVO = GroupUidVO.builder().groupUid(s.getMonitorUid()).build();
            res.add(resourceUidVO);
        }
        return ResponseEntity.ok(new PageData<GroupUidVO>(res));
    }

    /**
     * 取消纳管资源
     */
    @PutMapping("/resources/outmanage")
    public ResponseEntity<PageData<ResourceUidVO>> resourceOutManage(@RequestBody @Valid ResourceOutManageDTO resourceOutManageDTO) throws Exception {
        Preconditions.checkNotNull(resourceOutManageDTO);
        if (resourceOutManageDTO.getResourceType().equals(ResourceTypeEnum.DEVICE.getResourceType())) {
            DeviceUnInManageDTO deviceInManageDTO = DeviceUnInManageDTO.builder().uidList(resourceOutManageDTO.getResourceUidList()).build();
            monitorService.deviceUnInManage(deviceInManageDTO);
        } else if (resourceOutManageDTO.getResourceType().equals(ResourceTypeEnum.MEDIA.getResourceType())) {
            MediaUnInManageDTO mediaUnInManageDTO = MediaUnInManageDTO.builder().uidList(resourceOutManageDTO.getResourceUidList()).build();
            monitorService.mediaUnInManage(mediaUnInManageDTO);
        }
        List<ResourceUidVO> res = Lists.newArrayList();
        for (String s : resourceOutManageDTO.getResourceUidList()) {
            ResourceUidVO resourceUidVO = ResourceUidVO.builder().resourceUid(s).build();
            res.add(resourceUidVO);
        }
        return ResponseEntity.ok(new PageData<ResourceUidVO>(res));
    }

    /**
     * 取消纳管子资源
     */
    @PutMapping("/subresources/outmanage")
    public ResponseEntity<PageData<SubResourceUidVO>> subresourceOutManage(@RequestBody @Valid SubresourceOutManageDTO subresourceOutManageDTO) throws Exception {
        if (subresourceOutManageDTO.getResourceType().equals(ResourceTypeEnum.DEVICE.getResourceType())) {
            monitorService.deviceSubresourceOutManage(subresourceOutManageDTO);
        } else if (subresourceOutManageDTO.getResourceType().equals(ResourceTypeEnum.PLATFORM.getResourceType())) {
            monitorService.platformdeviceOutManage(subresourceOutManageDTO);
        }
        List<SubResourceUidVO> res = Lists.newArrayList();
        for (String s : subresourceOutManageDTO.getSubresourceUidList()) {
            SubResourceUidVO resourceUidVO = SubResourceUidVO.builder().subresourceUid(s).build();
            res.add(resourceUidVO);
        }
        return ResponseEntity.ok(new PageData<SubResourceUidVO>(res));
    }

    /**
     * 平台目录取消纳管
     *
     * @param platformFoldOutManageDTO
     * @return
     * @throws Exception
     */
    @PutMapping("/groups/outmanage")
    public ResponseEntity<Object> groupOutManage(@RequestBody @Valid PlatformFoldOutManageDTO platformFoldOutManageDTO) throws Exception {
        monitorService.platformDeviceFoldeOutManage(platformFoldOutManageDTO);
        return ResponseEntity.ok(null);
    }

    /**
     * 删除监控点（资源管理中脱管)
     */
    @DeleteMapping("/monitors/{UID}")
    public ResponseEntity<Object> monitorUnInManage(@PathVariable("UID") @Valid String monitorUid) throws Exception {
        MonitorUnInManageDTO monitorUnInManageDTO = MonitorUnInManageDTO.builder().uidList(Lists.newArrayList(monitorUid)).build();
        monitorService.monitorUnInManage(monitorUnInManageDTO);
        return ResponseEntity.ok(null);
    }

    /**
     * 批量删除监控点（资源管理中脱管）
     */
    @PostMapping("/monitors/batch/delete")
    public ResponseEntity<Object> monitorUnInManageBatch(@RequestBody MonitorUnInManageDTO monitorUnInManageDTO) throws Exception {
        if (null == monitorUnInManageDTO.getUidList() || monitorUnInManageDTO.getUidList().isEmpty()) {
            throw new NullPointerException();
        }
        monitorService.monitorUnInManage(monitorUnInManageDTO);
        return ResponseEntity.ok(null);
    }

    /**
     * 根据一组监控点UID 查询监控点的在线状态
     */
    @PostMapping("/monitors/online")
    public ResponseEntity<List<MonitorOnlineVO>> getMonitorOnlineStatus(@RequestBody @Valid BatchParamDTO batchParamDTO) throws Exception {
        List<MonitorOnlineVO> monitorOnline = monitorService.getMonitorOnline(batchParamDTO.getUidList());
        return ResponseEntity.ok(monitorOnline);
    }

    /**
     * 校验同一级目录下 监控点的名称是否重复
     */
    @GetMapping("/monitornodes/name/validation")
    public ResponseEntity<Object> groupNameValidation(@RequestParam("Name") String groupName,
                                                      @RequestParam("UID") String monitorUid) throws Exception {
        HashMap hashMap = monitorService.monitorNodeNameValidation(groupName, monitorUid);
        return ResponseEntity.ok(hashMap);
    }

    /**
     * 根据一组监控点UID 查询监控点的在线状态
     * status 0返回没有此任务的监控点集合 1返回列表中存在任务的监控点集合
     */
    @PostMapping("/monitors/enabledcapabilities/{UID}")
    public ResponseEntity<List<String>> getMonitorAlgorithmStatus(@RequestBody @Validated BatchParamDTO2 batchParamDTO, @PathVariable("UID") @Valid String algorithmUid) throws Exception {
        List<String> monitorUids = monitorService.getMonitorsByEvent(batchParamDTO.getUidList(), algorithmUid, batchParamDTO.getStatus());
        return ResponseEntity.ok(monitorUids);
    }

    /**
     * 修改监控点经纬度信息
     */
    @PatchMapping("/monitors/{UID}/coordinate")
    public ResponseEntity<Object> updateMonitorCoordinate(@PathVariable(name = "UID") String monitorUid, @RequestBody UpdateMonitorCoordinateDTO updateMonitorCoordinateDTO) throws Exception {
        monitorService.updateMonitorCoordinate(monitorUid, updateMonitorCoordinateDTO);
        return null;
    }


    /**
     * 批量修改监控点经纬度信息
     */
    @PostMapping("/monitors/coordinate/batch/update")
    public ResponseEntity<PageData<UpdateMonitorCoordinateDTO>> updateMonitorCoordinateBatch(@RequestBody List<UpdateMonitorCoordinateDTO> list) throws Exception {
        PageData<UpdateMonitorCoordinateDTO> pageData = monitorService.updateMonitorCoordinateBatch(list);
        return ResponseEntity.ok(pageData);
    }


    /**
     * 存储任务使用接口 查询选中的分组下的监控点数量 排除重复的监控点和已选中的监控点
     *
     * @param groupMonitorNum
     * @return
     * @throws Exception
     */
    @PostMapping("/groups/monitor-number")
    public ResponseEntity<ValueIntVO> getGroupMonitoeNumber(@Validated @RequestBody GroupMonitorNumDTO groupMonitorNum) throws Exception {
        int groupMonitoeNumber = monitorService.getGroupMonitoeNumber(groupMonitorNum);
        return ResponseEntity.ok(ValueIntVO.builder().value(groupMonitoeNumber).build());
    }
}
