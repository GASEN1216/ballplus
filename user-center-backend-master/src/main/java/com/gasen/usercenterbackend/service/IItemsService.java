package com.gasen.usercenterbackend.service;

import java.util.List;

public interface IItemsService {
    void addItem(Integer userId, Integer itemId);

    List<Integer> getItems(int userId);
}
