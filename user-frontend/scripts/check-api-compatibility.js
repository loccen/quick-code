#!/usr/bin/env node

/**
 * API兼容性检查脚本
 * 验证前端API调用与后端API端点的兼容性
 */

import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// 前端API定义
const frontendAPIs = {
  // 用户认证相关
  auth: {
    login: { method: 'POST', path: '/api/auth/login' },
    register: { method: 'POST', path: '/api/auth/register' },
    refreshToken: { method: 'POST', path: '/api/auth/refresh' },
    logout: { method: 'POST', path: '/api/auth/logout' },
    verifyEmail: { method: 'POST', path: '/api/auth/verify-email' },
    forgotPassword: { method: 'POST', path: '/api/auth/forgot-password' },
    resetPassword: { method: 'POST', path: '/api/auth/reset-password' },
    checkUsername: { method: 'GET', path: '/api/auth/check-username' },
    checkEmail: { method: 'GET', path: '/api/auth/check-email' },
    verifyTwoFactorLogin: { method: 'POST', path: '/api/auth/verify-2fa' }
  },
  
  // 用户管理相关
  user: {
    getCurrentUser: { method: 'GET', path: '/api/users/me' },
    updateCurrentUser: { method: 'PUT', path: '/api/users/me' },
    changePassword: { method: 'POST', path: '/api/users/me/change-password' },
    uploadAvatar: { method: 'POST', path: '/api/users/me/avatar' },
    getUserById: { method: 'GET', path: '/api/users/{id}' },
    updateUser: { method: 'PUT', path: '/api/users/{id}' },
    deleteUser: { method: 'DELETE', path: '/api/users/{id}' },
    getUserList: { method: 'GET', path: '/api/users' },
    checkUsername: { method: 'GET', path: '/api/users/check-username' },
    checkEmail: { method: 'GET', path: '/api/users/check-email' }
  },
  
  // 双因素认证相关（前端已实现，后端未实现）
  twoFactor: {
    getTwoFactorStatus: { method: 'GET', path: '/api/users/me/2fa/status' },
    setupTwoFactor: { method: 'POST', path: '/api/users/me/2fa/setup' },
    verifyTwoFactor: { method: 'POST', path: '/api/users/me/2fa/verify' },
    disableTwoFactor: { method: 'POST', path: '/api/users/me/2fa/disable' },
    generateBackupCodes: { method: 'POST', path: '/api/users/me/2fa/backup-codes' },
    getTwoFactorSetup: { method: 'GET', path: '/api/users/me/2fa/setup' }
  }
}

// 后端已实现的API端点（基于代码分析）
const backendAPIs = {
  auth: {
    login: { method: 'POST', path: '/api/auth/login', implemented: true },
    register: { method: 'POST', path: '/api/auth/register', implemented: true },
    refreshToken: { method: 'POST', path: '/api/auth/refresh', implemented: true },
    logout: { method: 'POST', path: '/api/auth/logout', implemented: true },
    verifyEmail: { method: 'POST', path: '/api/auth/verify-email', implemented: true },
    forgotPassword: { method: 'POST', path: '/api/auth/forgot-password', implemented: true },
    resetPassword: { method: 'POST', path: '/api/auth/reset-password', implemented: true },
    checkUsername: { method: 'GET', path: '/api/auth/check-username', implemented: true },
    checkEmail: { method: 'GET', path: '/api/auth/check-email', implemented: true },
    verifyTwoFactorLogin: { method: 'POST', path: '/api/auth/verify-2fa', implemented: false }
  },
  
  user: {
    getCurrentUser: { method: 'GET', path: '/api/users/profile', implemented: true }, // 注意：路径不同
    updateCurrentUser: { method: 'PUT', path: '/api/users/profile', implemented: true }, // 注意：路径不同
    changePassword: { method: 'POST', path: '/api/users/change-password', implemented: true }, // 注意：路径不同
    uploadAvatar: { method: 'PUT', path: '/api/users/avatar', implemented: true }, // 注意：方法和路径不同
    getUserById: { method: 'GET', path: '/api/users/{id}', implemented: true },
    updateUser: { method: 'PUT', path: '/api/users/{id}', implemented: true },
    deleteUser: { method: 'DELETE', path: '/api/users/{id}', implemented: true },
    getUserList: { method: 'GET', path: '/api/users', implemented: true },
    checkUsername: { method: 'GET', path: '/api/users/check-username', implemented: false },
    checkEmail: { method: 'GET', path: '/api/users/check-email', implemented: false }
  },
  
  twoFactor: {
    getTwoFactorStatus: { method: 'GET', path: '/api/users/me/2fa/status', implemented: false },
    setupTwoFactor: { method: 'POST', path: '/api/users/me/2fa/setup', implemented: false },
    verifyTwoFactor: { method: 'POST', path: '/api/users/me/2fa/verify', implemented: false },
    disableTwoFactor: { method: 'POST', path: '/api/users/me/2fa/disable', implemented: false },
    generateBackupCodes: { method: 'POST', path: '/api/users/me/2fa/backup-codes', implemented: false },
    getTwoFactorSetup: { method: 'GET', path: '/api/users/me/2fa/setup', implemented: false }
  }
}

