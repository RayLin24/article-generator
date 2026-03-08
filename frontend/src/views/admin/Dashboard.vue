<template>
  <div class="dashboard">
    <h2>管理仪表盘</h2>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon size="24"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.articleCount }}</div>
              <div class="stat-label">文章总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon size="24"><Menu /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.categoryCount }}</div>
              <div class="stat-label">栏目总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon size="24"><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.viewCount }}</div>
              <div class="stat-label">总阅读量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="action-card">
      <template #header>
        <span>快捷操作</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-button type="primary" :loading="generating" @click="generateAll">
            <el-icon><MagicStick /></el-icon>
            一键生成所有栏目文章
          </el-button>
        </el-col>
        <el-col :span="12">
          <el-button @click="$router.push('/admin/articles')">
            <el-icon><Document /></el-icon>
            文章管理
          </el-button>
        </el-col>
      </el-row>

      <el-divider />

      <h4>按栏目生成</h4>
      <el-row :gutter="10" style="margin-top: 16px;">
        <el-col :span="4" v-for="category in categories" :key="category.id">
          <el-button
            :loading="generatingCategory === category.id"
            @click="generateCategory(category.id)"
          >
            {{ category.name }}
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useCategoryStore } from '@/stores/category'
import { generateAllArticles, generateArticleForCategory, getArticles } from '@/api/article'

const categoryStore = useCategoryStore()
const categories = computed(() => categoryStore.categories)

const stats = ref({
  articleCount: 0,
  categoryCount: 0,
  userCount: 0,
  viewCount: 0
})

const generating = ref(false)
const generatingCategory = ref<number | null>(null)

const loadStats = async () => {
  try {
    const res = await getArticles({ page: 1, size: 1 })
    stats.value.articleCount = res.data?.total || 0
    stats.value.categoryCount = categories.value.length
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const generateAll = async () => {
  generating.value = true
  try {
    const res = await generateAllArticles()
    if (res.code === 200) {
      ElMessage.success('文章生成任务已执行')
      loadStats()
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    generating.value = false
  }
}

const generateCategory = async (categoryId: number) => {
  generatingCategory.value = categoryId
  try {
    const res = await generateArticleForCategory(categoryId)
    if (res.code === 200) {
      ElMessage.success('文章生成成功')
      loadStats()
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    generatingCategory.value = null
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard h2 {
  margin-bottom: 20px;
  color: #303133;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.action-card {
  margin-top: 20px;
}

.action-card h4 {
  margin: 0;
  color: #606266;
}
</style>
