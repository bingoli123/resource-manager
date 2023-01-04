package com.inspur.rms.rmspojo.VO;

import lombok.*;

import java.io.Serializable;

/**
 * @author : lidongbin
 * @date : 2022/2/14 11:10 AM
 * @Copyright : 2022 www.inspur.com Inc. All rights reserved.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParentIdCountVO implements Serializable {
    private static final long serialVersionUID = 6310608131422740042L;

    private Long parentId;

    private Integer count;


}
