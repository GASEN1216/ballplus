package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.model.Event;
import com.gasen.usercenterbackend.model.Request.UserBannedDaysRequest;
import com.gasen.usercenterbackend.model.Request.weChatAddItemRequest;
import com.gasen.usercenterbackend.model.Request.weChatUseItemRequest;
import com.gasen.usercenterbackend.model.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gasen.usercenterbackend.model.respond.goEasyUser;
import com.gasen.usercenterbackend.model.respond.indexEvent;
import com.gasen.usercenterbackend.model.userIdAndAvatar;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 * 用户信息 服务类
 * </p>
 *
 * @author gasen
 * @since 2024-02-22
 */
public interface IUserService extends IService<User> {

    long userRegister(String userAccount, String password);

    User userLogin(String userAccount, String password, HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);

    Boolean userBannedDays(UserBannedDaysRequest userBannedDaysRequest);

    List<User> usersList();

    void addExp(User user);

    void updateAvatar(weChatAddItemRequest weChatAddItemRequest);

    void onlyUpdateAvatar(weChatUseItemRequest weChatUseItemRequest);

    List<goEasyUser> getFriends(List<Integer> friendsId);

    List<userIdAndAvatar> getAvatarByUserIds(List<Integer> userIds);

    String getOpenIdByUserId(Integer userId);

    List<String> getOpenIdByUserIds(List<Integer> useridList);
}
