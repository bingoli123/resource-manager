package com.inspur.rms.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.http.ForestResponse;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.inspur.ivideo.common.constant.OnlineStatusEnum;
import com.inspur.ivideo.common.constant.ResourceNodeTypeEnum;
import com.inspur.ivideo.common.constant.TrueFalse;
import com.inspur.ivideo.common.entity.BatchActionResult;
import com.inspur.ivideo.common.entity.PageData;
import com.inspur.ivideo.common.exception.Res400Exception;
import com.inspur.ivideo.common.utils.JsonUtils;
import com.inspur.rms.api.ResourceAuthApi;
import com.inspur.rms.constant.RmsResStatusEnum;
import com.inspur.rms.dao.*;
import com.inspur.rms.mapstruct.ResourceTreeToResourceTreeListStruct;
import com.inspur.rms.rmspojo.PO.*;
import com.inspur.rms.rmspojo.VO.*;
import com.inspur.rms.rmspojo.pmspojo.RoleSummaryVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author : lidongbin
 * @date : 2021/9/9 9:27 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 * 资源权限查询
 */
@Service
public class ResourceAuthService {

    private final ResourceTreeMapper resourceTreeMapper;
    private final ResourceTreeToResourceTreeListStruct resourceTreeToResourceTreeListStruct;
    private final RoleResourceRelMapper roleResourceRelMapper;
    private final ResourceAuthApi resourceAuthApi;
    private final MonitorMapper monitorMapper;
    private final TreeListMapper treeListMapper;
    private final MonitorEventRelMapper monitorEventRelMapper;
    private final JsonUtils jsonUtils;

    public ResourceAuthService(ResourceTreeMapper resourceTreeMapper, ResourceTreeToResourceTreeListStruct resourceTreeToResourceTreeListStruct, RoleResourceRelMapper roleResourceRelMapper, ResourceAuthApi resourceAuthApi, MonitorMapper monitorMapper, TreeListMapper treeListMapper, MonitorEventRelMapper monitorEventRelMapper, JsonUtils jsonUtils) {
        this.resourceTreeMapper = resourceTreeMapper;
        this.resourceTreeToResourceTreeListStruct = resourceTreeToResourceTreeListStruct;
        this.roleResourceRelMapper = roleResourceRelMapper;
        this.resourceAuthApi = resourceAuthApi;
        this.monitorMapper = monitorMapper;
        this.treeListMapper = treeListMapper;
        this.monitorEventRelMapper = monitorEventRelMapper;
        this.jsonUtils = jsonUtils;
    }

