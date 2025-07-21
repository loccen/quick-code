package com.quickcode.controller;

import com.quickcode.common.response.ApiResponse;
import com.quickcode.dto.ProjectFileBatchUploadResponse;
import com.quickcode.dto.ProjectFileUploadRequest;
import com.quickcode.dto.ProjectFileUploadResponse;
import com.quickcode.service.ProjectFileService;
import com.quickcode.service.ProjectFileService.FileUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件上传控制器
 * 处理文件上传相关的HTTP请求
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Validated
public class FileUploadController extends BaseController {

    private final ProjectFileService projectFileService;

    @Value("${app.file.upload-path:/uploads}")
    private String uploadPath;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("上传头像请求: userId={}, fileName={}, fileSize={}", 
                getCurrentUserId(), file.getOriginalFilename(), file.getSize());

        try {
            // 验证文件
            validateAvatarFile(file);

            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            
            // 确保上传目录存在
            Path uploadDir;
            if (uploadPath.startsWith("/")) {
                // 绝对路径
                uploadDir = Paths.get(uploadPath, "avatars");
            } else {
                // 相对路径，基于当前工作目录
                uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath, "avatars");
            }
            Files.createDirectories(uploadDir);

            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 生成访问URL
            String fileUrl = baseUrl + "/uploads/avatars/" + fileName;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", fileName);

