<template>
  <div class="categories-page">
    <div class="page-header">
      <h2>栏目管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        新增栏目
      </el-button>
    </div>

    <el-card>
      <el-table :data="categories" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="栏目名称" width="140" />
        <el-table-column prop="code" label="栏目编码" width="160" />
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <div class="status-cell">
              <el-switch
                :model-value="row.status === 1"
                :loading="switchingCategoryId === row.id"
                inline-prompt
                active-text="启用"
                inactive-text="禁用"
                @change="handleStatusChange(row, $event)"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="promptTemplate" label="提示词模板" min-width="360" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row, $index }">
            <el-button size="small" :disabled="$index === 0 || movingCategoryId === row.id" @click="handleMoveUp(row.id)">
              上移
            </el-button>
            <el-button size="small" :disabled="$index === categories.length - 1 || movingCategoryId === row.id" @click="handleMoveDown(row.id)">
              下移
            </el-button>
            <el-button type="primary" size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该栏目吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑栏目' : '新增栏目'"
      width="640px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="栏目名称" prop="name">
          <el-input v-model="form.name" maxlength="50" placeholder="例如：旅行攻略" />
        </el-form-item>
        <el-form-item label="栏目编码" prop="code">
          <el-input v-model="form.code" maxlength="50" placeholder="例如：travel-guide" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="提示词模板" prop="promptTemplate">
          <el-input
            v-model="form.promptTemplate"
            type="textarea"
            :rows="6"
            placeholder="请输入 AI 提示词模板，可使用 {topic} 作为主题占位符"
          />
        </el-form-item>
        <el-alert
          type="info"
          :closable="false"
          title="建议在模板中保留 {topic} 占位符，这样系统生成文章时会自动注入该栏目的具体主题。"
        />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createCategory,
  deleteCategory,
  getAdminCategories,
  moveCategoryDown,
  moveCategoryUp,
  updateCategory,
  updateCategoryStatus,
  type Category
} from '@/api/article'
import { useCategoryStore } from '@/stores/category'

const categoryStore = useCategoryStore()

const loading = ref(false)
const submitting = ref(false)
const switchingCategoryId = ref<number | null>(null)
const movingCategoryId = ref<number | null>(null)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const categories = ref<Category[]>([])

const form = reactive({
  name: '',
  code: '',
  promptTemplate: '',
  sortOrder: 0,
  status: 1
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入栏目名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入栏目编码', trigger: 'blur' },
    { pattern: /^[a-z0-9_-]+$/, message: '只能包含小写字母、数字、下划线和中划线', trigger: 'blur' }
  ],
  promptTemplate: [{ required: true, message: '请输入提示词模板', trigger: 'blur' }]
}

function resetForm() {
  editingId.value = null
  isEdit.value = false
  form.name = ''
  form.code = ''
  form.promptTemplate = ''
  form.sortOrder = 0
  form.status = 1
}

async function loadCategories() {
  loading.value = true
  try {
    const res = await getAdminCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  } finally {
    loading.value = false
  }
}

async function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEditDialog(category: Category) {
  editingId.value = category.id
  isEdit.value = true
  form.name = category.name
  form.code = category.code
  form.promptTemplate = category.promptTemplate
  form.sortOrder = category.sortOrder
  form.status = category.status
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = {
      name: form.name.trim(),
      code: form.code.trim(),
      promptTemplate: form.promptTemplate.trim(),
      sortOrder: form.sortOrder,
      status: form.status
    }
    const res = isEdit.value && editingId.value
      ? await updateCategory(editingId.value, payload)
      : await createCategory(payload)

    if (res.code !== 200) {
      ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'))
      return
    }

    ElMessage.success(isEdit.value ? '栏目更新成功' : '栏目创建成功')
    dialogVisible.value = false
    await Promise.all([
      loadCategories(),
      categoryStore.fetchCategories(true)
    ])
  } catch (error: any) {
    ElMessage.error(error.message || (isEdit.value ? '更新失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    const res = await deleteCategory(id)
    if (res.code !== 200) {
      ElMessage.error(res.message || '删除失败')
      return
    }

    ElMessage.success('栏目删除成功')
    await Promise.all([
      loadCategories(),
      categoryStore.fetchCategories(true)
    ])
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

async function handleStatusChange(category: Category, value: string | number | boolean) {
  switchingCategoryId.value = category.id
  try {
    const status = value ? 1 : 0
    const res = await updateCategoryStatus(category.id, status)
    if (res.code !== 200) {
      ElMessage.error(res.message || '状态更新失败')
      await loadCategories()
      return
    }

    ElMessage.success(status === 1 ? '栏目已启用' : '栏目已禁用')
    await categoryStore.fetchCategories(true)
    await loadCategories()
  } catch (error: any) {
    ElMessage.error(error.message || '状态更新失败')
    await loadCategories()
  } finally {
    switchingCategoryId.value = null
  }
}

async function handleMoveUp(id: number) {
  movingCategoryId.value = id
  try {
    const categoryName = categories.value.find(category => category.id === id)?.name || '栏目'
    const res = await moveCategoryUp(id)
    if (res.code !== 200) {
      ElMessage.error(res.message || '上移失败')
      return
    }

    ElMessage.success(`已将“${categoryName}”上移一位`)
    await Promise.all([
      loadCategories(),
      categoryStore.fetchCategories(true)
    ])
  } catch (error: any) {
    ElMessage.error(error.message || '上移失败')
  } finally {
    movingCategoryId.value = null
  }
}

async function handleMoveDown(id: number) {
  movingCategoryId.value = id
  try {
    const categoryName = categories.value.find(category => category.id === id)?.name || '栏目'
    const res = await moveCategoryDown(id)
    if (res.code !== 200) {
      ElMessage.error(res.message || '下移失败')
      return
    }

    ElMessage.success(`已将“${categoryName}”下移一位`)
    await Promise.all([
      loadCategories(),
      categoryStore.fetchCategories(true)
    ])
  } catch (error: any) {
    ElMessage.error(error.message || '下移失败')
  } finally {
    movingCategoryId.value = null
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.status-cell {
  display: flex;
  align-items: center;
}
</style>
