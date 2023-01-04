package com.inspur.rms.rmspojo.DTO;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : lidongbin
 * @date : 2021/9/27 9:51 上午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KongRoutesCreateDTO implements Serializable {
    private static final long serialVersionUID = -2092415018256101688L;

    //<service name>-<inner/outer>[-自定义名称]
    private String name;
    //kong访问地址，ip:port或域名
    private List<String> hosts = new ArrayList<>();
    //版本号
    private List<String> tags = new ArrayList<>();
    //路径，外部统一以“/ivideo”开头
    private List<String> paths = new ArrayList<>();

    private List<String> protocols = new ArrayList<>();
}
