package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gasen.usercenterbackend.mapper.ProductMapper;
import com.gasen.usercenterbackend.model.dao.Product;
import com.gasen.usercenterbackend.model.dto.ProductQueryParams;
import com.gasen.usercenterbackend.service.IProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.baomidou.mybatisplus.extension.toolkit.Db.page;
import static com.baomidou.mybatisplus.extension.toolkit.Db.updateById;

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

    @Override
    public Page<Product> getAllProductsAdmin(long pageNum, long pageSize, ProductQueryParams queryParams) {
        Page<Product> pageRequest = new Page<>(pageNum, pageSize); // 创建 Page 对象作为请求参数
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

        // 添加查询条件
        if (queryParams != null) {
            if (StringUtils.isNotBlank(queryParams.getKeyword())) {
                // 假设按商品名称或描述搜索
                queryWrapper.like("name", queryParams.getKeyword())
                        .or()
                        .like("description", queryParams.getKeyword());
            }
            if (queryParams.getStatus() != null) {
                queryWrapper.eq("status", queryParams.getStatus());
            }
        }

        // 假设按创建时间降序排序
        queryWrapper.orderByDesc("create_time");

        // 将 IPage<Product> 强制转换为 Page<Product> 返回
        // 因为我们传入的是 Page 实例，所以理论上返回的也是 Page 实例，这个转换是安全的
        return this.productMapper.selectPage(pageRequest, queryWrapper);
    }

    @Override
    public boolean updateProductStatus(Integer id, Integer status) {
        if (id == null || status == null) {
            // 或者可以抛出异常
            return false;
        }
        // 校验 status 值是否合法 (例如 0 或 1)
        // if (status != 0 && status != 1) return false;

        Product product = new Product();
        product.setId(id);
        product.setStatus(status); // 假设状态字段是 status

        return updateById(product); // 使用 Mybatis-Plus 更新
    }
} 