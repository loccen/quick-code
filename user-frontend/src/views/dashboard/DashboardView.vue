<template>
  <div class="dashboard-view">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title" data-testid="page-title">仪表盘</h1>
      <p class="page-subtitle">欢迎回来，{{ userStore.user?.nickname || userStore.user?.username }}！</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <ModernCard
        v-for="(stat, index) in statsData"
        :key="stat.title"
        class="stats-card"
        :variant="stat.variant"
        data-testid="stats-card"
      >
        <div class="stats-content">
          <div class="stats-icon">
            <el-icon :size="32">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-value" data-testid="stats-value">{{ stat.value }}</div>
            <div class="stats-label">{{ stat.title }}</div>
            <div class="stats-change" :class="stat.changeType">
              <el-icon>
                <ArrowUp v-if="stat.changeType === 'increase'" />
                <ArrowDown v-else />
              </el-icon>
              {{ stat.change }}
            </div>
          </div>
        </div>
      </ModernCard>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content" data-testid="main-content">
      <el-row :gutter="24">
        <!-- 左侧内容 -->
        <el-col :xs="24" :lg="16">
          <!-- 图表卡片 -->
          <ModernCard title="数据趋势" class="chart-card">
            <template #extra>
              <el-button-group>
                <el-button
                  v-for="period in chartPeriods"
                  :key="period.value"
                  :type="selectedPeriod === period.value ? 'primary' : 'default'"
                  size="small"
                  @click="selectedPeriod = period.value"
                >
                  {{ period.label }}
                </el-button>
              </el-button-group>
            </template>

            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon :size="64"><TrendCharts /></el-icon>
                <p>图表数据加载中...</p>
              </div>
            </div>
          </ModernCard>

          <!-- 最近活动 -->
          <ModernCard title="最近活动" class="activity-card">
            <template #extra>
              <el-button type="text" size="small">查看全部</el-button>
            </template>

            <div class="activity-list">
              <div
                v-for="activity in recentActivities"
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-avatar">
                  <el-avatar :size="32" :src="activity.avatar">
                    <el-icon><User /></el-icon>
                  </el-avatar>
                </div>
                <div class="activity-content">
                  <div class="activity-text">{{ activity.text }}</div>
                  <div class="activity-time">{{ activity.time }}</div>
                </div>
                <div class="activity-type">
                  <el-tag :type="activity.type" size="small">{{ activity.label }}</el-tag>
                </div>
              </div>
            </div>
          </ModernCard>
        </el-col>

        <!-- 右侧内容 -->
        <el-col :xs="24" :lg="8">
          <!-- 快速操作 -->
          <ModernCard title="快速操作" class="quick-actions-card">
            <div class="quick-actions">
              <ModernButton
                v-for="action in quickActions"
                :key="action.title"
                :type="action.type"
                :icon="action.icon"
                class="action-button"
                @click="handleQuickAction(action.action)"
              >
                {{ action.title }}
              </ModernButton>
            </div>
          </ModernCard>

          <!-- 系统状态 -->
          <ModernCard title="系统状态" class="system-status-card">
            <div class="status-list">
              <div
                v-for="status in systemStatus"
                :key="status.name"
                class="status-item"
              >
                <div class="status-info">
                  <span class="status-name">{{ status.name }}</span>
                  <span class="status-value">{{ status.value }}</span>
                </div>
                <el-progress
                  :percentage="status.percentage"
                  :color="status.color"
                  :show-text="false"
                  :stroke-width="6"
                />
              </div>
            </div>
          </ModernCard>

          <!-- 通知中心 -->
          <ModernCard title="通知中心" class="notifications-card">
            <template #extra>
              <el-badge :value="notifications.length" :max="99">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </template>

            <div class="notifications-list">
              <div
                v-for="notification in notifications"
                :key="notification.id"
                class="notification-item"
                :class="{ 'unread': !notification.read }"
              >
                <div class="notification-icon">
                  <el-icon :color="notification.color">
                    <component :is="notification.icon" />
                  </el-icon>
                </div>
                <div class="notification-content">
                  <div class="notification-title">{{ notification.title }}</div>
                  <div class="notification-time">{{ notification.time }}</div>
                </div>
              </div>
            </div>
          </ModernCard>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import ModernButton from '@/components/ui/ModernButton.vue'
