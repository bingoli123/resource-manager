package com.inspur.rms.service;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dtflys.forest.http.ForestResponse;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.ivideo.common.constant.*;
import com.inspur.ivideo.common.entity.PageData;
import com.inspur.ivideo.common.entity.RabbitmqMessage;
import com.inspur.ivideo.common.exception.Res400Exception;
import com.inspur.ivideo.common.exception.Res404Exception;
import com.inspur.ivideo.common.utils.BeanCopy;
import com.inspur.ivideo.common.utils.JsonUtils;
import com.inspur.ivideo.common.utils.KeyUtils;
import com.inspur.ivideo.rabbit.api.MessageType;
import com.inspur.ivideo.rabbit.producer.broker.ProducerClient;
import com.inspur.rms.api.ConnectCoordinatorApi;
import com.inspur.rms.api.ConnectionManagerInnerApi;
import com.inspur.rms.constant.MonitorMqSubject;
import com.inspur.rms.constant.RmsResStatusEnum;
import com.inspur.rms.dao.MonitorMapper;
import com.inspur.rms.dao.ResourceTreeMapper;
import com.inspur.rms.dao.RoleResourceRelMapper;
import com.inspur.rms.dao.TreeListMapper;
import com.inspur.rms.mapstruct.ResourceTreeToResourceTreeSaveDtoStruct;
import com.inspur.rms.mapstruct.ResourceTreeToResourceTreeUpdateDtoStruct;
import com.inspur.rms.rmspojo.DTO.*;
import com.inspur.rms.rmspojo.PO.Monitor;
import com.inspur.rms.rmspojo.PO.ResourceTree;
import com.inspur.rms.rmspojo.PO.RoleResourceRel;
import com.inspur.rms.rmspojo.PO.TreeList;
import com.inspur.rms.rmspojo.VO.*;
import com.inspur.rms.rmspojo.cmspojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ResourceTreeService extends ServiceImpl<ResourceTreeMapper, ResourceTree> {

    private final ResourceTreeMapper resourceTreeMapper;
    private final RoleResourceRelMapper roleResourceRelMapper;
    private final ResourceTreeToResourceTreeSaveDtoStruct resourceTreeToResourceTreeSaveDtoStruct;
    private final ResourceTreeToResourceTreeUpdateDtoStruct resourceTreeToResourceTreeUpdateDtoStruct;
    private final MonitorMapper monitorMapper;
    private final ConnectionManagerInnerApi connectionManagerInnerApi;
    private final ProducerClient producerClient;
    private final TreeListMapper treeListMapper;
    private final MonitorService monitorService;
    private final ConnectCoordinatorApi connectCoordinatorApi;
    @Autowired
    private RabbitMqSendService rabbitMqSendService;
    private final JsonUtils jsonUtils;

    @Autowired
    public ResourceTreeService(ResourceTreeMapper resourceTreeMapper, RoleResourceRelMapper roleResourceRelMapper, ResourceTreeToResourceTreeSaveDtoStruct resourceTreeToResourceTreeSaveDtoStruct, ResourceTreeToResourceTreeUpdateDtoStruct resourceTreeToResourceTreeUpdateDtoStruct, MonitorMapper monitorMapper, ConnectionManagerInnerApi connectionManagerInnerApi, ProducerClient producerClient, TreeListMapper treeListMapper, MonitorService monitorService, ConnectCoordinatorApi connectCoordinatorApi, JsonUtils jsonUtils) {
        this.resourceTreeMapper = resourceTreeMapper;
        this.roleResourceRelMapper = roleResourceRelMapper;
        this.resourceTreeToResourceTreeSaveDtoStruct = resourceTreeToResourceTreeSaveDtoStruct;
        this.resourceTreeToResourceTreeUpdateDtoStruct = resourceTreeToResourceTreeUpdateDtoStruct;
        this.monitorMapper = monitorMapper;
        this.connectionManagerInnerApi = connectionManagerInnerApi;
        this.producerClient = producerClient;
        this.treeListMapper = treeListMapper;
        this.monitorService = monitorService;
        this.connectCoordinatorApi = connectCoordinatorApi;
        this.jsonUtils = jsonUtils;
    }

    public List<ResourceTree> findByAll(ResourceTree resourceTree) {
        return baseMapper.findByAll(resourceTree);
    }

    public int batchInsert(List<ResourceTree> list) {
        return baseMapper.batchInsert(list);
    }


    /**
     * 查询资源目录树
     */
    public List<ResourceTreeCatelogVO> getCatelog(String treeUid) {
        List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(
                Wrappers.<ResourceTree>lambdaQuery()
                        .eq(ResourceTree::getTreeUid, treeUid)
                        .eq(ResourceTree::getType, ResourceNodeTypeEnum.CATELOG.getResourceTreeType())
                        .orderByAsc(ResourceTree::getGroupId));
        //查询各级目录下的设备的在线和总设备数量
        //查询总数量
        List<MonitorNumOnlineNumDTO> total = resourceTreeMapper.getMonitorOnlineNum(treeUid, null);
        List<MonitorNumOnlineNumDTO> online = resourceTreeMapper.getMonitorOnlineNum(treeUid, OnlineStatusEnum.ONLINE.getOnlineStatus());
        Map<Long, ResourceTree> mapResourceTrees = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                .collect(Collectors.toMap(ResourceTree::getGroupId, Function.identity(), (k1, k2) -> k2));

        Map<Long, MonitorNumOnlineNumDTO> totalMap = Optional.ofNullable(total).map(List::stream).orElseGet(Stream::empty)
                .collect(Collectors.toMap(MonitorNumOnlineNumDTO::getGroupId, Function.identity(), (k1, k2) -> k2));
        Map<Long, MonitorNumOnlineNumDTO> onlineMap = Optional.ofNullable(online).map(List::stream).orElseGet(Stream::empty)
                .collect(Collectors.toMap(MonitorNumOnlineNumDTO::getGroupId, Function.identity(), (k1, k2) -> k2));
        List<ResourceTreeCatelogVO> res = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                .map(s -> BeanCopy.beanCopy(s, ResourceTreeCatelogVO.class)).collect(Collectors.toList());
        if (!res.isEmpty()) {
            for (ResourceTreeCatelogVO re : res) {
                MonitorNumOnlineNumDTO totalnum = totalMap.get(re.getGroupId());
                Integer total2 = totalnum == null ? 0 : totalnum.getNum();
                MonitorNumOnlineNumDTO onlinenum = onlineMap.get(re.getGroupId());
                Integer online2 = onlinenum == null ? 0 : onlinenum.getNum();
                re.setMonitorNum(total2);
                re.setMonitorOnlineNum(online2);
            }
        }

        return res;
    }

    /**
     * 查询节点详情
     */
    public ResourceTreeGroupSummaryVO getGroupSummary(String groupUid) throws Exception {
        ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, groupUid));
        ResourceTreeGroupSummaryVO resourceTreeGroupSummaryVO = new ResourceTreeGroupSummaryVO();
        BeanUtils.copyProperties(resourceTree, resourceTreeGroupSummaryVO);
        if (resourceTree != null && resourceTree.getParentId() != null && resourceTree.getParentId() != 0) {
            ResourceTree ParentResorce = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupId, resourceTree.getParentId()));
            resourceTreeGroupSummaryVO.setParentName(ParentResorce.getGroupName());
            resourceTreeGroupSummaryVO.setParentId(ParentResorce.getGroupId());
            resourceTreeGroupSummaryVO.setParentUid(ParentResorce.getGroupUid());
        }
        return resourceTreeGroupSummaryVO;
    }

    /**
     * 创建资源目录节点
     */
    public ResourceTreeSaveDTO saveResourceTreeCatelog(ResourceTreeSaveDTO resourceTreeSaveDTO) throws Exception {
        String groupUid2 = KeyUtils.generatorUUID();

        ResourceTree resourceTree = resourceTreeToResourceTreeSaveDtoStruct.targetToSource(resourceTreeSaveDTO);
//        父节点信息
        ResourceTree parent = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, resourceTreeSaveDTO.getParentUid()));
        if (null == resourceTree) {
            throw new Res400Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
        }
        //校验目录名称是否重复
        Integer sameNameCount = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, parent.getGroupId()).eq(ResourceTree::getGroupName, resourceTreeSaveDTO.getGroupName()));
        if (sameNameCount > 0) {
            throw new Res400Exception(RmsResStatusEnum.RESO_CATELOG_EXIST);
        }
        //1 计算出当前节点的目录层数
        List<String> PathArray = Arrays.stream(parent.getGroupPath().split("/")).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
        if ((PathArray.size() + 1) > 10) {
            throw new Res400Exception(RmsResStatusEnum.RESOURCE_GROUP_NOT_GT_10);
        }

        Optional.ofNullable(parent).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_NULL));
        resourceTree.setGroupUid(groupUid2);
        resourceTree.setType(ResourceNodeTypeEnum.CATELOG.getResourceTreeType());
        resourceTree.setParentId(parent.getGroupId());
        resourceTree.setTreeUid(parent.getTreeUid());
        resourceTreeMapper.insert(resourceTree);
        resourceTree.setGroupPath(parent.getGroupPath() + "/" + resourceTree.getGroupId());
        resourceTreeMapper.updateById(resourceTree);
        resourceTreeSaveDTO.setGroupId(resourceTree.getGroupId());
        resourceTreeSaveDTO.setGroupUid(resourceTree.getGroupUid());
        return resourceTreeSaveDTO;
    }

    /**
     * 修改资源目录节点
     */
    public void updateResourceTreeCatelog(ResourceTreeUpdateDTO resourceTreeUpdateDTO, String groupUid) throws Exception {
        ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, groupUid));
        Integer integer = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, resourceTree.getParentId())
                .eq(ResourceTree::getType, ResourceNodeTypeEnum.CATELOG.getResourceTreeType())
                .eq(ResourceTree::getGroupName, resourceTreeUpdateDTO.getGroupName())
                .ne(ResourceTree::getGroupUid, groupUid));
        if (integer > 0) {
            throw new Res400Exception(RmsResStatusEnum.RESO_CATELOG_EXIST);
        }

        Optional.ofNullable(resourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_NULL));
        BeanUtils.copyProperties(resourceTreeUpdateDTO, resourceTree);
        resourceTree.setUpdatedTime(null);
        resourceTreeMapper.updateById(resourceTree);
        //如果是树的根节点 同时需要修改对应树的名称
        if (resourceTree.getParentId() == null || resourceTree.getParentId() == 0) {
            treeListMapper.update(null, Wrappers.<TreeList>lambdaUpdate().set(TreeList::getTreeName, resourceTreeUpdateDTO.getGroupName()).eq(TreeList::getTreeUid, resourceTree.getTreeUid()));
        }
    }

    /**
     * 删除资源目录节点
     *
     * @param catelogUid 资源树uid
     * @param groupUid   删除的节点uid
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteResourceTree(String catelogUid, String groupUid) throws Exception {
        ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, groupUid));
        Optional.ofNullable(resourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_NULL));
        if (!resourceTree.getTreeUid().equals(catelogUid)) {
            throw new Res404Exception(RmsResStatusEnum.CATELOG_NOT_INCLUDE_GROUP);
        }
        if (resourceTree.getParentId() == null || resourceTree.getParentId() == 0) {
            throw new Res400Exception(RmsResStatusEnum.CAN_NOT_DELETE_ROOT);
        }
//      判断分组下有无监控点 不能删除有监控点的分组
        Integer monitorCount = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery()
                .likeRight(ResourceTree::getGroupPath, resourceTree.getGroupPath() + "/")
                .eq(ResourceTree::getType, ResourceNodeTypeEnum.MONITOR.getResourceTreeType()));
        if (monitorCount > 0) {
            throw new Res400Exception(RmsResStatusEnum.CANNOT_DELETE_GROUP);
        }
//        判断是否是基本资源树 如果是基本资源树需要取消纳管 如果是虚拟资源树 需要只删除树下的设备和目录
        TreeList treeList = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeUid, catelogUid));
        if (Objects.equals(treeList.getTreeType(), "0")) {
            //    基本资源树
            //    查询出节点下的所有子节点
            List<ResourceTree> resourceTreesMonitors = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                    .likeRight(ResourceTree::getGroupPath, resourceTree.getGroupPath() + "/")
                    .eq(ResourceTree::getType, ResourceNodeTypeEnum.MONITOR.getResourceTreeType()));
            if (resourceTreesMonitors != null && !resourceTreesMonitors.isEmpty()) {
                //    删除监控点 取消纳管监控点
                List<String> monitorGroupUids = resourceTreesMonitors.stream().map(ResourceTree::getGroupUid).collect(Collectors.toList());
                MonitorUnInManageDTO build = MonitorUnInManageDTO.builder().uidList(monitorGroupUids).build();
                monitorService.monitorUnInManage(build);
            }
            List<ResourceTree> resourceTreesCatelogs = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                    .likeRight(ResourceTree::getGroupPath, resourceTree.getGroupPath() + "/")
                    .eq(ResourceTree::getType, ResourceNodeTypeEnum.CATELOG.getResourceTreeType()));
            List<String> catelogUids = Lists.newArrayList();
            List<GroupMetaDataChange> groupMetaDataChanges = Lists.newArrayList();
            if (resourceTreesCatelogs != null && !resourceTreesCatelogs.isEmpty()) {
                //删除目录
                for (ResourceTree resourceTreesCatelog : resourceTreesCatelogs) {
                    GroupMetaDataChange groupMetaDataChange = GroupMetaDataChange.builder().groupUid(resourceTreesCatelog.getGroupUid()).originalPath(resourceTreesCatelog.getGroupPath()).build();
                    groupMetaDataChanges.add(groupMetaDataChange);
                }
                catelogUids = resourceTreesCatelogs.stream().map(ResourceTree::getGroupUid).collect(Collectors.toList());
            }
            catelogUids.add(groupUid);
            rabbitMqSendService.groupMetaDataChange(groupMetaDataChanges, MonitorMqSubject.DELETE);
            this.deleteMonitorNode(catelogUids);
        } else {
            //    虚拟资源树
            //查询该节点下的所有目录和设备
            List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().likeRight(ResourceTree::getGroupPath, resourceTree.getGroupPath() + "/"));
            List<String> groupUids = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty).map(ResourceTree::getGroupUid).collect(Collectors.toList());
            groupUids.add(groupUid);
            this.deleteMonitorNode(groupUids);
        }
    }

    public HashMap groupNameValidation(String groupName, String groupUid) throws Exception {
        ResourceTree parentResourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, groupUid));
        HashMap<Object, Object> hashMap = Maps.newHashMap();
        Integer integer = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, parentResourceTree.getGroupId())
                .eq(ResourceTree::getType, ResourceNodeTypeEnum.CATELOG.getResourceTreeType())
                .eq(ResourceTree::getGroupName, groupName));
        if (integer > 0) {
            hashMap.put("Available", TrueFalse.FALSE.isValue());
        } else {
            hashMap.put("Available", TrueFalse.TRUE.isValue());
        }
        return hashMap;
    }

    /**
     * 查询下级资源
     */
    public PageData<ResourceTreeMonitorVO> getChilds(Integer page,
                                                     Integer perpage,
                                                     String groupUid,
                                                     String monitorName,
                                                     Boolean recursion,
                                                     String catelogUid,
                                                     Integer online,
                                                     String abilityType,
                                                     Boolean ablityConfigStatus,
                                                     String ablityWorkStatus) throws Exception {
        List<ResourceTreeMonitorVO> resourceTreeCatelogVOs = Lists.newArrayList();
        Page<Object> page1 = null;
        String recursion1 = null;
        if (recursion != null && recursion) {
            recursion1 = RecursionEnum.RECURSION.getValue();
        } else {
            recursion1 = RecursionEnum.NOT_RECURSION.getValue();
        }
        //根绝abilityType 区分是查询资源管理的监控点列表还是能力相关功能的监控点列表，区别是是否关联monitor_event_rel表
        if (StringUtils.isBlank(abilityType)) {
            //    查询原监控点列表
            if (StringUtils.isNotBlank(groupUid)) {
                ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, groupUid));
                page1 = PageHelper.startPage(page, perpage);
                resourceTreeCatelogVOs = resourceTreeMapper.selectResourceTreeMonitor(resourceTree.getGroupId(), monitorName, resourceTree.getGroupPath() + "/", recursion1, online);
            } else {
                page1 = PageHelper.startPage(page, perpage);
                resourceTreeCatelogVOs = resourceTreeMapper.selectAllResourceTreeMonitor(catelogUid);
            }
        } else {
            //    查询相关能力的监控点列表
            ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, groupUid));
            page1 = PageHelper.startPage(page, perpage);
            resourceTreeCatelogVOs = resourceTreeMapper.selectResourceTreeMonitorAbility(resourceTree.getGroupId(), monitorName, resourceTree.getGroupPath() + "/", recursion1, abilityType, ablityConfigStatus, ablityWorkStatus);
            if (resourceTreeCatelogVOs != null && !resourceTreeCatelogVOs.isEmpty()) {
                for (ResourceTreeMonitorVO resourceTreeCatelogVO : resourceTreeCatelogVOs) {
                    if (StringUtils.isBlank(resourceTreeCatelogVO.getAblityWorkStatus())) {
                        //    表示未配置
                        ConfigStatusVO configStatusVO = ConfigStatusVO.builder().code(Boolean.FALSE).build();
                        resourceTreeCatelogVO.setConfigStatusVO(configStatusVO);
                    } else {
                        //    配置
                        ConfigStatusVO configStatusVO = ConfigStatusVO.builder().code(Boolean.TRUE).build();
                        resourceTreeCatelogVO.setConfigStatusVO(configStatusVO);
                    }
                    WorkStatusVO workStatusVO = WorkStatusVO.builder().code(resourceTreeCatelogVO.getAblityWorkStatus()).message(resourceTreeCatelogVO.getMessage()).build();
                    resourceTreeCatelogVO.setWorkStatusVO(workStatusVO);
                }
            }

        }

        if (resourceTreeCatelogVOs == null || resourceTreeCatelogVOs.isEmpty()) {
            return new PageData<>(page1, resourceTreeCatelogVOs);
        }
        for (ResourceTreeMonitorVO resourceTreeCatelogVO : resourceTreeCatelogVOs) {
            OnlineVO onlineVO = OnlineVO.builder().code(resourceTreeCatelogVO.getOnline()).message(resourceTreeCatelogVO.getEventCause()).build();
            resourceTreeCatelogVO.setOnline2(onlineVO);
        }
        Map<String, List<ResourceTreeMonitorVO>> resourceTypeMap = resourceTreeCatelogVOs.stream().collect(Collectors.groupingBy(ResourceTreeMonitorVO::getResourceType));
        List<ResourceTreeMonitorVO> deviceList = resourceTypeMap.get(ResourceTypeEnum.DEVICE.getResourceType());
        List<ResourceTreeMonitorVO> mediaList = resourceTypeMap.get(ResourceTypeEnum.MEDIA.getResourceType());
        if (deviceList != null && !deviceList.isEmpty()) {
            List<String> subresourceUids = deviceList.stream().map(ResourceTreeMonitorVO::getBusinessUid).collect(Collectors.toList());
            ForestResponse<List<Subresource>> response1 = connectionManagerInnerApi.getSubresourceAllList(subresourceUids);
            if (response1.isSuccess()) {
                List<Subresource> subresources = response1.getResult();
                Map<String, Subresource> subresourceUidMap = Optional.ofNullable(subresources).map(List::stream).orElseGet(Stream::empty).collect(Collectors.toMap(Subresource::getUid, Function.identity(), (k1, k2) -> k2));
                for (ResourceTreeMonitorVO resourceTreeCatelogVO : deviceList) {
                    Subresource subresource = subresourceUidMap.get(resourceTreeCatelogVO.getBusinessUid());
                    if (null != subresource) {
                        resourceTreeCatelogVO.setSubresourceId(subresource.getId());
                        resourceTreeCatelogVO.setDeviceUid(subresource.getOwnerResource());
                        RelationResourceVO relationResource = new RelationResourceVO();
                        relationResource.setSubresourceName(subresource.getName());
                        relationResource.setSubresourceUid(subresource.getUid());
                        relationResource.setOwnerResourceType(resourceTreeCatelogVO.getResourceType());
                        resourceTreeCatelogVO.setRelationResourceVO(relationResource);
                    }
                }
                //查询设备信息
                List<ResourceTreeMonitorVO> deviceResourceTree = deviceList.stream().filter(s -> s.getRelationResourceVO() != null && StringUtils.isNotBlank(s.getDeviceUid())).collect(Collectors.toList());
                List<String> deviceUids = Optional.ofNullable(deviceResourceTree).map(List::stream).orElseGet(Stream::empty).map(ResourceTreeMonitorVO::getDeviceUid).collect(Collectors.toList());
                ForestResponse<List<Device>> response2 = connectionManagerInnerApi.getDevice(deviceUids);
                if (response2.isSuccess()) {
                    List<Device> result = response2.getResult();
                    Map<String, Device> deviceMap = Optional.ofNullable(result).map(List::stream).orElseGet(Stream::empty).collect(Collectors.toMap(Device::getUid, Function.identity(), (k1, k2) -> k2));
                    for (ResourceTreeMonitorVO resourceTreeMonitorVO : deviceResourceTree) {
                        Device device = deviceMap.get(resourceTreeMonitorVO.getDeviceUid());
                        if (device != null) {
                            RelationResourceVO relationResource = resourceTreeMonitorVO.getRelationResourceVO() == null ? new RelationResourceVO() : resourceTreeMonitorVO.getRelationResourceVO();
                            relationResource.setProtocol(device.getProtocol());
                            relationResource.setOwnerResourceName(device.getName());
                            relationResource.setOwnerResourceUid(device.getUid());
                            if (device.getProtocol().equals(DeviceProtocolEnum.GB28181.getProtocol())) {
                                relationResource.setDeviceId(resourceTreeMonitorVO.getSubresourceId());
                            } else {
                                relationResource.setIpAddr(device.getIpAddr());
                                relationResource.setPort(device.getPort());
                            }
                            resourceTreeMonitorVO.setRelationResourceVO(relationResource);
                        }
                    }
                }

            }
        }

        if (mediaList != null && !mediaList.isEmpty()) {
            List<String> subresourceUids = mediaList.stream().map(ResourceTreeMonitorVO::getBusinessUid).collect(Collectors.toList());
            ForestResponse<List<Subresource>> response1 = connectionManagerInnerApi.getSubresourceAllList(subresourceUids);
            if (response1.isSuccess()) {
                List<Subresource> subresources = response1.getResult();
                Map<String, Subresource> subresourceUidMap = Optional.ofNullable(subresources).map(List::stream).orElseGet(Stream::empty).collect(Collectors.toMap(Subresource::getUid, Function.identity(), (k1, k2) -> k2));
                for (ResourceTreeMonitorVO resourceTreeCatelogVO : mediaList) {
                    Subresource subresource = subresourceUidMap.get(resourceTreeCatelogVO.getBusinessUid());
                    if (null != subresource) {
                        resourceTreeCatelogVO.setSubresourceId(subresource.getId());
                        resourceTreeCatelogVO.setDeviceUid(subresource.getOwnerResource());
                        RelationResourceVO relationResource = new RelationResourceVO();
                        relationResource.setSubresourceName(subresource.getName());
                        relationResource.setSubresourceUid(subresource.getUid());
                        relationResource.setOwnerResourceType(resourceTreeCatelogVO.getResourceType());
                        resourceTreeCatelogVO.setRelationResourceVO(relationResource);
                    }
                }
                //查询设备信息
                List<ResourceTreeMonitorVO> deviceResourceTree = mediaList.stream().filter(s -> s.getRelationResourceVO() != null && StringUtils.isNotBlank(s.getDeviceUid())).collect(Collectors.toList());
                List<String> deviceUids = Optional.ofNullable(deviceResourceTree).map(List::stream).orElseGet(Stream::empty).map(ResourceTreeMonitorVO::getDeviceUid).collect(Collectors.toList());
                ForestResponse<List<Media>> response2 = connectionManagerInnerApi.getMedia(deviceUids);
                if (response2.isSuccess()) {
                    List<Media> result = response2.getResult();
                    Map<String, Media> deviceMap = Optional.ofNullable(result).map(List::stream).orElseGet(Stream::empty).collect(Collectors.toMap(Media::getUid, Function.identity(), (k1, k2) -> k2));
                    for (ResourceTreeMonitorVO resourceTreeMonitorVO : deviceResourceTree) {
                        Media device = deviceMap.get(resourceTreeMonitorVO.getDeviceUid());
                        if (device != null) {
                            RelationResourceVO relationResource = resourceTreeMonitorVO.getRelationResourceVO() == null ? new RelationResourceVO() : resourceTreeMonitorVO.getRelationResourceVO();
                            relationResource.setProtocol(device.getProtocol());
                            relationResource.setUrl(device.getUrl());
                            relationResource.setOwnerResourceName(device.getName());
                            relationResource.setOwnerResourceUid(device.getUid());
                            relationResource.setProtocol(device.getProtocol());
                            relationResource.setUrl(device.getUrl());
                            resourceTreeMonitorVO.setRelationResourceVO(relationResource);
                        }
                    }
                }
            }
        }


