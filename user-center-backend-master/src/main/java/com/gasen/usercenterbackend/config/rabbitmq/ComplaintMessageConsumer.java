package com.gasen.usercenterbackend.config.rabbitmq;

import com.gasen.usercenterbackend.model.dao.Complaint;
import com.gasen.usercenterbackend.service.IComplaintService;
import com.gasen.usercenterbackend.service.IUserService;
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
    private IUserService userService;

    /**
     * 处理投诉消息
     * 使用自定义的线程池处理消息
     * @param complaintId 投诉ID
     */
    @RabbitListener(queues = "complaint.queue", containerFactory = "rabbitListenerContainerFactory")
    public void processComplaint(Long complaintId) {
        log.info("收到投诉处理消息，投诉ID: {}, 当前线程: {}", complaintId, Thread.currentThread().getName());
        
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
            IComplaintService.ComplaintCheckResult result = complaintService.checkComplaintContent(complaint.getContent());
            if(result==null) {
                log.error("检查投诉内容有效性失败");
                return;
            }
            if(result.isValid()){
                boolean res = complaintService.handleValidComplaint(complaintId);
                // 对用户进行扣分
                
                if(!res){
                    log.error("处理成功的投诉id: {} 消息失败", complaintId);
                }
            }else{
                boolean res = complaintService.handleInvalidComplaint(complaintId, result.rejectReason());
                if(!res){
                    log.error("处理失败的投诉id: {} 消息失败", complaintId);
                }
            }

        } catch (Exception e) {
            log.error("处理投诉消息异常", e);
        }
    }
}