package com.gasen.usercenterbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.constant.FriendsReqConstant;
import com.gasen.usercenterbackend.model.dao.FriendsRequest;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.vo.receivedApplicationRes;
import com.gasen.usercenterbackend.service.IFriendsRequestService;
import com.gasen.usercenterbackend.service.IFriendsService;
import com.gasen.usercenterbackend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/wx")
public class FriendsController {

    @Resource
    private IFriendsRequestService friendsRequestService;

    @Resource
    private IUserService userService;

    @Resource
    private IFriendsService friendsService;

    @Operation(summary = "获取用户的好友申请列表")
    @PostMapping("/getFriendsRequest")
    public BaseResponse getFriendsRequest(@RequestParam(value = "userId") int userId, @RequestParam(value = "type") int type) {
        // 新建数组
        List<receivedApplicationRes> resList = new ArrayList<>();
        List<FriendsRequest> friendsRequests;
        // 根据type查找
        if(type == FriendsReqConstant.REQUEST_TYPE_AGREE) {
            // userId作为friendId查找得到了appid，再根据appid从user表查找name，avatar
            friendsRequests = friendsRequestService.findAppUser(userId);
            // 封装返回信息
            for (FriendsRequest friendsRequest : friendsRequests) {
                User user = userService.getById(friendsRequest.getAppId());
                receivedApplicationRes rece = new receivedApplicationRes();
                rece.setId(friendsRequest.getAppId()).setName(user.getUserAccount()).setAvatar(user.getAvatarUrl()).setState(friendsRequest.getState());

                resList.add(rece);
            }
        }else if(type == FriendsReqConstant.REQUEST_TYPE_ADD) {
            // userId作为appId查找到了friendId，再根据friendId从user表查找name，avatar
            friendsRequests = friendsRequestService.findFriUser(userId);
            // 封装返回信息
            for (FriendsRequest friendsRequest : friendsRequests) {
                User user = userService.getById(friendsRequest.getFriendId());
                receivedApplicationRes rece = new receivedApplicationRes();
                rece.setId(userId).setName(user.getUserAccount()).setAvatar(user.getAvatarUrl()).setState(friendsRequest.getState());

                resList.add(rece);
            }
        }else {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"type错误");
        }

        // 返回信息
        return ResultUtils.success(resList);
    }

    @Operation(summary = "添加好友申请")
    @PostMapping("/addFriendsRequest")
    public BaseResponse addFriendsRequest(@RequestParam(value = "userId") int userId, @RequestParam(value = "ballNumber") String ballNumber) {
        // 检查ballNumber是否存在
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, ballNumber));
        if(user == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"用户不存在");
        }else {
            if(user.getId() == userId) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"不能添加自己为好友");
            }
            if(friendsService.ifFriends(userId, user.getId())){
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"已经是好友");
            }
            if(friendsRequestService.ifExists(userId, user.getId())){
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR,"已经发送过申请");
            }
        }
        // 添加到好友申请表里
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setAppId(userId).setFriendId(user.getId());
        if(friendsRequestService.addFriendRequest(friendsRequest))
            return ResultUtils.success("添加成功");
        else
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"添加好友失败");
    }

    @Operation(summary = "同意好友申请")
    @PostMapping("/agreeFriendsRequest")
    public BaseResponse agreeFriendsRequest(@RequestParam(value = "userId") int userId, @RequestParam(value = "friendId") int friendId) {
        if(friendsService.addFriends(userId, friendId))
            return ResultUtils.success("添加好友成功");
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,"添加好友失败");
    }
    
    @Operation(summary = "检查用户之间的好友关系")
    @PostMapping("/checkFriendship")
    public BaseResponse checkFriendship(@RequestParam(value = "userId") int userId, @RequestParam(value = "friendId") int friendId) {
        try {
            // 调用friendsService中的ifFriends方法检查好友关系
            boolean isFriend = friendsService.ifFriends(userId, friendId);
            
            // 返回好友关系状态（true/false）
            return ResultUtils.success(isFriend);
        } catch (Exception e) {
            log.error("检查好友关系出错", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "检查好友关系失败");
        }
    }
}
