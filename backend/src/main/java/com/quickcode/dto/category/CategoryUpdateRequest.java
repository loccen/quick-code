package com.quickcode.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新分类请求DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;

    /**
     * 分类代码（唯一标识）
     */
    @NotBlank(message = "分类代码不能为空")
    @Size(max = 50, message = "分类代码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "分类代码只能包含大写字母、数字和下划线，且必须以大写字母开头")
    private String code;

    /**
     * 分类描述
     */
    @Size(max = 255, message = "分类描述长度不能超过255个字符")
    private String description;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 分类状态
     */
    private Integer status;

    /**
     * 验证请求数据
     */
    public void validate() {
        // 验证分类代码格式
        if (code != null && !code.matches("^[A-Z][A-Z0-9_]*$")) {
            throw new IllegalArgumentException("分类代码格式不正确，只能包含大写字母、数字和下划线，且必须以大写字母开头");
        }

        // 验证排序顺序
        if (sortOrder != null && sortOrder < 0) {
            throw new IllegalArgumentException("排序顺序不能为负数");
        }

        // 验证状态
        if (status != null && status != 0 && status != 1) {
            throw new IllegalArgumentException("分类状态只能是0（禁用）或1（正常）");
        }

        // 验证分类代码不能是保留字
        if (code != null && isReservedCode(code)) {
            throw new IllegalArgumentException("分类代码不能使用保留字: " + code);
        }

        // 验证不能将分类设置为自己的子分类
        if (parentId != null && parentId.equals(id)) {
            throw new IllegalArgumentException("不能将分类设置为自己的子分类");
        }
    }

    /**
     * 检查是否为保留代码
     */
    private boolean isReservedCode(String code) {
        String[] reservedCodes = {
            "ALL", "NONE", "NULL", "UNDEFINED", "UNKNOWN", "DEFAULT",
            "ADMIN", "ROOT", "SYSTEM", "PUBLIC", "PRIVATE", "TEMP",
            "TEST", "DEBUG", "CONFIG", "SETTING", "OPTION"
        };
        
        for (String reserved : reservedCodes) {
            if (reserved.equals(code.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清理和标准化数据
     */
    public void normalize() {
        // 清理分类名称
        if (name != null) {
            name = name.trim();
        }

        // 清理分类代码
        if (code != null) {
            code = code.trim().toUpperCase();
        }

        // 清理描述
        if (description != null) {
            description = description.trim();
            if (description.isEmpty()) {
                description = null;
            }
        }

        // 设置默认排序顺序
        if (sortOrder == null) {
            sortOrder = 0;
        }

        // 设置默认状态
        if (status == null) {
            status = 1; // 默认为正常状态
        }
    }

    /**
     * 检查是否为根分类
     */
    public boolean isRootCategory() {
        return parentId == null;
    }

    /**
     * 检查是否有描述
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * 检查分类是否激活
     */
    public boolean isActive() {
        return Integer.valueOf(1).equals(status);
    }

    /**
     * 检查分类代码是否有效
     */
    public boolean isValidCode() {
        return code != null && code.matches("^[A-Z][A-Z0-9_]*$") && !isReservedCode(code);
    }

    /**
     * 获取状态文本
     */
    public String getStatusText() {
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
     * 获取分类层级提示文本
     */
    public String getHierarchyHint() {
        if (isRootCategory()) {
            return "根分类";
        }
        return "子分类";
    }

    /**
     * 检查是否修改了父分类
     */
    public boolean isParentChanged(Long originalParentId) {
        if (originalParentId == null && parentId == null) {
            return false;
        }
        if (originalParentId == null || parentId == null) {
            return true;
        }
        return !originalParentId.equals(parentId);
    }

    /**
     * 检查是否修改了状态
     */
    public boolean isStatusChanged(Integer originalStatus) {
        if (originalStatus == null && status == null) {
            return false;
        }
        if (originalStatus == null || status == null) {
            return true;
        }
        return !originalStatus.equals(status);
    }

    /**
     * 检查是否修改了排序顺序
     */
    public boolean isSortOrderChanged(Integer originalSortOrder) {
        if (originalSortOrder == null && sortOrder == null) {
            return false;
        }
        if (originalSortOrder == null || sortOrder == null) {
            return true;
        }
        return !originalSortOrder.equals(sortOrder);
    }

    /**
     * 启用分类
     */
    public void enable() {
        this.status = 1;
    }

    /**
     * 禁用分类
     */
    public void disable() {
        this.status = 0;
    }

    /**
     * 设置为根分类
     */
    public void setAsRoot() {
        this.parentId = null;
    }
}
