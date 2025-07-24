package com.quickcode.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 项目测试数据DTO
 * 用于从JSON文件加载测试数据
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
public class ProjectTestData {
    
    /**
     * 项目标题
     */
    private String title;
    
    /**
     * 项目描述
     */
    private String description;
    
    /**
     * 分类代码（如：VUE, REACT, SPRINGBOOT等）
     */
    private String categoryCode;
    
    /**
     * 备用分类代码（当主分类不存在时使用）
     */
    private String fallbackCategoryCode;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 项目价格
     */
    private BigDecimal price;
    
    /**
     * 技术栈列表
     */
    private List<String> techStack;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 点赞次数
     */
    private Integer likeCount;
    
    /**
     * 评分
     */
    private BigDecimal rating;
    
    /**
     * 评分人数
     */
    private Integer ratingCount;
    
    /**
     * 项目状态（1-正常，0-禁用）
     */
    private Integer status;
}
