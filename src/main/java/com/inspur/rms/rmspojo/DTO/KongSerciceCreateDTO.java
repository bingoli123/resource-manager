package com.inspur.rms.rmspojo.DTO;

import lombok.*;

import java.io.Serializable;
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
public class KongSerciceCreateDTO implements Serializable {
    private static final long serialVersionUID = -2092415018256101688L;

    //The Service name.
    private String name;
    //The number of retries to execute upon failure to proxy. Default: 5.
    private Integer retries;
    //The protocol used to communicate with the upstream. Accepted values are: "grpc", "grpcs", "http", "https", "tcp", "tls", "udp". Default: "http".
    private String protocol;
    //<service name>.service.consul
    private String host;
    //服务监听端口，kong路由时优先使用域名解析中的端口
    private Integer port;
    ///<service name>[/...]
    private String path;
    //版本号
    private List<String> tags;
    //http://host:port/path
    private String url;
}
