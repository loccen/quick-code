<template>
  <div class="profile-view">
    <!-- 页面容器 -->
    <div class="container">
      <div class="page-header">
        <h1 class="page-title">个人中心</h1>
        <p class="page-subtitle">管理您的个人信息和账户设置</p>
      </div>

      <el-row :gutter="24">
      <!-- 左侧个人信息 -->
      <el-col :xs="24" :lg="16">
        <ModernCard title="个人信息" class="profile-card">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <!-- 头像上传 -->
            <el-form-item label="头像">
              <AvatarUpload
                v-model="profileForm.avatar"
                :size="100"
                :max-size="2"
                :recommend-size="200"
                :crop-size="200"
                :enable-crop="true"
                @upload-success="handleAvatarSuccess"
                @upload-error="handleAvatarError"
                @upload-progress="handleAvatarProgress"
                data-testid="avatar-upload"
              />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="profileForm.nickname"
                data-testid="nickname-input"
                placeholder="请输入昵称"
                clearable
              />
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="profileForm.email"
                data-testid="email-input"
                placeholder="请输入邮箱"
                disabled
              />
            </el-form-item>

            <el-form-item label="个人简介" prop="bio">
              <el-input
                v-model="profileForm.bio"
                data-testid="bio-textarea"
                type="textarea"
                :rows="4"
                placeholder="请输入个人简介"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>

            <el-form-item>
              <ModernButton
                type="primary"
                :loading="saving"
                data-testid="save-button"
                @click="saveProfile"
              >
                保存信息
              </ModernButton>
              <ModernButton
                data-testid="cancel-button"
                @click="resetForm"
              >
                重置
              </ModernButton>
            </el-form-item>
          </el-form>
        </ModernCard>
      </el-col>

      <!-- 右侧安全设置 -->
      <el-col :xs="24" :lg="8">
        <ModernCard title="安全设置" class="security-card">
          <div class="security-items">
            <div class="security-item">
              <div class="security-info">
                <div class="security-title">登录密码</div>
                <div class="security-desc">定期更换密码以保护账户安全</div>
              </div>
              <ModernButton
                size="small"
                data-testid="change-password-button"
                @click="showPasswordDialog = true"
              >
                修改
              </ModernButton>
            </div>

            <div class="security-item">
              <div class="security-info">
                <div class="security-title">双因素认证</div>
                <div class="security-desc">增强账户安全性，建议开启</div>
              </div>
              <ModernButton
                size="small"
                @click="show2FADialog = true"
                data-testid="setup-2fa-button"
              >
                设置
              </ModernButton>
            </div>

            <div class="security-item">
              <div class="security-info">
                <div class="security-title">邮箱验证</div>
                <div class="security-desc">已验证 {{ profileForm.email }}</div>
              </div>
              <el-tag type="success" size="small">已验证</el-tag>
            </div>
          </div>
        </ModernCard>

        <!-- 积分账户 -->
        <ModernCard title="积分账户" class="points-card">
          <div class="points-overview">
            <div class="points-balance">
              <div class="balance-amount">{{ pointsData.balance }}</div>
              <div class="balance-label">当前积分</div>
            </div>
            <div class="points-actions">
              <ModernButton type="primary" size="small" @click="showRechargeDialog = true">
                充值积分
              </ModernButton>
              <ModernButton size="small" @click="showPointsHistory = true">
                交易记录
              </ModernButton>
            </div>
          </div>

          <div class="points-stats">
            <div class="stat-item">
              <div class="stat-value">{{ pointsData.totalEarned }}</div>
              <div class="stat-label">累计收益</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ pointsData.totalSpent }}</div>
              <div class="stat-label">累计消费</div>
            </div>
          </div>
        </ModernCard>

        <!-- 账户统计 -->
        <ModernCard title="账户统计" class="stats-card">
          <div class="account-stats">
            <div class="stat-item">
              <div class="stat-value">{{ accountStats.loginCount }}</div>
              <div class="stat-label">登录次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ accountStats.projectCount }}</div>
              <div class="stat-label">创建项目</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ accountStats.lastLogin }}</div>
              <div class="stat-label">最后登录</div>
            </div>
          </div>
        </ModernCard>
      </el-col>
      </el-row>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="400px"
      :before-close="handlePasswordDialogClose"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="当前密码" prop="currentPassword">
          <el-input
            v-model="passwordForm.currentPassword"
            data-testid="current-password-input"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            data-testid="new-password-input"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            data-testid="confirm-password-input"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <ModernButton @click="showPasswordDialog = false">取消</ModernButton>
        <ModernButton
          type="primary"
          :loading="changingPassword"
          data-testid="submit-password-button"
          @click="changePassword"
        >
          确认修改
        </ModernButton>
      </template>
    </el-dialog>

    <!-- 双因素认证设置对话框 -->
    <el-dialog
      v-model="show2FADialog"
      title="双因素认证设置"
      width="600px"
      :before-close="handle2FADialogClose"
    >
      <TwoFactorSetup @setup-complete="handle2FASetupComplete" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import AvatarUpload from '@/components/upload/AvatarUpload.vue'
