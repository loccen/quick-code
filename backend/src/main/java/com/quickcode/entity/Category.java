package com.quickcode.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 项目分类实体类
 * 对应数据库表：project_categories
 * 支持层级分类结构
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"parent", "children", "projects"})
@Entity
@Table(name = "project_categories", indexes = {
    @Index(name = "idx_parent_id", columnList = "parent_id"),
    @Index(name = "idx_sort_order", columnList = "sort_order"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "uk_code", columnList = "code", unique = true)
})
public class Category extends BaseEntity {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 分类代码（唯一标识）
     */
    @NotBlank(message = "分类代码不能为空")
    @Size(max = 50, message = "分类代码长度不能超过50个字符")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 分类描述
     */
    @Size(max = 255, message = "分类描述长度不能超过255个字符")
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 父分类ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序顺序
     */
    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * 分类状态
     * 0: 禁用
     * 1: 正常
     */
    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 父分类关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Category parent;

    /**
     * 子分类列表
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    /**
     * 该分类下的项目列表
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    /**
     * 分类状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        ACTIVE(1, "正常");

        private final Integer code;
        private final String description;

        Status(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static Status fromCode(Integer code) {
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("未知的分类状态代码: " + code);
        }
    }

    /**
     * 检查分类是否激活
     */
    public boolean isActive() {
        return Status.ACTIVE.getCode().equals(this.status);
    }

    /**
     * 检查是否为根分类
     */
    public boolean isRootCategory() {
        return this.parentId == null;
    }

    /**
     * 检查是否有子分类
     */
    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }

    /**
     * 添加子分类
     */
    public void addChild(Category child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        child.setParentId(this.getId());
        child.setParent(this);
    }

    /**
     * 移除子分类
     */
    public void removeChild(Category child) {
        if (this.children != null) {
            this.children.remove(child);
            child.setParentId(null);
            child.setParent(null);
        }
    }

    /**
     * 获取分类层级深度
     */
    public int getDepth() {
        int depth = 0;
        Category current = this.parent;
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    /**
     * 获取分类路径（从根分类到当前分类）
     */
    public String getCategoryPath() {
        if (isRootCategory()) {
            return this.name;
        }
        
        StringBuilder path = new StringBuilder();
        List<String> names = new ArrayList<>();
        
        Category current = this;
        while (current != null) {
            names.add(0, current.getName());
            current = current.getParent();
        }
        
        return String.join(" > ", names);
    }

    /**
     * 获取该分类下的项目数量
     */
    public long getProjectCount() {
        return this.projects != null ? this.projects.size() : 0;
    }

    /**
     * 获取该分类及其子分类下的项目总数
     */
    public long getTotalProjectCount() {
        long count = getProjectCount();
        
        if (hasChildren()) {
            for (Category child : this.children) {
                count += child.getTotalProjectCount();
            }
        }
        
        return count;
    }

    /**
     * 启用分类
     */
    public void enable() {
        this.status = Status.ACTIVE.getCode();
    }

    /**
     * 禁用分类
     */
    public void disable() {
        this.status = Status.DISABLED.getCode();
    }

    /**
     * 检查是否可以删除
     * 有子分类或项目的分类不能删除
     */
    public boolean canDelete() {
        return !hasChildren() && getProjectCount() == 0;
    }

    /**
     * 预定义分类代码
     */
    public static class CategoryCode {
        public static final String WEB = "WEB";
        public static final String MOBILE = "MOBILE";
        public static final String DESKTOP = "DESKTOP";
        public static final String BACKEND = "BACKEND";
        public static final String FRONTEND = "FRONTEND";
        public static final String FULLSTACK = "FULLSTACK";
        public static final String GAME = "GAME";
        public static final String AI_ML = "AI_ML";
        public static final String BLOCKCHAIN = "BLOCKCHAIN";
        public static final String IOT = "IOT";
        public static final String DEVOPS = "DEVOPS";
        public static final String TOOL = "TOOL";
        public static final String TEMPLATE = "TEMPLATE";
        public static final String COMPONENT = "COMPONENT";
        public static final String PLUGIN = "PLUGIN";
        public static final String OTHER = "OTHER";
    }
}
