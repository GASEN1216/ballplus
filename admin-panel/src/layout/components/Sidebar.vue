<template>
  <el-menu
    :default-active="activeMenu"
    :collapse="isCollapse"
    :unique-opened="false"
    :collapse-transition="false"
    class="sidebar-menu"
    @select="handleSelect"
  >
    <el-menu-item index="/dashboard">
      <el-icon><Odometer /></el-icon>
      <template #title>仪表盘</template>
    </el-menu-item>

    <el-menu-item index="/user">
      <el-icon><User /></el-icon>
      <template #title>用户管理</template>
    </el-menu-item>

    <el-menu-item index="/event">
      <el-icon><Calendar /></el-icon>
      <template #title>活动管理</template>
    </el-menu-item>

    <el-menu-item index="/post">
      <el-icon><Document /></el-icon>
      <template #title>帖子管理</template>
    </el-menu-item>

    <el-menu-item index="/comment">
      <el-icon><ChatDotRound /></el-icon>
      <template #title>评论管理</template>
    </el-menu-item>

    <el-menu-item index="/complaint">
      <el-icon><Warning /></el-icon>
      <template #title>举报管理</template>
    </el-menu-item>

    <el-menu-item index="/resource">
      <el-icon><Files /></el-icon>
      <template #title>资源管理</template>
    </el-menu-item>

    <el-menu-item index="/product">
      <el-icon><ShoppingBag /></el-icon>
      <template #title>商品管理</template>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Odometer,
  User,
  Calendar,
  Document,
  ChatDotRound,
  Warning,
  Files,
  ShoppingBag
} from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const isCollapse = computed(() => appStore.sidebarCollapsed)

const activeMenu = computed(() => route.path)

const handleSelect = (index: string) => {
  router.push(index)
}
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}
</style> 