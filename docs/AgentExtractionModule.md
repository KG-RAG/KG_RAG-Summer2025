# Agent智能抽取模块使用文档

## 📋 模块概述

Agent智能抽取模块是Neo4j知识图谱构建系统的核心组件，提供了多种文本抽取算法，包括TF-IDF、TextRank、HanLP命名实体识别等，支持自动选择最适合的抽取策略。

## 🏗️ 架构设计

### 核心组件

1. **实体类 (Entity)**
   - `ExtractionEntity`: 抽取实体类
   - `ExtractionRelation`: 抽取关系类
   - `ExtractionResult`: 抽取结果类

2. **算法工具类 (Util)**
   - `TFIDFUtil`: TF-IDF关键词提取算法
   - `TextRankUtil`: TextRank关键词和关键句提取算法
   - `HanLPUtil`: HanLP命名实体识别和关系抽取

3. **服务层 (Service)**
   - `AgentExtractionService`: 抽取服务接口
   - `AgentExtractionServiceImpl`: 抽取服务实现

4. **控制器层 (Controller)**
   - `AgentExtractionController`: REST API接口

## 🚀 快速开始

### 1. 环境准备

确保项目中已添加HanLP依赖：

```xml
<dependency>
    <groupId>com.hankcs</groupId>
    <artifactId>hanlp</artifactId>
    <version>portable-1.7.8</version>
</dependency>
```

### 2. 配置HanLP

在`application-dev.yml`中配置HanLP路径：

```yaml
hanlp:
  root: D:/HanLP/
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

## 📡 API接口

### 1. TF-IDF关键词提取

**接口**: `POST /api/agent/extract/tfidf`

**请求参数**:
```json
{
  "text": "要提取关键词的文本内容",
  "topK": 10
}
```

### 2. TextRank关键词提取

**接口**: `POST /api/agent/extract/textrank`

### 3. HanLP实体识别

**接口**: `POST /api/agent/extract/entities`

### 4. 混合抽取策略

**接口**: `POST /api/agent/extract/hybrid`

### 5. 自动抽取

**接口**: `POST /api/agent/extract/auto`

## 🔧 使用示例

### Java代码示例

```java
@Autowired
private AgentExtractionService agentExtractionService;

// TF-IDF关键词提取
ExtractionResult result = agentExtractionService.extractKeywordsByTFIDF(text, 10);
List<String> keywords = result.getKeywordsTFIDF();

// TextRank关键词和关键句提取
ExtractionResult result = agentExtractionService.extractKeywordsByTextRank(text, 10);
List<String> keywords = result.getKeywordsTextRank();
List<String> sentences = result.getKeySentences();

// HanLP实体识别
ExtractionResult result = agentExtractionService.extractEntities(text);
List<ExtractionEntity> entities = result.getEntities();
List<ExtractionRelation> relations = result.getRelations();

// 混合抽取
ExtractionResult result = agentExtractionService.hybridExtract(text, 10);

// 自动抽取
ExtractionResult result = agentExtractionService.autoExtract(text);
```

## 🧪 测试

运行测试类：

```bash
mvn test -Dtest=AgentExtractionServiceTest
```

## 📊 算法说明

### 1. TF-IDF算法

**原理**: 基于词频-逆文档频率的关键词提取算法
- **TF (Term Frequency)**: 词在文档中出现的频率
- **IDF (Inverse Document Frequency)**: 词的逆文档频率

**适用场景**: 短文本关键词提取

### 2. TextRank算法

**原理**: 基于图排序的关键词和关键句提取算法
- 将词汇或句子作为图的节点
- 根据共现关系建立边
- 使用PageRank算法计算重要性

**适用场景**: 长文本关键词和关键句提取

### 3. HanLP命名实体识别

**原理**: 基于HanLP的命名实体识别和关系抽取
- 支持人名、地名、机构名等实体识别
- 基于规则和统计的关系抽取

**适用场景**: 实体和关系抽取

### 4. 混合抽取策略

**原理**: 结合多种算法的混合抽取策略
- 根据文本长度自动选择算法
- 融合多种算法的结果

**适用场景**: 通用文本抽取

### 5. 自动抽取策略

**原理**: 根据文本特征自动选择最适合的抽取策略
- 短文本 (<100字符): TF-IDF
- 中等文本 (100-500字符): TextRank
- 长文本 (500-2000字符): HanLP
- 超长文本 (>2000字符): 混合策略

## 🔍 故障排除

### 常见问题

1. **HanLP依赖问题**
   - 确保HanLP依赖已正确添加
   - 检查HanLP配置文件路径

2. **内存不足**
   - 增加JVM内存参数: `-Xmx2g`
   - 优化文本长度限制

3. **抽取结果为空**
   - 检查输入文本是否为空
   - 确认文本包含有效内容

## 📈 性能优化

### 1. 缓存策略

- 对相同文本的抽取结果进行缓存
- 使用Redis缓存热点数据

### 2. 并发处理

- 使用线程池处理并发请求
- 异步处理大量文本抽取

### 3. 算法优化

- 优化停用词过滤
- 改进实体识别准确率
- 提升关系抽取效果

## 🔮 未来扩展

### 1. 深度学习集成

- 集成BERT等预训练模型
- 实现更准确的实体识别
- 支持复杂关系抽取

### 2. 多语言支持

- 扩展支持英文等其他语言
- 实现跨语言实体识别

### 3. 实时学习

- 支持在线学习用户反馈
- 动态调整抽取策略 