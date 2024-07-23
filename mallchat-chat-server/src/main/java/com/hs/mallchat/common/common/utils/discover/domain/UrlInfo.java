package com.hs.mallchat.common.common.utils.discover.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/22 - 17:03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UrlInfo {

    /**
     * 标题
     **/
    String title;

    /**
     * 描述
     **/
    String description;

    /**
     * 网站LOGO
     **/
    String image;

}
