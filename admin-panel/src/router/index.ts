import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'event',
        name: 'Event',
        component: () => import('@/views/event/index.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'post',
        name: 'Post',
        component: () => import('@/views/post/index.vue'),
        meta: { title: '帖子管理' }
      },
      {
        path: 'comment',
        name: 'Comment',
        component: () => import('@/views/comment/index.vue'),
        meta: { title: '评论管理' }
      },
      {
        path: 'complaint',
        name: 'Complaint',
        component: () => import('@/views/complaint/index.vue'),
        meta: { title: '举报管理' }
      },
      {
        path: 'resource',
        name: 'Resource',
        component: () => import('@/views/resource/index.vue'),
        meta: { title: '资源管理' }
      },
      {
        path: 'product',
        name: 'Product',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '商品管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 全局导航守卫
router.beforeEach((to, from, next) => {
  if (to.path === '/login') {
    next()
  } else {
    const userStore = useUserStore()
    if (userStore.userInfo) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router 