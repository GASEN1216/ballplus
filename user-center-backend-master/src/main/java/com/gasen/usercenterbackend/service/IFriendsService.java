package com.gasen.usercenterbackend.service;

import java.util.List;

public interface IFriendsService {
    List<Long> getFriends(Long userId);

    boolean ifFriends(Long userId, Long id);

    boolean addFriends(Long userId, Long friendId);
}
