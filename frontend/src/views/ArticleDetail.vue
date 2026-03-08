<template>
  <div class="article-detail-page">
    <el-skeleton :loading="loading" animated :rows="20">
      <template v-if="article">
        <!-- Article Header -->
        <header class="article-header">
          <div class="breadcrumb">
            <router-link to="/" class="breadcrumb-item">
              <el-icon><HomeFilled /></el-icon>
              首页
            </router-link>
            <el-icon class="breadcrumb-separator"><ArrowRight /></el-icon>
            <router-link 
              v-if="article.categoryCode" 
              :to="`/category/${article.categoryCode}`" 
              class="breadcrumb-item"
            >
              {{ article.categoryName }}
            </router-link>
            <el-icon class="breadcrumb-separator"><ArrowRight /></el-icon>
            <span class="breadcrumb-current">正文</span>
          </div>
          
          <div class="category-badge">
            <el-icon><FolderOpened /></el-icon>
            {{ article.categoryName }}
          </div>
          
          <h1 class="article-title">{{ article.title }}</h1>
          
          <div class="article-meta">
            <div class="meta-item">
              <div class="meta-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="meta-content">
                <span class="meta-label">发布日期</span>
                <span class="meta-value">{{ article.publishDate }}</span>
              </div>
            </div>
            <div class="meta-divider"></div>
            <div class="meta-item">
              <div class="meta-icon">
                <el-icon><View /></el-icon>
              </div>
              <div class="meta-content">
                <span class="meta-label">阅读量</span>
                <span class="meta-value">{{ article.viewCount }}</span>
              </div>
            </div>
          </div>
        </header>

        <!-- Cover Image -->
        <div v-if="article.coverImage" class="article-cover">
          <div class="cover-wrapper">
            <el-image 
              :src="article.coverImage"
              :preview-src-list="[article.coverImage]"
              fit="cover"
              class="cover-image"
            />
            <div class="cover-gradient"></div>
          </div>
        </div>

        <!-- Article Content -->
        <article class="article-content-wrapper">
          <div class="content-decoration">
            <div class="decoration-line"></div>
          </div>
          <div class="article-content" v-html="formattedContent"></div>
        </article>

        <!-- Article Footer -->
        <footer class="article-footer">
          <div class="footer-gradient"></div>
          <div class="footer-content">
            <el-button class="back-btn" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              <span>返回列表</span>
            </el-button>
            <div class="footer-actions">
              <el-button class="action-btn" circle>
                <el-icon><Star /></el-icon>
              </el-button>
              <el-button class="action-btn" circle>
                <el-icon><Share /></el-icon>
              </el-button>
            </div>
          </div>
        </footer>
      </template>

      <el-empty v-if="!loading && !article" description="文章不存在">
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </el-empty>
    </el-skeleton>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, ArrowRight, ArrowLeft, Calendar, View, FolderOpened, Star, Share } from '@element-plus/icons-vue'
import { getArticleById } from '@/api/article'
import type { Article } from '@/api/article'

const router = useRouter()
const route = useRoute()

const article = ref<Article | null>(null)
const loading = ref(false)

const articleId = computed(() => Number(route.params.id))

const formattedContent = computed(() => {
  if (!article.value?.content) return ''
  let content = article.value.content
  
  content = content.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (match, alt, url) => {
    return `<div class="content-image"><img src="${url}" alt="${alt}" /></div>`
  })
  
  return content
    .split('\n')
    .map(p => {
      if (p.trim().startsWith('<div class="content-image">')) {
        return p
      }
      if (p.trim() === '') {
        return ''
      }
      return `<p>${p}</p>`
    })
    .join('')
})

const goBack = () => {
  if (article.value?.categoryCode) {
    router.push(`/category/${article.value.categoryCode}`)
  } else {
    router.push('/')
  }
}

