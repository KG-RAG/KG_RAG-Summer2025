package com.warmer.web.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 抽取关系类
 * 用于存储从文本中抽取的关系信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionRelation {
    
    /**
     * 关系ID
     */
    private String id;
    
    /**
     * 源实体ID
     */
    private String sourceEntityId;
    
    /**
     * 目标实体ID
     */
    private String targetEntityId;
    
    /**
     * 关系类型
     */
    private String relationType;
    
    /**
     * 关系描述
     */
    private String description;
    
    /**
     * 关系置信度 (0.0-1.0)
     */
    private Double confidence;
    
    /**
     * 关系在文本中的位置
     */
    private Integer startPosition;
    
    /**
     * 关系在文本中的结束位置
     */
    private Integer endPosition;
} 