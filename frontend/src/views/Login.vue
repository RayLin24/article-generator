<template>
  <div class="login-page">
    <!-- Background Decoration -->
    <div class="bg-decoration">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <!-- Login Card -->
    <div class="login-container">
      <div class="login-card">
        <!-- Logo Section -->
        <div class="logo-section">
          <div class="logo-icon">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <h1 class="logo-text">AI每日文章</h1>
          <p class="logo-subtitle">智能内容生成平台</p>
        </div>

        <!-- Form Section -->
        <div class="form-section">
          <transition name="slide-fade" mode="out-in">
            <!-- Login Form -->
            <div v-if="isLogin" key="login" class="form-container">
              <div class="form-header">
                <h2 class="form-title">欢迎回来</h2>
                <p class="form-desc">登录您的账号继续</p>
              </div>

              <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
                <el-form-item prop="username">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><User /></el-icon>
                    <el-input
                      v-model="loginForm.username"
                      placeholder="用户名"
                      size="large"
                      class="custom-input"
                    />
                  </div>
                </el-form-item>

                <el-form-item prop="password">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Lock /></el-icon>
                    <el-input
                      v-model="loginForm.password"
                      type="password"
                      placeholder="密码"
                      size="large"
                      show-password
                      class="custom-input"
                      @keyup.enter="handleLogin"
                    />
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button
                    type="primary"
                    size="large"
                    :loading="loading"
                    class="submit-btn"
                    @click="handleLogin"
                  >
                    <span>登录</span>
                    <el-icon v-if="!loading"><ArrowRight /></el-icon>
                  </el-button>
                </el-form-item>
              </el-form>

              <div class="switch-mode">
                <span class="switch-text">还没有账号？</span>
                <span class="switch-link" @click="switchToRegister">
                  立即注册
                  <el-icon><ArrowRight /></el-icon>
                </span>
              </div>
            </div>

            <!-- Register Form -->
            <div v-else key="register" class="form-container">
              <div class="form-header">
                <h2 class="form-title">创建账号</h2>
                <p class="form-desc">填写以下信息注册账号</p>
              </div>

              <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="0">
                <el-form-item prop="username">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><User /></el-icon>
                    <el-input
                      v-model="registerForm.username"
                      placeholder="用户名"
                      size="large"
                      class="custom-input"
                    />
                  </div>
                </el-form-item>

                <el-form-item prop="password">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Lock /></el-icon>
                    <el-input
                      v-model="registerForm.password"
                      type="password"
                      placeholder="密码"
                      size="large"
                      show-password
                      class="custom-input"
                    />
                  </div>
                </el-form-item>

                <el-form-item prop="confirmPassword">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Lock /></el-icon>
                    <el-input
                      v-model="registerForm.confirmPassword"
                      type="password"
                      placeholder="确认密码"
                      size="large"
                      show-password
                      class="custom-input"
                      @keyup.enter="handleRegister"
                    />
                  </div>
                </el-form-item>

                <el-form-item prop="nickname">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Avatar /></el-icon>
                    <el-input
                      v-model="registerForm.nickname"
                      placeholder="昵称（可选）"
                      size="large"
                      class="custom-input"
                    />
                  </div>
                </el-form-item>

                <el-form-item prop="email">
                  <div class="input-wrapper">
                    <el-icon class="input-icon"><Message /></el-icon>
                    <el-input
                      v-model="registerForm.email"
                      placeholder="邮箱（可选）"
                      size="large"
                      class="custom-input"
                    />
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button
                    type="primary"
                    size="large"
                    :loading="loading"
                    class="submit-btn"
                    @click="handleRegister"
                  >
                    <span>注册</span>
                    <el-icon v-if="!loading"><Check /></el-icon>
                  </el-button>
                </el-form-item>
              </el-form>

              <div class="switch-mode">
                <span class="switch-text">已有账号？</span>
                <span class="switch-link" @click="switchToLogin">
                  去登录
                  <el-icon><ArrowRight /></el-icon>
                </span>
              </div>
            </div>
          </transition>
        </div>

        <!-- Footer -->
        <div class="card-footer">
          <span class="powered-by">Powered by</span>
          <span class="tech-name">智谱AI GLM</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, User, Lock, Avatar, Message, ArrowRight, Check } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/article'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const isLogin = ref(true)

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: ''
})

