package com.inspur.rms.rmspojo.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

/**
 * 字典表
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "`dictionary`")
public class Dictionary implements Serializable {
    public static final String COL_VALUE = "value";
    public static final String COL_NAME = "name";
    public static final String COL_SORT = "sort";
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分组名称
     */
    @TableField(value = "group_value")
    private String groupValue;

    /**
     * 分组名称
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * 关联分组
     */
    @TableField(value = "connection_group")
    private String connectionGroup;

    /**
     * 版本号
     */
    @TableField(value = "version")
    private Integer version;

    /**
     * 数据基本类型还是高级类型(1:基本类型2:高级类型，除国家行政区域填2外，其余皆可填1，视具体情况而定)
     */
    @TableField(value = "`scope`")
    private Integer scope;

    /**
     * 对用添加的字典映射为数据结构表示为list还是tree(1:list, 2:tree, 除国家行政区域填2外，其余默认填1，视具体情况而定)
     */
    @TableField(value = "struct")
    private Integer struct;

    /**
     * 对应添加该字典的服务名
     */
    @TableField(value = "`owner`")
    private String owner;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_GROUP_VALUE = "group_value";

    public static final String COL_GROUP_NAME = "group_name";

    public static final String COL_CONNECTION_GROUP = "connection_group";

    public static final String COL_VERSION = "version";

    public static final String COL_SCOPE = "scope";

    public static final String COL_STRUCT = "struct";

    public static final String COL_OWNER = "owner";
}