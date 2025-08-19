package com.warmer.web.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 抽取结果类
 * 用于存储完整的文本抽取结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResult {
    
    /**
     * 原始文本
     */
    private String originalText;
    
    /**
     * 抽取的实体列表
     */
    private List<ExtractionEntity> entities;
    
    /**
     * 抽取的关系列表
     */
    private List<ExtractionRelation> relations;
    
    /**
     * 抽取得到的三元组 (subject, predicate, object)
     */
    private List<ExtractionTriple> triples;
    
    /**
     * 关键词列表 (TF-IDF)
     */
    private List<String> keywordsTFIDF;
    
    /**
     * 关键词列表 (TextRank)
     */
    private List<String> keywordsTextRank;
    
    /**
     * 关键句列表
     */
    private List<String> keySentences;
    
    /**
     * 抽取统计信息
     */
    private Map<String, Object> statistics;
    
    /**
     * 抽取时间戳
     */
    private Long timestamp;
    
    /**
     * 抽取方法 (TF-IDF, TextRank, HanLP, Hybrid)
     */
    private String extractionMethod;
} 