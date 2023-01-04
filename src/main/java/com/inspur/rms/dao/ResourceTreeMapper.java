package com.inspur.rms.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inspur.rms.rmspojo.DTO.MonitorNumOnlineNumDTO;
import com.inspur.rms.rmspojo.PO.ResourceTree;
import com.inspur.rms.rmspojo.VO.MonitorListVO;
import com.inspur.rms.rmspojo.VO.MonitorOnlineGroupNameVO;
import com.inspur.rms.rmspojo.VO.ParentIdCountVO;
import com.inspur.rms.rmspojo.VO.ResourceTreeMonitorVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceTreeMapper extends BaseMapper<ResourceTree> {
    List<ResourceTree> findByAll(ResourceTree resourceTree);

    int batchInsert(@Param("list") List<ResourceTree> list);

    List<MonitorListVO> selecResourceTreeMonitor(@Param("groupId") Long groupId, @Param("monitorName") String monitorName);

    List<ResourceTreeMonitorVO> selectResourceTreeMonitor(@Param("groupId") Long groupId, @Param("name") String name, @Param("groupPath") String groupPath, @Param("recursion") String recursion, @Param("online") Integer online);

    List<ResourceTreeMonitorVO> selectResourceTreeMonitorAbility(@Param("groupId") Long groupId,
                                                                 @Param("name") String name,
                                                                 @Param("groupPath") String groupPath,
                                                                 @Param("recursion") String recursion,
                                                                 @Param("abilityType") String abilityType,
                                                                 @Param("ablityConfigStatus") Boolean ablityConfigStatus,
                                                                 @Param("ablityWorkStatus") String ablityWorkStatus);

    List<ResourceTreeMonitorVO> selectAllResourceTreeMonitor(@Param("catelogUid") String catelogUid);

    List<ResourceTree> getResourceTreeByRoleUidAndGroupId(@Param("roleUid") String roleUid, @Param("groupPath") String groupPath);

    Integer getResourceTreeByRoleUidAndGroupIdCount(@Param("roleUid") String roleUid, @Param("groupPath") String groupPath);

    List<ResourceTree> roleGroupNameSearch(@Param("groupPathLike") String groupPathLike, @Param("groupName") String groupName, @Param("groupIds") List<String> groupIds);

    List<ResourceTree> searchResourceName(@Param("name") String name, @Param("treeUid") String treeUid);

    //    查询资源目录下的监控点在线数量和总数量 1在线 2离线
    List<MonitorNumOnlineNumDTO> getMonitorOnlineNum(@Param("treeUid") String treeUid, @Param("online") String online);

    int syncDeviceResourceTreeName(@Param("deviceUids") List<String> deviceUids);

    int syncDeviceMonitorName(@Param("deviceUids") List<String> deviceUids);

    int syncMediaResourceTreeName(@Param("mediaUids") List<String> mediaUids);

    int syncMediaMonitorName(@Param("mediaUids") List<String> mediaUids);

    int syncPlatformResourceTreeName(@Param("platformUid") String platformUid);

    int syncPlatformMonitorName(@Param("platformUid") String platformUid);

    //强制同步设备 媒体的名称
    int syncResourceCatelogDeviceName(@Param("groupPath") String groupPath, @Param("treeUid") String treeUid);

    //强制同步边缘平台的名称
    int syncResourceCatelogPlatformName(@Param("groupPath") String groupPath, @Param("treeUid") String treeUid);

    //强制同步设备 媒体的名称
    int syncResourceCatelogDeviceNameNotForce(@Param("groupPath") String groupPath, @Param("treeUid") String treeUid);

    //强制同步边缘平台的名称
    int syncResourceCatelogPlatformNameNotForce(@Param("groupPath") String groupPath, @Param("treeUid") String treeUid);

    List<Long> getResourceAuthByGroupIdAndRoleUid(@Param("roleUid") String roleUid, @Param("groupUidStr") String groupUidStr);

    List<MonitorOnlineGroupNameVO> getMonitorOnlineAndGroupName(@Param("monitorUids") List<String> monitorUids);

    List<String> getResourceAuthByGroupIdAndRoleUid2(@Param("roleUid") String roleUid, @Param("monitorUids") String monitorUids, @Param("catelogUid") String catelogUid);

    List<ParentIdCountVO> queryByParentIdIn(@Param("parentIdCollection") List<Long> parentIdCollection);

    int selectResourceCount(@Param("groupPath") String groupPath, @Param("treeUid") String treeUid, @Param("groupUids") List<String> groupUids);

    int selectResourceDiffNameCount(@Param("groupPath") String groupPath, @Param("treeUid") String treeUid, @Param("groupUids") List<String> groupUids);

    int getGroupMonitorNum(@Param("catelogUid") String catelogUid, @Param("groupPaths") List<String> groupPaths, @Param("excludeMonitorUids") List<String> excludeMonitorUids);
}