//        查询设备或者平台名称  或者 通道名称
        for (ResourceTreeMonitorVO resourceTreeCatelogVO : resourceTreeCatelogVOs) {
            if (resourceTreeCatelogVO.getResourceType().equals(ResourceTypeEnum.PLATFORM.getResourceType())) {
                ForestResponse<List<PlatformDevice>> response = connectionManagerInnerApi.getPlatformedevice(Lists.newArrayList(resourceTreeCatelogVO.getBusinessUid()));
                if (!response.isSuccess()) {
                    continue;
                }
                List<PlatformDevice> result = response.getResult();
                if (result == null || result.isEmpty()) {
                    continue;
                }
                PlatformDevice platformDevice = result.get(0);
                RelationResourceVO relationResource = new RelationResourceVO();
                relationResource.setSubresourceName(platformDevice.getGroupName());
                relationResource.setSubresourceUid(platformDevice.getGroupUid());
                relationResource.setOwnerResourceType(resourceTreeCatelogVO.getResourceType());
                ForestResponse<Platform> response1 = connectionManagerInnerApi.getPlatform(platformDevice.getOwnerResource());
                if (!response1.isSuccess()) {
                    continue;
                }
                Platform platform = response1.getResult();
                if (platform == null) {
                    continue;
                }
                if (platform.getProtocol().equals(PlatformProtocolEnum.GB28181.getProtocol())) {
                    relationResource.setProtocol(platform.getProtocol());
                    relationResource.setDeviceId(platformDevice.getDeviceId());
                } else {
                    //    todo 浪潮连接设备有需求之后再判断
                }
                relationResource.setOwnerResourceName(platform.getName());
                relationResource.setOwnerResourceUid(platform.getUid());
                resourceTreeCatelogVO.setRelationResourceVO(relationResource);
            }
        }
        return new PageData<>(page1, resourceTreeCatelogVOs);
    }

    /**
     * 根据树id查询该树的跟节点
     */

    public ResourceTreeCatelogVO getTreeRoot(String treeUid) {
        List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                .eq(ResourceTree::getTreeUid, treeUid).and(s -> s.isNull(ResourceTree::getParentId).or().eq(ResourceTree::getParentId, "")));
        Optional.ofNullable(resourceTrees).orElse(Lists.newArrayList());
        return BeanCopy.beanCopy(resourceTrees.get(0), ResourceTreeCatelogVO.class);
    }

    /**
     * 资源管理目录中监控点查询
     */
    public PageData<MonitorListVO> queryMonitorList(Integer page, Integer perPage, String monitorName, String groupUid) {
//        现根据groupUid查询出groupId
        ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, groupUid));
        Page<MonitorListVO> objects = PageHelper.startPage(page, perPage);
        List<MonitorListVO> monitorListVOs = resourceTreeMapper.selecResourceTreeMonitor(resourceTree.getGroupId(), monitorName);
        return new PageData<MonitorListVO>(objects, monitorListVOs);
    }


    /**
     * 监控点复制 从基本资源树复制设备到虚拟资源树
     */
    @Transactional(rollbackFor = Exception.class)
    public void resourceCopy(MonitorNodeCopyDTO monitorNodeCopyDTO) throws Exception {
        ResourceTree targetResourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, monitorNodeCopyDTO.getMonitorNodeCopyDestinationDTO().getParentGroupUid()));
        Optional.ofNullable(targetResourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL));
        List<String> sourceMonitorNodes = monitorNodeCopyDTO.getMonitorNodeCopySourceDTO().getMonitorUidList();
        List<String> sourceGroupUids = monitorNodeCopyDTO.getMonitorNodeCopySourceDTO().getGroupUidList();
        if (sourceMonitorNodes != null && !sourceMonitorNodes.isEmpty()) {
//            监控点节点的复制
            List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupUid, sourceMonitorNodes));
            if (resourceTrees == null || resourceTrees.isEmpty()) {
                return;
            }
            Map<String, ResourceTree> collect = resourceTrees.stream().collect(Collectors.toMap(ResourceTree::getMonitorUid, Function.identity(), (k1, k2) -> k2));
