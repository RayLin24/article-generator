<template>
  <div class="home-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
        <div class="shape shape-4"></div>
      </div>
      <div class="hero-content">
        <div class="hero-badge">
          <el-icon><MagicStick /></el-icon>
          <span>AI Powered</span>
        </div>
        <h1 class="hero-title">
          AI每日文章
          <span class="title-highlight">生成系统</span>
        </h1>
        <p class="hero-description">
          基于智谱AI GLM，每日自动生成历史故事、爱情故事、经典寓言、热点新闻、科技热点等精彩内容
        </p>
        <div class="hero-actions">
          <el-button type="primary" class="hero-btn primary" @click="scrollToCategories">
            <el-icon><Compass /></el-icon>
            探索内容
          </el-button>
          <el-button class="hero-btn secondary">
            <el-icon><InfoFilled /></el-icon>
            了解更多
          </el-button>
        </div>
        <div class="hero-stats">
        <div class="stat-item">
          <span class="stat-number">{{ categoryTotal > 0 ? `${categoryTotal}+` : '5+' }}</span>
            <span class="stat-label">内容栏目</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">每日</span>
            <span class="stat-label">更新</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">AI</span>
            <span class="stat-label">智能生成</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Categories Section -->
    <section class="categories-section" ref="categoriesRef">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">
            <el-icon><Grid /></el-icon>
          </span>
          内容栏目
        </h2>
        <p class="section-subtitle">探索多元化的AI生成内容</p>
      </div>
      <div class="section-content" v-loading="categoriesLoading">
        <div class="categories-grid" v-if="categories.length > 0">
          <div
            v-for="(category, index) in categories"
            :key="category.id"
            class="category-card"
            :style="{ '--delay': `${index * 0.1}s` }"
            @click="goToCategory(category.code)"
          >
            <div class="card-glow"></div>
            <div class="card-content">
              <div class="category-icon-wrapper">
                <div class="icon-bg"></div>
                <el-icon class="category-icon" :size="32">
                  <component :is="getCategoryIcon(category.code)" />
                </el-icon>
              </div>
              <h3 class="category-name">{{ category.name }}</h3>
              <p class="category-desc">每日更新精彩内容</p>
              <div class="card-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else-if="!categoriesLoading" description="暂无栏目" />
      </div>
      <div class="section-pagination" v-if="categoryTotal > categoryPageSize">
        <el-pagination
          v-model:current-page="categoryCurrentPage"
          :page-size="categoryPageSize"
          :total="categoryTotal"
          layout="prev, pager, next"
          @current-change="handleCategoryPageChange"
        />
      </div>
    </section>

    <!-- Latest Articles Section -->
    <section class="latest-section" ref="latestSectionRef">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">
            <el-icon><Document /></el-icon>
          </span>
          最新文章
        </h2>
        <p class="section-subtitle">发现最新的AI创作内容</p>
      </div>
      
      <el-skeleton :loading="articlesLoading" animated :rows="5">
        <div class="articles-grid">
          <article 
            v-for="(article, index) in articles" 
            :key="article.id"
            class="article-card"
            :style="{ '--delay': `${index * 0.1}s` }"
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
                <el-icon :size="40"><Picture /></el-icon>
              </div>
              <div class="cover-overlay"></div>
              <div class="article-category-badge">
                {{ article.categoryName }}
              </div>
            </div>
            <div class="article-body">
              <div class="article-meta">
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  {{ article.publishDate }}
                </span>
                <span class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ article.viewCount }}
                </span>
              </div>
              <h3 class="article-title">{{ article.title }}</h3>
              <p class="article-summary">{{ article.summary }}</p>
              <div class="article-footer">
                <span class="read-more">
                  阅读全文
                  <el-icon><ArrowRight /></el-icon>
                </span>
              </div>
            </div>
          </article>
        </div>
      </el-skeleton>
      
      <el-empty v-if="!articlesLoading && articles.length === 0" description="暂无文章" />
      <div class="section-pagination" v-if="articleTotal > articlePageSize">
        <el-pagination
          v-model:current-page="articleCurrentPage"
          :page-size="articlePageSize"
          :total="articleTotal"
          layout="prev, pager, next"
          @current-change="handleArticlePageChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Picture, MagicStick, Compass, InfoFilled, Grid, ArrowRight, Calendar, View, Document } from '@element-plus/icons-vue'
import { getArticles, getCategoriesPage } from '@/api/article'
import type { Article, Category } from '@/api/article'

const router = useRouter()
const categoriesRef = ref<HTMLElement | null>(null)
const latestSectionRef = ref<HTMLElement | null>(null)

const categories = ref<Category[]>([])
const categoriesLoading = ref(false)
const categoryCurrentPage = ref(1)
const categoryPageSize = 10
const categoryTotal = ref(0)

const articles = ref<Article[]>([])
const articlesLoading = ref(false)
const articleCurrentPage = ref(1)
const articlePageSize = 6
const articleTotal = ref(0)

const getCategoryIcon = (code: string) => {
  const icons: Record<string, string> = {
    history: 'Clock',
    love: 'Heart',
    fable: 'ChatDotRound',
    news: 'Notification',
    tech: 'Monitor'
  }
  return icons[code] || 'Document'
}

const scrollToCategories = () => {
  categoriesRef.value?.scrollIntoView({ behavior: 'smooth' })
}

const goToCategory = (code: string) => {
  router.push(`/category/${code}`)
}

const goToArticle = (id: number) => {
  router.push(`/article/${id}`)
}

