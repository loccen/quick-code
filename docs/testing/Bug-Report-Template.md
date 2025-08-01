# 问题报告模板

## 问题基本信息

| 字段 | 内容 |
|------|------|
| **问题ID** | BUG-[YYYY][MM][DD]-[序号] (如: BUG-20240725-001) |
| **问题标题** | [简洁明确的问题描述] |
| **发现时间** | [YYYY-MM-DD HH:MM:SS] |
| **报告人** | [测试人员姓名] |
| **严重程度** | Blocker / Critical / Major / Minor |
| **优先级** | High / Medium / Low |
| **问题状态** | New / Assigned / In Progress / Resolved / Closed |
| **相关用例** | TC-XXX |
| **指派给** | [开发人员姓名] |

## 严重程度定义

- **Blocker (阻塞)**: 导致系统无法启动或核心功能完全无法使用
- **Critical (严重)**: 导致核心功能异常，严重影响用户使用
- **Major (重要)**: 导致重要功能异常，影响用户体验
- **Minor (一般)**: 界面显示问题或非核心功能异常

## 环境信息

| 项目 | 内容 |
|------|------|
| **测试环境** | 开发环境 / 测试环境 / 预生产环境 |
| **前端版本** | user-frontend v1.0.0 |
| **后端版本** | backend v1.0.0 |
| **数据库版本** | MySQL 8.0.33 |
| **浏览器** | Chrome 115.0.5790.110 / Firefox 115.0 / Safari 16.5 |
| **操作系统** | macOS 13.4.1 / Windows 11 / Ubuntu 22.04 |
| **屏幕分辨率** | 1920x1080 / 1366x768 / 其他 |

## 问题描述

### 问题现象
[详细描述问题的具体表现，包括错误信息、异常行为等]

### 影响范围
[描述问题影响的功能模块、用户群体等]

### 业务影响
[描述问题对业务流程的影响程度]

## 重现步骤

### 前置条件
1. [描述重现问题需要的前置条件]
2. [如：用户已登录、特定数据存在等]

### 操作步骤
1. [第一步操作]
2. [第二步操作]
3. [第三步操作]
4. [继续添加步骤...]

### 测试数据
- **用户账号**: [测试账号信息]
- **测试文件**: [使用的测试文件]
- **输入数据**: [具体的输入数据]

## 预期结果 vs 实际结果

### 预期结果
[描述按照正常逻辑应该出现的结果]

### 实际结果
[描述实际观察到的结果]

### 差异分析
[分析预期结果与实际结果的差异]

## 附件信息

### 截图
- **错误截图**: [文件名或路径]
- **正常对比截图**: [文件名或路径]
- **控制台截图**: [文件名或路径]

### 录屏
- **问题重现录屏**: [文件名或路径]
- **操作流程录屏**: [文件名或路径]

### 日志文件
- **前端控制台日志**: 
  ```
  [粘贴相关的控制台错误信息]
  ```

- **后端日志**: 
  ```
  [粘贴相关的后端日志信息]
  ```

- **网络请求日志**: 
  ```
  [粘贴相关的网络请求信息]
  ```

## 技术分析

### 可能原因
1. [分析可能的技术原因1]
2. [分析可能的技术原因2]
3. [分析可能的技术原因3]

### 相关代码
- **前端相关文件**: [文件路径和行号]
- **后端相关文件**: [文件路径和行号]
- **数据库相关**: [表名和字段]

### 错误堆栈
```
[如果有错误堆栈信息，请粘贴在这里]
```

## 临时解决方案

### 绕过方法
[如果有临时的绕过方法，请详细描述]

### 风险评估
[描述临时解决方案可能带来的风险]

## 修复建议

### 建议方案
1. [修复建议1]
2. [修复建议2]
3. [修复建议3]

### 测试建议
[建议开发人员修复后需要进行的测试]

## 相关问题

### 关联问题
- **相关BUG**: BUG-XXXXXXX-XXX
- **依赖问题**: [描述依赖的其他问题]
- **重复问题**: [如果是重复问题，请标注原问题ID]

### 历史问题
[如果是历史问题的回归，请提供历史问题信息]

## 问题跟踪

### 状态变更记录
| 时间 | 状态变更 | 操作人 | 备注 |
|------|----------|--------|------|
| [时间] | New → Assigned | [操作人] | [备注] |
| [时间] | Assigned → In Progress | [操作人] | [备注] |
| [时间] | In Progress → Resolved | [操作人] | [备注] |

### 沟通记录
| 时间 | 沟通内容 | 参与人 |
|------|----------|--------|
| [时间] | [沟通内容] | [参与人] |

## 验证信息

### 修复验证
- [ ] 开发人员自测通过
- [ ] 测试人员验证通过
- [ ] 回归测试通过
- [ ] 用户验收通过

### 验证结果
[描述验证结果和验证过程]

### 关闭条件
[描述问题关闭需要满足的条件]

---

## 常见问题报告示例

### 示例1：文件上传失败

**问题ID**: BUG-20240725-001  
**问题标题**: 上传大于50MB的文件时系统报错  
**严重程度**: Major  
**优先级**: High  

**问题描述**: 
当用户尝试上传大于50MB的ZIP文件时，系统显示"上传失败"错误，但错误信息不明确。

**重现步骤**:
1. 登录系统，进入项目上传页面
2. 点击"选择文件"按钮
3. 选择一个60MB的ZIP文件
4. 点击"开始上传"按钮

**预期结果**: 
应该显示明确的错误信息，如"文件大小超过50MB限制"

**实际结果**: 
显示模糊的"上传失败"错误信息

**附件**: 
- 错误截图: error-upload-large-file.png
- 控制台日志: console-error.txt

### 示例2：技术栈选择器样式问题

**问题ID**: BUG-20240725-002  
**问题标题**: 技术栈选择器在Safari浏览器中样式异常  
**严重程度**: Minor  
**优先级**: Medium  

**问题描述**: 
在Safari浏览器中，技术栈选择器的标签显示不完整，部分文字被截断。

**重现步骤**:
1. 使用Safari浏览器打开项目上传页面
2. 进入项目信息填写步骤
3. 查看技术栈选择器区域

**预期结果**: 
技术栈标签完整显示，样式正常

**实际结果**: 
部分技术栈标签文字被截断，显示不完整

**附件**: 
- Safari截图: safari-tech-stack-issue.png
- Chrome对比截图: chrome-tech-stack-normal.png

---

**模板版本**: 1.0.0  
**最后更新**: 2024-07-25  
**维护人员**: 速码网测试团队
