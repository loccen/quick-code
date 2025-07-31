<template>
  <div class="markdown-renderer" :class="{ 'glass-effect': glass }">
    <div 
      v-if="renderedContent" 
      class="markdown-content"
      v-html="renderedContent"
    />
    <div v-else-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="error" class="error-state">
      <el-alert
        title="内容加载失败"
        :description="error"
        type="error"
        show-icon
        :closable="false"
      />
    </div>
    <div v-else class="empty-state">
      <el-empty description="暂无内容" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import MarkdownIt from 'markdown-it'

interface Props {
  /** Markdown内容 */
  content?: string
  /** 是否使用毛玻璃效果 */
  glass?: boolean
  /** 是否显示加载状态 */
  loading?: boolean
  /** 错误信息 */
  error?: string
  /** Markdown-it配置选项 */
  options?: MarkdownIt.Options
}

const props = withDefaults(defineProps<Props>(), {
  content: '',
  glass: false,
  loading: false,
  error: '',
  options: () => ({
    html: true,
    linkify: true,
    typographer: true,
    breaks: true
  })
})

// Markdown解析器实例
const md = ref<MarkdownIt | null>(null)

// 渲染后的HTML内容
const renderedContent = computed(() => {
  if (!props.content || !md.value) {
    return ''
  }
  
  try {
    return md.value.render(props.content)
  } catch (error) {
    console.error('Markdown渲染失败:', error)
    return '<p class="error">内容渲染失败</p>'
  }
})

// 初始化Markdown解析器
const initMarkdown = () => {
  md.value = new MarkdownIt(props.options)
  
  // 添加自定义规则和插件
  md.value.renderer.rules.link_open = (tokens, idx, options, env, renderer) => {
    const token = tokens[idx]
    const href = token.attrGet('href')
    
    // 外部链接添加target="_blank"
    if (href && (href.startsWith('http') || href.startsWith('https'))) {
      token.attrSet('target', '_blank')
      token.attrSet('rel', 'noopener noreferrer')
    }
    
    return renderer.renderToken(tokens, idx, options)
  }
  
  // 代码块添加复制按钮
  md.value.renderer.rules.code_block = (tokens, idx, options, env, renderer) => {
    const token = tokens[idx]
    const content = token.content
    const lang = token.info || ''
    
    return `
      <div class="code-block-wrapper">
        <div class="code-block-header">
          <span class="code-lang">${lang}</span>
          <button class="copy-btn" onclick="copyToClipboard('${encodeURIComponent(content)}')">
            复制
          </button>
        </div>
        <pre><code class="language-${lang}">${content}</code></pre>
      </div>
    `
  }
}

// 监听配置变化，重新初始化
watch(() => props.options, () => {
  initMarkdown()
}, { deep: true })

// 组件挂载时初始化
onMounted(() => {
  initMarkdown()
  
  // 添加全局复制函数
  if (typeof window !== 'undefined') {
    (window as any).copyToClipboard = (encodedContent: string) => {
      const content = decodeURIComponent(encodedContent)
      navigator.clipboard.writeText(content).then(() => {
        // 这里可以添加复制成功的提示
        console.log('代码已复制到剪贴板')
      }).catch(err => {
        console.error('复制失败:', err)
      })
    }
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.markdown-renderer {
  width: 100%;
  
  &.glass-effect {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: $border-radius-lg;
    padding: $spacing-xl;
  }
}

.markdown-content {
  line-height: 1.6;
  color: var(--text-primary);
  
  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin: $spacing-xl 0 $spacing-lg 0;
    font-weight: $font-weight-bold;
    color: var(--text-primary);
    
    &:first-child {
      margin-top: 0;
    }
  }
  
  :deep(h1) { font-size: $font-size-3xl; }
  :deep(h2) { font-size: $font-size-2xl; }
  :deep(h3) { font-size: $font-size-xl; }
  :deep(h4) { font-size: $font-size-lg; }
  :deep(h5) { font-size: $font-size-base; }
  :deep(h6) { font-size: $font-size-sm; }
  
  :deep(p) {
    margin: $spacing-md 0;
    
    &:first-child {
      margin-top: 0;
    }
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  :deep(ul), :deep(ol) {
    margin: $spacing-md 0;
    padding-left: $spacing-xl;
    
    li {
      margin: $spacing-sm 0;
    }
  }
  
  :deep(blockquote) {
    margin: $spacing-lg 0;
    padding: $spacing-md $spacing-lg;
    border-left: 4px solid var(--primary-color);
    background: var(--bg-secondary);
    border-radius: 0 $border-radius-md $border-radius-md 0;
    
    p {
      margin: 0;
      color: var(--text-secondary);
      font-style: italic;
    }
  }
  
  :deep(code) {
    padding: 2px 6px;
    background: var(--bg-secondary);
    border-radius: $border-radius-sm;
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 0.9em;
    color: var(--primary-color);
  }
  
  :deep(.code-block-wrapper) {
    margin: $spacing-lg 0;
    border-radius: $border-radius-md;
    overflow: hidden;
    background: var(--bg-secondary);
    border: 1px solid var(--border-color);
    
    .code-block-header {
      @include flex-between();
      padding: $spacing-sm $spacing-md;
      background: var(--bg-tertiary);
      border-bottom: 1px solid var(--border-color);
      
      .code-lang {
        font-size: $font-size-sm;
        color: var(--text-secondary);
        font-weight: $font-weight-medium;
      }
      
      .copy-btn {
        padding: 4px 8px;
        background: var(--primary-color);
        color: white;
        border: none;
        border-radius: $border-radius-sm;
        font-size: $font-size-xs;
        cursor: pointer;
        transition: all 0.2s ease;
        
        &:hover {
          background: var(--primary-color-dark);
        }
      }
    }
    
    pre {
      margin: 0;
      padding: $spacing-md;
      overflow-x: auto;
      
      code {
        background: none;
        padding: 0;
        color: var(--text-primary);
        font-size: $font-size-sm;
      }
    }
  }
  
  :deep(table) {
    width: 100%;
    margin: $spacing-lg 0;
    border-collapse: collapse;
    border: 1px solid var(--border-color);
    border-radius: $border-radius-md;
    overflow: hidden;
    
    th, td {
      padding: $spacing-md;
      text-align: left;
      border-bottom: 1px solid var(--border-color);
    }
    
    th {
      background: var(--bg-secondary);
      font-weight: $font-weight-bold;
      color: var(--text-primary);
    }
    
    tr:last-child td {
      border-bottom: none;
    }
    
    tr:nth-child(even) {
      background: var(--bg-secondary);
    }
  }
  
  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: $border-radius-md;
    margin: $spacing-md 0;
  }
  
  :deep(a) {
    color: var(--primary-color);
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
  
  :deep(hr) {
    margin: $spacing-xl 0;
    border: none;
    height: 1px;
    background: var(--border-color);
  }
}

.loading-state, .error-state, .empty-state {
  padding: $spacing-xl;
  text-align: center;
}

.error-state {
  .el-alert {
    text-align: left;
  }
}
</style>
