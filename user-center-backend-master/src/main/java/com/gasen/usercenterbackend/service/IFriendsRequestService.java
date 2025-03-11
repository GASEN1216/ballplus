package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dao.FriendsRequest;

import java.util.List;

public interface IFriendsRequestService {

    List<FriendsRequest> findAppUser(int userId);

    List<FriendsRequest> findFriUser(int userId);

    boolean addFriendRequest(FriendsRequest friendsRequest);

    boolean ifExists(int userId, Integer id);
}
