<template>
  <div class="user-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="search-container">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名/手机/邮箱"
              clearable
              @input="handleSearch"
              style="width: 220px;"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>
      <el-table :data="filteredUserList" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userAccount" label="用户名" width="120" />
        <el-table-column prop="avatarUrl" label="头像" width="100">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatarUrl" />
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="state" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.state === 0 ? 'success' : row.state === -1 ? 'danger' : 'warning'">
              {{ row.state === 0 ? '正常' : row.state === -1 ? '禁用' : '管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="等级" width="80" />
        <el-table-column prop="signIn" label="最后登录" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="warning" link @click="handleStatus(row)">
              {{ row.state === -1 ? '启用' : '禁用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAllUsers, banUser, unbanUser } from '@/api/user'
import type { User } from '@/types/user'

const loading = ref(false)
const userList = ref<User[]>([])
const searchKeyword = ref('')

// 根据关键词过滤用户列表
const filteredUserList = computed(() => {
  if (!searchKeyword.value) {
    return userList.value
  }
  
  const keyword = searchKeyword.value.toLowerCase()
  return userList.value.filter(user => {
    // 搜索用户名、手机号、邮箱
    return (user.userAccount && user.userAccount.toLowerCase().includes(keyword)) ||
           (user.phone && user.phone.includes(keyword)) ||
           (user.email && user.email.toLowerCase().includes(keyword))
  })
})

// 处理搜索
const handleSearch = () => {
  // 搜索已在计算属性中处理，无需额外操作
}

const loadUsers = async () => {
  loading.value = true
  try {
    const users = await getAllUsers()
    if (users && Array.isArray(users)) {
      userList.value = users
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleView = (row: User) => {
  ElMessageBox.alert(
    `
    <div>
      <p><strong>ID:</strong> ${row.id}</p>
      <p><strong>用户名:</strong> ${row.userAccount}</p>
      <p><strong>手机号:</strong> ${row.phone || '未设置'}</p>
      <p><strong>邮箱:</strong> ${row.email || '未设置'}</p>
      <p><strong>等级:</strong> ${row.grade}</p>
      <p><strong>经验值:</strong> ${row.exp}</p>
      <p><strong>信用分:</strong> ${row.credit || '未设置'}</p>
      <p><strong>赛点:</strong> ${row.score || '未设置'}</p>
      <p><strong>最后登录:</strong> ${row.signIn || '无记录'}</p>
      <p><strong>描述:</strong> ${row.description || '无'}</p>
    </div>
    `,
    '用户详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

const handleStatus = (row: User) => {
  const isDisabled = row.state === -1
  const action = isDisabled ? '启用' : '禁用'
  const days = isDisabled ? 0 : 7 // 默认禁用7天
  
  ElMessageBox.confirm(
    `确定要${action}用户 "${row.userAccount}" 吗?${!isDisabled ? ' 默认禁用7天。' : ''}`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        let result
        if (isDisabled) {
          // 解除禁用
          result = await unbanUser(row.id)
          if (result) {
            // 立即更新本地用户状态
            row.state = 0
            ElMessage.success(`${action}用户成功`)
          }
        } else {
          // 禁用用户
          result = await banUser({ id: row.id, days })
          if (result) {
            // 立即更新本地用户状态
            row.state = -1
            row.unblockingTime = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toISOString()
            ElMessage.success(`${action}用户成功`)
          }
        }
        
        if (result) {
          // 重新加载用户列表以确保与服务器同步
          loadUsers()
        } else {
          ElMessage.error(`${action}用户失败`)
        }
      } catch (error) {
        ElMessage.error(`${action}用户失败`)
      }
    })
    .catch(() => {
      // 取消操作
    })
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-container {
  display: flex;
  align-items: center;
}
</style> 