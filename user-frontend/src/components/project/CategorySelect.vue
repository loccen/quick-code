<template>
  <el-cascader
    v-model="selectedValue"
    :options="categoryOptions"
    :props="cascaderProps"
    :placeholder="placeholder"
    :clearable="clearable"
    :filterable="filterable"
    :show-all-levels="showAllLevels"
    :separator="separator"
    :style="{ width: '100%' }"
    @change="handleChange"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { publicProjectApi } from '@/api/modules/public'
import { ElMessage } from 'element-plus'

// 分类数据类型
interface CategoryOption {
  value: number | string
  label: string
  children?: CategoryOption[]
  disabled?: boolean
}

interface CategoryData {
  id: number
  name: string
  code: string
  parentId?: number
  children?: CategoryData[]
  status?: number // 改为可选，因为API返回的数据可能没有这个字段
  count?: number // 添加count字段
}

// 组件属性
interface Props {
  modelValue?: number | string | null
  placeholder?: string
  clearable?: boolean
  filterable?: boolean
  showAllLevels?: boolean
  separator?: string
  disabled?: boolean
  valueField?: 'id' | 'code' // 新增：指定返回值字段
}

// 组件事件
interface Emits {
  (e: 'update:modelValue', value: number | string | null): void
  (e: 'change', value: number | string | null, selectedData?: CategoryData): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请选择分类',
  clearable: true,
  filterable: true,
  showAllLevels: false,
  separator: ' / ',
  disabled: false,
  valueField: 'code' // 默认返回code字段
})

const emit = defineEmits<Emits>()

// 响应式数据
const categories = ref<CategoryData[]>([])
const loading = ref(false)

// 级联选择器配置
const cascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  disabled: 'disabled',
  checkStrictly: true, // 允许选择任意级别
  emitPath: false // 只返回最后一级的值
}

// 计算属性
const selectedValue = computed({
  get: () => {
    if (!props.modelValue) return null
    return findCategoryPath(props.modelValue)
  },
  set: (value) => {
    const categoryValue = Array.isArray(value) ? value[value.length - 1] : value
    emit('update:modelValue', categoryValue)
  }
})

const categoryOptions = computed(() => {
  return convertToOptions(categories.value)
})

// 将分类数据转换为级联选择器选项
const convertToOptions = (categoryList: CategoryData[]): CategoryOption[] => {
  return categoryList.map(category => {
    const option: CategoryOption = {
      value: props.valueField === 'code' ? category.code : category.id,
      label: category.name,
      disabled: category.status !== undefined ? category.status !== 1 : false // 如果没有status字段，默认可选
    }

    if (category.children && category.children.length > 0) {
      option.children = convertToOptions(category.children)
    }

    return option
  })
}

// 查找分类路径（从根到指定分类的完整路径）
const findCategoryPath = (value: number | string): (number | string)[] | null => {
  const findPath = (categories: CategoryData[], targetValue: number | string, path: (number | string)[] = []): (number | string)[] | null => {
    for (const category of categories) {
      const currentValue = props.valueField === 'code' ? category.code : category.id
      const currentPath = [...path, currentValue]

      if (currentValue === targetValue) {
        return currentPath
      }

      if (category.children && category.children.length > 0) {
        const result = findPath(category.children, targetValue, currentPath)
        if (result) {
          return result
        }
      }
    }
    return null
  }

  return findPath(categories.value, value)
}

// 根据值查找分类数据
const findCategoryByValue = (value: number | string): CategoryData | null => {
  const findCategory = (categories: CategoryData[]): CategoryData | null => {
    for (const category of categories) {
      const categoryValue = props.valueField === 'code' ? category.code : category.id
      if (categoryValue === value) {
        return category
      }

      if (category.children && category.children.length > 0) {
        const result = findCategory(category.children)
        if (result) {
          return result
        }
      }
    }
    return null
  }

  return findCategory(categories.value)
}

// 处理选择变化
const handleChange = (value: (number | string) | (number | string)[]) => {
  const categoryValue = Array.isArray(value) ? value[value.length - 1] : value
  const selectedCategory = categoryValue ? findCategoryByValue(categoryValue) : null

  emit('change', categoryValue || null, selectedCategory || undefined)
}

// 加载分类数据
const loadCategories = async () => {
  if (loading.value) return
  
  loading.value = true
  try {
    const response = await publicProjectApi.getCategories()
    if (response && response.code === 200 && response.data) {
      categories.value = response.data
    } else {
      throw new Error(response?.message || '获取分类列表失败')
    }
  } catch (error: unknown) {
    console.error('加载分类失败:', error)
    const errorMessage = error instanceof Error ? error.message : '加载分类失败'
    ElMessage.error(errorMessage)
    categories.value = []
  } finally {
    loading.value = false
  }
}

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  if (newValue && categories.value.length === 0) {
    loadCategories()
  }
}, { immediate: true })

// 组件挂载时加载数据
onMounted(() => {
  loadCategories()
})

// 暴露方法供父组件调用
defineExpose({
  loadCategories,
  findCategoryByValue,
  findCategoryPath
})
</script>

<style lang="scss" scoped>
// 级联选择器样式可以在这里自定义
:deep(.el-cascader) {
  width: 100%;
}

:deep(.el-cascader__dropdown) {
  max-height: 300px;
}
</style>
