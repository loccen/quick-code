package com.quickcode.service.impl;

import com.quickcode.entity.ProjectFile;
import com.quickcode.repository.ProjectFileRepository;
import com.quickcode.repository.ProjectRepository;
import com.quickcode.service.FileExtractionService;
import com.quickcode.service.FileSecurityService;
import com.quickcode.service.FileStorageService;
import com.quickcode.service.ProjectFileService;
import com.quickcode.service.FileStorageService.StorageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目文件服务实现类
 *
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectFileServiceImpl implements ProjectFileService {

    private final ProjectFileRepository projectFileRepository;
    private final ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;
    private final FileSecurityService fileSecurityService;
    private final FileExtractionService fileExtractionService;



    @Override
    public FileUploadResult uploadProjectFile(Long projectId, MultipartFile file, String fileType, 
                                            String description, Long userId) throws IOException {
        
        log.info("开始上传项目文件: projectId={}, fileType={}, fileName={}, userId={}", 
                projectId, fileType, file.getOriginalFilename(), userId);

        try {
            // 验证项目是否存在
            if (!projectRepository.existsById(projectId)) {
                return new FileUploadResult(null, null, false, "项目不存在");
            }

            // 验证文件类型
            if (!fileSecurityService.validateFileType(file, fileType)) {
                return new FileUploadResult(null, null, false, "不支持的文件类型");
            }

            // 执行安全检查
            FileSecurityService.SecurityCheckResult securityResult = fileSecurityService.performSecurityCheck(file, fileType);
            if (!securityResult.isSafe()) {
                return new FileUploadResult(null, null, false,
                    "文件安全检查失败: " + String.join(", ", securityResult.getIssues()));
            }

            // 存储文件
            String category = "projects/" + projectId;
            StorageResult storageResult = fileStorageService.store(file, category);

            // 创建文件记录
            ProjectFile projectFile = ProjectFile.builder()
                    .projectId(projectId)
                    .fileName(storageResult.getFileName())
                    .originalName(file.getOriginalFilename())
                    .filePath(storageResult.getFilePath())
                    .fileUrl(storageResult.getFileUrl())
                    .fileType(fileType)
                    .mimeType(storageResult.getMimeType())
                    .fileSize(storageResult.getFileSize())
                    .fileHash(storageResult.getFileHash())
                    .fileStatus(ProjectFile.FileStatus.UPLOADED.getCode())
                    .uploadTime(LocalDateTime.now())
                    .isPrimary(false)
                    .description(description)
                    .build();

            // 保存到数据库
            projectFile = projectFileRepository.save(projectFile);

            log.info("项目文件上传成功: fileId={}, fileName={}", projectFile.getId(), projectFile.getFileName());
            return new FileUploadResult(projectFile, storageResult, true, "文件上传成功");

        } catch (Exception e) {
            log.error("项目文件上传失败: projectId={}, fileName={}", projectId, file.getOriginalFilename(), e);
            return new FileUploadResult(null, null, false, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<FileUploadResult> uploadProjectFiles(Long projectId, List<MultipartFile> files, 
                                                   String fileType, Long userId) {
        List<FileUploadResult> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                FileUploadResult result = uploadProjectFile(projectId, file, fileType, null, userId);
                results.add(result);
            } catch (IOException e) {
                log.error("批量上传文件失败: fileName={}", file.getOriginalFilename(), e);
                results.add(new FileUploadResult(null, null, false, "上传失败: " + e.getMessage()));
            }
        }
        
        return results;
    }

    @Override
    public FileUploadResult replaceProjectFile(Long fileId, MultipartFile newFile, Long userId) throws IOException {
        log.info("开始替换项目文件: fileId={}, newFileName={}, userId={}", 
                fileId, newFile.getOriginalFilename(), userId);

        Optional<ProjectFile> existingFileOpt = projectFileRepository.findById(fileId);
        if (existingFileOpt.isEmpty()) {
            return new FileUploadResult(null, null, false, "原文件不存在");
        }

        ProjectFile existingFile = existingFileOpt.get();
        
        // 检查权限
        if (!hasFileEditPermission(fileId, userId)) {
            return new FileUploadResult(null, null, false, "没有编辑权限");
        }

        try {
            // 删除原文件
            fileStorageService.delete(existingFile.getFilePath());

            // 上传新文件
            String category = "projects/" + existingFile.getProjectId();
            StorageResult storageResult = fileStorageService.store(newFile, category);

            // 更新文件记录
            existingFile.setFileName(storageResult.getFileName());
            existingFile.setOriginalName(newFile.getOriginalFilename());
            existingFile.setFilePath(storageResult.getFilePath());
            existingFile.setFileUrl(storageResult.getFileUrl());
            existingFile.setMimeType(storageResult.getMimeType());
            existingFile.setFileSize(storageResult.getFileSize());
            existingFile.setFileHash(storageResult.getFileHash());
            existingFile.setFileStatus(ProjectFile.FileStatus.UPLOADED.getCode());
            existingFile.setUploadTime(LocalDateTime.now());

            existingFile = projectFileRepository.save(existingFile);

            log.info("项目文件替换成功: fileId={}, newFileName={}", fileId, storageResult.getFileName());
            return new FileUploadResult(existingFile, storageResult, true, "文件替换成功");

        } catch (Exception e) {
            log.error("项目文件替换失败: fileId={}, newFileName={}", fileId, newFile.getOriginalFilename(), e);
            return new FileUploadResult(null, null, false, "文件替换失败: " + e.getMessage());
        }
    }

    @Override
    public Resource downloadProjectFile(Long fileId, Long userId) throws IOException {
        log.info("开始下载项目文件: fileId={}, userId={}", fileId, userId);

        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            throw new IllegalArgumentException("文件不存在");
        }

        ProjectFile projectFile = fileOpt.get();
        
        // 检查访问权限
        if (!hasFileAccess(fileId, userId)) {
            throw new SecurityException("没有文件访问权限");
        }

        try {
            Resource resource = fileStorageService.loadAsResource(projectFile.getFilePath());
            log.info("项目文件下载成功: fileId={}, fileName={}", fileId, projectFile.getFileName());
            return resource;
        } catch (IOException e) {
            log.error("项目文件下载失败: fileId={}, fileName={}", fileId, projectFile.getFileName(), e);
            throw new IOException("文件下载失败", e);
        }
    }

    @Override
    public Page<ProjectFile> getProjectFiles(Long projectId, String fileType, Pageable pageable) {
        if (fileType != null && !fileType.trim().isEmpty()) {
            return projectFileRepository.findByProjectIdAndFileType(projectId, fileType, pageable);
        } else {
            return projectFileRepository.findByProjectId(projectId, pageable);
        }
    }

    @Override
    public Optional<ProjectFile> getPrimaryFile(Long projectId, String fileType) {
        return projectFileRepository.findByProjectIdAndFileTypeAndIsPrimaryTrue(projectId, fileType);
    }

    @Override
    public boolean setPrimaryFile(Long fileId, Long userId) {
        log.info("设置主文件: fileId={}, userId={}", fileId, userId);

        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            log.warn("文件不存在: fileId={}", fileId);
            return false;
        }

        ProjectFile projectFile = fileOpt.get();
        
        // 检查权限
        if (!hasFileEditPermission(fileId, userId)) {
            log.warn("没有编辑权限: fileId={}, userId={}", fileId, userId);
            return false;
        }

        try {
            // 设置为主文件（会自动清除同类型的其他主文件标记）
            projectFileRepository.setPrimaryFile(projectFile.getProjectId(), 
                                                projectFile.getFileType(), fileId);
            
            log.info("主文件设置成功: fileId={}", fileId);
            return true;
        } catch (Exception e) {
            log.error("设置主文件失败: fileId={}", fileId, e);
            return false;
        }
    }

    @Override
    public boolean deleteProjectFile(Long fileId, Long userId) {
        log.info("删除项目文件: fileId={}, userId={}", fileId, userId);

        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            log.warn("文件不存在: fileId={}", fileId);
            return false;
        }

        ProjectFile projectFile = fileOpt.get();
        
        // 检查权限
        if (!hasFileEditPermission(fileId, userId)) {
            log.warn("没有删除权限: fileId={}, userId={}", fileId, userId);
            return false;
        }

        try {
            // 删除物理文件
            fileStorageService.delete(projectFile.getFilePath());
            
            // 标记为已删除
            projectFile.markDeleted();
            projectFileRepository.save(projectFile);
            
            log.info("项目文件删除成功: fileId={}", fileId);
            return true;
        } catch (Exception e) {
            log.error("删除项目文件失败: fileId={}", fileId, e);
            return false;
        }
    }

    @Override
    public int deleteProjectFiles(List<Long> fileIds, Long userId) {
        int deletedCount = 0;
        for (Long fileId : fileIds) {
            if (deleteProjectFile(fileId, userId)) {
                deletedCount++;
            }
        }
        log.info("批量删除项目文件完成: 成功删除 {} / {} 个文件", deletedCount, fileIds.size());
        return deletedCount;
    }

    @Override
    public int deleteAllProjectFiles(Long projectId, Long userId) {
        log.info("删除项目所有文件: projectId={}, userId={}", projectId, userId);

        List<ProjectFile> projectFiles = projectFileRepository.findByProjectId(projectId);
        int deletedCount = 0;

        for (ProjectFile projectFile : projectFiles) {
            if (hasFileEditPermission(projectFile.getId(), userId)) {
                try {
                    fileStorageService.delete(projectFile.getFilePath());
                    projectFile.markDeleted();
                    projectFileRepository.save(projectFile);
                    deletedCount++;
                } catch (Exception e) {
                    log.error("删除项目文件失败: fileId={}", projectFile.getId(), e);
                }
            }
        }

        log.info("删除项目所有文件完成: projectId={}, 删除文件数={}", projectId, deletedCount);
        return deletedCount;
    }



    // 基础CRUD方法实现
    @Override
    public ProjectFile save(ProjectFile entity) {
        return projectFileRepository.save(entity);
    }

    @Override
    public List<ProjectFile> saveAll(List<ProjectFile> entities) {
        return projectFileRepository.saveAll(entities);
    }

    @Override
    public Optional<ProjectFile> findById(Long id) {
        return projectFileRepository.findById(id);
    }

    @Override
    public ProjectFile getById(Long id) {
        return projectFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + id));
    }

    @Override
    public List<ProjectFile> findAll() {
        return projectFileRepository.findAll();
    }

    @Override
    public Page<ProjectFile> findAll(Pageable pageable) {
        return projectFileRepository.findAll(pageable);
    }

    @Override
    public boolean existsById(Long id) {
        return projectFileRepository.existsById(id);
    }

    @Override
    public long count() {
        return projectFileRepository.count();
    }

    @Override
    public void deleteById(Long id) {
        projectFileRepository.deleteById(id);
    }

    @Override
    public void delete(ProjectFile entity) {
        projectFileRepository.delete(entity);
    }

    @Override
    public void deleteAll(List<ProjectFile> entities) {
        projectFileRepository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        projectFileRepository.deleteAll();
    }

    @Override
    public SecurityCheckResult performSecurityCheck(MultipartFile file) {
        // 使用新的安全检查服务，默认文件类型为SOURCE
        FileSecurityService.SecurityCheckResult result = fileSecurityService.performSecurityCheck(file, "SOURCE");

        // 转换为旧的SecurityCheckResult格式以保持兼容性
        return new SecurityCheckResult(
            result.isSafe(),
            result.getRiskLevel().getCode(),
            result.getIssues(),
            result.getRecommendation()
        );
    }

    @Override
    public SecurityCheckResult performSecurityCheck(Long fileId) {
        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return new SecurityCheckResult(false, "UNKNOWN",
                Arrays.asList("文件不存在"), "无法进行安全检查");
        }

        ProjectFile projectFile = fileOpt.get();
        List<String> issues = new ArrayList<>();
        String riskLevel = "LOW";
        boolean isSafe = true;

        // 基于已存储文件的安全检查
        if (projectFile.getFileSize() != null && projectFile.getFileSize() > 500 * 1024 * 1024) {
            issues.add("文件过大");
            riskLevel = "MEDIUM";
        }

        String recommendation = isSafe ? "文件安全" : "建议重新检查文件";
        return new SecurityCheckResult(isSafe, riskLevel, issues, recommendation);
    }

    @Override
    public boolean processFile(Long fileId) {
        log.info("开始处理文件: fileId={}", fileId);

        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            log.warn("文件不存在: fileId={}", fileId);
            return false;
        }

        ProjectFile projectFile = fileOpt.get();

        try {
            // 标记为处理中
            projectFile.markProcessing();
            projectFileRepository.save(projectFile);

            // 根据文件类型进行不同的处理
            boolean processResult = false;
            switch (projectFile.getFileType()) {
                case "SOURCE":
                    processResult = processSourceFile(projectFile);
                    break;
                case "COVER":
                    processResult = processCoverImage(projectFile);
                    break;
                default:
                    processResult = true; // 其他类型暂时直接标记为成功
            }

            if (processResult) {
                projectFile.markProcessed();
                log.info("文件处理成功: fileId={}", fileId);
            } else {
                projectFile.markFailed();
                log.warn("文件处理失败: fileId={}", fileId);
            }

            projectFileRepository.save(projectFile);
            return processResult;

        } catch (Exception e) {
            log.error("文件处理异常: fileId={}", fileId, e);
            projectFile.markFailed();
            projectFileRepository.save(projectFile);
            return false;
        }
    }

    /**
     * 处理源码文件
     */
    private boolean processSourceFile(ProjectFile projectFile) {
        log.info("开始处理源码文件: fileId={}, fileName={}", projectFile.getId(), projectFile.getFileName());

        try {
            // 1. 解压文件
            FileExtractionService.ExtractionResult extractionResult = fileExtractionService.extractProjectFile(projectFile);
            if (!extractionResult.isSuccess()) {
                log.warn("文件解压失败: fileId={}, message={}", projectFile.getId(), extractionResult.getMessage());
                return false;
            }

            log.info("文件解压成功: fileId={}, extractedFiles={}, extractedSize={}",
                projectFile.getId(), extractionResult.getFileCount(), extractionResult.getExtractedSize());

            // 2. 分析项目结构
            FileExtractionService.ProjectStructureAnalysis structureAnalysis =
                fileExtractionService.analyzeProjectStructure(extractionResult.getExtractedPath());

            log.info("项目结构分析完成: fileId={}, projectType={}, techStack={}",
                projectFile.getId(), structureAnalysis.getProjectType(), structureAnalysis.getTechStack());

            // 3. 检测Docker配置
            FileExtractionService.DockerConfigDetection dockerDetection =
                fileExtractionService.detectDockerConfig(extractionResult.getExtractedPath());

            if (dockerDetection.isDockerized()) {
                log.info("检测到Docker配置: fileId={}, hasDockerfile={}, baseImage={}",
                    projectFile.getId(), dockerDetection.hasDockerfile(), dockerDetection.getBaseImage());
            }

            // 4. 检查项目完整性
            FileExtractionService.ProjectIntegrityCheck integrityCheck =
                fileExtractionService.checkProjectIntegrity(extractionResult.getExtractedPath());

            log.info("项目完整性检查完成: fileId={}, isComplete={}, qualityScore={}",
                projectFile.getId(), integrityCheck.isComplete(), integrityCheck.getQualityScore());

            // 5. 清理解压文件
            fileExtractionService.cleanupExtractedFiles(extractionResult.getExtractedPath());

            return true;

        } catch (Exception e) {
            log.error("处理源码文件时发生异常: fileId={}", projectFile.getId(), e);
            return false;
        }
    }

    /**
     * 处理封面图片
     */
    private boolean processCoverImage(ProjectFile projectFile) {
        // 这里可以实现图片压缩、缩略图生成等逻辑
        log.info("处理封面图片: {}", projectFile.getFileName());
        return true;
    }

    @Override
    public ProjectFile.FileStatus getFileStatus(Long fileId) {
        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return null;
        }
        return ProjectFile.FileStatus.fromCode(fileOpt.get().getFileStatus());
    }

    @Override
    public boolean updateFileStatus(Long fileId, ProjectFile.FileStatus status, Long userId) {
        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return false;
        }

        ProjectFile projectFile = fileOpt.get();

        // 检查权限
        if (!hasFileEditPermission(fileId, userId)) {
            return false;
        }

        try {
            projectFileRepository.updateFileStatus(fileId, status.getCode());
            log.info("文件状态更新成功: fileId={}, status={}", fileId, status);
            return true;
        } catch (Exception e) {
            log.error("文件状态更新失败: fileId={}, status={}", fileId, status, e);
            return false;
        }
    }

    @Override
    public boolean hasFileAccess(Long fileId, Long userId) {
        // 简化的权限检查逻辑
        // 实际项目中应该根据项目的购买状态、用户权限等进行复杂的权限判断
        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return false;
        }

        ProjectFile projectFile = fileOpt.get();

        // 检查是否是项目所有者
        // 这里需要通过projectId查询项目的userId
        // 暂时简化处理
        return true;
    }

    @Override
    public boolean hasFileEditPermission(Long fileId, Long userId) {
        // 简化的编辑权限检查
        // 实际项目中应该检查用户是否是项目所有者或有管理权限
        return hasFileAccess(fileId, userId);
    }

    @Override
    public FileStatistics getFileStatistics(Long projectId) {
        List<ProjectFile> projectFiles = projectFileRepository.findByProjectId(projectId);

        long totalFiles = projectFiles.size();
        long totalSize = projectFiles.stream().mapToLong(f -> f.getFileSize() != null ? f.getFileSize() : 0).sum();
        long sourceFiles = projectFiles.stream().filter(f -> "SOURCE".equals(f.getFileType())).count();
        long coverImages = projectFiles.stream().filter(f -> "COVER".equals(f.getFileType())).count();
        long demoFiles = projectFiles.stream().filter(f -> "DEMO".equals(f.getFileType())).count();
        long documentFiles = projectFiles.stream().filter(f -> "DOCUMENT".equals(f.getFileType())).count();

        return new FileStatistics(totalFiles, totalSize, sourceFiles, coverImages, demoFiles, documentFiles);
    }

    @Override
    public int cleanupExpiredFiles(int olderThanHours) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(olderThanHours);

        // 查找过期的临时文件
        List<ProjectFile> expiredFiles = projectFileRepository.findAll().stream()
                .filter(f -> f.getFileStatus().equals(ProjectFile.FileStatus.UPLOADING.getCode()) ||
                           f.getFileStatus().equals(ProjectFile.FileStatus.FAILED.getCode()))
                .filter(f -> f.getUpdatedTime().isBefore(cutoffTime))
                .collect(Collectors.toList());

        int cleanedCount = 0;
        for (ProjectFile file : expiredFiles) {
            try {
                fileStorageService.delete(file.getFilePath());
                projectFileRepository.delete(file);
                cleanedCount++;
            } catch (Exception e) {
                log.error("清理过期文件失败: fileId={}", file.getId(), e);
            }
        }

        log.info("清理过期文件完成: 清理了 {} 个文件", cleanedCount);
        return cleanedCount;
    }

    @Override
    public List<List<ProjectFile>> findDuplicateFiles(Long projectId) {
        List<ProjectFile> files;
        if (projectId != null) {
            files = projectFileRepository.findByProjectId(projectId);
        } else {
            files = projectFileRepository.findAll();
        }

        Map<String, List<ProjectFile>> hashGroups = files.stream()
                .filter(f -> f.getFileHash() != null)
                .collect(Collectors.groupingBy(ProjectFile::getFileHash));

        return hashGroups.values().stream()
                .filter(group -> group.size() > 1)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateFileIntegrity(Long fileId) {
        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return false;
        }

        ProjectFile projectFile = fileOpt.get();

        try {
            // 检查文件是否存在
            if (!fileStorageService.exists(projectFile.getFilePath())) {
                return false;
            }

            // 检查文件大小
            long actualSize = fileStorageService.getFileSize(projectFile.getFilePath());
            if (!Objects.equals(actualSize, projectFile.getFileSize())) {
                return false;
            }

            // 检查文件哈希
            String actualHash = fileStorageService.calculateFileHash(projectFile.getFilePath());
            return Objects.equals(actualHash, projectFile.getFileHash());

        } catch (IOException e) {
            log.error("验证文件完整性失败: fileId={}", fileId, e);
            return false;
        }
    }

    @Override
    public String generateDownloadUrl(Long fileId, Long userId, int expirationMinutes) {
        if (!hasFileAccess(fileId, userId)) {
            return null;
        }

        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return null;
        }

        ProjectFile projectFile = fileOpt.get();

        // 生成带时效性的下载URL
        // 实际项目中可以使用JWT或其他方式生成临时URL
        String token = UUID.randomUUID().toString();
        return fileStorageService.getFileUrl(projectFile.getFileName(), "projects/" + projectFile.getProjectId())
               + "?token=" + token + "&expires=" + (System.currentTimeMillis() + expirationMinutes * 60 * 1000);
    }

    @Override
    public String generatePreviewUrl(Long fileId, Long userId) {
        if (!hasFileAccess(fileId, userId)) {
            return null;
        }

        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return null;
        }

        ProjectFile projectFile = fileOpt.get();

        // 对于图片文件，可以直接返回URL
        if ("COVER".equals(projectFile.getFileType())) {
            return projectFile.getFileUrl();
        }

        // 其他类型文件可能需要特殊处理
        return null;
    }

    @Override
    public List<ProjectFile> findFilesByHash(String fileHash) {
        return projectFileRepository.findByFileHash(fileHash);
    }

    @Override
    public long countFilesByType(Long projectId, String fileType) {
        return projectFileRepository.countByProjectIdAndFileType(projectId, fileType);
    }

    @Override
    public long getProjectFilesTotalSize(Long projectId) {
        return projectFileRepository.sumFileSizeByProjectId(projectId);
    }

    @Override
    public Page<ProjectFile> findFailedFiles(Pageable pageable) {
        return projectFileRepository.findByFileStatus(ProjectFile.FileStatus.FAILED.getCode(), pageable);
    }

    @Override
    public Page<ProjectFile> findPendingFiles(Pageable pageable) {
        return projectFileRepository.findByFileStatus(ProjectFile.FileStatus.PROCESSING.getCode(), pageable);
    }

    @Override
    public boolean reprocessFailedFile(Long fileId) {
        Optional<ProjectFile> fileOpt = projectFileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return false;
        }

        ProjectFile projectFile = fileOpt.get();
        if (!ProjectFile.FileStatus.FAILED.getCode().equals(projectFile.getFileStatus())) {
            return false;
        }

        return processFile(fileId);
    }

    @Override
    public int reprocessFailedFiles(List<Long> fileIds) {
        int reprocessedCount = 0;
        for (Long fileId : fileIds) {
            if (reprocessFailedFile(fileId)) {
                reprocessedCount++;
            }
        }
        log.info("批量重新处理失败文件完成: 成功处理 {} / {} 个文件", reprocessedCount, fileIds.size());
        return reprocessedCount;
    }
}
