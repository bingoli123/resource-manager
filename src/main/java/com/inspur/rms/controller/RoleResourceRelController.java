package com.inspur.rms.controller;

import com.google.common.base.Preconditions;
import com.inspur.rms.rmspojo.DTO.BatchParamDTO;
import com.inspur.rms.service.RoleResourceRelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/9 8:07 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@RestController
@Slf4j
@RequestMapping("/resource-manager/v3")
public class RoleResourceRelController {

    private final RoleResourceRelService roleResourceRelService;

    public RoleResourceRelController(RoleResourceRelService roleResourceRelService) {
        this.roleResourceRelService = roleResourceRelService;
    }

    /**
     * 添加角色和资源关系
     */
    @PostMapping("/role/{RoleUID}/resourcerel")
    public ResponseEntity<Object> saveRoleResourceRel(
            @PathVariable("RoleUID") String roleUid,
            @RequestBody BatchParamDTO batchParamDTO) throws Exception {
        Preconditions.checkNotNull(roleUid);
        Preconditions.checkNotNull(batchParamDTO);
        Preconditions.checkNotNull(batchParamDTO.getUidList());
        roleResourceRelService.saveRoleResourceRel(roleUid, batchParamDTO.getUidList());
        return ResponseEntity.ok(null);
    }

    /**
     * 删除角色和资源关系
     */
    @DeleteMapping("/role/{RoleUID}/resourcerel")
    public ResponseEntity<Object> deleteRoleResourceRel(@PathVariable("RoleUID") String roleUid) throws Exception {
        Preconditions.checkNotNull(roleUid);
        roleResourceRelService.deleteRoleResourceRel(roleUid);
        return ResponseEntity.ok(null);
    }

    /**
     * 查询角色和资源权限关系
     */
    @GetMapping("/role/{RoleUID}/resourcerel")
    public ResponseEntity<List<String>> getRoleResourceRel(@PathVariable("RoleUID") String roleUid) throws Exception {
        List<String> roleResourceRel = roleResourceRelService.getRoleResourceRel(roleUid);
        return ResponseEntity.ok(roleResourceRel);
    }

}
