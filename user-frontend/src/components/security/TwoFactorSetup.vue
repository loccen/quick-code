<template>
  <div class="two-factor-setup">
    <!-- 2FA状态显示 -->
    <div v-if="!showSetup" class="status-section">
      <div class="status-card">
        <div class="status-header">
          <div class="status-icon" :class="{ enabled: twoFactorStatus.enabled }">
            <el-icon><Lock /></el-icon>
          </div>
          <div class="status-info">
            <h3 class="status-title">双因素认证</h3>
            <p class="status-desc">
              {{ twoFactorStatus.enabled ? '已启用' : '未启用' }} - 
              {{ twoFactorStatus.enabled ? '您的账户受到额外保护' : '建议启用以增强账户安全' }}
            </p>
          </div>
        </div>
        
        <div class="status-actions">
          <ModernButton
            v-if="!twoFactorStatus.enabled"
            type="primary"
            @click="startSetup"
            data-testid="enable-2fa-button"
          >
            启用2FA
          </ModernButton>
          <ModernButton
            v-else
            type="error"
            @click="showDisableDialog = true"
            data-testid="disable-2fa-button"
          >
            禁用2FA
          </ModernButton>
        </div>
      </div>


    </div>

    <!-- 2FA设置流程 -->
    <div v-else class="setup-section">
      <div class="setup-steps">
        <!-- 步骤指示器 -->
        <el-steps :active="currentStep" align-center>
          <el-step title="扫描二维码" />
          <el-step title="验证设置" />
        </el-steps>

        <!-- 步骤1: 扫描二维码 -->
        <div v-if="currentStep === 0" class="step-content">
          <div class="qr-section">
            <h3>使用认证应用扫描二维码</h3>
            <p class="step-desc">
              使用 Google Authenticator、Authy 或其他兼容的认证应用扫描下方二维码
            </p>
            
            <div class="qr-container" data-testid="qr-code-container">
              <div v-if="loading || generatingQR" class="qr-loading">
                <el-icon class="loading-icon"><Loading /></el-icon>
                <p>正在生成二维码...</p>
              </div>
              <div v-else-if="qrCodeDataUrl" class="qr-code">
                <img :src="qrCodeDataUrl" alt="2FA QR Code" />
              </div>
              <div v-else class="qr-error">
                <el-icon><WarningFilled /></el-icon>
                <p>二维码生成失败，请重试</p>
              </div>
            </div>

            <div v-if="setupData?.secret" class="manual-entry">
              <p class="manual-title">或手动输入密钥：</p>
              <div class="secret-code" data-testid="secret-code">
                <code>{{ setupData.secret }}</code>
                <el-button
                  size="small"
                  @click="copySecret"
                  data-testid="copy-secret-button"
                >
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </div>
            </div>
          </div>

          <div class="step-actions">
            <ModernButton @click="cancelSetup">取消</ModernButton>
            <ModernButton type="primary" @click="nextStep">下一步</ModernButton>
          </div>
        </div>

        <!-- 步骤2: 验证设置 -->
        <div v-if="currentStep === 1" class="step-content">
          <div class="verify-section">
            <h3>验证认证码</h3>
            <p class="step-desc">
              请输入认证应用中显示的6位数字验证码
            </p>

            <el-form
              ref="verifyFormRef"
              :model="verifyForm"
              :rules="verifyRules"
              class="verify-form"
            >
              <el-form-item prop="totpCode">
                <el-input
                  v-model="verifyForm.totpCode"
                  placeholder="请输入6位验证码"
                  maxlength="6"
                  size="large"
                  data-testid="totp-code-input"
                  @keyup.enter="verifySetup"
                />
              </el-form-item>
            </el-form>
          </div>

          <div class="step-actions">
            <ModernButton @click="prevStep">上一步</ModernButton>
            <ModernButton
              type="primary"
              :loading="verifying"
              @click="verifySetup"
              data-testid="verify-setup-button"
            >
              验证并启用
            </ModernButton>
          </div>
        </div>


      </div>
    </div>

    <!-- 禁用2FA确认对话框 -->
    <el-dialog
      v-model="showDisableDialog"
      title="禁用双因素认证"
      width="450px"
      :before-close="handleDisableDialogClose"
    >
      <div class="disable-content">
        <p>确定要禁用双因素认证吗？这将降低您账户的安全性。</p>
        
        <el-form
          ref="disableFormRef"
          :model="disableForm"
          :rules="disableRules"
          class="disable-form"
        >
          <el-form-item label="验证码" prop="totpCode">
            <el-input
              v-model="disableForm.totpCode"
              placeholder="请输入6位验证码"
              maxlength="6"
              data-testid="disable-totp-input"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <ModernButton @click="showDisableDialog = false">取消</ModernButton>
          <ModernButton
            type="error"
            :loading="disabling"
            @click="disableTwoFactor"
            data-testid="confirm-disable-button"
          >
            确认禁用
          </ModernButton>
        </div>
      </template>
    </el-dialog>


  </div>
