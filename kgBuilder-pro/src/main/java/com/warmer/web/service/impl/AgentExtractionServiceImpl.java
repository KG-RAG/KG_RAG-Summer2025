package com.warmer.web.service.impl;

import com.warmer.web.entity.ExtractionEntity;
import com.warmer.web.entity.ExtractionRelation;
import com.warmer.web.entity.ExtractionResult;
import com.warmer.web.entity.ExtractionTriple;
import com.warmer.web.service.AgentExtractionService;
import com.warmer.web.util.HanLPUtil;
import com.warmer.web.util.TFIDFUtil;
import com.warmer.web.util.TextRankUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent智能抽取服务实现类
 */
@Slf4j
@Service
public class AgentExtractionServiceImpl implements AgentExtractionService {
    
    @Autowired
    private TFIDFUtil tfidfUtil;
    
    @Autowired
    private TextRankUtil textRankUtil;
    
    @Autowired
    private HanLPUtil hanLPUtil;
    
    @Override
    public ExtractionResult extractKeywordsByTFIDF(String text, int topK) {
        try {
            log.info("开始使用TF-IDF算法提取关键词，文本长度: {}", text.length());
            log.info("调用 tfidfUtil.extractKeywords 开始");
            List<String> keywords = tfidfUtil.extractKeywords(text, topK);
            log.info("调用 tfidfUtil.extractKeywords 结束，提取到关键词数量: {}", keywords.size());
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("method", "TF-IDF");
            statistics.put("keywordCount", keywords.size());
            statistics.put("textLength", text.length());
            log.info("TF-IDF关键词提取流程结束，准备返回结果");
            
            log.info("开始构建 ExtractionResult 对象");
            ExtractionResult result = ExtractionResult.builder()
                .originalText(text)
                .keywordsTFIDF(keywords)
                .entities(new ArrayList<>())
                .relations(new ArrayList<>())
                .triples(new ArrayList<>())
                .keywordsTextRank(new ArrayList<>())
                .keySentences(new ArrayList<>())
                .statistics(statistics)
                .timestamp(System.currentTimeMillis())
                .extractionMethod("TF-IDF")
                .build();
            log.info("ExtractionResult 对象构建完成，准备返回");
            return result;
                
        } catch (Exception e) {
            log.error("TF-IDF关键词提取失败", e);
            return createEmptyResult(text, "TF-IDF");
        }
    }
    
    @Override
    public ExtractionResult extractKeywordsByTextRank(String text, int topK) {
        try {
            log.info("开始使用TextRank算法提取关键词，文本长度: {}", text.length());
            log.info("调用 textRankUtil.extractKeywords 开始");
            List<String> keywords = textRankUtil.extractKeywords(text, topK);
            log.info("调用 textRankUtil.extractKeywords 结束，提取到关键词数量: {}", keywords.size());
            log.info("调用 textRankUtil.extractKeySentences 开始");
            List<String> keySentences = textRankUtil.extractKeySentences(text, Math.min(5, topK));
            log.info("调用 textRankUtil.extractKeySentences 结束，提取到关键句数量: {}", keySentences.size());
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("method", "TextRank");
            statistics.put("keywordCount", keywords.size());
            statistics.put("sentenceCount", keySentences.size());
            statistics.put("textLength", text.length());
            log.info("TextRank关键词提取流程结束，准备返回结果");
            
            log.info("开始构建 ExtractionResult 对象");
            ExtractionResult result = ExtractionResult.builder()
                .originalText(text)
                .keywordsTextRank(keywords)
                .keySentences(keySentences)
                .entities(new ArrayList<>())
                .relations(new ArrayList<>())
                .triples(new ArrayList<>())
                .keywordsTFIDF(new ArrayList<>())
                .statistics(statistics)
                .timestamp(System.currentTimeMillis())
                .extractionMethod("TextRank")
                .build();
            log.info("ExtractionResult 对象构建完成，准备返回");
            return result;
                
        } catch (Exception e) {
            log.error("TextRank关键词提取失败", e);
            return createEmptyResult(text, "TextRank");
        }
    }
    
