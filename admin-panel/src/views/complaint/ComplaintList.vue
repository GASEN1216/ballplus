<template>
  <div class="complaint-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="内容">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入投诉内容关键词"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="投诉人">
          <el-input
            v-model="searchForm.complainantName"
            placeholder="请输入投诉人用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="被投诉人">
          <el-input
            v-model="searchForm.respondentName"
            placeholder="请输入被投诉人用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择投诉类型"
            clearable
          >
            <el-option
              v-for="item in complaintTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择投诉状态"
            clearable
          >
            <el-option
              v-for="item in complaintStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 投诉列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="complaintList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="投诉人" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.complainantAvatar" />
              <span class="username">{{ row.complainantName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="被投诉人" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.respondentAvatar" />
              <span class="username">{{ row.respondentName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getComplaintTypeTag(row.type)">
              {{ getComplaintTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getComplaintStatusTag(row.status)">
              {{ getComplaintStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="处理结果" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === ComplaintStatus.PENDING"
              type="warning"
              link
              @click="handleComplaint(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 加载更多 -->
      <div class="load-more">
        <el-button
          v-if="hasMore"
          :loading="loading"
          type="primary"
          link
          @click="loadMore"
        >
          加载更多
        </el-button>
        <span v-else class="no-more">没有更多数据了</span>
      </div>
    </el-card>

    <!-- 投诉详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="投诉详情"
      width="800px"
    >
      <div v-if="currentComplaint" class="complaint-detail">
        <div class="complaint-content">
          {{ currentComplaint.content }}
        </div>
        <div class="complaint-meta">
          <div class="user-info">
            <div class="user-item">
              <span class="label">投诉人：</span>
              <el-avatar :size="40" :src="currentComplaint.complainant.avatarUrl" />
              <span class="username">{{ currentComplaint.complainant.username }}</span>
            </div>
            <div class="user-item">
              <span class="label">被投诉人：</span>
              <el-avatar :size="40" :src="currentComplaint.respondent.avatarUrl" />
              <span class="username">{{ currentComplaint.respondent.username }}</span>
            </div>
          </div>
          <div class="complaint-info">
            <div class="info-item">
              <span class="label">投诉类型：</span>
              <el-tag :type="getComplaintTypeTag(currentComplaint.type)">
                {{ getComplaintTypeText(currentComplaint.type) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">投诉状态：</span>
              <el-tag :type="getComplaintStatusTag(currentComplaint.status)">
                {{ getComplaintStatusText(currentComplaint.status) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">处理结果：</span>
              <span>{{ currentComplaint.result || '暂无' }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span>{{ currentComplaint.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">更新时间：</span>
              <span>{{ currentComplaint.updateTime }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 处理投诉对话框 -->
    <el-dialog
      v-model="handleDialogVisible"
      title="处理投诉"
      width="600px"
    >
      <el-form
        v-if="handleForm"
        ref="handleFormRef"
        :model="handleForm"
        :rules="handleRules"
        label-width="80px"
      >
        <el-form-item label="处理结果" prop="status">
          <el-select v-model="handleForm.status" placeholder="请选择处理结果">
            <el-option
              v-for="item in complaintStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理说明" prop="result">
          <el-input
            v-model="handleForm.result"
            type="textarea"
            :rows="6"
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmHandle">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { ComplaintInfo, ComplaintDetail, HandleComplaintRequest } from '@/types/complaint'
import { ComplaintType, ComplaintStatus } from '@/types/complaint'
import {
  getComplaintListWithCursor,
  getComplaintDetail,
  handleComplaint
} from '@/api/complaint'

// 投诉类型选项
const complaintTypeOptions = [
  { value: ComplaintType.POST, label: '帖子' },
  { value: ComplaintType.COMMENT, label: '评论' },
  { value: ComplaintType.SUB_COMMENT, label: '子评论' },
  { value: ComplaintType.USER, label: '用户' }
]

// 投诉状态选项
const complaintStatusOptions = [
  { value: ComplaintStatus.PENDING, label: '待处理' },
  { value: ComplaintStatus.PROCESSING, label: '处理中' },
  { value: ComplaintStatus.RESOLVED, label: '已解决' },
  { value: ComplaintStatus.REJECTED, label: '已驳回' }
]

// 搜索表单
const searchForm = reactive({
  keyword: '',
  complainantName: '',
  respondentName: '',
  type: undefined as number | undefined,
  status: undefined as number | undefined
})

// 数据列表
const loading = ref(false)
const complaintList = ref<ComplaintInfo[]>([])
const nextCursor = ref<string>()
const hasMore = ref(true)

// 对话框
const detailDialogVisible = ref(false)
const handleDialogVisible = ref(false)
const currentComplaint = ref<ComplaintDetail>()
const handleFormRef = ref<FormInstance>()
const handleForm = ref<HandleComplaintRequest>()

// 表单验证规则
const handleRules = {
  status: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  result: [{ required: true, message: '请输入处理说明', trigger: 'blur' }]
}

// 获取投诉类型标签
const getComplaintTypeTag = (type: number) => {
  switch (type) {
    case ComplaintType.POST:
      return 'success'
    case ComplaintType.COMMENT:
      return 'warning'
    case ComplaintType.SUB_COMMENT:
      return 'info'
    case ComplaintType.USER:
      return 'danger'
    default:
      return ''
  }
}

// 获取投诉类型文本
const getComplaintTypeText = (type: number) => {
  const option = complaintTypeOptions.find(item => item.value === type)
  return option ? option.label : '未知'
}

// 获取投诉状态标签
const getComplaintStatusTag = (status: number) => {
  switch (status) {
    case ComplaintStatus.PENDING:
      return 'info'
    case ComplaintStatus.PROCESSING:
      return 'warning'
    case ComplaintStatus.RESOLVED:
      return 'success'
    case ComplaintStatus.REJECTED:
      return 'danger'
    default:
      return ''
  }
}

// 获取投诉状态文本
const getComplaintStatusText = (status: number) => {
  const option = complaintStatusOptions.find(item => item.value === status)
  return option ? option.label : '未知'
}

// 获取投诉列表
const fetchComplaintList = async (isLoadMore = false) => {
  if (!isLoadMore) {
    loading.value = true
    complaintList.value = []
    nextCursor.value = undefined
  }

  try {
    const { data } = await getComplaintListWithCursor({
      cursor: nextCursor.value,
      pageSize: 10,
      ...searchForm
    })

    if (isLoadMore) {
      complaintList.value.push(...data.records)
    } else {
      complaintList.value = data.records
    }

    nextCursor.value = data.nextCursor
    hasMore.value = data.hasMore
  } catch (error) {
    ElMessage.error('获取投诉列表失败')
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  fetchComplaintList(true)
}

// 搜索
const handleSearch = () => {
  fetchComplaintList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.complainantName = ''
  searchForm.respondentName = ''
  searchForm.type = undefined
  searchForm.status = undefined
  handleSearch()
}

// 查看详情
const handleView = async (complaint: ComplaintInfo) => {
  try {
    const { data } = await getComplaintDetail(complaint.id)
    currentComplaint.value = data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取投诉详情失败')
  }
}

// 处理投诉
const handleComplaint = (complaint: ComplaintInfo) => {
  handleForm.value = {
    id: complaint.id,
    status: ComplaintStatus.PROCESSING,
    result: ''
  }
  handleDialogVisible.value = true
}

// 确认处理
const confirmHandle = async () => {
  if (!handleFormRef.value) return

  await handleFormRef.value.validate(async (valid) => {
    if (valid && handleForm.value) {
      try {
        await handleComplaint(handleForm.value)
        ElMessage.success('处理成功')
        handleDialogVisible.value = false
        fetchComplaintList()
      } catch (error) {
        ElMessage.error('处理失败')
      }
    }
  })
}

// 初始化
onMounted(() => {
  fetchComplaintList()
})
</script>

<style scoped>
.complaint-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.list-card {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
}

.load-more {
  margin-top: 20px;
  text-align: center;
}

.no-more {
  color: #909399;
  font-size: 14px;
}

.complaint-detail {
  padding: 20px;
}

.complaint-content {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
}

.complaint-meta {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  color: #606266;
  font-size: 14px;
}

.complaint-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style> 