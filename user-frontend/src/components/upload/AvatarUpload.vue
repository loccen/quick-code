<template>
  <div class="avatar-upload">
    <div class="upload-container">
      <!-- 头像预览区域 -->
      <div class="avatar-preview" @click="triggerUpload" data-testid="avatar-preview">
        <div class="avatar-wrapper">
          <el-avatar
            v-if="currentAvatar"
            :src="currentAvatar"
            :size="avatarSize"
            class="avatar-image"
          />
          <div v-else class="avatar-placeholder">
            <el-icon><User /></el-icon>
          </div>
          
          <!-- 上传遮罩 -->
          <div class="upload-overlay">
            <el-icon><Camera /></el-icon>
            <span class="upload-text">{{ currentAvatar ? '更换头像' : '上传头像' }}</span>
          </div>
        </div>
      </div>

      <!-- 隐藏的文件输入 -->
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        style="display: none"
        @change="handleFileSelect"
        data-testid="file-input"
      />

      <!-- 上传进度 -->
      <div v-if="uploading" class="upload-progress" data-testid="upload-progress">
        <el-progress
          :percentage="uploadProgress"
          :stroke-width="4"
          :show-text="false"
          class="progress-bar"
        />
        <span class="progress-text">{{ uploadProgress }}%</span>
      </div>

      <!-- 操作按钮 -->
      <div class="upload-actions">
        <ModernButton
          size="small"
          @click="triggerUpload"
          :disabled="uploading"
          data-testid="select-file-button"
        >
          <el-icon><Upload /></el-icon>
          选择文件
        </ModernButton>
      </div>

      <!-- 上传提示 -->
      <div class="upload-tips">
        <p class="tip-text">支持 JPG、PNG、GIF 格式</p>
        <p class="tip-text">文件大小不超过 {{ maxSizeMB }}MB</p>
        <p class="tip-text">建议尺寸 {{ recommendSize }}px</p>
      </div>
    </div>

    <!-- 图片裁剪对话框 -->
    <el-dialog
      v-model="showCropDialog"
      title="裁剪头像"
      width="600px"
      :before-close="handleCropDialogClose"
      data-testid="crop-dialog"
    >
      <div class="crop-container">
        <div class="crop-preview">
          <canvas
            ref="cropCanvasRef"
            class="crop-canvas"
            @mousedown="startDragImage"
            @mousemove="handleMouseMove"
            @mouseup="endDragImage"
            @mouseleave="endDragImage"
            @wheel="handleWheel"
          ></canvas>
        </div>
        
        <div class="crop-controls">
          <div class="crop-info">
            <p>拖拽图片调整位置，滚轮缩放图片</p>
            <p>圆形头像裁剪，尺寸: {{ cropSize }}x{{ cropSize }}px</p>
          </div>

          <!-- 缩放控制 -->
          <div class="scale-controls">
            <div class="scale-info">
              <span>缩放: {{ Math.round(cropData.imageScale * 100) }}%</span>
            </div>
            <div class="scale-buttons">
              <ModernButton
                size="small"
                @click="adjustScale(-0.1)"
                :disabled="cropData.imageScale <= cropData.minScale"
              >
                -
              </ModernButton>
              <div class="scale-slider">
                <input
                  type="range"
                  :min="cropData.minScale"
                  :max="cropData.maxScale"
                  :step="0.1"
                  v-model.number="cropData.imageScale"
                  @input="onScaleChange"
                  class="slider"
                />
              </div>
              <ModernButton
                size="small"
                @click="adjustScale(0.1)"
                :disabled="cropData.imageScale >= cropData.maxScale"
              >
                +
              </ModernButton>
              <ModernButton size="small" @click="resetScale">适应</ModernButton>
            </div>
          </div>

          <div class="crop-actions">
            <ModernButton @click="resetCrop">重置</ModernButton>
            <ModernButton type="primary" @click="confirmCrop" data-testid="confirm-crop-button">
              确认裁剪
            </ModernButton>
          </div>
        </div>
      </div>

      <template #footer>
        <ModernButton @click="showCropDialog = false">取消</ModernButton>
        <ModernButton
          type="primary"
          :loading="uploading"
          @click="uploadCroppedImage"
          data-testid="upload-cropped-button"
        >
          上传头像
        </ModernButton>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import ModernButton from '@/components/ui/ModernButton.vue'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { Camera, Delete, Upload, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, nextTick, onMounted, ref, watch } from 'vue'

