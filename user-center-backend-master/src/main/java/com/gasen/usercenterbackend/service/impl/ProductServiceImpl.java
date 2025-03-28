package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.mapper.ProductMapper;
import com.gasen.usercenterbackend.model.dao.Product;
import com.gasen.usercenterbackend.service.IProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    @Resource
    private ProductMapper productMapper;

    @Override
    public List<Product> getAllProducts() {
        // 只返回上架的商品
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1);
        return productMapper.selectList(queryWrapper);
    }

    @Override
    public Product getProductById(Integer id) {
        return productMapper.selectById(id);
    }

    @Override
    public boolean addProduct(Product product) {
        if (product == null) {
            return false;
        }
        
        // 设置创建时间和更新时间
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        
        // 默认商品状态为上架
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        
        return productMapper.insert(product) > 0;
    }

    @Override
    public boolean updateProduct(Product product) {
        if (product == null || product.getId() == null) {
            return false;
        }
        
        // 更新时间
        product.setUpdateTime(LocalDateTime.now());
        
        return productMapper.updateById(product) > 0;
    }

    @Override
    public boolean deleteProduct(Integer id) {
        return productMapper.deleteById(id) > 0;
    }
} 