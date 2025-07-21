package com.quickcode.dto.category;

import com.quickcode.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类DTO
 * 用于分类信息展示
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    /**
     * 分类ID
     */
    private Long id;

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
     * 父分类ID
     */
    private Long parentId;

    /**
     * 父分类名称
     */
    private String parentName;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 分类状态
     */
    private Integer status;

    /**
     * 分类状态描述
     */
    private String statusText;

    /**
     * 子分类列表
     */
    private List<CategoryDTO> children;

    /**
     * 项目数量
     */
    private Long projectCount;

    /**
     * 分类层级深度
     */
    private Integer depth;

    /**
     * 分类路径
     */
    private String categoryPath;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 从Category实体转换为CategoryDTO
     */
    public static CategoryDTO fromCategory(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .code(category.getCode())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .sortOrder(category.getSortOrder())
                .status(category.getStatus())
                .statusText(getStatusText(category.getStatus()))
                .projectCount(category.getProjectCount())
                .depth(category.getDepth())
                .categoryPath(category.getCategoryPath())
                .createdTime(category.getCreatedTime())
                .updatedTime(category.getUpdatedTime())
                .build();
    }

    /**
     * 从Category实体转换为CategoryDTO（包含子分类）
     */
    public static CategoryDTO fromCategoryWithChildren(Category category) {
        CategoryDTO dto = fromCategory(category);
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                    .map(CategoryDTO::fromCategoryWithChildren)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    /**
     * 获取状态文本描述
     */
    private static String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "禁用";
            case 1 -> "正常";
            default -> "未知";
        };
    }

    /**
     * 检查分类是否激活
     */
    public boolean isActive() {
        return Integer.valueOf(1).equals(this.status);
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
     * 检查是否有项目
     */
    public boolean hasProjects() {
        return this.projectCount != null && this.projectCount > 0;
    }

    /**
     * 获取项目数量文本
     */
    public String getProjectCountText() {
        if (projectCount == null || projectCount == 0) {
            return "0个项目";
        }
        if (projectCount >= 1000) {
            return String.format("%.1fk个项目", projectCount / 1000.0);
        }
        return projectCount + "个项目";
    }

    /**
     * 获取层级缩进文本（用于树形显示）
     */
    public String getIndentText() {
        if (depth == null || depth <= 0) {
            return "";
        }
        return "　".repeat(depth); // 使用全角空格缩进
    }

    /**
     * 获取带缩进的分类名称
     */
    public String getIndentedName() {
        return getIndentText() + name;
    }

    /**
     * 检查是否可以删除
     */
    public boolean canDelete() {
        return !hasChildren() && !hasProjects();
    }

    /**
     * 获取所有子分类ID（递归）
     */
    public List<Long> getAllChildrenIds() {
        List<Long> ids = new java.util.ArrayList<>();
        if (hasChildren()) {
            for (CategoryDTO child : children) {
                ids.add(child.getId());
                ids.addAll(child.getAllChildrenIds());
            }
        }
        return ids;
    }

    /**
     * 获取所有子分类代码（递归）
     */
    public List<String> getAllChildrenCodes() {
        List<String> codes = new java.util.ArrayList<>();
        if (hasChildren()) {
            for (CategoryDTO child : children) {
                codes.add(child.getCode());
                codes.addAll(child.getAllChildrenCodes());
            }
        }
        return codes;
    }

    /**
     * 查找子分类
     */
    public CategoryDTO findChild(Long childId) {
        if (!hasChildren()) {
            return null;
        }
        for (CategoryDTO child : children) {
            if (child.getId().equals(childId)) {
                return child;
            }
            CategoryDTO found = child.findChild(childId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 查找子分类（按代码）
     */
    public CategoryDTO findChildByCode(String code) {
        if (!hasChildren()) {
            return null;
        }
        for (CategoryDTO child : children) {
            if (child.getCode().equals(code)) {
                return child;
            }
            CategoryDTO found = child.findChildByCode(code);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * 获取总项目数量（包含子分类）
     */
    public long getTotalProjectCount() {
        long total = projectCount != null ? projectCount : 0;
        if (hasChildren()) {
            for (CategoryDTO child : children) {
                total += child.getTotalProjectCount();
            }
        }
        return total;
    }

    /**
     * 获取子分类数量
     */
    public int getChildrenCount() {
        return hasChildren() ? children.size() : 0;
    }

    /**
     * 获取所有后代分类数量（递归）
     */
    public int getDescendantCount() {
        int count = getChildrenCount();
        if (hasChildren()) {
            for (CategoryDTO child : children) {
                count += child.getDescendantCount();
            }
        }
        return count;
    }
}
