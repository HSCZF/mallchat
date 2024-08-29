package com.hs.mallchat.common.chat.service;

import com.hs.mallchat.common.chat.domain.entity.GroupMember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.mallchat.common.chat.domain.vo.request.admin.AdminAddReq;
import com.hs.mallchat.common.chat.domain.vo.request.admin.AdminRevokeReq;
import com.hs.mallchat.common.chat.domain.vo.request.member.MemberExitReq;

/**
 * <p>
 * 群成员表 服务类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-07-22
 */
public interface IGroupMemberService{

    /**
     * 退出群聊
     *
     * @param uid     用户ID
     * @param request 请求信息
     */
    void exitGroup(Long uid, MemberExitReq request);

    /**
     * 增加管理员
     * @param uid
     * @param request
     */
    void addAdmin(Long uid, AdminAddReq request);

    /**
     * 撤销管理员
     * @param uid
     * @param request
     */
    void revokeAdmin(Long uid, AdminRevokeReq request);
}