    public PageData<ResourceTreeListVO> getSubresources(Long parentId, String catelogUid, List<String> monitorLabelNames, String algoUid) throws Exception {
        List<ResourceTreeListVO> resourceTreeList = Lists.newArrayList();
        if (0 == parentId) {
//            查询跟节点
            List<ResourceTree> resourceTreeList2 = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, 0)
                    .eq(ResourceTree::getTreeUid, catelogUid)
                    .orderByAsc(ResourceTree::getType)
                    .orderByAsc(ResourceTree::getGroupId));
            resourceTreeList = resourceTreeToResourceTreeListStruct.sourceToTarget(resourceTreeList2);
        } else {
            List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                    .eq(ResourceTree::getParentId, parentId)
                    .eq(ResourceTree::getTreeUid, catelogUid)
                    .orderByAsc(ResourceTree::getType)
                    .orderByAsc(ResourceTree::getGroupId));
            resourceTreeList = resourceTreeToResourceTreeListStruct.sourceToTarget(resourceTrees);

        }
        //查询监控点绑定的事件
        getMonitorEvents(resourceTreeList, monitorLabelNames, algoUid);
        //        判断是否还有下级节点
        List<ResourceTreeListVO> resourceTreeListVOs = Optional.ofNullable(resourceTreeList).map(List::stream).orElseGet(Stream::empty)
                .filter(s -> s.getType().equals(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())).collect(Collectors.toList());
        if (!resourceTreeListVOs.isEmpty()) {
            List<Long> parentIds = resourceTreeListVOs.stream().map(ResourceTreeListVO::getGroupId).collect(Collectors.toList());
            List<ParentIdCountVO> parentIdCountVOS = resourceTreeMapper.queryByParentIdIn(parentIds);
            if (parentIdCountVOS != null) {

                Map<Long, ParentIdCountVO> parentIdCountVOMap = parentIdCountVOS.stream().collect(Collectors.toMap(ParentIdCountVO::getParentId, Function.identity(), (k1, k2) -> k2));
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeList) {
                    if (resourceTreeListVO.getType().equals(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())) {
                        ParentIdCountVO parentIdCountVO = parentIdCountVOMap.get(resourceTreeListVO.getGroupId());
                        if (null != parentIdCountVO) {
                            if (parentIdCountVO.getCount() <= 0) {
                                resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                            } else {
                                resourceTreeListVO.setHasNodes(TrueFalse.TRUE.isValue());
                            }
                        } else {
                            resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                        }
                    }
                }
            }
        }
        List<String> monitorUids = Optional.ofNullable(resourceTreeList).map(List::stream).orElseGet(Stream::empty)
                .filter(s -> s.getType().equals(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())).map(ResourceTreeListVO::getMonitorUid).collect(Collectors.toList());
        if (!monitorUids.isEmpty()) {
            List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getMonitorUid, monitorUids));
            if (monitors != null && !monitorUids.isEmpty()) {
                Map<String, Monitor> collect = monitors.stream().collect(Collectors.toMap(Monitor::getMonitorUid, Function.identity(), (k1, k2) -> k2));
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeList) {
                    if (resourceTreeListVO.getType().equals(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())) {
                        Monitor monitor1 = collect.get(resourceTreeListVO.getMonitorUid());
                        if (monitor1 != null) {
                            if (null != monitor1) {
                                resourceTreeListVO.setOnline(monitor1.getOnline());
                            } else {
                                resourceTreeListVO.setOnline(OnlineStatusEnum.OFFLINE.getOnlineStatus());
                            }
                        }
                    }
                }
            }
        }
        for (ResourceTreeListVO resourceTreeListVO : resourceTreeList) {
            OnlineVO onlineVO = OnlineVO.builder().code(resourceTreeListVO.getOnline()).build();
            resourceTreeListVO.setOnline2(onlineVO);
        }
        return new PageData<>(resourceTreeList.size(), resourceTreeList, true);
    }


    public List<ResourceTreeListVO> getSubresource(Long groupId, String treeUid) throws Exception {
        List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                .eq(ResourceTree::getParentId, groupId)
                .eq(ResourceTree::getTreeUid, treeUid)
                .orderByAsc(ResourceTree::getType)
                .orderByAsc(ResourceTree::getGroupId));
        List<ResourceTreeListVO> resourceTreeList = resourceTreeToResourceTreeListStruct.sourceToTarget(resourceTrees);
        final List<String> collect = Optional.ofNullable(resourceTreeList).map(List::stream).orElseGet(Stream::empty)
                .filter(s -> s.getType().equals(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())).map(ResourceTreeListVO::getMonitorUid).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            final List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getMonitorUid, collect));

            final Map<String, Monitor> collect1 = Optional.ofNullable(monitors).map(List::stream).orElseGet(Stream::empty)
                    .collect(Collectors.toMap(Monitor::getMonitorUid, Function.identity(), (k1, k2) -> k2));
            for (ResourceTreeListVO resourceTreeListVO : resourceTreeList) {
                final Monitor monitor = collect1.get(resourceTreeListVO.getMonitorUid());
                if (null == monitor) {
                    resourceTreeListVO.setOnline(OnlineStatusEnum.NOT_CONNECT.getOnlineStatus());
                } else {
                    resourceTreeListVO.setOnline(monitor.getOnline());
                }
            }
        }

        //        判断是否还有下级节点
        List<ResourceTreeListVO> resourceTreeListVOs = Optional.ofNullable(resourceTreeList).map(List::stream).orElseGet(Stream::empty)
                .filter(s -> s.getType().equals(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())).collect(Collectors.toList());
        if (!resourceTreeListVOs.isEmpty()) {
            List<Long> parentIds = resourceTreeListVOs.stream().map(ResourceTreeListVO::getGroupId).collect(Collectors.toList());
            List<ParentIdCountVO> parentIdCountVOS = resourceTreeMapper.queryByParentIdIn(parentIds);
            if (parentIdCountVOS != null) {

                Map<Long, ParentIdCountVO> parentIdCountVOMap = parentIdCountVOS.stream().collect(Collectors.toMap(ParentIdCountVO::getParentId, Function.identity(), (k1, k2) -> k2));
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeList) {
                    if (resourceTreeListVO.getType().equals(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())) {
                        ParentIdCountVO parentIdCountVO = parentIdCountVOMap.get(resourceTreeListVO.getGroupId());
                        if (null != parentIdCountVO) {
                            if (parentIdCountVO.getCount() <= 0) {
                                resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                            } else {
                                resourceTreeListVO.setHasNodes(TrueFalse.TRUE.isValue());
                            }
                        } else {
                            resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                        }
                    }
                }
            }
        }
        //        判断是否还有下级节点
        //for (ResourceTreeListVO resourceTreeListVO : resourceTreeList) {
        //    if (resourceTreeListVO.getType().equals(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())) {
        //        Integer integer = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, resourceTreeListVO.getGroupId()));
        //        if (integer <= 0) {
        //            resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
        //        } else {
        //            resourceTreeListVO.setHasNodes(TrueFalse.TRUE.isValue());
        //        }
        //    }
        //
        //}
        return resourceTreeList;
    }

    /**
     * 逐层查询用户或角色资源树
     */
    public List<ResourceTreeListVO> getRoleSubresource(Long groupId, String roleUid, List<String> monitorLabelNames, String algoUid) throws Exception {
        if (groupId == null || groupId == 0) {
            ForestResponse<RoleSummaryVO> roleSummary = resourceAuthApi.getRoleSummary(roleUid);
            if (!roleSummary.isSuccess()) {
                throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
            }
            RoleSummaryVO roleSummaryVO = roleSummary.getResult();
            if (null == roleSummaryVO) {
                throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
            }
//            校验资源树是否已禁用
            TreeList treeList = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeUid, roleSummaryVO.getCatelogUid()));
            if (Objects.equals(treeList.getTreeType(), "0")) {

            } else {
                if (treeList != null && !treeList.isTreeStatus()) {
                    return Lists.newArrayList();
                }
            }


//            直接查询角色和角色绑定分组树的根节点
            ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery()
                    .eq(ResourceTree::getParentId, 0)
                    .eq(ResourceTree::getTreeUid, roleSummaryVO.getCatelogUid())
                    .orderByAsc(ResourceTree::getType)
                    .orderByAsc(ResourceTree::getGroupId));
            List<ResourceTreeListVO> res = resourceTreeToResourceTreeListStruct.sourceToTarget(Lists.newArrayList(resourceTree));
            if (res == null) {
                throw new Res400Exception(RmsResStatusEnum.RESO_QUERY_FAIL);
            }
//            判断节点是否还有下级节点
            int counts = this.getSubRersourceCount(res.get(0).getGroupId(), roleUid);
            if (counts > 0) {
                res.get(0).setHasNodes(TrueFalse.TRUE.isValue());
            } else {
                res.get(0).setHasNodes(TrueFalse.FALSE.isValue());
            }