    @Override
    public ExtractionResult extractEntities(String text) {
        try {
            log.info("开始使用HanLP进行命名实体识别，文本长度: {}", text.length());
            log.info("调用 hanLPUtil.extractEntities 开始");
            List<ExtractionEntity> entities = hanLPUtil.extractEntities(text);
            log.info("调用 hanLPUtil.extractEntities 结束，提取到实体数量: {}", entities.size());
            log.info("调用 hanLPUtil.extractRelations 开始");
            List<ExtractionRelation> relations = hanLPUtil.extractRelations(text, entities);
            log.info("调用 hanLPUtil.extractRelations 结束，提取到关系数量: {}", relations.size());
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("method", "HanLP");
            statistics.put("entityCount", entities.size());
            statistics.put("relationCount", relations.size());
            statistics.put("textLength", text.length());
            
            // 统计实体类型分布
            Map<String, Long> entityTypeStats = entities.stream()
                .collect(Collectors.groupingBy(ExtractionEntity::getType, Collectors.counting()));
            statistics.put("entityTypeDistribution", entityTypeStats);
            log.info("HanLP实体识别流程结束，准备返回结果");
            
            log.info("开始构建 ExtractionResult 对象");
            List<ExtractionTriple> triples = buildTriples(entities, relations);
            ExtractionResult result = ExtractionResult.builder()
                .originalText(text)
                .entities(entities)
                .relations(relations)
                .triples(triples)
                .keywordsTFIDF(new ArrayList<>())
                .keywordsTextRank(new ArrayList<>())
                .keySentences(new ArrayList<>())
                .statistics(statistics)
                .timestamp(System.currentTimeMillis())
                .extractionMethod("HanLP")
                .build();
            log.info("ExtractionResult 对象构建完成，准备返回");
            return result;
                
        } catch (Exception e) {
            log.error("HanLP实体识别失败", e);
            return createEmptyResult(text, "HanLP");
        }
    }
    
    @Override
    public ExtractionResult hybridExtract(String text, int topK) {
        try {
            log.info("开始使用混合抽取策略，文本长度: {}", text.length());
            log.info("调用 tfidfUtil.extractKeywords 开始");
            List<String> tfidfKeywords = tfidfUtil.extractKeywords(text, topK);
            log.info("调用 tfidfUtil.extractKeywords 结束，提取到关键词数量: {}", tfidfKeywords.size());
            log.info("调用 textRankUtil.extractKeywords 开始");
            List<String> textRankKeywords = textRankUtil.extractKeywords(text, topK);
            log.info("调用 textRankUtil.extractKeywords 结束，提取到关键词数量: {}", textRankKeywords.size());
            log.info("调用 textRankUtil.extractKeySentences 开始");
            List<String> keySentences = textRankUtil.extractKeySentences(text, Math.min(5, topK));
            log.info("调用 textRankUtil.extractKeySentences 结束，提取到关键句数量: {}", keySentences.size());
            log.info("调用 hanLPUtil.extractEntities 开始");
            List<ExtractionEntity> entities = hanLPUtil.extractEntities(text);
            log.info("调用 hanLPUtil.extractEntities 结束，提取到实体数量: {}", entities.size());
            log.info("调用 hanLPUtil.extractRelations 开始");
            List<ExtractionRelation> relations = hanLPUtil.extractRelations(text, entities);
            log.info("调用 hanLPUtil.extractRelations 结束，提取到关系数量: {}", relations.size());
            Set<String> allKeywords = new HashSet<>();
            allKeywords.addAll(tfidfKeywords);
            allKeywords.addAll(textRankKeywords);
            List<String> mergedKeywords = new ArrayList<>(allKeywords);
            log.info("合并关键词完成，合并后关键词数量: {}", mergedKeywords.size());
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("method", "Hybrid");
            statistics.put("tfidfKeywordCount", tfidfKeywords.size());
            statistics.put("textRankKeywordCount", textRankKeywords.size());
            statistics.put("mergedKeywordCount", mergedKeywords.size());
            statistics.put("entityCount", entities.size());
            statistics.put("relationCount", relations.size());
            statistics.put("sentenceCount", keySentences.size());
            statistics.put("textLength", text.length());
            log.info("混合抽取流程结束，准备返回结果");
            
            log.info("开始构建 ExtractionResult 对象");
            List<ExtractionTriple> triples = buildTriples(entities, relations);
            ExtractionResult result = ExtractionResult.builder()
                .originalText(text)
                .keywordsTFIDF(tfidfKeywords)
                .keywordsTextRank(textRankKeywords)
                .keySentences(keySentences)
                .entities(entities)
                .relations(relations)
                .triples(triples)
                .statistics(statistics)
                .timestamp(System.currentTimeMillis())
                .extractionMethod("Hybrid")
                .build();
            log.info("ExtractionResult 对象构建完成，准备返回");
            return result;
                
        } catch (Exception e) {
            log.error("混合抽取策略失败", e);
            return createEmptyResult(text, "Hybrid");
        }
    }
    
