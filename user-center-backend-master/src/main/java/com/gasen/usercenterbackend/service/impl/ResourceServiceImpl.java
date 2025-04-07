package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.mapper.ResourceMapper;
import com.gasen.usercenterbackend.mapper.UserFavoriteMapper;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.dto.ResourceQueryRequest;
import com.gasen.usercenterbackend.model.dto.ResourceVO;
import com.gasen.usercenterbackend.model.entity.Resource;
import com.gasen.usercenterbackend.model.entity.UserFavorite;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;
import com.gasen.usercenterbackend.service.ResourceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * 资源服务实现类
 */
@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Override
    public Page<ResourceVO> listResources(ResourceQueryRequest request) {
        Page<Resource> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(Resource::getCategory, request.getCategory());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Resource::getTitle, request.getKeyword())
                  .or()
                  .like(Resource::getDescription, request.getKeyword())
                  .or()
                  .like(Resource::getContent, request.getKeyword());
        }
        
        // 按创建时间降序排序
        wrapper.orderByDesc(Resource::getCreateTime);
        
        // 执行查询
        Page<Resource> resourcePage = resourceMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<ResourceVO> voPage = new Page<>();
        voPage.setRecords(resourcePage.getRecords().stream().map(resource -> {
            ResourceVO vo = new ResourceVO();
            vo.setId(resource.getId());
            vo.setTitle(resource.getTitle());
            vo.setDescription(resource.getDescription());
            vo.setCoverImage(resource.getCoverImage());
            vo.setType(resource.getType());
            vo.setContent(resource.getContent());
            vo.setViews(resource.getViews());
            vo.setLikes(resource.getLikes());
            vo.setCategory(resource.getCategory());
            vo.setCreateTime(resource.getCreateTime());
            vo.setIsFavorite(false);
            return vo;
        }).collect(Collectors.toList()));
        voPage.setCurrent(resourcePage.getCurrent());
        voPage.setSize(resourcePage.getSize());
        voPage.setTotal(resourcePage.getTotal());
        voPage.setPages(resourcePage.getPages());
        return voPage;
    }

    @Override
    public ResourceVO getResourceDetail(Long id) {
        Resource resource = resourceMapper.selectById(id);
        if (resource == null) {
            return null;
        }
        
        ResourceVO vo = new ResourceVO();
        vo.setId(resource.getId());
        vo.setTitle(resource.getTitle());
        vo.setDescription(resource.getDescription());
        vo.setCoverImage(resource.getCoverImage());
        vo.setType(resource.getType());
        vo.setContent(resource.getContent());
        vo.setViews(resource.getViews());
        vo.setLikes(resource.getLikes());
        vo.setCategory(resource.getCategory());
        vo.setCreateTime(resource.getCreateTime());
        vo.setIsFavorite(false);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateViews(Long id) {
        Resource resource = new Resource();
        resource.setId(id);
        resource.setViews(resourceMapper.selectById(id).getViews() + 1);
        return resourceMapper.updateById(resource) > 0;
    }

    @Override
    public ResourceVO getTopResource() {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Resource::getViews);
        wrapper.last("LIMIT 1");
        
        Resource resource = resourceMapper.selectOne(wrapper);
        if (resource == null) {
            return null;
        }
        
        ResourceVO vo = new ResourceVO();
        vo.setId(resource.getId());
        vo.setTitle(resource.getTitle());
        vo.setDescription(resource.getDescription());
        vo.setCoverImage(resource.getCoverImage());
        vo.setType(resource.getType());
        vo.setContent(resource.getContent());
        vo.setViews(resource.getViews());
        vo.setLikes(resource.getLikes());
        vo.setCategory(resource.getCategory());
        vo.setCreateTime(resource.getCreateTime());
        vo.setIsFavorite(false);
        return vo;
    }

    @Override
    public Page<ResourceVO> listFavorites(Long userId, Integer page, Integer size) {
        Page<UserFavorite> favoritePage = new Page<>(page, size);
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .orderByDesc(UserFavorite::getCreateTime);
        
        // 查询收藏记录
        Page<UserFavorite> userFavoritePage = userFavoriteMapper.selectPage(favoritePage, wrapper);
        
        // 获取资源ID列表
        List<Long> resourceIds = userFavoritePage.getRecords().stream()
                .map(UserFavorite::getResourceId)
                .collect(Collectors.toList());
        
        if (resourceIds.isEmpty()) {
            return new Page<>();
        }
        
        // 查询资源详情
        List<Resource> resources = resourceMapper.selectBatchIds(resourceIds);
        
        // 转换为VO
        List<ResourceVO> records = resources.stream().map(resource -> {
            ResourceVO vo = new ResourceVO();
            vo.setId(resource.getId());
            vo.setTitle(resource.getTitle());
            vo.setDescription(resource.getDescription());
            vo.setCoverImage(resource.getCoverImage());
            vo.setType(resource.getType());
            vo.setContent(resource.getContent());
            vo.setViews(resource.getViews());
            vo.setLikes(resource.getLikes());
            vo.setCategory(resource.getCategory());
            vo.setCreateTime(resource.getCreateTime());
            vo.setIsFavorite(true);
            return vo;
        }).collect(Collectors.toList());
        
        // 构建分页结果
        Page<ResourceVO> resultPage = new Page<>(userFavoritePage.getCurrent(), userFavoritePage.getSize(), userFavoritePage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFavorite(Long userId, Long resourceId) {
        // 检查是否已收藏
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getResourceId, resourceId);
        if (userFavoriteMapper.selectCount(wrapper) > 0) {
            return true;
        }
        
        // 添加收藏
        UserFavorite userFavorite = new UserFavorite();
        userFavorite.setUserId(userId);
        userFavorite.setResourceId(resourceId);
        return userFavoriteMapper.insert(userFavorite) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFavorite(Long userId, Long resourceId) {
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId)
               .eq(UserFavorite::getResourceId, resourceId);
        return userFavoriteMapper.delete(wrapper) > 0;
    }

    @Override
    public CursorPageResponse<ResourceVO> listResourcesWithCursor(ResourceQueryRequest request, CursorPageRequest cursorRequest) {
        // 获取游标和分页参数
        String cursor = cursorRequest.getCursor();
        Integer pageSize = cursorRequest.getPageSize();
        Boolean isAsc = cursorRequest.getAsc();
        
        // 默认参数处理
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10; // 默认每页10条
        }
        
        // 默认降序
        boolean asc = Boolean.TRUE.equals(isAsc);
        
        // 构建查询条件
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (request != null) {
            if (StringUtils.hasText(request.getCategory())) {
                wrapper.eq(Resource::getCategory, request.getCategory());
            }
            if (StringUtils.hasText(request.getKeyword())) {
                wrapper.like(Resource::getTitle, request.getKeyword())
                      .or()
                      .like(Resource::getDescription, request.getKeyword())
                      .or()
                      .like(Resource::getContent, request.getKeyword());
            }
        }
        
        // 添加游标条件
        if (StringUtils.hasText(cursor)) {
            try {
                // 将ISO格式的游标字符串转换为Date
                Date cursorTime = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(cursor);
                if (asc) {
                    // 升序查询，获取创建时间大于游标的记录
                    wrapper.gt(Resource::getCreateTime, cursorTime);
                    wrapper.orderByAsc(Resource::getCreateTime);
                } else {
                    // 降序查询，获取创建时间小于游标的记录
                    wrapper.lt(Resource::getCreateTime, cursorTime);
                    wrapper.orderByDesc(Resource::getCreateTime);
                }
            } catch (Exception e) {
                // 游标解析失败，使用默认排序
                if (asc) {
                    wrapper.orderByAsc(Resource::getCreateTime);
                } else {
                    wrapper.orderByDesc(Resource::getCreateTime);
                }
            }
        } else {
            // 没有游标时使用默认排序
            if (asc) {
                wrapper.orderByAsc(Resource::getCreateTime);
            } else {
                wrapper.orderByDesc(Resource::getCreateTime);
            }
        }
        
        // 多查询一条用于判断是否有更多数据
        wrapper.last("LIMIT " + (pageSize + 1));
        
        // 执行查询
        List<Resource> records = resourceMapper.selectList(wrapper);
        
        // 判断是否有更多数据
        boolean hasMore = false;
        if (records.size() > pageSize) {
            hasMore = true;
            // 移除多查询的一条数据
            records.remove(records.size() - 1);
        }
        
        // 转换为VO
        List<ResourceVO> voList = records.stream().map(resource -> {
            ResourceVO vo = new ResourceVO();
            vo.setId(resource.getId());
            vo.setTitle(resource.getTitle());
            vo.setDescription(resource.getDescription());
            vo.setCoverImage(resource.getCoverImage());
            vo.setType(resource.getType());
            vo.setContent(resource.getContent());
            vo.setViews(resource.getViews());
            vo.setLikes(resource.getLikes());
            vo.setCategory(resource.getCategory());
            vo.setCreateTime(resource.getCreateTime());
            vo.setIsFavorite(false);
            return vo;
        }).collect(Collectors.toList());
        
        // 获取下一个游标
        String nextCursor = null;
        if (hasMore && !records.isEmpty()) {
            Resource lastRecord = records.get(records.size() - 1);
            // 将Date转换为ISO格式字符串
            nextCursor = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(lastRecord.getCreateTime());
        }
        
        // 构建游标分页响应
        return new CursorPageResponse<>(voList, nextCursor, hasMore);
    }

    @Override
    public CursorPageResponse<ResourceVO> listFavoritesWithCursor(Long userId, CursorPageRequest cursorRequest) {
        // 获取游标和分页参数
        String cursor = cursorRequest.getCursor();
        Integer pageSize = cursorRequest.getPageSize();
        Boolean isAsc = cursorRequest.getAsc();
        
        // 默认参数处理
        if (pageSize == null || pageSize <= 0 || pageSize > 100) {
            pageSize = 10; // 默认每页10条
        }
        
        // 默认降序
        boolean asc = Boolean.TRUE.equals(isAsc);
        
        // 构建收藏记录查询条件
        LambdaQueryWrapper<UserFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFavorite::getUserId, userId);
        
        // 添加游标条件
        if (StringUtils.hasText(cursor)) {
            try {
                // 将ISO格式的游标字符串转换为Date
                Date cursorTime = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(cursor);
                if (asc) {
                    // 升序查询，获取创建时间大于游标的记录
                    wrapper.gt(UserFavorite::getCreateTime, cursorTime);
                    wrapper.orderByAsc(UserFavorite::getCreateTime);
                } else {
                    // 降序查询，获取创建时间小于游标的记录
                    wrapper.lt(UserFavorite::getCreateTime, cursorTime);
                    wrapper.orderByDesc(UserFavorite::getCreateTime);
                }
            } catch (Exception e) {
                // 游标解析失败，使用默认排序
                if (asc) {
                    wrapper.orderByAsc(UserFavorite::getCreateTime);
                } else {
                    wrapper.orderByDesc(UserFavorite::getCreateTime);
                }
            }
        } else {
            // 没有游标时使用默认排序
            if (asc) {
                wrapper.orderByAsc(UserFavorite::getCreateTime);
            } else {
                wrapper.orderByDesc(UserFavorite::getCreateTime);
            }
        }
        
        // 多查询一条用于判断是否有更多数据
        wrapper.last("LIMIT " + (pageSize + 1));
        
        // 查询收藏记录
        List<UserFavorite> userFavorites = userFavoriteMapper.selectList(wrapper);
        
        // 判断是否有更多数据
        boolean hasMore = false;
        if (userFavorites.size() > pageSize) {
            hasMore = true;
            // 移除多查询的一条数据
            userFavorites.remove(userFavorites.size() - 1);
        }
        
        // 如果没有收藏记录，直接返回空结果
        if (userFavorites.isEmpty()) {
            return new CursorPageResponse<>(new ArrayList<>(), null, false);
        }
        
        // 获取资源ID列表
        List<Long> resourceIds = userFavorites.stream()
                .map(UserFavorite::getResourceId)
                .collect(Collectors.toList());
        
        // 查询资源详情
        List<Resource> resources = resourceMapper.selectBatchIds(resourceIds);
        
        // 创建收藏ID到收藏记录的映射，用于获取创建时间
        final var favoriteMap = userFavorites.stream()
                .collect(Collectors.toMap(UserFavorite::getResourceId, favorite -> favorite));
        
        // 转换为VO
        List<ResourceVO> voList = resources.stream().map(resource -> {
            ResourceVO vo = new ResourceVO();
            vo.setId(resource.getId());
            vo.setTitle(resource.getTitle());
            vo.setDescription(resource.getDescription());
            vo.setCoverImage(resource.getCoverImage());
            vo.setType(resource.getType());
            vo.setContent(resource.getContent());
            vo.setViews(resource.getViews());
            vo.setLikes(resource.getLikes());
            vo.setCategory(resource.getCategory());
            vo.setCreateTime(resource.getCreateTime());
            vo.setIsFavorite(true);
            return vo;
        }).collect(Collectors.toList());
        
        // 获取下一个游标
        String nextCursor = null;
        if (hasMore && !userFavorites.isEmpty()) {
            UserFavorite lastFavorite = userFavorites.get(userFavorites.size() - 1);
            // 将Date转换为ISO格式字符串
            nextCursor = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(lastFavorite.getCreateTime());
        }
        
        // 构建游标分页响应
        return new CursorPageResponse<>(voList, nextCursor, hasMore);
    }
} 