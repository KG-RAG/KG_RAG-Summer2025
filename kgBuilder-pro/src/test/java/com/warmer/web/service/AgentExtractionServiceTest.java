package com.warmer.web.service;

import com.warmer.web.entity.ExtractionResult;
import com.warmer.web.service.impl.AgentExtractionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Agent抽取服务测试类
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-dev.yml")
public class AgentExtractionServiceTest {
    
    private AgentExtractionServiceImpl agentExtractionService = new AgentExtractionServiceImpl();
    
    @Test
    public void testTFIDFExtraction() {
        String testText = "人工智能是计算机科学的一个分支，它企图了解智能的实质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。该领域的研究包括机器人、语言识别、图像识别、自然语言处理和专家系统等。";
        
        ExtractionResult result = agentExtractionService.extractKeywordsByTFIDF(testText, 5);
        
        assertNotNull(result);
        assertEquals("TF-IDF", result.getExtractionMethod());
        assertNotNull(result.getKeywordsTFIDF());
        assertTrue(result.getKeywordsTFIDF().size() > 0);
        
        System.out.println("TF-IDF关键词: " + result.getKeywordsTFIDF());
    }
    
    @Test
    public void testTextRankExtraction() {
        String testText = "深度学习是机器学习的分支，是一种以人工神经网络为架构，对数据进行表征学习的算法。深度学习的概念源于人工神经网络的研究，含多隐层的多层感知器就是一种深度学习结构。";
        
        ExtractionResult result = agentExtractionService.extractKeywordsByTextRank(testText, 5);
        
        assertNotNull(result);
        assertEquals("TextRank", result.getExtractionMethod());
        assertNotNull(result.getKeywordsTextRank());
        assertTrue(result.getKeywordsTextRank().size() > 0);
        
        System.out.println("TextRank关键词: " + result.getKeywordsTextRank());
        System.out.println("关键句: " + result.getKeySentences());
    }
    
    @Test
    public void testEntityExtraction() {
        String testText = "张三在北京大学计算机科学系工作，他主要研究人工智能和机器学习。李四是清华大学的研究生，正在研究深度学习算法。";
        
        ExtractionResult result = agentExtractionService.extractEntities(testText);
        
        assertNotNull(result);
        assertEquals("HanLP", result.getExtractionMethod());
        assertNotNull(result.getEntities());
        assertTrue(result.getEntities().size() > 0);
        
        System.out.println("识别到的实体: ");
        result.getEntities().forEach(entity -> {
            System.out.println("- " + entity.getName() + " (" + entity.getType() + ")");
        });
        
        System.out.println("识别到的关系: ");
        result.getRelations().forEach(relation -> {
            System.out.println("- " + relation.getSourceEntityId() + " " + relation.getRelationType() + " " + relation.getTargetEntityId());
        });
    }
    
    @Test
    public void testHybridExtraction() {
        String testText = "知识图谱是人工智能的重要技术，它通过结构化的方式表示知识，支持智能问答、推荐系统等应用。Google、百度等公司都在积极发展知识图谱技术。";
        
        ExtractionResult result = agentExtractionService.hybridExtract(testText, 10);
        
        assertNotNull(result);
        assertEquals("Hybrid", result.getExtractionMethod());
        assertNotNull(result.getKeywordsTFIDF());
        assertNotNull(result.getKeywordsTextRank());
        assertNotNull(result.getEntities());
        
        System.out.println("TF-IDF关键词: " + result.getKeywordsTFIDF());
        System.out.println("TextRank关键词: " + result.getKeywordsTextRank());
        System.out.println("关键句: " + result.getKeySentences());
        System.out.println("实体数量: " + result.getEntities().size());
        System.out.println("关系数量: " + result.getRelations().size());
    }
    
    @Test
    public void testAutoExtraction() {
        // 短文本测试
        String shortText = "人工智能技术发展迅速。";
        ExtractionResult shortResult = agentExtractionService.autoExtract(shortText);
        assertNotNull(shortResult);
        System.out.println("短文本抽取方法: " + shortResult.getExtractionMethod());
        
        // 中等文本测试
        String mediumText = "机器学习是人工智能的一个重要分支，它通过算法让计算机从数据中学习，从而能够进行预测和决策。深度学习是机器学习的一个子领域，使用多层神经网络来学习复杂的模式。";
        ExtractionResult mediumResult = agentExtractionService.autoExtract(mediumText);
        assertNotNull(mediumResult);
        System.out.println("中等文本抽取方法: " + mediumResult.getExtractionMethod());
        
        // 长文本测试
        String longText = "知识图谱（Knowledge Graph）是人工智能领域的重要技术，它以结构化的方式表示知识，通过实体、属性和关系来描述现实世界中的概念和它们之间的联系。知识图谱可以支持多种智能应用，包括智能问答、推荐系统、搜索引擎优化等。在学术界，知识图谱的研究涉及自然语言处理、机器学习、数据库等多个领域。在工业界，Google、百度、阿里巴巴等大型科技公司都在积极发展知识图谱技术，并将其应用于搜索引擎、智能助手等产品中。知识图谱的构建过程包括信息抽取、知识融合、知识推理等步骤，需要综合运用多种技术手段。";
        ExtractionResult longResult = agentExtractionService.autoExtract(longText);
        assertNotNull(longResult);
        System.out.println("长文本抽取方法: " + longResult.getExtractionMethod());
    }
} 