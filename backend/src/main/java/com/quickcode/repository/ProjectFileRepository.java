package com.quickcode.repository;

import com.quickcode.entity.ProjectFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目文件Repository接口
 * 提供项目文件相关的数据访问方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Repository
public interface ProjectFileRepository extends BaseRepository<ProjectFile, Long> {

    /**
     * 根据项目ID查找文件列表
     */
    List<ProjectFile> findByProjectId(Long projectId);

    /**
     * 根据项目ID分页查找文件列表
     */
    Page<ProjectFile> findByProjectId(Long projectId, Pageable pageable);

    /**
     * 根据项目ID和文件类型查找文件
     */
    List<ProjectFile> findByProjectIdAndFileType(Long projectId, String fileType);

    /**
     * 根据项目ID和文件类型分页查找文件
     */
    Page<ProjectFile> findByProjectIdAndFileType(Long projectId, String fileType, Pageable pageable);

    /**
     * 根据项目ID查找主文件
     */
    Optional<ProjectFile> findByProjectIdAndIsPrimaryTrue(Long projectId);

    /**
     * 根据项目ID和文件类型查找主文件
     */
    Optional<ProjectFile> findByProjectIdAndFileTypeAndIsPrimaryTrue(Long projectId, String fileType);

    /**
     * 根据文件名查找文件
     */
    Optional<ProjectFile> findByFileName(String fileName);

    /**
     * 根据文件路径查找文件
     */
    Optional<ProjectFile> findByFilePath(String filePath);

    /**
     * 根据文件哈希值查找文件
     */
    List<ProjectFile> findByFileHash(String fileHash);

    /**
     * 根据文件状态查找文件
     */
    List<ProjectFile> findByFileStatus(Integer fileStatus);

    /**
     * 根据文件状态分页查找文件
     */
    Page<ProjectFile> findByFileStatus(Integer fileStatus, Pageable pageable);

    /**
     * 根据项目ID和文件状态查找文件
     */
    List<ProjectFile> findByProjectIdAndFileStatus(Long projectId, Integer fileStatus);

    /**
     * 根据文件类型查找文件
     */
    List<ProjectFile> findByFileType(String fileType);

    /**
     * 根据文件类型分页查找文件
     */
    Page<ProjectFile> findByFileType(String fileType, Pageable pageable);

