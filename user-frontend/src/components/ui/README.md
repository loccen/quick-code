# UI组件库

## MarkdownRenderer 组件

### 功能特性

- 支持完整的Markdown语法解析
- 内置代码高亮和复制功能
- 支持毛玻璃效果样式
- 响应式设计
- 错误处理和加载状态

### 使用方法

```vue
<template>
  <MarkdownRenderer 
    :content="markdownContent"
    :glass="true"
    :loading="loading"
    :error="error"
  />
</template>

<script setup>
import MarkdownRenderer from '@/components/ui/MarkdownRenderer.vue'

const markdownContent = ref(`
# 标题
这是一个**粗体**文本和*斜体*文本。

## 代码示例
\`\`\`javascript
console.log('Hello World!')
\`\`\`
`)
</script>
```

### Props

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| content | string | '' | Markdown内容 |
| glass | boolean | false | 是否使用毛玻璃效果 |
| loading | boolean | false | 是否显示加载状态 |
| error | string | '' | 错误信息 |
| options | MarkdownIt.Options | 默认配置 | Markdown-it配置选项 |

### 样式特性

- 遵循速码网UI设计规范
- 支持毛玻璃效果
- 现代化的代码块样式
- 响应式表格和图片
- 优雅的链接和引用样式
