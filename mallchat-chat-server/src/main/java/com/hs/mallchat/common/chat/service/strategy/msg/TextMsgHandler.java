package com.hs.mallchat.common.chat.service.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.hs.mallchat.common.chat.dao.MessageDao;
import com.hs.mallchat.common.chat.domain.entity.Message;
import com.hs.mallchat.common.chat.domain.entity.msg.MessageExtra;
import com.hs.mallchat.common.chat.domain.enums.MessageStatusEnum;
import com.hs.mallchat.common.chat.domain.enums.MessageTypeEnum;
import com.hs.mallchat.common.chat.domain.vo.request.ChatMessageReq;
import com.hs.mallchat.common.chat.domain.vo.request.msg.TextMsgReq;
import com.hs.mallchat.common.chat.domain.vo.response.msg.TextMsgResp;
import com.hs.mallchat.common.chat.service.adapter.MessageAdapter;
import com.hs.mallchat.common.chat.service.cache.MsgCache;
import com.hs.mallchat.common.common.algorithm.sensitiveWord.SensitiveWordBs;
import com.hs.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hs.mallchat.common.common.utils.AssertUtil;
import com.hs.mallchat.common.common.utils.discover.PrioritizedUrlDiscover;
import com.hs.mallchat.common.common.utils.discover.domain.UrlInfo;
import com.hs.mallchat.common.user.domain.entity.User;
import com.hs.mallchat.common.user.domain.enums.RoleEnum;
import com.hs.mallchat.common.user.service.IRoleService;
import com.hs.mallchat.common.user.service.cache.UserCache;
import com.hs.mallchat.common.user.service.cache.UserInfoCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description:
 *
 * @Author: CZF
 * @Create: 2024/7/23 - 8:42
 */
@Component
public class TextMsgHandler extends AbstractMsgHandler<TextMsgReq> {

    @Autowired
    private MessageDao messageDao;
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private IRoleService iRoleService;
    @Autowired
    private SensitiveWordBs sensitiveWordBs;

    private static final PrioritizedUrlDiscover URL_TITLE_DISCOVER = new PrioritizedUrlDiscover();


    /**
     * 消息类型
     */
    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.TEXT;
    }

    /**
     * 在各个子类实现
     *
     * @param body
     * @param roomId
     * @param uid
     */
    @Override
    protected void checkMsg(TextMsgReq body, Long roomId, Long uid) {
        // 校验下回复消息
        if (Objects.nonNull(body.getReplyMsgId())) {
            Message replyMsg = messageDao.getById(body.getReplyMsgId());
            AssertUtil.isNotEmpty(replyMsg, "回复消息不存在");
            AssertUtil.equal(replyMsg.getRoomId(), roomId, "只能回复相同会话内的消息");
        }
        if (CollectionUtil.isNotEmpty(body.getAtUidList())) {
            //前端传入的@用户列表可能会重复，需要去重
            List<Long> atUidList = body.getAtUidList().stream().distinct().collect(Collectors.toList());
            Map<Long, User> batch = userInfoCache.getBatch(atUidList);
            //如果@用户不存在，userInfoCache 返回的map中依然存在该key，但是value为null，需要过滤掉再校验
            long batchCount = batch.values().stream().filter(Objects::nonNull).count();
            AssertUtil.equal((long) atUidList.size(), batchCount, "@用户不存在");
            boolean atAll = body.getAtUidList().contains(0L);
            if (atAll) {
                AssertUtil.isTrue(iRoleService.hasPower(uid, RoleEnum.CHAT_MANAGER), "没有权限");
            }
        }

    }

    /**
     * 保存消息
     *
     * @param msg
     * @param body
     */
    @Override
    public void saveMsg(Message msg, TextMsgReq body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setContent(sensitiveWordBs.filter(body.getContent()));
        update.setExtra(extra);
        if (Objects.nonNull(body.getReplyMsgId())) {
            Integer gapCount = messageDao.getGapCount(msg.getRoomId(), body.getReplyMsgId(), msg.getId());
            update.setGapCount(gapCount);
            update.setReplyMsgId(body.getReplyMsgId());
        }
        // 判断消息url跳转
        Map<String, UrlInfo> urlContentMap = URL_TITLE_DISCOVER.getUrlContentMap(body.getContent());
        extra.setUrlContentMap(urlContentMap);
        // 艾特功能
        if (CollectionUtil.isNotEmpty(body.getAtUidList())) {
            extra.setAtUidList(body.getAtUidList());
        }
        messageDao.updateById(update);
    }

    /**
     * 展示消息
     *
     * @param msg
     */
    @Override
    public Object showMsg(Message msg) {
        TextMsgResp resp = new TextMsgResp();
        resp.setContent(msg.getContent());
        resp.setUrlContentMap(Optional.ofNullable(msg.getExtra()).map(MessageExtra::getUrlContentMap).orElse(null));
        resp.setAtUidList(Optional.ofNullable(msg.getExtra()).map(MessageExtra::getAtUidList).orElse(null));
        //回复消息
        Optional<Message> reply = Optional.ofNullable(msg.getReplyMsgId())
                .map(msgCache::getMsg)
                .filter(a -> Objects.equals(a.getStatus(), MessageStatusEnum.NORMAL.getStatus()));
        if (reply.isPresent()) {
            Message replyMessage = reply.get();
            TextMsgResp.ReplyMsg replyMsgVO = new TextMsgResp.ReplyMsg();
            replyMsgVO.setId(replyMessage.getId());
            replyMsgVO.setUid(replyMessage.getFromUid());
            replyMsgVO.setType(replyMessage.getType());
            replyMsgVO.setBody(MsgHandlerFactory.getStrategyNoNull(replyMessage.getType()).showReplyMsg(replyMessage));
            User replyUser = userCache.getUserInfo(replyMessage.getFromUid());
            replyMsgVO.setUsername(replyUser.getName());
            replyMsgVO.setCanCallback(YesOrNoEnum.toStatus(Objects.nonNull(msg.getGapCount()) && msg.getGapCount() <= MessageAdapter.CAN_CALLBACK_GAP_COUNT));
            replyMsgVO.setGapCount(msg.getGapCount());
            resp.setReply(replyMsgVO);
        }
        return resp;
    }

    /**
     * 被回复时-展示的消息
     *
     * @param msg
     */
    @Override
    public Object showReplyMsg(Message msg) {
        return msg.getContent();
    }

    /**
     * 会话列表-展示的消息
     *
     * @param msg
     */
    @Override
    public String showContactMsg(Message msg) {
        return msg.getContent();
    }
}
