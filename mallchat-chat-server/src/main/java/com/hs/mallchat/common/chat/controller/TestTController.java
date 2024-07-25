package com.hs.mallchat.common.chat.controller;

import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.common.domain.vo.response.ApiResult;
import com.hs.mallchat.common.common.domain.vo.response.IdRespVO;
import com.hs.mallchat.transaction.service.MQProducer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Description: 模拟测试接口
 *
 * @Author: CZF
 * @Create: 2024/7/24 - 20:15
 */
@RestController
@RequestMapping("/capi/test")
@Api(tags = "模拟测试相关接口")
@Slf4j
public class TestTController {


    @Autowired
    private MQProducer mqProducer;
    @Autowired
    private MessageDao messageDao;

    @Autowired
    @Lazy
    private TestTController testController;

    @PostMapping("/secureInvoke")
    @ApiOperation("本地消息表")
    @Transactional
    public ApiResult<IdRespVO> secureInvoke(String msg) {
        //testController.exec();
        Message build = Message.builder()
                .fromUid(270L)
                .type(1)
                .content(msg)
                .roomId(1L)
                .status(0)
                .build();
        // 两个方法调用都会被切面类SecureInvokeAspect捕捉到
        messageDao.save(build);
        mqProducer.sendSecureMsg("test-topic", msg, msg);
        return ApiResult.success();
    }

    @Transactional
    public void exec() {
        mqProducer.sendSecureMsg("1", "123", "");
    }

    public ApiResult<IdRespVO> sendMsg(@Valid @RequestBody ChatMessageReq request, @RequestBody IdRespVO idRespVO) {
        return ApiResult.success(idRespVO);
    }


}
