package com.quickcode.service.impl;

import com.quickcode.service.DownloadTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 下载令牌服务实现类
 * 使用JWT和Redis实现下载令牌的生成、验证和缓存管理
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadTokenServiceImpl implements DownloadTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.download.token.prefix:download_token:}")
    private String tokenPrefix;

    @Value("${app.download.token.user-prefix:user_tokens:}")
    private String userTokenPrefix;

    @Value("${app.download.token.project-prefix:project_tokens:}")
    private String projectTokenPrefix;

    @Value("${app.download.token.usage-prefix:token_usage:}")
    private String usagePrefix;

    /**
     * 获取JWT签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public DownloadTokenInfo generateDownloadToken(Long projectId, Long userId, int expirationMinutes, 
                                                 Map<String, Object> metadata) {
        log.debug("生成下载令牌: projectId={}, userId={}, expiration={}分钟", projectId, userId, expirationMinutes);

        try {
            // 生成唯一的令牌ID
            String tokenId = UUID.randomUUID().toString();
            
            // 计算过期时间
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(expirationMinutes);
            Date expirationDate = Date.from(expirationTime.atZone(ZoneId.systemDefault()).toInstant());

            // 构建JWT声明
            Map<String, Object> claims = new HashMap<>();
            claims.put("tokenId", tokenId);
            claims.put("projectId", projectId);
            claims.put("userId", userId);
            claims.put("type", "download");
            claims.put("createdAt", System.currentTimeMillis());
            
            if (metadata != null && !metadata.isEmpty()) {
                claims.put("metadata", metadata);
            }

            // 生成JWT令牌
            String token = Jwts.builder()
                    .subject(tokenId)
                    .issuedAt(new Date())
                    .expiration(expirationDate)
                    .claims(claims)
                    .signWith(getSigningKey())
                    .compact();

            // 存储到Redis
            String tokenKey = tokenPrefix + tokenId;
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("token", token);
            tokenData.put("projectId", projectId);
            tokenData.put("userId", userId);
            tokenData.put("expirationTime", expirationTime.toString());
            tokenData.put("metadata", metadata);
            tokenData.put("createdAt", LocalDateTime.now().toString());
            tokenData.put("active", true);

            redisTemplate.opsForHash().putAll(tokenKey, tokenData);
            redisTemplate.expire(tokenKey, expirationMinutes, TimeUnit.MINUTES);

            // 添加到用户令牌集合
            String userTokenKey = userTokenPrefix + userId;
            redisTemplate.opsForSet().add(userTokenKey, tokenId);
            redisTemplate.expire(userTokenKey, expirationMinutes, TimeUnit.MINUTES);

            // 添加到项目令牌集合
            String projectTokenKey = projectTokenPrefix + projectId;
            redisTemplate.opsForSet().add(projectTokenKey, tokenId);
            redisTemplate.expire(projectTokenKey, expirationMinutes, TimeUnit.MINUTES);

            log.info("下载令牌生成成功: tokenId={}, projectId={}, userId={}", tokenId, projectId, userId);

            return new DownloadTokenInfo(token, projectId, userId, expirationTime, metadata);

        } catch (Exception e) {
            log.error("生成下载令牌失败: projectId={}, userId={}", projectId, userId, e);
            throw new RuntimeException("生成下载令牌失败", e);
        }
    }

    @Override
    public TokenValidationResult validateDownloadToken(String token) {
        return validateDownloadToken(token, null);
    }

    @Override
    public TokenValidationResult validateDownloadToken(String token, Long projectId) {
        log.debug("验证下载令牌: projectId={}", projectId);

        try {
            if (token == null || token.trim().isEmpty()) {
                return new TokenValidationResult(false, "令牌为空", null, null, null);
            }

            // 解析JWT令牌
            Claims claims;
            try {
                claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (Exception e) {
                log.warn("JWT令牌解析失败: {}", e.getMessage());
                return new TokenValidationResult(false, "令牌格式无效", null, null, null);
            }

            // 检查令牌类型
            String tokenType = claims.get("type", String.class);
            if (!"download".equals(tokenType)) {
                return new TokenValidationResult(false, "令牌类型错误", null, null, null);
            }

            // 获取令牌信息
            String tokenId = claims.getSubject();
            Long tokenProjectId = claims.get("projectId", Long.class);
            Long tokenUserId = claims.get("userId", Long.class);

            // 检查项目ID匹配
            if (projectId != null && !projectId.equals(tokenProjectId)) {
                return new TokenValidationResult(false, "项目ID不匹配", tokenProjectId, tokenUserId, 
                                               convertClaimsToMap(claims));
            }

            // 检查Redis中的令牌状态
            String tokenKey = tokenPrefix + tokenId;
            if (!redisTemplate.hasKey(tokenKey)) {
                return new TokenValidationResult(false, "令牌已过期或被撤销", tokenProjectId, tokenUserId, 
                                               convertClaimsToMap(claims));
            }

            // 检查令牌是否活跃
            Boolean active = (Boolean) redisTemplate.opsForHash().get(tokenKey, "active");
            if (active == null || !active) {
                return new TokenValidationResult(false, "令牌已被禁用", tokenProjectId, tokenUserId, 
                                               convertClaimsToMap(claims));
            }

            log.debug("下载令牌验证成功: tokenId={}, projectId={}, userId={}", tokenId, tokenProjectId, tokenUserId);

            return new TokenValidationResult(true, "令牌有效", tokenProjectId, tokenUserId, 
                                           convertClaimsToMap(claims));

        } catch (Exception e) {
            log.error("验证下载令牌时发生异常", e);
            return new TokenValidationResult(false, "验证过程中发生异常", null, null, null);
        }
    }

    @Override
    public DownloadTokenInfo refreshDownloadToken(String token, int expirationMinutes) {
        log.debug("刷新下载令牌: expiration={}分钟", expirationMinutes);

        try {
            // 验证原令牌
            TokenValidationResult validationResult = validateDownloadToken(token);
            if (!validationResult.isValid()) {
                throw new IllegalArgumentException("原令牌无效: " + validationResult.getReason());
            }

            // 撤销原令牌
            revokeDownloadToken(token);

            // 生成新令牌
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) validationResult.getClaims().get("metadata");
            
            return generateDownloadToken(validationResult.getProjectId(), validationResult.getUserId(), 
                                       expirationMinutes, metadata);

        } catch (Exception e) {
            log.error("刷新下载令牌失败", e);
            throw new RuntimeException("刷新下载令牌失败", e);
        }
    }

    @Override
    public boolean revokeDownloadToken(String token) {
        log.debug("撤销下载令牌");

        try {
            // 验证令牌并获取信息
            TokenValidationResult validationResult = validateDownloadToken(token);
            if (!validationResult.isValid()) {
                return false;
            }

            String tokenId = (String) validationResult.getClaims().get("tokenId");
            Long userId = validationResult.getUserId();
            Long projectId = validationResult.getProjectId();

            // 从Redis中删除令牌
            String tokenKey = tokenPrefix + tokenId;
            redisTemplate.delete(tokenKey);

            // 从用户令牌集合中移除
            String userTokenKey = userTokenPrefix + userId;
            redisTemplate.opsForSet().remove(userTokenKey, tokenId);

            // 从项目令牌集合中移除
            String projectTokenKey = projectTokenPrefix + projectId;
            redisTemplate.opsForSet().remove(projectTokenKey, tokenId);

            log.info("下载令牌撤销成功: tokenId={}", tokenId);
            return true;

        } catch (Exception e) {
            log.error("撤销下载令牌失败", e);
            return false;
        }
    }

    @Override
    public int revokeUserDownloadTokens(Long userId) {
        log.debug("撤销用户所有下载令牌: userId={}", userId);

        try {
            String userTokenKey = userTokenPrefix + userId;
            Set<Object> tokenIds = redisTemplate.opsForSet().members(userTokenKey);
            
            if (tokenIds == null || tokenIds.isEmpty()) {
                return 0;
            }

            int revokedCount = 0;
            for (Object tokenIdObj : tokenIds) {
                String tokenId = (String) tokenIdObj;
                String tokenKey = tokenPrefix + tokenId;
                
                if (redisTemplate.hasKey(tokenKey)) {
                    redisTemplate.delete(tokenKey);
                    revokedCount++;
                }
            }

            // 清空用户令牌集合
            redisTemplate.delete(userTokenKey);

            log.info("用户下载令牌撤销完成: userId={}, count={}", userId, revokedCount);
            return revokedCount;

        } catch (Exception e) {
            log.error("撤销用户下载令牌失败: userId={}", userId, e);
            return 0;
        }
    }

    @Override
    public int revokeProjectDownloadTokens(Long projectId) {
        log.debug("撤销项目所有下载令牌: projectId={}", projectId);

        try {
            String projectTokenKey = projectTokenPrefix + projectId;
            Set<Object> tokenIds = redisTemplate.opsForSet().members(projectTokenKey);
            
            if (tokenIds == null || tokenIds.isEmpty()) {
                return 0;
            }

            int revokedCount = 0;
            for (Object tokenIdObj : tokenIds) {
                String tokenId = (String) tokenIdObj;
                String tokenKey = tokenPrefix + tokenId;
                
                if (redisTemplate.hasKey(tokenKey)) {
                    redisTemplate.delete(tokenKey);
                    revokedCount++;
                }
            }

            // 清空项目令牌集合
            redisTemplate.delete(projectTokenKey);

            log.info("项目下载令牌撤销完成: projectId={}, count={}", projectId, revokedCount);
            return revokedCount;

        } catch (Exception e) {
            log.error("撤销项目下载令牌失败: projectId={}", projectId, e);
            return 0;
        }
    }

    @Override
    public List<DownloadTokenInfo> getUserActiveTokens(Long userId) {
        log.debug("获取用户活跃下载令牌: userId={}", userId);

        try {
            String userTokenKey = userTokenPrefix + userId;
            Set<Object> tokenIds = redisTemplate.opsForSet().members(userTokenKey);

            if (tokenIds == null || tokenIds.isEmpty()) {
                return new ArrayList<>();
            }

            List<DownloadTokenInfo> activeTokens = new ArrayList<>();
            for (Object tokenIdObj : tokenIds) {
                String tokenId = (String) tokenIdObj;
                String tokenKey = tokenPrefix + tokenId;

                Map<Object, Object> tokenData = redisTemplate.opsForHash().entries(tokenKey);
                if (!tokenData.isEmpty()) {
                    Boolean active = (Boolean) tokenData.get("active");
                    if (active != null && active) {
                        DownloadTokenInfo tokenInfo = buildTokenInfoFromData(tokenData);
                        if (tokenInfo != null) {
                            activeTokens.add(tokenInfo);
                        }
                    }
                }
            }

            return activeTokens;

        } catch (Exception e) {
            log.error("获取用户活跃下载令牌失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<DownloadTokenInfo> getProjectActiveTokens(Long projectId) {
        log.debug("获取项目活跃下载令牌: projectId={}", projectId);

        try {
            String projectTokenKey = projectTokenPrefix + projectId;
            Set<Object> tokenIds = redisTemplate.opsForSet().members(projectTokenKey);

            if (tokenIds == null || tokenIds.isEmpty()) {
                return new ArrayList<>();
            }

            List<DownloadTokenInfo> activeTokens = new ArrayList<>();
            for (Object tokenIdObj : tokenIds) {
                String tokenId = (String) tokenIdObj;
                String tokenKey = tokenPrefix + tokenId;

                Map<Object, Object> tokenData = redisTemplate.opsForHash().entries(tokenKey);
                if (!tokenData.isEmpty()) {
                    Boolean active = (Boolean) tokenData.get("active");
                    if (active != null && active) {
                        DownloadTokenInfo tokenInfo = buildTokenInfoFromData(tokenData);
                        if (tokenInfo != null) {
                            activeTokens.add(tokenInfo);
                        }
                    }
                }
            }

            return activeTokens;

        } catch (Exception e) {
            log.error("获取项目活跃下载令牌失败: projectId={}", projectId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public int cleanupExpiredTokens() {
        log.debug("清理过期的下载令牌");

        try {
            // Redis的TTL机制会自动清理过期的键，这里主要是清理可能残留的引用
            Set<String> keys = redisTemplate.keys(tokenPrefix + "*");
            if (keys == null || keys.isEmpty()) {
                return 0;
            }

            int cleanedCount = 0;
            for (String key : keys) {
                Long ttl = redisTemplate.getExpire(key);
                if (ttl != null && ttl <= 0) {
                    redisTemplate.delete(key);
                    cleanedCount++;
                }
            }

            log.info("过期下载令牌清理完成: count={}", cleanedCount);
            return cleanedCount;

        } catch (Exception e) {
            log.error("清理过期下载令牌失败", e);
            return 0;
        }
    }

    @Override
    public Map<String, Object> getTokenStatistics() {
        log.debug("获取令牌统计信息");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 统计活跃令牌数量
            Set<String> tokenKeys = redisTemplate.keys(tokenPrefix + "*");
            int activeTokenCount = tokenKeys != null ? tokenKeys.size() : 0;

            // 统计用户令牌数量
            Set<String> userTokenKeys = redisTemplate.keys(userTokenPrefix + "*");
            int activeUserCount = userTokenKeys != null ? userTokenKeys.size() : 0;

            // 统计项目令牌数量
            Set<String> projectTokenKeys = redisTemplate.keys(projectTokenPrefix + "*");
            int activeProjectCount = projectTokenKeys != null ? projectTokenKeys.size() : 0;

            statistics.put("activeTokenCount", activeTokenCount);
            statistics.put("activeUserCount", activeUserCount);
            statistics.put("activeProjectCount", activeProjectCount);
            statistics.put("timestamp", LocalDateTime.now().toString());

            return statistics;

        } catch (Exception e) {
            log.error("获取令牌统计信息失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean tokenExists(String token) {
        try {
            TokenValidationResult result = validateDownloadToken(token);
            return result.isValid();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getTokenRemainingTime(String token) {
        try {
            TokenValidationResult result = validateDownloadToken(token);
            if (!result.isValid()) {
                return -1;
            }

            String tokenId = (String) result.getClaims().get("tokenId");
            String tokenKey = tokenPrefix + tokenId;

            Long ttl = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;

        } catch (Exception e) {
            log.error("获取令牌剩余时间失败", e);
            return -1;
        }
    }

    @Override
    public boolean updateTokenMetadata(String token, Map<String, Object> metadata) {
        try {
            TokenValidationResult result = validateDownloadToken(token);
            if (!result.isValid()) {
                return false;
            }

            String tokenId = (String) result.getClaims().get("tokenId");
            String tokenKey = tokenPrefix + tokenId;

            redisTemplate.opsForHash().put(tokenKey, "metadata", metadata);
            return true;

        } catch (Exception e) {
            log.error("更新令牌元数据失败", e);
            return false;
        }
    }

    @Override
    public boolean recordTokenUsage(String token, String action, String clientIp, String userAgent) {
        try {
            TokenValidationResult result = validateDownloadToken(token);
            if (!result.isValid()) {
                return false;
            }

            String tokenId = (String) result.getClaims().get("tokenId");
            String usageKey = usagePrefix + tokenId;

            Map<String, Object> usageRecord = new HashMap<>();
            usageRecord.put("action", action);
            usageRecord.put("clientIp", clientIp);
            usageRecord.put("userAgent", userAgent);
            usageRecord.put("timestamp", LocalDateTime.now().toString());

            redisTemplate.opsForList().rightPush(usageKey, usageRecord);
            redisTemplate.expire(usageKey, 7, TimeUnit.DAYS); // 保留7天的使用记录

            return true;

        } catch (Exception e) {
            log.error("记录令牌使用失败", e);
            return false;
        }
    }

    /**
     * 从Redis数据构建DownloadTokenInfo
     */
    @SuppressWarnings("unchecked")
    private DownloadTokenInfo buildTokenInfoFromData(Map<Object, Object> tokenData) {
        try {
            String token = (String) tokenData.get("token");
            Long projectId = Long.valueOf(tokenData.get("projectId").toString());
            Long userId = Long.valueOf(tokenData.get("userId").toString());
            String expirationTimeStr = (String) tokenData.get("expirationTime");
            LocalDateTime expirationTime = LocalDateTime.parse(expirationTimeStr);
            Map<String, Object> metadata = (Map<String, Object>) tokenData.get("metadata");

            return new DownloadTokenInfo(token, projectId, userId, expirationTime, metadata);

        } catch (Exception e) {
            log.error("构建DownloadTokenInfo失败", e);
            return null;
        }
    }

    /**
     * 将JWT Claims转换为Map
     */
    private Map<String, Object> convertClaimsToMap(Claims claims) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
