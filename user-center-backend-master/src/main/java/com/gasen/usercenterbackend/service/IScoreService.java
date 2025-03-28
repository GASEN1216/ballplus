package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gasen.usercenterbackend.model.dao.ScoreHistory;
import com.gasen.usercenterbackend.model.dao.User;
import com.gasen.usercenterbackend.model.dto.ScoreHistoryDTO;

import java.util.List;

public interface IScoreService extends IService<ScoreHistory> {

    /**
     * 记录赛点变动
     * @param userId 用户ID
     * @param changeAmount 变动数量
     * @param type 变动类型
     * @param description 描述
     * @return 是否成功
     */
    boolean recordScoreChange(Long userId, Integer changeAmount, String type, String description);

    /**
     * 获取用户赛点历史
     * @param userId 用户ID
     * @return 赛点历史列表
     */
    List<ScoreHistoryDTO> getUserScoreHistory(Long userId);

    /**
     * 处理用户每日首次登录加分
     * @param user 用户对象
     * @return 是否成功
     */
    boolean processLoginScore(User user);

    /**
     * 处理活动完成后的赛点奖励
     * @param creatorId 创建者ID
     * @param participantIds 参与者ID列表
     * @param activityName 活动名称
     * @return 是否成功
     */
    // TODO： 加到定时任务里去
    boolean processActivityCompletionScore(Long creatorId, List<Long> participantIds, String activityName);
} 