<template>
  <div class="product-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" ref="searchFormRef" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="商品类型">
          <el-select v-model="searchForm.type" placeholder="请选择商品类型" clearable>
            <el-option
              v-for="item in PRODUCT_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品状态">
          <el-select v-model="searchForm.status" placeholder="请选择商品状态" clearable>
            <el-option
              v-for="item in PRODUCT_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商品列表 -->
    <el-card class="list-card">
      <div class="table-header">
        <h3>商品列表</h3>
        <el-button type="primary" @click="handleCreate">新增商品</el-button>
      </div>
      <el-table
        v-loading="loading"
        :data="productList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            {{ row.price }} 赛点
          </template>
        </el-table-column>
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.image"
              :preview-src-list="[row.image]"
              fit="cover"
              class="product-image"
            />
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            {{ getProductTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === ProductStatus.ON_SHELF ? 'success' : 'info'">
              {{ row.status === ProductStatus.ON_SHELF ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleView(row)">查看</el-button>
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="text"
              class="delete-btn"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="商品详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentProduct?.id }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ currentProduct?.name }}</el-descriptions-item>
        <el-descriptions-item label="价格">{{ currentProduct?.price }} 赛点</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getProductTypeLabel(currentProduct?.type) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentProduct?.status === ProductStatus.ON_SHELF ? 'success' : 'info'">
            {{ currentProduct?.status === ProductStatus.ON_SHELF ? '上架' : '下架' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentProduct?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentProduct?.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="商品图片" :span="2">
          <el-image
            :src="currentProduct?.image"
            :preview-src-list="currentProduct?.image ? [currentProduct.image] : []"
            fit="cover"
            style="width: 200px; height: 200px"
          />
        </el-descriptions-item>
        <el-descriptions-item label="商品描述" :span="2">
          <div class="description-wrapper">{{ currentProduct?.description }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="800px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number
            v-model="editForm.price"
            :min="1"
            :max="99999"
            :precision="0"
            :step="1"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="商品类型" prop="type">
          <el-select v-model="editForm.type" placeholder="请选择商品类型">
            <el-option
              v-for="item in PRODUCT_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择商品状态">
            <el-option
              v-for="item in PRODUCT_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="editForm.image" :src="editForm.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            rows="6"
            placeholder="请输入商品描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleConfirmEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type UploadProps } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getProductList,
  getProductDetail,
  createProduct,
  updateProduct,
  deleteProduct
} from '@/api/product'
import type {
  Product,
  CreateProductRequest,
  UpdateProductRequest,
  ProductQueryParams
} from '@/types/product'
import { ProductStatus, PRODUCT_TYPES, PRODUCT_STATUS_OPTIONS } from '@/types/product'

// 搜索表单
const searchForm = reactive<ProductQueryParams>({
  keyword: '',
  type: undefined,
  status: undefined
})

// 商品列表数据
const productList = ref<Product[]>([])
const loading = ref(false)

// 对话框控制
const viewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)

// 当前选中的商品
const currentProduct = ref<Product>()

// 编辑表单
const editForm = reactive<CreateProductRequest & { id?: number }>({
  name: '',
  price: 1,
  image: '',
  type: '',
  description: '',
  status: ProductStatus.ON_SHELF
})

// 表单校验规则
const editRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  type: [{ required: true, message: '请选择商品类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择商品状态', trigger: 'change' }],
  image: [{ required: true, message: '请上传商品图片', trigger: 'change' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }]
}

// 表单引用
const searchFormRef = ref<FormInstance>()
const editFormRef = ref<FormInstance>()

// 获取商品类型标签
const getProductTypeLabel = (type?: string) => {
  return PRODUCT_TYPES.find(item => item.value === type)?.label || type
}

// 获取商品列表
const fetchProductList = async () => {
  try {
    loading.value = true
    const res = await getProductList(searchForm)
    productList.value = res
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  fetchProductList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    type: undefined,
    status: undefined
  })
  handleSearch()
}

// 查看详情
const handleView = async (row: Product) => {
  try {
    const res = await getProductDetail(row.id)
    currentProduct.value = res
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  }
}

// 编辑商品
const handleEdit = async (row: Product) => {
  isEdit.value = true
  const res = await getProductDetail(row.id)
  Object.assign(editForm, {
    id: res.id,
    name: res.name,
    price: res.price,
    image: res.image,
    type: res.type,
    description: res.description,
    status: res.status
  })
  editDialogVisible.value = true
}

// 新增商品
const handleCreate = () => {
  isEdit.value = false
  Object.assign(editForm, {
    id: undefined,
    name: '',
    price: 1,
    image: '',
    type: '',
    description: '',
    status: ProductStatus.ON_SHELF
  })
  editDialogVisible.value = true
}

// 确认编辑
const handleConfirmEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateProduct(editForm as UpdateProductRequest)
      ElMessage.success('更新成功')
    } else {
      await createProduct(editForm)
      ElMessage.success('创建成功')
    }
    
    editDialogVisible.value = false
    handleSearch()
  } catch (error) {
    console.error('保存商品失败:', error)
    ElMessage.error('保存商品失败')
  } finally {
    submitting.value = false
  }
}

// 删除商品
const handleDelete = (row: Product) => {
  ElMessageBox.confirm(
    '确定要删除该商品吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteProduct(row.id)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (error) {
      console.error('删除商品失败:', error)
      ElMessage.error('删除商品失败')
    }
  })
}

// 图片上传相关
const handleUploadSuccess: UploadProps['onSuccess'] = (
  response,
  uploadFile
) => {
  editForm.image = response.data
}

const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = /^image\//.test(file.type)
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('上传图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 初始化
onMounted(() => {
  fetchProductList()
})
</script>

<style scoped>
.product-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.description-wrapper {
  white-space: pre-wrap;
  word-break: break-all;
}

.delete-btn {
  color: #F56C6C;
}

.delete-btn:hover {
  color: #F78989;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);

    &:hover {
      border-color: var(--el-color-primary);
    }
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style> 