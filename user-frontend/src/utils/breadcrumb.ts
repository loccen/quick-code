/**
 * 面包屑工具函数
 */
import type { RouteLocationMatched } from 'vue-router'
import type { BreadcrumbItem } from '@/types/router'

/**
 * 生成面包屑
 */
export function generateBreadcrumb(matched: RouteLocationMatched[]): BreadcrumbItem[] {
  const breadcrumb: BreadcrumbItem[] = []

  matched.forEach((route) => {
    // 跳过没有标题的路由
    if (!route.meta?.title) return

    // 跳过隐藏的路由
    if (route.meta?.hidden) return

    breadcrumb.push({
      title: route.meta.title,
      path: route.path,
      icon: route.meta.icon
    })
  })

  return breadcrumb
}

/**
 * 获取路由面包屑
 */
export function getRouteBreadcrumb(route: any): BreadcrumbItem[] {
  // 如果路由元信息中有自定义面包屑，直接使用
  if (route.meta?.breadcrumb) {
    return route.meta.breadcrumb
  }

  // 否则根据路由匹配生成面包屑
  return generateBreadcrumb(route.matched)
}

/**
 * 格式化面包屑标题
 */
export function formatBreadcrumbTitle(title: string, params?: Record<string, any>): string {
  if (!params) return title

  let formattedTitle = title
  Object.keys(params).forEach(key => {
    formattedTitle = formattedTitle.replace(new RegExp(`{${key}}`, 'g'), params[key])
  })

  return formattedTitle
}
