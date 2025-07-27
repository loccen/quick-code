<template>
  <div class="tech-stack-selector">
    <!-- 技术栈分类选择 -->
    <div class="tech-categories">
      <el-tabs v-model="activeCategory" @tab-click="handleCategoryChange">
        <el-tab-pane
          v-for="category in techCategories"
          :key="category.key"
          :label="category.label"
          :name="category.key"
        >
          <div class="tech-options">
            <div class="tech-grid">
              <el-tag
                v-for="tech in category.technologies"
                :key="tech.name"
                :type="isSelected(tech.name) ? 'primary' : 'info'"
                :effect="isSelected(tech.name) ? 'dark' : 'plain'"
                class="tech-tag"
                @click="toggleTech(tech.name)"
              >
                <el-icon v-if="tech.icon" class="tech-icon">
                  <component :is="tech.icon" />
                </el-icon>
                {{ tech.name }}
                <el-icon v-if="isSelected(tech.name)" class="selected-icon">
                  <Check />
                </el-icon>
              </el-tag>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 自定义技术栈输入 -->
    <div class="custom-tech-input">
      <el-input
        v-model="customTech"
        placeholder="输入自定义技术栈，按回车添加"
        @keyup.enter="addCustomTech"
        @blur="addCustomTech"
      >
        <template #prepend>
          <el-icon><Plus /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 已选择的技术栈 -->
    <div class="selected-techs">
      <div class="selected-header">
        <span class="selected-title">已选择技术栈 ({{ selectedTechs.length }}/{{ maxTechs }})</span>
        <el-button 
          v-if="selectedTechs.length > 0"
          type="text" 
          size="small"
          @click="clearAll"
        >
          清空全部
        </el-button>
      </div>
      <div class="selected-list">
        <el-tag
          v-for="tech in selectedTechs"
          :key="tech"
          type="primary"
          closable
          class="selected-tag"
          @close="removeTech(tech)"
        >
          {{ tech }}
        </el-tag>
        <div v-if="selectedTechs.length === 0" class="empty-hint">
          请选择项目使用的技术栈
        </div>
      </div>
    </div>

    <!-- 推荐技术栈组合 -->
    <div class="tech-recommendations">
      <div class="recommendation-header">
        <span class="recommendation-title">推荐技术栈组合</span>
      </div>
      <div class="recommendation-list">
        <el-tag
          v-for="combo in techCombinations"
          :key="combo.name"
          type="success"
          effect="plain"
          class="combo-tag"
          @click="applyCombo(combo)"
        >
          {{ combo.name }}
          <el-tooltip :content="combo.description" placement="top">
            <el-icon class="combo-info"><InfoFilled /></el-icon>
          </el-tooltip>
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Check,
  Plus,
  InfoFilled,
  Monitor,
  Setting,
  Coin,
  Iphone,
  Tools
} from '@element-plus/icons-vue'

// Props
interface Props {
  modelValue: string[]
  maxTechs?: number
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  maxTechs: 20,
  disabled: false
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: string[]]
  change: [value: string[]]
}>()

// 响应式数据
const activeCategory = ref('frontend')
const customTech = ref('')
const selectedTechs = ref<string[]>([...props.modelValue])

// 技术栈分类
const techCategories = [
  {
    key: 'frontend',
    label: '前端技术',
    technologies: [
      { name: 'Vue.js', icon: 'Monitor' },
      { name: 'React', icon: 'Monitor' },
      { name: 'Angular', icon: 'Monitor' },
      { name: 'TypeScript', icon: 'Monitor' },
      { name: 'JavaScript', icon: 'Monitor' },
      { name: 'HTML5', icon: 'Monitor' },
      { name: 'CSS3', icon: 'Monitor' },
      { name: 'Sass/SCSS', icon: 'Monitor' },
      { name: 'Less', icon: 'Monitor' },
      { name: 'Webpack', icon: 'Tools' },
      { name: 'Vite', icon: 'Tools' },
      { name: 'Element Plus', icon: 'Monitor' },
      { name: 'Ant Design', icon: 'Monitor' },
      { name: 'Bootstrap', icon: 'Monitor' },
      { name: 'Tailwind CSS', icon: 'Monitor' }
    ]
  },
  {
    key: 'backend',
    label: '后端技术',
    technologies: [
      { name: 'Node.js', icon: 'Setting' },
      { name: 'Express', icon: 'Setting' },
      { name: 'Koa', icon: 'Setting' },
      { name: 'Nest.js', icon: 'Setting' },
      { name: 'Spring Boot', icon: 'Setting' },
      { name: 'Spring Cloud', icon: 'Setting' },
      { name: 'Django', icon: 'Setting' },
      { name: 'Flask', icon: 'Setting' },
      { name: 'FastAPI', icon: 'Setting' },
      { name: 'Laravel', icon: 'Setting' },
      { name: 'PHP', icon: 'Setting' },
      { name: 'Java', icon: 'Setting' },
      { name: 'Python', icon: 'Setting' },
      { name: 'Go', icon: 'Setting' },
      { name: 'Rust', icon: 'Setting' }
    ]
  },
  {
    key: 'database',
    label: '数据库',
    technologies: [
      { name: 'MySQL', icon: 'Coin' },
      { name: 'PostgreSQL', icon: 'Coin' },
      { name: 'MongoDB', icon: 'Coin' },
      { name: 'Redis', icon: 'Coin' },
      { name: 'SQLite', icon: 'Coin' },
      { name: 'Oracle', icon: 'Coin' },
      { name: 'SQL Server', icon: 'Coin' },
      { name: 'Elasticsearch', icon: 'Coin' },
      { name: 'InfluxDB', icon: 'Coin' },
      { name: 'Cassandra', icon: 'Coin' }
    ]
  },
  {
    key: 'mobile',
    label: '移动开发',
    technologies: [
      { name: 'React Native', icon: 'Iphone' },
      { name: 'Flutter', icon: 'Iphone' },
      { name: 'Ionic', icon: 'Iphone' },
      { name: 'Cordova', icon: 'Iphone' },
      { name: 'Swift', icon: 'Iphone' },
      { name: 'Kotlin', icon: 'Iphone' },
      { name: 'Xamarin', icon: 'Iphone' },
      { name: 'Unity', icon: 'Iphone' }
    ]
  },
  {
    key: 'devops',
    label: '运维部署',
    technologies: [
      { name: 'Docker', icon: 'Tools' },
      { name: 'Kubernetes', icon: 'Tools' },
      { name: 'Jenkins', icon: 'Tools' },
      { name: 'GitLab CI', icon: 'Tools' },
      { name: 'GitHub Actions', icon: 'Tools' },
      { name: 'AWS', icon: 'Tools' },
      { name: 'Azure', icon: 'Tools' },
      { name: 'Google Cloud', icon: 'Tools' },
      { name: 'Nginx', icon: 'Tools' },
      { name: 'Apache', icon: 'Tools' }
    ]
  }
]

