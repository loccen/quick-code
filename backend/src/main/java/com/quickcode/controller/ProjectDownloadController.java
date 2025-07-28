package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.dto.ProjectDownloadHistoryResponse;
import com.quickcode.dto.ProjectDownloadStatisticsResponse;
import com.quickcode.entity.ProjectDownload;
import com.quickcode.service.DownloadTokenService;
import com.quickcode.service.ProjectDownloadService;
import com.quickcode.service.ProjectDownloadService.DownloadResult;
import com.quickcode.service.ProjectDownloadService.DownloadStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 项目下载控制器
 * 处理项目文件下载相关的HTTP请求
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/download")
@RequiredArgsConstructor
public class ProjectDownloadController extends BaseController {

    private final ProjectDownloadService projectDownloadService;
    private final DownloadTokenService downloadTokenService;

    /**
     * 下载项目主文件
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadProject(
            @PathVariable Long projectId,
            @RequestParam(value = "source", defaultValue = "WEB") String downloadSource,
            HttpServletRequest request) throws IOException {
        
        Long userId = getCurrentUserId();
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        
        log.info("下载项目请求: projectId={}, userId={}, source={}, ip={}", 
                projectId, userId, downloadSource, clientIp);

        try {
            DownloadResult result = projectDownloadService.downloadProject(
                    projectId, userId, downloadSource, userAgent, clientIp);

            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            Resource resource = result.getResource();
            String filename = resource.getFilename();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            log.info("项目下载成功: projectId={}, userId={}, filename={}", 
                    projectId, userId, filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            log.error("项目下载失败: projectId={}, userId={}, error={}", 
                    projectId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * 下载指定项目文件
     */
    @GetMapping("/project/{projectId}/file/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadProjectFile(
            @PathVariable Long projectId,
            @PathVariable Long fileId,
            @RequestParam(value = "source", defaultValue = "WEB") String downloadSource,
            HttpServletRequest request) throws IOException {
        
        Long userId = getCurrentUserId();
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        
        log.info("下载项目文件请求: projectId={}, fileId={}, userId={}, source={}, ip={}", 
                projectId, fileId, userId, downloadSource, clientIp);

        try {
            DownloadResult result = projectDownloadService.downloadProjectFile(
                    projectId, fileId, userId, downloadSource, userAgent, clientIp);

            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            Resource resource = result.getResource();
            String filename = resource.getFilename();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            log.info("项目文件下载成功: projectId={}, fileId={}, userId={}, filename={}", 
                    projectId, fileId, userId, filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            log.error("项目文件下载失败: projectId={}, fileId={}, userId={}, error={}", 
                    projectId, fileId, userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * 检查下载权限
     */
    @GetMapping("/project/{projectId}/permission")
    public ApiResponse<ProjectDownloadService.DownloadPermissionInfo> checkDownloadPermission(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId();
        log.info("检查下载权限: projectId={}, userId={}", projectId, userId);

        try {
            ProjectDownloadService.DownloadPermissionInfo permissionInfo =
                    projectDownloadService.getDownloadPermissionInfo(projectId, userId);

            return success(permissionInfo);

        } catch (Exception e) {
            log.error("检查下载权限失败: projectId={}, userId={}", projectId, userId, e);
            return error("检查下载权限失败");
        }
    }

    /**
     * 生成下载令牌
     */
    @PostMapping("/project/{projectId}/token")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> generateDownloadToken(
            @PathVariable Long projectId,
            @RequestParam(value = "expirationMinutes", defaultValue = "60") int expirationMinutes) {
        
        Long userId = getCurrentUserId();
        log.info("生成下载令牌: projectId={}, userId={}, expiration={}分钟", 
                projectId, userId, expirationMinutes);

        try {
            // 检查下载权限
            if (!projectDownloadService.hasDownloadPermission(projectId, userId)) {
                return error("没有下载权限");
            }

            String token = projectDownloadService.generateDownloadToken(projectId, userId, expirationMinutes);
            
            Map<String, Object> result = Map.of(
                    "token", token,
                    "projectId", projectId,
                    "expirationMinutes", expirationMinutes,
                    "expirationTime", LocalDateTime.now().plusMinutes(expirationMinutes)
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            return success(result, "下载令牌生成成功");

        } catch (Exception e) {
            log.error("生成下载令牌失败: projectId={}, userId={}", projectId, userId, e);
            return error("生成下载令牌失败");
        }
    }

    /**
     * 使用令牌下载项目
     */
    @GetMapping("/project/{projectId}/token/{token}")
    public ResponseEntity<Resource> downloadWithToken(
            @PathVariable Long projectId,
            @PathVariable String token,
            HttpServletRequest request) throws IOException {
        
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        
        log.info("令牌下载项目请求: projectId={}, token={}, ip={}", 
                projectId, token, clientIp);

        try {
            // 验证令牌
            if (!projectDownloadService.validateDownloadToken(token, projectId, null)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(null);
            }

            // 执行下载（使用系统用户ID或从令牌中解析用户ID）
            DownloadResult result = projectDownloadService.downloadProject(
                    projectId, null, "TOKEN", userAgent, clientIp);

            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            Resource resource = result.getResource();
            String filename = resource.getFilename();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + filename + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            log.info("令牌下载成功: projectId={}, token={}, filename={}", 
                    projectId, token, filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            log.error("令牌下载失败: projectId={}, token={}, error={}", 
                    projectId, token, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * 获取用户下载历史
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<ProjectDownloadHistoryResponse>> getDownloadHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "downloadTime") String sort,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction) {
        
        Long userId = getCurrentUserId();
        log.info("获取下载历史: userId={}, page={}, size={}", userId, page, size);

        try {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<ProjectDownload> downloads = projectDownloadService.getUserDownloadHistory(userId, pageable);
            Page<ProjectDownloadHistoryResponse> response = downloads.map(ProjectDownloadHistoryResponse::fromProjectDownload);

            return success(response, "获取下载历史成功");

        } catch (Exception e) {
            log.error("获取下载历史失败: userId={}", userId, e);
            return error("获取下载历史失败");
        }
    }

    /**
     * 获取项目下载统计
     */
    @GetMapping("/project/{projectId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or @projectService.isProjectOwner(#projectId, authentication.name)")
    public ApiResponse<ProjectDownloadStatisticsResponse> getProjectDownloadStatistics(
            @PathVariable Long projectId,
            @RequestParam(value = "days", defaultValue = "30") int days) {
        
        log.info("获取项目下载统计: projectId={}, days={}", projectId, days);

        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            LocalDateTime endTime = LocalDateTime.now();
            
            DownloadStatistics statistics = projectDownloadService.getDownloadStatistics(
                    projectId, startTime, endTime);
            
            ProjectDownloadStatisticsResponse response = ProjectDownloadStatisticsResponse.fromDownloadStatistics(statistics);

            return success(response, "获取下载统计成功");

        } catch (Exception e) {
            log.error("获取项目下载统计失败: projectId={}", projectId, e);
            return error("获取下载统计失败");
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    // ==================== 令牌管理接口 ====================

    /**
     * 刷新下载令牌
     */
    @PostMapping("/token/{token}/refresh")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> refreshDownloadToken(
            @PathVariable String token,
            @RequestParam(value = "expirationMinutes", defaultValue = "60") int expirationMinutes) {

        Long userId = getCurrentUserId();
        log.info("刷新下载令牌: userId={}, expiration={}分钟", userId, expirationMinutes);

        try {
            DownloadTokenService.DownloadTokenInfo tokenInfo = downloadTokenService.refreshDownloadToken(token, expirationMinutes);

            Map<String, Object> result = Map.of(
                    "token", tokenInfo.getToken(),
                    "projectId", tokenInfo.getProjectId(),
                    "expirationMinutes", expirationMinutes,
                    "expirationTime", tokenInfo.getExpirationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            return success(result, "下载令牌刷新成功");

        } catch (Exception e) {
            log.error("刷新下载令牌失败: userId={}", userId, e);
            return error("刷新下载令牌失败: " + e.getMessage());
        }
    }

    /**
     * 撤销下载令牌
     */
    @DeleteMapping("/token/{token}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> revokeDownloadToken(@PathVariable String token) {

        Long userId = getCurrentUserId();
        log.info("撤销下载令牌: userId={}", userId);

        try {
            boolean success = downloadTokenService.revokeDownloadToken(token);

            if (success) {
                return success();
            } else {
                return error("撤销下载令牌失败");
            }

        } catch (Exception e) {
            log.error("撤销下载令牌失败: userId={}", userId, e);
            return error("撤销下载令牌失败");
        }
    }

    /**
     * 获取用户的活跃下载令牌
     */
    @GetMapping("/tokens/active")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<java.util.List<Map<String, Object>>> getUserActiveTokens() {

        Long userId = getCurrentUserId();
        log.info("获取用户活跃下载令牌: userId={}", userId);

        try {
            java.util.List<DownloadTokenService.DownloadTokenInfo> tokens = downloadTokenService.getUserActiveTokens(userId);

            java.util.List<Map<String, Object>> result = tokens.stream()
                    .map(tokenInfo -> Map.of(
                            "projectId", tokenInfo.getProjectId(),
                            "expirationTime", tokenInfo.getExpirationTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            "metadata", tokenInfo.getMetadata() != null ? tokenInfo.getMetadata() : Map.of(),
                            "remainingTime", downloadTokenService.getTokenRemainingTime(tokenInfo.getToken())
                    ))
                    .collect(java.util.stream.Collectors.toList());

            return success(result);

        } catch (Exception e) {
            log.error("获取用户活跃下载令牌失败: userId={}", userId, e);
            return error("获取活跃令牌失败");
        }
    }

    /**
     * 获取令牌统计信息
     */
    @GetMapping("/tokens/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> getTokenStatistics() {

        log.info("获取令牌统计信息");

        try {
            Map<String, Object> statistics = downloadTokenService.getTokenStatistics();
            return success(statistics);

        } catch (Exception e) {
            log.error("获取令牌统计信息失败", e);
            return error("获取令牌统计信息失败");
        }
    }

    /**
     * 清理过期令牌
     */
    @PostMapping("/tokens/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> cleanupExpiredTokens() {

        log.info("清理过期令牌");

        try {
            int cleanedCount = downloadTokenService.cleanupExpiredTokens();

            Map<String, Object> result = Map.of(
                    "cleanedCount", cleanedCount,
                    "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            return success(result, "过期令牌清理完成");

        } catch (Exception e) {
            log.error("清理过期令牌失败", e);
            return error("清理过期令牌失败");
        }
    }
}
