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
              fit="cover"
              style="width: 80px; height: 50px; border-radius: 4px;"
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
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

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑文章' : '新增文章'"
      width="900px"
      destroy-on-close
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
        
        <el-form-item label="内容" prop="content">
          <div class="content-editor">
            <div class="content-toolbar">
              <el-button size="small" @click="insertImageToContent" :loading="insertingImage">
                <el-icon><Picture /></el-icon>
                插入图片
              </el-button>
            </div>
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="15"
              placeholder="请输入文章内容（支持Markdown格式，可插入图片）"
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
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture, Refresh } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useCategoryStore } from '@/stores/category'
import { getAdminArticles, createArticle, updateArticle, deleteArticle, getRandomImage, type Article } from '@/api/article'

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
const insertingImage = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  categoryId: null as number | null,
  title: '',
  content: '',
  summary: '',
  coverImage: '',
  status: 1
})

const rules: FormRules = {
  categoryId: [{ required: true, message: '请选择栏目', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
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
  dialogVisible.value = true
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

const insertImageToContent = async () => {
  insertingImage.value = true
  try {
    const res = await getRandomImage(800, 400)
    if (res.code === 200 && res.data) {
      const imageMarkdown = `\n\n![配图](${res.data.url})\n\n`
      form.content += imageMarkdown
      ElMessage.success('图片已插入')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取图片失败')
  } finally {
    insertingImage.value = false
  }
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
        status: form.status
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
        status: form.status
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

.content-toolbar {
  margin-bottom: 10px;
}
</style>
