package com.gasen.usercenterbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.model.dao.Product;
import com.gasen.usercenterbackend.model.dto.ProductQueryParams;

import java.util.List;

public interface IProductService {
    
    /**
     * 获取所有商城商品
     * @return 商品列表
     */
    List<Product> getAllProducts();
    
    /**
     * 根据ID获取商品
     * @param id 商品ID
     * @return 商品信息
     */
    Product getProductById(Integer id);
    
    /**
     * 添加新商品
     * @param product 商品信息
     * @return 是否成功
     */
    boolean addProduct(Product product);
    
    /**
     * 更新商品信息
     * @param product 商品信息
     * @return 是否成功
     */
    boolean updateProduct(Product product);
    
    /**
     * 删除商品
     * @param id 商品ID
     * @return 是否成功
     */
    boolean deleteProduct(Integer id);

    /**
     * 获取商品分页列表 (Admin)
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param queryParams 查询参数 (例如 keyword, status)
     * @return 商品分页数据
     */
    Page<Product> getAllProductsAdmin(long pageNum, long pageSize, ProductQueryParams queryParams); // 参数类型可以调整

    /**
     * 更新商品状态 (上架/下架)
     *
     * @param id 商品ID
     * @param status 新的状态
     * @return 是否成功
     */
    boolean updateProductStatus(Integer id, Integer status); // 假设状态是 Integer

} 