//            判断目标目录是否已经包含了复制的监控点的节点
            List<String> monitorUids = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty).map(ResourceTree::getMonitorUid).collect(Collectors.toList());
            //查询出已经再此目录下的监控点
            List<ResourceTree> dupMonitors = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                    .in(ResourceTree::getMonitorUid, monitorUids)
                    .eq(ResourceTree::getParentId, targetResourceTree.getGroupId()));
            if (dupMonitors != null && !dupMonitors.isEmpty()) {
                //    将重复的数据从resourceTrees 中删除
                Set<String> dupMonitorUids = Optional.ofNullable(dupMonitors).map(List::stream).orElseGet(Stream::empty).map(ResourceTree::getMonitorUid).collect(Collectors.toSet());
                collect.entrySet().removeIf(entry -> dupMonitorUids.contains(entry.getKey()));
                resourceTrees = new ArrayList<>(collect.values());
            }

            if (resourceTrees.isEmpty()) {
                return;
            }
            List<ResourceTree> saveResourceTree = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                    .map(s -> {
                        ResourceTree resourceTree = new ResourceTree();
                        BeanUtils.copyProperties(s, resourceTree);
                        resourceTree.setTreeUid(targetResourceTree.getTreeUid());
                        resourceTree.setGroupPath(targetResourceTree.getGroupPath());
                        resourceTree.setGroupUid(KeyUtils.generatorUUID());
                        resourceTree.setParentId(targetResourceTree.getGroupId());
                        return resourceTree;
                    }).collect(Collectors.toList());
            resourceTreeMapper.batchInsert(saveResourceTree);
            List<Long> resourceTreeIds = Optional.ofNullable(saveResourceTree).map(List::stream).orElseGet(Stream::empty)
                    .map(ResourceTree::getGroupId).collect(Collectors.toList());
            resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().setSql("group_path=CONCAT(group_path,'/',group_id)").in(ResourceTree::getGroupId, resourceTreeIds));

        }
