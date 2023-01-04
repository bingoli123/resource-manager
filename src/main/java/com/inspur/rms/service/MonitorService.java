package com.inspur.rms.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dtflys.forest.http.ForestResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.ivideo.common.constant.*;
import com.inspur.ivideo.common.entity.PageData;
import com.inspur.ivideo.common.entity.ResStatus;
import com.inspur.ivideo.common.exception.Res400Exception;
import com.inspur.ivideo.common.utils.JsonUtils;
import com.inspur.ivideo.common.utils.KeyUtils;
import com.inspur.rms.api.ConnectCoordinatorApi;
import com.inspur.rms.api.ConnectionManagerInnerApi;
import com.inspur.rms.constant.MonitorMqSubject;
import com.inspur.rms.constant.RmsResStatusEnum;
import com.inspur.rms.dao.MonitorEventRelMapper;
import com.inspur.rms.dao.MonitorMapper;
import com.inspur.rms.dao.ResourceTreeMapper;
import com.inspur.rms.dao.TreeListMapper;
import com.inspur.rms.mapstruct.MonitorToMonitorOnlineVoStruct;
import com.inspur.rms.rmspojo.DTO.*;
import com.inspur.rms.rmspojo.PO.Monitor;
import com.inspur.rms.rmspojo.PO.MonitorEventRel;
import com.inspur.rms.rmspojo.PO.ResourceTree;
import com.inspur.rms.rmspojo.PO.TreeList;
import com.inspur.rms.rmspojo.VO.MonitorOnlineVO;
import com.inspur.rms.rmspojo.VO.MonitorSummaryVO;
import com.inspur.rms.rmspojo.VO.OnlineVO;
import com.inspur.rms.rmspojo.VO.RelationResourceVO;
import com.inspur.rms.rmspojo.cmspojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MonitorService extends ServiceImpl<MonitorMapper, Monitor> {

    private final ResourceTreeMapper resourceTreeMapper;

    private final MonitorMapper monitorMapper;

    private final TreeListMapper treeListMapper;

    private final MonitorToMonitorOnlineVoStruct monitorToMonitorOnlineVoStruct;
    private final ConnectionManagerInnerApi connectionManagerInnerApi;
    private final ConnectCoordinatorApi connectCoordinatorApi;
    private final MonitorEventRelMapper monitorEventRelMapper;
    private final RabbitMqSendService rabbitMqSendService;
    private final JsonUtils jsonUtils;
    private final ObjectMapper objectMapper;

    @Autowired
    public MonitorService(ResourceTreeMapper resourceTreeMapper, MonitorMapper monitorMapper, TreeListMapper treeListMapper, MonitorToMonitorOnlineVoStruct monitorToMonitorOnlineVoStruct, ConnectionManagerInnerApi connectionManagerInnerApi, ConnectCoordinatorApi connectCoordinatorApi, MonitorEventRelMapper monitorEventRelMapper, RabbitMqSendService rabbitMqSendService, JsonUtils jsonUtils, ObjectMapper objectMapper) {
        this.resourceTreeMapper = resourceTreeMapper;
        this.monitorMapper = monitorMapper;
        this.treeListMapper = treeListMapper;
        this.monitorToMonitorOnlineVoStruct = monitorToMonitorOnlineVoStruct;
        this.connectionManagerInnerApi = connectionManagerInnerApi;
        this.connectCoordinatorApi = connectCoordinatorApi;
        this.monitorEventRelMapper = monitorEventRelMapper;
        this.rabbitMqSendService = rabbitMqSendService;
        this.jsonUtils = jsonUtils;
        this.objectMapper = objectMapper;
    }

    public List<Monitor> findByAll(Monitor monitor) {
        return baseMapper.findByAll(monitor);
    }

    public int batchInsert(List<Monitor> list) {
        return baseMapper.batchInsert(list);
    }

    /**
     * 监控点详情
     */
    public MonitorSummaryVO monitorSummary(String monitorUid) throws Exception {
        //monitorUid可能为resourcetree表中的groupUid 或者 monitorUid
        Monitor monitor = monitorMapper.selectOne(Wrappers.<Monitor>lambdaQuery().eq(Monitor::getMonitorUid, monitorUid));
        if (monitor == null) {
            ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, monitorUid));
            if (resourceTree != null) {
                monitor = monitorMapper.selectOne(Wrappers.<Monitor>lambdaQuery().eq(Monitor::getMonitorUid, resourceTree.getMonitorUid()));
            }
        }
        MonitorSummaryVO monitorSummaryVO = new MonitorSummaryVO();
        BeanUtils.copyProperties(monitor, monitorSummaryVO);
        OnlineVO onlineVO = OnlineVO.builder().code(monitor.getOnline()).build();
        monitorSummaryVO.setOnline2(onlineVO);
        monitorSummaryVO.setEnabledCapabilities(Lists.newArrayList(Optional.ofNullable(monitor.getEnabledCapabilities()).orElse("").split(",")).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        monitorSummaryVO.setNativeCapabilities(Lists.newArrayList(Optional.ofNullable(monitor.getNativeCapabilities()).orElse("").split(",")).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        if (monitorSummaryVO.getResourceType().equals(ResourceTypeEnum.DEVICE.getResourceType())) {
            ForestResponse<List<Subresource>> response1 = connectionManagerInnerApi.getSubresourceAllList(Lists.newArrayList(monitorSummaryVO.getBusinessUid()));
            if (!response1.isSuccess()) {
                return monitorSummaryVO;
            }
            List<Subresource> result1 = response1.getResult();
            if (null == result1 || result1.isEmpty()) {
                monitorSummaryVO.setRelationResourceVO(new RelationResourceVO());
                return monitorSummaryVO;
            }
            Subresource subresource = result1.get(0);
            RelationResourceVO relationResource = new RelationResourceVO();
            relationResource.setSubresourceName(subresource.getName());
            relationResource.setSubresourceUid(subresource.getUid());
            relationResource.setOwnerResourceType(monitorSummaryVO.getResourceType());

            ForestResponse<List<Device>> response2 = connectionManagerInnerApi.getDevice(Lists.newArrayList(subresource.getOwnerResource()));
            if (!response2.isSuccess()) {
                return monitorSummaryVO;
            }
            List<Device> result = response2.getResult();
            if (null == result || result.isEmpty()) {
                return monitorSummaryVO;
            }
            Device device = result.get(0);
            relationResource.setProtocol(device.getProtocol());
            relationResource.setOwnerResourceName(device.getName());
            relationResource.setOwnerResourceUid(device.getUid());
            if (device.getProtocol().equals(DeviceProtocolEnum.GB28181.getProtocol())) {
                relationResource.setDeviceId(device.getDeviceId());
            } else {
                relationResource.setIpAddr(device.getIpAddr());
                relationResource.setPort(device.getPort());
            }
            monitorSummaryVO.setRelationResourceVO(relationResource);
        } else if (monitorSummaryVO.getResourceType().equals(ResourceTypeEnum.MEDIA.getResourceType())) {
            ForestResponse<List<Subresource>> response1 = connectionManagerInnerApi.getSubresourceAllList(Lists.newArrayList(monitorSummaryVO.getBusinessUid()));
            if (!response1.isSuccess()) {
                return monitorSummaryVO;
            }
            List<Subresource> result = response1.getResult();
            if (result == null || result.isEmpty()) {
                monitorSummaryVO.setRelationResourceVO(new RelationResourceVO());
                return monitorSummaryVO;
            }
            Subresource subresource = result.get(0);
            RelationResourceVO relationResource = new RelationResourceVO();
            relationResource.setSubresourceName(subresource.getName());
            relationResource.setSubresourceUid(subresource.getUid());
            relationResource.setOwnerResourceType(monitorSummaryVO.getResourceType());
            ForestResponse<List<Media>> response = connectionManagerInnerApi.getMedia(Lists.newArrayList(subresource.getOwnerResource()));
            if (!response.isSuccess()) {
                return monitorSummaryVO;
            }
            Media media = Optional.ofNullable(response.getResult()).orElse(Lists.newArrayList()).get(0);
            if (media == null) {
                monitorSummaryVO.setRelationResourceVO(relationResource);
                return monitorSummaryVO;
            }
            relationResource.setProtocol(media.getProtocol());
            relationResource.setUrl(media.getUrl());
            relationResource.setOwnerResourceName(media.getName());
            relationResource.setOwnerResourceUid(media.getUid());
            relationResource.setProtocol(media.getProtocol());
            relationResource.setUrl(media.getUrl());
            monitorSummaryVO.setRelationResourceVO(relationResource);
        } else if (monitorSummaryVO.getResourceType().equals(ResourceTypeEnum.PLATFORM.getResourceType())) {
            ForestResponse<List<PlatformDevice>> response = connectionManagerInnerApi.getPlatformedevice(Lists.newArrayList(monitorSummaryVO.getBusinessUid()));
            if (!response.isSuccess()) {
                return monitorSummaryVO;
            }
            List<PlatformDevice> result = response.getResult();
            if (result == null || result.isEmpty()) {
                monitorSummaryVO.setRelationResourceVO(new RelationResourceVO());
                return monitorSummaryVO;
            }
            PlatformDevice platformDevice = result.get(0);
            RelationResourceVO relationResource = new RelationResourceVO();
            relationResource.setSubresourceName(platformDevice.getGroupName());
            relationResource.setSubresourceUid(platformDevice.getGroupUid());
            relationResource.setOwnerResourceType(monitorSummaryVO.getResourceType());
            ForestResponse<Platform> response1 = connectionManagerInnerApi.getPlatform(platformDevice.getOwnerResource());
            if (!response1.isSuccess()) {
                return monitorSummaryVO;
            }
            Platform platform = response1.getResult();
            if (platform == null) {
                return monitorSummaryVO;
            }
            if (platform.getProtocol().equals(PlatformProtocolEnum.GB28181.getProtocol())) {
                relationResource.setProtocol(platform.getProtocol());
                relationResource.setDeviceId(platformDevice.getDeviceId());
            } else {
                //    todo 浪潮连接设备有需求之后再判断
            }
            relationResource.setOwnerResourceName(platform.getName());
            relationResource.setOwnerResourceUid(platform.getUid());
            monitorSummaryVO.setRelationResourceVO(relationResource);
        }
        return monitorSummaryVO;
    }

    /**
     * 批量查询监控点详情
     */
    public List<MonitorSummaryVO> monitorSummary(List<String> monitorUids) throws Exception {
        if (monitorUids.isEmpty()) {
            return Lists.newArrayList();
        }
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getMonitorUid, monitorUids));
        if (monitors == null || monitors.isEmpty()) {
            return Lists.newArrayList();
        }
        List<MonitorSummaryVO> monitorSummarys = Lists.newArrayList();
        for (Monitor monitor : monitors) {
            MonitorSummaryVO monitorSummaryVO = new MonitorSummaryVO();
            BeanUtils.copyProperties(monitor, monitorSummaryVO);
            OnlineVO onlineVO = OnlineVO.builder().code(monitor.getOnline()).build();
            monitorSummaryVO.setOnline2(onlineVO);
            monitorSummaryVO.setEnabledCapabilities(Lists.newArrayList(Optional.ofNullable(monitor.getEnabledCapabilities()).orElse("").split(",")).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
            monitorSummaryVO.setNativeCapabilities(Lists.newArrayList(Optional.ofNullable(monitor.getNativeCapabilities()).orElse("").split(",")).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
            monitorSummarys.add(monitorSummaryVO);
        }

        return monitorSummarys;
    }

    /**
     * 监控点修改
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMonitor(String monitorUid, MonitorUpdateDTO monitorUpdateDTO) throws Exception {
        Monitor monitor = monitorMapper.selectOne(Wrappers.<Monitor>lambdaQuery().eq(Monitor::getMonitorUid, monitorUid));
        Optional.ofNullable(monitor).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_FAIL));
        //判断监控点名称是否修改 如果修改了则将名称修改状态设置为true 并且修改resourcetree表的名称
        if (!monitor.getMonitorName().equals(monitorUpdateDTO.getMonitorName())) {
//            如果名称修改了则修改resourcetree表的所有关联的节点
            monitor.setMonitorNameUpdate(Boolean.TRUE);
            resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().set(ResourceTree::getGroupName, monitorUpdateDTO.getMonitorName()).eq(ResourceTree::getMonitorUid, monitor.getMonitorUid()));
        }
        if (monitorUpdateDTO.getLongitude() == null && monitor.getLongitude() == null) {
            monitor.setLongitudeUpdate(Boolean.FALSE);
        } else if (monitorUpdateDTO.getLongitude() != null && monitor.getLongitude() == null) {
            monitor.setLongitudeUpdate(Boolean.TRUE);
        } else if (monitorUpdateDTO.getLongitude() == null && monitor.getLongitude() != null) {
            monitor.setLongitudeUpdate(Boolean.TRUE);
        } else {
            if (!monitorUpdateDTO.getLongitude().equals(monitor.getLongitude())) {
                monitor.setLongitudeUpdate(Boolean.TRUE);
            }
        }

        if (monitorUpdateDTO.getLatitude() == null && monitor.getLatitude() == null) {
            monitor.setLatitudeUpdate(Boolean.FALSE);
        } else if (monitorUpdateDTO.getLatitude() != null && monitor.getLatitude() == null) {
            monitor.setLatitudeUpdate(Boolean.TRUE);
        } else if (monitorUpdateDTO.getLatitude() == null && monitor.getLatitude() != null) {
            monitor.setLatitudeUpdate(Boolean.TRUE);
        } else {
            if (!monitorUpdateDTO.getLatitude().equals(monitor.getLatitude())) {
                monitor.setLatitudeUpdate(Boolean.TRUE);
            }
        }

        if (monitorUpdateDTO.getAltitude() == null && monitor.getAltitude() == null) {
            monitor.setAltitudeUpdate(Boolean.FALSE);
        } else if (monitorUpdateDTO.getAltitude() != null && monitor.getAltitude() == null) {
            monitor.setAltitudeUpdate(Boolean.TRUE);
        } else if (monitorUpdateDTO.getAltitude() == null && monitor.getAltitude() != null) {
            monitor.setAltitudeUpdate(Boolean.TRUE);
        } else {
            if (!monitorUpdateDTO.getAltitude().equals(monitor.getAltitude())) {
                monitor.setAltitudeUpdate(Boolean.TRUE);
            }
        }

        if (monitorUpdateDTO.getPlaceCode() == null && monitor.getPlaceCode() == null) {
            monitor.setPlaceCodeUpdate(Boolean.FALSE);
        } else if (monitorUpdateDTO.getPlaceCode() != null && monitor.getPlaceCode() == null) {
            monitor.setPlaceCodeUpdate(Boolean.TRUE);
        } else if (monitorUpdateDTO.getPlaceCode() == null && monitor.getPlaceCode() != null) {
            monitor.setPlaceCodeUpdate(Boolean.TRUE);
        } else {
            if (!monitorUpdateDTO.getPlaceCode().equals(monitor.getPlaceCode())) {
                monitor.setPlaceCodeUpdate(Boolean.TRUE);
            }
        }

        if (monitorUpdateDTO.getFullAddress() == null && monitor.getFullAddress() == null) {
            monitor.setFullAddressUpdate(Boolean.FALSE);
        } else if (monitorUpdateDTO.getFullAddress() != null && monitor.getFullAddress() == null) {
            monitor.setFullAddressUpdate(Boolean.TRUE);
        } else if (monitorUpdateDTO.getFullAddress() == null && monitor.getFullAddress() != null) {
            monitor.setFullAddressUpdate(Boolean.TRUE);
        } else {
            if (!monitorUpdateDTO.getFullAddress().equals(monitor.getFullAddress())) {
                monitor.setFullAddressUpdate(Boolean.TRUE);
            }
        }

        if (monitorUpdateDTO.getEnabledCapabilities() != null && !monitorUpdateDTO.getEnabledCapabilities().isEmpty()) {
            String enableCapabilities = monitorUpdateDTO.getEnabledCapabilities().stream().collect(Collectors.joining(","));
            monitor.setEnabledCapabilities(enableCapabilities);
        }
        BeanUtils.copyProperties(monitorUpdateDTO, monitor);
        String collect = Optional.ofNullable(monitorUpdateDTO.getEnabledCapabilities()).orElse(Lists.newArrayList()).stream().collect(Collectors.joining(","));
        monitor.setEnabledCapabilities(collect);
        monitorMapper.updateById(monitor);
        //    todo 发送mq消息
        rabbitMqSendService.monitorMetadataChanged(Lists.newArrayList(monitor), MonitorMqSubject.UPDATE);
    }


    /**
     * 设备纳管
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Monitor> deviceInManage(DeviceInManageDTO deviceInManageDTO) throws Exception {
        ResourceTree targetResourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, deviceInManageDTO.getTargetGroupUid()));
        Optional.ofNullable(targetResourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_RESOURCE_TREE_NOT_FOUNT));
        //校验资源管理目录层数是否已经超过10层
        //1 计算出当前节点的目录层数
        List<String> PathArray = Arrays.stream(targetResourceTree.getGroupPath().split("/")).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
        ForestResponse<List<Subresource>> response = connectionManagerInnerApi.getSubresourceList(deviceInManageDTO.getUidList());
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<Subresource> subresources = response.getResult();
        if (null == subresources || subresources.isEmpty()) {
            return Lists.newArrayList();
        }
//        校验通道是否已经纳管
        TreeList treeList = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeType, 0));
        List<String> subresourceUids2 = subresources.stream().map(Subresource::getUid).collect(Collectors.toList());
        if (subresourceUids2.isEmpty()) {
            return Lists.newArrayList();
        }

        ForestResponse<List<Device>> response1 = connectionManagerInnerApi.getDevice(deviceInManageDTO.getUidList());
        if (!response1.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response1.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<Device> devices = objectMapper.readValue(response1.getContent(), new TypeReference<List<Device>>() {
        });
        Optional.ofNullable(devices).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_DEVICE_NOT_FOUNT));
        List<ResourceTree> resourceTrees = Lists.newArrayList();
        List<Monitor> monitors = Lists.newArrayList();

        if (deviceInManageDTO.isImportGroup()) {
            if ((PathArray.size() + 2) > 10) {
                throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
            }
            Map<String, List<Subresource>> subresourceListMap = subresources.stream().collect(Collectors.groupingBy(Subresource::getOwnerResource));
            for (Device device : devices) {
                String deviceGroupUid = KeyUtils.generatorUUID();
                List<Subresource> deviceSubresource = subresourceListMap.get(device.getUid());
                ResourceTree deviceResourceTree = null;
//                判断节点下有没有重名的节点 如果有直接纳管到这个分组下
                ResourceTree sameNameGroup = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, targetResourceTree.getGroupId())
                        .eq(ResourceTree::getGroupName, device.getName()));
                if (sameNameGroup != null) {
                    deviceResourceTree = sameNameGroup;
                } else {
                    deviceResourceTree = ResourceTree.builder()
                            .groupName(device.getName())
                            .groupUid(deviceGroupUid)
                            .groupPath(targetResourceTree.getGroupPath())
                            .type(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())
                            .parentId(targetResourceTree.getGroupId())
                            .treeUid(targetResourceTree.getTreeUid()).build();
                    checkSubNodeNum(deviceResourceTree.getParentId(), Lists.newArrayList(deviceResourceTree), ResourceNodeTypeEnum.CATELOG);
                    resourceTreeMapper.insert(deviceResourceTree);
                    deviceResourceTree.setGroupPath(deviceResourceTree.getGroupPath() + "/" + deviceResourceTree.getGroupId());
                    resourceTreeMapper.updateById(deviceResourceTree);
                }

                if (deviceSubresource != null) {
                    for (Subresource subresource : deviceSubresource) {
                        String groupUid = KeyUtils.generatorUUID();
                        //String monitorUid = KeyUtils.generatorUUID();
                        String monitorUid = subresource.getUid();
                        ResourceTree monitorResourceTree = ResourceTree.builder()
                                .groupName(subresource.getName())
                                .groupUid(groupUid)
                                .eventCause(subresource.getEventCause())
                                .description(subresource.getDescription())
                                .monitorUid(monitorUid)
                                .groupPath(deviceResourceTree.getGroupPath())
                                .businessUid(subresource.getUid())
                                .type(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())
                                .parentId(deviceResourceTree.getGroupId())
                                .treeUid(targetResourceTree.getTreeUid()).build();
                        resourceTrees.add(monitorResourceTree);
                        //校验同一分组下监控点名称不能重复
                        checksaveGroupNameRepet(monitorResourceTree.getGroupName(), monitorResourceTree.getParentId());
                        Monitor monitor = Monitor.builder()
                                .monitorUid(monitorUid)
                                .description(subresource.getDescription())
                                .monitorName(subresource.getName())
                                .altitude(subresource.getAltitude())
                                .eventCause(subresource.getEventCause())
                                .longitude(subresource.getLongitude())
                                .latitude(subresource.getLatitude())
                                .businessUid(subresource.getUid())
                                .online(subresource.getOnline())
                                .workState(null)
                                .recording(null)
                                .resourceType(ResourceTypeEnum.DEVICE.getResourceType())
                                .fullAddress(subresource.getFullAddress())
                                .placeCode(subresource.getPlaceCode())
                                .nativeCapabilities(subresource.getNativeCapabilities())
                                .build();
                        monitors.add(monitor);
                    }
                }
            }
        } else {
            if ((PathArray.size() + 1) > 10) {
                throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
            }
//            不导入分组 直接将设备下面未纳管的通道全部查询出来并组装数据存储
            buildMonitor(targetResourceTree, subresources, resourceTrees, monitors, ResourceTypeEnum.DEVICE.getResourceType());
        }

        //判断同一级目录下不能存在相同的监控点名称 需求取消
        //renameMonitorName(resourceTrees, monitors);
        if (!resourceTrees.isEmpty()) {
            //Set<String> collect = resourceTrees.stream().map(s -> {
            //    return s.getParentId() + "-" + s.getGroupName();
            //}).collect(Collectors.toSet());
            //if (collect.size() < resourceTrees.size()) {
            //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
            //}
            //    判断节点下的子分组不能超过1w，监控点数量不能超过1w
            Map<Long, List<ResourceTree>> parentIdMapList = resourceTrees.stream().collect(Collectors.groupingBy(ResourceTree::getParentId));
            for (Map.Entry<Long, List<ResourceTree>> items : parentIdMapList.entrySet()) {
                Long parentId = items.getKey();
                List<ResourceTree> resourceTreeList = items.getValue();
                if (null != parentId && resourceTreeList != null) {
                    checkSubNodeNum(parentId, resourceTreeList, ResourceNodeTypeEnum.MONITOR);
                }
            }
            resourceTreeMapper.batchInsert(resourceTrees);
        }
        if (!monitors.isEmpty()) {
            monitorMapper.batchInsert(monitors);
            rabbitMqSendService.monitorMetadataChanged(monitors, MonitorMqSubject.CREATE);
        }
        List<Long> resourceTreeIds = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                .map(ResourceTree::getGroupId).collect(Collectors.toList());
        resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().setSql("group_path=CONCAT(group_path,'/',group_id)").in(ResourceTree::getGroupId, resourceTreeIds));
//            修改设备的纳管数量和通道的纳管状态
        List<String> subresourceUids = subresources.stream().map(Subresource::getUid).collect(Collectors.toList());
        final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.deviceCascadeNum(subresourceUids, true);
        if (!listForestResponse.isSuccess()) {
            log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }

        }
        return monitors;
    }

    public void checksaveGroupNameRepet(String groupName, Long parentId) throws Exception {
        //Integer integer = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupName, groupName).eq(ResourceTree::getParentId, parentId));
        //if (integer > 0) {
        //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
        //}

    }

    public void checkSubNodeNum(Long parentId, List<ResourceTree> resourceTrees, ResourceNodeTypeEnum resourceNodeTypeEnum) throws Exception {
        Integer count = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getType, resourceNodeTypeEnum.getResourceTreeType())
                .eq(ResourceTree::getParentId, parentId));
        if (count + resourceTrees.size() > 10000) {
            if (resourceNodeTypeEnum.getResourceTreeType().equals(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())) {
                throw new Res400Exception(RmsResStatusEnum.SUBNODE_MONITOR_MORE_10000);
            } else {
                throw new Res400Exception(RmsResStatusEnum.SUBNODE_GROUP_MORE_10000);
            }
        }
    }

    public void test() throws Exception {
        ResourceTree resourceTree = ResourceTree.builder().groupName("大华新人脸枪机").parentId(933L).monitorUid("123").build();
        ResourceTree resourceTree1 = ResourceTree.builder().groupName("通道二十五").parentId(933L).monitorUid("saasd").build();
        ResourceTree resourceTree2 = ResourceTree.builder().groupName("通道十一").parentId(933L).monitorUid("adad").build();
        ResourceTree resourceTree3 = ResourceTree.builder().groupName("xxxxxx").parentId(933L).monitorUid("adaca").build();


        Monitor monitor1 = Monitor.builder().monitorName("大华新人脸枪机").monitorUid("123").build();
        Monitor Monitor2 = Monitor.builder().monitorName("通道二十五").monitorUid("saasd").build();
        Monitor Monitor3 = Monitor.builder().monitorName("通道十一").monitorUid("adad").build();
        Monitor Monitor4 = Monitor.builder().monitorName("xxxxxx").monitorUid("adaca").build();


        ResourceTree resourceTree5 = ResourceTree.builder().groupName("_39").parentId(967L).monitorUid("gergter").build();
        ResourceTree resourceTree6 = ResourceTree.builder().groupName("流媒体通道_39").parentId(967L).monitorUid("fsfw").build();
        ResourceTree resourceTree7 = ResourceTree.builder().groupName("39_").parentId(967L).monitorUid("casca").build();
        ResourceTree resourceTree8 = ResourceTree.builder().groupName("__39").parentId(967L).monitorUid("cassda").build();

        Monitor Monitor5 = Monitor.builder().monitorName("流媒体通道_39").monitorUid("gergter").build();
        Monitor Monito6 = Monitor.builder().monitorName("流媒体通道_60").monitorUid("fsfw").build();
        Monitor Monito7 = Monitor.builder().monitorName("流媒体通道_113").monitorUid("casca").build();
        Monitor Monito8 = Monitor.builder().monitorName("ssss").monitorUid("cassda").build();

        List<ResourceTree> resourceTrees = Lists.newArrayList(resourceTree, resourceTree1, resourceTree2, resourceTree3, resourceTree5, resourceTree6, resourceTree7, resourceTree8);
        List<Monitor> monitors = Lists.newArrayList(monitor1, Monitor2, Monitor3, Monitor4, Monitor5, Monito6, Monito7, Monito8);
        //需求取消
        //renameMonitorName(resourceTrees, monitors);
    }


    /**
     * 设备下通道纳管
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Monitor> deviceSubresourceInManage(DeviceSubresourceInManageDTO deviceSubresourceInManageDTO, String deviceUid) throws Exception {
//        校验通道是否已经被纳管了
//        TreeList treeList = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeType, 0));
//        Integer count = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getTreeUid, treeList.getTreeUid())
//                .in(ResourceTree::getBusinessUid, deviceSubresourceInManageDTO.getUidList()));
//        if (count > 0) {
//            throw new Res400Exception(RmsResStatusEnum.RESO_EXIST_INMANAGE_SUBRESOURCE);
//        }
        ResourceTree targetResourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, deviceSubresourceInManageDTO.getTargetGroupUid()));
        Optional.ofNullable(targetResourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_RESOURCE_TREE_NOT_FOUNT));
        ForestResponse<List<Device>> response1 = connectionManagerInnerApi.getDevice(Lists.newArrayList(deviceUid));
        if (!response1.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response1.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<Device> devices = response1.getResult();
        if (null == devices || devices.isEmpty()) {
            throw new Res400Exception(RmsResStatusEnum.RESO_INMANAGE_DEVICE_NOT_FOUNT);
        }
        Device device = devices.get(0);
        Optional.ofNullable(device).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_DEVICE_NOT_FOUNT));
        ForestResponse<List<Subresource>> response = connectionManagerInnerApi.getSubresourceAllList(deviceSubresourceInManageDTO.getUidList());
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<Subresource> subresources = response.getResult();
        Optional.ofNullable(subresources).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_DEVICE_NOT_FOUNT));
        List<Subresource> collect = Optional.ofNullable(subresources).map(List::stream).orElseGet(Stream::empty).filter(s -> InManagedEnum.NOT_CASCADE.getCascadeStatus().equals(s.getInManaged())).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return Lists.newArrayList();
        }

        List<ResourceTree> resourceTrees = Lists.newArrayList();
        List<Monitor> monitors = Lists.newArrayList();
        //1
        List<String> PathArray = Arrays.stream(targetResourceTree.getGroupPath().split("/")).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
        if (deviceSubresourceInManageDTO.isImportGroup()) {
            if ((PathArray.size() + 2) > 10) {
                throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
            }
            ResourceTree deviceResourceTree = null;
            //                判断节点下有没有重名的节点 如果有直接纳管到这个分组下
            ResourceTree sameNameGroup = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, targetResourceTree.getGroupId())
                    .eq(ResourceTree::getGroupName, device.getName()));
            if (sameNameGroup != null) {
                deviceResourceTree = sameNameGroup;
            } else {
                String deviceGroupUid = KeyUtils.generatorUUID();
                deviceResourceTree = ResourceTree.builder()
                        .groupName(device.getName())
                        .groupUid(deviceGroupUid)
                        .groupPath(targetResourceTree.getGroupPath())
                        .type(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())
                        .parentId(targetResourceTree.getGroupId())
                        .treeUid(targetResourceTree.getTreeUid()).build();
                checkSubNodeNum(deviceResourceTree.getParentId(), Lists.newArrayList(deviceResourceTree), ResourceNodeTypeEnum.CATELOG);
                resourceTreeMapper.insert(deviceResourceTree);
                deviceResourceTree.setGroupPath(deviceResourceTree.getGroupPath() + "/" + deviceResourceTree.getGroupId());
                resourceTreeMapper.updateById(deviceResourceTree);
            }
            for (Subresource subresource : collect) {
                String groupUid = KeyUtils.generatorUUID();
                //String monitorUid = KeyUtils.generatorUUID();
                String monitorUid = subresource.getUid();
                ResourceTree monitorResourceTree = ResourceTree.builder()
                        .groupName(subresource.getName())
                        .groupUid(groupUid)
                        .monitorUid(monitorUid)
                        .description(subresource.getDescription())
                        .groupPath(deviceResourceTree.getGroupPath())
                        .type(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())
                        .businessUid(subresource.getUid())
                        .parentId(deviceResourceTree.getGroupId())
                        .eventCause(subresource.getEventCause())
                        .treeUid(targetResourceTree.getTreeUid()).build();
                //校验同一分组下监控点名称不能重复
                checksaveGroupNameRepet(monitorResourceTree.getGroupName(), monitorResourceTree.getParentId());
                resourceTrees.add(monitorResourceTree);
                Monitor monitor = Monitor.builder()
                        .monitorUid(monitorUid)
                        .monitorName(subresource.getName())
                        .altitude(subresource.getAltitude())
                        .longitude(subresource.getLongitude())
                        .online(subresource.getOnline())
                        .description(subresource.getDescription())
                        .workState(null)
                        .recording(null)
                        .eventCause(subresource.getEventCause())
                        .latitude(subresource.getLatitude())
                        .businessUid(subresource.getUid())
                        .resourceType(ResourceTypeEnum.DEVICE.getResourceType())
                        .fullAddress(subresource.getFullAddress())
                        .placeCode(subresource.getPlaceCode())
                        .nativeCapabilities(subresource.getNativeCapabilities())
                        .build();
                monitors.add(monitor);
            }
        } else {
            if ((PathArray.size() + 1) > 10) {
                throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
            }
//            不导入分组 直接将设备下面未纳管的通道全部查询出来并组装数据存储
            buildMonitor(targetResourceTree, collect, resourceTrees, monitors, ResourceTypeEnum.DEVICE.getResourceType());
        }
        //判断同一级目录下不能存在相同的监控点名称 需求取消
        //renameMonitorName(resourceTrees, monitors);
        if (!resourceTrees.isEmpty()) {
            //    判断节点下的子分组不能超过1w，监控点数量不能超过1w
            Map<Long, List<ResourceTree>> parentIdMapList = resourceTrees.stream().collect(Collectors.groupingBy(ResourceTree::getParentId));
            for (Map.Entry<Long, List<ResourceTree>> items : parentIdMapList.entrySet()) {
                Long parentId = items.getKey();
                List<ResourceTree> resourceTreeList = items.getValue();
                if (null != parentId && resourceTreeList != null) {
                    checkSubNodeNum(parentId, resourceTreeList, ResourceNodeTypeEnum.MONITOR);
                }
            }
            resourceTreeMapper.batchInsert(resourceTrees);
        }
        if (!monitors.isEmpty()) {
            monitorMapper.batchInsert(monitors);
            //    todo 发送mq消息
            rabbitMqSendService.monitorMetadataChanged(monitors, MonitorMqSubject.CREATE);
        }
        List<Long> resourceTreeIds = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                .map(ResourceTree::getGroupId).collect(Collectors.toList());
        resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().setSql("group_path=CONCAT(group_path,'/',group_id)").in(ResourceTree::getGroupId, resourceTreeIds));
//            修改设备的纳管数量和通道的纳管状态
        List<String> subresourceUids = collect.stream().map(Subresource::getUid).collect(Collectors.toList());
        final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.deviceCascadeNum(subresourceUids, true);
        if (!listForestResponse.isSuccess()) {
            log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }

        }
        return monitors;
    }


    /**
     * 媒体纳管
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Monitor> mediaInManage(MediaInManageDTO mediaInManageDTO) throws Exception {

        ForestResponse<List<Media>> response = connectionManagerInnerApi.getMedia(mediaInManageDTO.getUidList());
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }

        List<Media> medias = response.getResult();
        List<Media> mediaNotInManage = Optional.ofNullable(medias).map(List::stream).orElseGet(Stream::empty).filter(s -> InManagedEnum.NOT_CASCADE.getCascadeStatus().equals(s.getInManaged())).collect(Collectors.toList());
        if (mediaNotInManage.isEmpty()) {
            return Lists.newArrayList();
        }
//        校验是否有媒体已经被纳管过了。
        List<String> mediaUids = mediaNotInManage.stream().map(Media::getUid).collect(Collectors.toList());
        ForestResponse<List<Subresource>> response1 = connectionManagerInnerApi.subresources(mediaUids);
        if (!response1.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response1.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<Subresource> subresources = response1.getResult();
        //如果所有的设备都已经纳管了直接返回
        List<Subresource> collect = Optional.ofNullable(subresources).map(List::stream).orElseGet(Stream::empty).filter(s -> InManagedEnum.NOT_CASCADE.getCascadeStatus().equals(s.getInManaged())).collect(Collectors.toList());
        if (collect.isEmpty()) {
            return Lists.newArrayList();
        }
        //List<String> subresourceUids = collect.stream().map(Subresource::getUid).collect(Collectors.toList());
        //Integer integer = monitorMapper.selectCount(Wrappers.<Monitor>lambdaQuery().in(Monitor::getBusinessUid, subresourceUids));
        ResourceTree targetResourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, mediaInManageDTO.getTargetGroupUid()));
        Optional.ofNullable(targetResourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_RESOURCE_TREE_NOT_FOUNT));
        //if (integer > 0) {
        //    throw new Res400Exception(RmsResStatusEnum.RESO_EXIST_INMANAGE_SUBRESOURCE);
        //}

        List<ResourceTree> resourceTrees = Lists.newArrayList();
        List<Monitor> monitors = Lists.newArrayList();

        //1 计算出当前节点的目录层数
        List<String> PathArray = Arrays.stream(targetResourceTree.getGroupPath().split("/")).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
        if (mediaInManageDTO.isImportGroup()) {
            if ((PathArray.size() + 2) > 10) {
                throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
            }
            Map<String, List<Subresource>> subresourceListMap = collect.stream().collect(Collectors.groupingBy(Subresource::getOwnerResource));
            for (Media media : mediaNotInManage) {
                ResourceTree deviceResourceTree = null;
                // 判断节点下有没有重名的节点 如果有直接纳管到这个分组下
                ResourceTree sameNameGroup = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, targetResourceTree.getGroupId())
                        .eq(ResourceTree::getGroupName, media.getName()));
                if (sameNameGroup != null) {
                    deviceResourceTree = sameNameGroup;
                } else {
                    String deviceGroupUid = KeyUtils.generatorUUID();
                    deviceResourceTree = ResourceTree.builder()
                            .groupName(media.getName())
                            .groupUid(deviceGroupUid)
                            .groupPath(targetResourceTree.getGroupPath())
                            .type(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())
                            .parentId(targetResourceTree.getGroupId())
                            .treeUid(targetResourceTree.getTreeUid()).build();

                }
                List<Subresource> deviceSubresource = subresourceListMap.get(media.getUid());
                if (deviceSubresource != null) {
                    for (Subresource subresource : deviceSubresource) {
                        String groupUid = KeyUtils.generatorUUID();
                        //String monitorUid = KeyUtils.generatorUUID();
                        String monitorUid = subresource.getUid();
                        ResourceTree monitorResourceTree = ResourceTree.builder()
                                .groupName(subresource.getName())
                                .groupUid(groupUid)
                                .monitorUid(monitorUid)
                                .groupPath(deviceResourceTree.getGroupPath())
                                .type(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())
                                .description(subresource.getDescription())
                                .businessUid(subresource.getUid())
                                .eventCause(subresource.getEventCause())
                                .parentId(deviceResourceTree.getGroupId())
                                .treeUid(targetResourceTree.getTreeUid()).build();
                        resourceTrees.add(monitorResourceTree);
                        //校验同一分组下监控点名称不能重复
                        Monitor monitor = Monitor.builder()
                                .monitorUid(monitorUid)
                                .description(subresource.getDescription())
                                .monitorName(subresource.getName())
                                .altitude(subresource.getAltitude())
                                .longitude(subresource.getLongitude())
                                .eventCause(subresource.getEventCause())
                                .latitude(subresource.getLatitude())
                                .online(media.getOnline())
                                //.workState(media.getWorkState())
                                //.recording(media.getRecording())
                                .workState(null)
                                .recording(null)
                                .businessUid(subresource.getUid())
                                .resourceType(ResourceTypeEnum.MEDIA.getResourceType())
                                .fullAddress(subresource.getFullAddress())
                                .placeCode(subresource.getPlaceCode())
                                .nativeCapabilities(subresource.getNativeCapabilities())
                                .build();
                        monitors.add(monitor);
                    }
                    checkSubNodeNum(deviceResourceTree.getParentId(), Lists.newArrayList(deviceResourceTree), ResourceNodeTypeEnum.CATELOG);
                    resourceTreeMapper.insert(deviceResourceTree);
                    deviceResourceTree.setGroupPath(deviceResourceTree.getGroupPath() + "/" + deviceResourceTree.getGroupId());
                    resourceTreeMapper.updateById(deviceResourceTree);
                }
            }
        } else {
            if ((PathArray.size() + 1) > 10) {
                throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
            }
//            不导入分组 直接将设备下面未纳管的通道全部查询出来并组装数据存储
            buildMonitor(targetResourceTree, collect, resourceTrees, monitors, ResourceTypeEnum.MEDIA.getResourceType());
        }
        //判断同一级目录下不能存在相同的监控点名称 需求取消
        //renameMonitorName(resourceTrees, monitors);
        if (!resourceTrees.isEmpty()) {
            //Set<String> collect2 = resourceTrees.stream().map(s -> {
            //    return s.getParentId() + "-" + s.getGroupName();
            //}).collect(Collectors.toSet());
            //if (collect2.size() < resourceTrees.size()) {
            //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
            //}
            //    判断节点下的子分组不能超过1w，监控点数量不能超过1w
            Map<Long, List<ResourceTree>> parentIdMapList = resourceTrees.stream().collect(Collectors.groupingBy(ResourceTree::getParentId));
            for (Map.Entry<Long, List<ResourceTree>> items : parentIdMapList.entrySet()) {
                Long parentId = items.getKey();
                List<ResourceTree> resourceTreeList = items.getValue();
                if (null != parentId && resourceTreeList != null) {
                    checkSubNodeNum(parentId, resourceTreeList, ResourceNodeTypeEnum.MONITOR);
                }
            }
            resourceTreeMapper.batchInsert(resourceTrees);
        }
        if (!monitors.isEmpty()) {
            monitorMapper.batchInsert(monitors);
            //    todo 发送mq消息
            rabbitMqSendService.monitorMetadataChanged(monitors, MonitorMqSubject.CREATE);
        }
        List<Long> resourceTreeIds = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                .map(ResourceTree::getGroupId).collect(Collectors.toList());
        resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().setSql("group_path=CONCAT(group_path,'/',group_id)").in(ResourceTree::getGroupId, resourceTreeIds));
//            修改设备的纳管数量和通道的纳管状态
        List<String> subresourceUids2 = collect.stream().map(Subresource::getUid).collect(Collectors.toList());
        final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.mediaCascadeNum(subresourceUids2, true);
        if (!listForestResponse.isSuccess()) {
            log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }

        }
        return monitors;
    }


    /**
     * 平台设备纳管
     */
    public List<Monitor> platformDeviceInManage(PlatformDeviceInManageDTO platformDeviceInManageDTO) throws Exception {
        //        校验通道是否已经纳管
        //TreeList treeList = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeType, 0));
        //Integer count = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getTreeUid, treeList.getTreeUid())
        //        .in(ResourceTree::getBusinessUid, platformDeviceInManageDTO.getUidList()));
        //if (count > 0) {
        //    throw new Res400Exception(RmsResStatusEnum.SUBRESOURCE_IS_INMANAGE);
        //}

        ForestResponse<List<PlatformDevice>> response = connectionManagerInnerApi.getPlatformedevice2(platformDeviceInManageDTO.getUidList());
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }

        List<PlatformDevice> platformDevices2 = response.getResult();
        if (platformDevices2.isEmpty()) {
            throw new Res400Exception(RmsResStatusEnum.DELETE_DEVICE_CAT_NOT_IMMANAGE);
        }
        List<PlatformDevice> platformDevices = platformDevices2.stream().filter(s -> s.getInManageNum().equals(InManagedEnum.NOT_CASCADE.getCascadeStatus())).collect(Collectors.toList());
        if (platformDevices.isEmpty()) {
            return Lists.newArrayList();
        }
        ResourceTree targetResourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, platformDeviceInManageDTO.getTargetGroupUid()));
        Optional.ofNullable(targetResourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_RESOURCE_TREE_NOT_FOUNT));
        //1 计算出当前节点的目录层数
        List<String> PathArray = Arrays.stream(targetResourceTree.getGroupPath().split("/")).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
        if ((PathArray.size() + 1) > 10) {
            throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
        }
        List<ResourceTree> resourceTrees = Lists.newArrayList();
        List<Monitor> monitors = Lists.newArrayList();

        for (PlatformDevice platformDevice : platformDevices) {
            String groupUid = KeyUtils.generatorUUID();
            //String monitorUid = KeyUtils.generatorUUID();
            String monitorUid = platformDevice.getGroupUid();
            ResourceTree resourceTree = ResourceTree.builder()
                    .groupName(platformDevice.getGroupName())
                    .groupUid(groupUid)
                    .monitorUid(monitorUid)
                    .groupPath(targetResourceTree.getGroupPath())
                    .type(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())
                    .description(platformDevice.getDescription())
                    .businessUid(platformDevice.getGroupUid())
                    .parentId(targetResourceTree.getGroupId())
                    .eventCause(platformDevice.getEventCause())
                    .treeUid(targetResourceTree.getTreeUid()).build();
            resourceTrees.add(resourceTree);
            checksaveGroupNameRepet(resourceTree.getGroupName(), resourceTree.getParentId());
            //校验同一分组下监控点名称不能重复
            Monitor monitor = Monitor.builder()
                    .monitorUid(monitorUid)
                    .description(platformDevice.getDescription())
                    .monitorName(platformDevice.getGroupName())
                    .altitude(platformDevice.getAltitude())
                    .longitude(platformDevice.getLongitude())
                    .latitude(platformDevice.getLatitude())
                    .businessUid(platformDevice.getGroupUid())
                    .online(platformDevice.getOnline())
                    .workState(null)
                    .recording(null)
                    .eventCause(platformDevice.getEventCause())
                    .resourceType(ResourceTypeEnum.PLATFORM.getResourceType())
                    .fullAddress(platformDevice.getFullAddress())
                    .placeCode(platformDevice.getPlaceCode())
                    .nativeCapabilities(platformDevice.getCapabilities())
                    .build();
            monitors.add(monitor);
        }
        //判断同一级目录下不能存在相同的监控点名称 需求取消
        //renameMonitorName(resourceTrees, monitors);
        if (!resourceTrees.isEmpty()) {
            //Set<String> collect2 = resourceTrees.stream().map(s -> {
            //    return s.getParentId() + "-" + s.getGroupName();
            //}).collect(Collectors.toSet());
            //if (collect2.size() < resourceTrees.size()) {
            //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
            //}
            //    判断节点下的子分组不能超过1w，监控点数量不能超过1w
            Map<Long, List<ResourceTree>> parentIdMapList = resourceTrees.stream().collect(Collectors.groupingBy(ResourceTree::getParentId));
            for (Map.Entry<Long, List<ResourceTree>> items : parentIdMapList.entrySet()) {
                Long parentId = items.getKey();
                List<ResourceTree> resourceTreeList = items.getValue();
                if (null != parentId && resourceTreeList != null) {
                    checkSubNodeNum(parentId, resourceTreeList, ResourceNodeTypeEnum.MONITOR);
                }
            }
            resourceTreeMapper.batchInsert(resourceTrees);
        }
        if (!monitors.isEmpty()) {
            monitorMapper.batchInsert(monitors);
            //    todo 发送mq消息
            rabbitMqSendService.monitorMetadataChanged(monitors, MonitorMqSubject.CREATE);
        }
        List<Long> resourceTreeIds = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                .map(ResourceTree::getGroupId).collect(Collectors.toList());
        resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().setSql("group_path=CONCAT(group_path,'/',group_id)").in(ResourceTree::getGroupId, resourceTreeIds));
