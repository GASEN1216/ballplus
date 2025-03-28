package com.gasen.usercenterbackend.service;

import com.gasen.usercenterbackend.model.dao.Product;

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
} 