//        复制目录下的监控点节点
        if (sourceGroupUids != null && !sourceGroupUids.isEmpty()) {
            List<ResourceTree> allResourceTree = Lists.newArrayList();
            for (String sourceGroupUid : sourceGroupUids) {
                ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, sourceGroupUid));
                if (resourceTree == null) {
                    continue;
                }
                List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                        .likeRight(ResourceTree::getGroupPath, resourceTree.getGroupPath() + "/")
                        .eq(ResourceTree::getType, 2));

                List<ResourceTree> saveResourceTree = Optional.ofNullable(resourceTrees).map(List::stream).orElseGet(Stream::empty)
                        .map(s -> {
                            ResourceTree resourceTree2 = new ResourceTree();
                            BeanUtils.copyProperties(s, resourceTree2);
                            resourceTree2.setTreeUid(targetResourceTree.getTreeUid());
                            resourceTree2.setGroupPath(targetResourceTree.getGroupPath());
                            resourceTree2.setGroupUid(KeyUtils.generatorUUID());
                            resourceTree2.setParentId(targetResourceTree.getGroupId());
                            return resourceTree2;
                        }).collect(Collectors.toList());
                allResourceTree.addAll(saveResourceTree);
            }
            if (allResourceTree.isEmpty()) {
                return;
            }
