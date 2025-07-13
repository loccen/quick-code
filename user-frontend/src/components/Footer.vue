<template>
  <footer class="footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-section">
          <div class="footer-brand">
            <div class="logo">
              <i class="fas fa-code"></i>
              <span class="brand-name">速码网</span>
            </div>
            <p class="footer-description">
              专业的源码交易平台，为开发者提供高质量的源码交易和部署服务
            </p>
          </div>
        </div>
        <div class="footer-section">
          <h4 class="footer-title">产品</h4>
          <ul class="footer-links">
            <li><router-link to="/market">项目市场</router-link></li>
            <li><a href="#" @click.prevent="handleFeatureClick('deploy')">一键部署</a></li>
            <li><a href="#" @click.prevent="handleFeatureClick('points')">积分系统</a></li>
            <li><a href="#" @click.prevent="handleFeatureClick('api')">API文档</a></li>
          </ul>
        </div>
        <div class="footer-section">
          <h4 class="footer-title">支持</h4>
          <ul class="footer-links">
            <li><a href="#" @click.prevent="handleFeatureClick('help')">帮助中心</a></li>
            <li><a href="#" @click.prevent="handleFeatureClick('guide')">开发者指南</a></li>
            <li><a href="#" @click.prevent="handleFeatureClick('contact')">联系我们</a></li>
            <li><a href="#" @click.prevent="handleFeatureClick('feedback')">意见反馈</a></li>
          </ul>
        </div>
        <div class="footer-section">
          <h4 class="footer-title">关于</h4>
          <ul class="footer-links">
            <li><a href="#" @click.prevent="handleFeatureClick('terms')">服务条款</a></li>
            <li><a href="#" @click.prevent="handleFeatureClick('privacy')">隐私政策</a></li>
            <li><a href="#" @click.prevent="handleFeatureClick('join')">加入我们</a></li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2024 速码网. 保留所有权利.</p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'

/**
 * 处理功能点击（开发中的功能）
 */
const handleFeatureClick = (feature: string) => {
  const featureNames: Record<string, string> = {
    deploy: '一键部署',
    points: '积分系统',
    api: 'API文档',
    help: '帮助中心',
    guide: '开发者指南',
    contact: '联系我们',
    feedback: '意见反馈',
    terms: '服务条款',
    privacy: '隐私政策',
    join: '加入我们'
  }
  
  ElMessage.info(`${featureNames[feature]}功能开发中...`)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.footer {
  background: $bg-dark;
  color: white;
  padding: $spacing-3xl 0 $spacing-lg;
  margin-top: auto; // 确保 footer 在页面底部

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 $spacing-lg;
  }

  .footer-content {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1fr;
    gap: $spacing-xl;
    margin-bottom: $spacing-xl;

    .footer-section {
      .footer-brand {
        .logo {
          display: flex;
          align-items: center;
          gap: $spacing-sm;
          font-size: $font-size-xl;
          font-weight: 700;
          margin-bottom: $spacing-md;
          color: white;

          i {
            color: $primary-color;
          }

          .brand-name {
            color: white;
          }
        }

        .footer-description {
          color: rgba(255, 255, 255, 0.7);
          line-height: 1.6;
          margin: 0;
        }
      }

      .footer-title {
        font-size: $font-size-lg;
        font-weight: 600;
        margin-bottom: $spacing-md;
        color: white;
      }

      .footer-links {
        display: flex;
        flex-direction: column;
        gap: $spacing-sm;
        list-style: none;
        margin: 0;
        padding: 0;

        li {
          a {
            color: rgba(255, 255, 255, 0.7);
            text-decoration: none;
            transition: color 0.3s ease;
            cursor: pointer;

            &:hover {
              color: white;
            }
          }
        }
      }
    }
  }

  .footer-bottom {
    padding-top: $spacing-lg;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    text-align: center;
    color: rgba(255, 255, 255, 0.7);

    p {
      margin: 0;
    }
  }
}

// 响应式设计
@media (max-width: 991px) {
  .footer {
    .footer-content {
      grid-template-columns: repeat(2, 1fr);
      gap: $spacing-lg;
    }
  }
}

@media (max-width: 767px) {
  .footer {
    padding: $spacing-2xl 0 $spacing-lg;

    .container {
      padding: 0 $spacing-md;
    }

    .footer-content {
      grid-template-columns: 1fr;
      gap: $spacing-lg;
      text-align: center;

      .footer-section {
        .footer-brand {
          .logo {
            justify-content: center;
          }
        }
      }
    }
  }
}
</style>
