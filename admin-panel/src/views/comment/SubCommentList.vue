<template>
  <div class="sub-comment-list">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="评论ID">
          <el-input
            v-model.number="searchForm.commentId"
            placeholder="请输入评论ID"
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
            <el-option label="正常" :value="SubCommentStatus.NORMAL" />
            <el-option label="已删除" :value="SubCommentStatus.DELETED" />
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
              <el-avatar :size="24" :src="row.user.avatarUrl" />
              <span class="username">{{ row.user.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="commentId" label="评论ID" width="100" />
        <el-table-column prop="likeCount" label="点赞数" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === SubCommentStatus.NORMAL ? 'success' : 'danger'">
              {{ row.status === SubCommentStatus.NORMAL ? '正常' : '已删除' }}
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

    <!-- 子评论详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="子评论详情"
      width="800px"
    >
      <div v-if="currentSubComment" class="sub-comment-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentSubComment.id }}</el-descriptions-item>
          <el-descriptions-item label="作者">
            <div class="user-info">
              <el-avatar :size="24" :src="currentSubComment.user.avatarUrl" />
              <span class="username">{{ currentSubComment.user.username }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="评论ID">{{ currentSubComment.commentId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentSubComment.status === SubCommentStatus.NORMAL ? 'success' : 'danger'">
              {{ currentSubComment.status === SubCommentStatus.NORMAL ? '正常' : '已删除' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ currentSubComment.likeCount }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentSubComment.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentSubComment.updateTime }}</el-descriptions-item>
        </el-descriptions>
        <div class="sub-comment-content">
          <h4>评论内容</h4>
          <p>{{ currentSubComment.content }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑子评论对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑子评论' : '新增子评论'"
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
        <el-form-item label="评论ID" prop="commentId">
          <el-input v-model.number="editForm.commentId" placeholder="请输入评论ID" />
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
  getSubCommentList,
  getSubCommentDetail,
  addSubComment,
  updateSubComment,
  deleteSubComment,
  likeSubComment
} from '@/api/subComment'
import type {
  SubCommentInfo,
  SubCommentDetail,
  AddSubCommentRequest,
  UpdateSubCommentRequest,
  SubCommentStatus
} from '@/types/subComment'

// 搜索表单
const searchForm = reactive({
  commentId: undefined as number | undefined,
  userId: undefined as number | undefined,
  status: undefined as SubCommentStatus | undefined
})

// 编辑表单
const editFormRef = ref<FormInstance>()
const editForm = reactive<AddSubCommentRequest & { subCommentId?: number }>({
  userId: undefined,
  commentId: undefined,
  content: ''
})
const editFormRules: FormRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  commentId: [{ required: true, message: '请输入评论ID', trigger: 'blur' }],
  content: [{ required: true, message: '请输入评论内容', trigger: 'blur' }]
}

// 列表数据
const loading = ref(false)
const subCommentList = ref<SubCommentInfo[]>([])
const currentSubComment = ref<SubCommentDetail | null>(null)
const hasNext = ref(false)
const nextCursor = ref<number | undefined>()

// 对话框控制
const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const isEdit = ref(false)

// 获取子评论列表
const fetchSubCommentList = async () => {
  try {
    loading.value = true
    const response = await getSubCommentList(searchForm.commentId!)
    subCommentList.value = response
    hasNext.value = false // 由于后端没有游标分页，这里暂时设置为false
  } catch (error) {
    console.error('获取子评论列表失败:', error)
    ElMessage.error('获取子评论列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  nextCursor.value = undefined
  fetchSubCommentList()
}

// 重置
const handleReset = () => {
  searchForm.commentId = undefined
  searchForm.userId = undefined
  searchForm.status = undefined
  handleSearch()
}

// 加载更多
const loadMore = () => {
  if (nextCursor.value) {
    fetchSubCommentList()
  }
}

// 查看详情
const handleView = async (row: SubCommentInfo) => {
  try {
    loading.value = true
    const response = await getSubCommentDetail(row.id)
    currentSubComment.value = response
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取子评论详情失败:', error)
    ElMessage.error('获取子评论详情失败')
  } finally {
    loading.value = false
  }
}

// 点赞/取消点赞
const handleLike = async (row: SubCommentInfo) => {
  try {
    await likeSubComment(row.id)
    row.isLiked = !row.isLiked
    row.likeCount += row.isLiked ? 1 : -1
    ElMessage.success(row.isLiked ? '点赞成功' : '取消点赞成功')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 编辑
const handleEdit = (row: SubCommentInfo) => {
  isEdit.value = true
  editForm.subCommentId = row.id
  editForm.userId = row.userId
  editForm.commentId = row.commentId
  editForm.content = row.content
  editDialogVisible.value = true
}

// 删除
const handleDelete = async (row: SubCommentInfo) => {
  try {
    await ElMessageBox.confirm('确定要删除该子评论吗？', '提示', {
      type: 'warning'
    })
    await deleteSubComment(row.commentId, row.id, row.userId)
    ElMessage.success('删除成功')
    fetchSubCommentList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除子评论失败:', error)
      ElMessage.error('删除子评论失败')
    }
  }
}

// 确认编辑
const handleConfirmEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate()
  try {
    if (isEdit.value) {
      const { subCommentId, userId, content } = editForm
      await updateSubComment({ subCommentId: subCommentId!, userId, content })
      ElMessage.success('更新成功')
    } else {
      const { userId, commentId, content } = editForm
      await addSubComment({ userId, commentId, content })
      ElMessage.success('添加成功')
    }
    editDialogVisible.value = false
    fetchSubCommentList()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

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
  margin-top: 20px;
}

.sub-comment-content h4 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #303133;
}

.sub-comment-content p {
  line-height: 1.6;
  color: #606266;
}
</style> 