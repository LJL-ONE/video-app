import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Index',
    component: () => import('@/views/Index.vue')
  },
  {
    path: '/video/:id',
    name: 'VideoPlay',
    component: () => import('@/views/VideoPlay.vue')
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('@/views/Upload.vue')
  },
  {
    path: '/category/:cat',
    name: 'Category',
    component: () => import('@/views/Index.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    children: [
      { path: 'user', name: 'AdminUser', component: () => import('@/views/admin/UserList.vue') },
      { path: 'video', name: 'AdminVideo', component: () => import('@/views/admin/VideoManage.vue') },
      { path: 'meeting', name: 'AdminMeeting', component: () => import('@/views/admin/Meeting.vue') }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    if (token) return next('/')
    next()
  } else if (to.path.startsWith('/admin')) {
    // 管理后台需要登录
    if (!token) return next('/login')
    next()
  } else {
    next()
  }
})

export default router