//            判断复制的监控点节点是否和目的地的监控点节点冲突
            List<String> monitorUids = Optional.ofNullable(allResourceTree).map(List::stream).orElseGet(Stream::empty).map(ResourceTree::getMonitorUid).collect(Collectors.toList());
            Map<String, ResourceTree> collect = allResourceTree.stream().collect(Collectors.toMap(ResourceTree::getMonitorUid, Function.identity(), (k1, k2) -> k2));
            //查询出已经再此目录下的监控点
            List<ResourceTree> dupMonitors = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                    .in(ResourceTree::getMonitorUid, monitorUids)
                    .eq(ResourceTree::getParentId, targetResourceTree.getGroupId()));
            if (dupMonitors != null && !dupMonitors.isEmpty()) {
                //    将重复的数据从resourceTrees 中删除
                Set<String> dupMonitorUids = Optional.ofNullable(dupMonitors).map(List::stream).orElseGet(Stream::empty).map(ResourceTree::getMonitorUid).collect(Collectors.toSet());
                collect.entrySet().removeIf(entry -> dupMonitorUids.contains(entry.getKey()));
                allResourceTree = new ArrayList<>(collect.values());
            }

            if (allResourceTree.isEmpty()) {
                return;
            }
            resourceTreeMapper.batchInsert(allResourceTree);
            List<Long> resourceTreeIds = Optional.of(allResourceTree).map(List::stream).orElseGet(Stream::empty)
                    .map(ResourceTree::getGroupId).collect(Collectors.toList());
            resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().setSql("group_path=CONCAT(group_path,'/',group_id)").in(ResourceTree::getGroupId, resourceTreeIds));
        }
    }

    /**
     * 监控点节点删除
     */
    public void deleteMonitorNode(String monitorNodeUid) throws Exception {
        //todo 校验不能删除基本资源树的监控点
        int delete = resourceTreeMapper.delete(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, monitorNodeUid));
        if (delete <= 0) {
            throw new Res400Exception(RmsResStatusEnum.RESOURCE_TREE_DELETE_FAIL);
        }
    }

    /**
     * 监控点节点批量删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMonitorNode(List<String> uidList) throws Exception {
        int delete = resourceTreeMapper.delete(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupUid, uidList));

        if (delete <= 0) {
            throw new Res400Exception(RmsResStatusEnum.RESOURCE_TREE_DELETE_FAIL);
        }
    }

    /**
     * 监控点移动
     */
    @Transactional(rollbackFor = Exception.class)
    public void monitorMove(MonitorBatchMoveDTO monitorBatchMoveDTO) throws Exception {
        ResourceTree targetResourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, monitorBatchMoveDTO.getParentUid()));
        List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                .eq(ResourceTree::getTreeUid, targetResourceTree.getTreeUid())
                .in(ResourceTree::getGroupUid, monitorBatchMoveDTO.getUidList()));
        if (resourceTrees == null || resourceTrees.isEmpty()) {
            throw new Res404Exception(RmsResStatusEnum.RESO_COMMON_FAIL);
        }
        List<String> monitorUids = resourceTrees.stream().map(ResourceTree::getMonitorUid).distinct().collect(Collectors.toList());
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getMonitorUid, monitorUids));
        Map<String, Monitor> collect = monitors.stream().collect(Collectors.toMap(Monitor::getMonitorUid, Function.identity(), (k1, k2) -> k2));
        for (ResourceTree resourceTree : resourceTrees) {
            Monitor monitor = collect.get(resourceTree.getMonitorUid());
            monitor.setOldpath(resourceTree.getGroupPath());
        }
        List<ResourceTree> newresourceTree = Lists.newArrayList();
        List<String> groupNames = resourceTrees.stream().map(ResourceTree::getGroupName).collect(Collectors.toList());
        List<String> groupUids = resourceTrees.stream().map(ResourceTree::getGroupUid).collect(Collectors.toList());
        //Integer integer = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery()
        //        .eq(ResourceTree::getParentId, targetResourceTree.getGroupId())
        //        .in(ResourceTree::getGroupName, groupNames)
        //        .notIn(ResourceTree::getGroupUid, groupUids));
        //if (integer > 0) {
        //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
        //}
        resourceTrees.forEach(s -> {
            s.setGroupPath(targetResourceTree.getGroupPath());
            s.setParentId(targetResourceTree.getGroupId());

            resourceTreeMapper.updateById(s);
            newresourceTree.add(s);
        });
        List<Long> resourceTreeIds = Optional.ofNullable(newresourceTree).map(List::stream).orElseGet(Stream::empty)
                .map(ResourceTree::getGroupId).collect(Collectors.toList());
        resourceTreeMapper.update(null, Wrappers.<ResourceTree>lambdaUpdate().setSql("group_path=CONCAT(group_path,'/',group_id)").in(ResourceTree::getGroupId, resourceTreeIds));
        //    发送修改监控点的消息
        for (ResourceTree resourceTree : resourceTrees) {
            Monitor monitor = collect.get(resourceTree.getMonitorUid());
            monitor.setNewpath(resourceTree.getGroupPath() + "/" + resourceTree.getGroupId());
            monitor.setParentId(resourceTree.getParentId());
        }
        rabbitMqSendService.monitorMetadataChanged(monitors, MonitorMqSubject.UPDATE);
    }

    public List<String> rolesGroups(String roleUid) throws Exception {
        List<RoleResourceRel> roleResourceRels = roleResourceRelMapper.selectList(Wrappers.<RoleResourceRel>lambdaQuery().eq(RoleResourceRel::getRoleUid, roleUid));
        return Optional.ofNullable(roleResourceRels).map(List::stream).orElseGet(Stream::empty)
                .map(RoleResourceRel::getGroupUid).collect(Collectors.toList());
    }

    /**
     * 取消纳管之后删除已纳管的监控点和监控点节点
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateDeviceManage(Collection<?> businessUids) throws Exception {
        List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getBusinessUid, businessUids));
        List<String> monitorUids = Optional.ofNullable(monitors).map(List::stream).orElseGet(Stream::empty)
                .map(Monitor::getMonitorUid).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        Map<String, Monitor> collect = monitors.stream().collect(Collectors.toMap(Monitor::getMonitorUid, Function.identity(), (k1, k2) -> k2));
        int delete = 0;
        if (businessUids != null && !businessUids.isEmpty()) {
            delete = monitorMapper.delete(Wrappers.<Monitor>lambdaQuery().in(Monitor::getBusinessUid, businessUids));
            resourceTreeMapper.delete(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getBusinessUid, businessUids));
        }
        if (!monitorUids.isEmpty()) {
            log.info("start send rabbitmq msg");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            for (String monitorUid : monitorUids) {
                String formatDate = simpleDateFormat.format(new Date(System.currentTimeMillis()));
                String messageId = UUID.fastUUID().toString();
                Monitor monitor = collect.get(monitorUid);
                if (monitor == null) {
                    continue;
                }
                RabbitmqMessage<List<Monitor>> rabbitmqMessage = RabbitmqMessage.<List<Monitor>>builder()
                        .specversion("1.0")
                        .type("Manage:Monitor:MetadataChanged")
                        .id(messageId)
                        .source("monitor/" + monitorUid)
                        .subject("Delete")
                        .traceroute("resource-manager")
                        .time(formatDate)
                        .datacontenttype("application/json")
                        .dataschema("")
                        .data(Lists.newArrayList(monitor))
                        .build();
                String s = jsonUtils.objectToJson(rabbitmqMessage);
                HashMap map = jsonUtils.jsonToObject(s, HashMap.class);
                com.inspur.ivideo.rabbit.api.Message message = new com.inspur.ivideo.rabbit.api.Message(messageId,
                        "manage.monitor.metadata-changed",
                        "Delete",
                        map, 0, MessageType.RAPID);
                producerClient.send(message);
                log.info(s);
                log.info("send msg............");
            }
            log.info("end send rabbitmq msg");
        }
        // 调用连接协调接口
        try {
            monitorService.sendDeleteMonitorToCoord(businessUids);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.toString());
        }

        return delete;
    }

    public List<MonitorOnlineGroupNameVO> getMonitorOnlineAndGroupName(List<String> monitorUids) throws Exception {
        List<MonitorOnlineGroupNameVO> monitorOnlineAndGroupName = resourceTreeMapper.getMonitorOnlineAndGroupName(monitorUids);
        return monitorOnlineAndGroupName;
    }

}

