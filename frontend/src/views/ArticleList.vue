<template>
  <div class="article-list-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">
            <el-icon><HomeFilled /></el-icon>
            首页
          </router-link>
          <el-icon class="breadcrumb-separator"><ArrowRight /></el-icon>
          <span class="breadcrumb-current">{{ categoryName }}</span>
        </div>
        <h1 class="page-title">
          <span class="title-icon">
            <el-icon><FolderOpened /></el-icon>
          </span>
          {{ categoryName }}
        </h1>
        <p class="page-description">探索{{ categoryName }}的精彩内容，每日更新</p>
      </div>
    </div>

    <!-- Articles List -->
    <el-skeleton :loading="loading" animated :rows="10">
      <div class="articles-container" v-if="articles.length > 0">
        <article 
          v-for="(article, index) in articles" 
          :key="article.id"
          class="article-item"
          :style="{ '--delay': `${index * 0.08}s` }"
          @click="goToArticle(article.id)"
        >
          <div class="article-cover-wrapper">
            <el-image
              v-if="article.coverImage"
              :src="article.coverImage"
              fit="cover"
              class="article-cover"
            />
            <div class="article-cover article-cover-placeholder" v-else>
              <el-icon :size="48"><Picture /></el-icon>
            </div>
            <div class="cover-overlay"></div>
          </div>
          
          <div class="article-content">
            <div class="article-header">
              <div class="article-meta">
                <span class="meta-item date">
                  <el-icon><Calendar /></el-icon>
                  {{ article.publishDate }}
                </span>
                <span class="meta-item views">
                  <el-icon><View /></el-icon>
                  {{ article.viewCount }} 阅读
                </span>
              </div>
            </div>
            
            <h3 class="article-title">{{ article.title }}</h3>
            <p class="article-summary">{{ article.summary }}</p>
            
            <div class="article-footer">
              <span class="read-more-btn">
                <span>阅读全文</span>
                <el-icon><ArrowRight /></el-icon>
              </span>
            </div>
          </div>
          
          <div class="article-indicator"></div>
        </article>
      </div>

      <el-empty v-if="!loading && articles.length === 0" description="暂无文章">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </el-empty>
    </el-skeleton>

    <!-- Pagination -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Picture, HomeFilled, ArrowRight, Calendar, View, FolderOpened } from '@element-plus/icons-vue'
import { getArticlesByCategory } from '@/api/article'
import { useCategoryStore } from '@/stores/category'
import type { Article } from '@/api/article'

const router = useRouter()
const route = useRoute()
const categoryStore = useCategoryStore()

const articles = ref<Article[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categoryCode = computed(() => route.params.code as string)
const categoryName = computed(() => {
  const category = categoryStore.categories.find(c => c.code === categoryCode.value)
  return category?.name || '文章列表'
})

const goToArticle = (id: number) => {
  router.push(`/article/${id}`)
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticlesByCategory(categoryCode.value, currentPage.value, pageSize.value)
    articles.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch articles:', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchArticles()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(categoryCode, () => {
  currentPage.value = 1
  fetchArticles()
})

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.article-list-page {
  max-width: 900px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  position: relative;
  padding: 48px 40px;
  margin-bottom: 40px;
  background: var(--primary-gradient);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.header-content {
  position: relative;
  z-index: 1;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 14px;
  transition: color var(--transition-fast);
}

.breadcrumb-item:hover {
  color: white;
}

.breadcrumb-separator {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

.breadcrumb-current {
  color: white;
  font-size: 14px;
  font-weight: 500;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 32px;
  font-weight: 700;
  color: white;
  margin-bottom: 12px;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-md);
}

.page-description {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
}

/* Articles Container */
.articles-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.article-item {
  position: relative;
  display: flex;
  gap: 24px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  cursor: pointer;
  overflow: hidden;
  transition: all var(--transition-normal);
  animation: fade-in-up 0.5s ease backwards;
  animation-delay: var(--delay);
}

.article-item::before {
  content: '';
  position: absolute;
  inset: 0;
  padding: 2px;
  border-radius: var(--radius-lg);
  background: var(--border-gradient);
  -webkit-mask: 
    linear-gradient(#fff 0 0) content-box, 
    linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.article-item:hover {
  transform: translateX(8px);
  box-shadow: var(--shadow-lg);
}

.article-item:hover::before {
  opacity: 1;
}

.article-cover-wrapper {
  position: relative;
  width: 220px;
  height: 150px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.article-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.article-item:hover .article-cover {
  transform: scale(1.05);
}

.article-cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
  color: #94a3b8;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, transparent 50%, rgba(102, 126, 234, 0.1) 100%);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.article-item:hover .cover-overlay {
  opacity: 1;
}

.article-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 4px 0;
}

.article-header {
  margin-bottom: 12px;
}

.article-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

.meta-item.date {
  color: #667eea;
}

.meta-item.views {
  color: var(--text-muted);
}

.article-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.5;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-normal);
}

.article-item:hover .article-title {
  color: #667eea;
}

.article-summary {
  flex: 1;
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 16px;
}

.article-footer {
  display: flex;
  justify-content: flex-end;
}

.read-more-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #667eea;
  opacity: 0;
  transform: translateX(-10px);
  transition: all var(--transition-normal);
}

.article-item:hover .read-more-btn {
  opacity: 1;
  transform: translateX(0);
}

.article-indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 0;
  background: var(--primary-gradient);
  border-radius: 2px;
  transition: height var(--transition-normal);
}

.article-item:hover .article-indicator {
  height: 60%;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 48px;
  padding: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
}

:deep(.el-pagination) {
  --el-pagination-button-bg-color: var(--bg-secondary);
  --el-pagination-hover-color: #667eea;
}

:deep(.el-pagination .is-active) {
  background: var(--primary-gradient) !important;
  color: white !important;
}

:deep(.el-pagination button:hover) {
  color: #667eea;
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    padding: 32px 24px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .article-item {
    flex-direction: column;
    gap: 16px;
  }
  
  .article-cover-wrapper {
    width: 100%;
    height: 180px;
  }
  
  .article-indicator {
    display: none;
  }
}
</style>
