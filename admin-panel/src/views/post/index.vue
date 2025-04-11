<template>
  <div class="post-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>帖子管理</span>
          <el-input
            v-model="keyword"
            placeholder="搜索帖子"
            style="width: 200px; margin-left: 20px"
            clearable
            @keyup.enter="handleSearch"
          />
        </div>
      </template>
      <el-table :data="postList" v-loading="loading">
        <el-table-column prop="postId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column label="作者" width="180">
          <template #default="{ row }">
            <div style="display: flex; align-items: center;">
              <el-avatar :size="30" :src="row.avatar" style="margin-right: 10px;" />
              <span>{{ row.appName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="comments" label="评论数" width="80" />
        <el-table-column prop="likes" label="点赞数" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination" v-if="hasMore">
        <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
      </div>
    </el-card>

    <!-- 帖子详情对话框 -->
    <el-dialog v-model="detailVisible" title="帖子详情" width="600px">
      <template v-if="currentPost">
        <div class="post-detail">
          <div class="post-header">
            <el-avatar :size="40" :src="currentPost.avatar" />
            <div class="post-author">
              <div class="author-name">{{ currentPost.appName }}</div>
              <div class="post-time">{{ formatTime(currentPost.createTime) }}</div>
            </div>
          </div>
          <h3 class="post-title">{{ currentPost.title }}</h3>
          <div class="post-content">{{ currentPost.content }}</div>
          <div class="post-image" v-if="currentPost.picture">
            <el-image :src="currentPost.picture" :preview-src-list="[currentPost.picture]" />
          </div>
          <div class="post-stats">
            <span>点赞: {{ currentPost.likes }}</span>
            <span>评论: {{ currentPost.comments }}</span>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostListWithCursor, getPostDetail, deletePost } from '@/api/post'
import type { PostInfo } from '@/types/post'

const loading = ref(false)
const loadingMore = ref(false)
const postList = ref<PostInfo[]>([])
const keyword = ref('')
const cursor = ref<string | undefined>(undefined)
const hasMore = ref(false)
const detailVisible = ref(false)
const currentPost = ref<PostInfo | null>(null)
const adminUserId = 1 // 管理员ID，实际应从用户存储中获取

// 格式化时间显示
const formatTime = (timeStr: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString()
}

// 加载帖子列表
const loadPosts = async (newSearch = false) => {
  if (newSearch) {
    loading.value = true
    cursor.value = undefined
    postList.value = []
  } else {
    loadingMore.value = true
  }

  try {
    const params = {
      cursor: cursor.value,
      pageSize: 10,
      asc: false,
      keyword: keyword.value
    }

    const response = await getPostListWithCursor(params)
    
    // 处理返回的数据
    const responseData = response as any
    
    if (responseData && responseData.records) {
      if (newSearch) {
        postList.value = responseData.records
      } else {
        postList.value = [...postList.value, ...responseData.records]
      }
      
      cursor.value = responseData.nextCursor
      hasMore.value = !!responseData.hasMore
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 搜索
const handleSearch = () => {
  loadPosts(true)
}

// 加载更多
const loadMore = () => {
  if (hasMore.value) {
    loadPosts(false)
  }
}

// 查看帖子详情
const handleView = async (row: PostInfo) => {
  try {
    loading.value = true
    // 这里可以调用详情API获取更完整的信息，也可以直接使用列表中的数据
    // const detail = await getPostDetail(row.postId)
    // currentPost.value = detail
    
    // 直接使用列表数据显示
    currentPost.value = row
    detailVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error('获取帖子详情失败')
  } finally {
    loading.value = false
  }
}

// 删除帖子
const handleDelete = (row: PostInfo) => {
  ElMessageBox.confirm(
    `确定要删除标题为"${row.title}"的帖子吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        const response = await deletePost(row.postId, adminUserId)
        ElMessage.success('删除成功')
        // 重新加载帖子列表
        loadPosts(true)
      } catch (error) {
        console.error(error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 取消删除
    })
}

onMounted(() => {
  loadPosts(true)
})
</script>

<style scoped>
.post-container {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.post-detail {
  padding: 15px;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.post-author {
  margin-left: 10px;
}

.author-name {
  font-weight: bold;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-title {
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: bold;
}

.post-content {
  margin-bottom: 15px;
  white-space: pre-wrap;
  word-break: break-word;
}

.post-image {
  margin-bottom: 15px;
}

.post-stats {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 14px;
}
</style> 