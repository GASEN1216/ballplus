package com.gasen.usercenterbackend.service;

import java.util.List;

public interface IFriendsService {
    List<Integer> getFriends(Integer userId);

    boolean ifFriends(int userId, Integer id);

    boolean addFriends(int userId, int friendId);
}