//            修改设备的纳管数量和通道的纳管状态
        List<String> platformDeviceGroupUids = platformDevices.stream().map(PlatformDevice::getGroupUid).collect(Collectors.toList());
        final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.updateInManageNum(platformDeviceGroupUids, true);
        if (!listForestResponse.isSuccess()) {
            log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }

        }
        return monitors;
    }

    /**
     * 平台下的目录纳管
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Monitor> platformFoldInManage(PlatformFoldInManageDTO platformFoldInManageDTO) throws Exception {
        ResourceTree targetResourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, platformFoldInManageDTO.getTargetGroupUid()));
        Optional.ofNullable(targetResourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_RESOURCE_TREE_NOT_FOUNT));
//        过滤出目录类型
        ForestResponse<List<PlatformDevice>> response = connectionManagerInnerApi.getCatelogByGroupUid(platformFoldInManageDTO.getUidList());
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<PlatformDevice> platformDevices = response.getResult();
        if (null == platformDevices || platformDevices.isEmpty()) {
            throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
        }
        //校验资源树下不能有重名的目录
        List<String> catelogNames = platformDevices.stream().map(PlatformDevice::getGroupName).collect(Collectors.toList());
        final Integer catelogCount = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery()
                .eq(ResourceTree::getParentId, targetResourceTree.getGroupId())
                .in(ResourceTree::getGroupName, catelogNames));
        if (catelogCount > 0) {
            throw new Res400Exception(RmsResStatusEnum.RESO_CATELOG_EXIST);
        }

        String platformUid = platformDevices.get(0).getOwnerResource();
        List<String> groupPaths = platformDevices.stream().map(PlatformDevice::getGroupPath)
                .map(s -> {
                    ArrayList<String> list = Lists.newArrayList();
                    String[] split = s.split("/");
                    for (int i = 0; i < split.length - 1; i++) {
                        if (StringUtils.isBlank(split[i])) {
                            continue;
                        }
                        list.add(split[i]);
                    }
                    return list;
                }).flatMap(List::stream).distinct().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<PlatformDevice> platformDevices2 = Lists.newArrayList();
//        表示只纳管了根节点
        if (groupPaths.isEmpty()) {
        } else {

            ForestResponse<List<PlatformDevice>> response2 = connectionManagerInnerApi.getCatelogByGroupUid(groupPaths);
            if (!response2.isSuccess()) {
                ResStatus resStatus = jsonUtils.jsonToObject(response2.getContent(), ResStatus.class);
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            }
            platformDevices2 = response2.getResult();
        }
        ArrayList<PlatformDevice> platformDevices3 = Lists.newArrayList();
        platformDevices.forEach(s -> s.setSave(1));
        platformDevices2.forEach(s -> s.setSave(0));
        HashMap<String, PlatformDevice> collect = (HashMap<String, PlatformDevice>) platformDevices2.stream().collect(Collectors.toMap(PlatformDevice::getGroupUid, Function.identity(), (k1, k2) -> k2));
        for (PlatformDevice platformDevice : platformDevices) {
            collect.put(platformDevice.getGroupUid(), platformDevice);
        }
        collect.forEach((k, v) -> platformDevices3.add(v));
//        封装树结构
        List<TreeNode<String>> nodeList = CollUtil.newArrayList();
        for (PlatformDevice platformDevice : platformDevices3) {
            TreeNode<String> stringTreeNode = new TreeNode<>();
            HashMap<String, Object> objectObjectHashMap = Maps.newHashMap();
            objectObjectHashMap.put("save", platformDevice.getSave());
            objectObjectHashMap.put("groupPath", platformDevice.getGroupPath());
            objectObjectHashMap.put("groupUid", platformDevice.getGroupUid());
            stringTreeNode.setId(platformDevice.getDeviceId())
                    .setName(platformDevice.getGroupName())
                    .setParentId(platformDevice.getParentUid())
                    .setWeight(platformDevice.getId())
                    .setExtra(objectObjectHashMap);
            nodeList.add(stringTreeNode);
        }
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        //转换器
        List<Tree<String>> treeNodes = TreeUtil.build(nodeList, "", treeNodeConfig,
                (treeNode, tree) -> {
                    tree.setId(treeNode.getId());
                    tree.setParentId(treeNode.getParentId());
                    tree.setWeight(treeNode.getWeight());
                    tree.setName(treeNode.getName());
                    // 扩展属性 ...
                    tree.putExtra("save", treeNode.getExtra().get("save"));
                    tree.putExtra("groupPath", treeNode.getExtra().get("groupPath"));
                    tree.putExtra("groupUid", treeNode.getExtra().get("groupUid"));
                });
//        递归树
        String batchNo = KeyUtils.generatorUUID();
        //todo 判断目录下有没有存在重名的目录
        //ResourceTree targetResourceTree2 = null;
        ////                判断节点下有没有重名的节点 如果有直接纳管到这个分组下
        //ResourceTree sameNameGroup = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, targetResourceTree.getGroupId())
        //        .eq(ResourceTree::getGroupName, treeNodes.get(0).getName().toString()));
        //if (sameNameGroup != null) {
        //    targetResourceTree2 = sameNameGroup;
        //} else {
        //    targetResourceTree2 = targetResourceTree;
        //}
        List<Monitor> monitors = Lists.newArrayList();
        recus(treeNodes, batchNo, targetResourceTree.getTreeUid(), targetResourceTree.getGroupId(), targetResourceTree.getGroupPath(), platformUid, monitors);
        return monitors;
    }

    @Transactional(rollbackFor = Exception.class)
    public void recus(List<Tree<String>> treeList, String batchNo, String treeUid, Long resourceTreeParentId, String groupPath, String platforUid, List<Monitor> monitors) throws Exception {
        for (Tree<String> stringTree : treeList) {

            if (stringTree.getChildren() != null && !stringTree.getChildren().isEmpty()) {
                if ((int) stringTree.getOrDefault("save", 0) == 1) {
//                    只存储当前目录节点
                    String groupUid = KeyUtils.generatorUUID();
                    ResourceTree resourceTree = ResourceTree.builder()
                            .groupName(stringTree.getName().toString())
                            .groupUid(groupUid)
                            .parentId(resourceTreeParentId)
                            .groupPath(groupPath)
//                            .batchNo(batchNo)
                            .type(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())
                            .businessUid((String) stringTree.getOrDefault("groupUid", 0))
                            .treeUid(treeUid).build();
                    //校验不能存在相同的分组
                    //Integer resourceGroupNameCount = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, resourceTreeParentId)
                    //        .eq(ResourceTree::getGroupName, stringTree.getName().toString()));
                    //if (resourceGroupNameCount > 0) {
                    //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_GROUP);
                    //}
                    checkSubNodeNum(resourceTree.getParentId(), Lists.newArrayList(resourceTree), ResourceNodeTypeEnum.CATELOG);
                    resourceTreeMapper.insert(resourceTree);
                    resourceTree.setGroupPath(resourceTree.getGroupPath() + "/" + resourceTree.getGroupId());
                    resourceTreeMapper.updateById(resourceTree);
                    //判断资源管理层数是否已经超过10层
                    //1 计算出当前节点的目录层数
                    List<String> PathArray = Arrays.stream(resourceTree.getGroupPath().split("/")).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
                    if ((PathArray.size()) > 10) {
                        throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
                    }
                    System.out.println("1--" + stringTree.getId());
                    recus(stringTree.getChildren(), batchNo, treeUid, resourceTree.getGroupId(), resourceTree.getGroupPath(), platforUid, monitors);
                } else {
                    System.out.println("0--" + stringTree.getId());
                    recus(stringTree.getChildren(), batchNo, treeUid, resourceTreeParentId, groupPath, platforUid, monitors);
                }
            } else {
                if ((int) stringTree.getOrDefault("save", 0) == 1) {
//                    递归存储节点下的目录和设备
                    ForestResponse<List<PlatformDevice>> response = connectionManagerInnerApi.getPlatformedevice(Lists.newArrayList((String) stringTree.getOrDefault("groupUid", 0)));
                    if (!response.isSuccess()) {
                        ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
                        throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
                    }
                    PlatformDevice platformDevice = response.getResult().get(0);
                    saveFoleAndDevice(platformDevice, batchNo, treeUid, resourceTreeParentId, groupPath, platforUid, monitors);
//                    存储此节点下的所有节点及设备
                    System.out.println("1--" + stringTree.getId());
                    continue;
                } else {
                    System.out.println("0--" + stringTree.getId());
                    continue;
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveFoleAndDevice(PlatformDevice platformDevice, String batchNo, String treeUid, Long resourceTreeParentId, String groupPath, String platformUid, List<Monitor> monitors) throws Exception {
//        存储当前目录
        String groupUid = KeyUtils.generatorUUID();
        ResourceTree resourceTree = ResourceTree.builder()
                .groupName(platformDevice.getGroupName())
                .groupUid(groupUid)
                .groupPath(groupPath)
                .parentId(resourceTreeParentId)
//                            .batchNo(batchNo)
                .type(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())
                .businessUid(platformDevice.getGroupUid())
                .treeUid(treeUid).build();
        //校验同一分组下监控点名称不能重复
        checksaveGroupNameRepet(resourceTree.getGroupName(), resourceTree.getParentId());
        checkSubNodeNum(resourceTree.getParentId(), Lists.newArrayList(resourceTree), ResourceNodeTypeEnum.CATELOG);
        resourceTreeMapper.insert(resourceTree);
        resourceTree.setGroupPath(resourceTree.getGroupPath() + "/" + resourceTree.getGroupId());
        resourceTreeMapper.updateById(resourceTree);
//        判断当前目录层数是否已经超过10层
        List<String> PathArray = Arrays.stream(resourceTree.getGroupPath().split("/")).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
        if ((PathArray.size()) > 10) {
            throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
        }
//        查询出子节点
        //            查询节点下的所有的设备和目录
        ForestResponse<List<PlatformDevice>> response = connectionManagerInnerApi.getCatelogOrDeviceByParentUid(platformDevice.getDeviceId(), ResourceNodeTypeEnum.CATELOG.getResourceTreeType(), platformUid);
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<PlatformDevice> platformFoldDevices = response.getResult();
        ForestResponse<List<PlatformDevice>> response2 = connectionManagerInnerApi.getCatelogOrDeviceByParentUid(platformDevice.getDeviceId(), ResourceNodeTypeEnum.MONITOR.getResourceTreeType(), platformUid);
        if (!response2.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response2.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<PlatformDevice> platformDeviceDevices = response2.getResult();

        if (null == platformFoldDevices || platformFoldDevices.isEmpty()) {
            if (null == platformDeviceDevices || platformDeviceDevices.isEmpty()) {
                return;
            } else {
//                    存储设备
                saveDevice(platformDeviceDevices, batchNo, treeUid, resourceTree.getGroupId(), resourceTree.getGroupPath(), monitors);
                return;
            }
        } else {
            if (null == platformDeviceDevices || platformDeviceDevices.isEmpty()) {
                for (PlatformDevice platformFoldDevice : platformFoldDevices) {
                    saveFoleAndDevice(platformFoldDevice, batchNo, treeUid, resourceTree.getGroupId(), resourceTree.getGroupPath(), platformUid, monitors);
                }
            } else {
//                存储设备并递归
                saveDevice(platformDeviceDevices, batchNo, treeUid, resourceTree.getGroupId(), resourceTree.getGroupPath(), monitors);
                for (PlatformDevice platformFoldDevice : platformFoldDevices) {
                    saveFoleAndDevice(platformFoldDevice, batchNo, treeUid, resourceTree.getGroupId(), resourceTree.getGroupPath(), platformUid, monitors);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveDevice(List<PlatformDevice> list, String batchNo, String treeUid, Long resourceTreeParentId, String groupPath, List<Monitor> monitors2) throws Exception {
        List<String> PathArray = Arrays.stream(groupPath.split("/")).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
        if ((PathArray.size() + 1) > 10) {
            throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
        }
        List<ResourceTree> resourceTrees = Lists.newArrayList();
        List<Monitor> monitors = Lists.newArrayList();
        for (PlatformDevice platformDevice : list) {
            //如果已经纳管则不允许再次纳管
            if (platformDevice.getInManageNum().equals(InManagedEnum.CASCADE.getCascadeStatus())) {
                continue;
            }
            String groupUid = KeyUtils.generatorUUID();
            //String monitorUid = KeyUtils.generatorUUID();
            String monitorUid = platformDevice.getGroupUid();
            ResourceTree resourceTree = ResourceTree.builder()
                    .groupName(platformDevice.getGroupName())
                    .groupUid(groupUid)
                    .monitorUid(monitorUid)
                    .groupPath(groupPath)
                    .eventCause(platformDevice.getEventCause())
                    .type(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())
                    .description(platformDevice.getDescription())
                    .businessUid(platformDevice.getGroupUid())
                    .parentId(resourceTreeParentId)
                    .treeUid(treeUid).build();
            resourceTrees.add(resourceTree);
            //校验同一分组下监控点名称不能重复
            checksaveGroupNameRepet(resourceTree.getGroupName(), resourceTree.getParentId());//校验同一分组下监控点名称不能重复
            Monitor monitor = Monitor.builder()
                    .monitorUid(monitorUid)
                    .description(platformDevice.getDescription())
                    .monitorName(platformDevice.getGroupName())
                    .altitude(platformDevice.getAltitude())
                    .longitude(platformDevice.getLongitude())
                    .latitude(platformDevice.getLatitude())
                    .businessUid(platformDevice.getGroupUid())
                    .online(platformDevice.getOnline())
                    .eventCause(platformDevice.getEventCause())
                    .workState(null)
                    .recording(null)
                    .fullAddress(platformDevice.getFullAddress())
                    .placeCode(platformDevice.getPlaceCode())
                    .nativeCapabilities(platformDevice.getCapabilities())
                    .resourceType(ResourceTypeEnum.PLATFORM.getResourceType())
                    .build();
            monitors.add(monitor);
        }
        if (!resourceTrees.isEmpty()) {
            //Set<String> collect2 = resourceTrees.stream().map(s -> {
            //    return s.getParentId() + "-" + s.getGroupName();
            //}).collect(Collectors.toSet());
            //if (collect2.size() < resourceTrees.size()) {
            //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
            //}
            //    判断节点下的子分组不能超过1w，监控点数量不能超过1w
            Map<Long, List<ResourceTree>> parentIdMapList = resourceTrees.stream().collect(Collectors.groupingBy(ResourceTree::getParentId));
            for (Map.Entry<Long, List<ResourceTree>> items : parentIdMapList.entrySet()) {
                Long parentId = items.getKey();
                List<ResourceTree> resourceTreeList = items.getValue();
                if (null != parentId && resourceTreeList != null) {
                    checkSubNodeNum(parentId, resourceTreeList, ResourceNodeTypeEnum.MONITOR);
                }
            }
            resourceTreeMapper.batchInsert(resourceTrees);
            List<Long> resourceTreeIds = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                    .map(ResourceTree::getGroupId).collect(Collectors.toList());
            resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().setSql("group_path=CONCAT(group_path,'/',group_id)").in(ResourceTree::getGroupId, resourceTreeIds));
        }
        if (!monitors.isEmpty()) {
            monitorMapper.batchInsert(monitors);
            monitors2.addAll(monitors);
            //    todo 发送mq消息
            rabbitMqSendService.monitorMetadataChanged(monitors, MonitorMqSubject.CREATE);
        }
//       修改设备的纳管数量和通道的纳管状态
        List<String> platformDeviceGroupUids = list.stream().filter(s -> InManagedEnum.NOT_CASCADE.getCascadeStatus().equals(s.getInManageNum())).map(PlatformDevice::getGroupUid).collect(Collectors.toList());
        if (!platformDeviceGroupUids.isEmpty()) {
            final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.updateInManageNum(platformDeviceGroupUids, true);
            if (!listForestResponse.isSuccess()) {
                log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
                ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
                if (resStatus != null && resStatus.getError() != null) {
                    throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
                } else {
                    throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
                }

            }
        }
    }


    //    封装监控点
    private void buildMonitor(ResourceTree targetResourceTree, List<Subresource> subresources, List<ResourceTree> resourceTrees, List<Monitor> monitors, String resourceType) throws Exception {
        for (Subresource subresource : subresources) {
            String groupUid = KeyUtils.generatorUUID();
            //String monitorUid = KeyUtils.generatorUUID();
            String monitorUid = subresource.getUid();
            ResourceTree resourceTree = ResourceTree.builder()
                    .groupName(subresource.getName())
                    .groupUid(groupUid)
                    .monitorUid(monitorUid)
                    .description(subresource.getDescription())
                    .groupPath(targetResourceTree.getGroupPath())
                    .type(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())
                    .businessUid(subresource.getUid())
                    .eventCause(subresource.getEventCause())
                    .parentId(targetResourceTree.getGroupId())
                    .treeUid(targetResourceTree.getTreeUid()).build();
            resourceTrees.add(resourceTree);
            //校验同一分组下监控点名称不能重复
            checksaveGroupNameRepet(resourceTree.getGroupName(), resourceTree.getParentId());
            Monitor monitor = Monitor.builder()
                    .monitorUid(monitorUid)
                    .monitorName(subresource.getName())
                    .description(subresource.getDescription())
                    .altitude(subresource.getAltitude())
                    .longitude(subresource.getLongitude())
                    .eventCause(subresource.getEventCause())
                    .latitude(subresource.getLatitude())
                    .businessUid(subresource.getUid())
                    .online(subresource.getOnline())
                    .workState(null)
                    .recording(null)
                    .resourceType(resourceType)
                    .fullAddress(subresource.getFullAddress())
                    .placeCode(subresource.getPlaceCode())
                    .nativeCapabilities(subresource.getNativeCapabilities())
                    .build();
            monitors.add(monitor);

        }
        //Set<String> collect = resourceTrees.stream().map(s -> {
        //    return s.getParentId() + "-" + s.getGroupName();
        //}).collect(Collectors.toSet());
        //if (collect.size() < resourceTrees.size()) {
        //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
        //}
    }

    /**
     * 平台目录不导入分组的处理
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Monitor> platformFoldInManageNotImportGroup(PlatformFoldInManageDTO platformFoldInManageDTO) throws Exception {
//        resourceTree 节点
        ResourceTree targetResourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, platformFoldInManageDTO.getTargetGroupUid()));
        Optional.ofNullable(targetResourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_FAIL));
//        查询出平台下的所有符合条件的设备  设备类型 未纳管 未删除
        ForestResponse<List<PlatformDevice>> response = connectionManagerInnerApi.getPlatformedevice(Lists.newArrayList(platformFoldInManageDTO.getGroupUid()));
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        PlatformDevice platformDevice2 = response.getResult().get(0);
        Optional.ofNullable(platformDevice2).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_FAIL));

        ForestResponse<List<PlatformDevice>> response2 = connectionManagerInnerApi.getCatelogOrDeviceByParentUid(platformDevice2.getGroupPath());
        if (!response2.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response2.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<PlatformDevice> platformDevices = response2.getResult();
        List<ResourceTree> resourceTrees = Lists.newArrayList();
        List<Monitor> monitors = Lists.newArrayList();

        //1 计算出当前节点的目录层数
        List<String> PathArray = Arrays.stream(targetResourceTree.getGroupPath().split("/")).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
        if ((PathArray.size() + 1) > 10) {
            throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
        }
        for (PlatformDevice platformDevice : platformDevices) {
            String groupUid = KeyUtils.generatorUUID();
            //String monitorUid = KeyUtils.generatorUUID();
            String monitorUid = platformDevice.getGroupUid();
            ResourceTree resourceTree = ResourceTree.builder()
                    .groupName(platformDevice.getGroupName())
                    .groupUid(groupUid)
                    .monitorUid(monitorUid)
                    .groupPath(targetResourceTree.getGroupPath())
                    .type(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())
                    .description(platformDevice.getDescription())
                    .businessUid(platformDevice.getGroupUid())
                    .parentId(targetResourceTree.getGroupId())
                    .eventCause(platformDevice.getEventCause())
                    .treeUid(targetResourceTree.getTreeUid()).build();
            resourceTrees.add(resourceTree);
            checksaveGroupNameRepet(resourceTree.getGroupName(), resourceTree.getParentId());
            Monitor monitor = Monitor.builder()
                    .monitorUid(monitorUid)
                    .description(platformDevice.getDescription())
                    .monitorName(platformDevice.getGroupName())
                    .altitude(platformDevice.getAltitude())
                    .longitude(platformDevice.getLongitude())
                    .latitude(platformDevice.getLatitude())
                    .eventCause(platformDevice.getEventCause())
                    .businessUid(platformDevice.getGroupUid())
                    .online(platformDevice.getOnline())
                    .workState(null)
                    .recording(null)
                    .resourceType(ResourceTypeEnum.PLATFORM.getResourceType())
                    .fullAddress(platformDevice.getFullAddress())
                    .placeCode(platformDevice.getPlaceCode())
                    .nativeCapabilities(platformDevice.getCapabilities())
                    .build();
            monitors.add(monitor);
        }
        //判断同一级目录下不能存在相同的监控点名称 需求取消
        //renameMonitorName(resourceTrees, monitors);
        if (!resourceTrees.isEmpty()) {
            //Set<String> collect2 = resourceTrees.stream().map(s -> {
            //    return s.getParentId() + "-" + s.getGroupName();
            //}).collect(Collectors.toSet());
            //if (collect2.size() < resourceTrees.size()) {
            //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
            //}
            //    判断节点下的子分组不能超过1w，监控点数量不能超过1w
            Map<Long, List<ResourceTree>> parentIdMapList = resourceTrees.stream().collect(Collectors.groupingBy(ResourceTree::getParentId));
            for (Map.Entry<Long, List<ResourceTree>> items : parentIdMapList.entrySet()) {
                Long parentId = items.getKey();
                List<ResourceTree> resourceTreeList = items.getValue();
                if (null != parentId && resourceTreeList != null) {
                    checkSubNodeNum(parentId, resourceTreeList, ResourceNodeTypeEnum.MONITOR);
                }
            }
            resourceTreeMapper.batchInsert(resourceTrees);
        }
        if (!monitors.isEmpty()) {
            monitorMapper.batchInsert(monitors);
            //    todo 发送mq消息
            rabbitMqSendService.monitorMetadataChanged(monitors, MonitorMqSubject.CREATE);
        }
        List<Long> resourceTreeIds = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                .map(ResourceTree::getGroupId).collect(Collectors.toList());
        if (!resourceTreeIds.isEmpty()) {
            resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().setSql("group_path=CONCAT(group_path,'/',group_id)").in(ResourceTree::getGroupId, resourceTreeIds));
        }
//            修改设备的纳管数量和通道的纳管状态
        List<String> platformDeviceGroupUids = platformDevices.stream().map(PlatformDevice::getGroupUid).collect(Collectors.toList());
        final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.updateInManageNum(platformDeviceGroupUids, true);
        if (!listForestResponse.isSuccess()) {
            log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }

        }
        return monitors;
    }

    /**
     * 设备取消纳管
     */
    @Transactional(rollbackFor = Exception.class)
    public void deviceUnInManage(DeviceUnInManageDTO deviceUnInManageDTO) throws Exception {
//        查询设备的所有通道或者视频源token
        ForestResponse<List<Subresource>> response1 = connectionManagerInnerApi.subresources(deviceUnInManageDTO.getUidList());
        if (!response1.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response1.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<Subresource> subresources = response1.getResult();
        List<String> subresourceUids = subresources.stream().map(Subresource::getUid).collect(Collectors.toList());
//        查询出已经纳管的资源
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getBusinessUid, subresourceUids));
        List<String> monitorBusinessUids = monitors.stream().map(Monitor::getBusinessUid).collect(Collectors.toList());
        //resourceTreeMapper.delete(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getBusinessUid, monitorBusinessUids));
        //monitorMapper.delete(Wrappers.<Monitor>lambdaQuery().in(Monitor::getBusinessUid, monitorBusinessUids));
        ForestResponse<List<Device>> response = connectionManagerInnerApi.deviceCascadeNum(monitorBusinessUids, false);
        if (response.isSuccess()) {

        } else {
            log.info("status---{},content----{},result---{}", response.getStatusCode(), response.getContent(), response.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }
        }
    }

    /**
     * 媒体取消纳管
     */
    @Transactional(rollbackFor = Exception.class)
    public void mediaUnInManage(MediaUnInManageDTO mediaUnInManageDTO) throws Exception {
        //        查询设备的所有通道或者视频源token
        ForestResponse<List<Subresource>> response1 = connectionManagerInnerApi.subresources(mediaUnInManageDTO.getUidList());
        if (!response1.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response1.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<Subresource> subresources = response1.getResult();
        List<String> subresourceUids = subresources.stream().map(Subresource::getUid).collect(Collectors.toList());
        //        查询出已经纳管的资源
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getBusinessUid, subresourceUids));
        List<String> monitorBusinessUids = monitors.stream().map(Monitor::getBusinessUid).collect(Collectors.toList());
        final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.mediaCascadeNum(monitorBusinessUids, false);
        if (!listForestResponse.isSuccess()) {
            log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }

        }
    }

    /**
     * 资源管理中的取消纳管
     */
    @Transactional(rollbackFor = Exception.class)
    public void monitorUnInManage(MonitorUnInManageDTO monitorUnInManageDTO) throws Exception {
        List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupUid, monitorUnInManageDTO.getUidList()));
        List<String> monitorUids = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                .map(ResourceTree::getMonitorUid).collect(Collectors.toList());
        List<String> monitorsNew = monitorUids.stream().distinct().collect(Collectors.toList());
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getMonitorUid, monitorsNew));
        Map<String, Monitor> collect = Optional.ofNullable(monitors).orElse(Lists.newArrayList()).stream().collect(Collectors.toMap(Monitor::getBusinessUid, Function.identity(), (k1, k2) -> k1));
        List<UpdateSubResourceNumDTO> subResourceNumDTOList = resourceTrees.stream().map(s -> {
            Monitor monitor = collect.get(s.getBusinessUid());
            if (monitor != null) {
                return UpdateSubResourceNumDTO.builder()
                        .resourceType(monitor.getResourceType())
                        .subResourceUid(s.getBusinessUid())
                        .build();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        ForestResponse<List<PlatformDevice>> outmanage = connectionManagerInnerApi.outmanage(subResourceNumDTOList, false);
        System.out.println("132");
        //resourceTreeMapper.delete(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getMonitorUid, monitorUnInManageDTO.getUidList()));
        //monitorMapper.delete(Wrappers.<Monitor>lambdaQuery().in(Monitor::getMonitorUid, monitorUnInManageDTO.getUidList()));
    }

    //    取消设备下通道纳管
    @Transactional(rollbackFor = Exception.class)
    public void deviceSubresourceOutManage(SubresourceOutManageDTO subresourceOutManageDTO) throws Exception {
        ForestResponse<List<Device>> response1 = connectionManagerInnerApi.getDevice(Lists.newArrayList(subresourceOutManageDTO.getResourceUid()));
        if (!response1.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response1.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        Device device = response1.getResult().get(0);
        Optional.ofNullable(device).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_DEVICE_NOT_FOUNT));

        ForestResponse<List<Subresource>> response = connectionManagerInnerApi.getSubresourceAllList(subresourceOutManageDTO.getSubresourceUidList());
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<Subresource> subresources = response.getResult();
        Optional.ofNullable(subresources).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_INMANAGE_DEVICE_NOT_FOUNT));
        List<String> subresourceUids = subresources.stream().map(Subresource::getUid).collect(Collectors.toList());
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getBusinessUid, subresourceUids));
        List<String> monitorBusinessUids = monitors.stream().map(Monitor::getBusinessUid).collect(Collectors.toList());
        final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.deviceCascadeNum(monitorBusinessUids, false);
        if (!listForestResponse.isSuccess()) {
            log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }

        }
    }

    //    取消平台下通道纳管
    @Transactional(rollbackFor = Exception.class)
    public void platformdeviceOutManage(SubresourceOutManageDTO subresourceOutManageDTO) throws Exception {
        ForestResponse<List<PlatformDevice>> response = connectionManagerInnerApi.getPlatformedevice(subresourceOutManageDTO.getSubresourceUidList());
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }

        List<PlatformDevice> platformDevices = response.getResult();
        Optional.ofNullable(platformDevices).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_NULL));
        List<String> subresourceUids = platformDevices.stream().map(PlatformDevice::getGroupUid).collect(Collectors.toList());
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getBusinessUid, subresourceUids));
        List<String> monitorBusinessUids = monitors.stream().map(Monitor::getBusinessUid).collect(Collectors.toList());
        //updateInManageNum 已经删除掉监控点信息 和resourcetree中的数据
        final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.updateInManageNum(monitorBusinessUids, false);
        if (!listForestResponse.isSuccess()) {
            log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }

        }

    }

    //    取消平台下目录纳管
    public void platformDeviceFoldeOutManage(PlatformFoldOutManageDTO platformDeviceFoldeOutManage) throws Exception {
//        查询平台节点下的所有设备或监控点
        ForestResponse<List<PlatformDevice>> response = connectionManagerInnerApi.getPlatformedevice(Lists.newArrayList(platformDeviceFoldeOutManage.getGroupUid()));
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        PlatformDevice platformDevice = response.getResult().get(0);
        ForestResponse<List<PlatformDevice>> response2 = connectionManagerInnerApi.getCatelogOrDeviceByParentUid2(platformDevice.getGroupPath());
        if (!response2.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response2.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<PlatformDevice> platformDevices = response2.getResult();
        if (platformDevices == null || platformDevices.isEmpty()) {
            return;
        }
        List<String> subresourceUids = platformDevices.stream().map(PlatformDevice::getGroupUid).collect(Collectors.toList());
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getBusinessUid, subresourceUids));
        List<String> monitorBusinessUids = monitors.stream().map(Monitor::getBusinessUid).collect(Collectors.toList());
        final ForestResponse<List<Device>> listForestResponse = connectionManagerInnerApi.updateInManageNum(monitorBusinessUids, false);
        if (!listForestResponse.isSuccess()) {
            log.info("status---{},content----{},result---{}", listForestResponse.getStatusCode(), listForestResponse.getContent(), listForestResponse.getResult());
            ResStatus resStatus = jsonUtils.jsonToObject(listForestResponse.getContent(), ResStatus.class);
            if (resStatus != null && resStatus.getError() != null) {
                throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
            } else {
                throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
            }

        }
    }

    /**
     * 根据监控点ui查询在线状态
     */
    public List<MonitorOnlineVO> getMonitorOnline(List<String> monitorList) throws Exception {
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().select(Monitor::getMonitorUid, Monitor::getOnline).in(Monitor::getMonitorUid, monitorList));
        if (monitors == null || monitors.isEmpty()) {
            return Lists.newArrayList();
        }
        List<MonitorOnlineVO> monitorOnlines = monitorToMonitorOnlineVoStruct.sourceToTarget(monitors);
        for (MonitorOnlineVO monitorOnline : monitorOnlines) {
            final OnlineVO onlineVO = OnlineVO.builder().code(monitorOnline.getOnline()).build();
            monitorOnline.setOnline2(onlineVO);
        }
        return monitorOnlines;
    }

    public HashMap monitorNodeNameValidation(String name, String monitorUid) throws Exception {
        Monitor monitor = monitorMapper.selectOne(Wrappers.<Monitor>lambdaQuery().eq(Monitor::getMonitorUid, monitorUid));
        Optional.ofNullable(monitor).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_FAIL));
        //查询基本资源树
        TreeList tree = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeType, 0));
        Optional.ofNullable(tree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL));
        ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getMonitorUid, monitorUid).eq(ResourceTree::getTreeUid, tree.getTreeUid()));

        HashMap<Object, Object> hashMap = Maps.newHashMap();
        Integer integer = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupName, name).eq(ResourceTree::getParentId, resourceTree.getParentId()));
        if (integer > 0) {
            hashMap.put("Available", TrueFalse.FALSE.isValue());
        } else {
            hashMap.put("Available", TrueFalse.TRUE.isValue());
        }
        return hashMap;
    }

    /**
     * 校验同一级目录下监控点名称不能重复
     *
     * @param resourceTrees
     * @param monitors
     * @throws Exception
     */

    private void renameMonitorName(List<ResourceTree> resourceTrees, List<Monitor> monitors) throws Exception {
        if (resourceTrees == null || resourceTrees.isEmpty()) {
            return;
        }
        if (monitors == null || monitors.isEmpty()) {
            return;
        }
        Map<String, Monitor> monitorMap = monitors.stream().collect(Collectors.toMap(Monitor::getMonitorUid, Function.identity(), (k1, k2) -> k2));
        Map<String, ResourceTree> resourceTreeMap = resourceTrees.stream().collect(Collectors.toMap(ResourceTree::getMonitorUid, Function.identity(), (k1, k2) -> k2));
        //给resourcetree重新设置名称
        List<Long> parentId = Optional.of(resourceTrees).map(List::stream).orElseGet(Stream::empty).map(ResourceTree::getParentId).distinct().collect(Collectors.toList());
        //    查找parentId下的所有子节点
        if (parentId.isEmpty()) {
            return;
        }
        List<ResourceTree> subResourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getParentId, parentId).eq(ResourceTree::getType, 2));
        if (subResourceTrees == null || subResourceTrees.isEmpty()) {
            return;
        }
        Map<Long, List<ResourceTree>> collect1 = subResourceTrees.stream().collect(Collectors.groupingBy(ResourceTree::getParentId));
        //已经在数据库中的名称
        for (ResourceTree resourceTree : resourceTrees) {
            List<ResourceTree> resourceTree1 = collect1.get(resourceTree.getParentId());
            if (resourceTree1 == null) {
                resourceTree1 = Lists.newArrayList();
            }
            List<String> resourceTreeNames = resourceTree1.stream().map(ResourceTree::getGroupName).distinct().collect(Collectors.toList());
            String name = resourceTree.getGroupName();
            int i = 1;
            for (; ; ) {
                //名称重复了
                if (resourceTreeNames.contains(name)) {
                    String[] s = name.split("_");
                    String index = (String) s[s.length - 1];
                    //判断分割完之后最后一个是否为数字
                    Pattern pattern = Pattern.compile("[0-9]*");
                    //如果name本身就是一个数字
                    Matcher isNum2 = pattern.matcher(name);
                    if (isNum2.matches()) {
                        name = (Integer.parseInt(name) + 1) + "";
                    } else {
                        Matcher isNum = pattern.matcher(index);
                        if (isNum.matches()) {
                            //    是数字
                            Integer index2 = Integer.parseInt(index);
                            String prefix = name.substring(0, name.lastIndexOf("_"));
                            name = prefix + "_" + (index2 + 1);
                        } else {
                            //    不是数字
                            name = name + "_" + i;
                        }
                        i++;
                    }

                } else {
                    resourceTreeNames.add(name);
                    resourceTree1.add(resourceTree);
                    resourceTree.setGroupName(name);
                    Monitor monitor = monitorMap.get(resourceTree.getMonitorUid());
                    if (null != monitor) {
                        monitor.setMonitorName(name);
                    }
                    break;
                }
            }
        }

    }

    /**
     * 一键同步监控点节点信息
     *
     * @param batchParamDTO
     * @throws Exception
     */
    public void sync(BatchSyncDTO batchParamDTO, boolean isForce) throws Exception {
        List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupUid, batchParamDTO.getUidList()));
        List<String> monitorUids = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                .map(ResourceTree::getMonitorUid).distinct().collect(Collectors.toList());
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getMonitorUid, monitorUids));
        Map<String, List<Monitor>> resourceTypeMonitor = monitors.stream().collect(Collectors.groupingBy(Monitor::getResourceType));
        List<Monitor> device = resourceTypeMonitor.get(ResourceTypeEnum.DEVICE.getResourceType());
        List<Monitor> media = resourceTypeMonitor.get(ResourceTypeEnum.MEDIA.getResourceType());
        List<Monitor> edge = resourceTypeMonitor.get(ResourceTypeEnum.EDGE.getResourceType());
        List<Monitor> platform = resourceTypeMonitor.get(ResourceTypeEnum.PLATFORM.getResourceType());

        List<String> deviceUids = Optional.ofNullable(device).map(List::stream).orElseGet(Stream::empty)
                .map(Monitor::getBusinessUid).collect(Collectors.toList());
        List<String> mediaUids = Optional.ofNullable(media).map(List::stream).orElseGet(Stream::empty)
                .map(Monitor::getBusinessUid).collect(Collectors.toList());
        List<String> edgeUids = Optional.ofNullable(edge).map(List::stream).orElseGet(Stream::empty)
                .map(Monitor::getBusinessUid).collect(Collectors.toList());
        List<String> platformUids = Optional.ofNullable(platform).map(List::stream).orElseGet(Stream::empty)
                .map(Monitor::getBusinessUid).collect(Collectors.toList());
        SubresourceDTO subresourceDTO = new SubresourceDTO();
        subresourceDTO.setDeviceSubresource(deviceUids);
        subresourceDTO.setMediaSubresource(mediaUids);
        subresourceDTO.setEdgeSubresource(edgeUids);
        subresourceDTO.setPlatformSubresource(platformUids);
        ForestResponse<List<DeviceMediaPlatformSubresourceVO>> response = connectionManagerInnerApi.deviceMediaPlatformSubresource(subresourceDTO);
        if (!response.isSuccess()) {
            ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
            throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
        }
        List<DeviceMediaPlatformSubresourceVO> list = response.getResult();
        //    修改对应监控点的名称 经纬度 或者修改地址等信息
        if (list != null && !list.isEmpty()) {
            for (DeviceMediaPlatformSubresourceVO param : list) {
                List<Monitor> monitors1 = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().eq(Monitor::getBusinessUid, param.getUid()));
                if (null == monitors1 || monitors1.isEmpty()) {
                    continue;
                }
                Monitor monitor = monitors1.get(0);
                //判断是否是强制更新 如果是强制更新 直接将信息覆盖，如果不是强制更新需要判断是否修改过 如果未修改过直接更新 修改过不更新
                if (isForce) {
                    LambdaUpdateWrapper<Monitor> updateWrapper = Wrappers.<Monitor>lambdaUpdate()
                            .set(Monitor::getMonitorName, param.getName()).set(Monitor::getMonitorNameUpdate, Boolean.FALSE);
                    updateWrapper.set(Monitor::getLongitude, param.getLongitude()).set(Monitor::getLongitudeUpdate, Boolean.FALSE);
                    updateWrapper.set(Monitor::getLatitude, param.getLatitude()).set(Monitor::getLatitudeUpdate, Boolean.FALSE);
                    updateWrapper.set(Monitor::getAltitude, param.getAltitude()).set(Monitor::getAltitudeUpdate, Boolean.FALSE);
                    updateWrapper.set(Monitor::getPlaceCode, param.getPlaceCode()).set(Monitor::getPlaceCodeUpdate, Boolean.FALSE);
                    updateWrapper.set(Monitor::getFullAddress, param.getFullAddress()).set(Monitor::getFullAddressUpdate, Boolean.FALSE);
                    updateWrapper.eq(Monitor::getBusinessUid, param.getUid());
                    monitorMapper.update(null, updateWrapper);
                    resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate()
                            .set(ResourceTree::getGroupName, param.getName())
                            .eq(ResourceTree::getBusinessUid, param.getUid())
                    );
                } else {

                    LambdaUpdateWrapper<Monitor> updateWrapper = Wrappers.<Monitor>lambdaUpdate();
                    if (!monitor.getMonitorNameUpdate()) {
                        updateWrapper.set(Monitor::getMonitorName, param.getName()).set(Monitor::getMonitorNameUpdate, Boolean.FALSE);
                        resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate()
                                .set(ResourceTree::getGroupName, param.getName())
                                .eq(ResourceTree::getBusinessUid, param.getUid())
                        );
                    }
                    if (!monitor.getLatitudeUpdate()) {
                        updateWrapper.set(Monitor::getLatitude, param.getLatitude()).set(Monitor::getLatitudeUpdate, Boolean.FALSE);
                    }
                    if (!monitor.getLongitudeUpdate()) {
                        updateWrapper.set(Monitor::getLongitude, param.getLongitude()).set(Monitor::getLongitudeUpdate, Boolean.FALSE);
                    }
                    if (!monitor.getAltitudeUpdate()) {
                        updateWrapper.set(Monitor::getAltitude, param.getAltitude()).set(Monitor::getAltitudeUpdate, Boolean.FALSE);
                    }
                    if (!monitor.getPlaceCodeUpdate()) {
                        updateWrapper.set(Monitor::getPlaceCode, param.getPlaceCode()).set(Monitor::getPlaceCodeUpdate, Boolean.FALSE);
                    }
                    if (!monitor.getFullAddressUpdate()) {
                        updateWrapper.set(Monitor::getFullAddress, param.getFullAddress()).set(Monitor::getFullAddressUpdate, Boolean.FALSE);
                    }
                    updateWrapper.eq(Monitor::getMonitorUid, param.getUid());
                    monitorMapper.update(null, updateWrapper);
                }

            }
            //    校验是否重名 如果重名的话直接提示错误并回滚
            //int ResourceCount = resourceTreeMapper.selectResourceCount(null, "0000000000000001", batchParamDTO.getUidList());
            //int ResourceDiffNameCount = resourceTreeMapper.selectResourceDiffNameCount(null, "0000000000000001", batchParamDTO.getUidList());
            //if (ResourceCount != ResourceDiffNameCount) {
            //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
            //}
        } else {
            log.info("query empty list...");

        }
    }

    public void updateMonitorOnline(String subresourceUid, String onlineStatus, String eventCause) throws Exception {
        LambdaUpdateWrapper<Monitor> monitorWrapper = Wrappers.<Monitor>lambdaUpdate()
                .set(Monitor::getOnline, onlineStatus);
        if (StringUtils.isNotBlank(eventCause)) {
            String eventCauseStr = null;
            if (OnlineStatusEnum.ONLINE.getOnlineStatus().equals(onlineStatus)) {
                eventCauseStr = "";
            } else {
                eventCauseStr = eventCause;
            }
            monitorWrapper.set(Monitor::getEventCause, eventCauseStr);
            resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate()
                    .set(ResourceTree::getEventCause, eventCauseStr)
                    .eq(ResourceTree::getBusinessUid, subresourceUid));
        } else {
            monitorWrapper.set(Monitor::getEventCause, "");
            resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate()
                    .set(ResourceTree::getEventCause, "")
                    .eq(ResourceTree::getBusinessUid, subresourceUid));
        }
        monitorWrapper.eq(Monitor::getBusinessUid, subresourceUid);
        monitorMapper.update(null, monitorWrapper);


    }

    @Async
    public void sendDeleteMonitorToCoord(Collection<?> businessUids) {
        // 调用连接协调接口
        try {
            Map<String, Object> map = new HashMap();
            map.put("SubResources", businessUids);
            ForestResponse<String> response = connectCoordinatorApi.subresource(map);
            if (response.isSuccess()) {
                log.info("connectCoordinatorApi.updateDeviceManage--{}", response.getResult());
            } else {
                log.info("status---{},content----{},result---{}", response.getStatusCode(), response.getContent(), response.getResult());
                //ResStatus resStatus = jsonUtils.jsonToObject(response.getContent(), ResStatus.class);
                //if (null == resStatus || null == resStatus.getError()) {
                //    throw new Res400Exception(RmsResStatusEnum.COON_COOR_API_FAIL);
                //} else {
                //    throw new Res400Exception(resStatus.getError().getCode(), resStatus.getError().getMessage());
                //}
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
        }
    }

    /**
     * 0返回没有此任务的监控点集合 1返回列表中存在任务的监控点集合
     */
    public List<String> getMonitorsByEvent(List<String> monitorUids, String algorithmUid, Integer status) throws Exception {
        if (null == monitorUids || monitorUids.isEmpty()) {
            return Lists.newArrayList();
        }
        if (status == 1) {
            final List<MonitorEventRel> monitorEventRels = monitorEventRelMapper.selectList(
                    Wrappers.<MonitorEventRel>lambdaQuery()
                            .in(MonitorEventRel::getMonitorUid, monitorUids)
                            .eq(MonitorEventRel::getEventUid, algorithmUid));
            final List<String> collect = Optional.ofNullable(monitorEventRels).map(List::stream).orElseGet(Stream::empty)
                    .map(MonitorEventRel::getMonitorUid).distinct().collect(Collectors.toList());
            return collect;
        } else {
            final List<MonitorEventRel> monitorEventRels = monitorEventRelMapper.selectList(
                    Wrappers.<MonitorEventRel>lambdaQuery()
                            .notIn(MonitorEventRel::getMonitorUid, monitorUids)
                            .eq(MonitorEventRel::getEventUid, algorithmUid));
            final List<String> collect = Optional.ofNullable(monitorEventRels).map(List::stream).orElseGet(Stream::empty)
                    .map(MonitorEventRel::getMonitorUid).distinct().collect(Collectors.toList());
            return collect;
        }
    }

    /**
     * 修改监控点经纬度信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMonitorCoordinate(String monitorUid, UpdateMonitorCoordinateDTO updateMonitorCoordinateDTO) throws Exception {
        monitorMapper.update(null, Wrappers.<Monitor>lambdaUpdate()
                .set(Monitor::getLatitude, updateMonitorCoordinateDTO.getLatitude())
                .set(Monitor::getLongitude, updateMonitorCoordinateDTO.getLongitude())
                .set(Monitor::getAltitude, updateMonitorCoordinateDTO.getAltitude())
                .set(Monitor::getLatitudeUpdate, TrueFalse.TRUE.isValue())
                .set(Monitor::getLongitudeUpdate, TrueFalse.TRUE.isValue())
                .set(Monitor::getAltitudeUpdate, TrueFalse.FALSE.isValue())
                .eq(Monitor::getMonitorUid, monitorUid));
        //    todo 发送mq消息
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().eq(Monitor::getMonitorUid, monitorUid));
        rabbitMqSendService.monitorMetadataChanged(monitors, MonitorMqSubject.UPDATE);
    }

    /**
     * 批量修改监控点经纬度信息
     */
    @Transactional(rollbackFor = Exception.class)
    public PageData<UpdateMonitorCoordinateDTO> updateMonitorCoordinateBatch(@RequestBody List<UpdateMonitorCoordinateDTO> list) throws Exception {
        if (list == null || list.isEmpty()) {
            return new PageData<>(list);
        }
        final List<UpdateMonitorCoordinateDTO> newList = list.stream().filter(s -> StringUtils.isNotBlank(s.getMonitorUid())).collect(Collectors.toList());

        for (UpdateMonitorCoordinateDTO updateMonitorCoordinateDTO : newList) {
            monitorMapper.update(null, Wrappers.<Monitor>lambdaUpdate()
                    .set(Monitor::getLatitude, updateMonitorCoordinateDTO.getLatitude())
                    .set(Monitor::getLongitude, updateMonitorCoordinateDTO.getLongitude())
                    .set(Monitor::getAltitude, updateMonitorCoordinateDTO.getAltitude())
                    .set(Monitor::getLatitudeUpdate, TrueFalse.TRUE.isValue())
                    .set(Monitor::getLongitudeUpdate, TrueFalse.TRUE.isValue())
                    .set(Monitor::getAltitudeUpdate, TrueFalse.FALSE.isValue())
                    .eq(Monitor::getMonitorUid, updateMonitorCoordinateDTO.getMonitorUid()));
        }
        final List<String> monitorUids = newList.stream().map(UpdateMonitorCoordinateDTO::getMonitorUid).collect(Collectors.toList());
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getMonitorUid, monitorUids));
        rabbitMqSendService.monitorMetadataChanged(monitors, MonitorMqSubject.UPDATE);
        PageData<UpdateMonitorCoordinateDTO> pageData1 = new PageData<>(newList);
        return pageData1;
    }

    public int getGroupMonitoeNumber(GroupMonitorNumDTO groupMonitorNum) {
        //如果两个值都是空 直接返回0
        if ((groupMonitorNum.getMonitorUidList() == null || groupMonitorNum.getMonitorUidList().isEmpty()) && (null == groupMonitorNum.getGroupUidList() || groupMonitorNum.getGroupUidList().isEmpty())) {
            return 0;
        } else if ((groupMonitorNum.getMonitorUidList() != null && !groupMonitorNum.getMonitorUidList().isEmpty()) && (null == groupMonitorNum.getGroupUidList() || groupMonitorNum.getGroupUidList().isEmpty())) {
            //    如果getMonitorUidList 不为空 getGroupUidList 为空 直接返回getMonitorUidList的size
            return (int) groupMonitorNum.getMonitorUidList().stream().distinct().count();
        } else if ((groupMonitorNum.getMonitorUidList() == null || groupMonitorNum.getMonitorUidList().isEmpty()) && (null != groupMonitorNum.getGroupUidList() && !groupMonitorNum.getGroupUidList().isEmpty())) {
            //    如果getMonitorUidList 为空 getGroupUidList 不为空
            List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupUid, groupMonitorNum.getGroupUidList()).eq(ResourceTree::getTreeUid, groupMonitorNum.getCatelogUid()));
            if (resourceTrees == null || resourceTrees.isEmpty()) {
                return 0;
            } else {
                List<String> groupPaths = resourceTrees.stream().map(ResourceTree::getGroupPath).distinct().map(s -> s + "/").collect(Collectors.toList());
                List<String> excludeGroupUids = Lists.newArrayList();
                excludeGroupUids.add(groupMonitorNum.getSourceMonitorUid());
                int groupMonitorNum1 = resourceTreeMapper.getGroupMonitorNum(groupMonitorNum.getCatelogUid(), groupPaths, excludeGroupUids);
                return groupMonitorNum1;
            }
        } else {
            //    如果 都 不为空
            List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupUid, groupMonitorNum.getGroupUidList()).eq(ResourceTree::getTreeUid, groupMonitorNum.getCatelogUid()));
            if (resourceTrees == null || resourceTrees.isEmpty()) {
                return (int) groupMonitorNum.getMonitorUidList().stream().distinct().count();
            } else {
                List<String> groupPaths = resourceTrees.stream().map(ResourceTree::getGroupPath).distinct().map(s -> s + "/").collect(Collectors.toList());
                List<String> excludeGroupUids = Lists.newArrayList();
                excludeGroupUids.add(groupMonitorNum.getSourceMonitorUid());
                excludeGroupUids.addAll(groupMonitorNum.getMonitorUidList().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
                int groupMonitorNum1 = resourceTreeMapper.getGroupMonitorNum(groupMonitorNum.getCatelogUid(), groupPaths, excludeGroupUids);
                if (groupMonitorNum.getMonitorUidList() != null) {
                    groupMonitorNum1 += groupMonitorNum.getMonitorUidList().stream().distinct().filter(StringUtils::isNotBlank).count();
                }
                return groupMonitorNum1;
            }
        }
    }

    public List<Device> queryDevice(String protocol) {
        return monitorMapper.queryDevice(protocol);
    }

    public List<Media> queryMedia() {
        return monitorMapper.queryMedia();
    }


}

