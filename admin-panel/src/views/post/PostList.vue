<template>
  <div class="post-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入标题关键词"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="作者">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入作者用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSearch">
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 帖子列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="postList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="作者" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.avatarUrl" />
              <span class="username">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="likes" label="点赞" width="80" />
        <el-table-column prop="comments" label="评论" width="80" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
            <el-button type="success" link @click="handleLike(row)">
              点赞
            </el-button>
            <el-button type="warning" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
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

    <!-- 帖子详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="帖子详情"
      width="800px"
    >
      <div v-if="currentPost" class="post-detail">
        <h2>{{ currentPost.title }}</h2>
        <div class="post-meta">
          <div class="user-info">
            <el-avatar :size="40" :src="currentPost.user.avatarUrl" />
            <span class="username">{{ currentPost.user.username }}</span>
          </div>
          <div class="post-stats">
            <span>点赞: {{ currentPost.likes }}</span>
            <span>评论: {{ currentPost.comments }}</span>
            <span>发布时间: {{ currentPost.createTime }}</span>
          </div>
        </div>
        <div class="post-content">
          {{ currentPost.content }}
        </div>
      </div>
    </el-dialog>

    <!-- 编辑帖子对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑帖子"
      width="600px"
    >
      <el-form
        v-if="editForm"
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="6"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmEdit">
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
import type { PostInfo, PostDetail, UpdatePostRequest } from '@/types/post'
import {
  getPostListWithCursor,
  getPostDetail,
  updatePost,
  deletePost,
  likePost
} from '@/api/post'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  username: ''
})

// 数据列表
const loading = ref(false)
const postList = ref<PostInfo[]>([])
const nextCursor = ref<string>()
const hasMore = ref(true)

// 对话框
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentPost = ref<PostDetail>()
const editFormRef = ref<FormInstance>()
const editForm = ref<UpdatePostRequest>()

// 表单验证规则
const editRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 获取帖子列表
const fetchPostList = async (isLoadMore = false) => {
  if (!isLoadMore) {
    loading.value = true
    postList.value = []
    nextCursor.value = undefined
  }

  try {
    const { data } = await getPostListWithCursor({
      cursor: nextCursor.value,
      pageSize: 10,
      keyword: searchForm.keyword
    })

    if (isLoadMore) {
      postList.value.push(...data.records)
    } else {
      postList.value = data.records
    }

    nextCursor.value = data.nextCursor
    hasMore.value = data.hasMore
  } catch (error) {
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  fetchPostList(true)
}

// 搜索
const handleSearch = () => {
  fetchPostList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.username = ''
  handleSearch()
}

// 查看详情
const handleView = async (post: PostInfo) => {
  try {
    const { data } = await getPostDetail(post.id)
    currentPost.value = data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取帖子详情失败')
  }
}

// 编辑
const handleEdit = (post: PostInfo) => {
  editForm.value = {
    id: post.id,
    title: post.title,
    content: post.content,
    userId: post.userId
  }
  editDialogVisible.value = true
}

// 确认编辑
const confirmEdit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid && editForm.value) {
      try {
        await updatePost(editForm.value)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        fetchPostList()
      } catch (error) {
        ElMessage.error('更新失败')
      }
    }
  })
}

// 删除
const handleDelete = async (post: PostInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除帖子"${post.title}"吗？`,
      '提示',
      {
        type: 'warning'
      }
    )
    await deletePost(post.id, post.userId)
    ElMessage.success('删除成功')
    fetchPostList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 点赞
const handleLike = async (post: PostInfo) => {
  try {
    await likePost(post.id)
    post.likes += 1
    ElMessage.success('点赞成功')
  } catch (error) {
    ElMessage.error('点赞失败')
  }
}

// 初始化
onMounted(() => {
  fetchPostList()
})
</script>

<style scoped>
.post-list {
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

.post-detail {
  padding: 20px;
}

.post-detail h2 {
  margin: 0 0 20px;
  font-size: 24px;
  color: #303133;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #EBEEF5;
}

.post-stats {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}

.post-content {
  line-height: 1.8;
  color: #606266;
}
</style> 