// Props
interface Props {
  /** 当前头像URL */
  modelValue?: string
  /** 头像尺寸 */
  size?: number
  /** 最大文件大小(MB) */
  maxSize?: number
  /** 推荐尺寸 */
  recommendSize?: number
  /** 裁剪尺寸 */
  cropSize?: number
  /** 是否启用裁剪 */
  enableCrop?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  size: 100,
  maxSize: 2,
  recommendSize: 200,
  cropSize: 200,
  enableCrop: true
})

// Emits
interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'upload-success', url: string): void
  (e: 'upload-error', error: Error): void
  (e: 'upload-progress', progress: number): void
}

const emit = defineEmits<Emits>()

// 响应式数据
const fileInputRef = ref<HTMLInputElement>()
const cropCanvasRef = ref<HTMLCanvasElement>()
const currentAvatar = ref(props.modelValue)
const uploading = ref(false)
const uploadProgress = ref(0)
const showCropDialog = ref(false)

// 裁剪相关状态
const selectedFile = ref<File>()
const originalImage = ref<HTMLImageElement>()
const cropData = ref({
  // 图片相关
  imageScale: 1.0,           // 图片缩放比例
  imageOffsetX: 0,           // 图片X偏移
  imageOffsetY: 0,           // 图片Y偏移
  imageWidth: 0,             // 缩放后的图片宽度
  imageHeight: 0,            // 缩放后的图片高度

  // 裁剪框相关（固定在中心）
  cropBoxSize: 200,          // 裁剪框尺寸
  cropBoxX: 100,             // 裁剪框X位置（中心）
  cropBoxY: 100,             // 裁剪框Y位置（中心）

  // 拖拽相关
  isDraggingImage: false,    // 是否正在拖拽图片
  dragStartX: 0,             // 拖拽开始X位置
  dragStartY: 0,             // 拖拽开始Y位置
  imageStartX: 0,            // 图片拖拽开始时的X偏移
  imageStartY: 0,            // 图片拖拽开始时的Y偏移

  // 缩放限制
  minScale: 0.1,             // 最小缩放比例
  maxScale: 5.0              // 最大缩放比例
})

// 计算属性
const avatarSize = computed(() => props.size)
const maxSizeMB = computed(() => props.maxSize)
const recommendSize = computed(() => props.recommendSize)
const cropSize = computed(() => props.cropSize)

// 获取用户store
const userStore = useUserStore()

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  currentAvatar.value = newValue
})

// 监听userStore中头像的变化
watch(() => userStore.user?.avatarUrl, (newAvatarUrl) => {
  if (newAvatarUrl && newAvatarUrl !== currentAvatar.value) {
    currentAvatar.value = newAvatarUrl
    emit('update:modelValue', newAvatarUrl)
  }
})

// 触发文件选择
const triggerUpload = () => {
  if (uploading.value) return
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  
  // 验证文件大小
  const maxSizeBytes = props.maxSize * 1024 * 1024
  if (file.size > maxSizeBytes) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
    return
  }
  
  selectedFile.value = file
  
  if (props.enableCrop) {
    // 显示裁剪对话框
    showCropDialog.value = true
    nextTick(() => {
      loadImageForCrop(file)
    })
  } else {
    // 直接上传
    uploadFile(file)
  }
  
  // 清空input值，允许重复选择同一文件
  target.value = ''
}

// 加载图片用于裁剪
const loadImageForCrop = (file: File) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    const img = new Image()
    img.onload = () => {
      originalImage.value = img
      initializeCropData()
      drawImageOnCanvas()
    }
    img.src = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

// 初始化裁剪数据
const initializeCropData = () => {
  if (!originalImage.value) return

  const canvasSize = 400
  const img = originalImage.value

  // 设置裁剪框固定在中心
  cropData.value.cropBoxSize = Math.min(canvasSize * 0.6, props.cropSize)
  cropData.value.cropBoxX = (canvasSize - cropData.value.cropBoxSize) / 2
  cropData.value.cropBoxY = (canvasSize - cropData.value.cropBoxSize) / 2

  // 计算初始缩放比例，让图片能够覆盖裁剪框
  const scaleToFitCrop = Math.max(
    cropData.value.cropBoxSize / img.width,
    cropData.value.cropBoxSize / img.height
  )
  cropData.value.imageScale = Math.max(scaleToFitCrop, 0.5)

  // 计算缩放后的图片尺寸
  cropData.value.imageWidth = img.width * cropData.value.imageScale
  cropData.value.imageHeight = img.height * cropData.value.imageScale

  // 初始化图片位置（居中）
  cropData.value.imageOffsetX = (canvasSize - cropData.value.imageWidth) / 2
  cropData.value.imageOffsetY = (canvasSize - cropData.value.imageHeight) / 2
}

