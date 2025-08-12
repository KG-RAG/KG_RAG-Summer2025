package com.warmer.web.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * HanLP数据文件下载器
 * 用于自动下载和安装HanLP数据文件
 */
@Slf4j
@Component
public class HanLPDataDownloader {
    
    private static final String HANLP_DATA_URL = "https://github.com/hankcs/HanLP/releases/download/v1.7.8/data-for-1.7.8.zip";
    private static final String HANLP_ROOT_DIR = "D:/HanLP/";
    private static final String TEMP_DOWNLOAD_FILE = "hanlp-data.zip";
    
    /**
     * 下载并安装HanLP数据文件
     */
    public boolean downloadAndInstallHanLPData() {
        try {
            log.info("开始下载HanLP数据文件...");
            
            // 1. 创建目录
            createDirectories();
            
            // 2. 下载数据文件
            if (!downloadDataFile()) {
                log.error("下载HanLP数据文件失败");
                return false;
            }
            
            // 3. 解压数据文件
            if (!extractDataFile()) {
                log.error("解压HanLP数据文件失败");
                return false;
            }
            
            // 4. 清理临时文件
            cleanupTempFiles();
            
            log.info("HanLP数据文件安装完成");
            return true;
            
        } catch (Exception e) {
            log.error("安装HanLP数据文件时发生错误", e);
            return false;
        }
    }
    
    /**
     * 创建必要的目录
     */
    private void createDirectories() throws IOException {
        log.info("创建HanLP目录结构...");
        
        // 创建主目录
        Path hanlpRoot = Paths.get(HANLP_ROOT_DIR);
        if (!Files.exists(hanlpRoot)) {
            Files.createDirectories(hanlpRoot);
        }
        
        // 创建子目录
        String[] subDirs = {
            "data/dictionary",
            "data/dictionary/custom",
            "data/dictionary/person",
            "data/dictionary/synonym",
            "data/dictionary/tc",
            "data/model/segment",
            "data/model/perceptron/pku199801",
            "data/model/crf/pku199801"
        };
        
        for (String subDir : subDirs) {
            Path dir = hanlpRoot.resolve(subDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        }
        
        log.info("目录结构创建完成");
    }
    
    /**
     * 下载数据文件
     */
    private boolean downloadDataFile() {
        try {
            log.info("正在下载HanLP数据文件: {}", HANLP_DATA_URL);
            
            URL url = new URL(HANLP_DATA_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(300000); // 5分钟超时
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("下载失败，HTTP响应码: {}", responseCode);
                return false;
            }
            
            // 获取文件大小
            int contentLength = connection.getContentLength();
            log.info("文件大小: {} bytes", contentLength);
            
            // 下载文件
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(TEMP_DOWNLOAD_FILE)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                int totalBytesRead = 0;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    // 显示下载进度
                    if (contentLength > 0) {
                        int progress = (int) ((double) totalBytesRead / contentLength * 100);
                        if (totalBytesRead % (1024 * 1024) == 0) { // 每MB显示一次进度
                            log.info("下载进度: {}%", progress);
                        }
                    }
                }
            }
            
            log.info("下载完成");
            return true;
            
        } catch (Exception e) {
            log.error("下载HanLP数据文件时发生错误", e);
            return false;
        }
    }
    
    /**
     * 解压数据文件
     */
    private boolean extractDataFile() {
        try {
            log.info("正在解压HanLP数据文件...");
            
            Path hanlpRoot = Paths.get(HANLP_ROOT_DIR);
            
            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(TEMP_DOWNLOAD_FILE))) {
                ZipEntry entry;
                int extractedFiles = 0;
                
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    
                    // 跳过目录条目
                    if (entry.isDirectory()) {
                        continue;
                    }
                    
                    // 构建目标路径
                    Path targetPath = hanlpRoot.resolve(entryName);
                    
                    // 确保父目录存在
                    Path parentDir = targetPath.getParent();
                    if (parentDir != null && !Files.exists(parentDir)) {
                        Files.createDirectories(parentDir);
                    }
                    
                    // 解压文件
                    try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    extractedFiles++;
                    if (extractedFiles % 100 == 0) {
                        log.info("已解压 {} 个文件", extractedFiles);
                    }
                }
                
                log.info("解压完成，共解压 {} 个文件", extractedFiles);
                return true;
                
            } catch (Exception e) {
                log.error("解压文件时发生错误", e);
                return false;
            }
            
        } catch (Exception e) {
            log.error("解压HanLP数据文件时发生错误", e);
            return false;
        }
    }
    
    /**
     * 清理临时文件
     */
    private void cleanupTempFiles() {
        try {
            Path tempFile = Paths.get(TEMP_DOWNLOAD_FILE);
            if (Files.exists(tempFile)) {
                Files.delete(tempFile);
                log.info("临时文件已清理");
            }
        } catch (Exception e) {
            log.warn("清理临时文件时发生错误", e);
        }
    }
    
    /**
     * 检查HanLP数据文件是否已安装
     */
    public boolean isHanLPDataInstalled() {
        try {
            Path hanlpRoot = Paths.get(HANLP_ROOT_DIR);
            if (!Files.exists(hanlpRoot)) {
                return false;
            }
            
            // 只检查基础词典文件，不检查已注释掉的模型文件
            String[] keyFiles = {
                "data/dictionary/CoreNatureDictionary.txt",
                "data/dictionary/CoreNatureDictionary.ngram.txt",
                "data/dictionary/stopwords.txt"
                // 注释掉不存在的模型文件检查
                // "data/model/segment/HMMSegmentModel.bin"
            };
            
            for (String keyFile : keyFiles) {
                Path filePath = hanlpRoot.resolve(keyFile);
                if (!Files.exists(filePath)) {
                    log.info("缺少关键文件: {}", keyFile);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("检查HanLP数据文件安装状态时发生错误", e);
            return false;
        }
    }
    
    /**
     * 获取安装状态信息
     */
    public String getInstallationStatus() {
        if (isHanLPDataInstalled()) {
            return "HanLP数据文件已正确安装";
        } else {
            return "HanLP数据文件未安装或安装不完整";
        }
    }
} 