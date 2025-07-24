package com.quickcode.dto;

import lombok.Data;

/**
 * 分类测试数据DTO
 * 用于从JSON文件加载测试数据
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
public class CategoryTestData {
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类代码
     */
    private String code;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 父分类代码（如果是子分类）
     */
    private String parentCode;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 状态（1-正常，0-禁用）
     */
    private Integer status;
}
