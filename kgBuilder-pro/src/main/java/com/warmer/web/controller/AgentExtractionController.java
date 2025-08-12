package com.warmer.web.controller;

import com.warmer.web.entity.ExtractionResult;
import com.warmer.web.service.AgentExtractionService;
import com.warmer.web.util.TFIDFUtil;
import com.warmer.web.util.TextRankUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent智能抽取控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class AgentExtractionController {
    
    @Autowired
    private AgentExtractionService agentExtractionService;
    
    @Autowired
    private TFIDFUtil tfidfUtil;
    
    @Autowired
    private TextRankUtil textRankUtil;
    
    /**
     * TF-IDF关键词提取
     */
    @PostMapping("/extract/tfidf")
    public ResponseEntity<Map<String, Object>> extractKeywordsByTFIDF(
            @RequestParam("text") String text,
            @RequestParam(value = "topK", defaultValue = "10") Integer topK) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文本内容不能为空"));
            }
            
            ExtractionResult result = agentExtractionService.extractKeywordsByTFIDF(text, topK);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", result);
            response.put("msg", "TF-IDF关键词提取成功");
            
            log.info("接口即将返回数据: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("TF-IDF关键词提取失败", e);
            log.error("错误详情: text={}, topK={}, error={}", text, topK, e.getMessage());
            return ResponseEntity.internalServerError().body(createErrorResponse("TF-IDF关键词提取失败: " + e.getMessage()));
        }
    }
    
    /**
     * TextRank关键词提取
     */
    @PostMapping("/extract/textrank")
    public ResponseEntity<Map<String, Object>> extractKeywordsByTextRank(
            @RequestParam("text") String text,
            @RequestParam(value = "topK", defaultValue = "10") Integer topK) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文本内容不能为空"));
            }
            
            ExtractionResult result = agentExtractionService.extractKeywordsByTextRank(text, topK);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", result);
            response.put("msg", "TextRank关键词提取成功");
            
            log.info("接口即将返回数据: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("TextRank关键词提取失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("TextRank关键词提取失败: " + e.getMessage()));
        }
    }
    
    /**
     * HanLP实体识别
     */
    @PostMapping("/extract/entities")
    public ResponseEntity<Map<String, Object>> extractEntities(@RequestParam("text") String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文本内容不能为空"));
            }
            
            ExtractionResult result = agentExtractionService.extractEntities(text);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", result);
            response.put("msg", "HanLP实体识别成功");
            
            log.info("接口即将返回数据: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("HanLP实体识别失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("HanLP实体识别失败: " + e.getMessage()));
        }
    }
    
    /**
     * 混合抽取策略
     */
    @PostMapping("/extract/hybrid")
    public ResponseEntity<Map<String, Object>> hybridExtract(
            @RequestParam("text") String text,
            @RequestParam(value = "topK", defaultValue = "10") Integer topK) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文本内容不能为空"));
            }
            
            ExtractionResult result = agentExtractionService.hybridExtract(text, topK);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", result);
            response.put("msg", "混合抽取策略成功");
            
            log.info("接口即将返回数据: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("混合抽取策略失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("混合抽取策略失败: " + e.getMessage()));
        }
    }
    
    /**
     * 自动抽取
     */
    @PostMapping("/extract/auto")
    public ResponseEntity<Map<String, Object>> autoExtract(@RequestParam("text") String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("文本内容不能为空"));
            }
            
            ExtractionResult result = agentExtractionService.autoExtract(text);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", result);
            response.put("msg", "自动抽取成功");
            
            log.info("接口即将返回数据: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("自动抽取失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("自动抽取失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取抽取方法列表
     */
    @GetMapping("/methods")
    public ResponseEntity<Map<String, Object>> getExtractionMethods() {
        try {
            Map<String, Object> methods = new HashMap<>();
            methods.put("TF-IDF", "基于词频-逆文档频率的关键词提取算法");
            methods.put("TextRank", "基于图排序的关键词和关键句提取算法");
            methods.put("HanLP", "基于HanLP的命名实体识别和关系抽取");
            methods.put("Hybrid", "混合抽取策略，结合多种算法");
            methods.put("Auto", "自动选择最适合的抽取策略");
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", methods);
            response.put("msg", "获取抽取方法列表成功");
            
            log.info("接口即将返回数据: {}", response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取抽取方法列表失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("获取抽取方法列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 系统诊断接口
     */
    @GetMapping("/diagnose")
    public ResponseEntity<Map<String, Object>> diagnose() {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("msg", "系统诊断完成");
            
            Map<String, Object> data = new HashMap<>();
            
            // 检查HanLP
            try {
                com.hankcs.hanlp.HanLP.segment("测试文本");
                data.put("hanlp", "正常");
            } catch (Exception e) {
                data.put("hanlp", "异常: " + e.getMessage());
            }
            
            // 检查TF-IDF工具
            try {
                tfidfUtil.extractKeywords("测试文本", 3);
                data.put("tfidf", "正常");
            } catch (Exception e) {
                data.put("tfidf", "异常: " + e.getMessage());
            }
            
            // 检查TextRank工具
            try {
                textRankUtil.extractKeywords("测试文本", 3);
                data.put("textrank", "正常");
            } catch (Exception e) {
                data.put("textrank", "异常: " + e.getMessage());
            }
            
            result.put("data", data);
            log.info("接口即将返回数据: {}", result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("系统诊断失败", e);
            return ResponseEntity.internalServerError().body(createErrorResponse("系统诊断失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("msg", message);
        response.put("data", null);
        return response;
    }
} 