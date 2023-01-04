package com.inspur.rms.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.inspur.ivideo.common.exception.Res400Exception;
import com.inspur.rms.constant.RmsResStatusEnum;
import com.inspur.rms.dao.ResourceTreeMapper;
import com.inspur.rms.rmspojo.PO.ResourceTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/11/9 3:10 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Service
public class ResourceSyncService {

    private final ResourceTreeMapper resourceTreeMapper;
    @Autowired
    private MonitorService monitorService;

    @Autowired
    public ResourceSyncService(ResourceTreeMapper resourceTreeMapper) {
        this.resourceTreeMapper = resourceTreeMapper;
    }

    /**
     * 设备下子资源名称经纬度等信息同步
     *
     * @param deviceUids 设备uid集合
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void deviceSync(List<String> deviceUids) throws Exception {
        resourceTreeMapper.syncDeviceResourceTreeName(deviceUids);
        resourceTreeMapper.syncDeviceMonitorName(deviceUids);
    }

    /**
     * 媒体下子资源名称经纬度等信息同步
     *
     * @param mediaUids 媒体uid集合
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void mediaSync(List<String> mediaUids) throws Exception {
        resourceTreeMapper.syncMediaResourceTreeName(mediaUids);
        resourceTreeMapper.syncMediaMonitorName(mediaUids);
    }

    /**
     * 设备下子资源名称经纬度等信息同步
     *
     * @param platformUid 平台uid
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void platformSync(String platformUid) throws Exception {
        resourceTreeMapper.syncPlatformResourceTreeName(platformUid);
        resourceTreeMapper.syncPlatformMonitorName(platformUid);
    }

    /**
     * 资源管理中一键同步 目录下所有监控点信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void catelogSync(String catelogUid, String groupUid, Boolean isForce) throws Exception {
        //查询groupUid对应的groupPath
        final ResourceTree resourceTree = resourceTreeMapper.selectOne(Wrappers.<ResourceTree>lambdaQuery().eq(ResourceTree::getGroupUid, groupUid));
        if (null == resourceTree) {
            throw new Res400Exception(RmsResStatusEnum.RESO_QUERY_FAIL);
        }
        if (isForce) {
            resourceTreeMapper.syncResourceCatelogDeviceName(resourceTree.getGroupPath(), catelogUid);
            resourceTreeMapper.syncResourceCatelogPlatformName(resourceTree.getGroupPath(), catelogUid);
        } else {
            resourceTreeMapper.syncResourceCatelogDeviceNameNotForce(resourceTree.getGroupPath(), catelogUid);
            resourceTreeMapper.syncResourceCatelogPlatformNameNotForce(resourceTree.getGroupPath(), catelogUid);
        }
        //    校验是否重名 如果重名的话直接提示错误并回滚
        //int ResourceCount = resourceTreeMapper.selectResourceCount(resourceTree.getGroupPath(), catelogUid, null);
        //int ResourceDiffNameCount = resourceTreeMapper.selectResourceDiffNameCount(resourceTree.getGroupPath(), catelogUid, null);
        //if (ResourceCount != ResourceDiffNameCount) {
        //    throw new Res400Exception(RmsResStatusEnum.TARGET_GROUP_INCLUDE_COPY_MONITOR2);
        //}


    }
}
