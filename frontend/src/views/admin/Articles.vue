<template>
  <div class="articles-page">
    <div class="page-header">
      <h2>文章管理</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        新增文章
      </el-button>
    </div>

    <el-card>
      <el-table :data="articles" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <el-image 
              v-if="row.coverImage"
              :src="row.coverImage"
              :preview-src-list="[row.coverImage]"
              :z-index="9999"
              preview-teleported
              fit="cover"
              style="width: 80px; height: 50px; border-radius: 4px; cursor: pointer;"
            />
            <span v-else style="color: #999;">无封面</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="栏目" width="120" />
        <el-table-column prop="publishDate" label="发布日期" width="120" />
        <el-table-column prop="viewCount" label="阅读量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contentType" label="格式" width="90">
          <template #default="{ row }">
            <el-tag :type="row.contentType === 'html' ? 'warning' : ''" size="small">
              {{ row.contentType === 'html' ? '富文本' : 'Markdown' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="showPreviewDialog(row)">预览</el-button>
            <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadArticles"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑文章' : '新增文章'"
      width="1000px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="栏目" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择栏目" style="width: 100%;">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        
        <el-form-item label="封面图">
          <div class="cover-image-section">
            <el-image 
              v-if="form.coverImage"
              :src="form.coverImage"
              :preview-src-list="[form.coverImage]"
              :z-index="9999"
              preview-teleported
              fit="cover"
              class="cover-preview"
            />
            <div v-else class="cover-placeholder">
              <el-icon size="40"><Picture /></el-icon>
              <span>暂无封面</span>
            </div>
            <div class="cover-actions">
              <el-button type="primary" @click="refreshCoverImage" :loading="refreshingCover">
                <el-icon><Refresh /></el-icon>
                {{ form.coverImage ? '换一张' : '生成封面' }}
              </el-button>
              <el-input 
                v-model="form.coverImage" 
                placeholder="或输入图片URL" 
                style="margin-top: 10px;"
              />
            </div>
          </div>
        </el-form-item>
        
        <el-form-item label="编辑器">
          <el-radio-group v-model="form.contentType" @change="handleEditorTypeChange">
            <el-radio value="markdown">Markdown</el-radio>
            <el-radio value="html">富文本</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <div class="content-editor">
            <MarkdownEditor
              v-if="form.contentType === 'markdown'"
              ref="markdownEditorRef"
              v-model="form.content"
              placeholder="请输入文章内容，支持 Markdown 语法"
            />
            <RichTextEditor
              v-else
              ref="richTextEditorRef"
              v-model="form.content"
              placeholder="请输入文章内容"
            />
          </div>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="可选，不填则自动生成" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button @click="handlePreview">预览</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="文章预览"
      width="900px"
      destroy-on-close
      @opened="renderPreviewContent"
    >
      <div class="preview-container" v-if="previewArticle">
        <div class="preview-header">
          <span class="preview-category">{{ previewArticle.categoryName }}</span>
          <h2 class="preview-title">{{ previewArticle.title }}</h2>
          <div class="preview-meta">
            <span><el-icon><Calendar /></el-icon> {{ previewArticle.publishDate }}</span>
            <span><el-icon><View /></el-icon> {{ previewArticle.viewCount }} 阅读</span>
          </div>
        </div>
        <el-image
          v-if="previewArticle.coverImage"
          :src="previewArticle.coverImage"
          fit="cover"
          class="preview-cover"
        />
        <div ref="previewContentRef" class="preview-content vditor-reset" v-html="previewContent"></div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Refresh, Plus, Calendar, View } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useCategoryStore } from '@/stores/category'
import { getAdminArticles, createArticle, updateArticle, deleteArticle, getRandomImage, type Article } from '@/api/article'
import MarkdownEditor from '@/components/MarkdownEditor.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { marked } from 'marked'

const categoryStore = useCategoryStore()
const categories = computed(() => categoryStore.categories)