// 在画布上绘制图片和裁剪框
const drawImageOnCanvas = () => {
  if (!cropCanvasRef.value || !originalImage.value) return

  const canvas = cropCanvasRef.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const canvasSize = 400

  // 设置画布尺寸
  canvas.width = canvasSize
  canvas.height = canvasSize

  // 清空画布
  ctx.clearRect(0, 0, canvasSize, canvasSize)

  // 绘制图片
  drawScaledImage(ctx)

  // 绘制裁剪框
  drawCropBox(ctx)
}

// 绘制缩放后的图片
const drawScaledImage = (ctx: CanvasRenderingContext2D) => {
  if (!originalImage.value) return

  const img = originalImage.value

  // 绘制缩放和偏移后的图片
  ctx.drawImage(
    img,
    cropData.value.imageOffsetX,
    cropData.value.imageOffsetY,
    cropData.value.imageWidth,
    cropData.value.imageHeight
  )
}

// 绘制裁剪框（圆形）
const drawCropBox = (ctx: CanvasRenderingContext2D) => {
  const canvasSize = 400
  const centerX = cropData.value.cropBoxX + cropData.value.cropBoxSize / 2
  const centerY = cropData.value.cropBoxY + cropData.value.cropBoxSize / 2
  const radius = cropData.value.cropBoxSize / 2

  // 绘制半透明遮罩
  ctx.fillStyle = 'rgba(0, 0, 0, 0.5)'
  ctx.fillRect(0, 0, canvasSize, canvasSize)

  // 清除圆形裁剪区域（显示原图）
  ctx.globalCompositeOperation = 'destination-out'
  ctx.beginPath()
  ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI)
  ctx.fill()
  ctx.globalCompositeOperation = 'source-over'

  // 绘制圆形裁剪框边框
  ctx.strokeStyle = '#1890ff'
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI)
  ctx.stroke()

  // 绘制内圆指示线（帮助用户对齐）
  ctx.strokeStyle = 'rgba(24, 144, 255, 0.3)'
  ctx.lineWidth = 1
  ctx.setLineDash([5, 5])
  ctx.beginPath()
  ctx.arc(centerX, centerY, radius * 0.8, 0, 2 * Math.PI)
  ctx.stroke()
  ctx.setLineDash([])

  // 绘制中心十字线
  ctx.strokeStyle = 'rgba(24, 144, 255, 0.4)'
  ctx.lineWidth = 1
  ctx.beginPath()
  // 水平线
  ctx.moveTo(centerX - radius * 0.3, centerY)
  ctx.lineTo(centerX + radius * 0.3, centerY)
  // 垂直线
  ctx.moveTo(centerX, centerY - radius * 0.3)
  ctx.lineTo(centerX, centerY + radius * 0.3)
  ctx.stroke()

  // 绘制提示文字（在圆形外部）
  ctx.fillStyle = '#1890ff'
  ctx.font = '12px Arial'
  ctx.textAlign = 'center'
  ctx.fillText(
    '拖拽图片调整位置',
    centerX,
    centerY + radius + 20
  )
}

// 检查点击是否在图片区域内
const isPointInImage = (x: number, y: number): boolean => {
  return x >= cropData.value.imageOffsetX &&
         x <= cropData.value.imageOffsetX + cropData.value.imageWidth &&
         y >= cropData.value.imageOffsetY &&
         y <= cropData.value.imageOffsetY + cropData.value.imageHeight
}

// 开始拖拽图片
const startDragImage = (event: MouseEvent) => {
  const rect = cropCanvasRef.value?.getBoundingClientRect()
  if (!rect) return

  const mouseX = event.clientX - rect.left
  const mouseY = event.clientY - rect.top

  // 检查是否点击在图片上
  if (isPointInImage(mouseX, mouseY)) {
    cropData.value.isDraggingImage = true
    cropData.value.dragStartX = mouseX
    cropData.value.dragStartY = mouseY
    cropData.value.imageStartX = cropData.value.imageOffsetX
    cropData.value.imageStartY = cropData.value.imageOffsetY

    // 改变鼠标样式
    if (cropCanvasRef.value) {
      cropCanvasRef.value.style.cursor = 'grabbing'
    }
  }
}

