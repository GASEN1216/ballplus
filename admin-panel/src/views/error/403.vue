<template>
  <div class="forbidden">
    <div class="content">
      <h1>403</h1>
      <h2>无访问权限</h2>
      <p>抱歉，您没有访问该页面的权限</p>
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.forbidden {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.content {
  text-align: center;
}

h1 {
  font-size: 120px;
  color: #F56C6C;
  margin: 0;
}

h2 {
  font-size: 30px;
  color: #606266;
  margin: 20px 0;
}

p {
  color: #909399;
  margin-bottom: 20px;
}
</style> 