import ModernButton from '@/components/ui/ModernButton.vue'
import ModernCard from '@/components/ui/ModernCard.vue'
import TwoFactorSetup from '@/components/security/TwoFactorSetup.vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import type { ChangePasswordRequest, UpdateUserRequest } from '@/types/user'

import { ElForm, ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'

const userStore = useUserStore()
const appStore = useAppStore()

// 表单引用
const profileFormRef = ref<InstanceType<typeof ElForm>>()
const passwordFormRef = ref<InstanceType<typeof ElForm>>()

// 个人信息表单
const profileForm = reactive<UpdateUserRequest & { email: string }>({
  nickname: '',
  avatar: '',
  bio: '',
  email: ''
})

// 密码修改表单
const passwordForm = reactive<ChangePasswordRequest>({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在2-20个字符', trigger: 'blur' }
  ]
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 状态
const saving = ref(false)
const changingPassword = ref(false)
const showPasswordDialog = ref(false)
const showRechargeDialog = ref(false)
const showPointsHistory = ref(false)
const show2FADialog = ref(false)

// 积分数据
const pointsData = reactive({
  balance: 2580,
  totalEarned: 5200,
  totalSpent: 2620
})



// 账户统计
const accountStats = reactive({
  loginCount: 156,
  projectCount: 12,
  lastLogin: '2024-01-15 14:30'
})

// 初始化表单数据
const initFormData = () => {
  if (userStore.user) {
    Object.assign(profileForm, {
      nickname: userStore.user.nickname || '',
      avatar: userStore.user.avatarUrl || '',
      bio: userStore.user.bio || '',
      email: userStore.user.email
    })
  }
}

// 头像上传成功
const handleAvatarSuccess = (url: string) => {
  profileForm.avatar = url
  // 不需要显示成功提示，AvatarUpload组件已经处理了
}

// 头像上传错误
const handleAvatarError = (error: Error) => {
  console.error('头像上传失败:', error)
  ElMessage.error('头像上传失败')
}

// 头像上传进度
const handleAvatarProgress = (progress: number) => {
  console.log('上传进度:', progress)
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    saving.value = true

    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { email, ...updateData } = profileForm
    const success = await userStore.updateUser(updateData)

    if (success) {
      ElMessage.success('个人信息保存成功')
    } else {
      ElMessage.error('个人信息保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

// 重置表单
const resetForm = () => {
  initFormData()
  ElMessage.info('表单已重置')
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    // 这里应该调用修改密码的API
    // const success = await userApi.changePassword(passwordForm)

    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false

    // 重置密码表单
    Object.assign(passwordForm, {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
  } catch (error) {
    console.error('密码修改失败:', error)
  } finally {
    changingPassword.value = false
  }
}

// 关闭密码对话框
const handlePasswordDialogClose = () => {
  passwordFormRef.value?.resetFields()
  showPasswordDialog.value = false
}

// 关闭2FA对话框
const handle2FADialogClose = () => {
  show2FADialog.value = false
}

// 处理2FA设置完成
const handle2FASetupComplete = () => {
  show2FADialog.value = false
  ElMessage.success('双因素认证设置完成')
}



// 页面初始化
onMounted(() => {
  appStore.setPageTitle('个人中心')
  initFormData()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;
@use '@/styles/mixins' as *;

.profile-view {
  min-height: calc(100vh - 200px);
  padding: $spacing-xl 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-lg;

    @include respond-below('lg') {
      padding: 0 $spacing-md;
    }

    @include respond-below('md') {
      padding: 0 $spacing-sm;
    }
  }

  .page-header {
    margin-bottom: $spacing-xl;

    .page-title {
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      color: var(--text-primary);
      margin: 0 0 $spacing-xs 0;
      @include gradient-text();
    }

    .page-subtitle {
      color: var(--text-secondary);
      font-size: $font-size-lg;
      margin: 0;
    }
  }

  .profile-card {
    margin-bottom: $spacing-lg;
    background: rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: $radius-2xl;
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.16),
      0 3px 6px rgba(0, 0, 0, 0.23);
    transition: all 0.3s ease;
    overflow: hidden;
    position: relative;

    // 增强毛玻璃效果的边框高光
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
      z-index: 1;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow:
        0 10px 20px rgba(0, 0, 0, 0.19),
        0 6px 6px rgba(0, 0, 0, 0.23);
    }

    .profile-form {
      .avatar-upload {
        .avatar-uploader {
          :deep(.el-upload) {
            border: 2px dashed var(--border-color);
            border-radius: $radius-xl;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: all 0.3s ease;
            @include flex-center();
            width: 100px;
            height: 100px;
            background: var(--gradient-glass);

            &:hover {
              border-color: var(--primary-color);
              box-shadow: var(--shadow-primary);
              transform: translateY(-2px);
            }
          }

          .avatar {
            width: 100px;
            height: 100px;
            box-shadow: var(--shadow-layered-sm);
            border-radius: $radius-xl;
          }

          .avatar-placeholder {
            @include flex-center();
            flex-direction: column;
            gap: $spacing-xs;
            color: var(--text-tertiary);

            .el-icon {
              font-size: 28px;
            }

            .upload-text {
              font-size: $font-size-sm;
              font-weight: 500;
            }
          }
        }
      }

      :deep(.el-form-item) {
        margin-bottom: $spacing-lg;
        position: relative;

        .el-form-item__label {
          color: var(--text-primary);
          font-weight: 500;
          font-size: $font-size-sm;
        }

        // 表单验证错误提示优化
        .el-form-item__error {
          position: absolute !important;
          top: 100% !important;
          left: 0 !important;
          right: 0 !important;
          transform: none !important;
          color: #ffffff !important;
          background: rgba(255, 77, 79, 0.9) !important;
          padding: $spacing-xs $spacing-sm !important;
          border-radius: $radius-md !important;
          font-size: $font-size-xs !important;
          font-weight: 500 !important;
          margin-top: $spacing-xs !important;
          box-shadow:
            0 2px 8px rgba(255, 77, 79, 0.3),
            0 1px 3px rgba(0, 0, 0, 0.2) !important;
          backdrop-filter: blur(10px) !important;
          -webkit-backdrop-filter: blur(10px) !important;
          border: 1px solid rgba(255, 77, 79, 0.5) !important;
          z-index: 10 !important;
        }

        .el-input,
        .el-textarea,
        .el-date-picker {
          .el-input__wrapper,
          .el-textarea__inner {
            border-radius: $radius-lg;
            transition: all 0.3s ease;
            background: rgba(255, 255, 255, 0.6);
            backdrop-filter: blur(10px);
            -webkit-backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.3);
            box-shadow:
              0 1px 3px rgba(0, 0, 0, 0.12),
              0 1px 2px rgba(0, 0, 0, 0.24);

            &:hover {
              border-color: rgba(24, 144, 255, 0.5);
              box-shadow:
                0 2px 8px rgba(0, 0, 0, 0.06),
                0 0 0 3px rgba(24, 144, 255, 0.1);
              transform: translateY(-1px);
            }

            &.is-focus {
              border-color: #1890ff;
              box-shadow:
                0 8px 25px rgba(24, 144, 255, 0.15),
                0 0 0 3px rgba(24, 144, 255, 0.1);
              transform: translateY(-2px);
            }
          }
        }
      }
    }
  }



  .security-card {
    margin-bottom: $spacing-lg;
    background: rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: $radius-2xl;
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.16),
      0 3px 6px rgba(0, 0, 0, 0.23);
    transition: all 0.3s ease;
    overflow: hidden;
    position: relative;

    // 增强毛玻璃效果的边框高光
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
      z-index: 1;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow:
        0 10px 20px rgba(0, 0, 0, 0.19),
        0 6px 6px rgba(0, 0, 0, 0.23);
    }

    .security-items {
      .security-item {
        @include flex-between();
        padding: $spacing-md 0;
        border-bottom: 1px solid var(--border-light);
        transition: all 0.3s ease;

        &:last-child {
          border-bottom: none;
        }

        &:hover {
          background: rgba(24, 144, 255, 0.05);
          backdrop-filter: blur(10px);
          -webkit-backdrop-filter: blur(10px);
          border-radius: $radius-md;
          margin: 0 (-$spacing-sm);
          padding: $spacing-md $spacing-sm;
          box-shadow:
            0 2px 8px rgba(24, 144, 255, 0.1),
            0 1px 3px rgba(0, 0, 0, 0.1);
          transform: translateX(4px);
        }

        .security-info {
          flex: 1;

          .security-title {
            font-weight: $font-weight-medium;
            color: var(--text-primary);
            margin-bottom: $spacing-xs;
          }

          .security-desc {
            color: var(--text-secondary);
            font-size: $font-size-sm;
          }
        }
      }
    }
  }

  .points-card {
    margin-bottom: $spacing-lg;
    background: rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: $radius-2xl;
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.16),
      0 3px 6px rgba(0, 0, 0, 0.23);
    transition: all 0.3s ease;
    overflow: hidden;
    position: relative;

    // 增强毛玻璃效果的边框高光
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
      z-index: 1;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow:
        0 10px 20px rgba(0, 0, 0, 0.19),
        0 6px 6px rgba(0, 0, 0, 0.23);
    }

    .points-overview {
      text-align: center;
      margin-bottom: $spacing-lg;

      .points-balance {
        margin-bottom: $spacing-md;

        .balance-amount {
          font-size: $font-size-3xl;
          font-weight: 700;
          background: var(--gradient-primary);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          margin-bottom: $spacing-xs;
        }

        .balance-label {
          color: var(--text-secondary);
          font-size: $font-size-sm;
        }
      }

      .points-actions {
        display: flex;
        gap: $spacing-sm;
        justify-content: center;
      }
    }

    .points-stats {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: $spacing-md;

      .stat-item {
        text-align: center;
        padding: $spacing-md;
        background: rgba(255, 255, 255, 0.1);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.18);
        border-radius: $radius-lg;
        box-shadow:
          0 1px 3px rgba(0, 0, 0, 0.12),
          0 1px 2px rgba(0, 0, 0, 0.24);
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow:
            0 4px 12px rgba(0, 0, 0, 0.1),
            0 0 0 1px rgba(255, 255, 255, 0.2);
          background: rgba(255, 255, 255, 0.15);
        }

        .stat-value {
          font-size: $font-size-xl;
          font-weight: 600;
          color: var(--primary-color);
          margin-bottom: $spacing-xs;
        }

        .stat-label {
          color: var(--text-secondary);
          font-size: $font-size-sm;
        }
      }
    }
  }

  .stats-card {
    background: rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: $radius-2xl;
    box-shadow:
      0 3px 6px rgba(0, 0, 0, 0.16),
      0 3px 6px rgba(0, 0, 0, 0.23);
    transition: all 0.3s ease;
    overflow: hidden;
    position: relative;

    // 增强毛玻璃效果的边框高光
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.8), transparent);
      z-index: 1;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow:
        0 10px 20px rgba(0, 0, 0, 0.19),
        0 6px 6px rgba(0, 0, 0, 0.23);
    }

    .account-stats {
      display: grid;
      grid-template-columns: 1fr;
      gap: $spacing-md;

      .stat-item {
        text-align: center;
        padding: $spacing-lg;
        background: rgba(255, 255, 255, 0.1);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        border: 1px solid rgba(255, 255, 255, 0.18);
        border-radius: $radius-xl;
        box-shadow:
          0 1px 3px rgba(0, 0, 0, 0.12),
          0 1px 2px rgba(0, 0, 0, 0.24);
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-3px);
          box-shadow:
            0 3px 6px rgba(0, 0, 0, 0.16),
            0 3px 6px rgba(0, 0, 0, 0.23);
          background: rgba(255, 255, 255, 0.15);
        }

        .stat-value {
          font-size: $font-size-2xl;
          font-weight: 700;
          background: var(--gradient-primary);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          margin-bottom: $spacing-xs;
        }

        .stat-label {
          color: var(--text-secondary);
          font-size: $font-size-sm;
          font-weight: 500;
        }
      }
    }
  }
}

