// 商品状态枚举 (假设 0=上架, 1=下架，需与后端确认)
export enum ProductStatus {
  ON_SALE = 0,
  OFF_SALE = 1
}

// 商品接口
export interface Product {
  id: number;
  name: string;
  description?: string; // 描述 (可选)
  price: number;        // 价格
  stock: number;        // 库存
  status: ProductStatus;  // 状态 (使用枚举)
  imageUrl?: string;     // 商品图片 URL (可选)
  createTime?: string;   // 创建时间 (后端返回，前端创建/更新时通常不需要)
  updateTime?: string;   // 更新时间 (后端返回)
}

// 分页查询参数
export interface ProductQueryParams {
  page: number;
  size: number;
  keyword?: string; // 可选的搜索关键词
  status?: ProductStatus | null; // 可选的状态过滤
}

// 创建商品请求 (通常不需要 id, createTime, updateTime)
export type CreateProductRequest = Omit<Product, 'id' | 'createTime' | 'updateTime'>;

// 更新商品请求 (需要 id，其他字段可选)
export type UpdateProductRequest = Partial<Omit<Product, 'createTime' | 'updateTime'>> & Required<Pick<Product, 'id'>>; 

// 通用分页响应接口 (如果项目中没有统一的定义)
export interface Page<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages?: number;
} 