package com.inspur.rms.constant;

import com.inspur.ivideo.common.constant.BaseErrorInfo;

public enum RmsResStatusEnum implements BaseErrorInfo {
    //  资源管理
    RESO_COMMON_FAIL("846.011.001", "系统忙，请稍后再试!"),
    RESO_QUERY_NULL("846.011.002", "查询到的数据为空"),
    RESO_UPDATE_FAIL("846.011.003", "修改失败"),
    RESO_QUERY_FAIL("846.011.004", "查询失败"),
    RESO_TREE_NAME_EXIST("846.011.005", "资源树名称重复！"),
    RESO_CATELOG_EXIST("846.011.006", "目录名称重复"),
    RESO_CATELOG_HAS_RESOURCE("846.011.007", "目录下还有资源"),
    RESO_INMANAGE_RESOURCE_TREE_NOT_FOUNT("846.011.008", "目标目录不存在"),
    RESO_INMANAGE_DEVICE_NOT_FOUNT("846.011.009", "纳管的设备不存在"),
    RESO_EXIST_INMANAGE_SUBRESOURCE("846.011.010", "存在已经被纳管过的资源"),
    QUERY_ROLE_SUBRESOURCE_FAIL("846.011.011", "根据目录id查询目录信息失败"),
    RESOURCE_AUTH_API_FAIL("846.011.012", "调用资源权限接口失败"),
    RESOURCE_AUTH_QUERY_FAIL("846.011.012", "查询到的数据为空"),
    QUERY_ROLE_TREE_FAIL("846.011.013", "查询分组树失败"),
    ROLE_MONITOR_QUERY_FAIL("846.011.014", "监控点不存在"),
    PARAM_IS_NULL("846.011.014", "参数不能为空"),
    RESOURCE_TREE_DELETE_FAIL("846.011.015", "删除监控点节点失败"),
    TARGET_GROUP_INCLUDE_COPY_MONITOR("846.011.016", "目标节点下包含复制的监控点"),
    CATELOG_NOT_INCLUDE_GROUP("846.011.017", "此节点不再目录下"),
    MONITOR_ALREADY_IN_GROUP("846.011.018", "监控点已存在"),
    SUBRESOURCE_IS_INMANAGE("846.011.019", "资源已被纳管"),
    ROLE_RESOURCE_FAIL("846.011.020", "查询资源权限失败"),
    CAN_NOT_DELETE_ROOT("846.011.021", "不能删除分组树的根节点"),
    RESOURCE_GROUP_NOT_GT_10("846.011.022", "监控点目录层数不能大于10"),
    UNINMANAGE_FAIL("846.011.023", "关闭下级资源操作失败"),
    COON_COOR_API_FAIL("846.011.024", "调用连接协调接口失败"),
    USER_TREE_IS_FORBIDEN("846.011.025", "用户绑定资源树已禁用"),
    MONITOR_IS_DELETED("846.011.026", "该监控点已删除"),
    CANNOT_FORBAN_TREE("846.011.027", "不能禁用基本资源树"),
    CANNOT_DELETE_TREE("846.011.027", "不能删除基本资源树"),
    CATELOG_IS_HAS_BINDING_ROLE("846.011.028", "该虚拟资源树已经关联角色"),
    VALUE_IS_NULL("846.011.29", "启用禁用状态不能为空"),
    DELETE_DEVICE_CAT_NOT_IMMANAGE("846.011.030", "删除状态的设备不能纳管"),
    TARGET_GROUP_INCLUDE_COPY_MONITOR2("846.011.031", "存在相同名称的监控点"),
    TARGET_GROUP_INCLUDE_COPY_GROUP("846.011.033", "存在相同名称的分组"),
    CANNOT_DELETE_GROUP("846.011.032", "分组下存在监控点不能删除"),
    SUBNODE_GROUP_MORE_10000("846.011.033", "分组下的子分组数不能超过10000"),
    SUBNODE_MONITOR_MORE_10000("846.011.033", "分组下的子监控点数不能超过10000");

    RmsResStatusEnum(String status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private final String status;

    private final String msg;

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
