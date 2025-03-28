package com.gasen.usercenterbackend.controller;

import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.model.dao.Product;
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
} 