// 微交互动画
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes shimmer {
  0% {
    background-position: -200px 0;
  }
  100% {
    background-position: calc(200px + 100%) 0;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.8;
  }
}

  // 页面加载动画
  .profile-card,
  .security-card,
  .points-card,
  .stats-card {
    animation: fadeInUp 0.6s ease-out;
    animation-fill-mode: both;

    &:nth-child(1) { animation-delay: 0.1s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.3s; }
    &:nth-child(4) { animation-delay: 0.4s; }
  }

  // 加载状态动画
  .loading-shimmer {
    background: linear-gradient(
      90deg,
      rgba(255, 255, 255, 0.1) 0%,
      rgba(255, 255, 255, 0.3) 50%,
      rgba(255, 255, 255, 0.1) 100%
    );
    background-size: 200px 100%;
    animation: shimmer 1.5s infinite;
  }

  // 数据更新动画
  .stat-value,
  .balance-amount {
    transition: all 0.3s ease;

    &.updating {
      animation: pulse 1s infinite;
    }
  }

// 响应式设计
@include respond-below('lg') {
  .profile-view {
    .security-card,
    .points-card,
    .stats-card {
      margin-top: $spacing-lg;
    }


  }
}

@include respond-below('md') {
  .profile-view {
    .page-header {
      .page-title {
        font-size: $font-size-2xl;
      }

      .page-subtitle {
        font-size: $font-size-base;
      }
    }

    .profile-form {
      :deep(.el-form-item) {
        .el-form-item__label {
          width: 80px !important;
        }
      }
    }

    .security-items {
      .security-item {
        flex-direction: column;
        align-items: flex-start;
        gap: $spacing-sm;
      }
    }



    .points-card {
      .points-overview {
        .balance-amount {
          font-size: $font-size-2xl;
        }

        .points-actions {
          flex-direction: column;
          gap: $spacing-xs;
        }
      }

      .points-stats {
        grid-template-columns: 1fr;
        gap: $spacing-sm;

        .stat-item {
          padding: $spacing-sm;
        }
      }
    }

    .account-stats {
      grid-template-columns: 1fr;
      gap: $spacing-sm;

      .stat-item {
        padding: $spacing-md;

        .stat-value {
          font-size: $font-size-xl;
        }
      }
    }
  }
}

// 暗色主题适配
.dark .profile-view {
  .security-items {
    .security-item {
      border-bottom-color: #434343;
    }
  }
}
</style>
