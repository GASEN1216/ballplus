package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dao.FriendsRequest;

import java.util.List;

public interface IFriendsRequestService {

    List<FriendsRequest> findAppUser(Long userId);

    List<FriendsRequest> findFriUser(Long userId);

    boolean addFriendRequest(FriendsRequest friendsRequest);

    boolean ifExists(Long userId, Long id);
}
