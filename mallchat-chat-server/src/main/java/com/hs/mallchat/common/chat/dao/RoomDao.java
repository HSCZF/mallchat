package com.hs.mallchat.common.chat.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hs.mallchat.common.chat.domain.entity.Room;
import com.hs.mallchat.common.chat.mapper.RoomMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/hsczf">czf</a>
 * @since 2024-07-22
 */
@Service
public class RoomDao extends ServiceImpl<RoomMapper, Room> implements IService<Room> {

}
