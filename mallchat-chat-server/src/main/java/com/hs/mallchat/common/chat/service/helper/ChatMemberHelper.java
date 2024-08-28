package com.hs.mallchat.common.chat.service.helper;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.hs.mallchat.common.user.domain.enums.ChatActiveStatusEnum;

/**
 * Description:
 * 成员列表工具类
 *
 * @Author: CZF
 * @Create: 2024/8/22 - 16:18
 */
public class ChatMemberHelper {

    private static final String SEPARATOR = "_";


    /**
     * 根据游标字符串获取聊天活跃状态和时间游标
     *
     * @param cursor 游标字符串，格式为"状态_时间"
     * @return Pair对象，包含聊天活跃状态枚举ChatActiveStatusEnum和时间游标字符串
     * <p>
     * 该方法解释了如何从一个字符串游标中解析出聊天用户的活跃状态和时间游标
     * 它首先检查游标是否为空或仅由空白字符组成；如果不为空，则通过SEPARATOR（未在代码段中定义，假设为"#"）
     * 分割字符串，从中解析出活跃状态和时间游标如果游标为空，则默认返回在线状态和空字符串作为时间游标
     * <p>
     * 注意：这个方法假设游标的格式是正确的，并且活跃状态部分可以转换为有效的ChatActiveStatusEnum枚举值
     */
    public static Pair<ChatActiveStatusEnum, String> getCursorPair(String cursor) {
        ChatActiveStatusEnum activeStatusEnum = ChatActiveStatusEnum.ONLINE;
        String timeCursor = null;
        if (StrUtil.isNotBlank(cursor)) {
            String activeStr = cursor.split(SEPARATOR)[0];
            String timeStr = cursor.split(SEPARATOR)[1];
            activeStatusEnum = ChatActiveStatusEnum.of(Integer.parseInt(activeStr));
            timeCursor = timeStr;
        }
        return Pair.of(activeStatusEnum, timeCursor);
    }


    public static String generateCursor(ChatActiveStatusEnum activeStatusEnum, String timeCursor) {
        return activeStatusEnum.getStatus() + SEPARATOR + timeCursor;
    }
}