import ModernCard from '@/components/ui/ModernCard.vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import {
    ArrowDown,
    ArrowUp,
    Bell,
    TrendCharts,
    User
} from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 选中的时间周期
const selectedPeriod = ref('week')

// 时间周期选项
const chartPeriods = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本年', value: 'year' }
]

// 统计数据
const statsData = ref([
  {
    title: '总项目数',
    value: '24',
    change: '+12%',
    changeType: 'increase',
    icon: 'FolderOpened',
    variant: 'primary'
  },
  {
    title: '活跃用户',
    value: '1,234',
    change: '+8%',
    changeType: 'increase',
    icon: 'User',
    variant: 'success'
  },
  {
    title: '代码提交',
    value: '856',
    change: '+15%',
    changeType: 'increase',
    icon: 'DataAnalysis',
    variant: 'warning'
  },
  {
    title: '系统负载',
    value: '68%',
    change: '-5%',
    changeType: 'decrease',
    icon: 'TrendCharts',
    variant: 'error'
  }
])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    text: '张三 创建了新项目 "Vue3管理系统"',
    time: '2分钟前',
    avatar: '',
    type: 'success',
    label: '创建'
  },
  {
    id: 2,
    text: '李四 提交了代码到 "React项目"',
    time: '5分钟前',
    avatar: '',
    type: 'primary',
    label: '提交'
  },
  {
    id: 3,
    text: '王五 完成了任务 "用户界面优化"',
    time: '10分钟前',
    avatar: '',
    type: 'warning',
    label: '完成'
  }
])

// 快速操作
const quickActions = ref([
  {
    title: '创建项目',
    action: 'createProject',
    type: 'primary',
    icon: 'Plus'
  },
  {
    title: '项目管理',
    action: 'manageProjects',
    type: 'default',
    icon: 'FolderOpened'
  },
  {
    title: '系统设置',
    action: 'systemSettings',
    type: 'default',
    icon: 'Setting'
  }
])

// 系统状态
const systemStatus = ref([
  {
    name: 'CPU使用率',
    value: '45%',
    percentage: 45,
    color: '#67c23a'
  },
  {
    name: '内存使用率',
    value: '68%',
    percentage: 68,
    color: '#e6a23c'
  },
  {
    name: '磁盘使用率',
    value: '32%',
    percentage: 32,
    color: '#409eff'
  }
])

// 通知列表
const notifications = ref([
  {
    id: 1,
    title: '系统维护通知',
    time: '1小时前',
    icon: 'Setting',
    color: '#409eff',
    read: false
  },
  {
    id: 2,
    title: '新用户注册',
    time: '2小时前',
    icon: 'User',
    color: '#67c23a',
    read: false
  },
  {
    id: 3,
    title: '数据备份完成',
    time: '3小时前',
    icon: 'DataAnalysis',
    color: '#e6a23c',
    read: true
  }
])

// 处理快速操作
const handleQuickAction = (action: string) => {
  switch (action) {
    case 'createProject':
      router.push('/projects/create')
      break
    case 'manageProjects':
      router.push('/projects')
      break
    case 'systemSettings':
      router.push('/settings')
      break
  }
}

