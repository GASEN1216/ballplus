package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ItemsEunm;
import com.gasen.usercenterbackend.controller.UserController;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.model.respond.PostInfo;
import com.gasen.usercenterbackend.model.Request.UserBannedDaysRequest;
import com.gasen.usercenterbackend.model.Request.weChatAddItemRequest;
import com.gasen.usercenterbackend.model.Request.weChatUseItemRequest;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.mapper.UserMapper;
import com.gasen.usercenterbackend.model.respond.goEasyUser;
import com.gasen.usercenterbackend.model.userIdAndAvatar;
import com.gasen.usercenterbackend.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gasen.usercenterbackend.constant.UserConstant.*;

/**
 * <p>
 * 用户信息 服务实现类
 * </p>
 *
 * @author gasen
 * @since 2024-02-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    /**
     * 盐值混淆密码
     * */
    private static final String SALT = "20240225";
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public long userRegister(String userAccount, String password) {
        inspectUser(userAccount, password);
        //检查用户名是否重复
        if (lambdaQuery().eq(User::getUserAccount, userAccount).exists()) {
            throw new BusinessExcetion(ErrorCode.USER_EXIST,"用户名重复");
        }
        //密码加密
        password = DigestUtils.md5DigestAsHex((password + SALT).getBytes());
        boolean save = this.save(new User(userAccount, password));
        return save ? 1 : 0;
    }

    @Override
    public User userLogin(String userAccount, String password, HttpServletRequest request) {
        inspectUser(userAccount, password);
        //密码加密
        password = DigestUtils.md5DigestAsHex((password + SALT).getBytes());
        User user = lambdaQuery().eq(User::getUserAccount, userAccount).eq(User::getPassword, password).one();
        if (user == null) {
            throw new BusinessExcetion(ErrorCode.USER_NOT_EXIST,"用户不存在或密码不正确");
        }
        //检查是否被封禁
        if(isUserBanned(user))
            return new User().setUnblockingTime(user.getUnblockingTime());
        //登录
        request.getSession().setAttribute(USER_LOGIN_IN, UserController.getSaftyUser(user));
        userSignInUpExp(user);
        return user;
    }
    /**
     * 退出登录
     * */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if(request.getSession().getAttribute(USER_LOGIN_IN)==null) {
            throw new BusinessExcetion(ErrorCode.USER_NOT_LOGIN,"用户未登录");
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        log.info(user.getUserAccount()+"登出");
        request.getSession().removeAttribute(USER_LOGIN_IN);
        return true;
    }

    /**
     * 检查用户是否被封禁
     * */
    public boolean isUserBanned(User user) {
        if(user.getState()==banned)  {
            if(LocalDateTime.now().isAfter(user.getUnblockingTime())) {
                lambdaUpdate().eq(User::getId, user.getId()).set(User::getState, USER).set(User::getUnblockingTime, null).update(new User());
                log.info("用户{}已解封", user.getUserAccount());
                return false;
            } else return true;
        } else return false;
    }

    /**
     * 用户封禁
     */
    @Override
    public Boolean userBannedDays(UserBannedDaysRequest userBannedDaysRequest) {
        if(lambdaQuery().eq(User::getId, userBannedDaysRequest.getId()).exists()) {
            lambdaUpdate().eq(User::getId, userBannedDaysRequest.getId()).set(User::getUnblockingTime, LocalDateTime.now().plusDays(userBannedDaysRequest.getDays())).update(new User());
            lambdaUpdate().eq(User::getId, userBannedDaysRequest.getId()).set(User::getState, banned).update(new User());
            return true;
        }
        throw new BusinessExcetion(ErrorCode.USER_NOT_EXIST, "用户不存在");
    }


    /**
     * 获取所有用户
     * */
    @Override
    public List<User> usersList() {
        return baseMapper.selectList(null);
    }

    /**
     * 登录增加经验
     * 登录增加（当前经验+随机1-4）
     * 连续登录增加（当前经验+随机1-4）*（1-3）
     * 每级的上限为当前等级*10
     * */
    @Override
    public void addExp(User user) {
        LocalDateTime userSignIn = user.getSignIn();
            LocalDate signIn = userSignIn.toLocalDate();
            // 如果登录日期不在上次的后面说明是今天，一天只加一次
            if(!LocalDate.now().isAfter(signIn))
                return;
            int exp = user.getExp();
            int grade = user.getGrade();
            Random random = new Random();
            if( Period.between(signIn, LocalDate.now()).getDays() == 1) {
                exp = exp + random.nextInt(1, 5)*random.nextInt(1,4);
            }else{
                exp = exp + random.nextInt(1, 5);
            }
        while(grade*10<=exp) {
            exp -= grade*10;
            grade++;
        }
        user.setExp(exp).setGrade(grade);
        user.setSignIn(LocalDateTime.now());
        //更新数据库
        boolean b = this.updateById(user);
        if(!b) throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR,"增加经验失败");
    }

    @Override
    public void updateAvatar(weChatAddItemRequest weChatAddItemRequest) {
        Integer userId = weChatAddItemRequest.getUserId();
        Integer itemId = weChatAddItemRequest.getItemId();
        String url = weChatAddItemRequest.getUrl();

        // 使用 LambdaQueryWrapper 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, userId)
        );

        // 如果查询结果为空，抛出异常
        if (user == null) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户不存在");
        }
        // 获取用户的当前赛点并计算新的值
        int score = user.getScore() - ItemsEunm.getPrice(itemId);
        // 确保扣分后分数不为负
        if (score < 0) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "扣分后分数不能小于0");
        }
        // 更新用户信息
        if(lambdaQuery().eq(User::getId, userId).exists()){
            if(!lambdaUpdate().eq(User::getId, userId).set(User::getAvatarUrl, url).set(User::getScore, score).update(new User())){
                throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR,"更新头像失败");
            }
            return;
        }
        throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR,"用户不存在");
    }

    /**
     * 更新用户头像
     * @param weChatUseItemRequest
     */
    @Override
    public void onlyUpdateAvatar(weChatUseItemRequest weChatUseItemRequest) {
        Integer userId = weChatUseItemRequest.getUserId();
        String url = weChatUseItemRequest.getUrl();

        // 更新用户信息
        if(lambdaQuery().eq(User::getId, userId).exists()){
            if(!lambdaUpdate().eq(User::getId, userId).set(User::getAvatarUrl, url).update(new User())){
                throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR,"更新头像失败");
            }
            return;
        }
        throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR,"用户不存在");
    }

    /**
     * 获取用户好友
     * */
    @Override
    public List<goEasyUser> getFriends(List<Integer> friendsId) {
        // 检查列表是否为空
        if (friendsId == null || friendsId.isEmpty()) {
            return Collections.emptyList(); // 返回空列表
        }
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, friendsId));
        return users.stream().map(User::toFriend).toList();
    }

    @Override
    public List<userIdAndAvatar> getAvatarByUserIds(List<Integer> userIds) {
        // 查询数据库，获取用户信息
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .in(User::getId, userIds)
        );

        // 将查询结果转换为 Map，方便根据 userId 快速查找
        Map<Integer, String> userAvatarMap = users.stream()
                .collect(Collectors.toMap(
                        User::getId, // Key: userId
                        User::getAvatarUrl // Value: avatarUrl
                ));

        // 按照 userIds 的顺序构建结果列表
        return userIds.stream()
                .map(userId -> new userIdAndAvatar(
                        userId,
                        userAvatarMap.getOrDefault(userId, null) // 如果 userId 不存在，返回 null
                ))
                .toList();
    }

    @Override
    public String getOpenIdByUserId(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户不存在");
        return user.getOpenId();
    }

    @Override
    public List<String> getOpenIdByUserIds(List<Integer> useridList) {
        return userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getId, useridList)).stream().map(User::getOpenId).toList();
    }

    /**
     * 判断用户是否合法
     * */
    public void inspectUser(String userAccount, String password) {
        if(StringUtils.isAnyBlank(userAccount, password)) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR,"用户名或密码为空");
        if(userAccount.length() < 6 || password.length() < 6) throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户名或密码少于6位");
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "用户名不能包含特殊字符");
        }
    }

    /**
     * 登录增加经验
     * 登录增加（当前经验+随机1-99）
     * 连续登录增加（当前经验+随机1-99）*（1-3）
     * */
    public void userSignInUpExp(User user) {
        LocalDateTime userSignIn = user.getSignIn();
        if(userSignIn != null) {
            LocalDate signIn = userSignIn.toLocalDate();
            int exp = user.getExp();
            int grade = user.getGrade();
            if(LocalDate.now().isAfter(signIn) && Period.between(signIn, LocalDate.now()).getDays() == 1) {
                Random random = new Random();
                exp = exp + random.nextInt(1, 100)*random.nextInt(1,4);
                while(grade*10<=exp) {
                    grade++;
                    exp -= grade*10;
                }
                user.setExp(exp).setGrade(grade);
            }
        }
        user.setSignIn(LocalDateTime.now());
        //更新数据库
        boolean b = this.updateById(user);
        if(!b) throw new BusinessExcetion(ErrorCode.SYSTEM_ERROR,"增加经验失败");
    }

}
