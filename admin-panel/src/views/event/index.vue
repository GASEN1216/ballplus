<template>
  <div class="event-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动管理</span>
          <div class="search-container">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索活动名称/地点/电话"
              clearable
              @input="handleSearch"
              style="width: 220px;"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <el-button type="primary" @click="handleCreate">创建活动</el-button>
        </div>
      </template>
      <el-table :data="filteredEventList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="state" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.state)">
              {{ getStatusText(row.state) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventDate" label="日期" width="120" />
        <el-table-column prop="eventTime" label="时间" width="100" />
        <el-table-column label="人数" width="100">
          <template #default="{ row }">
            {{ row.participants }}/{{ row.totalParticipants }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="success" link @click="handleParticipants(row)">参与者</el-button>
            <el-button v-if="row.state === 0" type="warning" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 活动详情对话框 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="600px">
      <template v-if="currentEvent">
        <div class="event-detail">
          <h3>{{ currentEvent.name }}</h3>
          <p class="event-info"><strong>时间：</strong>{{ currentEvent.eventDate }} {{ currentEvent.eventTime }} - {{ currentEvent.eventTimee }}</p>
          <p class="event-info"><strong>地点：</strong>{{ currentEvent.location }}</p>
          <p class="event-info"><strong>详细地址：</strong>{{ currentEvent.locationDetail }}</p>
          <p class="event-info"><strong>人数：</strong>{{ currentEvent.participants }}/{{ currentEvent.totalParticipants }}</p>
          <p class="event-info"><strong>联系电话：</strong>{{ currentEvent.phoneNumber }}</p>
          <p class="event-info"><strong>状态：</strong>{{ getStatusText(currentEvent.state) }}</p>
          <p class="event-info"><strong>创建时间：</strong>{{ currentEvent.createTime }}</p>
          <div class="event-content">
            <p><strong>备注：</strong></p>
            <div class="content-box">{{ currentEvent.remarks || '无' }}</div>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 参与者对话框 -->
    <el-dialog v-model="participantsVisible" title="活动参与者" width="600px">
      <el-table :data="participants" v-loading="participantsLoading">
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatarUrl" />
          </template>
        </el-table-column>
        <el-table-column label="用户ID" prop="userId" width="120" />
      </el-table>
    </el-dialog>

    <!-- 创建活动表单 -->
    <el-dialog v-model="createVisible" title="创建活动" width="600px">
      <el-form :model="eventForm" label-width="120px">
        <el-form-item label="活动名称" required>
          <el-input v-model="eventForm.name" />
        </el-form-item>
        <el-form-item label="活动备注">
          <el-input v-model="eventForm.remarks" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="活动日期" required>
          <el-date-picker v-model="eventForm.eventDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-time-picker v-model="eventForm.eventTime" format="HH:mm" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-picker v-model="eventForm.eventTimee" format="HH:mm" placeholder="选择时间" />
        </el-form-item>
        <el-form-item label="活动地点" required>
          <el-input v-model="eventForm.location" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="eventForm.locationDetail" />
        </el-form-item>
        <el-form-item label="经度">
          <el-input v-model="eventForm.longitude" type="number" />
        </el-form-item>
        <el-form-item label="纬度">
          <el-input v-model="eventForm.latitude" type="number" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="eventForm.phoneNumber" />
        </el-form-item>
        <el-form-item label="最大人数" required>
          <el-input-number v-model="eventForm.totalParticipants" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreateEvent">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 取消活动对话框 -->
    <el-dialog v-model="cancelVisible" title="取消活动" width="500px">
      <el-form>
        <el-form-item label="取消原因" required>
          <el-input v-model="cancelReason" type="textarea" :rows="4" placeholder="请输入取消活动的原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCancelEvent">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getEventList, getEventDetail, getEventParticipants, createEvent, cancelEvent } from '@/api/event'
import type { Event, DetailEvent, UserIdAndAvatar } from '@/types/event'

const loading = ref(false)
const eventList = ref<Event[]>([])
const searchKeyword = ref('')
const detailVisible = ref(false)
const participantsVisible = ref(false)
const createVisible = ref(false)
const cancelVisible = ref(false)
const currentEvent = ref<Event | null>(null)
const participants = ref<UserIdAndAvatar[]>([])
const participantsLoading = ref(false)
const cancelReason = ref('')
const adminUserId = 1 // 管理员ID，实际应从用户存储中获取

// 创建活动表单
const eventForm = reactive({
  appId: adminUserId,
  name: '',
  remarks: '',
  eventDate: '',
  eventTime: '',
  eventTimee: '',
  location: '',
  locationDetail: '',
  latitude: 0,
  longitude: 0,
  phoneNumber: '',
  totalParticipants: 10,
  type: 0,
  limits: 0,
  visibility: true,
  level: 0,
  feeMode: 0,
  fee: 0,
  penalty: true
})

// 根据关键词过滤活动列表
const filteredEventList = computed(() => {
  if (!searchKeyword.value) {
    return eventList.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return eventList.value.filter(event => {
    // 搜索活动名称、地点、电话
    return (event.name && event.name.toLowerCase().includes(keyword)) ||
           (event.location && event.location.toLowerCase().includes(keyword)) ||
           (event.phoneNumber && event.phoneNumber.includes(keyword))
  })
})

// 处理搜索
const handleSearch = () => {
  // 搜索已在计算属性中处理，无需额外操作
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'success',
    1: 'warning',
    2: 'danger',
    3: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '正常',
    1: '已满员',
    2: '已取消',
    3: '已结束'
  }
  return texts[status] || '未知'
}

const loadEvents = async () => {
  loading.value = true
  try {
    const response = await getEventList()
    if (response && Array.isArray(response)) {
      eventList.value = response
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取活动列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  createVisible.value = true
}

const submitCreateEvent = async () => {
  try {
    // 表单验证
    if (!eventForm.name || !eventForm.eventDate || 
        !eventForm.eventTime || !eventForm.eventTimee || 
        !eventForm.location || !eventForm.totalParticipants) {
      ElMessage.warning('请填写必填项')
      return
    }

    // 格式化日期和时间
    if (typeof eventForm.eventDate !== 'string') {
      const date = new Date(eventForm.eventDate)
      eventForm.eventDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    }

    if (typeof eventForm.eventTime !== 'string') {
      const time = new Date(eventForm.eventTime)
      eventForm.eventTime = `${String(time.getHours()).padStart(2, '0')}:${String(time.getMinutes()).padStart(2, '0')}`
    }

    if (typeof eventForm.eventTimee !== 'string') {
      const time = new Date(eventForm.eventTimee)
      eventForm.eventTimee = `${String(time.getHours()).padStart(2, '0')}:${String(time.getMinutes()).padStart(2, '0')}`
    }

    const response = await createEvent(eventForm)
    if (response) {
      ElMessage.success('创建活动成功')
      createVisible.value = false
      // 重置表单
      Object.assign(eventForm, {
        appId: adminUserId,
        name: '',
        remarks: '',
        eventDate: '',
        eventTime: '',
        eventTimee: '',
        location: '',
        locationDetail: '',
        latitude: 0,
        longitude: 0,
        phoneNumber: '',
        totalParticipants: 10,
        type: 0,
        limits: 0,
        visibility: true,
        level: 0,
        feeMode: 0,
        fee: 0,
        penalty: true
      })
      // 重新加载活动列表
      loadEvents()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('创建活动失败')
  }
}

const handleView = (row: Event) => {
  currentEvent.value = row
  detailVisible.value = true
}

const handleParticipants = async (row: Event) => {
  participantsLoading.value = true
  participants.value = []
  try {
    currentEvent.value = row
    const response = await getEventParticipants(row.id)
    if (response && Array.isArray(response)) {
      participants.value = response
    }
    participantsVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error('获取参与者列表失败')
  } finally {
    participantsLoading.value = false
  }
}

const handleCancel = (row: Event) => {
  currentEvent.value = row
  cancelReason.value = ''
  cancelVisible.value = true
}

const submitCancelEvent = async () => {
  if (!cancelReason.value) {
    ElMessage.warning('请输入取消原因')
    return
  }

  if (!currentEvent.value) {
    ElMessage.error('未选择活动')
    return
  }

  try {
    const response = await cancelEvent(adminUserId, currentEvent.value.id, cancelReason.value)
    if (response) {
      ElMessage.success('取消活动成功')
      cancelVisible.value = false
      // 重新加载活动列表
      loadEvents()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('取消活动失败')
  }
}

onMounted(() => {
  loadEvents()
})
</script>

<style scoped>
.event-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.event-detail {
  padding: 10px;
}

.event-info {
  margin: 10px 0;
}

.event-content {
  margin-top: 20px;
}

.content-box {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  min-height: 100px;
  white-space: pre-wrap;
}
</style> 