            log.info("头像上传成功: userId={}, fileUrl={}", getCurrentUserId(), fileUrl);
            return success(result, "头像上传成功");

        } catch (IOException e) {
            log.error("头像上传失败: userId={}, error={}", getCurrentUserId(), e.getMessage(), e);
            return error("文件上传失败");
        } catch (IllegalArgumentException e) {
            log.warn("头像上传验证失败: userId={}, error={}", getCurrentUserId(), e.getMessage());
            return error(e.getMessage());
        }
    }

    /**
     * 上传项目文件
     */
    @PostMapping("/project")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, String>> uploadProjectFile(@RequestParam("file") MultipartFile file) {
        log.info("上传项目文件请求: userId={}, fileName={}, fileSize={}", 
                getCurrentUserId(), file.getOriginalFilename(), file.getSize());

        try {
            // 验证文件
            validateProjectFile(file);

            // 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            
            // 确保上传目录存在
            Path uploadDir;
            if (uploadPath.startsWith("/")) {
                // 绝对路径
                uploadDir = Paths.get(uploadPath, "projects");
            } else {
                // 相对路径，基于当前工作目录
                uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath, "projects");
            }
            Files.createDirectories(uploadDir);

            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 生成访问URL
            String fileUrl = baseUrl + "/uploads/projects/" + fileName;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", fileName);

            log.info("项目文件上传成功: userId={}, fileUrl={}", getCurrentUserId(), fileUrl);
            return success(result, "文件上传成功");

        } catch (IOException e) {
            log.error("项目文件上传失败: userId={}, error={}", getCurrentUserId(), e.getMessage(), e);
            return error("文件上传失败");
        } catch (IllegalArgumentException e) {
            log.warn("项目文件上传验证失败: userId={}, error={}", getCurrentUserId(), e.getMessage());
            return error(e.getMessage());
        }
    }

    /**
     * 上传项目文件（增强版）
     * 支持指定项目ID、文件类型、描述等参数
     */
    @PostMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProjectFileUploadResponse> uploadProjectFileEnhanced(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", defaultValue = "SOURCE") String fileType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary) {

        Long userId = getCurrentUserId();
        log.info("上传项目文件（增强版）: projectId={}, userId={}, fileType={}, fileName={}, fileSize={}",
                projectId, userId, fileType, file.getOriginalFilename(), file.getSize());

        try {
            FileUploadResult result = projectFileService.uploadProjectFile(
                    projectId, file, fileType, description, userId);

            if (!result.isSuccess()) {
                return error(result.getMessage());
            }

            // 如果需要设为主文件
            if (isPrimary && result.getProjectFile() != null) {
                projectFileService.setPrimaryFile(result.getProjectFile().getId(), userId);
            }

            ProjectFileUploadResponse response = ProjectFileUploadResponse.fromProjectFile(result.getProjectFile());
            return success(response, "文件上传成功");

        } catch (IOException e) {
            log.error("项目文件上传失败: projectId={}, userId={}, error={}", projectId, userId, e.getMessage(), e);
            return error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("项目文件上传异常: projectId={}, userId={}", projectId, userId, e);
            return error("文件上传失败");
        }
    }

    /**
     * 批量上传项目文件
     */
    @PostMapping("/project/{projectId}/batch")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProjectFileBatchUploadResponse> uploadProjectFilesBatch(
            @PathVariable Long projectId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "fileType", defaultValue = "SOURCE") String fileType) {

        Long userId = getCurrentUserId();
        log.info("批量上传项目文件: projectId={}, userId={}, fileType={}, fileCount={}",
                projectId, userId, fileType, files.size());

        try {
            List<FileUploadResult> results = projectFileService.uploadProjectFiles(
                    projectId, files, fileType, userId);

            List<ProjectFileUploadResponse> successFiles = new ArrayList<>();
            List<ProjectFileBatchUploadResponse.FailedFileInfo> failedFiles = new ArrayList<>();

            for (int i = 0; i < results.size(); i++) {
                FileUploadResult result = results.get(i);
                MultipartFile file = files.get(i);

                if (result.isSuccess()) {
                    successFiles.add(ProjectFileUploadResponse.fromProjectFile(result.getProjectFile()));
                } else {
                    failedFiles.add(ProjectFileBatchUploadResponse.FailedFileInfo.builder()
                            .originalName(file.getOriginalFilename())
                            .reason(result.getMessage())
                            .errorCode("UPLOAD_FAILED")
                            .build());
                }
            }

            ProjectFileBatchUploadResponse response = ProjectFileBatchUploadResponse.builder()
                    .totalFiles(files.size())
                    .successCount(successFiles.size())
                    .failureCount(failedFiles.size())
                    .successFiles(successFiles)
                    .failedFiles(failedFiles)
                    .build();

            return success(response, String.format("批量上传完成：成功 %d 个，失败 %d 个",
                    successFiles.size(), failedFiles.size()));

        } catch (Exception e) {
            log.error("批量上传项目文件异常: projectId={}, userId={}", projectId, userId, e);
            return error("批量上传失败");
        }
    }

    /**
     * 替换项目文件
     */
    @PutMapping("/project/file/{fileId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProjectFileUploadResponse> replaceProjectFile(
            @PathVariable Long fileId,
            @RequestParam("file") MultipartFile file) {

        Long userId = getCurrentUserId();
        log.info("替换项目文件: fileId={}, userId={}, fileName={}, fileSize={}",
                fileId, userId, file.getOriginalFilename(), file.getSize());

        try {
            FileUploadResult result = projectFileService.replaceProjectFile(fileId, file, userId);

            if (!result.isSuccess()) {
                return error(result.getMessage());
            }

            ProjectFileUploadResponse response = ProjectFileUploadResponse.fromProjectFile(result.getProjectFile());
            return success(response, "文件替换成功");

        } catch (IOException e) {
            log.error("替换项目文件失败: fileId={}, userId={}, error={}", fileId, userId, e.getMessage(), e);
            return error("文件替换失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("替换项目文件异常: fileId={}, userId={}", fileId, userId, e);
            return error("文件替换失败");
        }
    }

    /**
     * 验证头像文件
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小 (2MB)
        long maxSize = 2L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过2MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件");
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
            throw new IllegalArgumentException("只支持 JPG、PNG、GIF、WebP 格式的图片");
        }
    }

    /**
     * 验证项目文件
     */
    private void validateProjectFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小 (100MB)
        long maxSize = 100L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!extension.matches("\\.(zip|rar|tar\\.gz|7z)$")) {
            throw new IllegalArgumentException("只支持 ZIP、RAR、TAR.GZ、7Z 格式的压缩文件");
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
