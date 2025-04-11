import request, { getRequestConfig } from '@/utils/request'
import type { 
  Product, 
  CreateProductRequest, 
  UpdateProductRequest, 
  ProductQueryParams, 
  Page,
  ProductStatus
} from '@/types/product'

/**
 * 获取商品分页列表 (Admin)
 */
export function getAllProductsAdmin(params: ProductQueryParams) {
  return request<Page<Product>>(getRequestConfig({
    url: '/product/admin/list',
    method: 'get',
    params
  }))
}

/**
 * 获取商品详情 (保留，但管理后台可能不用)
 */
export function getProductDetail(id: number) {
  return request<Product>(getRequestConfig({
    url: '/product/wx/detail',
    method: 'get',
    params: { id }
  }))
}

/**
 * 创建商品 (Admin)
 */
export function createProduct(data: CreateProductRequest) {
  return request<boolean>(getRequestConfig({
    url: '/product/admin/add',
    method: 'post',
    data
  }))
}

/**
 * 更新商品 (Admin)
 */
export function updateProduct(data: UpdateProductRequest) {
  return request<boolean>(getRequestConfig({
    url: '/product/admin/update',
    method: 'put',
    data
  }))
}

/**
 * 删除商品 (Admin)
 */
export function deleteProduct(id: number) {
  return request<boolean>(getRequestConfig({
    url: '/product/admin/delete',
    method: 'delete',
    params: { id }
  }))
}

/**
 * 更新商品状态 (上架/下架) (Admin)
 */
export function updateProductStatus(id: number, status: ProductStatus) {
  return request<boolean>(getRequestConfig({
    url: `/product/admin/status/${id}`,
    method: 'put',
    params: { status }
  }))
} 