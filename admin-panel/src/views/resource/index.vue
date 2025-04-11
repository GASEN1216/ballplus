<template>
  <div class="resource-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>资源管理</span>
          <!-- 过滤区域 -->
          <div style="margin-left: auto; display: flex; gap: 10px; align-items: center;">
            <el-input
              v-model="filters.keyword"
              placeholder="搜索标题/描述"
              clearable
              style="width: 200px;"
              @keyup.enter="handleSearch"
            />
            <el-select
              v-model="filters.type"
              placeholder="按类型过滤"
              clearable
              style="width: 130px;"
            >
              <el-option label="文章" value="article" />
              <el-option label="视频" value="video" />
              <el-option label="音频" value="audio" />
              <el-option label="文档" value="document" />
            </el-select>
            <el-select
              v-model="filters.category"
              placeholder="按分类过滤"
              clearable
              style="width: 130px;"
            >
              <el-option label="基础" value="basic" />
              <el-option label="进阶" value="advanced" />
              <el-option label="攻略" value="strategy" />
              <el-option label="装备" value="equipment" />
              <el-option label="健身" value="fitness" />
            </el-select>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
          <el-button type="primary" @click="handleUpload" style="margin-left: 10px;">上传/创建资源</el-button>
        </div>
      </template>
      <el-table :data="resourceList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="views" label="浏览" width="80" />
        <el-table-column prop="likes" label="点赞" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDownload(row)" :disabled="!row.content">查看/下载</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
            <!-- 可以添加编辑按钮 -->
            <!-- <el-button type="warning" link @click="handleEdit(row)">编辑</el-button> -->
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

    <!-- 创建/编辑对话框 (骨架) -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <!-- TODO: 添加表单用于创建/编辑资源元数据 -->
      <p>创建/编辑表单待实现...</p>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElPagination, ElTable, ElTableColumn, ElCard, ElTag, ElButton, ElInput, ElSelect, ElOption, ElDialog } from 'element-plus'
import { getAllResourcesAdmin, deleteResourceAdmin, createResourceAdmin, updateResourceAdmin } from '@/api/resource'
import type { ResourceVO, ResourceQueryRequest, ResourcePageResponse, CreateResourceRequest, UpdateResourceRequest } from '@/types/resource'

const loading = ref(false)
const resourceList = ref<ResourceVO[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const filters = reactive<Partial<ResourceQueryRequest>>({
  keyword: '',
  type: null,
  category: null
})

// 对话框相关状态
const dialogVisible = ref(false)
const dialogTitle = ref('创建资源')
const currentResource = ref<Partial<ResourceVO & { content?: string }>>({}) // 用于编辑
const isEditMode = ref(false)

// 获取资源类型文本
const getTypeText = (type: string | undefined | null) => {
  if (!type) return '未知';
  const lowerType = type.toLowerCase();
  if (lowerType === 'article') return '文章';
  if (lowerType === 'video') return '视频';
  if (lowerType === 'audio') return '音频';
  if (lowerType === 'document') return '文档';
  return type; // 返回原始字符串作为备用
}

// 加载资源列表
const loadResources = async () => {
  loading.value = true
  resourceList.value = []
  pagination.total = 0
  try {
    const params: ResourceQueryRequest = {
      page: pagination.page,
      size: pagination.size,
      keyword: filters.keyword || undefined,
      type: filters.type !== null ? filters.type : undefined,
      category: filters.category !== null ? filters.category : undefined,
    }
    const res = await getAllResourcesAdmin(params) as unknown as ResourcePageResponse<ResourceVO>; 
    if (res && res.records) {
      resourceList.value = res.records
      pagination.total = res.total || 0
    } else {
      console.error('获取资源列表响应无效:', res)
      ElMessage.error('获取资源数据格式错误')
    }
  } catch (error) {
    console.error('获取资源列表失败:', error)
    ElMessage.error('获取资源列表接口请求失败')
  } finally {
    loading.value = false
  }
}

// 分页和搜索
const handleSizeChange = (val: number) => {
  pagination.size = val
  pagination.page = 1
  loadResources()
}
const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadResources()
}
const handleSearch = () => {
  pagination.page = 1
  loadResources()
}

// 处理上传/创建
const handleUpload = () => {
  isEditMode.value = false
  dialogTitle.value = '创建资源'
  currentResource.value = {} // 清空表单
  dialogVisible.value = true
  // ElMessage.info('上传/创建功能待实现')
}

// 处理下载/查看
const handleDownload = (row: ResourceVO) => {
  if (row.content) {
    window.open(row.content, '_blank')
  } else {
    ElMessage.warning('该资源没有可用的内容链接')
  }
}

// 处理删除
const handleDelete = async (row: ResourceVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除资源 "${row.title}" (ID: ${row.id})吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteResourceAdmin(row.id)
    ElMessage.success('删除成功')
    // 处理分页（如果删除的是最后一页的最后一条）
    if (resourceList.value.length === 1 && pagination.page > 1) {
        pagination.page--;
    }
    loadResources()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除资源失败:', error)
      ElMessage.error('删除资源失败')
    }
  }
}

// 处理编辑 (占位)
const handleEdit = (row: ResourceVO) => {
  isEditMode.value = true
  dialogTitle.value = '编辑资源'
  // currentResource.value = { ...row }; // 浅拷贝数据到表单模型
  // dialogVisible.value = true;
  ElMessage.info('编辑功能待实现')
}

// 提交表单 (创建/编辑 - 占位)
const submitForm = async () => {
  ElMessage.info('表单提交逻辑待实现')
  // try {
  //   if (isEditMode.value) {
  //     // 调用 updateResourceAdmin
  //   } else {
  //     // 调用 createResourceAdmin
  //   }
  //   dialogVisible.value = false;
  //   loadResources();
  // } catch (error) {
  //   // ...
  // }
}

onMounted(() => {
  loadResources()
})
</script>

<style scoped>
.resource-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 