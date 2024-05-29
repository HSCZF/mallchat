package com.hs.mallchat.common.websocket.service;

import io.netty.channel.Channel;

/**
 * @Author: CZF
 * @Create: 2024/5/29 - 16:32
 */
public interface WebSocketService {
 

    void connect(Channel channel);

    void handleLoginReq(Channel channel);

    void offline(Channel channel);

    void scanLoginSuccess(Integer code, Long id);

    void scanSuccess(Integer code);
}
