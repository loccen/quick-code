package com.quickcode.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建分类请求DTO
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {

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

        // 验证分类代码不能是保留字
        if (code != null && isReservedCode(code)) {
            throw new IllegalArgumentException("分类代码不能使用保留字: " + code);
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
     * 生成建议的分类代码（基于名称）
     */
    public String generateCodeFromName() {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        // 移除特殊字符，转换为大写，用下划线连接
        String cleanName = name.trim()
                .replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", "") // 保留中文、英文、数字和空格
                .replaceAll("\\s+", "_") // 空格替换为下划线
                .toUpperCase();

        // 如果包含中文，转换为拼音首字母
        if (cleanName.matches(".*[\\u4e00-\\u9fa5].*")) {
            cleanName = convertChineseToPinyin(cleanName);
        }

        // 确保以字母开头
        if (!cleanName.matches("^[A-Z].*")) {
            cleanName = "CAT_" + cleanName;
        }

        // 限制长度
        if (cleanName.length() > 50) {
            cleanName = cleanName.substring(0, 50);
        }

        return cleanName;
    }

    /**
     * 简单的中文转拼音首字母（示例实现）
     */
    private String convertChineseToPinyin(String chinese) {
        // 这里是一个简化的实现，实际项目中可以使用专门的拼音库
        StringBuilder result = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fa5') {
                // 简单映射一些常用字的拼音首字母
                result.append(getChinesePinyinInitial(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 获取中文字符的拼音首字母
     */
    private char getChinesePinyinInitial(char chinese) {
        // 简化实现，实际应该使用完整的拼音库
        int unicode = (int) chinese;
        if (unicode >= 0x4e00 && unicode <= 0x9fa5) {
            // 根据Unicode范围大致判断拼音首字母
            if (unicode >= 0x4e00 && unicode <= 0x4fff) return 'A';
            if (unicode >= 0x5000 && unicode <= 0x51ff) return 'B';
            if (unicode >= 0x5200 && unicode <= 0x53ff) return 'C';
            if (unicode >= 0x5400 && unicode <= 0x55ff) return 'D';
            if (unicode >= 0x5600 && unicode <= 0x57ff) return 'E';
            if (unicode >= 0x5800 && unicode <= 0x59ff) return 'F';
            if (unicode >= 0x5a00 && unicode <= 0x5bff) return 'G';
            if (unicode >= 0x5c00 && unicode <= 0x5dff) return 'H';
            if (unicode >= 0x5e00 && unicode <= 0x5fff) return 'J';
            if (unicode >= 0x6000 && unicode <= 0x61ff) return 'K';
            if (unicode >= 0x6200 && unicode <= 0x63ff) return 'L';
            if (unicode >= 0x6400 && unicode <= 0x65ff) return 'M';
            if (unicode >= 0x6600 && unicode <= 0x67ff) return 'N';
            if (unicode >= 0x6800 && unicode <= 0x69ff) return 'P';
            if (unicode >= 0x6a00 && unicode <= 0x6bff) return 'Q';
            if (unicode >= 0x6c00 && unicode <= 0x6dff) return 'R';
            if (unicode >= 0x6e00 && unicode <= 0x6fff) return 'S';
            if (unicode >= 0x7000 && unicode <= 0x71ff) return 'T';
            if (unicode >= 0x7200 && unicode <= 0x73ff) return 'W';
            if (unicode >= 0x7400 && unicode <= 0x75ff) return 'X';
            if (unicode >= 0x7600 && unicode <= 0x77ff) return 'Y';
            if (unicode >= 0x7800 && unicode <= 0x9fa5) return 'Z';
        }
        return 'X'; // 默认返回X
    }

    /**
     * 检查分类代码是否有效
     */
    public boolean isValidCode() {
        return code != null && code.matches("^[A-Z][A-Z0-9_]*$") && !isReservedCode(code);
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
}
