package com.hs.mallchat.common.user.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @Author: CZF
 * @Create: 2024/6/17 - 11:42
 * Description:
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpDetail {
    private String ip;
    private String isp;
    private String isp_id;
    private String city;
    private String city_id;
    private String country;
    private String county_id;
    private String region;
    private String region_id;
}
