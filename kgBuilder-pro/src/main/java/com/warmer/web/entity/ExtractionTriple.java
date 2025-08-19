package com.warmer.web.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 三元组结果
 * 表示 (subject, predicate, object) 结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionTriple {
    /**
     * 主语（源实体名称）
     */
    private String subject;

    /**
     * 谓语（关系类型/关系词）
     */
    private String predicate;

    /**
     * 宾语（目标实体名称）
     */
    private String object;

    /**
     * 置信度 (0.0-1.0)
     */
    private Double confidence;

    /**
     * 可选：主语类型
     */
    private String subjectType;

    /**
     * 可选：宾语类型
     */
    private String objectType;
} 