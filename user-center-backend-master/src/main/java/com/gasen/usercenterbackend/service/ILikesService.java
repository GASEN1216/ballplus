package com.gasen.usercenterbackend.service;

public interface ILikesService {
    Integer getLikesById(Long id);
    boolean updateLikes(Long id, Integer likes);
}
