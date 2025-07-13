<template>
  <div class="profile-view">
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
              <div class="avatar-upload" data-testid="avatar-upload">
                <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-success="handleAvatarSuccess"
                  action="#"
                  :http-request="uploadAvatar"
                >
                  <el-avatar
                    v-if="profileForm.avatar"
                    :src="profileForm.avatar"
                    :size="80"
                    class="avatar"
                  />
                  <div v-else class="avatar-placeholder">
                    <el-icon><Plus /></el-icon>
                    <div class="upload-text">上传头像</div>
                  </div>
                </el-upload>
              </div>
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

            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="profileForm.phone"
                data-testid="phone-input"
                placeholder="请输入手机号"
                clearable
              />
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="profileForm.gender">
                <el-radio value="MALE">男</el-radio>
                <el-radio value="FEMALE">女</el-radio>
                <el-radio value="OTHER">其他</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="生日" prop="birthday">
              <el-date-picker
                v-model="profileForm.birthday"
                type="date"
                placeholder="请选择生日"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>

            <el-form-item label="个人简介" prop="bio">
              <el-input
                v-model="profileForm.bio"
                data-testid="bio-textarea"
                type="textarea"
                :rows="4"
                placeholder="请输入个人简介"
                maxlength="200"
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
                <div class="security-title">手机绑定</div>
                <div class="security-desc">
                  {{ profileForm.phone ? `已绑定 ${profileForm.phone}` : '未绑定手机号' }}
                </div>
              </div>
              <ModernButton size="small">
                {{ profileForm.phone ? '更换' : '绑定' }}
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
  </div>
</template>

<script setup lang="ts">
import ModernButton from '@/components/ui/ModernButton.vue'
import ModernCard from '@/components/ui/ModernCard.vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import type { ChangePasswordRequest, UpdateUserRequest } from '@/types/user'
import { Plus } from '@element-plus/icons-vue'
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
  phone: '',
  gender: undefined,
  birthday: '',
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
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
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
      avatar: userStore.user.avatar || '',
      phone: userStore.user.phone || '',
      gender: userStore.user.gender,
      birthday: userStore.user.birthday || '',
      bio: userStore.user.bio || '',
      email: userStore.user.email
    })
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  return true
}

// 自定义上传
const uploadAvatar = async ({ file }: { file: File }) => {
  try {
    const success = await userStore.uploadAvatar(file)
    if (success && userStore.user) {
      profileForm.avatar = userStore.user.avatar || ''
    }
  } catch (error) {
    console.error('头像上传失败:', error)
  }
}

// 头像上传成功
const handleAvatarSuccess = () => {
  ElMessage.success('头像上传成功')
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    saving.value = true

    const { email, ...updateData } = profileForm
    const success = await userStore.updateUser(updateData)

    if (success) {
      ElMessage.success('个人信息保存成功')
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

// 页面初始化
onMounted(() => {
  appStore.setPageTitle('个人中心')
  initFormData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.profile-view {
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

    .profile-form {
      .avatar-upload {
        .avatar-uploader {
          :deep(.el-upload) {
            border: 2px dashed var(--border-color);
            border-radius: $radius-xl;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: all var(--transition-base);
            @include flex-center();
            width: 80px;
            height: 80px;

            &:hover {
              border-color: var(--primary-color);
              @include shadow-colored(var(--primary-color), 0.2);
            }
          }

          .avatar {
            width: 80px;
            height: 80px;
            @include shadow-sm();
          }

          .avatar-placeholder {
            @include flex-center();
            flex-direction: column;
            gap: $spacing-xs;
            color: var(--text-tertiary);

            .el-icon {
              font-size: 24px;
            }

            .upload-text {
              font-size: $font-size-xs;
            }
          }
        }
      }

      :deep(.el-form-item) {
        margin-bottom: $spacing-lg;

        .el-input,
        .el-textarea,
        .el-date-picker {
          .el-input__wrapper,
          .el-textarea__inner {
            border-radius: $radius-lg;
            transition: all var(--transition-base);

            &:hover {
              border-color: rgba(var(--primary-color), 0.5);
            }

            &.is-focus {
              border-color: var(--primary-color);
              @include shadow-colored(var(--primary-color), 0.1);
            }
          }
        }
      }
    }
  }

  .security-card {
    margin-bottom: $spacing-lg;

    .security-items {
      .security-item {
        @include flex-between();
        padding: $spacing-md 0;
        border-bottom: 1px solid var(--border-light);

        &:last-child {
          border-bottom: none;
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

  .stats-card {
    .account-stats {
      display: grid;
      grid-template-columns: 1fr;
      gap: $spacing-md;

      .stat-item {
        text-align: center;
        padding: $spacing-md;
        background: var(--gradient-glass);
        border-radius: $radius-lg;
        @include shadow-sm();

        .stat-value {
          font-size: $font-size-xl;
          font-weight: $font-weight-bold;
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
}

// 响应式设计
@include respond-below('lg') {
  .profile-view {
    .security-card,
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

    .account-stats {
      grid-template-columns: repeat(3, 1fr);
      gap: $spacing-sm;

      .stat-item {
        padding: $spacing-sm;

        .stat-value {
          font-size: $font-size-lg;
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
