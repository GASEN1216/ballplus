<template>
  <div class="product-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <div style="margin-left: auto; display: flex; align-items: center; gap: 10px;">
            <!-- 搜索和过滤 -->
            <el-input
              v-model="searchKeyword"
              placeholder="输入商品名称或描述搜索"
              clearable
              style="width: 240px;"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />

            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
          <el-button type="primary" @click="handleCreate">创建商品</el-button>
        </div>
      </template>
      <el-table :data="productList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="100">
           <template #default="{ row }">
            <div class="product-image-container">
              <!-- 使用 img 标签显示图片, 绑定到 row.image -->
              <img :src="row.image" :alt="row.name" class="product-image" v-if="row.image" />
              <!-- v-if 也检查 row.image -->
              <span v-else>无图</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handleEdit(row)">编辑</el-button>
             <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
       <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

     <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form :model="productForm" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="productForm.description" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="productForm.price" :precision="2" :step="0.1" :min="0" placeholder="请输入价格" />
        </el-form-item>
        <!-- 更新图片表单项 -->
        <el-form-item label="图片" prop="image">
          <el-input v-model="productForm.image" placeholder="请输入商品图片URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">提交</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import {
  ElMessage, ElMessageBox, ElPagination, ElTable, ElTableColumn,
  ElCard, ElButton, ElDialog, ElForm, ElFormItem,
  ElInput, ElInputNumber,
  ElSelect, ElOption,
  type FormInstance, type FormRules
} from 'element-plus'
import {
  getAllProductsAdmin,
  deleteProduct,
  createProduct,
  updateProduct,
} from '@/api/product'
import { ProductStatus } from '@/types/product'
import type { Product, ProductQueryParams, Page, CreateProductRequest, UpdateProductRequest } from '@/types/product'
// 不再需要导入 AxiosResponse 或 BackendResponse

const loading = ref(false)
const productList = ref<Product[]>([])
const searchKeyword = ref('')
// 修复 filterStatus 类型
const filterStatus = ref<ProductStatus | undefined>(undefined);

const pagination = reactive({
  page: 1, // 页码对应 MybatisPlus 的 current
  size: 10, // 每页数量对应 MybatisPlus 的 size
  total: 0
})

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('创建商品')
const isEditMode = ref(false)
const formRef = ref<FormInstance | null>(null)
// 更新 productForm 定义，使用 image
const productForm = reactive<Partial<Omit<Product, 'stock' | 'status' | 'createTime' | 'updateTime'>>>({
  id: undefined,
  name: '',
  description: '',
  price: 0,
  image: '' // 使用 image
})

// 更新 formRules，使用 image
const formRules = reactive<FormRules>({
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }, { type: 'number', message: '价格必须是数字'}],
  image: [{ required: false, message: '请输入有效的图片URL', trigger: 'blur', type: 'url' }], // 规则针对 image
})

// 加载商品列表 - 假设 request 工具返回 Page<Product>
const loadProducts = async () => {
  loading.value = true
  try {
    const params: ProductQueryParams = {
      page: pagination.page, // 传递 page
      size: pagination.size, // 传递 size
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value ?? undefined,
    }
    // 假设 getAllProductsAdmin 成功时直接返回 Page<Product> 对象
    // 如果 request 工具处理失败 (如 code != 0)，它应该抛出错误
    const res: Page<Product> = await getAllProductsAdmin(params);

    // 直接访问 records 和 total
    if (res && res.records) {
        productList.value = res.records
        // MybatisPlus 返回的是 total
        pagination.total = res.total || 0
        // 更新当前页码以防万一（虽然通常是输入参数）
        pagination.page = res.current || pagination.page; 
    } else {
        // 可能 API 成功返回但数据为空或结构错误
        console.error('获取商品列表成功，但数据结构无效:', res)
        ElMessage.error('获取商品数据格式错误')
        productList.value = []
        pagination.total = 0
    }
  } catch (error: any) { // 捕获 request 工具抛出的错误
    console.error('获取商品列表失败:', error)
    // 尝试从 error 中获取 message (如果 request 工具设置了)
    ElMessage.error(error?.message || '获取商品列表失败')
    productList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}


// 处理搜索和过滤
const handleSearch = () => {
  pagination.page = 1 // 重置为第一页
  loadProducts()
}

// 分页 - handleSizeChange 应该重置页码
const handleSizeChange = (val: number) => {
  pagination.size = val
  pagination.page = 1 // 切换 size 时重置到第一页
  loadProducts()
}
// 分页 - handleCurrentChange 更新页码
const handleCurrentChange = (val: number) => {
  pagination.page = val // 更新当前页码
  loadProducts()
}

// 重置表单 - 更新为 image
const resetForm = () => {
  productForm.id = undefined;
  productForm.name = '';
  productForm.description = '';
  productForm.price = 0;
  productForm.image = ''; // 重置 image
  formRef.value?.resetFields();
}

// 处理创建
const handleCreate = () => {
  isEditMode.value = false
  dialogTitle.value = '创建商品'
  resetForm()
  dialogVisible.value = true
}

// 处理编辑 - 更新为 image
const handleEdit = (row: Product) => {
  isEditMode.value = true
  dialogTitle.value = '编辑商品'
  nextTick(() => {
    // 只拷贝表单中存在的字段
    const { id, name, description, price, image } = row; // 使用 image
    Object.assign(productForm, { id, name, description, price, image }); // 使用 image
  });
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (row: Product) => {
   try {
    await ElMessageBox.confirm(`确定要删除商品 "${row.name}" (ID: ${row.id})吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // 假设 deleteProduct 成功时返回 boolean 或 request 工具处理错误
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    // 如果删除的是当前页最后一条数据，且不是第一页，则加载前一页
    if (productList.value.length === 1 && pagination.page > 1) {
        pagination.page--;
    }
    loadProducts() // 重新加载数据
  } catch (error: any) {
    if (error !== 'cancel') { // 排除 ElMessageBox 的取消操作
      console.error('删除商品失败:', error)
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

// 提交表单 (创建/编辑) - 更新为 image
const submitForm = async () => {
  if (!formRef.value) return;

  try {
      await formRef.value.validate();
  } catch (validationError) {
       console.error('表单校验失败:', validationError);
       return;
  }

  loading.value = true;
  try {
      // 准备提交的数据 (显式构造，确保类型安全)
      const dataToSubmit: Partial<Product> = {
          id: productForm.id,
          name: productForm.name!,
          description: productForm.description,
          price: productForm.price!,
          image: productForm.image, // 使用 image
      };

      if (isEditMode.value) {
          if (dataToSubmit.id === undefined) {
              ElMessage.error('商品ID丢失，无法更新');
              throw new Error('商品ID丢失');
          }
          // 假设 updateProduct 成功返回 boolean
          await updateProduct(dataToSubmit as UpdateProductRequest);
          ElMessage.success('更新成功');
      } else {
          delete dataToSubmit.id;
          // 假设 createProduct 成功返回 boolean
          await createProduct(dataToSubmit as CreateProductRequest);
          ElMessage.success('创建成功');
      }
      dialogVisible.value = false;
      loadProducts(); // 成功后刷新列表

  } catch(apiError: any) {
      console.error('提交商品表单失败:', apiError);
      ElMessage.error(apiError?.message || (isEditMode.value ? '更新失败，请重试' : '创建失败，请重试'));
  } finally {
      loading.value = false;
  }
}


onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
/* 新增图片样式 */
.product-image-container {
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border: 1px solid #eee;
  background-color: #f8f8f8;
}
.product-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}
</style>