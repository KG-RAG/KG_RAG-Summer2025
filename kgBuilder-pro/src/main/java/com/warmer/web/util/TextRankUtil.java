package com.warmer.web.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TextRank算法工具类
 * 用于关键词和关键句提取
 */
@Slf4j
@Component
public class TextRankUtil {
    
    /**
     * 停用词集合
     */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这"
    ));
    
    /**
     * 词性过滤集合
     */
    private static final Set<String> VALID_POS = new HashSet<>(Arrays.asList(
        "n", "nr", "ns", "nt", "nz", "v", "vn", "a", "an"
    ));
    
    /**
     * 最大迭代次数
     */
    private static final int MAX_ITERATIONS = 10;
    
    /**
     * 收敛阈值
     */
    private static final double CONVERGENCE_THRESHOLD = 0.5;
    
    /**
     * 阻尼系数
     */
    private static final double DAMPING_FACTOR = 0.85;
    
    /**
     * 使用TextRank算法提取关键词
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
            
            // 3. 构建词汇图
            Map<String, Set<String>> graph = buildWordGraph(filteredWords);
            
            // 4. 计算TextRank分数
            Map<String, Double> scores = calculateTextRank(graph);
            
            // 5. 排序并返回前K个关键词
            return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("TextRank关键词提取失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 提取关键句
     * @param text 输入文本
     * @param topK 返回前K个关键句
     * @return 关键句列表
     */
    public List<String> extractKeySentences(String text, int topK) {
        try {
            // 1. 分句
            List<String> sentences = HanLP.extractSummary(text, topK);
            
            // 2. 构建句子图
            Map<String, Set<String>> graph = buildSentenceGraph(sentences);
            
            // 3. 计算TextRank分数
            Map<String, Double> scores = calculateTextRank(graph);
            
            // 4. 排序并返回关键句
            return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("TextRank key sentence extraction failed", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 构建词汇图
     * @param words 词汇列表
     * @return 词汇图
     */
    private Map<String, Set<String>> buildWordGraph(List<String> words) {
        Map<String, Set<String>> graph = new HashMap<>();
        int windowSize = 5; // 窗口大小
        
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (!graph.containsKey(word)) {
                graph.put(word, new HashSet<>());
            }
            
            // 在窗口内建立连接
            for (int j = Math.max(0, i - windowSize); j < Math.min(words.size(), i + windowSize + 1); j++) {
                if (i != j) {
                    String neighbor = words.get(j);
                    graph.get(word).add(neighbor);
                    if (!graph.containsKey(neighbor)) {
                        graph.put(neighbor, new HashSet<>());
                    }
                    graph.get(neighbor).add(word);
                }
            }
        }
        
        return graph;
    }
    
    /**
     * 构建句子图
     * @param sentences 句子列表
     * @return 句子图
     */
    private Map<String, Set<String>> buildSentenceGraph(List<String> sentences) {
        Map<String, Set<String>> graph = new HashMap<>();
        
        for (int i = 0; i < sentences.size(); i++) {
            String sentence1 = sentences.get(i);
            if (!graph.containsKey(sentence1)) {
                graph.put(sentence1, new HashSet<>());
            }
            
            for (int j = 0; j < sentences.size(); j++) {
                if (i != j) {
                    String sentence2 = sentences.get(j);
                    double similarity = calculateSentenceSimilarity(sentence1, sentence2);
                    if (similarity > 0.05) { // 相似度阈值
                        graph.get(sentence1).add(sentence2);
                    }
                }
            }
        }
        
        return graph;
    }
    
    /**
     * 计算句子相似度
     * @param sentence1 句子1
     * @param sentence2 句子2
     * @return 相似度
     */
    private double calculateSentenceSimilarity(String sentence1, String sentence2) {
        List<Term> terms1 = HanLP.segment(sentence1);
        List<Term> terms2 = HanLP.segment(sentence2);
        
        Set<String> words1 = terms1.stream()
            .map(term -> term.word)
            .collect(Collectors.toSet());
        Set<String> words2 = terms2.stream()
            .map(term -> term.word)
            .collect(Collectors.toSet());
        
        // 计算Jaccard相似度
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * 计算TextRank分数
     * @param graph 图
     * @return 分数映射
     */
    private Map<String, Double> calculateTextRank(Map<String, Set<String>> graph) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Double> newScores = new HashMap<>();
        
        // 初始化分数
        for (String node : graph.keySet()) {
            scores.put(node, 1.0);
        }
        
        // 迭代计算
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            double maxDiff = 0.0;
            
            for (String node : graph.keySet()) {
                double score = 1 - DAMPING_FACTOR;
                
                for (String neighbor : graph.get(node)) {
                    if (graph.containsKey(neighbor) && !graph.get(neighbor).isEmpty()) {
                        score += DAMPING_FACTOR * scores.get(neighbor) / graph.get(neighbor).size();
                    }
                }
                
                newScores.put(node, score);
                maxDiff = Math.max(maxDiff, Math.abs(score - scores.get(node)));
            }
            
            // 更新分数
            scores.putAll(newScores);
            
            // 检查收敛
            if (maxDiff < CONVERGENCE_THRESHOLD) {
                break;
            }
        }
        
        return scores;
    }
} 