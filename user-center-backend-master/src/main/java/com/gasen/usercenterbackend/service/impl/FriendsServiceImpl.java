package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.mapper.FriendsMapper;
import com.gasen.usercenterbackend.mapper.FriendsRequestMapper;
import com.gasen.usercenterbackend.model.Friends;
import com.gasen.usercenterbackend.model.FriendsRequest;
import com.gasen.usercenterbackend.service.IFriendsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class FriendsServiceImpl implements IFriendsService {

    @Resource
    private FriendsMapper friendsMapper;

    @Resource
    private FriendsRequestMapper friendsRequestMapper;

    @Override
    public List<Integer> getFriends(Integer userId) {
        List<Friends> friends = friendsMapper.selectList(new LambdaQueryWrapper<Friends>().eq(Friends::getUserId, userId));
        return friends.stream().map(map-> map.getFriendId().intValue()).toList();
    }

    @Override
    public boolean ifFriends(int userId, Integer id) {
        return friendsMapper.selectCount(new LambdaQueryWrapper<Friends>().eq(Friends::getUserId, userId).eq(Friends::getFriendId, id)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFriends(int userId, int friendId) {
        try {
            // 插入 userId -> friendId
            int result1 = friendsMapper.insert(new Friends(userId, friendId));

            // 插入 friendId -> userId
            int result2 = friendsMapper.insert(new Friends(friendId, userId));

            // 好友请求 state -> 1
            FriendsRequest friendsRequest = friendsRequestMapper.selectOne(new LambdaQueryWrapper<FriendsRequest>().eq(FriendsRequest::getAppId, friendId).eq(FriendsRequest::getFriendId, userId));
            friendsRequest.setState(1);
            int result3 = friendsRequestMapper.updateById(friendsRequest);

            // 检查两次操作是否都成功
            return result1 > 0 && result2 > 0 && result3 > 0;
        } catch (Exception e) {
            // 抛出异常以触发事务回滚
            throw new BusinessExcetion(ErrorCode.OPERATION_ERROR, e.toString());
        }
    }
}