const loading = ref(false)
const articles = ref<Article[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const refreshingCover = ref(false)
const formRef = ref<FormInstance>()
const markdownEditorRef = ref<InstanceType<typeof MarkdownEditor> | null>(null)
const richTextEditorRef = ref<InstanceType<typeof RichTextEditor> | null>(null)

const form = reactive({
  categoryId: null as number | null,
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  status: 1,
  contentType: 'markdown'
})

const rules: FormRules = {
  categoryId: [{ required: true, message: '请选择栏目', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 预览相关
const previewVisible = ref(false)
const previewArticle = ref<Article | null>(null)
const previewContentRef = ref<HTMLElement | null>(null)

const previewContent = ref('')

const renderPreviewContent = () => {
  if (!previewArticle.value) return
  
  const content = previewArticle.value.content || ''
  
  if (previewArticle.value.contentType === 'html') {
    previewContent.value = content
    return
  }
  
  // Markdown 内容使用 marked 渲染
  try {
    // 配置 marked
    marked.setOptions({
      breaks: true,
      gfm: true
    })
    previewContent.value = marked.parse(content) as string
  } catch (error) {
    console.error('Failed to render markdown:', error)
    // 降级处理
    previewContent.value = content
      .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<div class="content-image"><img src="$2" alt="$1" /></div>')
      .split('\n')
      .map(p => {
        if (p.trim().startsWith('<div class="content-image">')) return p
        if (p.trim() === '') return ''
        return `<p>${p}</p>`
      })
      .join('')
  }
}

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getAdminArticles(currentPage.value, pageSize.value)
    if (res.code === 200) {
      articles.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('Failed to load articles:', error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.categoryId = null
  form.title = ''
  form.content = ''
  form.summary = ''
  form.coverImage = ''
  form.status = 1
  form.contentType = 'markdown'
  editId.value = null
  isEdit.value = false
}

const showCreateDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const showEditDialog = (article: Article) => {
  isEdit.value = true
  editId.value = article.id
  form.categoryId = article.categoryId
  form.title = article.title
  form.content = article.content
  form.summary = article.summary || ''
  form.coverImage = article.coverImage || ''
  form.status = article.status
  form.contentType = article.contentType || 'markdown'
  dialogVisible.value = true
}

const handleEditorTypeChange = () => {
  form.content = ''
}

const refreshCoverImage = async () => {
  refreshingCover.value = true
  try {
    const res = await getRandomImage(1200, 600)
    if (res.code === 200 && res.data) {
      form.coverImage = res.data.url
      ElMessage.success('封面图已更新')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取图片失败')
  } finally {
    refreshingCover.value = false
  }
}

const handlePreview = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  previewArticle.value = {
    id: 0,
    categoryId: form.categoryId || 0,
    title: form.title,
    content: form.content,
    summary: form.summary || '',
    coverImage: form.coverImage,
    publishDate: new Date().toLocaleDateString(),
    viewCount: 0,
    status: form.status,
    contentType: form.contentType,
    categoryName: categories.value.find(c => c.id === form.categoryId)?.name || '',
    categoryCode: ''
  }
  previewContent.value = form.contentType === 'html' ? form.content : ''
  previewVisible.value = true
}

const showPreviewDialog = (article: Article) => {
  previewArticle.value = { ...article }
  previewContent.value = article.contentType === 'html' ? article.content : ''
  previewVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      const res = await updateArticle(editId.value, {
        categoryId: form.categoryId!,
        title: form.title,
        content: form.content,
        summary: form.summary || undefined,
        coverImage: form.coverImage || undefined,
        status: form.status,
        contentType: form.contentType
      })
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadArticles()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res = await createArticle({
        categoryId: form.categoryId!,
        title: form.title,
        content: form.content,
        summary: form.summary || undefined,
        coverImage: form.coverImage || undefined,
        status: form.status,
        contentType: form.contentType
      })
      if (res.code === 200) {
        ElMessage.success('创建成功')
        dialogVisible.value = false
        loadArticles()
      } else {
        ElMessage.error(res.message || '创建失败')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    const res = await deleteArticle(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadArticles()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.articles-page h2 {
  margin: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.cover-image-section {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.cover-preview {
  width: 200px;
  height: 100px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
}

.cover-placeholder {
  width: 200px;
  height: 100px;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.cover-actions {
  flex: 1;
}

.content-editor {
  width: 100%;
}

/* 预览样式 */
.preview-container {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.preview-header {
  text-align: center;
  margin-bottom: 24px;
}

.preview-category {
  display: inline-block;
  padding: 6px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 13px;
  border-radius: 20px;
  margin-bottom: 16px;
}

.preview-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 16px;
}

.preview-meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  color: #909399;
  font-size: 14px;
}

.preview-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.preview-cover {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  margin-bottom: 24px;
  object-fit: cover;
}

.preview-content {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}

.preview-content.vditor-reset :deep(p) {
  margin-bottom: 16px;
  text-indent: 2em;
  text-align: justify;
}

.preview-content.vditor-reset :deep(h1),
.preview-content.vditor-reset :deep(h2) {
  font-size: 22px;
  font-weight: 600;
  margin: 24px 0 16px;
  padding-left: 12px;
  border-left: 4px solid #667eea;
  border-bottom: none;
}

.preview-content.vditor-reset :deep(h3) {
  font-size: 18px;
  font-weight: 600;
  margin: 20px 0 12px;
  border-bottom: none;
}

.preview-content.vditor-reset :deep(h4) {
  font-size: 16px;
  font-weight: 600;
  margin: 16px 0 8px;
}

.preview-content.vditor-reset :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin: 16px auto;
  display: block;
}

.preview-content.vditor-reset :deep(ul),
.preview-content.vditor-reset :deep(ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.preview-content.vditor-reset :deep(li) {
  margin-bottom: 8px;
}

.preview-content.vditor-reset :deep(blockquote) {
  margin: 16px 0;
  padding: 16px 20px;
  background: #f5f7fa;
  border-left: 4px solid #667eea;
  border-radius: 0 8px 8px 0;
}

.preview-content.vditor-reset :deep(code:not(.hljs)) {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
}

.preview-content.vditor-reset :deep(pre) {
  background: #282c34;
  color: #abb2bf;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.preview-content.vditor-reset :deep(pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}

.preview-content.vditor-reset :deep(table) {
  width: 100%;
  margin: 16px 0;
  border-collapse: collapse;
}

.preview-content.vditor-reset :deep(th),
.preview-content.vditor-reset :deep(td) {
  border: 1px solid #ebeef5;
  padding: 10px 14px;
  text-align: left;
}

.preview-content.vditor-reset :deep(th) {
  background: #f5f7fa;
  font-weight: 600;
}

.preview-content.vditor-reset :deep(hr) {
  border: none;
  height: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: 24px 0;
}

.preview-content :deep(p) {
  margin-bottom: 16px;
  text-indent: 2em;
  text-align: justify;
}

.preview-content :deep(h2) {
  font-size: 22px;
  font-weight: 600;
  margin: 24px 0 16px;
  padding-left: 12px;
  border-left: 4px solid #667eea;
}

.preview-content :deep(h3) {
  font-size: 18px;
  font-weight: 600;
  margin: 20px 0 12px;
}

.preview-content :deep(.content-image) {
  margin: 24px 0;
  text-align: center;
}

.preview-content :deep(.content-image img) {
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.preview-content :deep(ul),
.preview-content :deep(ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.preview-content :deep(li) {
  margin-bottom: 8px;
}

.preview-content :deep(blockquote) {
  margin: 16px 0;
  padding: 16px 20px;
  background: #f5f7fa;
  border-left: 4px solid #667eea;
  border-radius: 0 8px 8px 0;
}

.preview-content :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
}

.preview-content :deep(pre) {
  background: #282c34;
  color: #abb2bf;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.preview-content :deep(pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}
</style>
