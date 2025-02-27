package com.gasen.usercenterbackend.controller;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.config.QiniuConfig;
import com.gasen.usercenterbackend.config.WXConfig;
import com.gasen.usercenterbackend.mapper.UserMapper;
import com.gasen.usercenterbackend.model.Request.UserBannedDaysRequest;
import com.gasen.usercenterbackend.model.Request.UserRegisterLoginRequest;
import com.gasen.usercenterbackend.model.Request.weChatAddItemRequest;
import com.gasen.usercenterbackend.model.Request.weChatUseItemRequest;
import com.gasen.usercenterbackend.model.User;
import com.gasen.usercenterbackend.model.respond.goEasyUser;
import com.gasen.usercenterbackend.model.respond.wxUser;
import com.gasen.usercenterbackend.service.IFriendsService;
import com.gasen.usercenterbackend.service.IItemsService;
import com.gasen.usercenterbackend.service.IUserService;
import com.gasen.usercenterbackend.utils.WechatUtil;
import com.qiniu.util.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import  java.util.concurrent.TimeUnit;

import static com.gasen.usercenterbackend.common.ErrorCode.INVALID_TOKEN;
import static com.gasen.usercenterbackend.common.ErrorCode.SUCCESS;
import static com.gasen.usercenterbackend.constant.UserConstant.ADMIN;
import static com.gasen.usercenterbackend.constant.UserConstant.USER_LOGIN_IN;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author gasen
 * @since 2024-02-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IItemsService itemsService;

    @Resource
    private IFriendsService friendsService;

    @Resource
    private UserMapper userMapper;

    private final WXConfig wxConfig;

    private final QiniuConfig qiniuConfig;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Autowired
    public UserController(WXConfig wxConfig, QiniuConfig qiniuConfig) {
        this.wxConfig = wxConfig;
        this.qiniuConfig = qiniuConfig;
    }

    //TODO：ZSet做排行榜

    @Operation(summary = "获取用户的好友列表")
    @PostMapping("/wx/getFriends")
    public BaseResponse getFriends(@RequestParam(value = "userId") int userId){
        List<Integer> friendsId = friendsService.getFriends(userId);
        List<goEasyUser> friends = userService.getFriends(friendsId);
        return ResultUtils.success(friends);
    }

    @Operation(summary = "获取指定用户的物品列表")
    @PostMapping("/wx/getItems")
    public BaseResponse getItems(@RequestParam(value = "userId") int userId){
        List<Integer> items = itemsService.getItems(userId);
        return ResultUtils.success(items);
    }

    /**
     * 添加物品
     */
    @Operation(summary = "wx用户使用物品信息")
    @PostMapping("/wx/useItem")
    public BaseResponse useItem(@RequestBody weChatUseItemRequest weChatUseItemRequest) {
        // 更新用户信息
        userService.onlyUpdateAvatar(weChatUseItemRequest);
        return ResultUtils.success(SUCCESS);
    }

    /**
     * 添加物品
     */
    @Operation(summary = "更新wx用户物品信息")
    @PostMapping("/wx/addItem")
    public BaseResponse addItem(@RequestBody weChatAddItemRequest weChatAddItemRequest) {
        // 添加物品
        itemsService.addItem(weChatAddItemRequest.getUserId(), weChatAddItemRequest.getItemId());
        // 更新用户信息
        userService.updateAvatar(weChatAddItemRequest);
        return ResultUtils.success(SUCCESS);
    }

    /**
     * 获取上传凭证
     * @param token
     * @return
     */
    @PostMapping("/wx/uptoken")
    public BaseResponse testupToken(@RequestParam("token") String token) {
        if(!validateToken(token)) {
            return ResultUtils.error(INVALID_TOKEN, "无效的token");
        }
        Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
        // 获取上传凭证
        String upToken = auth.uploadToken(qiniuConfig.getBucket());
        return ResultUtils.success(upToken);
    }

    /**
     * 接收code并输出
     */
    @PostMapping("/receiveCode")
    public BaseResponse<String> receiveCode(@RequestParam("code") String code) {
        if (StringUtils.isBlank(code)) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "code为空");
        }

        // 输出接收到的code
        log.info("Received code: " + code);

        // 假设你想要返回接收到的code作为响应
        return ResultUtils.success("Code received: " + code);
    }

    /**
     * 验证微信小程序登录的token
     * @param token 用户传入的token
     * @return 是否有效的布尔值
     */
    public boolean validateToken(String token) {
        // 从Redis中获取token对应的值
        String storedValue = (String) redisTemplate.opsForValue().get(token);

        // 检查token是否存在且有效
        // 如果存在，token有效
        return storedValue != null;
        // 如果Redis中不存在该token，返回无效
    }

    @PostMapping("/wx/token")
    public BaseResponse someProtectedEndpoint(@RequestParam("token") String token) {
        // 验证token
        if (!validateToken(token)) {
            return ResultUtils.error(INVALID_TOKEN);
        }
        // 处理受保护的逻辑
        return ResultUtils.success("Token正确");
    }


    /**
     * 微信小程序登录
     */
    @PostMapping("/wx/login")
    public BaseResponse user_login(@RequestParam(value = "code", required = false) String code) {
        // 1.接收小程序发送的code
        // 2.开发者服务器 登录凭证校验接口 appi + appsecret + code
        JSONObject SessionKeyOpenId = WechatUtil.getSessionKeyOrOpenId(code, wxConfig.getAppid(), wxConfig.getAppsecret());
        // 3.接收微信接口服务 获取返回的参数
        String openid = SessionKeyOpenId.getString("openid");
        String sessionKey = SessionKeyOpenId.getString("session_key");
        String os = openid + "+" + sessionKey;

        // 3.5 判断是否缓存里已有记录,已有直接取出token并更新过期时间
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        String token = (String) ops.get(os);
        if(token==null){
            // 4. 将openid和sessionKey和随机数使用加密算法得到有效期为一天的token，存到redis中，key为token，value为openid+sessionKey
            token = WechatUtil.generateToken(openid, sessionKey, wxConfig.getSalt());
            ops.set(token, os, 1, TimeUnit.DAYS);
            ops.set(os, token, 1, TimeUnit.DAYS);
        }else {
            redisTemplate.expire(token, 1, TimeUnit.DAYS);
            redisTemplate.expire(os, 1, TimeUnit.DAYS);
        }

        // 5.根据返回的User实体类，判断用户是否是新用户，是的话，将用户信息存到数据库；
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        lqw.eq(User::getOpenId, openid);
        User user = userService.getOne(lqw);
        if (user == null) {
            // 用户信息入库
            user = new User(WechatUtil.generateRandomUsername(), "12345678");
            user.setOpenId(openid);
            user.setSignIn(LocalDateTime.now());
            user.setUserName(user.getOnlyBallNumber());
            user.setAvatarUrl("https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0");
            userService.save(user);
        }
        if(user.getState()==-1)
            return ResultUtils.error(ErrorCode.BANNED_USER, user.getUnblockingTime());
        // 加经验
        userService.addExp(user);
        // 返回安全用户信息
        wxUser saftyUser = getSaftywxUser(user);
        saftyUser.setToken(token);
        return ResultUtils.success(saftyUser);
    }

    /**
     * 更新用户
     * User传进来要有id
     * */
    @Operation(summary = "更新wx用户信息")
    @PostMapping("/wx/update")
    public BaseResponse updatewxUser(@RequestBody wxUser user, HttpServletRequest request) {
        //判断是否是用户自己，是的话也可以进入修改
        //通过token从redis缓存中拿取openid+sessionid，然后分割字符串取得openid
        String os = (String) redisTemplate.opsForValue().get(user.getToken());
        if(os==null) return ResultUtils.error(INVALID_TOKEN);
        String openid = os.split("\\+")[0];
        //通过openid查询数据库得到用户的id，再和传过来的id进行比较，如果相同则可以修改，如果不同则不可以修改
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Integer id = userMapper.selectOne(userLambdaQueryWrapper.eq(User::getOpenId, openid)).getId();

        //1.是否为管理员 or 是否是自己
        if(isAdmin(request) || id.equals(user.getId())) {
            //2.判断用户是否存在
            if(userService.lambdaQuery().eq(User::getId, user.getId()).exists()) {
                //TODO：判断是否有更新信息，未更新直接返回
                //3.更新用户信息
                log.info("id为'{}' name为'{}' 的用户更新信息", user.getId(), user.getUserAccount());
                User updatedUser = User.wxUserToUser(user);
                if(userService.updateById(updatedUser))
                    //4.返回更新后的用户信息
                    return ResultUtils.success(getSaftyUser(updatedUser));
                else return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"更新用户信息失败");
            } else return ResultUtils.error(ErrorCode.USER_NOT_EXIST);
        }else return ResultUtils.error(ErrorCode.USER_NOT_LOGIN_OR_NOT_ADMIN);
    }

    @Operation(summary = "获取用户的手机号码")
    @PostMapping("/wx/getPhone")
    public BaseResponse getPhone(@RequestParam(value = "code") String code) {
        String phoneNumber = WechatUtil.getPhoneNumber(code, wxConfig.getAppid(), wxConfig.getAppsecret());
        if(phoneNumber==null || phoneNumber.isEmpty())
            return ResultUtils.error(ErrorCode.PHONE_NUMBER_ERROR);
        return ResultUtils.success(phoneNumber);
    }

    /**
     * 更新用户
     * User传进来要有id
     * */
    @Operation(summary = "更新用户信息")
    @PostMapping("/update")
    public BaseResponse updateUser(@RequestBody User user, HttpServletRequest request) {
        //TODO：判断是否是用户自己，是的话也可以进入修改
        //1.是否为管理员
        if(isAdmin(request)) {
            //2.判断用户是否存在
            if(userService.lambdaQuery().eq(User::getId, user.getId()).exists()) {
                //TODO：判断是否有更新信息，未更新直接返回
                //3.更新用户信息
                log.info("id为"+user.getId()+"的用户更新信息");
                if(userService.updateById(user))
                //4.返回更新后的用户信息
                    return ResultUtils.success(getSaftyUser(user));
                else return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"更新用户信息失败");
            } else return ResultUtils.error(ErrorCode.USER_NOT_EXIST);
        }else return ResultUtils.error(ErrorCode.USER_NOT_LOGIN_OR_NOT_ADMIN);
    }

    /**
     * 删除用户
     * */
    @PostMapping("/delete")
    public BaseResponse deleteUser(@RequestParam("id") int id, HttpServletRequest request) {
        //1.是否为管理员
        if(isAdmin(request)) {
            //2.判断用户是否存在
            if(userService.lambdaQuery().eq(User::getId, id).exists()) {
                //3.删除用户
                userMapper.deleteById(id);
                log.info("id为"+id+"的用户删除成功");
                return ResultUtils.success("id为"+id+"的用户删除成功");
            } else return ResultUtils.error(ErrorCode.USER_NOT_EXIST);
        }else return ResultUtils.error(ErrorCode.USER_NOT_LOGIN_OR_NOT_ADMIN);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/current")
    public BaseResponse getCurrentUser(HttpServletRequest request) {
        //1.获取用户信息
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        //2.不为null返回安全用户信息
        if(user!=null) {
            log.info("请求获取当前用户信息："+user.getUserAccount());
            //获取最新的用户信息
            User latestUser = getSaftyUser(userMapper.selectById(user.getId()));
            return ResultUtils.success(latestUser);
        }
        //3.为null返回用户未登录
        else return ResultUtils.error(ErrorCode.USER_NOT_LOGIN);
    }

    /**
     * 获取所有用户信息
     */
    @GetMapping("/all")
    public BaseResponse getAllUser(HttpServletRequest request) {
        if(isAdmin(request)) {
            log.info("请求获取所有用户信息");
            List<User> users = userService.usersList();
            // 使用 Java 8 Stream API 来处理用户列表并生成新的安全用户列表
            List<User> safeUsers = users.stream()
                    .map(UserController::getSaftyUser) // 对每个用户应用安全处理
                    .toList(); // 收集处理后的用户到新的列表中
            return ResultUtils.success(safeUsers);
        }else return ResultUtils.error(ErrorCode.USER_NOT_LOGIN_OR_NOT_ADMIN);
    }


    /**
     * 用户注册
     * */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterLoginRequest user) {
        if(user==null) return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"用户为空");
        String userAccount = user.getUserAccount();
        String password = user.getPassword();
        userDetail(user,"注册");
        if(StringUtils.isAnyBlank(userAccount,password)) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"账户名或密码为空");
        }
        long l = userService.userRegister(userAccount, password);
        return ResultUtils.success(l);
    }


    /**
     * 用户登录暨签到
     * */
    @PostMapping("/login")
    public BaseResponse<?> login(@RequestBody UserRegisterLoginRequest user, HttpServletRequest request) {
        if(user==null) return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"用户为空");
        String userAccount = user.getUserAccount();
        String password = user.getPassword();
        userDetail(user,"登录");
        if(StringUtils.isAnyBlank(userAccount,password)) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"账户名或密码为空");
        }
        User user1 = userService.userLogin(userAccount, password, request);
        if(user1.getUnblockingTime()!=null)
            return ResultUtils.error(ErrorCode.BANNED_USER,user1.getUnblockingTime());
        User saftyUser = getSaftyUser(user1);
        return ResultUtils.success(saftyUser);
    }

    /**
     * 用户登出
     * */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        boolean logout = userService.userLogout(request);
        return ResultUtils.success(logout);
    }

    /**
     * 用户封禁
     * */
    @PostMapping("/banned")
    public BaseResponse banned(@RequestBody UserBannedDaysRequest userBannedDaysRequest, HttpServletRequest request) {
        if(!isAdmin(request)) return ResultUtils.error(ErrorCode.USER_NOT_LOGIN_OR_NOT_ADMIN);
        if(userBannedDaysRequest ==null) return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"参数为空");
        log.info("ID为"+userBannedDaysRequest.getId()+"的用户被封禁"+userBannedDaysRequest.getDays()+"天");
        Boolean banned = userService.userBannedDays(userBannedDaysRequest);
        return ResultUtils.success(banned);
    }

    @Operation(summary = "通过用户id获取用户展示信息")
    @PostMapping("/wx/getUserInfoByUserId")
    public BaseResponse getUserInfoByUserId(@RequestParam("userId") Integer userId) {
        if (userId == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户id为空");
        User user = userService.getOne(new QueryWrapper<User>().eq("id", userId));
        if (user == null)
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户不存在");
        else
            return ResultUtils.success(getSaftywxUser(user));
    }

    /**
     * 用户信息
     * */
    public void userDetail(UserRegisterLoginRequest user, String behavior) {
        log.info("用户"+user.getUserAccount()+behavior+"："+ user);
    }


    /**
     * 获取安全wx用户信息
     * */
    public static wxUser getSaftywxUser(User user) {
        wxUser safetyUser = new wxUser();
        safetyUser.setId(user.getId())
                .setBallNumber(user.getUserName())
                .setUserAccount(user.getUserAccount())
                .setAvatarUrl(user.getAvatarUrl())
                .setGender(user.getGender())
                .setGrade(user.getGrade())
                .setExp(user.getExp())
                .setState(user.getState())
                .setUnblockingTime(user.getUnblockingTime())
                .setBirthday(user.getBirthday())
                .setCredit(user.getCredit())
                .setScore(user.getScore())
                .setDescription(user.getDescription())
                .setLabel(user.getLabel())
                .setPhone(user.getPhone());
        return safetyUser;
    }

    /**
     * 获取安全用户信息
     * */
    public static User getSaftyUser(User user) {
        User saftyUser = new User();
        saftyUser.setId(user.getId());
        saftyUser.setUserAccount(user.getUserAccount());
        saftyUser.setUserName(user.getUserName());
        saftyUser.setAvatarUrl(user.getAvatarUrl());
        saftyUser.setGender(user.getGender());
        saftyUser.setGrade(user.getGrade());
        saftyUser.setExp(user.getExp());
        saftyUser.setState(user.getState());
        saftyUser.setEmail(user.getEmail());
        saftyUser.setPhone(user.getPhone());
        saftyUser.setSignIn(user.getSignIn());
        saftyUser.setUnblockingTime(user.getUnblockingTime());
        return saftyUser;
    }

    /**
     * 是否是管理员
     * */
    public boolean isAdmin(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_IN);
        return user != null && user.getState() == ADMIN;
    }
}