// 推荐技术栈组合
const techCombinations = [
  {
    name: 'Vue3 全栈',
    description: 'Vue3 + TypeScript + Element Plus + Node.js + MySQL',
    techs: ['Vue.js', 'TypeScript', 'Element Plus', 'Node.js', 'MySQL']
  },
  {
    name: 'React 全栈',
    description: 'React + TypeScript + Ant Design + Node.js + MongoDB',
    techs: ['React', 'TypeScript', 'Ant Design', 'Node.js', 'MongoDB']
  },
  {
    name: 'Spring Boot 后端',
    description: 'Spring Boot + MySQL + Redis + Docker',
    techs: ['Spring Boot', 'Java', 'MySQL', 'Redis', 'Docker']
  },
  {
    name: 'Python Web',
    description: 'Django + PostgreSQL + Redis + Docker',
    techs: ['Django', 'Python', 'PostgreSQL', 'Redis', 'Docker']
  },
  {
    name: '微服务架构',
    description: 'Spring Cloud + Docker + Kubernetes + MySQL',
    techs: ['Spring Cloud', 'Docker', 'Kubernetes', 'MySQL', 'Redis']
  }
]

// 计算属性
const canAddMore = computed(() => selectedTechs.value.length < props.maxTechs)

// 方法
const isSelected = (tech: string): boolean => {
  return selectedTechs.value.includes(tech)
}

const toggleTech = (tech: string) => {
  if (props.disabled) return

  if (isSelected(tech)) {
    removeTech(tech)
  } else {
    addTech(tech)
  }
}

const addTech = (tech: string) => {
  if (!canAddMore.value) {
    ElMessage.warning(`最多只能选择 ${props.maxTechs} 个技术栈`)
    return
  }

  if (!selectedTechs.value.includes(tech)) {
    selectedTechs.value.push(tech)
    updateModelValue()
  }
}

const removeTech = (tech: string) => {
  const index = selectedTechs.value.indexOf(tech)
  if (index > -1) {
    selectedTechs.value.splice(index, 1)
    updateModelValue()
  }
}

const addCustomTech = () => {
  const tech = customTech.value.trim()
  if (tech && !selectedTechs.value.includes(tech)) {
    addTech(tech)
    customTech.value = ''
  }
}

const clearAll = () => {
  selectedTechs.value = []
  updateModelValue()
}

const applyCombo = (combo: any) => {
  if (props.disabled) return

  const newTechs = [...selectedTechs.value]
  combo.techs.forEach((tech: string) => {
    if (!newTechs.includes(tech) && newTechs.length < props.maxTechs) {
      newTechs.push(tech)
    }
  })
  
  selectedTechs.value = newTechs
  updateModelValue()
  ElMessage.success(`已应用 ${combo.name} 技术栈组合`)
}

const handleCategoryChange = () => {
  // 分类切换时的处理逻辑
}

const updateModelValue = () => {
  emit('update:modelValue', selectedTechs.value)
  emit('change', selectedTechs.value)
}

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  selectedTechs.value = [...newValue]
}, { deep: true })
</script>

<style scoped>
.tech-stack-selector {
  width: 100%;
}

.tech-categories {
  margin-bottom: 20px;
}

.tech-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tech-tag {
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.tech-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tech-icon {
  font-size: 14px;
}

.selected-icon {
  font-size: 12px;
  margin-left: 4px;
}

.custom-tech-input {
  margin-bottom: 20px;
}

.selected-techs {
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.selected-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.selected-title {
  font-weight: 500;
  color: #303133;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 32px;
  align-items: center;
}

.selected-tag {
  margin: 0;
}

.empty-hint {
  color: #909399;
  font-size: 14px;
  font-style: italic;
}

.tech-recommendations {
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
  border: 1px solid #e1f5fe;
}

.recommendation-header {
  margin-bottom: 12px;
}

.recommendation-title {
  font-weight: 500;
  color: #1976d2;
}

.recommendation-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.combo-tag {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.combo-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.combo-info {
  font-size: 12px;
  opacity: 0.7;
}
</style>