// 页面初始化
onMounted(() => {
  appStore.setPageTitle('仪表盘')
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dashboard-view {
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

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: $spacing-lg;
    margin-bottom: $spacing-xl;

    .stats-card {
      .stats-content {
        @include flex-center();
        gap: $spacing-md;

        .stats-icon {
          @include flex-center();
          width: 64px;
          height: 64px;
          border-radius: $radius-xl;
          background: var(--gradient-glass);
          color: var(--primary-color);
          @include shadow-sm();
        }

        .stats-info {
          flex: 1;

          .stats-value {
            font-size: $font-size-2xl;
            font-weight: $font-weight-bold;
            color: var(--text-white);
            margin-bottom: $spacing-xs;
          }

          .stats-label {
            color: rgba(255, 255, 255, 0.8);
            font-size: $font-size-sm;
            margin-bottom: $spacing-xs;
          }

          .stats-change {
            @include flex-center();
            gap: $spacing-xs;
            font-size: $font-size-xs;
            font-weight: $font-weight-medium;

            &.increase {
              color: var(--success-color);
            }

            &.decrease {
              color: var(--error-color);
            }
          }
        }
      }
    }
  }

  .main-content {
    .chart-card {
      margin-bottom: $spacing-lg;

      .chart-container {
        height: 300px;
        @include flex-center();

        .chart-placeholder {
          @include flex-center();
          flex-direction: column;
          gap: $spacing-md;
          color: var(--text-tertiary);

          p {
            margin: 0;
            font-size: $font-size-sm;
          }
        }
      }
    }

    .activity-card {
      .activity-list {
        .activity-item {
          @include flex-center();
          gap: $spacing-md;
          padding: $spacing-md 0;
          border-bottom: 1px solid var(--border-light);

          &:last-child {
            border-bottom: none;
          }

          .activity-avatar {
            flex-shrink: 0;
          }

          .activity-content {
            flex: 1;

            .activity-text {
              color: var(--text-primary);
              font-size: $font-size-sm;
              margin-bottom: $spacing-xs;
            }

            .activity-time {
              color: var(--text-tertiary);
              font-size: $font-size-xs;
            }
          }

          .activity-type {
            flex-shrink: 0;
          }
        }
      }
    }

    .quick-actions-card {
      margin-bottom: $spacing-lg;

      .quick-actions {
        display: grid;
        gap: $spacing-sm;

        .action-button {
          justify-content: flex-start;
        }
      }
    }

    .system-status-card {
      margin-bottom: $spacing-lg;

      .status-list {
        .status-item {
          margin-bottom: $spacing-md;

          &:last-child {
            margin-bottom: 0;
          }

          .status-info {
            @include flex-between();
            margin-bottom: $spacing-xs;

            .status-name {
              color: var(--text-secondary);
              font-size: $font-size-sm;
            }

            .status-value {
              color: var(--text-primary);
              font-weight: $font-weight-medium;
              font-size: $font-size-sm;
            }
          }
        }
      }
    }

    .notifications-card {
      .notifications-list {
        .notification-item {
          @include flex-center();
          gap: $spacing-sm;
          padding: $spacing-sm 0;
          border-bottom: 1px solid var(--border-light);
          transition: all var(--transition-fast);

          &:last-child {
            border-bottom: none;
          }

          &.unread {
            background: rgba(var(--primary-color), 0.05);
            border-radius: $radius-md;
            padding: $spacing-sm;
            margin: $spacing-xs 0;
          }

          &:hover {
            background: var(--bg-secondary);
            border-radius: $radius-md;
            padding: $spacing-sm;
          }

          .notification-icon {
            flex-shrink: 0;
          }

          .notification-content {
            flex: 1;

            .notification-title {
              color: var(--text-primary);
              font-size: $font-size-sm;
              margin-bottom: $spacing-xs;
            }

            .notification-time {
              color: var(--text-tertiary);
              font-size: $font-size-xs;
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@include respond-below('lg') {
  .dashboard-view {
    .stats-grid {
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: $spacing-md;
    }

    .main-content {
      .chart-card {
        .chart-container {
          height: 250px;
        }
      }
    }
  }
}

@include respond-below('md') {
  .dashboard-view {
    .page-header {
      .page-title {
        font-size: $font-size-2xl;
      }

      .page-subtitle {
        font-size: $font-size-base;
      }
    }

    .stats-grid {
      grid-template-columns: 1fr;
      gap: $spacing-sm;

      .stats-card {
        .stats-content {
          .stats-icon {
            width: 48px;
            height: 48px;
          }

          .stats-info {
            .stats-value {
              font-size: $font-size-xl;
            }
          }
        }
      }
    }
  }
}

// 暗色主题适配
.dark .dashboard-view {
  .main-content {
    .activity-card,
    .notifications-card {
      .activity-item,
      .notification-item {
        border-bottom-color: #434343;

        &:hover {
          background: #2a2a2a;
        }

        &.unread {
          background: rgba(var(--primary-color), 0.1);
        }
      }
    }
  }
}
</style>
