<template>
  <div class="markdown-editor">
    <div ref="editorRef" class="vditor-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import Vditor from 'vditor'
import 'vditor/dist/index.css'

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const editorRef = ref<HTMLElement | null>(null)
let vditorInstance: Vditor | null = null

const initVditor = () => {
  if (!editorRef.value) return

  vditorInstance = new Vditor(editorRef.value, {
    height: 400,
    placeholder: props.placeholder || '请输入内容，支持 Markdown 语法',
    theme: 'classic',
    value: props.modelValue || '',
    mode: 'ir',
    toolbar: [
      'headings',
      'bold',
      'italic',
      'strike',
      'link',
      '|',
      'list',
      'ordered-list',
      'check',
      '|',
      'quote',
      'line',
      'code',
      'inline-code',
      '|',
      'table',
      'undo',
      'redo',
      '|',
      'edit-mode',
      'preview',
      'fullscreen'
    ],
    cache: {
      enable: false
    },
    input: (value: string) => {
      emit('update:modelValue', value)
    },
    after: () => {
      if (props.modelValue) {
        vditorInstance?.setValue(props.modelValue)
      }
    }
  })
}

watch(
  () => props.modelValue,
  (newValue) => {
    if (vditorInstance && vditorInstance.getValue() !== newValue) {
      vditorInstance.setValue(newValue || '')
    }
  }
)

onMounted(() => {
  initVditor()
})

onUnmounted(() => {
  vditorInstance?.destroy()
  vditorInstance = null
})

defineExpose({
  getValue: () => vditorInstance?.getValue() || '',
  setValue: (value: string) => vditorInstance?.setValue(value),
  focus: () => vditorInstance?.focus()
})
</script>

<style scoped>
.markdown-editor {
  width: 100%;
}

.vditor-container {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
}

:deep(.vditor) {
  border: none;
}

:deep(.vditor-toolbar) {
  border-bottom: 1px solid var(--el-border-color);
  background-color: #fafafa;
}

:deep(.vditor-content) {
  background-color: #fff;
}
</style>
