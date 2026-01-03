// 1. 从vue包中导入 createApp 函数
import { createApp } from 'vue'

// 2. 导入我们的根组件
import App from './App.vue'

// 3. 导入路由配置
import router from './router'
import 'element-plus/dist/index.css'  // 确保这行存在（你原有代码已保留，无需删除）

// 补充：导入 Element Plus 核心库 + 中文包（新增，保障完整功能）
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';

// 4. 创建Vue应用实例，并传入根组件
const app = createApp(App)

// 你原有代码：单独配置 ElMessage（保留不变，确保 $message 全局可用）
import { ElMessage } from 'element-plus'
ElMessage.install = (app) => {
  // 确保 ElMessage 使用 document.body 作为容器
  app.config.globalProperties.$message = ElMessage
}

// 5. 使用路由（你原有代码已保留）
app.use(router)

import { createPinia } from 'pinia' // 导入 createPinia
const pinia = createPinia()
app.use(pinia)

// 补充：全局注册 Element Plus（新增，核心步骤，让所有组件生效 + 配置中文）
app.use(ElementPlus, {
  locale: zhCn, // 可选，配置中文显示，不想要可以删除这一行
});

// 6. 把应用挂载到 #app 元素上（你原有代码已保留）
app.mount('#app')

console.log('✅ Vue应用启动成功！访问 http://localhost:3000')
