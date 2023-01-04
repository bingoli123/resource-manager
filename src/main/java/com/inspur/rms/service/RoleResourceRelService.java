package com.inspur.rms.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.inspur.rms.dao.RoleResourceRelMapper;
import com.inspur.rms.rmspojo.PO.RoleResourceRel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoleResourceRelService extends ServiceImpl<RoleResourceRelMapper, RoleResourceRel> {


    private final RoleResourceRelMapper roleResourceRelMapper;

    @Autowired
    public RoleResourceRelService(RoleResourceRelMapper roleResourceRelMapper) {
        this.roleResourceRelMapper = roleResourceRelMapper;
    }

    public void saveRoleResourceRel(String roleUid, List<String> uidList) throws Exception {
        List<RoleResourceRel> list = Lists.newArrayList();
        for (String s : uidList) {
            RoleResourceRel roleResourceRel = RoleResourceRel.builder()
                    .roleUid(roleUid)
                    .groupUid(s)
                    .build();
            list.add(roleResourceRel);
        }
        roleResourceRelMapper.batchInsert(list);
    }

    public int batchInsert(List<RoleResourceRel> list) {
        return baseMapper.batchInsert(list);
    }

    /**
     * 删除角色所有的资源权限
     *
     * @param roleUid
     * @throws Exception
     */
    public void deleteRoleResourceRel(String roleUid) throws Exception {
        roleResourceRelMapper.delete(Wrappers.<RoleResourceRel>lambdaQuery().eq(RoleResourceRel::getRoleUid, roleUid));
    }

    /**
     *
     */

    public List<String> getRoleResourceRel(String roleUid) throws Exception {
        List<RoleResourceRel> roleResourceRels = roleResourceRelMapper.selectList(Wrappers.<RoleResourceRel>lambdaQuery().eq(RoleResourceRel::getRoleUid, roleUid));
        List<String> collect = Optional.ofNullable(roleResourceRels).map(List::stream).orElseGet(Stream::empty)
                .map(RoleResourceRel::getGroupUid).collect(Collectors.toList());
        return collect;
    }
}


