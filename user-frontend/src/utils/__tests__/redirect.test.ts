/**
 * 智能重定向工具测试
 */
import { describe, expect, it, vi } from 'vitest'
import { getSmartRedirectPath, performSmartRedirect, generateLoginUrl, generateRegisterUrl, isAuthPage } from '../redirect'

describe('智能重定向工具', () => {
  describe('getSmartRedirectPath', () => {
    it('应该返回安全的重定向路径', () => {
      expect(getSmartRedirectPath('/user/profile')).toBe('/user/profile')
      expect(getSmartRedirectPath('/user/my-projects')).toBe('/user/my-projects')
      expect(getSmartRedirectPath('/market')).toBe('/market')
    })

    it('应该拒绝不安全的重定向路径', () => {
      expect(getSmartRedirectPath('http://evil.com')).toBe('/user/profile')
      expect(getSmartRedirectPath('https://evil.com')).toBe('/user/profile')
      expect(getSmartRedirectPath('//evil.com')).toBe('/user/profile')
      expect(getSmartRedirectPath('/unsafe/path')).toBe('/user/profile')
    })

    it('应该使用默认路径当没有redirect参数时', () => {
      expect(getSmartRedirectPath(null, '/user/profile')).toBe('/user/profile')
      expect(getSmartRedirectPath(null, '/market')).toBe('/market')
      expect(getSmartRedirectPath(undefined, '/user/my-projects')).toBe('/user/my-projects')
    })

    it('应该处理动态路径', () => {
      expect(getSmartRedirectPath('/project/123')).toBe('/project/123')
      expect(getSmartRedirectPath('/market/project/456')).toBe('/market/project/456')
    })

    it('应该处理带查询参数的路径', () => {
      expect(getSmartRedirectPath('/user/profile?tab=security')).toBe('/user/profile?tab=security')
      expect(getSmartRedirectPath('/market?category=web')).toBe('/market?category=web')
    })
  })

  describe('performSmartRedirect', () => {
    it('应该调用router.push进行跳转', () => {
      const mockRouter = {
        push: vi.fn(),
        currentRoute: { value: { path: '/login' } }
      }

      performSmartRedirect(mockRouter, '/user/profile')
      expect(mockRouter.push).toHaveBeenCalledWith('/user/profile')
    })

    it('应该避免跳转到当前页面', () => {
      const mockRouter = {
        push: vi.fn(),
        currentRoute: { value: { path: '/user/profile' } }
      }

      performSmartRedirect(mockRouter, '/user/profile')
      expect(mockRouter.push).not.toHaveBeenCalled()
    })
  })

  describe('isAuthPage', () => {
    it('应该正确识别认证页面', () => {
      expect(isAuthPage('/login')).toBe(true)
      expect(isAuthPage('/register')).toBe(true)
      expect(isAuthPage('/forgot-password')).toBe(true)
      expect(isAuthPage('/reset-password')).toBe(true)
    })

    it('应该正确识别非认证页面', () => {
      expect(isAuthPage('/user/profile')).toBe(false)
      expect(isAuthPage('/market')).toBe(false)
      expect(isAuthPage('/')).toBe(false)
    })
  })

  describe('generateLoginUrl', () => {
    it('应该为非认证页面生成带redirect的登录URL', () => {
      expect(generateLoginUrl('/user/profile')).toBe('/login?redirect=%2Fuser%2Fprofile')
      expect(generateLoginUrl('/market')).toBe('/login?redirect=%2Fmarket')
    })

    it('应该为认证页面生成不带redirect的登录URL', () => {
      expect(generateLoginUrl('/login')).toBe('/login')
      expect(generateLoginUrl('/register')).toBe('/login')
    })
  })

  describe('generateRegisterUrl', () => {
    it('应该为非认证页面生成带redirect的注册URL', () => {
      expect(generateRegisterUrl('/user/profile')).toBe('/register?redirect=%2Fuser%2Fprofile')
      expect(generateRegisterUrl('/market')).toBe('/register?redirect=%2Fmarket')
    })

    it('应该为认证页面生成不带redirect的注册URL', () => {
      expect(generateRegisterUrl('/login')).toBe('/register')
      expect(generateRegisterUrl('/register')).toBe('/register')
    })
  })
})