const fetchArticle = async () => {
  loading.value = true
  try {
    const res = await getArticleById(articleId.value)
    article.value = res.data
  } catch (error) {
    console.error('Failed to fetch article:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchArticle()
})
</script>

<style scoped>
.article-detail-page {
  max-width: 860px;
  margin: 0 auto;
}

/* Breadcrumb */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: color var(--transition-fast);
}

.breadcrumb-item:hover {
  color: #667eea;
}

.breadcrumb-separator {
  color: var(--text-muted);
  font-size: 12px;
}

.breadcrumb-current {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
}

/* Article Header */
.article-header {
  margin-bottom: 32px;
  animation: fade-in-up 0.6s ease;
}

.category-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--primary-gradient);
  color: white;
  font-size: 13px;
  font-weight: 500;
  border-radius: 50px;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.25);
}

.article-title {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
  margin-bottom: 24px;
  background: linear-gradient(135deg, var(--text-primary) 0%, #667eea 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 20px 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: var(--radius-md);
  color: #667eea;
}

.meta-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.meta-label {
  font-size: 12px;
  color: var(--text-muted);
}

.meta-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.meta-divider {
  width: 1px;
  height: 40px;
  background: var(--bg-secondary);
}

/* Cover Image */
.article-cover {
  margin-bottom: 40px;
  animation: fade-in-up 0.6s ease 0.1s backwards;
}

.cover-wrapper {
  position: relative;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.cover-image {
  width: 100%;
  height: 450px;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.cover-wrapper:hover .cover-image {
  transform: scale(1.02);
}

.cover-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.3) 0%, transparent 30%);
  pointer-events: none;
}

/* Article Content */
.article-content-wrapper {
  position: relative;
  margin-bottom: 48px;
  animation: fade-in-up 0.6s ease 0.2s backwards;
}

.content-decoration {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
}

.decoration-line {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100px;
  background: var(--primary-gradient);
  border-radius: 4px;
}

.article-content {
  padding-left: 32px;
  font-size: 17px;
  line-height: 2;
  color: var(--text-primary);
}

.article-content :deep(p) {
  margin-bottom: 20px;
  text-indent: 2em;
  text-align: justify;
}

.article-content :deep(h2) {
  font-size: 24px;
  font-weight: 700;
  margin: 40px 0 20px;
  color: var(--text-primary);
  padding-left: 16px;
  border-left: 4px solid #667eea;
}

.article-content :deep(h3) {
  font-size: 20px;
  font-weight: 600;
  margin: 32px 0 16px;
  color: var(--text-primary);
}

.article-content :deep(.content-image) {
  margin: 32px 0;
  text-align: center;
}

.article-content :deep(.content-image img) {
  max-width: 100%;
  height: auto;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  transition: all var(--transition-normal);
}

.article-content :deep(.content-image img:hover) {
  transform: scale(1.02);
  box-shadow: var(--shadow-lg);
}

.article-content :deep(blockquote) {
  margin: 24px 0;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
  border-left: 4px solid #667eea;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  font-style: italic;
  color: var(--text-secondary);
}

/* Article Footer */
.article-footer {
  position: relative;
  padding: 32px;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  animation: fade-in-up 0.6s ease 0.3s backwards;
}

.footer-gradient {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 150px;
  height: 4px;
  background: var(--primary-gradient);
  border-radius: 4px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 28px;
  background: var(--primary-gradient);
  border: none;
  border-radius: var(--radius-md);
  color: white;
  font-weight: 500;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  transition: all var(--transition-normal);
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.footer-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  width: 44px;
  height: 44px;
  background: var(--bg-secondary);
  border: none;
  color: var(--text-secondary);
  transition: all var(--transition-normal);
}

.action-btn:hover {
  background: var(--primary-gradient);
  color: white;
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 768px) {
  .article-title {
    font-size: 26px;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .meta-divider {
    display: none;
  }
  
  .cover-image {
    height: 280px;
  }
  
  .article-content {
    padding-left: 20px;
    font-size: 16px;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 20px;
  }
}
</style>
