package com.warmer.web.controller;

import com.warmer.base.util.R;
import com.warmer.web.util.HanLPDataDownloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * HanLP数据文件管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/hanlp")
public class HanLPDataController {
    
    @Autowired
    private HanLPDataDownloader hanLPDataDownloader;
    
    /**
     * 检查HanLP数据文件安装状态
     */
    @GetMapping("/status")
    public R<Map<String, Object>> checkInstallationStatus() {
        try {
            boolean isInstalled = hanLPDataDownloader.isHanLPDataInstalled();
            String statusMessage = hanLPDataDownloader.getInstallationStatus();
            
            Map<String, Object> result = new HashMap<>();
            result.put("installed", isInstalled);
            result.put("status", statusMessage);
            result.put("dataPath", "D:/HanLP/");
            
            return R.success(result, "检查完成");
            
        } catch (Exception e) {
            log.error("检查HanLP安装状态时发生错误", e);
            return R.error("检查安装状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载并安装HanLP数据文件
     */
    @PostMapping("/install")
    public R<String> installHanLPData() {
        try {
            log.info("开始安装HanLP数据文件...");
            
            // 检查是否已经安装
            if (hanLPDataDownloader.isHanLPDataInstalled()) {
                return R.success("HanLP数据文件已安装，无需重复安装");
            }
            
            // 执行安装
            boolean success = hanLPDataDownloader.downloadAndInstallHanLPData();
            
            if (success) {
                return R.success("HanLP数据文件安装成功");
            } else {
                return R.error("HanLP数据文件安装失败");
            }
            
        } catch (Exception e) {
            log.error("安装HanLP数据文件时发生错误", e);
            return R.error("安装失败: " + e.getMessage());
        }
    }
    
    /**
     * 强制重新安装HanLP数据文件
     */
    @PostMapping("/reinstall")
    public R<String> reinstallHanLPData() {
        try {
            log.info("开始重新安装HanLP数据文件...");
            
            // 执行安装
            boolean success = hanLPDataDownloader.downloadAndInstallHanLPData();
            
            if (success) {
                return R.success("HanLP数据文件重新安装成功");
            } else {
                return R.error("HanLP数据文件重新安装失败");
            }
            
        } catch (Exception e) {
            log.error("重新安装HanLP数据文件时发生错误", e);
            return R.error("重新安装失败: " + e.getMessage());
        }
    }
} 