</template>

<script setup lang="ts">
import ModernButton from '@/components/ui/ModernButton.vue'
import { userApi } from '@/api/user'
import type {
  TwoFactorSetupResponse,
  TwoFactorStatusResponse,
  TwoFactorVerifyRequest
} from '@/types/user'
import {
  CopyDocument,
  Loading,
  Lock,
  WarningFilled
} from '@element-plus/icons-vue'
import { ElForm, ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import QRCode from 'qrcode'

// 表单引用
const verifyFormRef = ref<InstanceType<typeof ElForm>>()
const disableFormRef = ref<InstanceType<typeof ElForm>>()

// 状态数据
const twoFactorStatus = reactive<TwoFactorStatusResponse>({
  enabled: false,
  backupCodesCount: 0
})

const setupData = ref<TwoFactorSetupResponse>()
const showSetup = ref(false)
const currentStep = ref(0)
const loading = ref(false)
const verifying = ref(false)
const disabling = ref(false)
const generatingQR = ref(false)
const qrCodeDataUrl = ref<string>('')

// 表单数据
const verifyForm = reactive<TwoFactorVerifyRequest>({
  totpCode: ''
})

const disableForm = reactive<TwoFactorVerifyRequest>({
  totpCode: ''
})

// 对话框状态
const showDisableDialog = ref(false)

// 表单验证规则
const verifyRules = {
  totpCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须是6位数字', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码只能包含数字', trigger: 'blur' }
  ]
}

const disableRules = {
  totpCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须是6位数字', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码只能包含数字', trigger: 'blur' }
  ]
}

// 初始化加载2FA状态
const loadTwoFactorStatus = async () => {
  try {
    const response = await userApi.getTwoFactorStatus()
    if (response.code === 200 && response.data) {
      Object.assign(twoFactorStatus, response.data)
    }
  } catch (error) {
    console.error('加载2FA状态失败:', error)
    ElMessage.error('加载2FA状态失败')
  }
}

// 生成二维码
const generateQRCode = async (qrCodeUrl: string) => {
  try {
    generatingQR.value = true
    const dataUrl = await QRCode.toDataURL(qrCodeUrl, {
      width: 200,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    })
    qrCodeDataUrl.value = dataUrl
  } catch (error) {
    console.error('生成二维码失败:', error)
    ElMessage.error('生成二维码失败')
  } finally {
    generatingQR.value = false
  }
}

// 开始设置2FA
const startSetup = async () => {
  try {
    loading.value = true
    const response = await userApi.getTwoFactorSetup()
    if (response.code === 200 && response.data) {
      setupData.value = response.data
      showSetup.value = true
      currentStep.value = 0

      // 生成二维码
      if (response.data.qrCodeUrl) {
        await generateQRCode(response.data.qrCodeUrl)
      }
    }
  } catch (error) {
    console.error('获取2FA设置信息失败:', error)
    ElMessage.error('获取2FA设置信息失败')
  } finally {
    loading.value = false
  }
}

// 取消设置
const cancelSetup = () => {
  showSetup.value = false
  currentStep.value = 0
  setupData.value = undefined
  verifyForm.totpCode = ''
  qrCodeDataUrl.value = ''
}