//            判断是否有全部权限
            Integer integer = roleResourceRelMapper.selectCount(Wrappers.<RoleResourceRel>lambdaQuery()
                    .eq(RoleResourceRel::getRoleUid, roleUid)
                    .eq(RoleResourceRel::getGroupUid, resourceTree.getGroupUid()));
            if (integer > 0) {
                res.get(0).setIsAuthNode(TrueFalse.TRUE.isValue());
            } else {
                res.get(0).setIsAuthNode(TrueFalse.FALSE.isValue());
            }
            for (ResourceTreeListVO resourceTreeListVO : res) {
                OnlineVO onlineVO = OnlineVO.builder().code(resourceTreeListVO.getOnline()).build();
                resourceTreeListVO.setOnline2(onlineVO);
            }
            //查询监控点绑定的事件
            getMonitorEvents(res, monitorLabelNames, algoUid);
            return res;
        } else {
            //判断资源树是否已被禁用
            ForestResponse<RoleSummaryVO> roleSummary = resourceAuthApi.getRoleSummary(roleUid);
            if (!roleSummary.isSuccess()) {
                throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
            }
            RoleSummaryVO roleSummaryVO = roleSummary.getResult();
            if (null == roleSummaryVO) {
                throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
            }
//            校验资源树是否已禁用
            TreeList treeList = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeUid, roleSummaryVO.getCatelogUid()));
            if (treeList != null && !treeList.isTreeStatus()) {
                return Lists.newArrayList();
            }
            ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupId, groupId));
            Optional.ofNullable(resourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.QUERY_ROLE_SUBRESOURCE_FAIL));
//            拆分path
            String[] split = resourceTree.getGroupPath().split("/");
            List<String> collect2 = Lists.newArrayList(split).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
            List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupId, collect2));
            List<String> groupUids2 = resourceTrees.stream().map(ResourceTree::getGroupUid).collect(Collectors.toList());
//        判断此角色是否有此目录以及父目录的所有权限
            Integer count = roleResourceRelMapper.selectCount(Wrappers.<RoleResourceRel>lambdaQuery()
                    .eq(RoleResourceRel::getRoleUid, roleUid)
                    .in(RoleResourceRel::getGroupUid, groupUids2));
            if (count > 0) {
//            查询这个节点下的所有设备和节点
                List<ResourceTreeListVO> subresource = getSubresource(groupId, resourceTree.getTreeUid());
                Optional.ofNullable(subresource).orElse(Lists.newArrayList()).forEach(s -> s.setIsAuthNode(TrueFalse.TRUE.isValue()));
                //查询监控点绑定的事件
                getMonitorEvents(subresource, monitorLabelNames, algoUid);
                return subresource;
            } else {
//            没有查询到 按照sql查询节点下有权限的
                List<ResourceTree> resourceTreeByRoleUidAndGroup = resourceTreeMapper.getResourceTreeByRoleUidAndGroupId(roleUid, resourceTree.getGroupPath());
                List<ResourceTreeListVO> resourceTreeListVOs = resourceTreeToResourceTreeListStruct.sourceToTarget(resourceTreeByRoleUidAndGroup);
//                判断目录下面是否还有下级目录
                List<ResourceTreeListVO> resourceTreeListVOs2 = Optional.ofNullable(resourceTreeListVOs).map(List::stream).orElseGet(Stream::empty)
                        .filter(s -> s.getType().equals(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())).collect(Collectors.toList());
                if (!resourceTreeListVOs2.isEmpty()) {
                    List<Long> parentIds = resourceTreeListVOs2.stream().map(ResourceTreeListVO::getGroupId).collect(Collectors.toList());
                    List<ParentIdCountVO> parentIdCountVOS = resourceTreeMapper.queryByParentIdIn(parentIds);
                    if (parentIdCountVOS != null) {

                        Map<Long, ParentIdCountVO> parentIdCountVOMap = parentIdCountVOS.stream().collect(Collectors.toMap(ParentIdCountVO::getParentId, Function.identity(), (k1, k2) -> k2));
                        for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
                            if (resourceTreeListVO.getType().equals(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())) {
                                ParentIdCountVO parentIdCountVO = parentIdCountVOMap.get(resourceTreeListVO.getGroupId());
                                if (null != parentIdCountVO) {
                                    if (parentIdCountVO.getCount() <= 0) {
                                        resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                                    } else {
                                        resourceTreeListVO.setHasNodes(TrueFalse.TRUE.isValue());
                                    }
                                } else {
                                    resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                                }
                            }
                        }
                    }
                }
                //for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
                //    int counts = this.getSubRersourceCount(resourceTreeListVO.getGroupId(), roleUid);
                //    if (counts > 0) {
                //        resourceTreeListVO.setHasNodes(TrueFalse.TRUE.isValue());
                //    } else {
                //        resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                //    }
                //}
