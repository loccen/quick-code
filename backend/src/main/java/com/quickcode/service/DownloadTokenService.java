package com.quickcode.service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 下载令牌服务接口
 * 提供JWT下载令牌的生成、验证和缓存管理功能
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public interface DownloadTokenService {

    /**
     * 下载令牌信息
     */
    class DownloadTokenInfo {
        private final String token;
        private final Long projectId;
        private final Long userId;
        private final LocalDateTime expirationTime;
        private final Map<String, Object> metadata;

        public DownloadTokenInfo(String token, Long projectId, Long userId, 
                               LocalDateTime expirationTime, Map<String, Object> metadata) {
            this.token = token;
            this.projectId = projectId;
            this.userId = userId;
            this.expirationTime = expirationTime;
            this.metadata = metadata;
        }

        // Getters
        public String getToken() { return token; }
        public Long getProjectId() { return projectId; }
        public Long getUserId() { return userId; }
        public LocalDateTime getExpirationTime() { return expirationTime; }
        public Map<String, Object> getMetadata() { return metadata; }
    }

    /**
     * 令牌验证结果
     */
    class TokenValidationResult {
        private final boolean valid;
        private final String reason;
        private final Long projectId;
        private final Long userId;
        private final Map<String, Object> claims;

        public TokenValidationResult(boolean valid, String reason, Long projectId, 
                                   Long userId, Map<String, Object> claims) {
            this.valid = valid;
            this.reason = reason;
            this.projectId = projectId;
            this.userId = userId;
            this.claims = claims;
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getReason() { return reason; }
        public Long getProjectId() { return projectId; }
        public Long getUserId() { return userId; }
        public Map<String, Object> getClaims() { return claims; }
    }

    /**
     * 生成下载令牌
     * 
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param expirationMinutes 过期时间（分钟）
     * @param metadata 附加元数据
     * @return 下载令牌信息
     */
    DownloadTokenInfo generateDownloadToken(Long projectId, Long userId, int expirationMinutes, 
                                          Map<String, Object> metadata);

    /**
     * 验证下载令牌
     * 
     * @param token 下载令牌
     * @return 验证结果
     */
    TokenValidationResult validateDownloadToken(String token);

    /**
     * 验证下载令牌并检查项目权限
     * 
     * @param token 下载令牌
     * @param projectId 项目ID
     * @return 验证结果
     */
    TokenValidationResult validateDownloadToken(String token, Long projectId);

    /**
     * 刷新下载令牌
     * 
     * @param token 原令牌
     * @param expirationMinutes 新的过期时间（分钟）
     * @return 新的令牌信息
     */
    DownloadTokenInfo refreshDownloadToken(String token, int expirationMinutes);

    /**
     * 撤销下载令牌
     * 
     * @param token 下载令牌
     * @return 是否撤销成功
     */
    boolean revokeDownloadToken(String token);

    /**
     * 撤销用户的所有下载令牌
     * 
     * @param userId 用户ID
     * @return 撤销的令牌数量
     */
    int revokeUserDownloadTokens(Long userId);

    /**
     * 撤销项目的所有下载令牌
     * 
     * @param projectId 项目ID
     * @return 撤销的令牌数量
     */
    int revokeProjectDownloadTokens(Long projectId);

    /**
     * 获取用户的活跃下载令牌
     * 
     * @param userId 用户ID
     * @return 活跃令牌列表
     */
    java.util.List<DownloadTokenInfo> getUserActiveTokens(Long userId);

    /**
     * 获取项目的活跃下载令牌
     * 
     * @param projectId 项目ID
     * @return 活跃令牌列表
     */
    java.util.List<DownloadTokenInfo> getProjectActiveTokens(Long projectId);

    /**
     * 清理过期的下载令牌
     * 
     * @return 清理的令牌数量
     */
    int cleanupExpiredTokens();

    /**
     * 获取令牌统计信息
     * 
     * @return 统计信息
     */
    Map<String, Object> getTokenStatistics();

    /**
     * 检查令牌是否存在
     * 
     * @param token 下载令牌
     * @return 是否存在
     */
    boolean tokenExists(String token);

    /**
     * 获取令牌的剩余有效时间（秒）
     * 
     * @param token 下载令牌
     * @return 剩余有效时间，-1表示令牌无效或已过期
     */
    long getTokenRemainingTime(String token);

    /**
     * 更新令牌的元数据
     * 
     * @param token 下载令牌
     * @param metadata 新的元数据
     * @return 是否更新成功
     */
    boolean updateTokenMetadata(String token, Map<String, Object> metadata);

    /**
     * 记录令牌使用
     * 
     * @param token 下载令牌
     * @param action 使用动作
     * @param clientIp 客户端IP
     * @param userAgent 用户代理
     * @return 是否记录成功
     */
    boolean recordTokenUsage(String token, String action, String clientIp, String userAgent);
}
