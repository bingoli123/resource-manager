package com.inspur.rms.rmspojo.DTO;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/11/10 3:03 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KongPluginCreateDTO implements Serializable {
    private static final long serialVersionUID = -8332076239679020402L;

    private String name;

    private List<String> protocols;

    private Boolean enabled;
}