//                判断下面的数据是全选还是半选
//                List<String> groupUids = resourceTreeListVOs.stream().map(ResourceTreeListVO::getGroupUid).collect(Collectors.toList());
//                if (!groupUids.isEmpty()) {
////                    需要设置成1的点
//                    List<RoleResourceRel> roleResourceRels = roleResourceRelMapper.selectList(Wrappers.<RoleResourceRel>lambdaQuery()
//                            .in(RoleResourceRel::getGroupUid, groupUids)
//                            .eq(RoleResourceRel::getRoleUid, roleUid));
//                    if (!roleResourceRels.isEmpty()) {
//                        List<String> collect = roleResourceRels.stream().map(RoleResourceRel::getGroupUid).collect(Collectors.toList());
//                        resourceTreeListVOs.forEach(s -> {
//                            if (collect.contains(s.getGroupUid())) {
//                                s.setIsAuthNode(1);
//                            } else {
//                                s.setIsAuthNode(0);
//                            }
//                        });
//                    } else {
//                        for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
//                            resourceTreeListVO.setIsAuthNode(0);
//                        }
////                        resourceTreeListVOs.stream().map(s -> s.setIsAuthNode(0)).collect(Collectors.toList());
//                    }
//                }
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
                    OnlineVO onlineVO = OnlineVO.builder().code(resourceTreeListVO.getOnline()).build();
                    resourceTreeListVO.setOnline2(onlineVO);
                }
                //查询监控点绑定的事件
                getMonitorEvents(resourceTreeListVOs, monitorLabelNames, algoUid);
                return resourceTreeListVOs;
            }
        }
    }


    /**
     * 资源树搜索
     */

    public PageData<ResourceTreeListVO> resourceTreeSearch(Integer page, Integer perPage, String groupName, String treeUid, List<String> monitorLabelNames, String algoUid) throws Exception {
        Page<ResourceTree> objects = PageHelper.startPage(page, perPage);
        List<ResourceTree> resourceTrees = resourceTreeMapper.searchResourceName(groupName, treeUid);
        //List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().like(ResourceTree::getGroupName, groupName).eq(ResourceTree::getTreeUid, treeUid));
        List<ResourceTreeListVO> resourceTreeListVOs = resourceTreeToResourceTreeListStruct.sourceToTarget(resourceTrees);
        if (page != null && perPage != null) {
            if (resourceTreeListVOs != null && !resourceTreeListVOs.isEmpty()) {
                final Set<String> set = Sets.newHashSet();
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
                    String[] split = resourceTreeListVO.getGroupPath().split("/");
                    Set<String> collect2 = Sets.newHashSet(split).stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
                    set.addAll(collect2);
                }
                List<ResourceTree> resourceTreeList = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupId, set));
                Map<Long, ResourceTree> resourceTreeMap = Optional.ofNullable(resourceTreeList).map(List::stream).orElseGet(Stream::empty)
                        .collect(Collectors.toMap(ResourceTree::getGroupId, Function.identity(), (k1, k2) -> k2));
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
                    //在线状态赋值
                    if (resourceTreeListVO.getType().equals(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())) {
                        //    是监控点类型查询监控点在线状态
                        Monitor monitor = monitorMapper.selectOne(Wrappers.<Monitor>lambdaQuery().eq(Monitor::getMonitorUid, resourceTreeListVO.getMonitorUid()));
                        if (null != monitor) {
                            resourceTreeListVO.setOnline(monitor.getOnline());
                        } else {
                            resourceTreeListVO.setOnline(OnlineStatusEnum.OFFLINE.getOnlineStatus());
                        }
                    }
                    String[] split = resourceTreeListVO.getGroupPath().split("/");
                    List<String> collect2 = Lists.newArrayList(split).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
                    StringBuffer buffer = new StringBuffer();
                    //buffer.append("/");
                    for (String s : collect2) {
                        if (StringUtils.isBlank(s)) {
                            continue;
                        }
                        final ResourceTree resourceTree = resourceTreeMap.get(Long.parseLong(s));
                        if (resourceTree == null) {
                            continue;
                        } else {
                            buffer.append("/");
                            buffer.append(resourceTree.getGroupName());
                        }


                    }
                    resourceTreeListVO.setGroupPathName(buffer.toString());
                }
                //查询监控点绑定的事件
                getMonitorEvents(resourceTreeListVOs, monitorLabelNames, algoUid);
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
                    OnlineVO onlineVO = OnlineVO.builder().code(resourceTreeListVO.getOnline()).build();
                    resourceTreeListVO.setOnline2(onlineVO);
                }
            }
        }


        return new PageData<>(objects, resourceTreeListVOs);
    }


    /**
     * 用户或者角色资源树搜索
     */
    public PageData<ResourceTreeListVO> roleResourceTreeSearch(Integer page, Integer perPage, String groupName, String roleUid, List<String> monitorLabelNames, String algoUid) throws Exception {
//        查询角色关联的所有资源
        List<RoleResourceRel> roleResourceRels = roleResourceRelMapper.selectList(Wrappers.<RoleResourceRel>lambdaQuery().eq(RoleResourceRel::getRoleUid, roleUid));
        if (null == roleResourceRels || roleResourceRels.isEmpty()) {
            throw new Res400Exception(RmsResStatusEnum.ROLE_RESOURCE_FAIL);
        }
        List<String> groupUidList = roleResourceRels.stream().map(RoleResourceRel::getGroupUid).collect(Collectors.toList());
        List<ResourceTree> resourceTrees1 = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupUid, groupUidList));
//        向上查询所有的节点
        List<String> groupIds = resourceTrees1.stream().map(s -> {
            ArrayList<String> list = Lists.newArrayList();
            String[] split = s.getGroupPath().split("/");
            for (int i = 0; i < split.length; i++) {
                list.add(split[i]);
            }
            return list;
        }).flatMap(List::stream).distinct().filter(StringUtils::isNotBlank).collect(Collectors.toList());
//        向下搜索
        String groupPathLike = resourceTrees1.stream().map(ResourceTree::getGroupPath).distinct().filter(StringUtils::isNotBlank).map(s -> s + "/").collect(Collectors.joining("|"));
        if (page == null) {
            page = 1;
        }
        if (perPage == null) {
            perPage = 50;
        }
        Page<ResourceTree> objects = PageHelper.startPage(page, perPage);
        List<ResourceTree> resourceTrees = resourceTreeMapper.roleGroupNameSearch(groupPathLike, groupName, groupIds);
        List<ResourceTreeListVO> resourceTreeListVOs = resourceTreeToResourceTreeListStruct.sourceToTarget(resourceTrees);
        //查询监控点绑定的事件
        getMonitorEvents(resourceTreeListVOs, monitorLabelNames, algoUid);
        for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
            if (Objects.equals(resourceTreeListVO.getType(), ResourceNodeTypeEnum.MONITOR.getResourceTreeType())) {
                OnlineVO onlineVO = OnlineVO.builder().code(resourceTreeListVO.getOnline()).build();
                resourceTreeListVO.setOnline2(onlineVO);
            } else {
                resourceTreeListVO.setOnline2(new OnlineVO());
            }
        }
        return new PageData<>(objects, resourceTreeListVOs);
    }

    /**
     * 资源树跳转
     */
    public List<List<ResourceTreeListVO>> jupmToResource(Long groupId, List<String> monitorLabelNames, String algoUid) throws Exception {
        ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupId, groupId));
        Optional.ofNullable(resourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.QUERY_ROLE_SUBRESOURCE_FAIL));
