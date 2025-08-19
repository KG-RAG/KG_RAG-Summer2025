package com.warmer.web.util;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.utility.SentencesUtil;
import com.warmer.web.entity.ExtractionEntity;
import com.warmer.web.entity.ExtractionRelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
	
	// 启用NER与自定义词典的分词器
	private Segment segment;
	
	/**
	 * 外置关系短语与同义词映射
	 */
	@Value("${agent.relationPatterns}")
	private Resource relationPatternsResource;
	
	@Value("${agent.predicateSynonyms}")
	private Resource predicateSynonymsResource;
	
	@Value("${agent.minRelationConfidence:0.55}")
	private double minRelationConfidence;
	
	@Value("${agent.maxCharDistance:40}")
	private int maxCharDistance;
	
	private List<String> relationPhrases = new ArrayList<>();
	private Map<String, String> predicateSynonyms = new HashMap<>();
	
	@PostConstruct
	private void loadResources() {
		// 初始化分词器：开启NER与自定义词典
		segment = HanLP
			.newSegment()
			.enableNameRecognize(true)
			.enablePlaceRecognize(true)
			.enableOrganizationRecognize(true)
			.enableCustomDictionary(true)
			.enableOffset(true); // 开启offset，确保term.offset为字符起点
		try {
			if (relationPatternsResource != null && relationPatternsResource.exists()) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(relationPatternsResource.getInputStream(), StandardCharsets.UTF_8))) {
					relationPhrases = br.lines()
						.map(String::trim)
						.filter(s -> !s.isEmpty() && !s.startsWith("#"))
						.collect(Collectors.toList());
				}
			}
		} catch (Exception e) {
			log.warn("加载关系短语词表失败，将使用默认内置关系词: {}", e.getMessage());
		}
		if (relationPhrases.isEmpty()) {
			relationPhrases = Arrays.asList("是","在","有","包含","属于","位于","工作","研究","开发","发明","发现","创建","建立","成立","担任","负责","管理","领导");
		}
		try {
			if (predicateSynonymsResource != null && predicateSynonymsResource.exists()) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(predicateSynonymsResource.getInputStream(), StandardCharsets.UTF_8))) {
					br.lines().forEach(line -> {
						String s = line.trim();
						if (s.isEmpty() || s.startsWith("#")) return;
						int idx = s.indexOf('=');
						if (idx > 0 && idx < s.length() - 1) {
							String k = s.substring(0, idx).trim();
							String v = s.substring(idx + 1).trim();
							if (!k.isEmpty() && !v.isEmpty()) predicateSynonyms.put(k, v);
						}
					});
				}
			}
		} catch (Exception e) {
			log.warn("加载谓语同义映射失败，将不进行归一：{}", e.getMessage());
		}
		log.info("Agent关系短语加载: {} 条，同义映射: {} 条，minConfidence={}, maxCharDistance={}", relationPhrases.size(), predicateSynonyms.size(), minRelationConfidence, maxCharDistance);
	}
	
	/**
	 * 文本中是否包含关系触发短语（含模板“…”的左右片段按顺序出现）
	 */
	public boolean containsRelationTrigger(String text) {
		if (text == null || text.isEmpty()) return false;
		for (String pat : relationPhrases) {
			if (pat.contains("…")) {
				String[] parts = pat.split("…");
				if (parts.length == 2) {
					int leftIdx = text.indexOf(parts[0]);
					if (leftIdx >= 0) {
						int rightIdx = text.indexOf(parts[1], leftIdx + parts[0].length());
						if (rightIdx > leftIdx) return true;
					}
				}
			} else {
				if (text.contains(pat)) return true;
			}
		}
		return false;
	}
	
	/**
	 * 使用HanLP进行命名实体识别
	 * 使用分词offset对齐实体位置
	 */
	public List<ExtractionEntity> extractEntities(String text) {
		try {
			List<ExtractionEntity> entities = new ArrayList<>();
			List<Term> terms = segment.seg(text);
			
			for (int i = 0; i < terms.size(); i++) {
				Term term = terms.get(i);
				String nature = String.valueOf(term.nature);
				
				if (ENTITY_TYPE_MAP.containsKey(nature)) {
					String entityType = ENTITY_TYPE_MAP.get(nature);
					StringBuilder entityName = new StringBuilder(term.word);
					int startOffset = term.offset;
					int endOffset = term.offset + term.word.length();
					
					// 合并连续相同词性
					int j = i + 1;
					while (j < terms.size()) {
						Term next = terms.get(j);
						if (String.valueOf(next.nature).equals(nature) && next.offset == endOffset) {
							entityName.append(next.word);
							endOffset = next.offset + next.word.length();
							j++;
						} else {
							break;
						}
					}
					// 跳过已合并部分
					i = Math.max(i, j - 1);
					
					ExtractionEntity entity = ExtractionEntity.builder()
						.id(generateEntityId())
						.name(entityName.toString())
						.type(entityType)
						.startPosition(startOffset)
						.endPosition(endOffset)
						.confidence(calculateEntityConfidence(entityName.toString(), entityType))
						.properties(new ArrayList<>())
						.build();
					entities.add(entity);
				}
			}
			return entities;
		} catch (Exception e) {
			log.error("HanLP recognition failed", e);
			return new ArrayList<>();
		}
	}
	
	/**
	 * 分句后抽取关系（支持短语匹配+同义归一）
	 */
	public List<ExtractionRelation> extractRelations(String text, List<ExtractionEntity> allEntities) {
		try {
			List<ExtractionRelation> relations = new ArrayList<>();
			// 正确分句
			List<String> sentences = SentencesUtil.toSentenceList(text);
			if (sentences == null || sentences.isEmpty()) {
				sentences = Collections.singletonList(text);
			}
			int baseOffset = 0;
			for (String sentence : sentences) {
				// 在原文中定位句子起始偏移
				int sentStart = text.indexOf(sentence, baseOffset);
				if (sentStart < 0) {
					sentStart = baseOffset; // 回退策略
				}
				int sentEnd = sentStart + sentence.length();
				baseOffset = sentEnd;
				final int sentStartFinal = sentStart;
				final int sentEndFinal = sentEnd;
				
				// 句内分词（启用NER）
				List<Term> terms = segment.seg(sentence);
				// 句内实体：通过位置过滤
				List<ExtractionEntity> sentenceEntities = allEntities.stream()
					.filter(e -> e.getStartPosition() >= sentStartFinal && e.getEndPosition() <= sentEndFinal)
					.collect(Collectors.toList());
				Map<Integer, ExtractionEntity> entityPositionMap = buildEntityPositionMap(sentenceEntities, sentStartFinal);
				String sentenceEntityBrief = sentenceEntities.stream()
					.map(e -> e.getName() + "[" + e.getStartPosition() + "," + e.getEndPosition() + "]")
					.collect(Collectors.joining(", "));
				
				// 在句内查找关系短语
				for (int i = 0; i < terms.size(); i++) {
					Term term = terms.get(i);
					int triggerStart = sentStartFinal + term.offset; // 映射回原文offset
					String matchedPredicate = matchPredicatePhrase(terms, i);
					if (matchedPredicate == null) continue;
					String normalizedPredicate = normalizePredicate(matchedPredicate);
					int triggerEnd = triggerStart + matchedPredicate.length();
					
					log.info("[Relation] 命中谓语='{}' | 触发区间=[{}, {}] | 句子='{}'", normalizedPredicate, triggerStart, triggerEnd, sentence);
					ExtractionEntity source = findNearestEntityByCharDistance(triggerStart, entityPositionMap, -1, sentStartFinal);
					ExtractionEntity target = findNearestEntityByCharDistance(triggerEnd, entityPositionMap, 1, sentStartFinal);
					if (source == null || target == null) {
						log.info("[Relation] 邻接实体查找失败 | 左实体={} 右实体={} | 窗口={} | 句内实体={}",
							source != null ? source.getName() : "null",
							target != null ? target.getName() : "null",
							maxCharDistance,
							sentenceEntityBrief);
						continue;
					}
					
					// 语义过滤（去除主宾同名、类型不合理等）
					if (!isSemanticallyValidRelation(normalizedPredicate, source, target)) {
						log.info("[Relation] 过滤: 语义规则不通过 | 三元组=({},{},{}) | 类型=({},{})",
							source.getName(), normalizedPredicate, target.getName(),
							source.getType(), target.getType());
						continue;
					}
					
					double confidence = calculateRelationConfidence(normalizedPredicate, source, target);
					log.info("[Relation] 候选三元组=({} [{}-{}], {}, {} [{}-{}]) | 置信度={}",
						source.getName(), source.getStartPosition(), source.getEndPosition(),
						normalizedPredicate,
						target.getName(), target.getStartPosition(), target.getEndPosition(),
						confidence);
					if (confidence < minRelationConfidence) {
						log.info("[Relation] 置信度未达阈值 ({} < {})，丢弃", confidence, minRelationConfidence);
						continue;
					}
					
					ExtractionRelation rel = ExtractionRelation.builder()
						.id(generateRelationId())
						.sourceEntityId(source.getId())
						.targetEntityId(target.getId())
						.relationType(normalizedPredicate)
						.description(normalizedPredicate)
						.confidence(confidence)
						.startPosition(triggerStart)
						.endPosition(triggerEnd)
						.build();
					relations.add(rel);
				}
			}
			// 后处理：去重（主语-谓语-宾语-区间）
			relations = deduplicateRelations(relations);
			return relations;
		} catch (Exception e) {
			log.error("HanLP relation extraction failed", e);
			return new ArrayList<>();
		}
	}
	
	private Map<Integer, ExtractionEntity> buildEntityPositionMap(List<ExtractionEntity> entities, int sentenceStartOffset) {
		Map<Integer, ExtractionEntity> map = new HashMap<>();
		for (ExtractionEntity e : entities) {
			for (int pos = e.getStartPosition(); pos < e.getEndPosition(); pos++) {
				map.put(pos - sentenceStartOffset, e); // 句内相对offset映射
			}
		}
		return map;
	}
	
	/**
	 * 在当前位置尝试匹配多词谓语/短语，按词表优先级最长匹配
	 */
	private String matchPredicatePhrase(List<Term> terms, int startIndex) {
		// 简化：优先用当前term的word作为候选，再尝试将相邻term拼接，匹配词表中包含“…”的模板按去掉省略符匹配两端存在性
		String single = terms.get(startIndex).word;
		// 先看单词是否在词表
		if (relationPhrases.contains(single)) return single;
		// 再尝试2-4词拼接
		StringBuilder sb = new StringBuilder(single);
		for (int len = 2; len <= 4 && startIndex + len - 1 < terms.size(); len++) {
			sb.append(terms.get(startIndex + len - 1).word);
			String phrase = sb.toString();
			if (relationPhrases.contains(phrase)) return phrase;
		}
		// 模板（包含…）处理：如果词表项形如“在…工作”，这里只返回“在”和“工作”的包围式匹配，触发词用左端词
		for (String pat : relationPhrases) {
			if (pat.contains("…")) {
				String[] parts = pat.split("…");
				if (parts.length == 2) {
					String left = parts[0];
					String right = parts[1];
					if (single.startsWith(left)) {
						// 向后扫描是否出现right
						for (int j = startIndex + 1; j < Math.min(terms.size(), startIndex + 8); j++) {
							if (terms.get(j).word.startsWith(right)) {
								return left + right; // 归一成无省略号短语
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private String normalizePredicate(String predicate) {
		return predicateSynonyms.getOrDefault(predicate, predicate);
	}
	
	/**
	 * 基于字符距离的最近实体查找
	 */
	private ExtractionEntity findNearestEntityByCharDistance(int triggerCharPos,
	                                                        Map<Integer, ExtractionEntity> entityPositionMap,
	                                                        int direction,
	                                                        int sentenceStartOffset) {
		int distance = 0;
		int step = direction;
		while (distance <= maxCharDistance) {
			int probe = triggerCharPos - sentenceStartOffset + (distance * step);
			ExtractionEntity e = entityPositionMap.get(probe);
			if (e != null) return e;
			distance++;
		}
		return null;
	}
	
	/**
	 * 关系去重
	 */
	private List<ExtractionRelation> deduplicateRelations(List<ExtractionRelation> relations) {
		Map<String, ExtractionRelation> map = new LinkedHashMap<>();
		for (ExtractionRelation r : relations) {
			String key = r.getSourceEntityId() + "|" + r.getRelationType() + "|" + r.getTargetEntityId();
			ExtractionRelation exist = map.get(key);
			if (exist == null || (exist.getConfidence() != null && r.getConfidence() != null && r.getConfidence() > exist.getConfidence())) {
				map.put(key, r);
			}
		}
		return new ArrayList<>(map.values());
	}
	
	/**
	 * 语义校验，过滤“主宾同名”等明显低质量关系
	 */
	private boolean isSemanticallyValidRelation(String predicate, ExtractionEntity source, ExtractionEntity target) {
		String sName = source.getName();
		String tName = target.getName();
		// 仅保留：主宾同名直接过滤
		if (sName != null && sName.equals(tName)) return false;
		return true;
	}
	
	/**
	 * 计算实体置信度
	 */
	private Double calculateEntityConfidence(String entityName, String entityType) {
		double confidence = 0.1; // 注意：你当前的基础值较低，如需更严格可调高到0.7~0.8
		if (entityName.length() >= 2) confidence += 0.1;
		if ("PERSON".equals(entityType) || "LOCATION".equals(entityType)) confidence += 0.1;
		return Math.min(confidence, 1.0);
	}
	
	/**
	 * 计算关系置信度
	 */
	private Double calculateRelationConfidence(String relationWord, ExtractionEntity sourceEntity, ExtractionEntity targetEntity) {
		double confidence = 0.65; // 略微降低基础阈值，提高召回
		if ("是".equals(relationWord) || "在".equals(relationWord)) confidence += 0.15;
		if ("PERSON".equals(sourceEntity.getType()) && "ORGANIZATION".equals(targetEntity.getType())) confidence += 0.1;
		if ("PERSON".equals(sourceEntity.getType()) && ("研究".equals(relationWord) || relationWord.contains("研究"))) confidence += 0.05;
		// 主宾同名强力降权
		if (sourceEntity.getName() != null && sourceEntity.getName().equals(targetEntity.getName())) confidence -= 0.3;
		return Math.min(confidence, 1.0);
	}
	
	private String generateEntityId() {
		return "entity_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
	}
	
	private String generateRelationId() {
		return "relation_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
	}
} 