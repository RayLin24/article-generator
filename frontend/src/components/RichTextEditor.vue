<template>
  <div class="rich-text-editor">
    <Toolbar
      class="toolbar"
      :editor="editorRef"
      :defaultConfig="toolbarConfig"
      mode="default"
    />
    <Editor
      class="editor"
      :defaultConfig="editorConfig"
      :modelValue="modelValue"
      mode="default"
      @onChange="handleChange"
      @onCreated="handleCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { shallowRef, onBeforeUnmount, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const editorRef = shallowRef<IDomEditor | null>(null)

const toolbarConfig: Partial<IToolbarConfig> = {
  excludeKeys: []
}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: props.placeholder || '请输入内容',
  MENU_CONF: {
    uploadImage: {
      customUpload: async (file: File, insertFn: (url: string, alt: string, href: string) => void) => {
        const formData = new FormData()
        formData.append('file', file)
        try {
          const response = await fetch('/api/images/upload', {
            method: 'POST',
            body: formData
          })
          const result = await response.json()
          if (result.code === 200 && result.data?.url) {
            insertFn(result.data.url, file.name, result.data.url)
          }
        } catch (error) {
          console.error('Upload failed:', error)
        }
      }
    }
  }
}

const handleCreated = (editor: IDomEditor) => {
  editorRef.value = editor
}

const handleChange = (editor: IDomEditor) => {
  emit('update:modelValue', editor.getHtml())
}

watch(
  () => props.modelValue,
  (newValue) => {
    if (editorRef.value && editorRef.value.getHtml() !== newValue) {
      editorRef.value.setHtml(newValue || '<p></p>')
    }
  }
)

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) {
    editor.destroy()
  }
})

defineExpose({
  getHtml: () => editorRef.value?.getHtml() || '',
  setHtml: (html: string) => editorRef.value?.setHtml(html),
  focus: () => editorRef.value?.focus()
})
</script>

<style scoped>
.rich-text-editor {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
}

.toolbar {
  border-bottom: 1px solid var(--el-border-color);
  background-color: #fafafa;
}

.editor {
  height: 400px;
  overflow-y: auto;
}
</style>
