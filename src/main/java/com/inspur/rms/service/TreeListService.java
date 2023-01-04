package com.inspur.rms.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dtflys.forest.http.ForestResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.inspur.ivideo.common.constant.ResourceNodeTypeEnum;
import com.inspur.ivideo.common.constant.TrueFalse;
import com.inspur.ivideo.common.entity.ValueResult;
import com.inspur.ivideo.common.exception.Res400Exception;
import com.inspur.ivideo.common.exception.ResException;
import com.inspur.ivideo.common.utils.BeanCopy;
import com.inspur.ivideo.common.utils.KeyUtils;
import com.inspur.rms.api.ResourceAuthApi;
import com.inspur.rms.constant.RmsResStatusEnum;
import com.inspur.rms.dao.ResourceTreeMapper;
import com.inspur.rms.dao.TreeListMapper;
import com.inspur.rms.rmspojo.DTO.EnableTreeDTO;
import com.inspur.rms.rmspojo.DTO.TreeUpdateDTO;
import com.inspur.rms.rmspojo.PO.ResourceTree;
import com.inspur.rms.rmspojo.PO.TreeList;
import com.inspur.rms.rmspojo.VO.TreeListVO;
import com.inspur.rms.rmspojo.VO.TreeSummaryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TreeListService extends ServiceImpl<TreeListMapper, TreeList> {


    private final TreeListMapper treeListMapper;
    private final ResourceTreeMapper resourceTreeMapper;
    private final ResourceAuthApi resourceAuthApi;

    @Autowired
    public TreeListService(TreeListMapper treeListMapper,
                           ResourceTreeMapper resourceTreeMapper, ResourceAuthApi resourceAuthApi) {
        this.treeListMapper = treeListMapper;
        this.resourceTreeMapper = resourceTreeMapper;
        this.resourceAuthApi = resourceAuthApi;
    }

    public List<TreeListVO> findByAll(TreeList treeList) {
        List<TreeList> list = treeListMapper.findByAll(treeList);
        return Optional.ofNullable(list).map(List::stream).orElseGet(Stream::empty)
                .map(s -> BeanCopy.beanCopy(s, TreeListVO.class))
                .collect(Collectors.toList());
    }


    /**
     * @data 查询虚拟资源详情
     */
    public TreeSummaryVO queryTreeSummary(String treeUid) throws ResException, Res400Exception {
        TreeList treeList = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeUid, treeUid));
        return Optional.ofNullable(treeList).map(s -> BeanCopy.beanCopy(s, TreeSummaryVO.class))
                .orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_NULL));
    }

    /**
     * @data 新增虚拟资源树
     */
    public void saveTree(TreeList treeList) throws Exception {
        String treeUid = KeyUtils.generatorUUID();
        checkTreeNameExist(treeList);
        treeList.setTreeType("1");
        treeList.setTreeStatus(TrueFalse.TRUE.isValue());
        treeList.setTreeUid(treeUid);
        treeListMapper.batchInsert(Lists.newArrayList(treeList));
//        创建虚拟资源树跟节点
        ResourceTree resourceTree1 = ResourceTree.builder()
                .groupName(treeList.getTreeName())
                .treeUid(treeUid)
                .type(ResourceNodeTypeEnum.CATELOG.getResourceTreeType())
                .parentId(0L)
                .groupUid(KeyUtils.generatorUUID())
                .build();
        resourceTreeMapper.insert(resourceTree1);
        resourceTree1.setGroupPath("/" + resourceTree1.getGroupId());
        resourceTreeMapper.updateById(resourceTree1);

    }

    private void checkTreeNameExist(TreeList treeList) throws Res400Exception {
        Integer integer = treeListMapper.selectCount(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeName, treeList.getTreeName()));
        if (integer > 0) {
            throw new Res400Exception(RmsResStatusEnum.RESO_TREE_NAME_EXIST);
        }
    }

    /**
     * @data 编辑虚拟资源树
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTree(String treeUid, TreeUpdateDTO treeUpdateDTO) throws Exception {
//
        Integer integer2 = treeListMapper.selectCount(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeName, treeUpdateDTO.getTreeName()).ne(TreeList::getTreeUid, treeUid));
        if (integer2 > 0) {
            throw new Res400Exception(RmsResStatusEnum.RESO_TREE_NAME_EXIST);
        }
        TreeList treeList1 = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeUid, treeUid));
        Integer integer = Optional.ofNullable(treeList1).map(s -> {
            BeanUtil.copyProperties(treeUpdateDTO, treeList1);
            s.setUpdatedTime(null);
            return treeListMapper.updateById(treeList1);
        }).orElseThrow(() -> new ResException(RmsResStatusEnum.RESO_QUERY_FAIL));
        if (integer <= 0) {
            throw new Res400Exception(RmsResStatusEnum.RESO_UPDATE_FAIL);
        }
        //修改resourceTree根目录的名称
        int update = resourceTreeMapper
                .update(null, Wrappers.<ResourceTree>lambdaUpdate()
                        .set(ResourceTree::getGroupName, treeUpdateDTO.getTreeName())
                        .eq(ResourceTree::getTreeUid, treeUid).and(s -> s.isNull(ResourceTree::getParentId).or().eq(ResourceTree::getParentId, 0)));
        if (update != 1) {
            throw new Res400Exception(RmsResStatusEnum.RESO_UPDATE_FAIL);
        }
    }

    /**
     * @Description 删除虚拟资源树
     */
    public void deleteTree(String treeUid) throws Res400Exception {
        TreeList treeList1 = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeUid, treeUid));
        if ("0".equals(treeList1.getTreeType())) {
            throw new Res400Exception(RmsResStatusEnum.CANNOT_DELETE_TREE);
        }
        //资源树如果绑定角色则不允许删除
        ForestResponse<ValueResult> roleSummary = resourceAuthApi.catelogUidRoleRel(treeUid);
        if (!roleSummary.isSuccess()) {
            throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
        }
        ValueResult valueResult = roleSummary.getResult();
        if (null == valueResult) {
            throw new Res400Exception(RmsResStatusEnum.QUERY_ROLE_TREE_FAIL);
        }
        if (!valueResult.isValue()) {
            throw new Res400Exception(RmsResStatusEnum.CATELOG_IS_HAS_BINDING_ROLE);
        }
        Optional.ofNullable(treeList1).map(s -> treeListMapper.deleteById(treeList1.getTreeId())).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.
                RESO_QUERY_FAIL));
    }

    /**
     * @data 禁用虚拟资源树
     */
    public void treeStatus(String treeUid, EnableTreeDTO enableTreeDTO) throws Exception {
        TreeList treeList1 = treeListMapper.selectOne(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeUid, treeUid));
        if ("0".equals(treeList1.getTreeType())) {
            throw new Res400Exception(RmsResStatusEnum.CANNOT_FORBAN_TREE);
        }
        Optional.ofNullable(treeList1).map(s -> {
            s.setTreeStatus(enableTreeDTO.getValue());
            s.setUpdatedTime(null);
            return treeListMapper.updateById(treeList1);
        }).orElseThrow(() -> new Res400Exception(RmsResStatusEnum.RESO_QUERY_FAIL));
    }


    public int batchInsert(List<TreeList> list) {
        return treeListMapper.batchInsert(list);
    }


    public HashMap catelogNameValidation(String treeName) throws Exception {
        HashMap<Object, Object> hashMap = Maps.newHashMap();
        Integer integer = treeListMapper.selectCount(Wrappers.<TreeList>lambdaQuery().eq(TreeList::getTreeName, treeName));
        if (integer > 0) {
            hashMap.put("Available", TrueFalse.FALSE.isValue());
        } else {
            hashMap.put("Available", TrueFalse.TRUE.isValue());
        }
        return hashMap;
    }

}

