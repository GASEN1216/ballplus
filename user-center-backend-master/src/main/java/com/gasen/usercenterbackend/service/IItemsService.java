package com.gasen.usercenterbackend.service;

import java.util.List;

public interface IItemsService {
    void addItem(Long userId, Integer itemId);

    List<Integer> getItems(Long userId);
}
