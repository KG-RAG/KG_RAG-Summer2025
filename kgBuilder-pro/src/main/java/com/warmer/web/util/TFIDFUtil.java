package com.warmer.web.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TF-IDF算法工具类
 * 用于关键词提取
 */
@Slf4j
@Component
public class TFIDFUtil {
    
    /**
     * 停用词集合
     */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这"
    ));
    
    /**
     * 词性过滤集合 (只保留名词、动词、形容词)
     */
    private static final Set<String> VALID_POS = new HashSet<>(Arrays.asList(
        "n", "nr", "ns", "nt", "nz", "v", "vn", "a", "an"
    ));
    
    /**
     * 使用TF-IDF算法提取关键词
     * @param text 输入文本
     * @param topK 返回前K个关键词
     * @return 关键词列表
     */
    public List<String> extractKeywords(String text, int topK) {
        try {
            // 1. 分词
            List<Term> terms = HanLP.segment(text);
            
            // 2. 过滤停用词和无效词性
            List<String> filteredWords = terms.stream()
                .filter(term -> !STOP_WORDS.contains(term.word))
                .filter(term -> VALID_POS.contains(term.nature.toString()))
                .map(term -> term.word)
                .collect(Collectors.toList());
            
            // 3. 计算TF值
            Map<String, Double> tfMap = calculateTF(filteredWords);
            
            // 4. 计算IDF值 (这里简化处理，实际应该基于语料库)
            Map<String, Double> idfMap = calculateIDF(filteredWords);
            
            // 5. 计算TF-IDF值
            Map<String, Double> tfidfMap = new HashMap<>();
            for (String word : tfMap.keySet()) {
                double tfidf = tfMap.get(word) * idfMap.getOrDefault(word, 1.0);
                tfidfMap.put(word, tfidf);
            }
            
            // 6. 排序并返回前K个关键词
            return tfidfMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("TF-IDF extraction failed", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 计算词频TF
     * @param words 词列表
     * @return TF值映射
     */
    private Map<String, Double> calculateTF(List<String> words) {
        Map<String, Integer> wordCount = new HashMap<>();
        int totalWords = words.size();
        
        // 统计词频
        for (String word : words) {
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }
        
        // 计算TF值
        Map<String, Double> tfMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            double tf = (double) entry.getValue() / totalWords;
            tfMap.put(entry.getKey(), tf);
        }
        
        return tfMap;
    }
    
    /**
     * 计算逆文档频率IDF (简化版本)
     * @param words 词列表
     * @return IDF值映射
     */
    private Map<String, Double> calculateIDF(List<String> words) {
        Map<String, Double> idfMap = new HashMap<>();
        Set<String> uniqueWords = new HashSet<>(words);
        
        // 简化处理：基于词长度和词频计算IDF
        for (String word : uniqueWords) {
            double idf = Math.log((double) words.size() / (1 + countWordFrequency(word, words)));
            idfMap.put(word, idf);
        }
        
        return idfMap;
    }
    
    /**
     * 计算词在文档中的频率
     * @param word 词
     * @param words 词列表
     * @return 频率
     */
    private int countWordFrequency(String word, List<String> words) {
        return (int) words.stream().filter(w -> w.equals(word)).count();
    }
} 