package com.inspur.rms.rmspojo.PO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "dictionary_data")
public class DictionaryData implements Serializable {
    @TableField(value = "group_value")
    private String groupValue;

    @TableField(value = "`label`")
    private String label;

    @TableField(value = "`key`")
    private String key;

    @TableField(value = "sort")
    private Integer sort;

    private static final long serialVersionUID = 1L;

    public static final String COL_GROUP_VALUE = "group_value";

    public static final String COL_LABEL = "label";

    public static final String COL_KEY = "key";

    public static final String COL_SORT = "sort";
}