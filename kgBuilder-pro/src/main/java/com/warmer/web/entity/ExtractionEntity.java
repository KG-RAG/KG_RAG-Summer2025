package com.warmer.web.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 抽取实体类
 * 用于存储从文本中抽取的实体信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionEntity {
    
    /**
     * 实体ID
     */
    private String id;
    
    /**
     * 实体名称
     */
    private String name;
    
    /**
     * 实体类型 (PERSON, LOCATION, ORGANIZATION, CONCEPT, etc.)
     */
    private String type;
    
    /**
     * 实体在文本中的位置
     */
    private Integer startPosition;
    
    /**
     * 实体在文本中的结束位置
     */
    private Integer endPosition;
    
    /**
     * 实体置信度 (0.0-1.0)
     */
    private Double confidence;
    
    /**
     * 实体属性
     */
    private List<EntityProperty> properties;
    
    /**
     * 实体属性类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityProperty {
        private String key;
        private String value;
        private String type;
    }
} 