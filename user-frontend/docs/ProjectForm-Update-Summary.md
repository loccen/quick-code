# ProjectForm 组件更新总结

## 更新概述

本次更新对 `user-frontend/src/components/project/ProjectForm.vue` 进行了大幅简化，移除了与后端简化后的 `ProjectUploadRequest` 不匹配的多余表单字段，使前后端数据结构保持一致。

## 保留的字段

以下字段被保留，与后端 `ProjectUploadRequest` DTO 完全匹配：

1. **title** (项目标题) - 必填，1-100字符
2. **description** (项目描述) - 必填，10-2000字符  
3. **categoryId** (项目分类ID) - 必填
4. **tags** (项目标签) - 可选，最多10个，每个最长50字符
5. **price** (项目价格) - 可选，0-999999.99，0表示免费
6. **demoUrl** (演示URL) - 可选，必须以http://或https://开头
7. **techStack** (技术栈) - 必填，最少1个，最多20个，每个最长50字符
8. **coverImage** (封面图片URL) - 可选，必须以http://或https://开头

## 移除的字段

以下15个字段已被移除：

1. **isFree** - 通过 `price === 0` 来判断是否免费
2. **documentUrl** - 文档URL
3. **repositoryUrl** - 源码仓库URL
4. **features** - 项目特性
5. **installInstructions** - 安装说明
6. **usageInstructions** - 使用说明
7. **systemRequirements** - 系统要求
8. **version** - 项目版本
9. **isOpenSource** - 是否开源
10. **isCommercialUse** - 是否支持商业使用
11. **licenseType** - 许可证类型
12. **contactInfo** - 联系方式
13. **screenshots** - 项目截图
14. **publishImmediately** - 是否立即发布
15. **remarks** - 备注信息

## 主要变更

### 1. 表单结构简化

- 移除了"项目特性"、"使用说明"、"其他设置"等整个表单区块
- 保留了"基本信息"、"价格设置"、"技术信息"、"链接信息"四个核心区块
- 将"封面图片"字段移到"链接信息"区块

### 2. 价格设置优化

- 移除了"定价模式"单选框（免费/付费）
- 直接使用数字输入框，0表示免费项目
- 支持小数点后2位精度
- 价格范围：0-999999.99

### 3. 表单验证增强

- 添加了完整的字段验证规则
- URL格式验证（演示地址、封面图片）
- 数组长度验证（标签、技术栈）
- 价格范围验证

### 4. 代码清理

- 移除了未使用的常量（`commonFeatures`）
- 移除了项目属性相关的响应式变量和方法
- 简化了表单重置逻辑

## 验证规则详情

### 必填字段验证
- `title`: 1-100字符
- `description`: 10-2000字符
- `categoryId`: 必须选择
- `techStack`: 至少1个，最多20个

### 可选字段验证
- `tags`: 最多10个，每个最长50字符
- `price`: 0-999999.99，支持2位小数
- `demoUrl`: URL格式验证
- `coverImage`: URL格式验证

## 兼容性说明

### 前端兼容性
- 更新了 `ProjectEditView.vue` 中的示例数据
- 修复了 `HomeView.vue` 中的API响应处理
- 保持了组件的API接口不变（props、emits）

### 后端兼容性
- 完全匹配后端 `ProjectUploadRequest` DTO
- 支持后端的字段验证注解
- 与项目审核流程兼容

## 测试

创建了 `ProjectForm.test.ts` 测试文件，验证：
- 数据结构的正确性
- 必需字段的存在
- 已删除字段的移除
- 免费/付费项目的支持

## 使用示例

```typescript
// 免费项目
const freeProject: ProjectUploadRequest = {
  title: '开源项目',
  description: '这是一个开源项目',
  categoryId: 1,
  techStack: ['Vue.js', 'TypeScript'],
  price: 0
}

// 付费项目
const paidProject: ProjectUploadRequest = {
  title: '商业项目',
  description: '这是一个商业项目',
  categoryId: 2,
  techStack: ['React', 'Node.js'],
  price: 299.99,
  demoUrl: 'https://demo.example.com',
  coverImage: 'https://example.com/cover.jpg'
}
```

## 注意事项

1. **价格判断**: 使用 `price === 0` 或 `!price` 来判断是否为免费项目
2. **URL验证**: 演示地址和封面图片必须是完整的HTTP/HTTPS URL
3. **技术栈必填**: 至少需要选择一个技术栈
4. **向后兼容**: 组件接口保持不变，现有使用方式无需修改

## 后续优化建议

1. 考虑添加图片上传组件替代URL输入
2. 技术栈可以考虑从后端API动态加载
3. 可以添加项目模板功能，预填常用字段组合
