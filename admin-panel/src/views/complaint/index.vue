<template>
  <div class="complaint-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>投诉管理</span>
          <div style="margin-left: auto; display: flex; gap: 10px;">
            <el-select
              v-model="filterStatus"
              placeholder="按状态过滤"
              clearable
              @change="handleFilterChange"
              style="width: 150px;"
            >
              <el-option label="待处理" :value="ComplaintStatus.PENDING" />
              <el-option label="已通过" :value="ComplaintStatus.APPROVED" />
              <el-option label="已拒绝" :value="ComplaintStatus.REJECTED" />
            </el-select>
          </div>
        </div>
      </template>
      <el-table :data="complaintList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="投诉人" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-avatar :size="30" :src="row.complainerAvatar" style="margin-right: 5px;" />
              <span>{{ row.complainerName }} (ID: {{ row.complainerId }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="被投诉人" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-avatar :size="30" :src="row.complainedAvatar" style="margin-right: 5px;" />
              <span>{{ row.complainedName }} (ID: {{ row.complainedId }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="投诉内容" show-overflow-tooltip />
        <el-table-column prop="eventId" label="活动ID" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === ComplaintStatus.PENDING"
              type="success"
              link
              @click="handleProcess(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 投诉详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="投诉详情" width="600px">
      <div v-if="currentComplaintDetail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="投诉ID">{{ currentComplaintDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="活动ID">{{ currentComplaintDetail.eventId }}</el-descriptions-item>
          <el-descriptions-item label="投诉人">
            <div style="display: flex; align-items: center;">
              <el-avatar :size="30" :src="currentComplaintDetail.complainerAvatar" style="margin-right: 5px;" />
              <span>{{ currentComplaintDetail.complainerName }} (ID: {{ currentComplaintDetail.complainerId }})</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="被投诉人">
             <div style="display: flex; align-items: center;">
              <el-avatar :size="30" :src="currentComplaintDetail.complainedAvatar" style="margin-right: 5px;" />
              <span>{{ currentComplaintDetail.complainedName }} (ID: {{ currentComplaintDetail.complainedId }})</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="投诉内容">
            <div style="white-space: pre-wrap;">{{ currentComplaintDetail.content }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentComplaintDetail.status)">
              {{ getStatusText(currentComplaintDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentComplaintDetail.status === ComplaintStatus.REJECTED" label="拒绝原因">
            {{ currentComplaintDetail.rejectReason }}
          </el-descriptions-item>
          <el-descriptions-item label="投诉时间">{{ currentComplaintDetail.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox, ElPagination, ElTable, ElTableColumn, ElCard, ElTag, ElButton, ElAvatar, ElSelect, ElOption, ElInput, ElDialog, ElDescriptions, ElDescriptionsItem, ElIcon } from 'element-plus'
import { getAllComplaintsAdmin, handleComplaintAdmin } from '@/api/complaint'
import type { Page } from '@/api/comment'
import { ComplaintStatus } from '@/types/complaint'
import type { ComplaintVO, HandleComplaintRequest } from '@/types/complaint'

const loading = ref(false)
const complaintList = ref<ComplaintVO[]>([])
const filterStatus = ref<ComplaintStatus | undefined>(undefined)

const detailDialogVisible = ref(false)
const currentComplaintDetail = ref<ComplaintVO | null>(null)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const getStatusType = (status: ComplaintStatus) => {
  switch (status) {
    case ComplaintStatus.PENDING: return 'warning'
    case ComplaintStatus.APPROVED: return 'success'
    case ComplaintStatus.REJECTED: return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: ComplaintStatus) => {
  switch (status) {
    case ComplaintStatus.PENDING: return '待处理'
    case ComplaintStatus.APPROVED: return '已通过'
    case ComplaintStatus.REJECTED: return '已拒绝'
    default: return '未知'
  }
}

const loadComplaints = async () => {
  loading.value = true
  complaintList.value = []
  pagination.total = 0
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      status: filterStatus.value
    }
    const res = await getAllComplaintsAdmin(params) as unknown as Page<ComplaintVO>
    if (res && res.records) {
      complaintList.value = res.records
      pagination.total = res.total || 0
    } else {
      console.error('获取投诉列表响应无效:', res)
      ElMessage.error('获取投诉数据格式错误')
    }
  } catch (error) {
    console.error('获取投诉列表失败:', error)
    ElMessage.error('获取投诉列表接口请求失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadComplaints()
}
const handleCurrentChange = (val: number) => {
  pagination.currentPage = val
  loadComplaints()
}
const handleFilterChange = () => {
  pagination.currentPage = 1
  loadComplaints()
}

const handleView = (row: ComplaintVO) => {
  currentComplaintDetail.value = row
  detailDialogVisible.value = true
}

const handleProcess = async (row: ComplaintVO) => {
  ElMessageBox.prompt(
    `请选择处理结果：通过 或 拒绝 (ID: ${row.id})。如果拒绝，请输入原因。`,
    '处理投诉',
    {
      confirmButtonText: '确认处理',
      cancelButtonText: '取消',
      inputPlaceholder: '如果拒绝，请在此输入原因',
    }
  ).then(async ({ value }) => {
    const isReject = value && value.trim().length > 0
    const status = isReject ? ComplaintStatus.REJECTED : ComplaintStatus.APPROVED
    const requestData: HandleComplaintRequest = {
      complaintId: row.id,
      status: status,
      rejectReason: isReject ? value.trim() : undefined
    }

    try {
      loading.value = true
      await handleComplaintAdmin(requestData)
      ElMessage.success(`投诉已处理为：${getStatusText(status)}`)
      loadComplaints()
    } catch (error) {
      console.error('处理投诉失败:', error)
      ElMessage.error('处理投诉失败')
    } finally {
      loading.value = false
    }
  }).catch(() => {
    ElMessage.info('已取消处理')
  })
}

onMounted(() => {
  loadComplaints()
})
</script>

<style scoped>
.complaint-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 