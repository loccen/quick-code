package com.quickcode.service.impl;

import com.quickcode.service.FileExtractionService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件解压服务辅助类
 * 包含项目完整性检查等辅助方法
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public class FileExtractionServiceImplHelper {

    /**
     * 检查是否有README文件
     */
    public static boolean hasReadmeFile(Path rootPath) {
        try {
            return Files.walk(rootPath, 1)
                .anyMatch(path -> path.getFileName().toString().toLowerCase().startsWith("readme"));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查是否有LICENSE文件
     */
    public static boolean hasLicenseFile(Path rootPath) {
        try {
            return Files.walk(rootPath, 1)
                .anyMatch(path -> path.getFileName().toString().toLowerCase().startsWith("license"));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查项目结构是否有效
     */
    public static boolean hasValidProjectStructure(Path rootPath) {
        try {
            long fileCount = Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .count();
            return fileCount > 0 && fileCount < 1000; // 最大1000个文件
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查是否有过大的文件
     */
    public static boolean hasLargeFiles(Path rootPath) {
        try {
            return Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .anyMatch(path -> {
                    try {
                        return Files.size(path) > 50 * 1024 * 1024; // 50MB
                    } catch (IOException e) {
                        return false;
                    }
                });
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 检查文件数量是否过多
     */
    public static boolean hasTooManyFiles(Path rootPath) {
        try {
            long fileCount = Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .count();
            return fileCount > 500;
        } catch (IOException e) {
            return false;
        }
    }
}
