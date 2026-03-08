import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategories } from '@/api/article'
import type { Category } from '@/api/article'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref<Category[]>([])
  const loading = ref(false)

  const fetchCategories = async (force = false) => {
    if (!force && categories.value.length > 0) return

    loading.value = true
    try {
      const res = await getCategories()
      categories.value = res.data || []
    } catch (error) {
      console.error('Failed to fetch categories:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    categories,
    loading,
    fetchCategories
  }
})
