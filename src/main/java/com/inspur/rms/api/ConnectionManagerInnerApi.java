package com.inspur.rms.api;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import com.inspur.rms.rmspojo.cmspojo.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/28 2:31 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Component
@BaseRequest(
        baseURL = "${inner_api}",
        contentType = "application/json"
)
public interface ConnectionManagerInnerApi {
    /**
     * 查询下级资源信息未纳管的通道
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/subresources",
            dataType = "json"
    )
    ForestResponse<List<Subresource>> getSubresourceList(@JSONBody List<String> deviceUids);

    /**
     * 根据通道uid查询通道信息
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/subresourcesall",
            dataType = "json"
    )
    ForestResponse<List<Subresource>> getSubresourceAllList(@JSONBody List<String> subresourceUids);

    /**
     * 根据设备uid查询设备下的全部通道
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/resources/subresources",
            dataType = "json"
    )
    ForestResponse<List<Subresource>> subresources(@JSONBody List<String> uidlists);

    /**
     * 查询资源
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/media",
            dataType = "json"
    )
    ForestResponse<List<Media>> getMedia(@JSONBody List<String> mediaUids);


    /**
     * 查询设备信息
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/devices",
            dataType = "json"
    )
    ForestResponse<List<Device>> getDevice(@JSONBody List<String> deviceUids);

    //    删除平台下的设备 修改总数量或者纳管数量
    @PostRequest(
            url = "/connection-manager/v3/inner/updateSubresourceNum",
            dataType = "json"
    )
    ForestResponse<List<Device>> updateSubresourceNum(@JSONBody List<String> groupUids, @Query("upper") boolean upper);


    //    取消纳管 新增纳管 修改纳管数量
    @PostRequest(
            url = "/connection-manager/v3/inner/updateInManageNum",
            dataType = "json"
    )
    ForestResponse<List<Device>> updateInManageNum(@JSONBody List<String> groupUids, @Query("upper") boolean upper);

    //    设备取消纳管新增纳管修改设通道的总数量
    @PostRequest(
            url = "/connection-manager/v3/inner/deviceCascadeNum",
            dataType = "json"
    )
    ForestResponse<List<Device>> deviceCascadeNum(@JSONBody List<String> subresourceUids, @Query("upper") boolean upper);

    //    媒体取消纳管新增纳管修改设通道的总数量
    @PostRequest(
            url = "/connection-manager/v3/inner/mediaCascadeNum",
            dataType = "json"
    )
    ForestResponse<List<Device>> mediaCascadeNum(@JSONBody List<String> subresourceUids, @Query("upper") boolean upper);


    /**
     * 根据节点uids查询平台树下的节点信息
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/platforms/groups",
            dataType = "json"
    )
    ForestResponse<List<PlatformDevice>> getPlatformedevice(@JSONBody List<String> uidlist);

    /**
     * 根据节点uids查询平台树下的节点信息 未删除的节点
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/platforms/groups2",
            dataType = "json"
    )
    ForestResponse<List<PlatformDevice>> getPlatformedevice2(@JSONBody List<String> uidlist);


    /**
     * 根据节点deviceUid查询平台树下的节点信息
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/platforms/devices",
            dataType = "json"
    )
    ForestResponse<List<PlatformDevice>> getPlatformedeviceDeviceId(@JSONBody List<String> uidlist);

    /**
     * 根据节点uids查询出这些节点中的目录类型
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/platforms/catelogs",
            dataType = "json"
    )
    ForestResponse<List<PlatformDevice>> getCatelogByGroupUid(@JSONBody List<String> uidlist);

    /**
     * 查询出以此groupuid为父节点的子节点的目录和设备
     */
    @PostRequest(
            url = "/connection-manager/v3/inner/platforms/parentuid",
            dataType = "json"
    )
    ForestResponse<List<PlatformDevice>> getCatelogOrDeviceByParentUid(@Query("GroupUID") String groupUid, @Query("NodeType") String nodeType, @Query("PlatformUID") String platformUid);

    /**
     * 根据grouppath模糊查询
     */
    @GetRequest(
            url = "/connection-manager/v3/inner/platform/grouppath",
            dataType = "json"
    )
    ForestResponse<List<PlatformDevice>> getCatelogOrDeviceByParentUid(@Query("GroupPath") String groupPath);


    /**
     * 资源管理总取消纳管
     */

    @PostRequest(
            url = "/connection-manager/v3/inner/subresource/outmanage",
            dataType = "json"
    )
    ForestResponse<List<PlatformDevice>> outmanage(@JSONBody List<UpdateSubResourceNumDTO> parems, @Query("upper") boolean upper);

    /**
     * 根据grouppath模糊查询
     */
    @GetRequest(
            url = "/connection-manager/v3/inner/platform/grouppath2",
            dataType = "json"
    )
    ForestResponse<List<PlatformDevice>> getCatelogOrDeviceByParentUid2(@Query("GroupPath") String groupPath);


    /**
     * 根据grouppath模糊查询
     */
    @GetRequest(
            url = "/connection-manager/v3/inner/platforms",
            dataType = "json"
    )
    ForestResponse<Platform> getPlatform(@Query("UID") String uid);

    @PostRequest(
            url = "/connection-manager/v3/inner/subresource/resource",
            dataType = "json"
    )
    ForestResponse<List<DeviceMediaPlatformSubresourceVO>> deviceMediaPlatformSubresource(@JSONBody SubresourceDTO subresourceDTO);
}
