<template>
  <div class="layout">
    <el-container>
      <el-aside width="210px" class="sidebar">
        <div class="logo">
          <img src="@/assets/logo.svg" alt="Logo" class="logo-img"/>
          <span class="logo-text">球plus管理系统</span>
        </div>
        <el-scrollbar>
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical"
            :router="true"
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/dashboard">
              <el-icon><Monitor /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/user">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/event">
              <el-icon><Calendar /></el-icon>
              <span>活动管理</span>
            </el-menu-item>
            <el-menu-item index="/product">
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </el-menu-item>
            <el-menu-item index="/post">
              <el-icon><Document /></el-icon>
              <span>帖子管理</span>
            </el-menu-item>
            <el-menu-item index="/comment">
              <el-icon><ChatLineRound /></el-icon>
              <span>评论管理</span>
            </el-menu-item>
            <el-menu-item index="/complaint">
              <el-icon><Warning /></el-icon>
              <span>举报管理</span>
            </el-menu-item>
            <el-menu-item index="/resource">
              <el-icon><Folder /></el-icon>
              <span>资源管理</span>
            </el-menu-item>
          </el-menu>
        </el-scrollbar>
      </el-aside>
      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb>
              <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRouteTitle">{{ currentRouteTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo?.avatarUrl || defaultAvatar" />
                <span class="username">{{ userStore.userInfo?.userAccount || 'Admin' }}</span>
                <el-icon class="el-icon--right"><CaretBottom /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main class="main-content">
          <el-scrollbar>
            <router-view v-slot="{ Component }">
              <transition name="fade-transform" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  ElContainer, ElAside, ElHeader, ElMain, ElMenu, ElMenuItem, ElIcon,
  ElBreadcrumb, ElBreadcrumbItem, ElDropdown, ElDropdownMenu, ElDropdownItem,
  ElAvatar, ElScrollbar
} from 'element-plus'
import {
  Monitor, User, Calendar, Document, ChatLineRound, Warning, Folder, Goods, CaretBottom
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

if (!userStore.userInfo) {
  // userStore.fetchUserInfo(); // 或者其他加载逻辑
}

const defaultAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

const activeMenu = computed(() => route.path)

const currentRouteTitle = computed(() => {
  return route.matched[1]?.meta?.title as string || route.meta?.title as string || ''
})

const handleLogout = () => {
  userStore.logout()
  router.replace({ path: '/login', query: { redirect: route.fullPath } })
}

const goToProfile = () => {
  router.push('/profile')
}
</script>

<style scoped lang="scss">
.layout {
  height: 100vh;
  display: flex;
}

.el-container {
  height: 100%;
}

.sidebar {
  background-color: #304156;
  transition: width 0.28s;
  display: flex;
  flex-direction: column;
  height: 100%;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 10px;
    background-color: lighten(#304156, 5%);
    flex-shrink: 0;

    .logo-img {
      height: 32px;
      width: 32px;
      margin-right: 10px;
    }
    .logo-text {
      color: white;
      font-size: 18px;
      font-weight: 600;
      white-space: nowrap;
    }
  }

  .el-scrollbar {
    flex-grow: 1;
  }

  .el-menu-vertical {
    border-right: none;
    height: 100%;

    .el-menu-item {
      .el-icon {
        margin-right: 10px;
      }
      &:hover {
        background-color: #263445 !important;
      }
      &.is-active {
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 0;
          bottom: 0;
          width: 3px;
          background-color: #409EFF;
        }
      }
    }
  }
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, .08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 50px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;

  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 5px;
    border-radius: 4px;
    &:hover {
      background-color: #f5f7fa;
    }

    .el-avatar {
      margin-right: 8px;
    }
    
    .username {
      font-size: 14px;
      color: #303133;
      margin-right: 5px;
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  flex-grow: 1;
  overflow: hidden;

  .el-scrollbar {
    height: 100%;
  }
}

.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all .3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style> 