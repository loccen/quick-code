package com.quickcode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * 基础Repository接口
 * 继承JPA Repository和Specification Executor，提供基础的CRUD和动态查询功能
 * 
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author QuickCode Team
 * @since 1.0.0
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    
    // 这里可以添加通用的查询方法
    // 例如：按状态查询、软删除等
}