//        分割grouppath
        List<List<ResourceTreeListVO>> res = Lists.newArrayList();
        String[] split = resourceTree.getGroupPath().split("/");
        List<String> collect = Lists.newArrayList(split).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
//        根节点本身
        List<ResourceTree> rootResource = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupId, collect.get(0)).orderByAsc(ResourceTree::getType).orderByDesc(ResourceTree::getCreatedTime));
        res.add(resourceTreeToResourceTreeListStruct.sourceToTarget(rootResource));
        collect.forEach(s -> {
            if (!s.equals(String.valueOf(groupId))) {
                List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, s).orderByAsc(ResourceTree::getType).orderByDesc(ResourceTree::getCreatedTime));
                List<ResourceTreeListVO> resourceTreeListVOs = resourceTreeToResourceTreeListStruct.sourceToTarget(resourceTrees);
                List<ResourceTreeListVO> resourceTreeListVOs2 = Optional.ofNullable(resourceTreeListVOs).map(List::stream).orElseGet(Stream::empty)
                        .filter(i -> i.getType().equals(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())).collect(Collectors.toList());
                if (!resourceTreeListVOs2.isEmpty()) {
                    List<Long> parentIds = resourceTreeListVOs2.stream().map(ResourceTreeListVO::getGroupId).collect(Collectors.toList());
                    List<ParentIdCountVO> parentIdCountVOS = resourceTreeMapper.queryByParentIdIn(parentIds);
                    if (parentIdCountVOS != null) {

                        Map<Long, ParentIdCountVO> parentIdCountVOMap = parentIdCountVOS.stream().collect(Collectors.toMap(ParentIdCountVO::getParentId, Function.identity(), (k1, k2) -> k2));
                        for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
                            if (resourceTreeListVO.getType().equals(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())) {
                                ParentIdCountVO parentIdCountVO = parentIdCountVOMap.get(resourceTreeListVO.getGroupId());
                                if (null != parentIdCountVO) {
                                    if (parentIdCountVO.getCount() <= 0) {
                                        resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                                    } else {
                                        resourceTreeListVO.setHasNodes(TrueFalse.TRUE.isValue());
                                    }
                                } else {
                                    resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                                }
                            }
                        }
                    }
                }
                //for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
                //    if (resourceTreeListVO.getType().equals(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())) {
                //        Integer integer = resourceTreeMapper.selectCount(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getParentId, resourceTreeListVO.getGroupId()));
                //        if (integer <= 0) {
                //            resourceTreeListVO.setHasNodes(TrueFalse.FALSE.isValue());
                //        } else {
                //            resourceTreeListVO.setHasNodes(TrueFalse.TRUE.isValue());
                //        }
                //    }
                //}
                //查询监控点绑定的事件
                getMonitorEvents(resourceTreeListVOs, monitorLabelNames, algoUid);
                res.add(resourceTreeListVOs);
            }
        });
        for (List<ResourceTreeListVO> re : res) {
            for (ResourceTreeListVO resourceTreeListVO : re) {
                if (resourceTreeListVO.getType().equals(ResourceNodeTypeEnum.MONITOR.getResourceTreeType())) {
                    Monitor monitor = monitorMapper.selectOne(Wrappers.<Monitor>lambdaQuery().eq(Monitor::getMonitorUid, resourceTreeListVO.getMonitorUid()));
                    if (null != monitor) {
                        OnlineVO onlineVO = OnlineVO.builder().code(monitor.getOnline()).build();
                        resourceTreeListVO.setOnline2(onlineVO);
                    }
                } else {
                    OnlineVO onlineVO = OnlineVO.builder().code(resourceTreeListVO.getOnline()).build();
                    resourceTreeListVO.setOnline2(onlineVO);
                }
            }
        }
        return res;
    }

    /**
     * 用户或者角色资源树跳转
     */
    public List<List<ResourceTreeListVO>> roleJupmToResource(Long groupId, String roleUid, List<String> monitorLabelNames, String algoUid) throws Exception {
        ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupId, groupId));
        Optional.ofNullable(resourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.QUERY_ROLE_SUBRESOURCE_FAIL));
//        分割grouppath
        List<List<ResourceTreeListVO>> res = Lists.newArrayList();
        String[] split = resourceTree.getGroupPath().split("/");
        List<String> collect = Lists.newArrayList(split).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
//        根节点本身
        ResourceTree rootResource = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupId, collect.get(0)).orderByAsc(ResourceTree::getType).orderByDesc(ResourceTree::getCreatedTime));
        ResourceTreeListVO resourceTree2 = resourceTreeToResourceTreeListStruct.sourceToTarget(rootResource);
        //       判断根节点是否有全部权限
        Integer integer = roleResourceRelMapper.selectCount(Wrappers.<RoleResourceRel>lambdaQuery()
                .eq(RoleResourceRel::getRoleUid, roleUid)
                .eq(RoleResourceRel::getGroupUid, resourceTree2.getGroupUid()));
        if (integer > 0) {
            resourceTree2.setIsAuthNode(TrueFalse.TRUE.isValue());
        } else {
            resourceTree2.setIsAuthNode(TrueFalse.FALSE.isValue());
        }
        resourceTree2.setHasNodes(TrueFalse.TRUE.isValue());
        res.add(Lists.newArrayList(resourceTree2));
        for (String s : collect) {
            if (!s.equals(String.valueOf(groupId))) {
                List<ResourceTreeListVO> roleSubresource = getRoleSubresource(Long.valueOf(s), roleUid, monitorLabelNames, algoUid);
                //查询监控点绑定的事件上面那行代码已经执行了
                //getMonitorEvents(roleSubresource,monitorLabelNames,algoUid);
                res.add(roleSubresource);
            }
        }
        for (List<ResourceTreeListVO> re : res) {
            for (ResourceTreeListVO resourceTreeListVO : re) {
                OnlineVO onlineVO = OnlineVO.builder().code(resourceTreeListVO.getOnline()).build();
                resourceTreeListVO.setOnline2(onlineVO);
            }
        }
        return res;
    }

    /**
     * 根据权限节点查询权限节点和路径节点
     */
    public List<ResourceTreeListVO> getRoleResource(List<String> roleUids) throws Exception {
//        将多个角色的权限关系合成一个
        List<RoleResourceRel> roleResourceRels = roleResourceRelMapper.selectList(Wrappers.<RoleResourceRel>lambdaQuery().in(RoleResourceRel::getRoleUid, roleUids));
        List<String> collect = Optional.ofNullable(roleResourceRels).map(List::stream).orElseGet(Stream::empty)
                .map(RoleResourceRel::getGroupUid).collect(Collectors.toList());
        //        查询角色关联的所有资源
        List<ResourceTree> resourceTrees1 = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupUid, collect));
