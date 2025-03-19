package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.model.dto.ResourceQueryRequest;
import com.gasen.usercenterbackend.model.dto.ResourceVO;

/**
 * 资源服务接口
 */
public interface ResourceService {
    /**
     * 分页查询资源列表
     *
     * @param request 查询请求
     * @return 资源列表
     */
    Page<ResourceVO> listResources(ResourceQueryRequest request);

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
     */
    Page<ResourceVO> listFavorites(Long userId, Integer page, Integer size);

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