/**
 * 检查API兼容性
 */
function checkAPICompatibility() {
  console.log('🔍 开始检查前后端API兼容性...\n')
  
  let totalAPIs = 0
  let compatibleAPIs = 0
  let incompatibleAPIs = 0
  let missingAPIs = 0
  
  const issues = []
  
  // 遍历前端API
  for (const [category, apis] of Object.entries(frontendAPIs)) {
    console.log(`📂 检查 ${category} 类别API:`)
    
    for (const [apiName, frontendAPI] of Object.entries(apis)) {
      totalAPIs++
      
      const backendAPI = backendAPIs[category]?.[apiName]
      
      if (!backendAPI) {
        missingAPIs++
        issues.push({
          type: 'missing',
          category,
          apiName,
          frontend: frontendAPI,
          message: '后端缺少对应的API端点'
        })
        console.log(`  ❌ ${apiName}: 后端缺少对应的API端点`)
        continue
      }
      
      if (!backendAPI.implemented) {
        missingAPIs++
        issues.push({
          type: 'not_implemented',
          category,
          apiName,
          frontend: frontendAPI,
          backend: backendAPI,
          message: '后端API端点存在但未实现'
        })
        console.log(`  ⚠️  ${apiName}: 后端API端点存在但未实现`)
        continue
      }
      
      // 检查方法和路径是否匹配
      const methodMatch = frontendAPI.method === backendAPI.method
      const pathMatch = frontendAPI.path === backendAPI.path
      
      if (methodMatch && pathMatch) {
        compatibleAPIs++
        console.log(`  ✅ ${apiName}: 兼容`)
      } else {
        incompatibleAPIs++
        issues.push({
          type: 'incompatible',
          category,
          apiName,
          frontend: frontendAPI,
          backend: backendAPI,
          message: `方法或路径不匹配 - 前端: ${frontendAPI.method} ${frontendAPI.path}, 后端: ${backendAPI.method} ${backendAPI.path}`
        })
        console.log(`  ❌ ${apiName}: 方法或路径不匹配`)
        console.log(`     前端: ${frontendAPI.method} ${frontendAPI.path}`)
        console.log(`     后端: ${backendAPI.method} ${backendAPI.path}`)
      }
    }
    console.log('')
  }
  
  // 输出总结
  console.log('📊 兼容性检查总结:')
  console.log(`总API数量: ${totalAPIs}`)
  console.log(`兼容API: ${compatibleAPIs} (${((compatibleAPIs / totalAPIs) * 100).toFixed(1)}%)`)
  console.log(`不兼容API: ${incompatibleAPIs} (${((incompatibleAPIs / totalAPIs) * 100).toFixed(1)}%)`)
  console.log(`缺失/未实现API: ${missingAPIs} (${((missingAPIs / totalAPIs) * 100).toFixed(1)}%)`)
  console.log('')
  
  // 输出详细问题
  if (issues.length > 0) {
    console.log('🚨 发现的问题:')
    issues.forEach((issue, index) => {
      console.log(`${index + 1}. [${issue.type.toUpperCase()}] ${issue.category}.${issue.apiName}`)
      console.log(`   ${issue.message}`)
      console.log('')
    })
  }
  
  // 生成修复建议
  generateFixSuggestions(issues)
  
  return {
    total: totalAPIs,
    compatible: compatibleAPIs,
    incompatible: incompatibleAPIs,
    missing: missingAPIs,
    issues
  }
}

