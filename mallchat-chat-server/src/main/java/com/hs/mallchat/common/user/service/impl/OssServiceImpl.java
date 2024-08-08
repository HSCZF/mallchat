package com.hs.mallchat.common.user.service.impl;

import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.user.domain.enums.OssSceneEnum;
import com.hs.mallchat.common.user.domain.vo.request.oss.UploadUrlReq;
import com.hs.mallchat.common.user.service.OssService;
import com.hs.mallchat.oss.MinIOTemplate;
import com.hs.mallchat.oss.domain.OssReq;
import com.hs.mallchat.oss.domain.OssResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/8/8 - 9:31
 */
@Service
public class OssServiceImpl implements OssService {

    @Autowired
    private MinIOTemplate minIOTemplate;

    /**
     * 获取临时的上传链接
     *
     * @param uid
     * @param req
     */
    @Override
    public OssResp getUploadUrl(Long uid, UploadUrlReq req) {
        OssSceneEnum sceneEnum = OssSceneEnum.of(req.getScene());
        AssertUtil.isNotEmpty(sceneEnum, "场景有误");
        OssReq ossReq = OssReq.builder()
                .fileName(req.getFileName())
                .filePath(sceneEnum.getPath())
                .uid(uid)
                .build();
        return minIOTemplate.getPreSignedObjectUrl(ossReq);
    }
}
