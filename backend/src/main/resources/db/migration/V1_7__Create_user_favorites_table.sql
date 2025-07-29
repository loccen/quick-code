-- 创建用户收藏表
-- 用于存储用户收藏的项目信息

CREATE TABLE user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    CONSTRAINT fk_user_favorites_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_favorites_project_id FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    
    -- 唯一约束：同一用户不能重复收藏同一项目
    CONSTRAINT uk_user_project UNIQUE (user_id, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 创建索引
CREATE INDEX idx_user_favorites_user_id ON user_favorites(user_id);
CREATE INDEX idx_user_favorites_project_id ON user_favorites(project_id);
CREATE INDEX idx_user_favorites_created_time ON user_favorites(created_time);

-- 为项目表添加收藏数量字段（如果不存在）
ALTER TABLE projects 
ADD COLUMN IF NOT EXISTS favorite_count INT DEFAULT 0 COMMENT '收藏数量';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_projects_favorite_count ON projects(favorite_count);
