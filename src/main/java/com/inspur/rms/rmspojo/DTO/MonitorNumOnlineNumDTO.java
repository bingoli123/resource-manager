package com.inspur.rms.rmspojo.DTO;

import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2021/11/1 4:17 下午
 * @Copyright : 2021 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorNumOnlineNumDTO implements Serializable {
    private static final long serialVersionUID = -5447113742437082782L;

    private Long groupId;

    private Integer num;
}