// 处理鼠标移动
const handleMouseMove = (event: MouseEvent) => {
  if (cropData.value.isDraggingImage) {
    // 如果正在拖拽图片，执行拖拽逻辑
    dragImage(event)
  } else {
    // 如果没有拖拽，检查鼠标是否在图片上，更新鼠标样式
    const rect = cropCanvasRef.value?.getBoundingClientRect()
    if (!rect) return

    const mouseX = event.clientX - rect.left
    const mouseY = event.clientY - rect.top

    if (isPointInImage(mouseX, mouseY)) {
      if (cropCanvasRef.value) {
        cropCanvasRef.value.style.cursor = 'grab'
      }
    } else if (cropCanvasRef.value) {
      cropCanvasRef.value.style.cursor = 'default'
    }
  }
}

// 拖拽图片
const dragImage = (event: MouseEvent) => {
  if (!cropData.value.isDraggingImage) return

  const rect = cropCanvasRef.value?.getBoundingClientRect()
  if (!rect) return

  const mouseX = event.clientX - rect.left
  const mouseY = event.clientY - rect.top

  // 计算移动距离
  const deltaX = mouseX - cropData.value.dragStartX
  const deltaY = mouseY - cropData.value.dragStartY

  // 更新图片位置
  cropData.value.imageOffsetX = cropData.value.imageStartX + deltaX
  cropData.value.imageOffsetY = cropData.value.imageStartY + deltaY

  // 重新绘制
  drawImageOnCanvas()
}

// 结束拖拽图片
const endDragImage = () => {
  cropData.value.isDraggingImage = false

  // 恢复鼠标样式
  if (cropCanvasRef.value) {
    cropCanvasRef.value.style.cursor = 'default'
  }
}

// 处理滚轮缩放
const handleWheel = (event: WheelEvent) => {
  event.preventDefault()

  const scaleStep = 0.1
  const delta = event.deltaY > 0 ? -scaleStep : scaleStep

  adjustScale(delta)
}

// 调整缩放
const adjustScale = (delta: number) => {
  const newScale = cropData.value.imageScale + delta

  // 限制缩放范围
  if (newScale >= cropData.value.minScale && newScale <= cropData.value.maxScale) {
    const oldScale = cropData.value.imageScale
    cropData.value.imageScale = newScale

    // 更新图片尺寸
    updateImageSize()

    // 调整图片位置，保持中心点不变
    const scaleFactor = newScale / oldScale
    const canvasSize = 400
    const centerX = canvasSize / 2
    const centerY = canvasSize / 2

    cropData.value.imageOffsetX = centerX - (centerX - cropData.value.imageOffsetX) * scaleFactor
    cropData.value.imageOffsetY = centerY - (centerY - cropData.value.imageOffsetY) * scaleFactor

    // 重新绘制
    drawImageOnCanvas()
  }
}

// 更新图片尺寸
const updateImageSize = () => {
  if (!originalImage.value) return

  const img = originalImage.value
  cropData.value.imageWidth = img.width * cropData.value.imageScale
  cropData.value.imageHeight = img.height * cropData.value.imageScale
}

// 缩放变化处理
const onScaleChange = () => {
  updateImageSize()
  drawImageOnCanvas()
}

// 重置缩放
const resetScale = () => {
  if (!originalImage.value) return

  const canvasSize = 400
  const img = originalImage.value

  // 计算让图片能够覆盖裁剪框的最小缩放比例
  const scaleToFitCrop = Math.max(
    cropData.value.cropBoxSize / img.width,
    cropData.value.cropBoxSize / img.height
  )

  cropData.value.imageScale = Math.max(scaleToFitCrop, 0.5)
  updateImageSize()

  // 居中图片
  cropData.value.imageOffsetX = (canvasSize - cropData.value.imageWidth) / 2
  cropData.value.imageOffsetY = (canvasSize - cropData.value.imageHeight) / 2

  drawImageOnCanvas()
}

// 重置裁剪
const resetCrop = () => {
  // 重新初始化裁剪数据
  initializeCropData()
  drawImageOnCanvas()
}

// 确认裁剪
const confirmCrop = () => {
  // 这里可以添加裁剪预览逻辑
  // 不显示提示，因为这只是确认裁剪区域，真正的成功提示在上传完成后
}

