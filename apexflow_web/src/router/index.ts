import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'
import Home from '@/views/Home.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { title: '首页' }
  },
  {
    path: '/app',
    component: Layout,
    redirect: '/app/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/business/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: '/orders/list',
        name: 'OrderList',
        component: () => import('@/views/business/Order.vue'),
        meta: { title: '订单列表' }
      },
      {
        path: '/orders/analysis',
        name: 'OrderAnalysis',
        component: () => import('@/views/business/OrderAnalysis.vue'),
        meta: { title: '订单分析' }
      },
      {
        path: '/logistics/tracking',
        name: 'LogisticsTracking',
        component: () => import('@/views/business/Logistics.vue'),
        meta: { title: '物流跟踪' }
      },
      {
        path: '/after-sales',
        name: 'AfterSales',
        component: () => import('@/views/business/AfterSales.vue'),
        meta: { title: '售后管理' }
      },
      {
        path: '/evaluation',
        name: 'Evaluation',
        component: () => import('@/views/business/Evaluation.vue'),
        meta: { title: '评价管理' }
      },
      {
        path: '/warehouse/inventory',
        name: 'WarehouseInventory',
        component: () => import('@/views/business/Warehouse.vue'),
        meta: { title: '库存管理' }
      },
      {
        path: '/finance/income',
        name: 'FinanceIncome',
        component: () => import('@/views/business/Finance.vue'),
        meta: { title: '收入统计' }
      },
      {
        path: '/system',
        name: 'System',
        component: () => import('@/views/business/System.vue'),
        meta: { title: '系统设置' }
      }
    ]
  },
  // 保留原有的登录页路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  }
]

const router = createRouter({
  history: createWebHistory('/ApexFlow/'),
  routes
})

export default router
