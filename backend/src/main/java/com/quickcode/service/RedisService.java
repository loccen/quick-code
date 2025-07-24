package com.quickcode.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务类
 * 提供Redis操作的封装方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 项目浏览计数相关的Redis键前缀
     */
    private static final String PROJECT_VIEW_COUNT_PREFIX = "quickcode:project:view:";
    private static final String PROJECT_VIEW_BATCH_PREFIX = "quickcode:project:view:batch:";

    /**
     * 增加项目浏览次数（异步计数）
     * 使用Redis计数器，定期批量同步到数据库
     */
    public void incrementProjectViewCount(Long projectId) {
        try {
            String key = PROJECT_VIEW_COUNT_PREFIX + projectId;
            stringRedisTemplate.opsForValue().increment(key);
            
            // 设置过期时间为24小时，确保数据不会永久占用内存
            stringRedisTemplate.expire(key, Duration.ofHours(24));
            
            log.debug("项目浏览次数Redis计数增加: projectId={}", projectId);
        } catch (Exception e) {
            log.warn("Redis增加项目浏览次数失败: projectId={}", projectId, e);
        }
    }

    /**
     * 获取项目在Redis中的浏览次数
     */
    public Long getProjectViewCount(Long projectId) {
        try {
            String key = PROJECT_VIEW_COUNT_PREFIX + projectId;
            String count = stringRedisTemplate.opsForValue().get(key);
            return count != null ? Long.parseLong(count) : 0L;
        } catch (Exception e) {
            log.warn("获取项目Redis浏览次数失败: projectId={}", projectId, e);
            return 0L;
        }
    }

    /**
     * 获取并清除项目在Redis中的浏览次数
     * 用于批量同步到数据库时
     */
    public Long getAndClearProjectViewCount(Long projectId) {
        try {
            String key = PROJECT_VIEW_COUNT_PREFIX + projectId;
            String count = stringRedisTemplate.opsForValue().getAndDelete(key);
            return count != null ? Long.parseLong(count) : 0L;
        } catch (Exception e) {
            log.warn("获取并清除项目Redis浏览次数失败: projectId={}", projectId, e);
            return 0L;
        }
    }

    /**
     * 设置字符串值
     */
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置字符串值并指定过期时间
     */
    public void set(String key, String value, Duration timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout);
    }

    /**
     * 获取字符串值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    /**
     * 检查键是否存在
     */
    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    /**
     * 设置键的过期时间
     */
    public Boolean expire(String key, Duration timeout) {
        return stringRedisTemplate.expire(key, timeout);
    }

    /**
     * 获取键的剩余过期时间
     */
    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 原子递增
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 原子递增指定值
     */
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 原子递减
     */
    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    /**
     * 原子递减指定值
     */
    public Long decrement(String key, long delta) {
        return stringRedisTemplate.opsForValue().decrement(key, delta);
    }
}
