package com.gasen.usercenterbackend.mq;

import com.gasen.usercenterbackend.model.dao.Complaint;
import com.gasen.usercenterbackend.service.IComplaintService;
import com.gasen.usercenterbackend.service.ICreditRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 投诉消息消费者
 */
@Component
@Slf4j
public class ComplaintMessageConsumer {
    
    @Resource
    private IComplaintService complaintService;
    
    @Resource
    private ICreditRecordService creditRecordService;
    
    /**
     * 处理投诉消息
     * @param complaintId 投诉ID
     */
    @RabbitListener(queues = "complaint.queue")
    public void processComplaint(Long complaintId) {
        log.info("收到投诉处理消息，投诉ID: {}", complaintId);
        
        try {
            // 获取投诉记录
            Complaint complaint = complaintService.getById(complaintId);
            if (complaint == null) {
                log.error("投诉记录不存在: {}", complaintId);
                return;
            }
            
            // 如果投诉状态不是待处理，则跳过
            if (complaint.getStatus() != 0) {
                log.info("投诉已处理，状态: {}", complaint.getStatus());
                return;
            }
            
            // 检查投诉内容有效性
            IComplaintService.ComplaintCheckResult checkResult = complaintService.checkComplaintContent(complaint.getContent());
            
        } catch (Exception e) {
            log.error("处理投诉消息异常", e);
        }
    }
} 