<template>
  <div class="resource-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" ref="searchFormRef" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入标题或内容关键词" clearable />
        </el-form-item>
        <el-form-item label="作者ID">
          <el-input v-model="searchForm.creatorId" placeholder="请输入作者ID" clearable />
        </el-form-item>
        <el-form-item label="资源类型">
          <el-select v-model="searchForm.type" placeholder="请选择资源类型" clearable>
            <el-option v-for="type in resourceTypes" :key="type" :label="type" :value="type" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 资源列表 -->
    <el-card class="list-card">
      <div class="table-header">
        <h3>资源列表</h3>
        <el-button type="primary" @click="handleCreate">新增资源</el-button>
      </div>
      <el-table
        v-loading="loading"
        :data="resourceList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            {{ getResourceTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="creator.nickname" label="作者" width="120" />
        <el-table-column prop="views" label="浏览量" width="100" />
        <el-table-column prop="favorites" label="收藏数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="handleView(row)">查看</el-button>
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" class="delete-btn" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-button
          type="primary"
          :disabled="!hasMore"
          @click="loadMore"
          :loading="loading"
        >
          加载更多
        </el-button>
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="资源详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentResource?.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentResource?.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentResource?.type }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ currentResource?.creator?.nickname }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ currentResource?.views }}</el-descriptions-item>
        <el-descriptions-item label="收藏量">{{ currentResource?.favorites }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentResource?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentResource?.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="内容" :span="2">
          <div class="content-wrapper">{{ currentResource?.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑资源' : '新增资源'"
      width="800px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="editForm.type" placeholder="请选择资源类型">
            <el-option
              v-for="item in resourceTypes"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            rows="6"
            placeholder="请输入内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleConfirmEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  getResourceList,
  getResourceDetail,
  createResource,
  updateResource,
  deleteResource
} from '@/api/resource'
import type {
  ResourceInfo,
  ResourceDetail,
  ResourceType,
  CreateResourceRequest,
  UpdateResourceRequest,
  ResourceQueryRequest
} from '@/types/resource'

// 资源类型列表
const resourceTypes: ResourceType[] = ['ARTICLE', 'VIDEO', 'AUDIO', 'DOCUMENT']

// 搜索表单
const searchForm = reactive<ResourceQueryRequest>({
  keyword: '',
  creatorId: undefined,
  type: undefined
})

// 资源列表数据
const resourceList = ref<ResourceInfo[]>([])
const loading = ref(false)
const hasMore = ref(false)
const nextCursor = ref<string>()

// 对话框控制
const viewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEdit = ref(false)

// 当前选中的资源
const currentResource = ref<ResourceDetail>()

// 编辑表单
const editForm = reactive<CreateResourceRequest & { id?: number }>({
  title: '',
  content: '',
  type: 'ARTICLE'
})

// 表单验证规则
const editRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择资源类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 表单引用
const searchFormRef = ref<FormInstance>()
const editFormRef = ref<FormInstance>()

// 获取资源类型标签
const getResourceTypeLabel = (type: ResourceType) => {
  return resourceTypes.find(item => item === type)?.toUpperCase() || type
}

// 获取资源列表
const fetchResourceList = async (isLoadMore = false) => {
  if (loading.value) return
  
  try {
    loading.value = true
    const params: ResourceQueryRequest = { ...searchForm }
    if (isLoadMore && nextCursor.value) {
      params.cursor = nextCursor.value
    }
    const res = await getResourceList(params)
    if (isLoadMore) {
      resourceList.value.push(...res.records)
    } else {
      resourceList.value = res.records
    }
    nextCursor.value = res.nextCursor
    hasMore.value = res.hasMore
  } catch (error) {
    console.error('获取资源列表失败:', error)
    ElMessage.error('获取资源列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  nextCursor.value = undefined
  fetchResourceList()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    keyword: '',
    creatorId: undefined,
    type: undefined
  })
  handleSearch()
}

// 加载更多
const loadMore = () => {
  if (!hasMore.value || loading.value) return
  fetchResourceList(true)
}

// 查看详情
const handleView = async (row: ResourceInfo) => {
  try {
    const res = await getResourceDetail(row.id)
    currentResource.value = res
    viewDialogVisible.value = true
  } catch (error) {
    console.error('获取资源详情失败:', error)
    ElMessage.error('获取资源详情失败')
  }
}

// 编辑资源
const handleEdit = (row?: ResourceInfo) => {
  if (row) {
    const res = await getResourceDetail(row.id)
    Object.assign(editForm, {
      id: res.id,
      title: res.title,
      content: res.content,
      type: res.type
    })
  } else {
    Object.assign(editForm, {
      id: undefined,
      title: '',
      content: '',
      type: 'ARTICLE'
    })
  }
  editDialogVisible.value = true
}

// 新增资源
const handleCreate = () => {
  isEdit.value = false
  editForm.id = undefined
  editForm.title = ''
  editForm.content = ''
  editForm.type = 'ARTICLE'
  editDialogVisible.value = true
}

// 确认编辑
const handleConfirmEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    
    if (isEdit.value) {
      await updateResource(editForm as UpdateResourceRequest)
      ElMessage.success('更新成功')
    } else {
      await createResource(editForm)
      ElMessage.success('创建成功')
    }
    
    editDialogVisible.value = false
    handleSearch()
  } catch (error) {
    console.error('保存资源失败:', error)
    ElMessage.error('保存资源失败')
  }
}

// 删除资源
const handleDelete = (row: ResourceInfo) => {
  ElMessageBox.confirm(
    '确定要删除该资源吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteResource(row.id)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (error) {
      console.error('删除资源失败:', error)
      ElMessage.error('删除资源失败')
    }
  })
}

// 初始化
onMounted(() => {
  fetchResourceList()
})
</script>

<style scoped>
.resource-list {
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

.pagination {
  margin-top: 20px;
  text-align: center;
}

.content-wrapper {
  white-space: pre-wrap;
  word-break: break-all;
}

.delete-btn {
  color: #F56C6C;
}

.delete-btn:hover {
  color: #F78989;
}
</style> 