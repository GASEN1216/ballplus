package com.gasen.usercenterbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.dto.CursorPageRequest;
import com.gasen.usercenterbackend.model.dto.ResourceQueryRequest;
import com.gasen.usercenterbackend.model.dto.ResourceVO;
import com.gasen.usercenterbackend.model.entity.Resource;
import com.gasen.usercenterbackend.model.vo.CursorPageResponse;
import com.gasen.usercenterbackend.service.ResourceService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源控制器
 */
@RestController
@RequestMapping("/user/wx/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    /**
     * 创建资源
     */
    @PostMapping("/")
    public BaseResponse<Long> createResource(@RequestBody Resource resource) {
        if (resource == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源信息不能为空");
        }
        if (resource.getTitle() == null || resource.getTitle().trim().isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源标题不能为空");
        }
        if (resource.getContent() == null || resource.getContent().trim().isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源内容不能为空");
        }
        if (resource.getType() == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源类型不能为空");
        }
        
        Long resourceId = resourceService.createResource(resource);
        return ResultUtils.success(resourceId);
    }

    /**
     * 更新资源
     */
    @PutMapping("/{id}")
    public BaseResponse<Boolean> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源ID不能为空或非法");
        }
        if (resource == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源信息不能为空");
        }
        if (!id.equals(resource.getId())) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源ID不匹配");
        }
        
        boolean result = resourceService.updateResource(resource);
        return ResultUtils.success(result);
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteResource(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源ID不能为空或非法");
        }
        
        boolean result = resourceService.deleteResource(id);
        return ResultUtils.success(result);
    }

    /**
     * 获取资源列表
     */
    @GetMapping("/")
    public BaseResponse listResources(ResourceQueryRequest request) {
        if (request == null) {
            request = new ResourceQueryRequest();
        }
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getSize() == null || request.getSize() < 1) {
            request.setSize(10);
        }
        Page<ResourceVO> page = resourceService.listResources(request);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("size", page.getSize());
        result.put("current", page.getCurrent());
        result.put("pages", page.getPages());
        
        return ResultUtils.success(result);
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/{id}")
    public BaseResponse getResourceDetail(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源ID不能为空或非法");
        }
        ResourceVO result = resourceService.getResourceDetail(id);
        if (result == null) {
            return ResultUtils.error(ErrorCode.POST_NOT_FOUND, "资源不存在");
        }
        // 更新浏览量
        resourceService.updateViews(id);
        return ResultUtils.success(result);
    }

    /**
     * 获取浏览量最高的资源
     */
    @GetMapping("/top")
    public BaseResponse getTopResource() {
        ResourceVO topResource = resourceService.getTopResource();
        if (topResource == null) {
            return ResultUtils.error(ErrorCode.POST_NOT_FOUND, "没有资源数据");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", topResource.getId());
        result.put("title", topResource.getTitle());
        return ResultUtils.success(result);
    }

    /**
     * 更新资源浏览量
     */
    @PutMapping("/{id}/view")
    public BaseResponse updateViews(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源ID不能为空或非法");
        }
        boolean result = resourceService.updateViews(id);
        return ResultUtils.success(result);
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/favorites")
    public BaseResponse listFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long userId) {
        if (userId == null || userId <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户ID不能为空或非法");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        Page<ResourceVO> pageResult = resourceService.listFavorites(userId, page, size);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("size", pageResult.getSize());
        result.put("current", pageResult.getCurrent());
        result.put("pages", pageResult.getPages());
        
        return ResultUtils.success(result);
    }

    /**
     * 添加收藏
     */
    @PostMapping("/{id}/favorite")
    public BaseResponse addFavorite(@PathVariable Long id, @RequestParam Long userId) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源ID不能为空或非法");
        }
        if (userId == null || userId <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户ID不能为空或非法");
        }
        boolean result = resourceService.addFavorite(userId, id);
        return ResultUtils.success(result);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{id}/favorite")
    public BaseResponse removeFavorite(@PathVariable Long id, @RequestParam Long userId) {
        if (id == null || id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "资源ID不能为空或非法");
        }
        if (userId == null || userId <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户ID不能为空或非法");
        }
        boolean result = resourceService.removeFavorite(userId, id);
        return ResultUtils.success(result);
    }

    /**
     * 获取资源列表（游标分页）
     */
    @PostMapping("/listWithCursor")
    public BaseResponse listResourcesWithCursor(@RequestBody Map<String, Object> requestMap) {
        ResourceQueryRequest request = null;
        CursorPageRequest cursorRequest = null;
        
        try {
            // 从Map中提取请求参数
            if (requestMap.containsKey("request")) {
                // 将request部分转换为ResourceQueryRequest对象
                Map<String, Object> requestData = (Map<String, Object>) requestMap.get("request");
                request = new ResourceQueryRequest();
                
                if (requestData.containsKey("type")) {
                    request.setType((Integer) requestData.get("type"));
                }
                if (requestData.containsKey("keyword")) {
                    request.setKeyword((String) requestData.get("keyword"));
                }
                if (requestData.containsKey("categoryId")) {
                    request.setCategoryId(Long.valueOf(requestData.get("categoryId").toString()));
                }
            }
            
            if (requestMap.containsKey("cursorRequest")) {
                // 将cursorRequest部分转换为CursorPageRequest对象
                Map<String, Object> cursorData = (Map<String, Object>) requestMap.get("cursorRequest");
                cursorRequest = new CursorPageRequest();
                
                if (cursorData.containsKey("cursor")) {
                    cursorRequest.setCursor((String) cursorData.get("cursor"));
                }
                if (cursorData.containsKey("pageSize")) {
                    cursorRequest.setPageSize((Integer) cursorData.get("pageSize"));
                }
                if (cursorData.containsKey("asc")) {
                    cursorRequest.setAsc((Boolean) cursorData.get("asc"));
                }
            }
        } catch (Exception e) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数解析错误");
        }
        
        if (request == null) {
            request = new ResourceQueryRequest();
        }
        if (cursorRequest == null) {
            cursorRequest = new CursorPageRequest();
        }
        
        CursorPageResponse<ResourceVO> response = resourceService.listResourcesWithCursor(request, cursorRequest);
        return ResultUtils.success(response);
    }

    /**
     * 获取用户收藏列表（游标分页）
     */
    @PostMapping("/favoritesWithCursor")
    public BaseResponse listFavoritesWithCursor(@RequestParam Long userId, @RequestBody CursorPageRequest cursorRequest) {
        if (userId == null || userId <= 0) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "用户ID不能为空或非法");
        }
        
        CursorPageResponse<ResourceVO> response = resourceService.listFavoritesWithCursor(userId, cursorRequest);
        return ResultUtils.success(response);
    }
} 