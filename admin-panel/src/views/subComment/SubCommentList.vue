<template>
  <div class="sub-comment-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="内容">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入子评论内容关键词"
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
        <el-form-item label="父评论">
          <el-input
            v-model="searchForm.commentContent"
            placeholder="请输入父评论内容关键词"
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

    <!-- 子评论列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="subCommentList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="作者" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.avatarUrl" />
              <span class="username">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="commentContent" label="父评论" min-width="150" show-overflow-tooltip />
        <el-table-column prop="postTitle" label="所属帖子" min-width="150" show-overflow-tooltip />
        <el-table-column prop="likes" label="点赞" width="80" />
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看
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

    <!-- 子评论详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="子评论详情"
      width="800px"
    >
      <div v-if="currentSubComment" class="sub-comment-detail">
        <div class="sub-comment-content">
          {{ currentSubComment.content }}
        </div>
        <div class="sub-comment-meta">
          <div class="user-info">
            <el-avatar :size="40" :src="currentSubComment.user.avatarUrl" />
            <span class="username">{{ currentSubComment.user.username }}</span>
          </div>
          <div class="comment-info">
            <span>父评论：{{ currentSubComment.comment.content }}</span>
          </div>
          <div class="post-info">
            <span>所属帖子：{{ currentSubComment.comment.postTitle }}</span>
          </div>
          <div class="sub-comment-stats">
            <span>点赞: {{ currentSubComment.likes }}</span>
            <span>发布时间: {{ currentSubComment.createTime }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑子评论对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑子评论"
      width="600px"
    >
      <el-form
        v-if="editForm"
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
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
import type { SubCommentInfo, SubCommentDetail, UpdateSubCommentRequest } from '@/types/subComment'
import {
  getSubCommentListWithCursor,
  getSubCommentDetail,
  updateSubComment,
  deleteSubComment
} from '@/api/subComment'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  username: '',
  commentContent: ''
})

// 数据列表
const loading = ref(false)
const subCommentList = ref<SubCommentInfo[]>([])
const nextCursor = ref<string>()
const hasMore = ref(true)

// 对话框
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentSubComment = ref<SubCommentDetail>()
const editFormRef = ref<FormInstance>()
const editForm = ref<UpdateSubCommentRequest>()

// 表单验证规则
const editRules = {
  content: [{ required: true, message: '请输入子评论内容', trigger: 'blur' }]
}

// 获取子评论列表
const fetchSubCommentList = async (isLoadMore = false) => {
  if (!isLoadMore) {
    loading.value = true
    subCommentList.value = []
    nextCursor.value = undefined
  }

  try {
    const { data } = await getSubCommentListWithCursor({
      cursor: nextCursor.value,
      pageSize: 10,
      ...searchForm
    })

    if (isLoadMore) {
      subCommentList.value.push(...data.records)
    } else {
      subCommentList.value = data.records
    }

    nextCursor.value = data.nextCursor
    hasMore.value = data.hasMore
  } catch (error) {
    ElMessage.error('获取子评论列表失败')
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  fetchSubCommentList(true)
}

// 搜索
const handleSearch = () => {
  fetchSubCommentList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.username = ''
  searchForm.commentContent = ''
  handleSearch()
}

// 查看详情
const handleView = async (subComment: SubCommentInfo) => {
  try {
    const { data } = await getSubCommentDetail(subComment.id)
    currentSubComment.value = data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取子评论详情失败')
  }
}

// 编辑
const handleEdit = (subComment: SubCommentInfo) => {
  editForm.value = {
    id: subComment.id,
    content: subComment.content,
    userId: subComment.userId
  }
  editDialogVisible.value = true
}

// 确认编辑
const confirmEdit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid && editForm.value) {
      try {
        await updateSubComment(editForm.value)
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        fetchSubCommentList()
      } catch (error) {
        ElMessage.error('更新失败')
      }
    }
  })
}

// 删除
const handleDelete = async (subComment: SubCommentInfo) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该子评论吗？',
      '提示',
      {
        type: 'warning'
      }
    )
    await deleteSubComment(subComment.id)
    ElMessage.success('删除成功')
    fetchSubCommentList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchSubCommentList()
})
</script>

<style scoped>
.sub-comment-list {
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

.sub-comment-detail {
  padding: 20px;
}

.sub-comment-content {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
}

.sub-comment-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-info {
  color: #606266;
  font-size: 14px;
}

.post-info {
  color: #606266;
  font-size: 14px;
}

.sub-comment-stats {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
}
</style> 