/**
 * 生成修复建议
 */
function generateFixSuggestions(issues) {
  console.log('💡 修复建议:')
  
  const pathMismatches = issues.filter(issue => 
    issue.type === 'incompatible' && 
    issue.frontend.path !== issue.backend.path
  )
  
  const methodMismatches = issues.filter(issue => 
    issue.type === 'incompatible' && 
    issue.frontend.method !== issue.backend.method
  )
  
  const notImplemented = issues.filter(issue => issue.type === 'not_implemented')
  const missing = issues.filter(issue => issue.type === 'missing')
  
  if (pathMismatches.length > 0) {
    console.log('\n🔧 路径不匹配问题:')
    pathMismatches.forEach(issue => {
      console.log(`- 更新前端API路径: ${issue.frontend.path} → ${issue.backend.path}`)
    })
  }
  
  if (methodMismatches.length > 0) {
    console.log('\n🔧 HTTP方法不匹配问题:')
    methodMismatches.forEach(issue => {
      console.log(`- 统一HTTP方法: ${issue.apiName} (前端: ${issue.frontend.method}, 后端: ${issue.backend.method})`)
    })
  }
  
  if (notImplemented.length > 0) {
    console.log('\n🔧 未实现的API:')
    notImplemented.forEach(issue => {
      console.log(`- 实现后端API: ${issue.apiName} (${issue.frontend.method} ${issue.frontend.path})`)
    })
  }
  
  if (missing.length > 0) {
    console.log('\n🔧 缺失的API:')
    missing.forEach(issue => {
      console.log(`- 添加后端API端点: ${issue.apiName} (${issue.frontend.method} ${issue.frontend.path})`)
    })
  }
  
  console.log('\n📋 优先级建议:')
  console.log('1. 修复路径和方法不匹配问题（影响现有功能）')
  console.log('2. 实现双因素认证相关API（新功能）')
  console.log('3. 添加缺失的API端点（完善功能）')
}

/**
 * 生成兼容性报告
 */
function generateCompatibilityReport(result) {
  const reportPath = path.join(__dirname, '../docs/api-compatibility-report.md')
  
  const report = `# API兼容性检查报告

生成时间: ${new Date().toLocaleString()}

## 总览

- 总API数量: ${result.total}
- 兼容API: ${result.compatible} (${((result.compatible / result.total) * 100).toFixed(1)}%)
- 不兼容API: ${result.incompatible} (${((result.incompatible / result.total) * 100).toFixed(1)}%)
- 缺失/未实现API: ${result.missing} (${((result.missing / result.total) * 100).toFixed(1)}%)

## 详细问题

${result.issues.map((issue, index) => `
### ${index + 1}. ${issue.category}.${issue.apiName}

**问题类型**: ${issue.type}
**描述**: ${issue.message}

${issue.frontend ? `**前端API**: ${issue.frontend.method} ${issue.frontend.path}` : ''}
${issue.backend ? `**后端API**: ${issue.backend.method} ${issue.backend.path}` : ''}
`).join('\n')}

## 修复建议

### 立即修复（影响现有功能）
- 统一用户API路径：前端使用 \`/api/users/me\`，后端使用 \`/api/users/profile\`
- 统一头像上传方法：前端使用 POST，后端使用 PUT

### 中期实现（新功能）
- 实现完整的双因素认证API
- 添加用户名/邮箱可用性检查API

### 长期优化
- 建立API版本管理机制
- 完善API文档和测试覆盖
`

  fs.writeFileSync(reportPath, report)
  console.log(`\n📄 兼容性报告已生成: ${reportPath}`)
}

// 执行检查
if (import.meta.url === `file://${process.argv[1]}`) {
  const result = checkAPICompatibility()
  generateCompatibilityReport(result)

  // 如果有不兼容的API，退出码为1
  process.exit(result.incompatible > 0 || result.missing > 0 ? 1 : 0)
}

export { checkAPICompatibility }
