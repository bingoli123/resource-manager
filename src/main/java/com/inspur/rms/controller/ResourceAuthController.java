package com.inspur.rms.controller;

import com.dtflys.forest.http.ForestResponse;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.inspur.ivideo.common.constant.AuthHeaderKeyConst;
import com.inspur.ivideo.common.entity.BatchActionResult;
import com.inspur.ivideo.common.entity.PageData;
import com.inspur.ivideo.common.exception.Res404Exception;
import com.inspur.ivideo.common.exception.ResException;
import com.inspur.rms.api.ResourceAuthApi;
import com.inspur.rms.constant.RmsResStatusEnum;
import com.inspur.rms.rmspojo.DTO.AuthorizationDTO;
import com.inspur.rms.rmspojo.DTO.ResourcePermissionParamDTO;
import com.inspur.rms.rmspojo.VO.ResourceTreeListVO;
import com.inspur.rms.rmspojo.VO.RoleAuthMonitor;
import com.inspur.rms.rmspojo.pmspojo.UserSummaryVO;
import com.inspur.rms.service.ResourceAuthService;
import com.inspur.rms.utils.AuthorizationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;

/**
 * @author : lidongbin
 * @date : 2021/9/9 5:15 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@RestController
@Slf4j
@RequestMapping("/resource-manager/v3")
public class ResourceAuthController {

    private final ResourceAuthService resourceAuthService;
    private final ResourceAuthApi resourceAuthApi;

    @Autowired
    public ResourceAuthController(ResourceAuthService resourceAuthService, ResourceAuthApi resourceAuthApi) {
        this.resourceAuthService = resourceAuthService;
        this.resourceAuthApi = resourceAuthApi;
    }

    /**
     * 查询资源树(分层查询) 和 搜索
     */
    @GetMapping("/catelogs/{CatelogUID}/nodes")
    public ResponseEntity<PageData<ResourceTreeListVO>> getSubresource(@PathVariable("CatelogUID") String catelogUid,
                                                                       @RequestParam(value = "Page", required = false) Integer page,
                                                                       @RequestParam(value = "PerPage", required = false) Integer perPage,
                                                                       @RequestParam(value = "ParentID", required = false) Long parentId,
                                                                       @RequestParam(value = "Name", required = false) String name,
                                                                       @RequestParam(value = "MonitorLabelNames", required = false) List<String> monitorLabelNames,
                                                                       @RequestParam(value = "AlgorithmUID", required = false) String algoUid) throws Exception {
        PageData<ResourceTreeListVO> pageData = new PageData();
        if (page == null && perPage == null) {
//            查询下级子节点
            pageData = resourceAuthService.getSubresources(parentId, catelogUid, monitorLabelNames, algoUid);
        } else {
//            搜索
            name = URLDecoder.decode(name.replace("\\", "\\\\").replace("%", "%25"), "utf-8").replace("%", "\\%").replace("_", "\\_");
            pageData = resourceAuthService.resourceTreeSearch(page, perPage, name, catelogUid, monitorLabelNames, algoUid);
        }
        return ResponseEntity.ok(pageData);
    }

    /**
     * 分层查询用户资源树  用户资源树搜索
     */
    @GetMapping("/users/nodes")
    public ResponseEntity<PageData<ResourceTreeListVO>> getUserSbresource(
            @RequestHeader HttpHeaders headers,
            @RequestParam(value = "Page", required = false) Integer page,
            @RequestParam(value = "PerPage", required = false) Integer perPage,
            @RequestParam(value = "ParentID", required = false) Long parenId,
            @RequestParam(value = "Name", required = false) String name, HttpServletRequest request,
            @RequestParam(value = "MonitorLabelNames", required = false) List<String> monitorLabelNames,
            @RequestParam(value = "AlgorithmUID", required = false) String algoUid) throws Exception {
        String accountName = "";
        String xagent = request.getHeader(AuthHeaderKeyConst.XAGENT);
        if (xagent.equalsIgnoreCase(AuthHeaderKeyConst.AGENT_CLOUD)) {
            accountName = "admin";
        } else {
            String authorization = headers.getFirst("authorization");
            AuthorizationDTO dto = AuthorizationUtils.parseAuthorization(authorization);
            accountName = dto.getAccountName();
        }
        UserSummaryVO accountSummaryVO = queryAccountSummary(accountName);
        String roleUid = accountSummaryVO.getRoleUid();
//        String roleUid = "8ed1ba18107511ecac2f2aa909a6d642";
        PageData<ResourceTreeListVO> resourceTreeListVoPageData = null;
        if (StringUtils.isNotBlank(name)) {
//            搜索
            name = URLDecoder.decode(name.replace("%", "%25"), "utf-8").replace("%", "\\%").replace("_", "\\_").replace("\\", "\\\\");
            resourceTreeListVoPageData = resourceAuthService.roleResourceTreeSearch(page, perPage, name, roleUid, monitorLabelNames, algoUid);
        } else {
//            分层查询
            List<ResourceTreeListVO> roleSubresource = Lists.newArrayList();
            if (parenId != null) {
                roleSubresource = resourceAuthService.getRoleSubresource(parenId, roleUid, monitorLabelNames, algoUid);
            } else {
                roleSubresource = resourceAuthService.getRoleSubresource(0L, roleUid, monitorLabelNames, algoUid);
            }
            resourceTreeListVoPageData = new PageData<>();
            resourceTreeListVoPageData.setCompleted(true);
            resourceTreeListVoPageData.setTotals(roleSubresource.size());
            resourceTreeListVoPageData.setValues(roleSubresource);
        }
        return ResponseEntity.ok(resourceTreeListVoPageData);
    }

    /**
     * 分层查询角色资源树 角色资源树搜索
     */
    @GetMapping("/roles/{RoleUID}/nodes")
    public ResponseEntity<PageData<ResourceTreeListVO>> getUserSbresource(
            @PathVariable("RoleUID") String roleUid,
            @RequestParam(value = "Page", required = false) Integer page,
            @RequestParam(value = "PerPage", required = false) Integer perPage,
            @RequestParam(value = "ParentID", required = false) Long parentId,
            @RequestParam(value = "Name", required = false) String name,
            @RequestParam(value = "MonitorLabelNames", required = false) List<String> monitorLabelNames,
            @RequestParam(value = "AlgorithmUID", required = false) String algoUid) throws Exception {
        PageData<ResourceTreeListVO> resourceTreeListVoPageData = null;
        if (StringUtils.isNotBlank(name)) {
            name = URLDecoder.decode(name.replace("%", "%25"), "utf-8").replace("%", "\\%").replace("_", "\\_").replace("\\", "\\\\");
            resourceTreeListVoPageData = resourceAuthService.roleResourceTreeSearch(page, perPage, name, roleUid, monitorLabelNames, algoUid);
        } else {
            List<ResourceTreeListVO> roleSubresource = Lists.newArrayList();
            if (parentId != null) {
//                ResourceTree groupIdbyGroupUid = resourceAuthService.getGroupIdbyGroupUid(parentUid);
                roleSubresource = resourceAuthService.getRoleSubresource(parentId, roleUid, monitorLabelNames, algoUid);
            } else {
                roleSubresource = resourceAuthService.getRoleSubresource(0L, roleUid, monitorLabelNames, algoUid);
            }
            resourceTreeListVoPageData = new PageData<>();
            resourceTreeListVoPageData.setCompleted(true);
            resourceTreeListVoPageData.setTotals(roleSubresource.size());
            resourceTreeListVoPageData.setValues(roleSubresource);
        }
        return ResponseEntity.ok(resourceTreeListVoPageData);
    }

    /**
     * 资源树查询结果跳转
     */
    @GetMapping("/catelogs/{CatelogUID}/resources/{ID}/path")
    public ResponseEntity<List<List<ResourceTreeListVO>>> jupmToResource(
            @PathVariable("ID") Long groupId,
            @PathVariable("CatelogUID") String treeUid,
            @RequestParam(value = "MonitorLabelNames", required = false) List<String> monitorLabelNames,
            @RequestParam(value = "AlgorithmUID", required = false) String algoUid) throws Exception {
        Preconditions.checkNotNull(groupId);
        Preconditions.checkNotNull(treeUid);
        List<List<ResourceTreeListVO>> lists = resourceAuthService.jupmToResource(groupId, monitorLabelNames, algoUid);
        return ResponseEntity.ok(lists);
    }

    /**
     * 用户资源树查询结果跳转
     */
    @GetMapping("/users/resources/{ID}/path")
    public ResponseEntity<List<List<ResourceTreeListVO>>> userJupmToResource(
            @RequestHeader HttpHeaders headers,
            @PathVariable("ID") Long groupId, HttpServletRequest request,
            @RequestParam(value = "MonitorLabelNames", required = false) List<String> monitorLabelNames,
            @RequestParam(value = "AlgorithmUID", required = false) String algoUid) throws Exception {
        Preconditions.checkNotNull(groupId);
        String accountName = "";
        String xagent = request.getHeader(AuthHeaderKeyConst.XAGENT);
        if (xagent.equalsIgnoreCase(AuthHeaderKeyConst.AGENT_CLOUD)) {
            accountName = "admin";
        } else {
            String authorization = headers.getFirst("authorization");
            AuthorizationDTO dto = AuthorizationUtils.parseAuthorization(authorization);
            accountName = dto.getAccountName();
        }

        UserSummaryVO accountSummaryVO = queryAccountSummary(accountName);
        String roleUid = accountSummaryVO.getRoleUid();
//        String roleUid = "8ed1ba18107511ecac2f2aa909a6d642";
        List<List<ResourceTreeListVO>> lists = resourceAuthService.roleJupmToResource(groupId, roleUid, monitorLabelNames, algoUid);
        return ResponseEntity.ok(lists);
    }

    /**
     * 角色资源树查询结果跳转
     */
    @GetMapping("/roles/{RoleUID}/resources/{ID}/path")
    public ResponseEntity<List<List<ResourceTreeListVO>>> roleJupmToResource(
            @PathVariable("ID") Long groupId,
            @PathVariable("RoleUID") String roleUid,
            @RequestParam(value = "MonitorLabelNames", required = false) List<String> monitorLabelNames,
            @RequestParam(value = "AlgorithmUID", required = false) String algoUid) throws Exception {
        Preconditions.checkNotNull(groupId);
        Preconditions.checkNotNull(roleUid);
        List<List<ResourceTreeListVO>> lists = resourceAuthService.roleJupmToResource(groupId, roleUid, monitorLabelNames, algoUid);
        return ResponseEntity.ok(lists);
    }

    /**
     * 根据角色权限节点查询路径节点和权限节点
     */
    @GetMapping("/resource-permissions/{UID}")
    public ResponseEntity<List<ResourceTreeListVO>> getRoleResource(@PathVariable("UID") @NotEmpty(message = "参数不能为空") List<String> roleUids) throws Exception {
        List<ResourceTreeListVO> roleResource = resourceAuthService.getRoleResource(roleUids);
        return ResponseEntity.ok(roleResource);
    }

    private UserSummaryVO queryAccountSummary(String userName) throws Exception {
        ForestResponse<UserSummaryVO> response = resourceAuthApi.getAccounrSummary(userName);
        log.info("调用资源权限接口返回--{},{},{}", response.isSuccess(), response.getContent(), response.getResult());
        if (!response.isSuccess()) {
            throw new ResException(RmsResStatusEnum.RESOURCE_AUTH_API_FAIL);
        }
        if (response.getResult() == null) {
            throw new ResException(RmsResStatusEnum.RESOURCE_AUTH_QUERY_FAIL);
        }
        return response.getResult();
    }

    /**
     * 判断角色下是否有监控点权限
     */
    @GetMapping("/monitors/{MonitorUID}/permission")
    public ResponseEntity<RoleAuthMonitor> roleMonitorAuth(@RequestParam("ResourcePermissionUID") String roleUid, @PathVariable("MonitorUID") String monitorUid) throws Exception {
        Preconditions.checkNotNull(monitorUid);
        Preconditions.checkNotNull(roleUid);
        List<RoleAuthMonitor> roleAuthMonitors = resourceAuthService.roleAuthMonitor(roleUid, Lists.newArrayList(monitorUid));
        if (null == roleAuthMonitors || roleAuthMonitors.isEmpty()) {
            throw new Res404Exception(RmsResStatusEnum.ROLE_MONITOR_QUERY_FAIL);
        }
        return ResponseEntity.ok(roleAuthMonitors.get(0));
    }

    /**
     * 批量判断角色下是否有监控点权限
     */
    @PostMapping("/monitors/permission/search")
    public ResponseEntity<BatchActionResult<RoleAuthMonitor>> roleMonitorAuths(@RequestBody @Valid ResourcePermissionParamDTO resourcePermissionParamDTO) throws Exception {
        BatchActionResult<RoleAuthMonitor> roleAuthMonitors = resourceAuthService.roleAuthMonitors(resourcePermissionParamDTO.getResourcePermissionUid(), resourcePermissionParamDTO.getMonitorUidList());
        return ResponseEntity.ok(roleAuthMonitors);
    }

    @PostMapping("/test")
    public Set<String> roleAuthMonitors2(@RequestBody List<String> monitorUid) throws Exception {
        long start = System.currentTimeMillis();
        final Set<String> strings = resourceAuthService.roleAuthMonitors2("8ed1ba18107511ecac2f2aa909a6d642", monitorUid);
        long end = System.currentTimeMillis();
        log.info("执行时间---{}，size---{}", end - start, strings.size());
        return strings;
    }

    @PostMapping("/test2")
    public List<String> roleAuthMonitors3(@RequestBody List<String> monitorUid) throws Exception {
        long start = System.currentTimeMillis();
        final List<String> strings = resourceAuthService.roleAuthMonitors3("8ed1ba18107511ecac2f2aa909a6d642", monitorUid);
        long end = System.currentTimeMillis();
        log.info("执行时间---{}，size---{}", end - start, strings.size());
        return strings;
    }

    @PostMapping("/users/{UserName}/nodes")
    public PageData<List<String>> roleAuthMonitors(@RequestBody List<String> monitorUid, @PathVariable("UserName") String userName) throws Exception {
        UserSummaryVO accountSummaryVO = queryAccountSummary(userName);
        String roleUid = accountSummaryVO.getRoleUid();
        long start = System.currentTimeMillis();
        final List<String> strings = resourceAuthService.roleAuthMonitors3(roleUid, monitorUid);
        long end = System.currentTimeMillis();
        log.info("执行时间---{}，size---{}", end - start, strings.size());
        PageData pageData = new PageData(strings);
        return pageData;
    }
}

