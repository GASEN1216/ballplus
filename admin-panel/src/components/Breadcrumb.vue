<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
      {{ item.meta?.title || item.name }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, RouteLocationMatched } from 'vue-router'

const route = useRoute()
const breadcrumbs = ref<RouteLocationMatched[]>([])

const getBreadcrumbs = () => {
  // 过滤掉没有meta.title的路由
  breadcrumbs.value = route.matched.filter(
    item => item.meta && item.meta.title
  )
}

watch(
  () => route.path,
  () => {
    getBreadcrumbs()
  },
  {
    immediate: true
  }
)
</script>

<style scoped>
.el-breadcrumb {
  line-height: 60px;
}
</style> 