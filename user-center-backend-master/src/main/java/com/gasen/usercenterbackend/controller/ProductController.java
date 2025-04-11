package com.gasen.usercenterbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.dao.Product;
import com.gasen.usercenterbackend.model.dto.ProductQueryParams;
import com.gasen.usercenterbackend.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private IProductService productService;

    /**
     * 获取所有商品列表
     * @return 商品列表
     */
    @Operation(summary = "获取商城所有商品")
    @GetMapping("/wx/list")
    public BaseResponse<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResultUtils.success(products);
    }

    /**
     * 根据ID获取商品
     * @param id 商品ID
     * @return 商品信息
     */
    @Operation(summary = "获取商品详情")
    @GetMapping("/wx/detail")
    public BaseResponse<Product> getProductById(@RequestParam("id") Integer id) {
        if (id == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "商品ID不能为空");
        }
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "商品不存在");
        }
        return ResultUtils.success(product);
    }

    /**
     * 管理员添加商品
     * @param product 商品信息
     * @return 添加结果
     */
    @Operation(summary = "添加商品")
    @PostMapping("/admin/add")
    public BaseResponse<Boolean> addProduct(@RequestBody Product product) {
        if (product == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "商品信息不能为空");
        }
        boolean result = productService.addProduct(product);
        return ResultUtils.success(result);
    }

    /**
     * 管理员更新商品
     * @param product 商品信息
     * @return 更新结果
     */
    @Operation(summary = "更新商品")
    @PutMapping("/admin/update")
    public BaseResponse<Boolean> updateProduct(@RequestBody Product product) {
        if (product == null || product.getId() == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "商品信息不完整");
        }
        boolean result = productService.updateProduct(product);
        return ResultUtils.success(result);
    }

    /**
     * 管理员删除商品
     * @param id 商品ID
     * @return 删除结果
     */
    @Operation(summary = "删除商品")
    @DeleteMapping("/admin/delete")
    public BaseResponse<Boolean> deleteProduct(@RequestParam("id") Integer id) {
        if (id == null) {
            return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "商品ID不能为空");
        }
        boolean result = productService.deleteProduct(id);
        return ResultUtils.success(result);
    }

    /**
     * 获取商品分页列表 (Admin)
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词 (可选)
     * @param status 状态过滤 (可选)
     * @return 商品分页列表
     */
    @GetMapping("/admin/list") // 新的 Admin 列表接口路径
    public BaseResponse getAllProductsAdmin(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            // 将请求参数封装到 DTO 中 (如果使用 DTO)
            ProductQueryParams queryParams = new ProductQueryParams();
            queryParams.setKeyword(keyword);
            queryParams.setStatus(status);

            Page<Product> productPage = productService.getAllProductsAdmin(pageNum, pageSize, queryParams);
            // 注意：这里直接返回 Page<Product>，如果前端期望特定 Map 结构，需要在这里转换
            // 或者修改前端 API 调用期望 Page<Product>
            return ResultUtils.success(productPage);
        } catch (Exception e) {
            log.error("管理员获取商品列表异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "获取商品列表失败！");
        }
    }

    /**
     * 更新商品状态 (上架/下架) (Admin)
     * @param id 商品ID (路径参数)
     * @param status 新的状态 (查询参数)
     * @return 操作结果
     */
    @PutMapping("/admin/status/{id}") // 新的状态更新接口路径
    public BaseResponse updateProductStatus(
            @PathVariable Integer id, // 从路径获取 ID
            @RequestParam Integer status) { // 从查询参数获取状态
        try {
            if (id == null || status == null) {
                return ResultUtils.error(ErrorCode.PARAMETER_ERROR, "参数不能为空");
            }
            // 可以在这里添加更严格的状态值校验 (例如必须是 0 或 1)
            boolean result = productService.updateProductStatus(id, status);
            if (result) {
                return ResultUtils.success(true);
            } else {
                // 可能商品不存在或更新失败
                return ResultUtils.error(ErrorCode.OPERATION_ERROR, "更新商品状态失败！");
            }
        } catch (Exception e) {
            log.error("管理员更新商品状态异常", e);
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "更新商品状态失败！");
        }
    }
} 