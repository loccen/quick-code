/**
 * 路由相关类型定义
 */
import type { Component } from 'vue'

/**
 * 路由元信息
 */
export interface RouteMeta {
  /** 页面标题 */
  title?: string
  /** 页面图标 */
  icon?: string
  /** 是否需要认证 */
  requiresAuth?: boolean
  /** 需要的权限 */
  permissions?: string[]
  /** 需要的角色 */
  roles?: string[]
  /** 是否在菜单中隐藏 */
  hidden?: boolean
  /** 是否固定在标签页 */
  affix?: boolean
  /** 是否缓存页面 */
  keepAlive?: boolean
  /** 面包屑路径 */
  breadcrumb?: BreadcrumbItem[]
  /** 页面布局 */
  layout?: string
  /** 排序权重 */
  order?: number
  /** 外部链接 */
  externalLink?: string
  /** 是否为根菜单 */
  isRoot?: boolean
  /** 重定向路径 */
  redirect?: string
}

// 扩展Vue Router的路由元信息类型
declare module 'vue-router' {
  interface RouteMeta extends RouteMeta {}
}

/**
 * 面包屑项
 */
export interface BreadcrumbItem {
  /** 标题 */
  title: string
  /** 路径 */
  path?: string
  /** 图标 */
  icon?: string
}

/**
 * 菜单项
 */
export interface MenuItem {
  /** 菜单ID */
  id: string
  /** 菜单名称 */
  name: string
  /** 菜单路径 */
  path: string
  /** 菜单图标 */
  icon?: string
  /** 菜单组件 */
  component?: Component | string
  /** 子菜单 */
  children?: MenuItem[]
  /** 路由元信息 */
  meta?: RouteMeta
  /** 是否隐藏 */
  hidden?: boolean
  /** 排序权重 */
  order?: number
}

/**
 * 标签页项
 */
export interface TabItem {
  /** 标签页名称 */
  name: string
  /** 标签页标题 */
  title: string
  /** 标签页路径 */
  path: string
  /** 标签页图标 */
  icon?: string
  /** 是否固定 */
  affix?: boolean
  /** 是否可关闭 */
  closable?: boolean
}

/**
 * 路由守卫类型
 */
export type RouteGuard = 'auth' | 'permission' | 'role'

/**
 * 路由配置
 */
export interface RouteConfig {
  /** 路由路径 */
  path: string
  /** 路由名称 */
  name?: string
  /** 路由组件 */
  component?: Component | string
  /** 子路由 */
  children?: RouteConfig[]
  /** 路由元信息 */
  meta?: RouteMeta
  /** 重定向 */
  redirect?: string
  /** 路由别名 */
  alias?: string | string[]
  /** 路由参数 */
  props?: boolean | object | Function
}