//        向上查询所有的节点
        List<String> groupIds = resourceTrees1.stream().map(s -> {
            ArrayList<String> list = Lists.newArrayList();
            String[] split = s.getGroupPath().split("/");
            for (int i = 0; i < split.length; i++) {
                list.add(split[i]);
            }
            return list;
        }).flatMap(List::stream).distinct().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (groupIds.isEmpty()) {
            return Lists.newArrayList();
        }
        List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupId, groupIds));
        List<ResourceTreeListVO> resourceTreeListVOs = resourceTreeToResourceTreeListStruct.sourceToTarget(resourceTrees);
        for (ResourceTreeListVO resourceTreeListVO : resourceTreeListVOs) {
            OnlineVO onlineVO = OnlineVO.builder().code(resourceTreeListVO.getOnline()).build();
            resourceTreeListVO.setOnline2(onlineVO);
        }
        return resourceTreeToResourceTreeListStruct.sourceToTarget(resourceTrees);
    }

    /**
     * 查询角色是否有监控点的权限
     */
    public List<RoleAuthMonitor> roleAuthMonitor(String roleUid, List<String> monitorUids) throws Exception {
        List<RoleAuthMonitor> res = Lists.newArrayList();
//        查询角色绑定的资源树
        ForestResponse<RoleSummaryVO> roleSummary = resourceAuthApi.getRoleSummary(roleUid);
        if (!roleSummary.isSuccess()) {
            throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
        }
        RoleSummaryVO roleSummaryVO = roleSummary.getResult();
        if (null == roleSummaryVO) {
            throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
        }
        for (String monitorUid : monitorUids) {
            RoleAuthMonitor roleAuthMonitor = RoleAuthMonitor.builder().monitorUid(monitorUid).build();
            final Monitor monitor = monitorMapper.selectOne(Wrappers.<Monitor>lambdaQuery().eq(Monitor::getMonitorUid, monitorUid));
            if (monitor == null) {
                throw new Res400Exception(RmsResStatusEnum.MONITOR_IS_DELETED);
            }
            List<ResourceTree> resourceTrees2 = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                    .eq(ResourceTree::getTreeUid, roleSummaryVO.getCatelogUid())
                    .eq(ResourceTree::getMonitorUid, monitorUid));
            if (resourceTrees2 == null) {
                continue;
            }
            List<String> collect3 = Lists.newArrayList();
            for (ResourceTree resourceTree : resourceTrees2) {
                String[] split = resourceTree.getGroupPath().split("/");
                List<String> collect2 = Lists.newArrayList(split).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
                collect3.addAll(collect2);
            }
            collect3.stream().distinct();
            List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupId, collect3));
            List<String> groupUids2 = resourceTrees.stream().map(ResourceTree::getGroupUid).collect(Collectors.toList());
