<template>
  <div :class="['layout-container', { 'sidebar-collapsed': isCollapse }]">
    <!-- 侧边栏 -->
    <el-aside class="sidebar">
      <div class="logo">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-img" v-if="!isCollapse"/> 
        <h2 v-if="!isCollapse" class="logo-text">管理系统</h2>
        <img src="@/assets/logo-small.svg" alt="Logo" class="logo-img-small" v-else/> 
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          router
          class="menu"
          :collapse="isCollapse"
          :collapse-transition="false" 
          unique-opened 
        >
          <el-menu-item index="/dashboard">
            <el-icon><Histogram /></el-icon>
            <template #title><span>仪表盘</span></template>
          </el-menu-item>
          
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <template #title><span>用户管理</span></template>
          </el-menu-item>
          
          <el-menu-item index="/events">
            <el-icon><Calendar /></el-icon>
            <template #title><span>活动管理</span></template>
          </el-menu-item>
           
           <el-menu-item index="/products"> 
             <el-icon><Goods /></el-icon> 
             <template #title><span>商品管理</span></template> 
           </el-menu-item>

          <el-menu-item index="/posts">
            <el-icon><Document /></el-icon>
            <template #title><span>帖子管理</span></template>
          </el-menu-item>
          
          <el-menu-item index="/comments">
            <el-icon><ChatDotRound /></el-icon>
            <template #title><span>评论管理</span></template>
          </el-menu-item>
          
          <el-menu-item index="/complaints">
            <el-icon><Warning /></el-icon>
            <template #title><span>投诉管理</span></template>
          </el-menu-item>
          
          <el-menu-item index="/resources">
            <el-icon><Folder /></el-icon>
            <template #title><span>资源管理</span></template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="toggle-sidebar" @click="toggleSidebar" :size="20">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span>欢迎使用管理系统</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar 
                :src="userStore.userInfo?.avatarUrl || defaultAvatar" 
                size="small" 
                style="margin-right: 8px;" 
              />
              {{ userStore.userInfo?.userAccount || '管理员' }}
              <el-icon class="el-icon--right"><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main">
        <el-scrollbar>
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-scrollbar>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElAside, ElContainer, ElHeader, ElMain, ElMenu, ElMenuItem, ElIcon, ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar, ElScrollbar } from 'element-plus'
import {
  Histogram, User, Calendar, Document, ChatDotRound, Warning, Folder, CaretBottom, Fold, Expand, Goods
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

if (!userStore.userInfo) {
  userStore.loadUserInfo()
}

const isCollapse = ref(false)
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu as string
  }
  return path
})

const defaultAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command: string | number | object) => {
  if (command === 'logout') {
    userStore.logout()
    router.push(`/login?redirect=${route.fullPath}`)
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped lang="scss">
$sidebar-width: 210px
$sidebar-collapsed-width: 64px
$sidebar-bg: #304156
$sidebar-text-color: #bfcbd9
$sidebar-active-text-color: #409EFF
$sidebar-hover-bg: #263445

.layout-container {
  height: 100vh
  display: flex
  background-color: #f0f2f5

  .sidebar {
    width: $sidebar-width
    background-color: $sidebar-bg
    height: 100%
    transition: width 0.28s
    overflow: hidden
    display: flex
    flex-direction: column

    .logo {
      height: 60px
      display: flex
      align-items: center
      justify-content: center
      padding: 0 10px
      background-color: lighten($sidebar-bg, 5%)
      flex-shrink: 0

      .logo-img {
         height: 32px
         width: 32px
         margin-right: 12px
       }
       .logo-img-small {
         height: 32px
         width: 32px
       }

      .logo-text {
        color: white
        font-size: 18px
        font-weight: 600
        white-space: nowrap
        overflow: hidden
      }
    }

    .el-scrollbar {
      flex-grow: 1
      height: calc(100% - 60px)
    }

    .menu {
      border: none
      height: 100%
      background-color: $sidebar-bg

      .el-menu-item, ::v-deep(.el-sub-menu__title) {
        color: $sidebar-text-color
        &:hover {
          background-color: $sidebar-hover-bg !important
          color: white !important
        }
      }

      .el-menu-item.is-active {
        color: $sidebar-active-text-color !important
        background-color: $sidebar-hover-bg !important
         &::before {
           content: ''
           position: absolute
           left: 0
           top: 0
           bottom: 0
           width: 3px
           background-color: $sidebar-active-text-color
         }
      }
    }
      &.el-aside {
         overflow-x: hidden
      }
  }

  .main-container {
    flex: 1
    display: flex
    flex-direction: column
    transition: margin-left 0.28s
    margin-left: $sidebar-width
    min-width: 0
  }

  &.sidebar-collapsed {
    .sidebar {
      width: $sidebar-collapsed-width
       .logo-text {
         display: none
       }
    }
    .main-container {
      margin-left: $sidebar-collapsed-width
    }
  }


  .header {
    background: white
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08)
    height: 50px
    display: flex
    align-items: center
    justify-content: space-between
    padding: 0 15px
    flex-shrink: 0

    .header-left {
      display: flex
      align-items: center
       color: #303133
       font-size: 14px

      .toggle-sidebar {
        cursor: pointer
        margin-right: 15px
        padding: 5px
         color: #606266
         &:hover {
            background-color: #f5f7fa
            border-radius: 3px
         }
      }
    }

    .header-right {
      display: flex
      align-items: center

      .user-info {
        display: flex
        align-items: center
        cursor: pointer
         color: #606266
         padding: 5px
          &:hover {
            background-color: #f5f7fa
            border-radius: 3px
         }
        .el-icon--right {
          margin-left: 5px
        }
      }
    }
  }

  .main {
    flex-grow: 1
    background: #f0f2f5
    padding: 15px
    overflow: auto

     .el-scrollbar {
        height: 100%
     }
  }
   
   .footer {
      height: 40px
      background-color: #fff
      border-top: 1px solid #e6e6e6
      display: flex
      align-items: center
      justify-content: center
      font-size: 12px
      color: #909399
      flex-shrink: 0
   }
}

.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all .5s
}
.fade-transform-enter-from {
  opacity: 0
  transform: translateX(-30px)
}
.fade-transform-leave-to {
  opacity: 0
  transform: translateX(30px)
}

.el-menu--collapse .el-sub-menu__title .el-icon,
.el-menu--collapse .el-menu-item .el-icon {
    margin-right: 0
}
.el-menu--collapse .el-menu-item span,
.el-menu--collapse .el-sub-menu__title span {
    display: none
}
.el-menu--collapse .el-sub-menu__title .el-icon-arrow-right {
    display: none
}

</style> 