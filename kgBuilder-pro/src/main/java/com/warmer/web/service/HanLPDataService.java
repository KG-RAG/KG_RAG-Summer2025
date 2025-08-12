package com.warmer.web.service;

import com.warmer.web.util.HanLPDataDownloader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

/**
 * HanLP数据文件服务
 * 负责启动时检查和安装HanLP数据文件
 */
@Slf4j
@Service
public class HanLPDataService implements ApplicationRunner {
    
    @Autowired
    private HanLPDataDownloader hanLPDataDownloader;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("启动HanLP数据文件检查...");
        // 只检查，不自动下载
        if (!hanLPDataDownloader.isHanLPDataInstalled()) {
            log.warn("HanLP数据文件未安装，请手动安装！");
            // 注释掉自动下载
            // boolean success = hanLPDataDownloader.downloadAndInstallHanLPData();
            // if (success) {
            //     log.info("HanLP数据文件自动安装成功");
            // } else {
            //     log.error("HanLP数据文件自动安装失败，请手动安装");
            //     log.error("请访问 http://localhost:8080/api/hanlp/install 进行手动安装");
            // }
        } else {
            log.info("HanLP数据文件已正确安装");
        }
    }
    
    /**
     * 手动检查并安装HanLP数据文件
     */
    public boolean checkAndInstallHanLPData() {
        try {
            if (!hanLPDataDownloader.isHanLPDataInstalled()) {
                log.info("HanLP数据文件未安装，开始安装...");
                return hanLPDataDownloader.downloadAndInstallHanLPData();
            } else {
                log.info("HanLP数据文件已安装");
                return true;
            }
        } catch (Exception e) {
            log.error("检查并安装HanLP数据文件时发生错误", e);
            return false;
        }
    }
    
    /**
     * 获取安装状态
     */
    public String getInstallationStatus() {
        return hanLPDataDownloader.getInstallationStatus();
    }
} 