<template>
  <div class="user-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="账号">
          <el-input
            v-model="searchForm.account"
            placeholder="请输入账号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="0" />
            <el-option label="封禁" :value="1" />
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

    <!-- 用户列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="account" label="账号" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="头像" width="100">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatarUrl" />
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="isAdmin" label="管理员" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isAdmin ? 'success' : 'info'">
              {{ row.isAdmin ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '正常' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button
              v-if="!row.isAdmin"
              type="primary"
              link
              @click="handleSetAdmin(row)"
            >
              设为管理员
            </el-button>
            <el-button
              v-else
              type="warning"
              link
              @click="handleCancelAdmin(row)"
            >
              取消管理员
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              link
              @click="handleBanUser(row)"
            >
              封禁
            </el-button>
            <el-button
              v-else
              type="success"
              link
              @click="handleUnbanUser(row)"
            >
              解封
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 封禁用户对话框 -->
    <el-dialog
      v-model="banDialogVisible"
      title="封禁用户"
      width="500px"
    >
      <el-form :model="banForm" label-width="100px">
        <el-form-item label="封禁时长">
          <el-input-number
            v-model="banForm.duration"
            :min="1"
            :max="365"
            controls-position="right"
          />
          <span class="ml-2">天</span>
        </el-form-item>
        <el-form-item label="封禁原因">
          <el-input
            v-model="banForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入封禁原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="banDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBanUser">
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
import type { User } from '@/types/user'
import { getUserList, setAdmin, cancelAdmin, banUser, unbanUser } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  account: '',
  username: '',
  status: undefined as undefined | number
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 数据列表
const loading = ref(false)
const userList = ref<User[]>([])

// 封禁对话框
const banDialogVisible = ref(false)
const banForm = reactive({
  userId: undefined as undefined | number,
  duration: 1,
  reason: ''
})

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const { data } = await getUserList({
      page: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    })
    userList.value = data.records
    total.value = data.total
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  searchForm.account = ''
  searchForm.username = ''
  searchForm.status = undefined
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchUserList()
}

// 页码改变
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchUserList()
}

// 设置管理员
const handleSetAdmin = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要将用户 ${user.username} 设置为管理员吗？`,
      '提示',
      {
        type: 'warning'
      }
    )
    await setAdmin(user.id)
    ElMessage.success('设置管理员成功')
    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('设置管理员失败')
    }
  }
}

// 取消管理员
const handleCancelAdmin = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消用户 ${user.username} 的管理员权限吗？`,
      '提示',
      {
        type: 'warning'
      }
    )
    await cancelAdmin(user.id)
    ElMessage.success('取消管理员成功')
    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消管理员失败')
    }
  }
}

// 封禁用户
const handleBanUser = (user: User) => {
  banForm.userId = user.id
  banForm.duration = 1
  banForm.reason = ''
  banDialogVisible.value = true
}

// 确认封禁
const confirmBanUser = async () => {
  if (!banForm.reason) {
    ElMessage.warning('请输入封禁原因')
    return
  }
  try {
    await banUser({
      userId: banForm.userId!,
      duration: banForm.duration,
      reason: banForm.reason
    })
    ElMessage.success('封禁用户成功')
    banDialogVisible.value = false
    fetchUserList()
  } catch (error) {
    ElMessage.error('封禁用户失败')
  }
}

// 解封用户
const handleUnbanUser = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要解封用户 ${user.username} 吗？`,
      '提示',
      {
        type: 'warning'
      }
    )
    await unbanUser(user.id)
    ElMessage.success('解封用户成功')
    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('解封用户失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-list {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.ml-2 {
  margin-left: 8px;
}

:deep(.el-table) {
  margin-top: 20px;
}
</style> 