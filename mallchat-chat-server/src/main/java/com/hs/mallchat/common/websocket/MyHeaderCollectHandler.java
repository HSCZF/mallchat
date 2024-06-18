package com.hs.mallchat.common.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * @Author: CZF
 * @Create: 2024/6/5 - 10:49
 */
public class MyHeaderCollectHandler extends ChannelInboundHandlerAdapter {

    /**
     * 当通道读取到HttpRequest消息时的处理程序。
     * 该方法主要用于处理HTTP请求，特别是从中提取token并将其存储在通道属性中，
     * 同时修改请求的URI以去除查询参数，以便后续处理。
     *
     * @param ctx 通道上下文，用于通道操作和事件传播。
     * @param msg 读取到的消息对象，此处期望为HttpRequest类型。
     * @throws Exception 如果处理过程中发生异常。
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 检查消息是否为HttpRequest类型
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            // 从HTTP请求中构建URL对象，以便解析和操作URI
            UrlBuilder urlBuilder = UrlBuilder.ofHttp(request.getUri());
            // 从URL构建器中尝试获取token查询参数，并转换为字符串形式
            Optional<String> tokenOptional = Optional.of(urlBuilder)
                    .map(UrlBuilder::getQuery)
                    .map(k -> k.get("token"))
                    .map(CharSequence::toString);
            // 如果token存在，则将其存储在通道属性中
            tokenOptional.ifPresent(s -> NettyUtil.setAttr(ctx.channel(), NettyUtil.TOKEN, tokenOptional.get()));
            // 移除URI中的查询参数，只保留路径部分
            request.setUri(urlBuilder.getPath().toString());
            // 取出用户ip
            String ip = request.headers().get("X-Real-IP");
            if (StringUtils.isBlank(ip)) {
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
            }
            // 保存到channel附件
            NettyUtil.setAttr(ctx.channel(), NettyUtil.IP, ip);
            // 处理器只需要用到一次
            ctx.pipeline().remove(this);
        }
        // 将消息进一步向下传播到处理链中的下一个处理器
        ctx.fireChannelRead(msg);
    }

}
