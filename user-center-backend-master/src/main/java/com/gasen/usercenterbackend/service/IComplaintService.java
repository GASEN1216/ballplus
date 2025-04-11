package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gasen.usercenterbackend.model.dao.Complaint;
import com.gasen.usercenterbackend.model.vo.ComplaintVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 投诉记录表 服务类
 * </p>
 *
 * @author claude
 * @since 2024-03-09
 */
public interface IComplaintService extends IService<Complaint> {

    /**
     * 检查投诉内容有效性
     */
    ComplaintCheckResult checkComplaintContent(String content);
    
    /**
     * 创建投诉记录
     * @param complaint 投诉记录
     * @return 投诉ID
     */
    Long createComplaint(Complaint complaint);
    
    /**
     * 检查用户是否已经对该活动提交过投诉
     * @param userId 用户ID
     * @param eventId 活动ID
     * @return 是否存在投诉
     */
    boolean hasComplaint(Long userId, Long eventId);
    
    /**
     * 获取活动的投诉记录
     * @param eventId 活动ID
     * @return 投诉记录列表
     */
    List<Complaint> getComplaintsByEventId(Long eventId);
    
    /**
     * 批量异步处理投诉并返回结果
     * @param userId 投诉人ID
     * @param eventId 活动ID
     * @param content 投诉内容
     * @param complainedIds 被投诉人ID列表
     * @return CompletableFuture<List<Long>> 投诉ID列表的Future
     */
    CompletableFuture<List<Long>> processComplaintsAsync(Long userId, Long eventId, String content, List<Long> complainedIds);
    
    /**
     * 批量创建投诉记录
     * @param complaints 投诉记录列表
     * @return 投诉ID列表
     */
    List<Long> batchCreateComplaints(List<Complaint> complaints);

    boolean handleValidComplaint(Long complaintId);

    boolean handleInvalidComplaint(Long complaintId, String rejectReason);

    /**
     * 获取所有投诉列表（管理员分页）
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param status 状态过滤 (可选, 0=待处理, 1=通过, 2=拒绝)
     * @return 投诉视图对象分页结果
     */
    Page<ComplaintVO> getAllComplaintsAdmin(long pageNum, long pageSize, Integer status);


    /**
     * 投诉内容检查结果
     */
    record ComplaintCheckResult(boolean isValid, String rejectReason) {}
} 