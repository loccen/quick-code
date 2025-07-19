/**
 * 图片加载功能测试
 * 用于验证图片重复请求问题的修复效果
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import ImageWithFallback from '@/components/common/ImageWithFallback.vue'
import { loadImage, clearImageCache, checkImageAvailable } from '@/utils/imageLoader'

// Mock Image constructor
global.Image = class {
  onload: (() => void) | null = null
  onerror: (() => void) | null = null
  src: string = ''
  
  constructor() {
    setTimeout(() => {
      if (this.src.includes('valid')) {
        this.onload?.()
      } else {
        this.onerror?.()
      }
    }, 10)
  }
} as any

describe('ImageWithFallback 组件', () => {
  beforeEach(() => {
    clearImageCache()
  })

  it('应该正确渲染有效图片', async () => {
    const wrapper = mount(ImageWithFallback, {
      props: {
        src: 'https://example.com/valid-image.jpg',
        alt: '测试图片'
      }
    })

    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 20))

    const img = wrapper.find('img')
    expect(img.exists()).toBe(true)
    expect(img.attributes('src')).toBe('https://example.com/valid-image.jpg')
    expect(img.attributes('alt')).toBe('测试图片')
  })

  it('应该在主图片失败时显示备用图片', async () => {
    const wrapper = mount(ImageWithFallback, {
      props: {
        src: 'https://example.com/invalid-image.jpg',
        fallbackSrc: 'https://example.com/valid-fallback.jpg',
        alt: '测试图片'
      }
    })

    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 30))

    const img = wrapper.find('img')
    expect(img.exists()).toBe(true)
    expect(img.attributes('src')).toBe('https://example.com/valid-fallback.jpg')
  })

  it('应该在所有图片失败时显示占位符', async () => {
    const wrapper = mount(ImageWithFallback, {
      props: {
        src: 'https://example.com/invalid-image.jpg',
        fallbackSrc: 'https://example.com/invalid-fallback.jpg',
        placeholderText: '图片加载失败'
      }
    })

    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 30))

    const placeholder = wrapper.find('.image-placeholder')
    expect(placeholder.exists()).toBe(true)
    expect(placeholder.text()).toContain('图片加载失败')
    
    const img = wrapper.find('img')
    expect(img.exists()).toBe(false)
  })

  it('应该触发正确的事件', async () => {
    const wrapper = mount(ImageWithFallback, {
      props: {
        src: 'https://example.com/valid-image.jpg'
      }
    })

    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 20))

    expect(wrapper.emitted('load')).toBeTruthy()
    expect(wrapper.emitted('load')).toHaveLength(1)
  })

  it('应该支持自定义占位符插槽', async () => {
    const wrapper = mount(ImageWithFallback, {
      props: {
        src: 'https://example.com/invalid-image.jpg',
        fallbackSrc: 'https://example.com/invalid-fallback.jpg'
      },
      slots: {
        placeholder: '<div class="custom-placeholder">自定义占位符</div>'
      }
    })

    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 30))

    const customPlaceholder = wrapper.find('.custom-placeholder')
    expect(customPlaceholder.exists()).toBe(true)
    expect(customPlaceholder.text()).toBe('自定义占位符')
  })
})

describe('imageLoader 工具函数', () => {
  beforeEach(() => {
    clearImageCache()
  })

  it('应该成功加载有效图片', async () => {
    const result = await loadImage('https://example.com/valid-image.jpg')
    
    expect(result.success).toBe(true)
    expect(result.src).toBe('https://example.com/valid-image.jpg')
    expect(result.usedFallback).toBe(false)
    expect(result.error).toBeUndefined()
  })

  it('应该在主图片失败时使用备用图片', async () => {
    const result = await loadImage('https://example.com/invalid-image.jpg', {
      fallbackSrc: 'https://example.com/valid-fallback.jpg'
    })
    
    expect(result.success).toBe(true)
    expect(result.src).toBe('https://example.com/valid-fallback.jpg')
    expect(result.usedFallback).toBe(true)
  })

  it('应该在所有图片失败时返回失败结果', async () => {
    const result = await loadImage('https://example.com/invalid-image.jpg', {
      fallbackSrc: 'https://example.com/invalid-fallback.jpg'
    })
    
    expect(result.success).toBe(false)
    expect(result.usedFallback).toBe(false)
    expect(result.error).toBeDefined()
  })

  it('应该正确处理空URL', async () => {
    const result = await loadImage('')
    
    expect(result.success).toBe(false)
    expect(result.error).toBe('图片URL为空')
  })

  it('应该支持图片缓存', async () => {
    const url = 'https://example.com/valid-image.jpg'
    
    // 第一次加载
    const result1 = await loadImage(url, { cache: true })
    expect(result1.success).toBe(true)
    
    // 第二次加载应该使用缓存
    const result2 = await loadImage(url, { cache: true })
    expect(result2.success).toBe(true)
    expect(result2.src).toBe(url)
  })
})

describe('checkImageAvailable 函数', () => {
  it('应该正确检查有效图片', async () => {
    const isAvailable = await checkImageAvailable('https://example.com/valid-image.jpg')
    expect(isAvailable).toBe(true)
  })

  it('应该正确检查无效图片', async () => {
    const isAvailable = await checkImageAvailable('https://example.com/invalid-image.jpg')
    expect(isAvailable).toBe(false)
  })

  it('应该正确处理空URL', async () => {
    const isAvailable = await checkImageAvailable('')
    expect(isAvailable).toBe(false)
  })
})

describe('防止无限循环测试', () => {
  it('应该防止重复请求相同的失败图片', async () => {
    let requestCount = 0
    
    // Mock Image to count requests
    const OriginalImage = global.Image
    global.Image = class extends OriginalImage {
      set src(value: string) {
        if (value.includes('default-project.jpg')) {
          requestCount++
        }
        super.src = value
      }
    } as any

    const wrapper = mount(ImageWithFallback, {
      props: {
        src: 'https://example.com/invalid-image.jpg',
        fallbackSrc: '/images/default-project.jpg'
      }
    })

    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 50))

    // 应该只请求一次默认图片
    expect(requestCount).toBeLessThanOrEqual(1)
    
    // 恢复原始Image
    global.Image = OriginalImage
  })
})

// 集成测试：模拟真实的ProjectDetailView场景
describe('ProjectDetailView 图片加载集成测试', () => {
  it('应该正确处理项目缩略图加载失败的情况', async () => {
    // 模拟项目数据
    const project = {
      id: 1,
      title: '测试项目',
      thumbnail: null // 没有缩略图
    }

    const wrapper = mount({
      template: `
        <div class="project-thumbnail">
          <ImageWithFallback
            :src="project.thumbnail"
            :alt="project.title"
            fallback-src="/images/default-project.jpg"
          />
        </div>
      `,
      components: { ImageWithFallback },
      setup() {
        return { project }
      }
    })

    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 30))

    // 应该显示默认图片或占位符，而不是无限循环请求
    const imageComponent = wrapper.findComponent(ImageWithFallback)
    expect(imageComponent.exists()).toBe(true)
    
    // 检查是否有图片或占位符显示
    const hasImage = wrapper.find('img').exists()
    const hasPlaceholder = wrapper.find('.image-placeholder').exists()
    expect(hasImage || hasPlaceholder).toBe(true)
  })
})