// 上传裁剪后的图片
const uploadCroppedImage = async () => {
  if (!selectedFile.value) return

  try {
    // 获取裁剪后的图片数据
    const croppedFile = await getCroppedImage()
    if (croppedFile) {
      await uploadFile(croppedFile)
      showCropDialog.value = false
    }
  } catch (error) {
    console.error('裁剪图片失败:', error)
    ElMessage.error('裁剪图片失败')
  }
}

// 获取裁剪后的图片
const getCroppedImage = (): Promise<File | null> => {
  return new Promise((resolve) => {
    if (!originalImage.value) {
      resolve(null)
      return
    }

    // 创建新的画布用于裁剪
    const cropCanvas = document.createElement('canvas')
    const cropCtx = cropCanvas.getContext('2d')
    if (!cropCtx) {
      resolve(null)
      return
    }

    cropCanvas.width = props.cropSize
    cropCanvas.height = props.cropSize

    // 计算裁剪区域在原始图片中的位置和尺寸
    const img = originalImage.value

    // 裁剪框在画布中的位置
    const cropBoxX = cropData.value.cropBoxX
    const cropBoxY = cropData.value.cropBoxY
    const cropBoxSize = cropData.value.cropBoxSize

    // 图片在画布中的位置和尺寸
    const imageOffsetX = cropData.value.imageOffsetX
    const imageOffsetY = cropData.value.imageOffsetY
    const imageWidth = cropData.value.imageWidth
    const imageHeight = cropData.value.imageHeight

    // 计算裁剪区域相对于图片的位置
    const cropRelativeX = (cropBoxX - imageOffsetX) / imageWidth
    const cropRelativeY = (cropBoxY - imageOffsetY) / imageHeight
    const cropRelativeSize = cropBoxSize / imageWidth

    // 计算在原始图片中的裁剪区域
    const sourceX = cropRelativeX * img.width
    const sourceY = cropRelativeY * img.height
    const sourceSize = cropRelativeSize * img.width

    // 创建圆形裁剪路径
    cropCtx.save()
    cropCtx.beginPath()
    cropCtx.arc(props.cropSize / 2, props.cropSize / 2, props.cropSize / 2, 0, 2 * Math.PI)
    cropCtx.clip()

    // 从原始图片中裁剪并绘制到目标画布
    cropCtx.drawImage(
      img,
      sourceX,
      sourceY,
      sourceSize,
      sourceSize,
      0,
      0,
      props.cropSize,
      props.cropSize
    )

    cropCtx.restore()

    // 转换为Blob
    cropCanvas.toBlob((blob) => {
      if (blob) {
        const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })
        resolve(file)
      } else {
        resolve(null)
      }
    }, 'image/jpeg', 0.9)
  })
}

