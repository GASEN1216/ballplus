package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dto.UserBannedDaysRequest;
import com.gasen.usercenterbackend.model.dto.weChatAddItemRequest;
import com.gasen.usercenterbackend.model.dto.weChatUseItemRequest;
import com.gasen.usercenterbackend.model.dao.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gasen.usercenterbackend.model.vo.goEasyUser;
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

    List<goEasyUser> getFriends(List<Long> friendsId);

    List<userIdAndAvatar> getAvatarByUserIds(List<Long> userIds);

    String getOpenIdByUserId(Long userId);

    List<String> getOpenIdByUserIds(List<Long> useridList);

    void updateCredit(Long userId, int creditChange);
}
