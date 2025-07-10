package com.quickcode.config.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 自定义Logback转换器，用于获取日志打印位置的相对路径
 * 支持IDE编辑器的快速跳转功能
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
public class RelativePathConverter extends ClassicConverter {
    
    private static final String PROJECT_ROOT_MARKER = "src";
    private static final String BACKEND_MODULE = "backend";
    
    @Override
    public String convert(ILoggingEvent event) {
        StackTraceElement[] stackTrace = event.getCallerData();
        
        if (stackTrace == null || stackTrace.length == 0) {
            return "Unknown";
        }
        
        // 获取调用者信息
        StackTraceElement caller = stackTrace[0];
        String fileName = caller.getFileName();
        
        if (fileName == null) {
            return "Unknown";
        }
        
        // 尝试获取相对路径
        String relativePath = getRelativePath(caller.getClassName(), fileName);
        
        return relativePath;
    }
    
    /**
     * 根据类名和文件名构建相对路径
     * 
     * @param className 完整类名
     * @param fileName 文件名
     * @return 相对路径
     */
    private String getRelativePath(String className, String fileName) {
        try {
            // 将包名转换为路径
            String packagePath = className.replace('.', File.separatorChar);
            
            // 移除类名，保留包路径
            int lastDotIndex = packagePath.lastIndexOf(File.separatorChar);
            if (lastDotIndex > 0) {
                packagePath = packagePath.substring(0, lastDotIndex);
            }
            
            // 构建完整的相对路径
            String relativePath = BACKEND_MODULE + File.separator + 
                                 PROJECT_ROOT_MARKER + File.separator + 
                                 "main" + File.separator + 
                                 "java" + File.separator + 
                                 packagePath + File.separator + 
                                 fileName;
            
            // 标准化路径分隔符（统一使用正斜杠，IDE通常能正确处理）
            relativePath = relativePath.replace(File.separatorChar, '/');
            
            return relativePath;
            
        } catch (Exception e) {
            // 如果路径构建失败，返回简单的文件名
            return fileName;
        }
    }
}