// 下一步
const nextStep = () => {
  if (currentStep.value < 1) {
    currentStep.value++
  }
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 验证设置
const verifySetup = async () => {
  if (!verifyFormRef.value) return

  try {
    await verifyFormRef.value.validate()
    verifying.value = true

    const response = await userApi.enableTwoFactor({
      totpCode: verifyForm.totpCode
    })

    if (response.code === 200) {
      // 直接完成设置
      showSetup.value = false
      currentStep.value = 0
      qrCodeDataUrl.value = ''
      verifyForm.totpCode = ''
      loadTwoFactorStatus()
      ElMessage.success('双因素认证启用成功')
    }
  } catch (error) {
    console.error('2FA启用失败:', error)
    ElMessage.error('验证码错误，请重试')
  } finally {
    verifying.value = false
  }
}

// 禁用2FA
const disableTwoFactor = async () => {
  if (!disableFormRef.value) return

  try {
    await disableFormRef.value.validate()
    disabling.value = true

    await userApi.disableTwoFactor({
      totpCode: disableForm.totpCode
    })

    showDisableDialog.value = false
    disableForm.totpCode = ''
    loadTwoFactorStatus()
    ElMessage.success('双因素认证已禁用')
  } catch (error) {
    console.error('禁用2FA失败:', error)
    ElMessage.error('验证码错误，请重试')
  } finally {
    disabling.value = false
  }
}

// 关闭禁用对话框
const handleDisableDialogClose = () => {
  disableFormRef.value?.resetFields()
  showDisableDialog.value = false
}

// 复制密钥
const copySecret = async () => {
  if (setupData.value?.secret) {
    try {
      await navigator.clipboard.writeText(setupData.value.secret)
      ElMessage.success('密钥已复制到剪贴板')
    } catch (error) {
      console.error('复制失败:', error)
      ElMessage.error('复制失败')
    }
  }
}

// 组件挂载时加载状态
onMounted(() => {
  loadTwoFactorStatus()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.two-factor-setup {
  .status-section {
    .status-card {
      @include glass-effect();
      border: 1px solid var(--glass-border);
      border-radius: $radius-xl;
      padding: $spacing-xl;
      margin-bottom: $spacing-lg;
      @include shadow-layered-md();

      .status-header {
        @include flex-between();
        margin-bottom: $spacing-lg;

        .status-icon {
          width: 48px;
          height: 48px;
          border-radius: $radius-lg;
          @include flex-center();
          background: rgba(255, 77, 79, 0.1);
          color: #ff4d4f;
          font-size: 24px;
          transition: all 0.3s ease;

          &.enabled {
            background: rgba(82, 196, 26, 0.1);
            color: #52c41a;
          }
        }

        .status-info {
          flex: 1;
          margin-left: $spacing-md;

          .status-title {
            font-size: $font-size-lg;
            font-weight: $font-weight-semibold;
            color: var(--text-primary);
            margin: 0 0 $spacing-xs 0;
          }

          .status-desc {
            color: var(--text-secondary);
            font-size: $font-size-sm;
            margin: 0;
          }
        }
      }

      .status-actions {
        text-align: right;
      }
    }


  }

  .setup-section {
    .setup-steps {
      :deep(.el-steps) {
        margin-bottom: $spacing-xl;
      }

      .step-content {
        min-height: 400px;
        padding: $spacing-lg;
        @include glass-effect();
        border: 1px solid var(--glass-border);
        border-radius: $radius-xl;
        @include shadow-layered-md();

        h3 {
          font-size: $font-size-xl;
          font-weight: $font-weight-semibold;
          color: var(--text-primary);
          margin: 0 0 $spacing-sm 0;
          text-align: center;
        }

        .step-desc {
          color: var(--text-secondary);
          text-align: center;
          margin-bottom: $spacing-xl;
        }

        .qr-section {
          .qr-container {
            @include flex-center();
            flex-direction: column;
            margin: $spacing-xl 0;

            .qr-loading,
            .qr-error {
              @include flex-center();
              flex-direction: column;
              gap: $spacing-md;
              color: var(--text-secondary);

              .loading-icon {
                font-size: 32px;
                animation: spin 1s linear infinite;
              }
            }

            .qr-code {
              padding: $spacing-lg;
              background: #ffffff;
              border-radius: $radius-lg;
              @include shadow-layered-sm();

              img {
                display: block;
                width: 200px;
                height: 200px;
              }
            }
          }

          .manual-entry {
            text-align: center;
            margin-top: $spacing-xl;

            .manual-title {
              color: var(--text-secondary);
              font-size: $font-size-sm;
              margin-bottom: $spacing-sm;
            }

            .secret-code {
              @include flex-center();
              gap: $spacing-sm;
              margin-top: $spacing-sm;

              code {
                background: var(--gradient-glass);
                border: 1px solid var(--glass-border);
                border-radius: $radius-md;
                padding: $spacing-sm $spacing-md;
                font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
                font-size: $font-size-sm;
                color: var(--text-primary);
                letter-spacing: 2px;
              }
            }
          }
        }

        .verify-section {
          .verify-form {
            max-width: 300px;
            margin: 0 auto;

            :deep(.el-input) {
              .el-input__wrapper {
                text-align: center;
                font-size: $font-size-lg;
                font-weight: $font-weight-medium;
                letter-spacing: 4px;
              }
            }
          }
        }



        .step-actions {
          @include flex-between();
          margin-top: $spacing-xl;
          padding-top: $spacing-lg;
          border-top: 1px solid var(--border-light);
        }
      }
    }
  }

  .disable-content {
    text-align: center;

    p {
      color: var(--text-secondary);
      margin-bottom: $spacing-lg;
    }

    .disable-form {
      max-width: 200px;
      margin: 0 auto;
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-md;
    padding-top: $spacing-sm;
  }


}

// 动画
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 响应式设计
@include respond-below('md') {
  .two-factor-setup {
    .setup-section {
      .step-content {
        padding: $spacing-md;
        min-height: 300px;

        .qr-section {
          .qr-container {
            .qr-code {
              padding: $spacing-md;

              img {
                width: 160px;
                height: 160px;
              }
            }
          }
        }



        .step-actions {
          flex-direction: column;
          gap: $spacing-sm;
        }
      }
    }

    .dialog-footer {
      flex-direction: column;
      gap: $spacing-sm;

      .modern-button {
        width: 100%;
      }
    }
  }
}
</style>
