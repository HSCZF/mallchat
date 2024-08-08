package com.hs.mallchat.common.user.service;

import com.hs.mallchat.common.user.domain.vo.request.oss.UploadUrlReq;
import com.hs.mallchat.oss.domain.OssResp;

/**
 * Description: 服务类
 *
 * @Author: CZF
 * @Create: 2024/8/8 - 9:30
 */
public interface OssService {

    /**
     * 获取临时的上传链接
     */
    OssResp getUploadUrl(Long uid, UploadUrlReq req);

}
