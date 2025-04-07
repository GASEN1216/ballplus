package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.mapper.FriendsRequestMapper;
import com.gasen.usercenterbackend.model.dao.FriendsRequest;
import com.gasen.usercenterbackend.service.IFriendsRequestService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class FriendsRequestServiceImpl implements IFriendsRequestService {
    @Resource
    private FriendsRequestMapper friendsRequestMapper;


    @Override
    public List<FriendsRequest> findAppUser(Long userId) {
        return friendsRequestMapper.selectList(new LambdaQueryWrapper<FriendsRequest>().eq(FriendsRequest::getFriendId, userId));
    }

    @Override
    public List<FriendsRequest> findFriUser(Long userId) {
        return friendsRequestMapper.selectList(new LambdaQueryWrapper<FriendsRequest>().eq(FriendsRequest::getAppId, userId));
    }

    @Override
    public boolean addFriendRequest(FriendsRequest friendsRequest) {
        return friendsRequestMapper.insert(friendsRequest) > 0;
    }

    @Override
    public boolean ifExists(Long userId, Long id) {
        return friendsRequestMapper.selectOne(new LambdaQueryWrapper<FriendsRequest>().eq(FriendsRequest::getAppId, userId).eq(FriendsRequest::getFriendId, id)) != null;
    }
}