//        判断此角色是否有此目录以及父目录的所有权限
            Integer count = roleResourceRelMapper.selectCount(Wrappers.<RoleResourceRel>lambdaQuery()
                    .eq(RoleResourceRel::getRoleUid, roleUid)
                    .in(RoleResourceRel::getGroupUid, groupUids2));
            if (count > 0) {
                roleAuthMonitor.setAuth(TrueFalse.TRUE.isValue());
            } else {
                roleAuthMonitor.setAuth(TrueFalse.FALSE.isValue());
            }
            res.add(roleAuthMonitor);
        }
        return res;
    }

    /**
     * 查询角色是否有监控点的权限
     */
    public BatchActionResult<RoleAuthMonitor> roleAuthMonitors(String roleUid, List<String> monitorUids) throws Exception {
        BatchActionResult<RoleAuthMonitor> batchActionResult = new BatchActionResult<>();
        List<RoleAuthMonitor> res = Lists.newArrayList();
//        查询角色对应的菜单树
        ForestResponse<RoleSummaryVO> roleSummary = resourceAuthApi.getRoleSummary(roleUid);
        if (!roleSummary.isSuccess()) {
            throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
        }
        RoleSummaryVO roleSummaryVO = roleSummary.getResult();
        if (null == roleSummaryVO) {
            throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
        }
        for (String monitorUid : monitorUids) {
            RoleAuthMonitor roleAuthMonitor = RoleAuthMonitor.builder().monitorUid(monitorUid).build();
            List<ResourceTree> resourceTrees2 = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery()
                    .eq(ResourceTree::getTreeUid, roleSummaryVO.getCatelogUid())
                    .eq(ResourceTree::getMonitorUid, monitorUid));
            if (resourceTrees2 == null) {
                roleAuthMonitor.setAuth(TrueFalse.FALSE.isValue());
                res.add(roleAuthMonitor);
                continue;
            }
            List<String> collect3 = Lists.newArrayList();
            for (ResourceTree resourceTree : resourceTrees2) {
                String[] split = resourceTree.getGroupPath().split("/");
                List<String> collect2 = Lists.newArrayList(split).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
                collect3.addAll(collect2);
            }
            collect3.stream().distinct();
            List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupId, collect3));
            List<String> groupUids2 = resourceTrees.stream().map(ResourceTree::getGroupUid).collect(Collectors.toList());
            Integer count = roleResourceRelMapper.selectCount(Wrappers.<RoleResourceRel>lambdaQuery()
                    .eq(RoleResourceRel::getRoleUid, roleUid)
                    .in(RoleResourceRel::getGroupUid, groupUids2));
            if (count > 0) {
                roleAuthMonitor.setAuth(TrueFalse.TRUE.isValue());
            } else {
                roleAuthMonitor.setAuth(TrueFalse.FALSE.isValue());
            }
            res.add(roleAuthMonitor);
        }
        batchActionResult.setTotal(monitorUids.size());
        batchActionResult.setCompleted(true);
        batchActionResult.setValue(res);
        return batchActionResult;
    }

    //    根据groupUID查询groupId
    public ResourceTree getGroupIdbyGroupUid(String groupUid) throws Exception {
        ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, groupUid));
        return resourceTree;
    }


    private int getSubRersourceCount(Long groupId, String roleUid) throws Exception {
        ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupId, groupId));
        Optional.ofNullable(resourceTree).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.QUERY_ROLE_SUBRESOURCE_FAIL));
        String[] split = resourceTree.getGroupPath().split("/");
        List<String> collect2 = Lists.newArrayList(split).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<ResourceTree> resourceTrees = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().in(ResourceTree::getGroupId, collect2));
        List<String> groupUids2 = resourceTrees.stream().map(ResourceTree::getGroupUid).collect(Collectors.toList());
        Integer count = roleResourceRelMapper.selectCount(Wrappers.<RoleResourceRel>lambdaQuery()
                .eq(RoleResourceRel::getRoleUid, roleUid)
                .in(RoleResourceRel::getGroupUid, groupUids2));
        if (count > 0) {
            List<ResourceTreeListVO> subresource = getSubresource(groupId, resourceTree.getTreeUid());
            return subresource.size();
        } else {
            List<ResourceTree> resourceTreeByRoleUidAndGroup = resourceTreeMapper.getResourceTreeByRoleUidAndGroupId(roleUid, resourceTree.getGroupPath());
            return resourceTreeByRoleUidAndGroup.size();
        }
    }


    /**
     * 查询角色是否有监控点的权限
     */
    public Set<String> roleAuthMonitors2(String roleUid, List<String> monitorUids) throws Exception {
        String catelogUid = "0000000000000001";
        //开始判断
        final List<ResourceTree> resourceTrees1 = resourceTreeMapper.selectList(Wrappers.<ResourceTree>lambdaQuery().select(ResourceTree::getMonitorUid, ResourceTree::getGroupPath)
                .eq(ResourceTree::getTreeUid, catelogUid)
                .in(ResourceTree::getMonitorUid, monitorUids));
        if (resourceTrees1 == null) {
            return new HashSet<>();
        }
        Set<String> collect4 = Sets.newHashSet();
        Multimap<String, String> multimap = ArrayListMultimap.create();
        resourceTrees1.forEach(resourceTree -> {
            String[] split = resourceTree.getGroupPath().split("/");
            List<String> collect2 = Lists.newArrayList(split).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
            for (String s : collect2) {
                multimap.put(s, resourceTree.getMonitorUid());
            }
            collect4.addAll(collect2);
        });
        final String collect1 = collect4.stream().collect(Collectors.joining(","));
        final List<Long> resourceAuthByGroupIdAndRoleUid = resourceTreeMapper.getResourceAuthByGroupIdAndRoleUid("8ed1ba18107511ecac2f2aa909a6d642", collect1);
        final Map<String, Collection<String>> stringCollectionMap = multimap.asMap();
        Set<String> result = new HashSet<>();
        for (Long aLong : resourceAuthByGroupIdAndRoleUid) {
            final Collection<String> strings = stringCollectionMap.get(String.valueOf(aLong));
            if (strings != null) {
                result.addAll(strings);
            }
        }
        return result;
    }

    public List<String> roleAuthMonitors3(String roleUid, List<String> monitorUids) throws Exception {
        String catelogUid = "0000000000000001";
        String collect = monitorUids.stream().collect(Collectors.joining("','"));
        collect = "'" + collect + "'";
        return resourceTreeMapper.getResourceAuthByGroupIdAndRoleUid2(roleUid, collect, catelogUid);
    }

    /**
     * 查询监控点有没有任务 或者在地图上有没有落点信息
     *
     * @param resourceTreeList  监控点列表
     * @param monitorLabelNames 数组中目前包含algo-task，emap 代表地图上有无落点，或者监控点有无算法任务
     * @param algoUid
     */
    private void getMonitorEvents(List<ResourceTreeListVO> resourceTreeList, List<String> monitorLabelNames, String algoUid) {
        if (null == monitorLabelNames || monitorLabelNames.isEmpty()) {
            return;
        }
        if (null == resourceTreeList || resourceTreeList.isEmpty()) {
            return;
        }
        final List<String> monitorUidList = Optional.ofNullable(resourceTreeList).map(List::stream).orElseGet(Stream::empty)
                .filter(s -> ResourceNodeTypeEnum.MONITOR.getResourceTreeType().equals(s.getType()))
                .map(ResourceTreeListVO::getMonitorUid)
                .distinct()
                .collect(Collectors.toList());
        if (monitorUidList.isEmpty()) {
            return;
        }
        //当传algo-task的时候algoUid必须有值
        for (String monitorLabelName : monitorLabelNames) {
            if ("algo-task".equals(monitorLabelName)) {
                //    算法查询逻辑
                if (StringUtils.isBlank(algoUid)) {
                    continue;
                }
                List<MonitorEventRelGroupByUidVO> monitorEventByMonitor = monitorEventRelMapper.getMonitorEventByMonitorUid(monitorUidList, algoUid);
                if (monitorEventByMonitor == null || monitorEventByMonitor.isEmpty()) {
                    continue;
                }
                Map<String, MonitorEventRelGroupByUidVO> monitorEventRelGroupByUidMap = Optional.ofNullable(monitorEventByMonitor).map(List::stream).orElseGet(Stream::empty)
                        .collect(Collectors.toMap(MonitorEventRelGroupByUidVO::getMonitorUid, Function.identity(), (k1, k2) -> k2));
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeList) {
                    MonitorEventRelGroupByUidVO monitorEventRelGroupByUidVO = monitorEventRelGroupByUidMap.get(resourceTreeListVO.getMonitorUid());
                    if (monitorEventRelGroupByUidVO == null) {
                        continue;
                    } else {
                        List<MonitorEventRel> events = monitorEventRelGroupByUidVO.getEvents();
                        if (events == null || events.isEmpty()) {
                            continue;
                        } else {
                            List<MonitorLabelItemVO> monitorLabels = resourceTreeListVO.getMonitorLabels();
                            if (monitorLabels == null) {
                                monitorLabels = Lists.newArrayList();
                                MonitorLabelItemVO monitorLabelItemVO = new MonitorLabelItemVO();
                                monitorLabelItemVO.setName("algo-task");
                                monitorLabelItemVO.setDetail(events.get(0).getDetail());
                                monitorLabels.add(monitorLabelItemVO);
                            } else {
                                MonitorLabelItemVO monitorLabelItemVO = new MonitorLabelItemVO();
                                monitorLabelItemVO.setName("algo-task");
                                monitorLabelItemVO.setDetail(events.get(0).getDetail());
                                monitorLabels.add(monitorLabelItemVO);
                            }
                            resourceTreeListVO.setMonitorLabels(monitorLabels);
                        }

                    }
                }
            } else if ("emap".equals(monitorLabelName)) {
                List<Monitor> monitors = monitorMapper.selectList(Wrappers.<Monitor>lambdaQuery().in(Monitor::getMonitorUid, monitorUidList));
                if (monitors == null || monitors.isEmpty()) {
                    continue;
                }
                Map<String, Monitor> monitorMap = Optional.ofNullable(monitors).map(List::stream).orElseGet(Stream::empty)
                        .collect(Collectors.toMap(Monitor::getMonitorUid, Function.identity(), (k1, k2) -> k2));
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeList) {
                    Monitor monitor = monitorMap.get(resourceTreeListVO.getMonitorUid());
                    if (monitor == null) {
                        continue;
                    } else {
                        if (monitor.getLongitude() != null && monitor.getLatitude() != null) {
                            HashMap jsonObject = new HashMap();
                            jsonObject.put("Longitude", monitor.getLongitude().doubleValue());
                            jsonObject.put("Latitude", monitor.getLatitude().doubleValue());
                            List<MonitorLabelItemVO> monitorLabels = resourceTreeListVO.getMonitorLabels();
                            if (monitorLabels == null) {
                                monitorLabels = Lists.newArrayList();
                                MonitorLabelItemVO monitorLabelItemVO = new MonitorLabelItemVO();
                                monitorLabelItemVO.setName("emap");
                                monitorLabelItemVO.setDetail(jsonUtils.objectToJson(jsonObject));
                                monitorLabels.add(monitorLabelItemVO);
                            } else {
                                MonitorLabelItemVO monitorLabelItemVO = new MonitorLabelItemVO();
                                monitorLabelItemVO.setName("emap");
                                monitorLabelItemVO.setDetail(jsonUtils.objectToJson(jsonObject));
                                monitorLabels.add(monitorLabelItemVO);
                            }
                            resourceTreeListVO.setMonitorLabels(monitorLabels);
                        }
                    }
                }
            } else if ("video-plan".equals(monitorLabelName)) {
                List<MonitorEventRelGroupByUidVO> monitorEventByMonitor = monitorEventRelMapper.getMonitorEventByMonitorUid(monitorUidList, "Video");
                if (monitorEventByMonitor == null || monitorEventByMonitor.isEmpty()) {
                    continue;
                }
                Map<String, MonitorEventRelGroupByUidVO> monitorEventRelGroupByUidMap = Optional.ofNullable(monitorEventByMonitor).map(List::stream).orElseGet(Stream::empty)
                        .collect(Collectors.toMap(MonitorEventRelGroupByUidVO::getMonitorUid, Function.identity(), (k1, k2) -> k2));
                for (ResourceTreeListVO resourceTreeListVO : resourceTreeList) {
                    MonitorEventRelGroupByUidVO monitorEventRelGroupByUidVO = monitorEventRelGroupByUidMap.get(resourceTreeListVO.getMonitorUid());
                    if (monitorEventRelGroupByUidVO == null) {
                        continue;
                    } else {
                        List<MonitorEventRel> events = monitorEventRelGroupByUidVO.getEvents();
                        if (events == null || events.isEmpty()) {
                            continue;
                        } else {
                            List<MonitorLabelItemVO> monitorLabels = resourceTreeListVO.getMonitorLabels();
                            if (monitorLabels == null) {
                                monitorLabels = Lists.newArrayList();
                                MonitorLabelItemVO monitorLabelItemVO = new MonitorLabelItemVO();
                                monitorLabelItemVO.setName("video-plan");
                                monitorLabelItemVO.setDetail(events.get(0).getDetail());
                                monitorLabels.add(monitorLabelItemVO);
                            } else {
                                MonitorLabelItemVO monitorLabelItemVO = new MonitorLabelItemVO();
                                monitorLabelItemVO.setName("video-plan");
                                monitorLabelItemVO.setDetail(events.get(0).getDetail());
                                monitorLabels.add(monitorLabelItemVO);
                            }
                            resourceTreeListVO.setMonitorLabels(monitorLabels);
                        }

                    }
                }
            }
        }

    }
}
