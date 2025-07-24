package com.quickcode.service;

import com.quickcode.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

/**
 * 项目浏览次数同步服务
 * 定期将Redis中的浏览计数同步到数据库
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectViewCountSyncService {

    private final RedisService redisService;
    private final ProjectRepository projectRepository;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String PROJECT_VIEW_COUNT_PREFIX = "quickcode:project:view:";
    private static final String SYNC_LOCK_KEY = "quickcode:sync:view-count";
    private static final int MAX_BATCH_SIZE = 1000;

    /**
     * 定时同步Redis中的浏览计数到数据库
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    @Async
    @Transactional
    public void syncViewCountsToDatabase() {
        // 添加分布式锁，防止多实例重复同步
        Boolean lockAcquired = stringRedisTemplate.opsForValue()
            .setIfAbsent(SYNC_LOCK_KEY, "1", Duration.ofMinutes(10));

        if (Boolean.FALSE.equals(lockAcquired)) {
            log.debug("同步任务已在其他实例运行，跳过本次执行");
            return;
        }

        try {
            log.debug("开始同步项目浏览次数到数据库...");

            // 获取所有项目浏览计数的Redis键
            Set<String> keys = stringRedisTemplate.keys(PROJECT_VIEW_COUNT_PREFIX + "*");

            if (keys == null || keys.isEmpty()) {
                log.debug("没有需要同步的项目浏览次数");
                return;
            }

            // 限制单次处理数量，避免长时间占用
            List<String> limitedKeys = keys.stream()
                .limit(MAX_BATCH_SIZE)
                .toList();

            // 批量获取Redis数据
            List<String> values = stringRedisTemplate.opsForValue().multiGet(limitedKeys);

            // 批量处理数据
            int syncCount = processBatchViewCounts(limitedKeys, values);

            if (syncCount > 0) {
                log.info("项目浏览次数同步完成，共同步 {} 个项目", syncCount);
            }

        } catch (Exception e) {
            log.error("同步项目浏览次数到数据库失败", e);
        } finally {
            // 释放分布式锁
            stringRedisTemplate.delete(SYNC_LOCK_KEY);
        }
    }

    /**
     * 批量处理浏览次数数据
     */
    private int processBatchViewCounts(List<String> keys, List<String> values) {
        int syncCount = 0;
        List<String> processedKeys = new ArrayList<>();

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = values.get(i);

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            try {
                // 从Redis键中提取项目ID
                String projectIdStr = key.substring(PROJECT_VIEW_COUNT_PREFIX.length());
                Long projectId = Long.parseLong(projectIdStr);
                Integer count = Integer.parseInt(value);

                if (count > 0) {
                    // 更新数据库中的浏览次数
                    int updatedRows = projectRepository.incrementViewCountByAmount(projectId, count);

                    if (updatedRows > 0) {
                        syncCount++;
                        processedKeys.add(key);
                        log.debug("同步项目浏览次数成功: projectId={}, count={}", projectId, count);
                    } else {
                        log.warn("同步项目浏览次数失败，项目可能不存在: projectId={}", projectId);
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("无效的数据格式: key={}, value={}", key, value);
            } catch (Exception e) {
                log.error("同步单个项目浏览次数失败: key={}", key, e);
            }
        }

        // 批量删除已处理的Redis键
        if (!processedKeys.isEmpty()) {
            stringRedisTemplate.delete(processedKeys);
            log.debug("删除已处理的Redis键: {}", processedKeys.size());
        }

        return syncCount;
    }

    /**
     * 手动触发同步（用于测试或紧急情况）
     */
    @Async
    @Transactional
    public void manualSyncViewCounts() {
        log.info("手动触发项目浏览次数同步...");
        syncViewCountsToDatabase();
    }

    /**
     * 同步指定项目的浏览次数
     */
    @Async
    @Transactional
    public void syncProjectViewCount(Long projectId) {
        try {
            Long redisCount = redisService.getAndClearProjectViewCount(projectId);
            
            if (redisCount > 0) {
                int updatedRows = projectRepository.incrementViewCountByAmount(projectId, redisCount.intValue());
                
                if (updatedRows > 0) {
                    log.debug("手动同步项目浏览次数成功: projectId={}, count={}", projectId, redisCount);
                } else {
                    log.warn("手动同步项目浏览次数失败，项目可能不存在: projectId={}", projectId);
                }
            }
        } catch (Exception e) {
            log.error("手动同步项目浏览次数失败: projectId={}", projectId, e);
        }
    }
}