    @Override
    public ExtractionResult autoExtract(String text) {
        try {
            log.info("开始自动抽取知识图谱，文本长度: {}", text.length());
            int textLength = text.length();
            int topK = calculateTopK(textLength);
            log.info("文本长度: {}, 计算得到topK: {}", textLength, topK);
            
            // 关系触发优先：短文本或包含明显关系短语时，优先执行实体+关系抽取
            if (hanLPUtil.containsRelationTrigger(text)) {
                log.info("检测到关系触发短语，优先执行实体+关系抽取");
                return extractEntities(text);
            }
            
            if (textLength < 100) {
                log.info("选择TF-IDF抽取策略");
                return extractKeywordsByTFIDF(text, topK);
            } else if (textLength < 500) {
                log.info("选择TextRank抽取策略");
                return extractKeywordsByTextRank(text, topK);
            } else if (textLength < 2000) {
                log.info("选择HanLP实体识别策略");
                return extractEntities(text);
            } else {
                log.info("选择混合抽取策略");
                return hybridExtract(text, topK);
            }
            
        } catch (Exception e) {
            log.error("自动抽取失败", e);
            return createEmptyResult(text, "Auto");
        }
    }
    
    /**
     * 根据实体与关系列表生成三元组
     */
    private List<ExtractionTriple> buildTriples(List<ExtractionEntity> entities, List<ExtractionRelation> relations) {
        if (entities == null || relations == null) {
            return new ArrayList<>();
        }
        Map<String, ExtractionEntity> idToEntity = entities.stream()
            .collect(Collectors.toMap(ExtractionEntity::getId, e -> e, (a, b) -> a));
        List<ExtractionTriple> triples = new ArrayList<>();
        for (ExtractionRelation r : relations) {
            ExtractionEntity s = idToEntity.get(r.getSourceEntityId());
            ExtractionEntity o = idToEntity.get(r.getTargetEntityId());
            if (s == null || o == null) continue;
            ExtractionTriple t = ExtractionTriple.builder()
                .subject(s.getName())
                .predicate(r.getRelationType())
                .object(o.getName())
                .confidence(r.getConfidence())
                .subjectType(s.getType())
                .objectType(o.getType())
                .build();
            triples.add(t);
        }
        return triples;
    }
    
    /**
     * 根据文本长度计算关键词数量
     * @param textLength 文本长度
     * @return 关键词数量
     */
    private int calculateTopK(int textLength) {
        if (textLength < 100) {
            return 3;
        } else if (textLength < 500) {
            return 5;
        } else if (textLength < 2000) {
            return 10;
        } else {
            return 15;
        }
    }
    
    /**
     * 创建空结果
     * @param text 原始文本
     * @param method 抽取方法
     * @return 空结果
     */
    private ExtractionResult createEmptyResult(String text, String method) {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("method", method);
        statistics.put("error", true);
        statistics.put("textLength", text.length());
        
        return ExtractionResult.builder()
            .originalText(text)
            .entities(new ArrayList<>())
            .relations(new ArrayList<>())
            .triples(new ArrayList<>())
            .keywordsTFIDF(new ArrayList<>())
            .keywordsTextRank(new ArrayList<>())
            .keySentences(new ArrayList<>())
            .statistics(statistics)
            .timestamp(System.currentTimeMillis())
            .extractionMethod(method)
            .build();
    }
} 