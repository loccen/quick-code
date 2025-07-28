package com.quickcode.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单创建请求
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 订单备注
     */
    @Size(max = 500, message = "订单备注长度不能超过500个字符")
    private String remark;

    /**
     * 验证请求数据
     */
    public void validate() {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("项目ID必须为正数");
        }
    }

    /**
     * 标准化请求数据
     */
    public void normalize() {
        if (remark != null) {
            remark = remark.trim();
            if (remark.isEmpty()) {
                remark = null;
            }
        }
    }
}
