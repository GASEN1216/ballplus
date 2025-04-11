package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.dto.ResourceQueryRequest;
import com.gasen.usercenterbackend.model.dto.ResourceVO;
import com.gasen.usercenterbackend.model.entity.Resource;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;

/**
 * 资源服务接口
 */
public interface ResourceService {
    /**
     * 创建资源
     * @param resource 资源信息
     * @return 资源ID
     */
    Long createResource(Resource resource);

    /**
     * 更新资源
     * @param resource 资源信息
     * @return 是否成功
     */
    boolean updateResource(Resource resource);

    /**
     * 删除资源
     * @param id 资源ID
     * @return 是否成功
     */
    boolean deleteResource(Long id);

    /**
     * 分页查询资源列表
     *
     * @param request 查询请求
     * @return 资源列表
     * @deprecated 使用 listResourcesWithCursor 替代
     */
    @Deprecated
    Page<ResourceVO> listResources(ResourceQueryRequest request);
    
    /**
     * 游标分页查询资源列表
     *
     * @param request 查询请求
     * @param cursorRequest 游标分页请求
     * @return 游标分页资源列表
     */
    CursorPageResponse<ResourceVO> listResourcesWithCursor(ResourceQueryRequest request, CursorPageRequest cursorRequest);

    /**
     * 获取资源详情
     *
     * @param id 资源ID
     * @return 资源详情
     */
    ResourceVO getResourceDetail(Long id);

    /**
     * 更新资源浏览量
     *
     * @param id 资源ID
     * @return 是否成功
     */
    boolean updateViews(Long id);

    /**
     * 获取用户收藏列表
     *
     * @param userId 用户ID
     * @return 收藏列表
     * @deprecated 使用 listFavoritesWithCursor 替代
     */
    @Deprecated
    Page<ResourceVO> listFavorites(Long userId, Integer page, Integer size);
    
    /**
     * 游标分页获取用户收藏列表
     *
     * @param userId 用户ID
     * @param cursorRequest 游标分页请求
     * @return 游标分页收藏列表
     */
    CursorPageResponse<ResourceVO> listFavoritesWithCursor(Long userId, CursorPageRequest cursorRequest);

    /**
     * 添加收藏
     *
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否成功
     */
    boolean addFavorite(Long userId, Long resourceId);

    /**
     * 取消收藏
     *
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long resourceId);

    /**
     * 获取浏览量最高的资源
     *
     * @return 浏览量最高的资源
     */
    ResourceVO getTopResource();
} 