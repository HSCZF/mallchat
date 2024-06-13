package com.hs.mallchat.common.websocket;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.hs.mallchat.common.websocket.domain.enums.WSReqTypeEnum;
import com.hs.mallchat.common.websocket.domain.vo.request.WSBaseReq;
import com.hs.mallchat.common.websocket.service.WebSocketService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: CZF
 * @Create: 2024/5/27 - 17:06
 */
@Slf4j
@Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    private WebSocketService webSocketService;

    /**
     * 当web客户端连接后，触发该方法
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        webSocketService = SpringUtil.getBean(WebSocketService.class);
        webSocketService.connect(ctx.channel());
    }

    /**
     * 用户事件触发器，http握手连接
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            if (StrUtil.isNotBlank(token)) {
                webSocketService.authorize(ctx.channel(), token);
            }
            System.out.println("握手完成！");
        } else if (evt instanceof IdleStateEvent) {
            // 心跳包
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("读空闲时间");
                //30s心跳时间到了，用户下线
                userOffLine(ctx.channel());
            }
        }
    }

    /**
     * 用户下线，统一处理
     *
     * @param channel
     */
    private void userOffLine(Channel channel) {
        webSocketService.remove(channel);
        channel.close();
    }


    /**
     * 用户下线
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        userOffLine(ctx.channel());
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String text = msg.text();
        // 拿到text，反序列化
        WSBaseReq wsBaseReq = JSONUtil.toBean(text, WSBaseReq.class);
        System.out.println("channelRead0拿到的text反序列化数据：" + wsBaseReq.toString());
        switch (WSReqTypeEnum.of(wsBaseReq.getType())) {
            case AUTHORIZE:
                // {
                //    "type":3,
                //    "data": "你的token值"
                // }
                // 去连接认证拿到token，在断开在连接输入这个
                webSocketService.authorize(ctx.channel(), wsBaseReq.getData());
                break;
            case HEARTBEAT:
                break;
            case LOGIN:
                // {"type":1} 去连接认证
                System.out.println("请求登录二维码");
                webSocketService.handleLoginReq(ctx.channel());

        }
    }
}
