package com.quickcode.service.impl;

import com.quickcode.entity.Project;
import com.quickcode.entity.ProjectDownload;
import com.quickcode.entity.ProjectFile;
import com.quickcode.repository.ProjectDownloadRepository;
import com.quickcode.repository.ProjectFileRepository;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.service.DownloadTokenService;
import com.quickcode.service.FileStorageService;
import com.quickcode.service.OrderService;
import com.quickcode.service.ProjectDownloadService;
import com.quickcode.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目下载服务实现类
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    private final ProjectDownloadRepository projectDownloadRepository;
    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final FileStorageService fileStorageService;
    private final OrderService orderService;
    private final DownloadTokenService downloadTokenService;
    private final ProjectFileService projectFileService;

    // 下载频率限制配置
    private static final int MAX_DOWNLOADS_PER_HOUR = 10;
    private static final int MAX_DOWNLOADS_PER_DAY = 50;

    @Override
    public DownloadResult downloadProject(Long projectId, Long userId, String downloadSource, 
                                        String userAgent, String clientIp) throws IOException {
        
        log.info("开始下载项目: projectId={}, userId={}, source={}", projectId, userId, downloadSource);

        // 检查项目是否存在
        if (!projectRepository.existsById(projectId)) {
            return new DownloadResult(false, "项目不存在", null, null);
        }

        // 检查下载权限
        if (!hasDownloadPermission(projectId, userId)) {
            return new DownloadResult(false, "没有下载权限", null, null);
        }

        // 检查下载频率限制
        if (!checkDownloadRateLimit(userId, clientIp)) {
            return new DownloadResult(false, "下载频率超限，请稍后再试", null, null);
        }

        try {
            // 查找项目的主源码文件
            Optional<ProjectFile> primaryFileOpt = projectFileRepository
                    .findByProjectIdAndFileTypeAndIsPrimaryTrue(projectId, "SOURCE");
            
            if (primaryFileOpt.isEmpty()) {
                return new DownloadResult(false, "项目源码文件不存在", null, null);
            }

            ProjectFile primaryFile = primaryFileOpt.get();

            // 记录下载开始
            ProjectDownload downloadRecord = recordDownloadStart(projectId, userId, primaryFile.getId(), 
                                                               downloadSource, userAgent, clientIp);

            long startTime = System.currentTimeMillis();

            try {
                // 加载文件资源
                Resource resource = fileStorageService.loadAsResource(primaryFile.getFilePath());

                // 记录下载完成
                long duration = System.currentTimeMillis() - startTime;
                recordDownloadComplete(downloadRecord.getId(), primaryFile.getFileSize(), duration);

                // 更新项目下载次数
                updateProjectDownloadCount(projectId);

                log.info("项目下载成功: projectId={}, userId={}, fileSize={}, duration={}ms", 
                        projectId, userId, primaryFile.getFileSize(), duration);

                return new DownloadResult(true, "下载成功", resource, downloadRecord);

            } catch (IOException e) {
                // 记录下载失败
                recordDownloadFailed(downloadRecord.getId(), e.getMessage());
                throw e;
            }

        } catch (Exception e) {
            log.error("项目下载失败: projectId={}, userId={}", projectId, userId, e);
            return new DownloadResult(false, "下载失败: " + e.getMessage(), null, null);
        }
    }

    @Override
    public DownloadResult downloadProjectFile(Long projectId, Long fileId, Long userId, 
                                            String downloadSource, String userAgent, String clientIp) throws IOException {
        
        log.info("开始下载项目文件: projectId={}, fileId={}, userId={}", projectId, fileId, userId);

        // 检查项目是否存在
        if (!projectRepository.existsById(projectId)) {
            return new DownloadResult(false, "项目不存在", null, null);
        }

        // 检查文件是否存在
        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return new DownloadResult(false, "文件不存在", null, null);
        }

        ProjectFile projectFile = fileOpt.get();

        // 验证文件属于指定项目
        if (!projectFile.getProjectId().equals(projectId)) {
            return new DownloadResult(false, "文件不属于指定项目", null, null);
        }

        // 检查下载权限
        if (!hasDownloadPermission(projectId, userId)) {
            return new DownloadResult(false, "没有下载权限", null, null);
        }

        // 检查文件访问权限
        if (!projectFileService.hasFileAccess(fileId, userId)) {
            return new DownloadResult(false, "没有文件访问权限", null, null);
        }

        try {
            // 记录下载开始
            ProjectDownload downloadRecord = recordDownloadStart(projectId, userId, fileId, 
                                                               downloadSource, userAgent, clientIp);

            long startTime = System.currentTimeMillis();

            try {
                // 加载文件资源
                Resource resource = fileStorageService.loadAsResource(projectFile.getFilePath());

                // 记录下载完成
                long duration = System.currentTimeMillis() - startTime;
                recordDownloadComplete(downloadRecord.getId(), projectFile.getFileSize(), duration);

                log.info("项目文件下载成功: fileId={}, userId={}, fileSize={}, duration={}ms", 
                        fileId, userId, projectFile.getFileSize(), duration);

                return new DownloadResult(true, "下载成功", resource, downloadRecord);

            } catch (IOException e) {
                // 记录下载失败
                recordDownloadFailed(downloadRecord.getId(), e.getMessage());
                throw e;
            }

        } catch (Exception e) {
            log.error("项目文件下载失败: fileId={}, userId={}", fileId, userId, e);
            return new DownloadResult(false, "下载失败: " + e.getMessage(), null, null);
        }
    }

    @Override
    public boolean hasDownloadPermission(Long projectId, Long userId) {
        log.debug("检查下载权限: projectId={}, userId={}", projectId, userId);

        try {
            // 1. 检查项目是否存在
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                log.warn("项目不存在: projectId={}", projectId);
                return false;
            }

            Project project = projectOpt.get();

            // 2. 检查项目是否已发布
            if (!project.isPublished()) {
                log.warn("项目未发布: projectId={}", projectId);
                return false;
            }

            // 3. 如果用户未登录，只能下载免费项目
            if (userId == null) {
                boolean isFree = project.getPrice() == null ||
                               project.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0;
                log.debug("匿名用户下载权限检查: projectId={}, isFree={}", projectId, isFree);
                return isFree;
            }

            // 4. 检查用户是否是项目所有者
            if (project.getUserId().equals(userId)) {
                log.debug("项目所有者下载: projectId={}, userId={}", projectId, userId);
                return true;
            }

            // 5. 检查项目是否免费
            boolean isFree = project.getPrice() == null ||
                           project.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0;
            if (isFree) {
                log.debug("免费项目下载: projectId={}, userId={}", projectId, userId);
                return true;
            }

            // 6. 检查用户是否已购买项目
            boolean hasPurchased = orderService.hasUserPurchasedProject(projectId, userId);
            log.debug("付费项目下载权限检查: projectId={}, userId={}, hasPurchased={}",
                     projectId, userId, hasPurchased);

            return hasPurchased;

        } catch (Exception e) {
            log.error("检查下载权限时发生异常: projectId={}, userId={}", projectId, userId, e);
            return false;
        }
    }

    @Override
    public DownloadPermissionInfo getDownloadPermissionInfo(Long projectId, Long userId) {
        log.debug("获取下载权限详细信息: projectId={}, userId={}", projectId, userId);

        try {
            // 1. 检查项目是否存在
            Optional<Project> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return new DownloadPermissionInfo(false, "项目不存在", false, false, false, false, null);
            }

            Project project = projectOpt.get();
            boolean isProjectPublished = project.isPublished();
            boolean isFreeProject = project.getPrice() == null ||
                                  project.getPrice().compareTo(java.math.BigDecimal.ZERO) == 0;
            boolean isProjectOwner = userId != null && project.getUserId().equals(userId);
            boolean hasPurchased = false;

            // 2. 检查项目是否已发布
            if (!isProjectPublished) {
                return new DownloadPermissionInfo(false, "项目未发布", isProjectOwner, isFreeProject,
                                                hasPurchased, isProjectPublished, project.getPrice());
            }

            // 3. 如果用户未登录，只能下载免费项目
            if (userId == null) {
                if (isFreeProject) {
                    return new DownloadPermissionInfo(true, "免费项目，允许下载", isProjectOwner, isFreeProject,
                                                    hasPurchased, isProjectPublished, project.getPrice());
                } else {
                    return new DownloadPermissionInfo(false, "付费项目，需要登录并购买", isProjectOwner, isFreeProject,
                                                    hasPurchased, isProjectPublished, project.getPrice());
                }
            }

            // 4. 检查用户是否是项目所有者
            if (isProjectOwner) {
                return new DownloadPermissionInfo(true, "项目所有者，允许下载", isProjectOwner, isFreeProject,
                                                hasPurchased, isProjectPublished, project.getPrice());
            }

            // 5. 检查项目是否免费
            if (isFreeProject) {
                return new DownloadPermissionInfo(true, "免费项目，允许下载", isProjectOwner, isFreeProject,
                                                hasPurchased, isProjectPublished, project.getPrice());
            }

            // 6. 检查用户是否已购买项目
            hasPurchased = orderService.hasUserPurchasedProject(projectId, userId);
            if (hasPurchased) {
                return new DownloadPermissionInfo(true, "已购买项目，允许下载", isProjectOwner, isFreeProject,
                                                hasPurchased, isProjectPublished, project.getPrice());
            } else {
                return new DownloadPermissionInfo(false, "付费项目，需要购买后才能下载", isProjectOwner, isFreeProject,
                                                hasPurchased, isProjectPublished, project.getPrice());
            }

        } catch (Exception e) {
            log.error("获取下载权限详细信息时发生异常: projectId={}, userId={}", projectId, userId, e);
            return new DownloadPermissionInfo(false, "系统异常，请稍后重试", false, false, false, false, null);
        }
    }

    @Override
    public boolean hasUserDownloaded(Long projectId, Long userId) {
        return projectDownloadRepository.hasUserDownloadedProject(userId, projectId);
    }

    @Override
    public Page<ProjectDownload> getUserDownloadHistory(Long userId, Pageable pageable) {
        return projectDownloadRepository.findUserRecentDownloads(userId, pageable);
    }

    @Override
    public Page<ProjectDownload> getProjectDownloadHistory(Long projectId, Pageable pageable) {
        return projectDownloadRepository.findProjectRecentDownloads(projectId, pageable);
    }

    @Override
    public DownloadStatistics getDownloadStatistics(Long projectId, LocalDateTime startTime, LocalDateTime endTime) {
        // 设置默认时间范围
        final LocalDateTime finalStartTime = startTime != null ? startTime : LocalDateTime.now().minusDays(30);
        final LocalDateTime finalEndTime = endTime != null ? endTime : LocalDateTime.now();

        List<ProjectDownload> downloads;
        if (projectId != null) {
            downloads = projectDownloadRepository.findByProjectId(projectId).stream()
                    .filter(d -> d.getDownloadTime() != null &&
                               d.getDownloadTime().isAfter(finalStartTime) &&
                               d.getDownloadTime().isBefore(finalEndTime))
                    .filter(d -> ProjectDownload.DownloadStatus.COMPLETED.getCode().equals(d.getDownloadStatus()))
                    .collect(Collectors.toList());
        } else {
            downloads = projectDownloadRepository.findByDownloadTimeBetween(finalStartTime, finalEndTime).stream()
                    .filter(d -> ProjectDownload.DownloadStatus.COMPLETED.getCode().equals(d.getDownloadStatus()))
                    .collect(Collectors.toList());
        }

        long totalDownloads = downloads.size();
        long uniqueDownloaders = downloads.stream()
                .map(ProjectDownload::getUserId)
                .distinct()
                .count();
        
        long totalSize = downloads.stream()
                .mapToLong(d -> d.getFileSize() != null ? d.getFileSize() : 0)
                .sum();
        
        double averageDuration = downloads.stream()
                .filter(d -> d.getDownloadDuration() != null)
                .mapToLong(ProjectDownload::getDownloadDuration)
                .average()
                .orElse(0.0);

        // 按来源统计
        Map<String, Long> downloadsBySource = downloads.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getDownloadSource() != null ? d.getDownloadSource() : "UNKNOWN",
                    Collectors.counting()
                ));

        // 按日期统计
        Map<String, Long> downloadsByDate = downloads.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getDownloadTime().toLocalDate().toString(),
                    Collectors.counting()
                ));

        return new DownloadStatistics(totalDownloads, uniqueDownloaders, totalSize, 
                                    averageDuration, downloadsBySource, downloadsByDate);
    }

    @Override
    public ProjectDownload recordDownloadStart(Long projectId, Long userId, Long fileId, 
                                             String downloadSource, String userAgent, String clientIp) {
        
        // 检查是否为重复下载
        boolean isRepeat = hasUserDownloaded(projectId, userId);

        ProjectDownload downloadRecord = ProjectDownload.builder()
                .projectId(projectId)
                .userId(userId)
                .fileId(fileId)
                .downloadTime(LocalDateTime.now())
                .downloadStatus(ProjectDownload.DownloadStatus.DOWNLOADING.getCode())
                .downloadIp(clientIp)
                .userAgent(userAgent)
                .downloadSource(downloadSource)
                .isRepeat(isRepeat)
                .build();

        downloadRecord = projectDownloadRepository.save(downloadRecord);
        
        log.info("下载记录创建成功: downloadId={}, projectId={}, userId={}", 
                downloadRecord.getId(), projectId, userId);
        
        return downloadRecord;
    }

    @Override
    public boolean recordDownloadComplete(Long downloadId, Long fileSize, Long duration) {
        try {
            Optional<ProjectDownload> downloadOpt = projectDownloadRepository.findById(downloadId);
            if (downloadOpt.isEmpty()) {
                log.warn("下载记录不存在: downloadId={}", downloadId);
                return false;
            }

            ProjectDownload download = downloadOpt.get();
            download.completeDownload(duration);
            download.setFileSize(fileSize);
            
            projectDownloadRepository.save(download);
            
            log.info("下载完成记录更新成功: downloadId={}, fileSize={}, duration={}ms", 
                    downloadId, fileSize, duration);
            return true;
        } catch (Exception e) {
            log.error("记录下载完成失败: downloadId={}", downloadId, e);
            return false;
        }
    }

    @Override
    public boolean recordDownloadFailed(Long downloadId, String reason) {
        try {
            Optional<ProjectDownload> downloadOpt = projectDownloadRepository.findById(downloadId);
            if (downloadOpt.isEmpty()) {
                log.warn("下载记录不存在: downloadId={}", downloadId);
                return false;
            }

            ProjectDownload download = downloadOpt.get();
            download.failDownload();
            download.setRemark(reason);
            
            projectDownloadRepository.save(download);
            
            log.info("下载失败记录更新成功: downloadId={}, reason={}", downloadId, reason);
            return true;
        } catch (Exception e) {
            log.error("记录下载失败失败: downloadId={}", downloadId, e);
            return false;
        }
    }

    @Override
    public boolean recordDownloadCancelled(Long downloadId) {
        try {
            Optional<ProjectDownload> downloadOpt = projectDownloadRepository.findById(downloadId);
            if (downloadOpt.isEmpty()) {
                log.warn("下载记录不存在: downloadId={}", downloadId);
                return false;
            }

            ProjectDownload download = downloadOpt.get();
            download.cancelDownload();
            
            projectDownloadRepository.save(download);
            
            log.info("下载取消记录更新成功: downloadId={}", downloadId);
            return true;
        } catch (Exception e) {
            log.error("记录下载取消失败: downloadId={}", downloadId, e);
            return false;
        }
    }

    @Override
    public boolean updateProjectDownloadCount(Long projectId) {
        try {
            // 这里应该调用ProjectRepository的方法来增加下载次数
            // 暂时简化处理
            log.info("更新项目下载次数: projectId={}", projectId);
            return true;
        } catch (Exception e) {
            log.error("更新项目下载次数失败: projectId={}", projectId, e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getPopularDownloads(int limit, int days) {
        log.info("获取热门下载项目: limit={}, days={}", limit, days);
        // 这里应该使用Repository的聚合查询方法
        // 暂时返回空列表
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getTopDownloaders(int limit, int days) {
        log.info("获取下载排行用户: limit={}, days={}", limit, days);
        // 这里应该使用Repository的聚合查询方法
        // 暂时返回空列表
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getDownloadTrends(Long projectId, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        LocalDateTime endTime = LocalDateTime.now();

        List<Object[]> trendData;
        if (projectId != null) {
            // 获取特定项目的下载趋势
            trendData = projectDownloadRepository.countDownloadsByDate(startTime, endTime);
        } else {
            // 获取全站下载趋势
            trendData = projectDownloadRepository.countDownloadsByDate(startTime, endTime);
        }

        return trendData.stream()
                .map(data -> {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("date", data[0]);
                    trend.put("count", data[1]);
                    return trend;
                })
                .collect(Collectors.toList());
    }

    @Override
    public int cleanupExpiredDownloads(int olderThanDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(olderThanDays);

        try {
            projectDownloadRepository.cleanupExpiredDownloads(cutoffTime);
            log.info("清理过期下载记录完成: 清理 {} 天前的记录", olderThanDays);
            return 0; // 实际应该返回清理的记录数
        } catch (Exception e) {
            log.error("清理过期下载记录失败", e);
            return 0;
        }
    }

    @Override
    public boolean detectAbnormalDownloadBehavior(Long userId, String clientIp, int timeWindowMinutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(timeWindowMinutes);
        LocalDateTime endTime = LocalDateTime.now();

        // 检查时间窗口内的下载次数
        List<ProjectDownload> recentDownloads = projectDownloadRepository
                .findByDownloadTimeBetween(startTime, endTime).stream()
                .filter(d -> (userId != null && userId.equals(d.getUserId())) ||
                           (clientIp != null && clientIp.equals(d.getDownloadIp())))
                .collect(Collectors.toList());

        // 简单的异常检测逻辑
        int maxDownloadsInWindow = 20; // 时间窗口内最大下载次数
        return recentDownloads.size() > maxDownloadsInWindow;
    }

    @Override
    public boolean checkDownloadRateLimit(Long userId, String clientIp) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        // 检查1小时内的下载次数
        List<ProjectDownload> hourlyDownloads = projectDownloadRepository
                .findByDownloadTimeBetween(oneHourAgo, LocalDateTime.now()).stream()
                .filter(d -> (userId != null && userId.equals(d.getUserId())) ||
                           (clientIp != null && clientIp.equals(d.getDownloadIp())))
                .collect(Collectors.toList());

        if (hourlyDownloads.size() >= MAX_DOWNLOADS_PER_HOUR) {
            log.warn("用户下载频率超限(小时): userId={}, ip={}, count={}", userId, clientIp, hourlyDownloads.size());
            return false;
        }

        // 检查24小时内的下载次数
        List<ProjectDownload> dailyDownloads = projectDownloadRepository
                .findByDownloadTimeBetween(oneDayAgo, LocalDateTime.now()).stream()
                .filter(d -> (userId != null && userId.equals(d.getUserId())) ||
                           (clientIp != null && clientIp.equals(d.getDownloadIp())))
                .collect(Collectors.toList());

        if (dailyDownloads.size() >= MAX_DOWNLOADS_PER_DAY) {
            log.warn("用户下载频率超限(日): userId={}, ip={}, count={}", userId, clientIp, dailyDownloads.size());
            return false;
        }

        return true;
    }

    @Override
    public String generateDownloadToken(Long projectId, Long userId, int expirationMinutes) {
        log.info("生成下载令牌: projectId={}, userId={}, expiration={}分钟", projectId, userId, expirationMinutes);

        try {
            // 使用DownloadTokenService生成JWT令牌
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "download_api");
            metadata.put("generatedAt", LocalDateTime.now().toString());

            DownloadTokenService.DownloadTokenInfo tokenInfo = downloadTokenService.generateDownloadToken(
                    projectId, userId, expirationMinutes, metadata);

            log.info("下载令牌生成成功: projectId={}, userId={}, tokenLength={}",
                    projectId, userId, tokenInfo.getToken().length());

            return tokenInfo.getToken();

        } catch (Exception e) {
            log.error("生成下载令牌失败: projectId={}, userId={}", projectId, userId, e);
            throw new RuntimeException("生成下载令牌失败", e);
        }
    }

    @Override
    public boolean validateDownloadToken(String token, Long projectId, Long userId) {
        log.debug("验证下载令牌: projectId={}, userId={}", projectId, userId);

        try {
            // 使用DownloadTokenService验证JWT令牌
            DownloadTokenService.TokenValidationResult result = downloadTokenService.validateDownloadToken(token, projectId);

            if (!result.isValid()) {
                log.warn("下载令牌验证失败: projectId={}, userId={}, reason={}",
                        projectId, userId, result.getReason());
                return false;
            }

            // 检查用户ID匹配（如果提供）
            if (userId != null && !userId.equals(result.getUserId())) {
                log.warn("下载令牌用户ID不匹配: expected={}, actual={}", userId, result.getUserId());
                return false;
            }

            log.debug("下载令牌验证成功: projectId={}, userId={}", projectId, userId);
            return true;

        } catch (Exception e) {
            log.error("验证下载令牌时发生异常: projectId={}, userId={}", projectId, userId, e);
            return false;
        }
    }

    @Override
    public int getDownloadProgress(Long downloadId) {
        // 获取下载进度
        // 实际项目中应该从缓存或数据库中获取实时进度
        return 0;
    }

    @Override
    public boolean updateDownloadProgress(Long downloadId, int progress) {
        // 更新下载进度
        // 实际项目中应该更新到缓存或数据库中
        log.debug("更新下载进度: downloadId={}, progress={}%", downloadId, progress);
        return true;
    }

    @Override
    public boolean pauseDownload(Long downloadId) {
        // 暂停下载
        log.info("暂停下载: downloadId={}", downloadId);
        return true;
    }

    @Override
    public boolean resumeDownload(Long downloadId) {
        // 恢复下载
        log.info("恢复下载: downloadId={}", downloadId);
        return true;
    }

    @Override
    public long getResumePosition(Long downloadId) {
        // 获取断点续传位置
        return 0;
    }

    @Override
    public DownloadResult downloadWithRange(Long projectId, Long userId, long rangeStart, long rangeEnd) throws IOException {
        // 支持断点续传的下载
        log.info("断点续传下载: projectId={}, userId={}, range={}-{}", projectId, userId, rangeStart, rangeEnd);

        // 这里应该实现Range请求的处理逻辑
        // 暂时调用普通下载方法
        return downloadProject(projectId, userId, "WEB", null, null);
    }

    // 基础CRUD方法实现
    @Override
    public ProjectDownload save(ProjectDownload entity) {
        return projectDownloadRepository.save(entity);
    }

    @Override
    public List<ProjectDownload> saveAll(List<ProjectDownload> entities) {
        return projectDownloadRepository.saveAll(entities);
    }

    @Override
    public Optional<ProjectDownload> findById(Long id) {
        return projectDownloadRepository.findById(id);
    }

    @Override
    public ProjectDownload getById(Long id) {
        return projectDownloadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("下载记录不存在: " + id));
    }

    @Override
    public List<ProjectDownload> findAll() {
        return projectDownloadRepository.findAll();
    }

    @Override
    public Page<ProjectDownload> findAll(Pageable pageable) {
        return projectDownloadRepository.findAll(pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return projectDownloadRepository.existsById(id);
    }

    @Override
    public long count() {
        return projectDownloadRepository.count();
    }

    @Override
    public void deleteById(Long id) {
        projectDownloadRepository.deleteById(id);
    }

    @Override
    public void delete(ProjectDownload entity) {
        projectDownloadRepository.delete(entity);
    }

    @Override
    public void deleteAll(List<ProjectDownload> entities) {
        projectDownloadRepository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        projectDownloadRepository.deleteAll();
    }
}
