<template>
  <div class="comment-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评论管理</span>
          <!-- 添加搜索框 -->
          <div class="search-container" style="margin-left: auto;">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索内容/用户名/用户ID/帖子ID"
              clearable
              style="width: 300px;"
            >
              <template #suffix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>
      <el-table :data="filteredCommentList" v-loading="loading">
        <el-table-column prop="commentId" label="ID" width="80" />
        <el-table-column prop="appName" label="用户名" width="120" />
        <el-table-column prop="content" label="评论内容" show-overflow-tooltip />
        <el-table-column prop="postId" label="帖子ID" width="100" />
        <el-table-column prop="comments" label="回复数" width="80" />
        <el-table-column prop="likes" label="点赞数" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加分页组件 -->
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

    <!-- 评论详情对话框 -->
    <el-dialog v-model="detailVisible" title="评论详情" width="600px">
      <div v-loading="detailLoading">
        <el-descriptions v-if="currentCommentDetail" :column="1" border>
          <el-descriptions-item label="ID">{{ currentCommentDetail.commentId }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentCommentDetail.appId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentCommentDetail.appName || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="用户头像">
            <el-avatar :size="40" :src="currentCommentDetail.avatar" />
          </el-descriptions-item>
          <el-descriptions-item label="用户等级">{{ currentCommentDetail.grade }}</el-descriptions-item>
          <el-descriptions-item label="帖子ID">{{ currentCommentDetail.postId }}</el-descriptions-item>
          <el-descriptions-item label="评论内容">{{ currentCommentDetail.content }}</el-descriptions-item>
          <el-descriptions-item label="点赞数">{{ currentCommentDetail.likes }}</el-descriptions-item>
          <el-descriptions-item label="回复数">{{ currentCommentDetail.subComments?.length || 0 }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentCommentDetail.createTime }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="加载中..." />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElDialog, ElDescriptions, ElDescriptionsItem, ElAvatar, ElTag, ElEmpty, ElPagination, ElInput, ElIcon } from 'element-plus'
import { getAllCommentsAdmin, deleteCommentAdmin, getCommentDetail } from '@/api/comment'
import type { Page } from '@/api/comment'
import { CommentStatus } from '@/types/comment'
import type { CommentInfo, CommentDetail } from '@/types/comment'

const loading = ref(false)
const commentList = ref<CommentInfo[]>([])
const detailVisible = ref(false)
const currentCommentDetail = ref<CommentDetail | null>(null)
const detailLoading = ref(false)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 添加搜索关键词 ref
const searchKeyword = ref('')

// 计算属性：过滤后的评论列表
const filteredCommentList = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return commentList.value // 没有关键词，返回原始列表
  }

  return commentList.value.filter(comment => {
    // 检查 content (string)
    const contentMatch = comment.content && comment.content.toLowerCase().includes(keyword)
    // 检查 appName (string)
    const appNameMatch = comment.appName && comment.appName.toLowerCase().includes(keyword)
    // 检查 appId (number)
    const appIdMatch = String(comment.appId).includes(keyword)
    // 检查 postId (number)
    const postIdMatch = String(comment.postId).includes(keyword)

    return contentMatch || appNameMatch || appIdMatch || postIdMatch
  })
})

const loadComments = async () => {
  loading.value = true
  commentList.value = []
  pagination.total = 0
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    // 使用双重断言解决 Linter 报错，明确告知 TS 实际类型
    const res = await getAllCommentsAdmin(params) as unknown as Page<CommentInfo>; 
    
    if (res && res.records) {
      commentList.value = res.records
      pagination.total = res.total || 0
    } else {
      // 响应无效或缺少 records
      console.error('获取评论列表响应无效或缺少 records:', res)
      ElMessage.error('获取评论数据格式错误')
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表接口请求失败')
  } finally {
    loading.value = false
  }
}

// 分页大小改变处理
const handleSizeChange = (val: number) => {
  pagination.pageSize = val
  pagination.currentPage = 1 // 页大小改变时回到第一页
  loadComments()
}

// 当前页改变处理
const handleCurrentChange = (val: number) => {
  pagination.currentPage = val
  loadComments()
}

const handleView = async (row: CommentInfo) => {
  currentCommentDetail.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    // res 直接是后端返回的数据对象，使用双重断言处理 Linter
    const res = await getCommentDetail(row.commentId) as unknown as CommentDetail;
    
    // 直接使用 res 进行赋值
    if (res) { // 添加检查确保 res 有效
      currentCommentDetail.value = res;
    } else {
      console.error('获取评论详情响应无效:', res);
      ElMessage.error('获取评论详情失败');
      currentCommentDetail.value = null; // 确保置为 null
      detailVisible.value = false; // 获取失败则关闭对话框
    }
  } catch (error) {
    console.error('获取评论详情失败:', error)
    ElMessage.error('获取评论详情失败')
    currentCommentDetail.value = null; // 确保置为 null
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const handleDelete = async (row: CommentInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条评论吗？ (ID: ${row.commentId})`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteCommentAdmin(row.commentId)
    ElMessage.success('删除成功')
    if (commentList.value.length === 1 && pagination.currentPage > 1) {
        pagination.currentPage--;
    }
    loadComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
/* 搜索框容器样式（如果需要） */
/* .search-container { ... } */
</style> 