// 登录校验规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ]
}

// 确认密码校验
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 注册校验规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 切换到注册页面
const switchToRegister = () => {
  isLogin.value = false
}

// 切换到登录页面
const switchToLogin = () => {
  isLogin.value = true
}

// 清空登录表单
const resetLoginForm = () => {
  loginForm.username = ''
  loginForm.password = ''
  loginFormRef.value?.clearValidate()
}

// 清空注册表单
const resetRegisterForm = () => {
  registerForm.username = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.nickname = ''
  registerForm.email = ''
  registerFormRef.value?.clearValidate()
}

// 登录
const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 注册
const handleRegister = async () => {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname || undefined,
      email: registerForm.email || undefined
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      // 清空注册表单
      resetRegisterForm()
      // 切换到登录页面
      isLogin.value = true
      // 预填充用户名
      loginForm.username = registerForm.username
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

/* Background Decoration */
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
  width: 500px;
  height: 500px;
  top: -200px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.shape-2 {
  width: 400px;
  height: 400px;
  bottom: -150px;
  left: -100px;
  animation: float 10s ease-in-out infinite reverse;
}

.shape-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 10%;
  animation: float 6s ease-in-out infinite;
}

.shape-4 {
  width: 150px;
  height: 150px;
  bottom: 30%;
  right: 15%;
  animation: float 7s ease-in-out infinite reverse;
}

/* Login Container */
.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

/* Logo Section */
.logo-section {
  text-align: center;
  padding: 40px 40px 32px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: var(--primary-gradient);
  border-radius: var(--radius-lg);
  color: white;
  margin-bottom: 16px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.logo-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

/* Form Section */
.form-section {
  padding: 0 40px 32px;
  min-height: 380px;
}

.form-container {
  width: 100%;
}

.form-header {
  text-align: center;
  margin-bottom: 28px;
}

.form-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.form-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

/* Input Styles */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: var(--text-muted);
  z-index: 1;
  transition: color var(--transition-fast);
}

.input-wrapper:focus-within .input-icon {
  color: #667eea;
}

.custom-input :deep(.el-input__wrapper) {
  padding-left: 48px;
  border-radius: var(--radius-md);
  box-shadow: none;
  border: 2px solid transparent;
  background: var(--bg-secondary);
  transition: all var(--transition-normal);
}

.custom-input :deep(.el-input__wrapper:hover) {
  background: var(--bg-primary);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background: var(--bg-primary);
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.custom-input :deep(.el-input__inner) {
  height: 48px;
}

/* Submit Button */
.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  background: var(--primary-gradient);
  border: none;
  border-radius: var(--radius-md);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.35);
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 40px rgba(102, 126, 234, 0.45);
}

.submit-btn:active {
  transform: translateY(0);
}

/* Switch Mode */
.switch-mode {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--bg-secondary);
}

.switch-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.switch-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.switch-link:hover {
  color: #764ba2;
}

.switch-link .el-icon {
  transition: transform var(--transition-fast);
}

.switch-link:hover .el-icon {
  transform: translateX(4px);
}

/* Page Transition Animation */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* Card Footer */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.03) 0%, rgba(118, 75, 162, 0.03) 100%);
  border-top: 1px solid var(--bg-secondary);
}

.powered-by {
  font-size: 12px;
  color: var(--text-muted);
}

.tech-name {
  font-size: 12px;
  font-weight: 600;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Responsive */
@media (max-width: 480px) {
  .logo-section {
    padding: 32px 24px 24px;
  }
  
  .form-section {
    padding: 0 24px 24px;
  }
}
</style>
