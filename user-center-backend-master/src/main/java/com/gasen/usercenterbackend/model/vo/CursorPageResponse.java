package com.gasen.usercenterbackend.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 游标分页响应
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
public class CursorPageResponse<T> {
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 下一页游标
     */
    private String nextCursor;
    
    /**
     * 是否还有更多数据
     */
    private Boolean hasMore;
    
    /**
     * 构造函数
     * 
     * @param records 记录列表
     * @param nextCursor 下一页游标
     * @param hasMore 是否还有更多数据
     */
    public CursorPageResponse(List<T> records, String nextCursor, Boolean hasMore) {
        this.records = records;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
    }
    
    /**
     * 构建游标分页响应
     * 
     * @param records 记录列表
     * @param nextCursor 下一页游标
     * @param hasMore 是否还有更多数据
     * @return 游标分页响应
     */
    public static <T> CursorPageResponse<T> build(List<T> records, String nextCursor, Boolean hasMore) {
        CursorPageResponse<T> response = new CursorPageResponse<>();
        response.setRecords(records);
        response.setNextCursor(nextCursor);
        response.setHasMore(hasMore);
        return response;
    }
} 