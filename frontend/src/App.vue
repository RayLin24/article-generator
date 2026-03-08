<template>
  <div class="app-container">
    <div class="background-decoration">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
    </div>
    
    <el-container>
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo" @click="$router.push('/')">
            <div class="logo-icon">
              <el-icon size="22"><Document /></el-icon>
            </div>
            <span class="logo-text">AI每日文章</span>
          </div>
          
          <nav class="nav-menu">
            <router-link 
              v-for="category in categories" 
              :key="category.code"
              :to="`/category/${category.code}`"
              class="nav-item"
              :class="{ active: activeMenu === category.code }"
            >
              <span class="nav-text">{{ category.name }}</span>
              <span class="nav-indicator"></span>
            </router-link>
          </nav>
          
          <div class="user-area">
            <template v-if="userStore.isLoggedIn">
              <el-dropdown @command="handleUserCommand" trigger="click">
                <div class="user-avatar-wrapper">
                  <div class="user-avatar">
                    <el-icon><User /></el-icon>
                  </div>
                  <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username }}</span>
                  <el-tag v-if="userStore.isAdmin" size="small" class="admin-tag">管理员</el-tag>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="userStore.isAdmin" command="admin">
                      <el-icon><Setting /></el-icon>管理后台
                    </el-dropdown-item>
                    <el-dropdown-item command="logout" divided>
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" class="login-btn" @click="$router.push('/login')">
                <el-icon><User /></el-icon>
                <span>登录</span>
              </el-button>
            </template>
          </div>
        </div>
      </el-header>
      
      <el-main class="app-main">
        <transition name="fade-slide" mode="out-in">
          <router-view />
        </transition>
      </el-main>
      
      <el-footer class="app-footer">
        <div class="footer-content">
          <div class="footer-gradient-line"></div>
          <p>
            <span class="footer-icon">AI</span>
            AI每日文章生成系统 - 基于智谱AI GLM
          </p>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCategoryStore } from '@/stores/category'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const categoryStore = useCategoryStore()
const userStore = useUserStore()

const categories = computed(() => categoryStore.categories)
const activeMenu = computed(() => {
  const code = route.params.code as string
  return code || ''
})

const handleUserCommand = (command: string) => {
  if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/')
    }).catch(() => {})
  }
}

onMounted(() => {
  categoryStore.fetchCategories()
  userStore.initUser()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: var(--bg-primary);
  color: var(--text-primary);
  overflow-x: hidden;
}

.app-container {
  min-height: 100vh;
  position: relative;
}

/* Background Decoration */
.background-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 8s ease-in-out infinite;
}

.blob-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.blob-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #00d4ff, #667eea);
  bottom: 20%;
  left: -50px;
  animation-delay: -3s;
}

.blob-3 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, #f472b6, #764ba2);
  top: 50%;
  right: 10%;
  animation-delay: -5s;
}

/* Header */
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 70px;
  padding: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 30px rgba(102, 126, 234, 0.1);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 32px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform var(--transition-normal);
}

.logo:hover {
  transform: scale(1.02);
}

.logo-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-gradient);
  border-radius: var(--radius-md);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Navigation */
.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.nav-item {
  position: relative;
  padding: 8px 16px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  overflow: hidden;
}

.nav-item:hover {
  color: var(--text-primary);
  background: rgba(102, 126, 234, 0.08);
}

.nav-item.active {
  color: #667eea;
  background: rgba(102, 126, 234, 0.12);
}

.nav-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: var(--primary-gradient);
  border-radius: 2px;
  transition: all var(--transition-normal);
  transform: translateX(-50%);
}

.nav-item:hover .nav-indicator,
.nav-item.active .nav-indicator {
  width: 60%;
}

/* User Area */
.user-area {
  display: flex;
  align-items: center;
}

.user-avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
  background: rgba(102, 126, 234, 0.08);
}

.user-avatar-wrapper:hover {
  background: rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.user-avatar {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-gradient);
  border-radius: var(--radius-sm);
  color: white;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.admin-tag {
  background: linear-gradient(135deg, #f472b6, #ec4899);
  border: none;
  color: white;
  font-weight: 500;
}

.login-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: var(--primary-gradient);
  border: none;
  border-radius: var(--radius-md);
  font-weight: 500;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  transition: all var(--transition-normal);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* Main Content */
.app-main {
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 150px);
  padding: 32px 24px;
}

/* Page Transition */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Footer */
.app-footer {
  position: relative;
  z-index: 1;
  height: auto;
  padding: 24px;
  background: transparent;
}

.footer-content {
  text-align: center;
}

.footer-gradient-line {
  width: 200px;
  height: 3px;
  margin: 0 auto 16px;
  background: var(--primary-gradient);
  border-radius: 3px;
}

.footer-content p {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.footer-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: var(--primary-gradient);
  border-radius: 6px;
  color: white;
  font-size: 10px;
  font-weight: 700;
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    gap: 16px;
    padding: 0 16px;
  }
  
  .nav-menu {
    display: none;
  }
  
  .logo-text {
    display: none;
  }
  
  .user-name {
    display: none;
  }
}
</style>
