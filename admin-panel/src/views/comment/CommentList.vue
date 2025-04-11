<template>
  <div class="comment-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="帖子ID">
          <el-input
            v-model.number="searchForm.postId"
            placeholder="请输入帖子ID"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input
            v-model.number="searchForm.userId"
            placeholder="请输入用户ID"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="CommentStatus.NORMAL" />
            <el-option label="已删除" :value="CommentStatus.DELETED" />
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

    <!-- 评论列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="commentList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="作者" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.user.avatarUrl" />
              <span class="username">{{ row.user.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="postId" label="帖子ID" width="100" />
        <el-table-column prop="subCommentCount" label="回复数" width="80" />
        <el-table-column prop="likeCount" label="点赞数" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === CommentStatus.NORMAL ? 'success' : 'danger'">
              {{ row.status === CommentStatus.NORMAL ? '正常' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
            <el-button type="success" link @click="handleLike(row)">
              {{ row.isLiked ? '取消点赞' : '点赞' }}
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
          v-if="hasNext"
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

    <!-- 评论详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="评论详情"
      width="800px"
    >
      <div v-if="currentComment" class="comment-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentComment.id }}</el-descriptions-item>
          <el-descriptions-item label="作者">
            <div class="user-info">
              <el-avatar :size="24" :src="currentComment.user.avatarUrl" />
              <span class="username">{{ currentComment.user.username }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="帖子ID">{{ currentComment.postId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentComment.status === CommentStatus.NORMAL ? 'success' : 'danger'">
              {{ currentComment.status === CommentStatus.NORMAL ? '正常' : '已删除' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="回复数">{{ currentComment.subCommentCount }}</el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ currentComment.likeCount }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentComment.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentComment.updateTime }}</el-descriptions-item>
        </el-descriptions>
        <div class="comment-content">
          <h4>评论内容</h4>
          <p>{{ currentComment.content }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑评论对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑评论' : '新增评论'"
      width="600px"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="80px"
      >
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model.number="editForm.userId" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="帖子ID" prop="postId">
          <el-input v-model.number="editForm.postId" placeholder="请输入帖子ID" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评论内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getCommentList,
  getCommentDetail,
  addComment,
  updateComment,
  deleteComment,
  likeComment
} from '@/api/comment'
import type {
  CommentInfo,
  CommentDetail,
  AddCommentRequest,
  UpdateCommentRequest,
  CommentStatus
} from '@/types/comment'

// 搜索表单
const searchForm = reactive({
  postId: undefined as number | undefined,
  userId: undefined as number | undefined,
  status: undefined as CommentStatus | undefined
})

// 编辑表单
const editFormRef = ref<FormInstance>()
const editForm = reactive<AddCommentRequest & { commentId?: number }>({
  userId: undefined,
  postId: undefined,
  content: ''
})
const editFormRules: FormRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  postId: [{ required: true, message: '请输入帖子ID', trigger: 'blur' }],
  content: [{ required: true, message: '请输入评论内容', trigger: 'blur' }]
}

// 列表数据
const loading = ref(false)
const commentList = ref<CommentInfo[]>([])
const currentComment = ref<CommentDetail | null>(null)
const hasNext = ref(false)
const nextCursor = ref<number | undefined>()

// 对话框控制
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEdit = ref(false)

// 获取评论列表
const fetchCommentList = async () => {
  try {
    loading.value = true
    const response = await getCommentList(searchForm.postId!)
    commentList.value = response
    hasNext.value = false // 由于后端没有游标分页，这里暂时设置为false
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  nextCursor.value = undefined
  fetchCommentList()
}

// 重置
const handleReset = () => {
  searchForm.postId = undefined
  searchForm.userId = undefined
  searchForm.status = undefined
  handleSearch()
}

// 加载更多
const loadMore = () => {
  if (nextCursor.value) {
    fetchCommentList()
  }
}

// 查看详情
const handleView = async (row: CommentInfo) => {
  try {
    loading.value = true
    const response = await getCommentDetail(row.id)
    currentComment.value = response
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取评论详情失败:', error)
    ElMessage.error('获取评论详情失败')
  } finally {
    loading.value = false
  }
}

// 点赞/取消点赞
const handleLike = async (row: CommentInfo) => {
  try {
    await likeComment(row.id)
    row.isLiked = !row.isLiked
    row.likeCount += row.isLiked ? 1 : -1
    ElMessage.success(row.isLiked ? '点赞成功' : '取消点赞成功')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 编辑
const handleEdit = (row: CommentInfo) => {
  isEdit.value = true
  editForm.commentId = row.id
  editForm.userId = row.userId
  editForm.postId = row.postId
  editForm.content = row.content
  editDialogVisible.value = true
}

// 删除
const handleDelete = async (row: CommentInfo) => {
  try {
    await ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
      type: 'warning'
    })
    await deleteComment(row.postId, row.id, row.userId)
    ElMessage.success('删除成功')
    fetchCommentList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除评论失败')
    }
  }
}

// 确认编辑
const handleConfirmEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate()
  try {
    if (isEdit.value) {
      const { commentId, userId, content } = editForm
      await updateComment({ commentId: commentId!, userId, content })
      ElMessage.success('更新成功')
    } else {
      const { userId, postId, content } = editForm
      await addComment({ userId, postId, content })
      ElMessage.success('添加成功')
    }
    editDialogVisible.value = false
    fetchCommentList()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchCommentList()
})
</script>

<style scoped>
.comment-list {
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

.comment-detail {
  padding: 20px;
}

.comment-content {
  margin-top: 20px;
}

.comment-content h4 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.comment-content p {
  line-height: 1.6;
  color: #606266;
}
</style> 