package com.warmer.web.service;

import com.warmer.web.entity.ExtractionResult;

/**
 * Agent智能抽取服务接口
 */
public interface AgentExtractionService {
    
    /**
     * 使用TF-IDF算法提取关键词
     * @param text 输入文本
     * @param topK 返回前K个关键词
     * @return 抽取结果
     */
    ExtractionResult extractKeywordsByTFIDF(String text, int topK);
    
    /**
     * 使用TextRank算法提取关键词
     * @param text 输入文本
     * @param topK 返回前K个关键词
     * @return 抽取结果
     */
    ExtractionResult extractKeywordsByTextRank(String text, int topK);
    
    /**
     * 使用HanLP进行命名实体识别
     * @param text 输入文本
     * @return 抽取结果
     */
    ExtractionResult extractEntities(String text);
    
    /**
     * 混合抽取策略
     * @param text 输入文本
     * @param topK 关键词数量
     * @return 抽取结果
     */
    ExtractionResult hybridExtract(String text, int topK);
    
    /**
     * 自动抽取知识图谱
     * @param text 输入文本
     * @return 抽取结果
     */
    ExtractionResult autoExtract(String text);
} 