// 上传文件
const uploadFile = async (file: File) => {
  try {
    uploading.value = true
    uploadProgress.value = 0

    const response = await userApi.uploadAvatar(file, (progress) => {
      uploadProgress.value = progress
      emit('upload-progress', progress)
    })

    if (response.code === 200 && response.data) {
      const avatarUrl = response.data.url
      currentAvatar.value = avatarUrl
      emit('update:modelValue', avatarUrl)
      emit('upload-success', avatarUrl)

      // 更新用户store中的头像URL
      const userStore = useUserStore()
      if (userStore.user) {
        userStore.user.avatarUrl = avatarUrl
        // 更新localStorage中的用户信息
        localStorage.setItem('user_info', JSON.stringify(userStore.user))
      }

      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    const errorObj = error instanceof Error ? error : new Error('上传失败')
    emit('upload-error', errorObj)
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }
}

// 关闭裁剪对话框
const handleCropDialogClose = () => {
  showCropDialog.value = false
  selectedFile.value = undefined
  originalImage.value = undefined
}

// 初始化头像值
const initializeAvatar = () => {
  // 优先使用props传入的值，如果没有则使用userStore中的值
  const avatarUrl = props.modelValue || userStore.user?.avatarUrl
  if (avatarUrl) {
    currentAvatar.value = avatarUrl
  }
}

// 组件挂载时的初始化
onMounted(() => {
  initializeAvatar()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.avatar-upload {
  .upload-container {
    @include flex-center();
    flex-direction: column;
    gap: $spacing-md;

    .avatar-preview {
      position: relative;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);

        .upload-overlay {
          opacity: 1;
        }
      }

      .avatar-wrapper {
        position: relative;
        border-radius: 50%;
        overflow: hidden;
        @include shadow-layered-md();
        border: 3px solid var(--glass-border);
        background: var(--gradient-glass);

        .avatar-image {
          display: block;
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .avatar-placeholder {
          @include flex-center();
          width: 100%;
          height: 100%;
          background: var(--gradient-glass);
          color: var(--text-tertiary);
          font-size: calc(var(--avatar-size, 100px) * 0.4);

          .el-icon {
            font-size: inherit;
          }
        }

        .upload-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          @include flex-center();
          flex-direction: column;
          gap: $spacing-xs;
          background: rgba(0, 0, 0, 0.6);
          color: #ffffff;
          opacity: 0;
          transition: opacity 0.3s ease;
          backdrop-filter: blur(4px);

          .el-icon {
            font-size: 24px;
          }

          .upload-text {
            font-size: $font-size-sm;
            font-weight: $font-weight-medium;
          }
        }
      }
    }

    .upload-progress {
      width: 100%;
      max-width: 200px;
      @include flex-center();
      flex-direction: column;
      gap: $spacing-xs;

      .progress-bar {
        width: 100%;
      }

      .progress-text {
        font-size: $font-size-sm;
        color: var(--text-secondary);
        font-weight: $font-weight-medium;
      }
    }

    .upload-actions {
      @include flex-center();
      gap: $spacing-sm;
    }

    .upload-tips {
      text-align: center;

      .tip-text {
        color: var(--text-tertiary);
        font-size: $font-size-xs;
        margin: 0;
        line-height: 1.4;

        &:not(:last-child) {
          margin-bottom: $spacing-xs;
        }
      }
    }
  }

  .crop-container {
    @include flex-center();
    gap: $spacing-xl;

    .crop-preview {
      flex-shrink: 0;

      .crop-canvas {
        border: 1px solid var(--border-color);
        border-radius: $radius-lg;
        cursor: crosshair;
        @include shadow-layered-sm();
      }
    }

    .crop-controls {
      flex: 1;
      @include flex-center();
      flex-direction: column;
      gap: $spacing-lg;

      .crop-info {
        text-align: center;

        p {
          color: var(--text-secondary);
          font-size: $font-size-sm;
          margin: 0;
          line-height: 1.5;

          &:not(:last-child) {
            margin-bottom: $spacing-xs;
          }
        }
      }

      .scale-controls {
        width: 100%;
        @include flex-center();
        flex-direction: column;
        gap: $spacing-sm;

        .scale-info {
          color: var(--text-secondary);
          font-size: $font-size-sm;
          font-weight: 500;
        }

        .scale-buttons {
          @include flex-center();
          gap: $spacing-sm;
          width: 100%;

          .scale-slider {
            flex: 1;

            .slider {
              width: 100%;
              height: 4px;
              border-radius: 2px;
              background: var(--bg-secondary);
              outline: none;
              -webkit-appearance: none;
              appearance: none;

              &::-webkit-slider-thumb {
                -webkit-appearance: none;
                appearance: none;
                width: 16px;
                height: 16px;
                border-radius: 50%;
                background: var(--primary-color);
                cursor: pointer;
                @include shadow-layered-sm();
              }

              &::-moz-range-thumb {
                width: 16px;
                height: 16px;
                border-radius: 50%;
                background: var(--primary-color);
                cursor: pointer;
                border: none;
                @include shadow-layered-sm();
              }
            }
          }
        }
      }

      .crop-actions {
        @include flex-center();
        gap: $spacing-sm;
      }
    }
  }
  // 动态设置头像尺寸
  --avatar-size: v-bind('avatarSize + "px"');

  .avatar-preview .avatar-wrapper {
    width: var(--avatar-size);
    height: var(--avatar-size);
  }

  // 上传状态样式
  &.uploading {
    .avatar-preview {
      pointer-events: none;
      opacity: 0.7;
    }

    .upload-actions {
      .modern-button {
        pointer-events: none;
        opacity: 0.7;
      }
    }
  }

  // 错误状态样式
  &.error {
    .avatar-preview .avatar-wrapper {
      border-color: #ff4d4f;
      box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.2);
    }
  }
}

// 响应式设计
@include respond-below('md') {
  .avatar-upload {
    .crop-container {
      flex-direction: column;
      gap: $spacing-lg;

      .crop-preview {
        .crop-canvas {
          max-width: 100%;
          height: auto;
        }
      }

      .crop-controls {
        .crop-actions {
          flex-direction: column;
          width: 100%;

          .modern-button {
            width: 100%;
          }
        }
      }
    }
  }
}
</style>