    /**
     * 查找指定时间范围内上传的文件
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.uploadTime BETWEEN :startTime AND :endTime")
    List<ProjectFile> findByUploadTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 查找指定大小范围的文件
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.fileSize BETWEEN :minSize AND :maxSize")
    List<ProjectFile> findByFileSizeBetween(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);

    /**
     * 查找大于指定大小的文件
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.fileSize > :size")
    List<ProjectFile> findByFileSizeGreaterThan(@Param("size") Long size);

    /**
     * 查找处理失败的文件
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.fileStatus = 4")
    List<ProjectFile> findFailedFiles();

    /**
     * 查找待处理的文件
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.fileStatus IN (1, 2)")
    List<ProjectFile> findPendingFiles();

    /**
     * 查找已删除的文件
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.fileStatus = 5")
    List<ProjectFile> findDeletedFiles();

    /**
     * 查找孤儿文件（项目不存在的文件）
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.projectId NOT IN (SELECT p.id FROM Project p)")
    List<ProjectFile> findOrphanFiles();

    /**
     * 根据项目ID统计文件数量
     */
    @Query("SELECT COUNT(pf) FROM ProjectFile pf WHERE pf.projectId = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和文件类型统计文件数量
     */
    @Query("SELECT COUNT(pf) FROM ProjectFile pf WHERE pf.projectId = :projectId AND pf.fileType = :fileType")
    Long countByProjectIdAndFileType(@Param("projectId") Long projectId, @Param("fileType") String fileType);

    /**
     * 根据文件状态统计文件数量
     */
    @Query("SELECT COUNT(pf) FROM ProjectFile pf WHERE pf.fileStatus = :fileStatus")
    Long countByFileStatus(@Param("fileStatus") Integer fileStatus);

    /**
     * 统计项目的总文件大小
     */
    @Query("SELECT COALESCE(SUM(pf.fileSize), 0) FROM ProjectFile pf WHERE pf.projectId = :projectId")
    Long sumFileSizeByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计所有文件的总大小
     */
    @Query("SELECT COALESCE(SUM(pf.fileSize), 0) FROM ProjectFile pf")
    Long sumTotalFileSize();

    /**
     * 根据文件类型统计总大小
     */
    @Query("SELECT COALESCE(SUM(pf.fileSize), 0) FROM ProjectFile pf WHERE pf.fileType = :fileType")
    Long sumFileSizeByFileType(@Param("fileType") String fileType);

    /**
     * 更新文件状态
     */
    @Modifying
    @Query("UPDATE ProjectFile pf SET pf.fileStatus = :status WHERE pf.id = :fileId")
    void updateFileStatus(@Param("fileId") Long fileId, @Param("status") Integer status);

    /**
     * 批量更新文件状态
     */
    @Modifying
    @Query("UPDATE ProjectFile pf SET pf.fileStatus = :status WHERE pf.id IN :fileIds")
    void updateFileStatusBatch(@Param("fileIds") List<Long> fileIds, @Param("status") Integer status);

    /**
     * 更新文件处理时间
     */
    @Modifying
    @Query("UPDATE ProjectFile pf SET pf.processTime = :processTime WHERE pf.id = :fileId")
    void updateProcessTime(@Param("fileId") Long fileId, @Param("processTime") LocalDateTime processTime);

    /**
     * 设置主文件
     */
    @Modifying
    @Query("UPDATE ProjectFile pf SET pf.isPrimary = CASE WHEN pf.id = :fileId THEN true ELSE false END " +
           "WHERE pf.projectId = :projectId AND pf.fileType = :fileType")
    void setPrimaryFile(@Param("projectId") Long projectId, @Param("fileType") String fileType, 
                       @Param("fileId") Long fileId);

    /**
     * 清除项目的主文件标记
     */
    @Modifying
    @Query("UPDATE ProjectFile pf SET pf.isPrimary = false WHERE pf.projectId = :projectId AND pf.fileType = :fileType")
    void clearPrimaryFile(@Param("projectId") Long projectId, @Param("fileType") String fileType);

    /**
     * 删除项目的所有文件
     */
    @Modifying
    @Query("UPDATE ProjectFile pf SET pf.fileStatus = 5 WHERE pf.projectId = :projectId")
    void markDeletedByProjectId(@Param("projectId") Long projectId);

    /**
     * 物理删除已标记删除的文件
     */
    @Modifying
    @Query("DELETE FROM ProjectFile pf WHERE pf.fileStatus = 5 AND pf.updatedTime < :cutoffTime")
    void deleteMarkedFiles(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 检查文件是否存在
     */
    boolean existsByFileName(String fileName);

    /**
     * 检查文件路径是否存在
     */
    boolean existsByFilePath(String filePath);

    /**
     * 检查项目是否有指定类型的文件
     */
    boolean existsByProjectIdAndFileType(Long projectId, String fileType);

    /**
     * 检查项目是否有主文件
     */
    boolean existsByProjectIdAndIsPrimaryTrue(Long projectId);

    /**
     * 查找最近上传的文件
     */
    @Query("SELECT pf FROM ProjectFile pf ORDER BY pf.uploadTime DESC")
    Page<ProjectFile> findRecentFiles(Pageable pageable);

    /**
     * 查找最大的文件
     */
    @Query("SELECT pf FROM ProjectFile pf ORDER BY pf.fileSize DESC")
    Page<ProjectFile> findLargestFiles(Pageable pageable);

    /**
     * 根据MIME类型查找文件
     */
    List<ProjectFile> findByMimeType(String mimeType);

    /**
     * 根据MIME类型模糊查找文件
     */
    @Query("SELECT pf FROM ProjectFile pf WHERE pf.mimeType LIKE :mimeTypePattern")
    List<ProjectFile> findByMimeTypeLike(@Param("mimeTypePattern") String mimeTypePattern);
}
