package com.quickcode.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 * 用于存储用户收藏的项目信息
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_favorites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class UserFavorite {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 项目ID
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 关联的用户实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 关联的项目实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    /**
     * 创建收藏记录
     * 
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 收藏记录
     */
    public static UserFavorite create(Long userId, Long projectId) {
        return UserFavorite.builder()
                .userId(userId)
                .projectId(projectId)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();
    }

    /**
     * 检查是否为有效的收藏记录
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return userId != null && userId > 0 && 
               projectId != null && projectId > 0;
    }
}
