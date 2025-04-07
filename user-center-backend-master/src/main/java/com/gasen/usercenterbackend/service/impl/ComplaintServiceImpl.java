package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gasen.usercenterbackend.config.DeepSeekConfig;
import com.gasen.usercenterbackend.mapper.ComplaintMapper;
import com.gasen.usercenterbackend.model.dao.Complaint;
import com.gasen.usercenterbackend.service.IComplaintService;
import com.gasen.usercenterbackend.service.ICreditRecordService;
import com.gasen.usercenterbackend.service.IUserEventService;
import com.gasen.usercenterbackend.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * <p>
 * 投诉记录表服务实现类
 * </p>
 */
@Service
@Slf4j
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements IComplaintService {

    @Resource
    private IUserService userService;
    
    @Resource
    private IUserEventService userEventService;
    
    @Resource
    private ICreditRecordService creditRecordService;
    
    @Resource
    private DeepSeekConfig deepSeekConfig;
    
    @Resource
    private RestTemplate restTemplate;
    
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建投诉记录
     * @param complaint 投诉记录
     * @return 投诉ID
     */
    @Override
    public Long createComplaint(Complaint complaint) {
        // 设置初始状态为待处理
        complaint.setStatus((byte) 0);
        complaint.setCreateTime(LocalDateTime.now());
        
        // 保存记录
        boolean saved = save(complaint);
        if (!saved) {
            log.error("保存投诉记录失败: {}", complaint);
            return null;
        }
        
        return complaint.getId();
    }

    /**
     * 批量创建投诉记录
     * @param complaints 投诉记录列表
     * @return 投诉ID列表
     */
    @Override
    public List<Long> batchCreateComplaints(List<Complaint> complaints) {
        // 设置初始状态和创建时间
        LocalDateTime now = LocalDateTime.now();
        complaints.forEach(complaint -> {
            complaint.setStatus((byte) 0);
            complaint.setCreateTime(now);
        });
        
        // 批量保存
        boolean saved = saveBatch(complaints);
        if (!saved) {
            log.error("批量保存投诉记录失败");
            return Collections.emptyList();
        }
        
        // 返回投诉ID列表
        return complaints.stream()
                .map(Complaint::getId)
                .collect(Collectors.toList());
    }

    /**
     * 批量异步处理投诉
     * @param userId 投诉人ID
     * @param eventId 活动ID
     * @param content 投诉内容
     * @param complainedIds 被投诉人ID列表
     * @return CompletableFuture<List<Long>> 投诉ID列表的Future
     */
    @Override
    @Async("asyncTaskExecutor")
    public CompletableFuture<List<Long>> processComplaintsAsync(Long userId, Long eventId, String content, List<Long> complainedIds) {
        log.info("开始异步处理投诉，投诉人：{}，活动：{}，被投诉人数：{}", userId, eventId, complainedIds.size());
        
        // 创建投诉对象列表
        List<Complaint> complaints = complainedIds.stream()
                .filter(complainedId -> !complainedId.equals(userId)) // 不能投诉自己
                .filter(complainedId -> userEventService.isUserParticipated(complainedId, eventId)) // 必须参与活动
                .map(complainedId -> new Complaint()
                        .setEventId(eventId)
                        .setComplainerId(userId)
                        .setComplainedId(complainedId)
                        .setContent(content))
                .collect(Collectors.toList());
        
        if (complaints.isEmpty()) {
            log.warn("没有有效的投诉对象");
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        
        // 批量保存投诉记录
        List<Long> complaintIds = batchCreateComplaints(complaints);
        
        // 将投诉ID发送到消息队列进行异步处理
        complaintIds.forEach(complaintId -> {
            rabbitTemplate.convertAndSend("complaint.queue", complaintId);
            log.info("投诉已提交到消息队列处理，投诉ID：{}", complaintId);
        });
        
        return CompletableFuture.completedFuture(complaintIds);
    }

    /**
     * 检查投诉内容有效性
     * 调用DeepSeek API进行内容审核
     *
     * @param content 投诉内容
     */
    @Override
    public ComplaintCheckResult checkComplaintContent(String content) {
        // 内容长度基本检查
        if (content == null || content.trim().length() < 5) {
            return new ComplaintCheckResult(false, "投诉内容过于简短，请详细描述投诉原因");
        }
        
        try {
            // 调用DeepSeek API进行内容审核
            String prompt = "你是一个内容审核助手，负责审核用户提交的投诉内容。请判断以下投诉内容是否合理、是否包含不当言论或敏感内容。" +
                    "如果内容合理，请回复\"VALID\"，如果内容不合理，请回复\"INVALID:\"后跟拒绝原因。\n\n" +
                    "投诉内容: " + content;
            
            String apiUrl = "https://api.deepseek.com/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepSeekConfig.getKey());
            
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("messages", List.of(Map.of(
                    "role", "system", 
                    "content", "你是一个内容审核助手。"
                    ), message));
            requestBody.put("stream", false);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        Map<String, String> messageObj = (Map<String, String>) choice.get("message");
                        String result = messageObj.get("content").trim();
                        
                        log.info("DeepSeek API 返回结果: {}", result);
                        
                        if (result.startsWith("VALID")) {
                            return new ComplaintCheckResult(true, null);
                        } else if (result.startsWith("INVALID:")) {
                            String reason = result.substring(8).trim();
                            return new ComplaintCheckResult(false, reason);
                        }
                    }
                }
            }
            
            log.error("调用DeepSeek API失败: {}", response);

        } catch (Exception e) {
            log.error("调用DeepSeek API异常", e);
        }
        return null;
    }

    /**
     * 检查用户是否已经对该活动提交过投诉
     * @param userId 用户ID
     * @param eventId 活动ID
     * @return 是否存在投诉
     */
    @Override
    public boolean hasComplaint(Long userId, Long eventId) {
        QueryWrapper<Complaint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("complainer_id", userId)
                .eq("event_id", eventId);
        
        return count(queryWrapper) > 0;
    }

    /**
     * 获取活动的投诉记录
     * @param eventId 活动ID
     * @return 投诉记录列表
     */
    @Override
    public List<Complaint> getComplaintsByEventId(Long eventId) {
        QueryWrapper<Complaint> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("event_id", eventId)
                .orderByDesc("create_time");
        
        return list(queryWrapper);
    }
    
    /**
     * 处理有效投诉，扣除被投诉人信誉分
     * @param complaintId 投诉ID
     * @return 处理结果
     */
    @Override
    public boolean handleValidComplaint(Long complaintId) {
        Complaint complaint = getById(complaintId);
        if (complaint == null) {
            log.error("投诉记录不存在: {}", complaintId);
            return false;
        }
        
        // 更新投诉状态为有效
        complaint.setStatus((byte) 1);
        updateById(complaint);
        
        // 记录信誉分变动并扣除被投诉人信誉分
        Long complainedId = complaint.getComplainedId();
        creditRecordService.addCreditRecord(
                complainedId,
                -1, // 扣除1分
                1, // 类型：投诉扣分
                "被用户" + complaint.getComplainerId() + "投诉",
                complaintId
        );
        log.info("用户 {} 因被投诉扣除1分信誉分", complainedId);
        
        return true;
    }
    
    /**
     * 处理无效投诉
     * @param complaintId 投诉ID
     * @param rejectReason 拒绝原因
     * @return 处理结果
     */
    @Override
    public boolean handleInvalidComplaint(Long complaintId, String rejectReason) {
        Complaint complaint = getById(complaintId);
        if (complaint == null) {
            log.error("投诉记录不存在: {}", complaintId);
            return false;
        }
        
        // 更新投诉状态为无效
        complaint.setStatus((byte) 2);
        complaint.setRejectReason(rejectReason);
        
        return updateById(complaint);
    }
} 