import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export interface Category {
  id: number
  name: string
  code: string
  promptTemplate: string
  sortOrder: number
  status: number
}

export interface Article {
  id: number
  categoryId: number
  title: string
  content: string
  summary: string
  coverImage: string
  publishDate: string
  viewCount: number
  status: number
  contentType: string
  categoryName: string
  categoryCode: string
}

export interface User {
  id: number
  username: string
  nickname: string
  email: string
  avatar: string
  role: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface LoginResponse {
  token: string
  user: User
}

export const login = (username: string, password: string) => {
  return api.post<ApiResponse<LoginResponse>>('/auth/login', { username, password }).then(res => res.data)
}

export const register = (data: { username: string; password: string; nickname?: string; email?: string }) => {
  return api.post<ApiResponse<string>>('/auth/register', data).then(res => res.data)
}

export const getCurrentUser = () => {
  return api.get<ApiResponse<User>>('/auth/me').then(res => res.data)
}

export const getCategories = () => {
  return api.get<ApiResponse<Category[]>>('/categories').then(res => res.data)
}

export const getCategoriesPage = (page = 1, size = 10) => {
  return api.get<ApiResponse<PageResult<Category>>>('/categories/page', {
    params: { page, size }
  }).then(res => res.data)
}

export const getArticles = (params: { page?: number; size?: number; categoryCode?: string }) => {
  return api.get<ApiResponse<PageResult<Article>>>('/articles', { params }).then(res => res.data)
}

export const getArticleById = (id: number) => {
  return api.get<ApiResponse<Article>>(`/articles/${id}`).then(res => res.data)
}

export const getArticlesByCategory = (code: string, page = 1, size = 10) => {
  return api.get<ApiResponse<PageResult<Article>>>(`/articles/category/${code}`, {
    params: { page, size }
  }).then(res => res.data)
}

export const generateAllArticles = () => {
  return api.post<ApiResponse<string>>('/admin/articles/generate').then(res => res.data)
}

export const generateArticleForCategory = (categoryId: number) => {
  return api.post<ApiResponse<string>>(`/admin/articles/generate/${categoryId}`).then(res => res.data)
}

export const getAdminCategories = () => {
  return api.get<ApiResponse<Category[]>>('/admin/categories').then(res => res.data)
}

export const createCategory = (data: Partial<Category>) => {
  return api.post<ApiResponse<Category>>('/admin/categories', data).then(res => res.data)
}

export const updateCategory = (id: number, data: Partial<Category>) => {
  return api.put<ApiResponse<Category>>(`/admin/categories/${id}`, data).then(res => res.data)
}

export const deleteCategory = (id: number) => {
  return api.delete<ApiResponse<string>>(`/admin/categories/${id}`).then(res => res.data)
}

export const updateCategoryStatus = (id: number, status: number) => {
  return api.put<ApiResponse<Category>>(`/admin/categories/${id}/status`, { status }).then(res => res.data)
}

export const moveCategoryUp = (id: number) => {
  return api.post<ApiResponse<Category>>(`/admin/categories/${id}/move-up`).then(res => res.data)
}

export const moveCategoryDown = (id: number) => {
  return api.post<ApiResponse<Category>>(`/admin/categories/${id}/move-down`).then(res => res.data)
}

export const getAdminArticles = (page = 1, size = 10) => {
  return api.get<ApiResponse<PageResult<Article>>>('/admin/articles', { params: { page, size } }).then(res => res.data)
}

export const createArticle = (data: Partial<Article>) => {
  return api.post<ApiResponse<Article>>('/admin/articles', data).then(res => res.data)
}

export const updateArticle = (id: number, data: Partial<Article>) => {
  return api.put<ApiResponse<Article>>(`/admin/articles/${id}`, data).then(res => res.data)
}

export const deleteArticle = (id: number) => {
  return api.delete<ApiResponse<string>>(`/admin/articles/${id}`).then(res => res.data)
}

export interface ImageResponse {
  url: string
  width: number
  height: number
}

export const getRandomImage = (width = 800, height = 400) => {
  return api.get<ApiResponse<ImageResponse>>('/images/random', { params: { width, height } }).then(res => res.data)
}

export const getRandomImages = (count = 5, width = 800, height = 400) => {
  return api.get<ApiResponse<ImageResponse[]>>('/images/batch', { params: { count, width, height } }).then(res => res.data)
}

export const getCoverImage = (articleId: number) => {
  return api.get<ApiResponse<ImageResponse>>(`/images/cover/${articleId}`).then(res => res.data)
}

export interface StatsResponse {
  articleCount: number
  categoryCount: number
  userCount: number
  totalViewCount: number
}

export const getStats = () => {
  return api.get<ApiResponse<StatsResponse>>('/admin/stats').then(res => res.data)
}

export interface AdminUser {
  id: number
  username: string
  password?: string
  nickname: string
  email: string
  avatar: string
  role: string
  status: number
  createTime: string
  updateTime: string
}

export const getUsers = (page = 1, size = 10, username?: string) => {
  return api.get<ApiResponse<PageResult<AdminUser>>>('/admin/users', {
    params: { page, size, username }
  }).then(res => res.data)
}

export const createUser = (data: Partial<AdminUser>) => {
  return api.post<ApiResponse<AdminUser>>('/admin/users', data).then(res => res.data)
}

export const updateUser = (id: number, data: Partial<AdminUser>) => {
  return api.put<ApiResponse<AdminUser>>(`/admin/users/${id}`, data).then(res => res.data)
}

export const deleteUser = (id: number) => {
  return api.delete<ApiResponse<string>>(`/admin/users/${id}`).then(res => res.data)
}
