package com.warmer.web.test;

import com.warmer.web.util.HanLPDataDownloader;
import lombok.extern.slf4j.Slf4j;

/**
 * HanLP数据文件测试类
 */
@Slf4j
public class HanLPDataTest {
    
    public static void main(String[] args) {
        log.info("开始测试HanLP数据文件下载...");
        
        HanLPDataDownloader downloader = new HanLPDataDownloader();
        
        // 检查安装状态
        boolean isInstalled = downloader.isHanLPDataInstalled();
        log.info("HanLP数据文件安装状态: {}", isInstalled);
        log.info("安装状态信息: {}", downloader.getInstallationStatus());
        
        // 注释掉自动下载和安装相关代码
        // if (!isInstalled) {
        //     log.info("开始下载并安装HanLP数据文件...");
        //     boolean success = downloader.downloadAndInstallHanLPData();
        //     if (success) {
        //         log.info("HanLP数据文件安装成功！");
        //     } else {
        //         log.error("HanLP数据文件安装失败！");
        //     }
        // } else {
        //     log.info("HanLP数据文件已安装，无需重复安装");
        // }
    }
} 