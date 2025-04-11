<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>用户总数</span>
              <el-icon><User /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ stats.userCount }}</div>
            <div class="trend">
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>帖子总数</span>
              <el-icon><Document /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ stats.postCount }}</div>
            <div class="trend">
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>评论总数</span>
              <el-icon><ChatLineRound /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ stats.commentCount }}</div>
            <div class="trend">
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>投诉总数</span>
              <el-icon><Warning /></el-icon>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ stats.complaintCount }}</div>
            <div class="trend">
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="userChart" style="width: 100%; height: 300px"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>内容发布趋势</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="contentChart" style="width: 100%; height: 300px"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, Document, ChatLineRound, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboardStats, getUserTrend, getContentTrend } from '@/api/dashboard'
import type { DashboardStats, UserTrendResponse, ContentTrendResponse } from '@/types/dashboard'
import { ElMessage } from 'element-plus'

const stats = ref<DashboardStats>({
  userCount: 0,
  userTrend: 0,
  postCount: 0,
  postTrend: 0,
  commentCount: 0,
  commentTrend: 0,
  complaintCount: 0,
  complaintTrend: 0
})

const userChart = ref<HTMLElement | null>(null)
const contentChart = ref<HTMLElement | null>(null)

onMounted(() => {
  // 初始化统计数据
  initStats()
  // 初始化图表
  initCharts()
})

const initStats = async () => {
  try {
    const response = await getDashboardStats()
    stats.value = response
  } catch (error: any) {
    ElMessage.error('获取统计数据失败：' + (error.message || '未知错误'))
  }
}

const initCharts = async () => {
  if (!userChart.value || !contentChart.value) return

  const userChartInstance = echarts.init(userChart.value)
  const contentChartInstance = echarts.init(contentChart.value)

  try {
    // 获取用户增长趋势数据
    const userTrendResponse = await getUserTrend()
    const userTrendData = userTrendResponse.data

    // 获取内容发布趋势数据
    const contentTrendResponse = await getContentTrend()
    const postData = contentTrendResponse.postData
    const commentData = contentTrendResponse.commentData

    // 用户增长趋势图配置
    userChartInstance.setOption({
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: userTrendData.map(item => item.date)
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: userTrendData.map(item => item.count),
        type: 'line',
        smooth: true
      }]
    })

    // 内容发布趋势图配置
    contentChartInstance.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['帖子', '评论']
      },
      xAxis: {
        type: 'category',
        data: postData.map(item => item.date)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '帖子',
          type: 'bar',
          data: postData.map(item => item.count)
        },
        {
          name: '评论',
          type: 'bar',
          data: commentData.map(item => item.count)
        }
      ]
    })
  } catch (error: any) {
    ElMessage.error('获取趋势数据失败：' + (error.message || '未知错误'))
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
}

.number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.trend {
  font-size: 14px;
  color: #666;
}

.trend .up {
  color: #67c23a;
}

.trend .down {
  color: #f56c6c;
}

.chart-row {
  margin-top: 20px;
}

.chart-container {
  margin-top: 20px;
}
</style> 