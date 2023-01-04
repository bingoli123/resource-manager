package com.inspur.rms.rmspojo.cmspojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/8/19 10:26 上午
 * 批量操作 批量删除参数
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Builder
public class BatchDeleteDTO implements Serializable {
    private static final long serialVersionUID = -3424658606893013259L;


    @NotEmpty(message = "参数不能为空")
    @JsonProperty(value = "UIDList")
    private List<String> uidList;

}
