package com.warmer.web.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.warmer.web.entity.ExtractionEntity;
import com.warmer.web.entity.ExtractionRelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * HanLP工具类
 * 用于命名实体识别和关系抽取
 */
@Slf4j
@Component
public class HanLPUtil {
    
    /**
     * 实体类型映射
     */
    private static final Map<String, String> ENTITY_TYPE_MAP = new HashMap<>();
    
    static {
        ENTITY_TYPE_MAP.put("nr", "PERSON");
        ENTITY_TYPE_MAP.put("ns", "LOCATION");
        ENTITY_TYPE_MAP.put("nt", "ORGANIZATION");
        ENTITY_TYPE_MAP.put("nz", "CONCEPT");
        ENTITY_TYPE_MAP.put("n", "CONCEPT");
    }
    
    /**
     * 关系模式
     */
    private static final List<String> RELATION_PATTERNS = Arrays.asList(
        "是", "在", "有", "包含", "属于", "位于", "工作", "研究", "开发", "发明", "发现", "创建", "建立", "成立", "担任", "负责", "管理", "领导"
    );
    
    /**
     * 使用HanLP进行命名实体识别
     * @param text 输入文本
     * @return 实体列表
     */
    public List<ExtractionEntity> extractEntities(String text) {
        try {
            List<ExtractionEntity> entities = new ArrayList<>();
            List<Term> terms = HanLP.segment(text);
            
            int position = 0;
            for (int i = 0; i < terms.size(); i++) {
                Term term = terms.get(i);
                String nature = term.nature.toString();
                
                // 检查是否为实体
                if (ENTITY_TYPE_MAP.containsKey(nature)) {
                    String entityType = ENTITY_TYPE_MAP.get(nature);
                    String entityName = term.word;
                    
                    // 合并连续的相同类型实体
                    int endPosition = position + entityName.length();
                    while (i + 1 < terms.size()) {
                        Term nextTerm = terms.get(i + 1);
                        if (nextTerm.nature.toString().equals(nature)) {
                            entityName += nextTerm.word;
                            endPosition += nextTerm.word.length();
                            i++;
                        } else {
                            break;
                        }
                    }
                    
                    // 创建实体对象
                    ExtractionEntity entity = ExtractionEntity.builder()
                        .id(generateEntityId())
                        .name(entityName)
                        .type(entityType)
                        .startPosition(position)
                        .endPosition(endPosition)
                        .confidence(calculateEntityConfidence(entityName, entityType))
                        .properties(new ArrayList<>())
                        .build();
                    
                    entities.add(entity);
                }
                
                position += term.word.length();
            }
            
            return entities;
            
        } catch (Exception e) {
            log.error("HanLP recognition failed", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 关系抽取
     * @param text 输入文本
     * @param entities 实体列表
     * @return 关系列表
     */
    public List<ExtractionRelation> extractRelations(String text, List<ExtractionEntity> entities) {
        try {
            List<ExtractionRelation> relations = new ArrayList<>();
            List<Term> terms = HanLP.segment(text);
            
            // 构建实体位置映射
            Map<Integer, ExtractionEntity> entityPositionMap = new HashMap<>();
            for (ExtractionEntity entity : entities) {
                for (int i = entity.getStartPosition(); i < entity.getEndPosition(); i++) {
                    entityPositionMap.put(i, entity);
                }
            }
            
            // 查找关系词
            for (int i = 0; i < terms.size(); i++) {
                Term term = terms.get(i);
                String word = term.word;
                
                if (RELATION_PATTERNS.contains(word)) {
                    // 查找前后实体
                    ExtractionEntity sourceEntity = findNearestEntity(i, terms, entityPositionMap, -1);
                    ExtractionEntity targetEntity = findNearestEntity(i, terms, entityPositionMap, 1);
                    
                    if (sourceEntity != null && targetEntity != null) {
                        ExtractionRelation relation = ExtractionRelation.builder()
                            .id(generateRelationId())
                            .sourceEntityId(sourceEntity.getId())
                            .targetEntityId(targetEntity.getId())
                            .relationType(word)
                            .description(word)
                            .confidence(calculateRelationConfidence(word, sourceEntity, targetEntity))
                            .startPosition(term.offset)
                            .endPosition(term.offset + word.length())
                            .build();
                        
                        relations.add(relation);
                    }
                }
            }
            
            return relations;
            
        } catch (Exception e) {
            log.error("HanLP relation extraction failed", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 查找最近的实体
     * @param currentIndex 当前词索引
     * @param terms 词列表
     * @param entityPositionMap 实体位置映射
     * @param direction 搜索方向 (-1: 向前, 1: 向后)
     * @return 找到的实体
     */
    private ExtractionEntity findNearestEntity(int currentIndex, List<Term> terms, 
                                            Map<Integer, ExtractionEntity> entityPositionMap, int direction) {
        int index = currentIndex + direction;
        int maxDistance = 5; // 最大搜索距离
        int distance = 0;
        
        while (index >= 0 && index < terms.size() && distance < maxDistance) {
            Term term = terms.get(index);
            int offset = term.offset;
            
            if (entityPositionMap.containsKey(offset)) {
                return entityPositionMap.get(offset);
            }
            
            index += direction;
            distance++;
        }
        
        return null;
    }
    
    /**
     * 计算实体置信度
     * @param entityName 实体名称
     * @param entityType 实体类型
     * @return 置信度
     */
    private Double calculateEntityConfidence(String entityName, String entityType) {
        double confidence = 0.8; // 基础置信度
        
        // 根据实体长度调整置信度
        if (entityName.length() >= 2) {
            confidence += 0.1;
        }
        
        // 根据实体类型调整置信度
        if ("PERSON".equals(entityType) || "LOCATION".equals(entityType)) {
            confidence += 0.1;
        }
        
        return Math.min(confidence, 1.0);
    }
    
    /**
     * 计算关系置信度
     * @param relationWord 关系词
     * @param sourceEntity 源实体
     * @param targetEntity 目标实体
     * @return 置信度
     */
    private Double calculateRelationConfidence(String relationWord, ExtractionEntity sourceEntity, 
                                            ExtractionEntity targetEntity) {
        double confidence = 0.7; // 基础置信度
        
        // 根据关系词类型调整置信度
        if ("是".equals(relationWord) || "在".equals(relationWord)) {
            confidence += 0.2;
        }
        
        // 根据实体类型组合调整置信度
        if ("PERSON".equals(sourceEntity.getType()) && "ORGANIZATION".equals(targetEntity.getType())) {
            confidence += 0.1;
        }
        
        return Math.min(confidence, 1.0);
    }
    
    /**
     * 生成实体ID
     * @return 实体ID
     */
    private String generateEntityId() {
        return "entity_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
    }
    
    /**
     * 生成关系ID
     * @return 关系ID
     */
    private String generateRelationId() {
        return "relation_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
    }
} 