const fetchCategories = async () => {
  categoriesLoading.value = true
  try {
    const res = await getCategoriesPage(categoryCurrentPage.value, categoryPageSize)
    categories.value = res.data?.records || []
    categoryTotal.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  } finally {
    categoriesLoading.value = false
  }
}

const fetchLatestArticles = async () => {
  articlesLoading.value = true
  try {
    const res = await getArticles({ page: articleCurrentPage.value, size: articlePageSize })
    articles.value = res.data?.records || []
    articleTotal.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch articles:', error)
  } finally {
    articlesLoading.value = false
  }
}

const handleCategoryPageChange = (page: number) => {
  categoryCurrentPage.value = page
  fetchCategories()
  categoriesRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handleArticlePageChange = (page: number) => {
  articleCurrentPage.value = page
  fetchLatestArticles()
  latestSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(() => {
  fetchCategories()
  fetchLatestArticles()
})
</script>

<style scoped>
.home-page {
  max-width: 1280px;
  margin: 0 auto;
  padding-bottom: 40px;
}

/* Hero Section */
.hero-section {
  position: relative;
  padding: 80px 40px;
  margin-bottom: 60px;
  background: var(--primary-gradient);
  border-radius: var(--radius-xl);
  overflow: hidden;
  min-height: 400px;
  display: flex;
  align-items: center;
}

.hero-bg-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
  animation-delay: 0s;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: 10%;
  animation-delay: -2s;
}

.shape-3 {
  width: 150px;
  height: 150px;
  top: 30%;
  right: 20%;
  animation-delay: -4s;
}

.shape-4 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 30%;
  animation-delay: -1s;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 600px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 50px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 24px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  color: white;
  line-height: 1.2;
  margin-bottom: 20px;
}

.title-highlight {
  display: block;
  background: linear-gradient(90deg, #00d4ff, #fff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-description {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.7;
  margin-bottom: 32px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
}

.hero-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-size: 15px;
  transition: all var(--transition-normal);
}

.hero-btn.primary {
  background: white;
  color: #667eea;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.hero-btn.primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}

.hero-btn.secondary {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
}

.hero-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-3px);
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: white;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

/* Section Styles */
.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: var(--primary-gradient);
  border-radius: var(--radius-md);
  color: white;
}

.section-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
}

.section-content {
  min-height: 120px;
}

.section-pagination {
  display: flex;
  justify-content: center;
  margin-top: 28px;
  padding: 20px 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
}

/* Categories Grid */
.categories-section {
  margin-bottom: 60px;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
}

.category-card {
  position: relative;
  padding: 32px 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  cursor: pointer;
  overflow: hidden;
  transition: all var(--transition-normal);
  animation: fade-in-up 0.6s ease backwards;
  animation-delay: var(--delay);
}

.category-card::before {
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

.category-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
}

.category-card:hover::before {
  opacity: 1;
}

.card-glow {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 150px;
  height: 150px;
  background: var(--primary-gradient);
  filter: blur(60px);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.category-card:hover .card-glow {
  opacity: 0.15;
}

.card-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.category-icon-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  margin-bottom: 20px;
}

.icon-bg {
  position: absolute;
  inset: 0;
  background: var(--primary-gradient);
  border-radius: var(--radius-lg);
  opacity: 0.1;
  transition: all var(--transition-normal);
}

.category-card:hover .icon-bg {
  opacity: 0.2;
  transform: scale(1.1);
}

.category-icon {
  position: relative;
  color: #667eea;
  transition: transform var(--transition-normal);
}

.category-card:hover .category-icon {
  transform: scale(1.1);
}

.category-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.category-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.card-arrow {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border-radius: 50%;
  color: var(--text-muted);
  opacity: 0;
  transform: translateX(10px);
  transition: all var(--transition-normal);
}

.category-card:hover .card-arrow {
  opacity: 1;
  transform: translateX(0);
}

/* Articles Grid */
.latest-section {
  margin-bottom: 40px;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 28px;
}

.article-card {
  position: relative;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  animation: fade-in-up 0.6s ease backwards;
  animation-delay: var(--delay);
}

.article-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
}

.article-cover-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.article-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.article-card:hover .article-cover {
  transform: scale(1.08);
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
  background: linear-gradient(to top, rgba(0,0,0,0.4) 0%, transparent 50%);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.article-card:hover .cover-overlay {
  opacity: 1;
}

.article-category-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  padding: 6px 14px;
  background: var(--primary-gradient);
  color: white;
  font-size: 12px;
  font-weight: 600;
  border-radius: 50px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.article-body {
  padding: 20px;
}

.article-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
}

.article-title {
  font-size: 18px;
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

.article-card:hover .article-title {
  color: #667eea;
}

.article-summary {
  font-size: 14px;
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

.read-more {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
  color: #667eea;
  opacity: 0;
  transform: translateX(-10px);
  transition: all var(--transition-normal);
}

.article-card:hover .read-more {
  opacity: 1;
  transform: translateX(0);
}

:deep(.el-pagination) {
  --el-pagination-button-bg-color: var(--bg-secondary);
  --el-pagination-hover-color: #667eea;
}

:deep(.el-pagination .is-active) {
  background: var(--primary-gradient) !important;
  color: white !important;
}

/* Responsive */
@media (max-width: 768px) {
  .hero-section {
    padding: 48px 24px;
    min-height: auto;
  }
  
  .hero-title {
    font-size: 32px;
  }
  
  .hero-description {
    font-size: 16px;
  }
  
  .hero-actions {
    flex-direction: column;
  }
  
  .hero-stats {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .stat-divider {
    display: none;
  }
  
  .categories-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .articles-grid {
    grid-template-columns: 1fr;
  }
}
</style>
