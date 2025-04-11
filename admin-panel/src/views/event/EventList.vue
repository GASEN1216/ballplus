<template>
  <div class="event-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="活动名称/描述" clearable />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="searchForm.type" placeholder="请选择活动类型" clearable>
            <el-option
              v-for="item in EVENT_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动状态">
          <el-select v-model="searchForm.status" placeholder="请选择活动状态" clearable>
            <el-option
              v-for="item in EVENT_STATUS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 活动列表 -->
    <el-card class="list-card">
      <div class="table-header">
        <el-button type="primary" @click="handleCreate">创建活动</el-button>
        <el-button type="success" @click="handleCreateTemplate">创建模板</el-button>
      </div>
      <el-table :data="eventList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="type" label="活动类型" width="120">
          <template #default="{ row }">
            {{ getEventTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="活动状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventDate" label="活动日期" width="120" />
        <el-table-column prop="eventTime" label="活动时间" width="120" />
        <el-table-column prop="location" label="活动地点" min-width="150" />
        <el-table-column prop="maxParticipants" label="最大参与人数" width="120" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="success" link @click="handleViewParticipants(row)">参与者</el-button>
            <el-button type="warning" link @click="handleViewMatches(row)">匹配</el-button>
            <el-button type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="load-more">
        <el-button
          type="primary"
          :loading="loadingMore"
          :disabled="!hasMore"
          @click="loadMore"
        >
          加载更多
        </el-button>
      </div>
    </el-card>

    <!-- 活动详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="活动详情"
      width="60%"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentEvent.id }}</el-descriptions-item>
        <el-descriptions-item label="活动名称">{{ currentEvent.name }}</el-descriptions-item>
        <el-descriptions-item label="活动类型">{{ getEventTypeLabel(currentEvent.type) }}</el-descriptions-item>
        <el-descriptions-item label="活动状态">
          <el-tag :type="getStatusType(currentEvent.status)">
            {{ getStatusLabel(currentEvent.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="活动日期">{{ currentEvent.eventDate }}</el-descriptions-item>
        <el-descriptions-item label="活动时间">{{ currentEvent.eventTime }}</el-descriptions-item>
        <el-descriptions-item label="活动地点">{{ currentEvent.location }}</el-descriptions-item>
        <el-descriptions-item label="最大参与人数">{{ currentEvent.maxParticipants }}</el-descriptions-item>
        <el-descriptions-item label="活动描述" :span="2">{{ currentEvent.description }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 创建活动对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="isEdit ? '编辑活动' : '创建活动'"
      width="50%"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
      >
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="createForm.type" placeholder="请选择活动类型">
            <el-option
              v-for="item in EVENT_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动日期" prop="eventDate">
          <el-date-picker
            v-model="createForm.eventDate"
            type="date"
            placeholder="请选择活动日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="活动时间" prop="eventTime">
          <el-time-picker
            v-model="createForm.eventTime"
            placeholder="请选择活动时间"
            value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="createForm.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="最大参与人数" prop="maxParticipants">
          <el-input-number
            v-model="createForm.maxParticipants"
            :min="1"
            :max="1000"
          />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入活动描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateConfirm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 创建模板对话框 -->
    <el-dialog
      v-model="templateDialogVisible"
      title="创建活动模板"
      width="50%"
    >
      <el-form
        ref="templateFormRef"
        :model="templateForm"
        :rules="templateRules"
        label-width="100px"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="templateForm.type" placeholder="请选择活动类型">
            <el-option
              v-for="item in EVENT_TYPES"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="templateForm.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="最大参与人数" prop="maxParticipants">
          <el-input-number
            v-model="templateForm.maxParticipants"
            :min="1"
            :max="1000"
          />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入活动描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTemplateConfirm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 参与者列表对话框 -->
    <el-dialog
      v-model="participantsDialogVisible"
      title="活动参与者"
      width="50%"
    >
      <el-table :data="participants" v-loading="participantsLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column label="头像" width="100">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatarUrl" />
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 活动匹配对话框 -->
    <el-dialog
      v-model="matchesDialogVisible"
      title="活动匹配"
      width="60%"
    >
      <el-table :data="matches" v-loading="matchesLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="type" label="活动类型" width="120">
          <template #default="{ row }">
            {{ getEventTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="eventDate" label="活动日期" width="120" />
        <el-table-column prop="eventTime" label="活动时间" width="120" />
        <el-table-column prop="location" label="活动地点" min-width="150" />
      </el-table>
    </el-dialog>

    <!-- 取消活动对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消活动"
      width="40%"
    >
      <el-form
        ref="cancelFormRef"
        :model="cancelForm"
        :rules="cancelRules"
        label-width="80px"
      >
        <el-form-item label="取消原因" prop="reason">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleCancelConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import {
  getEventListWithCursor,
  getEventDetail,
  createEvent,
  cancelEvent,
  getEventParticipants,
  getEventMatches,
  createEventTemplate
} from '@/api/event'
import type {
  Event,
  EventDetail,
  EventInfo,
  CreateEventRequest,
  CursorPageRequest,
  CursorPageResponse,
  EventQueryParams
} from '@/types/event'
import { EVENT_TYPES, EVENT_STATUS } from '@/types/event'

// 状态变量
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const eventList = ref<EventInfo[]>([])
const currentEvent = ref<EventDetail>({} as EventDetail)
const participants = ref<{ id: number; username: string; avatarUrl: string }[]>([])
const matches = ref<EventInfo[]>([])
const participantsLoading = ref(false)
const matchesLoading = ref(false)

// 对话框可见性
const detailDialogVisible = ref(false)
const createDialogVisible = ref(false)
const templateDialogVisible = ref(false)
const participantsDialogVisible = ref(false)
const matchesDialogVisible = ref(false)
const cancelDialogVisible = ref(false)

// 表单引用
const createFormRef = ref<FormInstance>()
const templateFormRef = ref<FormInstance>()
const cancelFormRef = ref<FormInstance>()

// 搜索表单
const searchForm = reactive<EventQueryParams>({
  keyword: '',
  type: undefined,
  status: undefined
})

// 创建表单
const createForm = reactive<CreateEventRequest>({
  name: '',
  type: '',
  eventDate: '',
  eventTime: '',
  location: '',
  maxParticipants: 10,
  description: ''
})

// 模板表单
const templateForm = reactive({
  name: '',
  type: '',
  location: '',
  maxParticipants: 10,
  description: ''
})

// 取消表单
const cancelForm = reactive({
  reason: ''
})

// 表单验证规则
const createRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  eventDate: [{ required: true, message: '请选择活动日期', trigger: 'change' }],
  eventTime: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  maxParticipants: [{ required: true, message: '请输入最大参与人数', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }]
}

const templateRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  maxParticipants: [{ required: true, message: '请输入最大参与人数', trigger: 'blur' }],
  description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }]
}

const cancelRules = {
  reason: [{ required: true, message: '请输入取消原因', trigger: 'blur' }]
}

// 获取活动类型标签
const getEventTypeLabel = (type: string) => {
  const item = EVENT_TYPES.find(item => item.value === type)
  return item ? item.label : type
}

// 获取活动状态标签
const getStatusLabel = (status: string) => {
  const item = EVENT_STATUS.find(item => item.value === status)
  return item ? item.label : status
}

// 获取活动状态类型
const getStatusType = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'ONGOING':
      return 'success'
    case 'COMPLETED':
      return 'info'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取活动列表
const fetchEventList = async (reset = false) => {
  if (reset) {
    eventList.value = []
    hasMore.value = true
  }
  if (!hasMore.value) return

  try {
    loading.value = true
    const params: CursorPageRequest & EventQueryParams = {
      ...searchForm,
      cursor: reset ? undefined : eventList.value[eventList.value.length - 1]?.id,
      pageSize: 10
    }
    const res = await getEventListWithCursor(params)
    if (reset) {
      eventList.value = res.data.list
    } else {
      eventList.value.push(...res.data.list)
    }
    hasMore.value = res.data.hasNext
  } catch (error) {
    console.error('获取活动列表失败:', error)
    ElMessage.error('获取活动列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  fetchEventList(true)
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.type = undefined
  searchForm.status = undefined
  fetchEventList(true)
}

// 加载更多
const loadMore = () => {
  fetchEventList()
}

// 查看详情
const handleView = async (row: EventInfo) => {
  try {
    const res = await getEventDetail(row.id)
    currentEvent.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  }
}

// 查看参与者
const handleViewParticipants = async (row: EventInfo) => {
  try {
    participantsLoading.value = true
    const res = await getEventParticipants(row.id)
    participants.value = res.data
    participantsDialogVisible.value = true
  } catch (error) {
    console.error('获取参与者列表失败:', error)
    ElMessage.error('获取参与者列表失败')
  } finally {
    participantsLoading.value = false
  }
}

// 查看匹配
const handleViewMatches = async (row: EventInfo) => {
  try {
    matchesLoading.value = true
    const res = await getEventMatches(row.id)
    matches.value = res.data
    matchesDialogVisible.value = true
  } catch (error) {
    console.error('获取活动匹配失败:', error)
    ElMessage.error('获取活动匹配失败')
  } finally {
    matchesLoading.value = false
  }
}

// 创建活动
const handleCreate = () => {
  createForm.name = ''
  createForm.type = ''
  createForm.eventDate = ''
  createForm.eventTime = ''
  createForm.location = ''
  createForm.maxParticipants = 10
  createForm.description = ''
  createDialogVisible.value = true
}

// 创建模板
const handleCreateTemplate = () => {
  templateForm.name = ''
  templateForm.type = ''
  templateForm.location = ''
  templateForm.maxParticipants = 10
  templateForm.description = ''
  templateDialogVisible.value = true
}

// 确认创建活动
const handleCreateConfirm = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await createEvent(createForm)
        ElMessage.success('创建活动成功')
        createDialogVisible.value = false
        fetchEventList(true)
      } catch (error) {
        console.error('创建活动失败:', error)
        ElMessage.error('创建活动失败')
      }
    }
  })
}

// 确认创建模板
const handleTemplateConfirm = async () => {
  if (!templateFormRef.value) return
  await templateFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await createEventTemplate(templateForm)
        ElMessage.success('创建模板成功')
        templateDialogVisible.value = false
      } catch (error) {
        console.error('创建模板失败:', error)
        ElMessage.error('创建模板失败')
      }
    }
  })
}

// 取消活动
const handleCancel = (row: EventInfo) => {
  currentEvent.value = row as EventDetail
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

// 确认取消活动
const handleCancelConfirm = async () => {
  if (!cancelFormRef.value) return
  await cancelFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await cancelEvent(currentEvent.value.id, cancelForm.reason)
        ElMessage.success('取消活动成功')
        cancelDialogVisible.value = false
        fetchEventList(true)
      } catch (error) {
        console.error('取消活动失败:', error)
        ElMessage.error('取消活动失败')
      }
    }
  })
}

// 初始化
onMounted(() => {
  fetchEventList(true)
})
</script>

<style scoped>
.event-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.table-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.load-more {
  margin-top: 20px;
  text-align: center;
}
</style> 