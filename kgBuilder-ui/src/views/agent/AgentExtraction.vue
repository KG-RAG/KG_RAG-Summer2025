<template>
  <div class="agent-extraction-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>🤖 Agent智能抽取模块</h2>
      <p class="subtitle">支持多种算法的智能文本抽取工具</p>
    </div>

    <!-- 主要内容区域 -->
    <el-row :gutter="20">
      <!-- 左侧：输入和配置区域 -->
      <el-col :span="12">
        <el-card class="input-card">
          <div slot="header">
            <span>📝 文本输入</span>
          </div>
          
          <!-- 抽取方法选择 -->
          <el-form :model="form" label-width="100px">
            <el-form-item label="抽取方法">
              <el-select v-model="form.method" placeholder="请选择抽取方法" style="width: 100%">
                <el-option label="自动抽取 (推荐)" value="auto"></el-option>
                <el-option label="TF-IDF关键词提取" value="tfidf"></el-option>
                <el-option label="TextRank关键词提取" value="textrank"></el-option>
                <el-option label="HanLP实体识别" value="entities"></el-option>
                <el-option label="混合抽取策略" value="hybrid"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="关键词数量" v-if="form.method !== 'entities'">
              <el-input-number 
                v-model="form.topK" 
                :min="1" 
                :max="50" 
                style="width: 100%">
              </el-input-number>
            </el-form-item>
            
            <el-form-item label="输入文本">
              <el-input
                v-model="form.text"
                type="textarea"
                :rows="8"
                placeholder="请输入要抽取的文本内容..."
                :disabled="loading">
              </el-input>
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleExtract" 
                :loading="loading"
                :disabled="!form.text.trim()">
                🚀 开始抽取
              </el-button>
              <el-button @click="handleClear">清空</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：结果显示区域 -->
      <el-col :span="12">
        <el-card class="result-card">
          <div slot="header">
            <span>📊 抽取结果</span>
            <el-button 
              style="float: right; padding: 3px 0" 
              type="text"
              @click="handleExport"
              v-if="result">
              导出结果
            </el-button>
          </div>
          
          <div v-if="!result" class="empty-result">
            <i class="el-icon-document"></i>
            <p>请输入文本并点击"开始抽取"按钮</p>
          </div>
          
          <div v-else class="result-content">
            <!-- 统计信息 -->
            <el-alert
              :title="`抽取方法: ${result.extractionMethod} | 文本长度: ${result.originalText.length} 字符`"
              type="info"
              :closable="false"
              style="margin-bottom: 20px">
            </el-alert>

            <!-- 关键词结果 -->
            <div v-if="result.keywordsTFIDF && result.keywordsTFIDF.length > 0" class="result-section">
              <h4>🔑 TF-IDF关键词</h4>
              <el-tag 
                v-for="keyword in result.keywordsTFIDF" 
                :key="keyword"
                style="margin: 5px">
                {{ keyword }}
              </el-tag>
            </div>

            <div v-if="result.keywordsTextRank && result.keywordsTextRank.length > 0" class="result-section">
              <h4>📈 TextRank关键词</h4>
              <el-tag 
                v-for="keyword in result.keywordsTextRank" 
                :key="keyword"
                type="success"
                style="margin: 5px">
                {{ keyword }}
              </el-tag>
            </div>

            <!-- 关键句结果 -->
            <div v-if="result.keySentences && result.keySentences.length > 0" class="result-section">
              <h4>💬 关键句</h4>
              <el-card 
                v-for="(sentence, index) in result.keySentences" 
                :key="index"
                shadow="hover"
                style="margin: 10px 0">
                <div>{{ sentence }}</div>
              </el-card>
            </div>

            <!-- 实体结果 -->
            <div v-if="result.entities && result.entities.length > 0" class="result-section">
              <h4>👤 识别实体</h4>
              <el-table :data="result.entities" size="mini" style="width: 100%">
                <el-table-column prop="name" label="实体名称"></el-table-column>
                <el-table-column prop="type" label="实体类型">
                  <template slot-scope="scope">
                    <el-tag :type="getEntityTypeColor(scope.row.type)">
                      {{ scope.row.type }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="confidence" label="置信度">
                  <template slot-scope="scope">
                    <el-progress 
                      :percentage="Math.round(scope.row.confidence * 100)"
                      :color="getConfidenceColor(scope.row.confidence)">
                    </el-progress>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <!-- 关系结果 -->
            <div v-if="result.relations && result.relations.length > 0" class="result-section">
              <h4>🔗 实体关系</h4>
              <el-table :data="result.relations" size="mini" style="width: 100%">
                <el-table-column prop="sourceEntityId" label="源实体"></el-table-column>
                <el-table-column prop="relationType" label="关系类型">
                  <template slot-scope="scope">
                    <el-tag type="warning">{{ scope.row.relationType }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="targetEntityId" label="目标实体"></el-table-column>
                <el-table-column prop="confidence" label="置信度">
                  <template slot-scope="scope">
                    <el-progress 
                      :percentage="Math.round(scope.row.confidence * 100)"
                      :color="getConfidenceColor(scope.row.confidence)">
                    </el-progress>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 示例文本区域 -->
    <el-card style="margin-top: 20px">
      <div slot="header">
        <span>📋 示例文本</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="8" v-for="(example, index) in examples" :key="index">
          <el-card shadow="hover" class="example-card">
            <div slot="header">
              <span>{{ example.title }}</span>
            </div>
            <p class="example-text">{{ example.text }}</p>
            <el-button 
              type="text" 
              @click="loadExample(example.text)"
              style="padding: 0">
              使用此示例
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import agentExtractionApi from '@/api/modules/agentExtractionApi'

export default {
  name: 'AgentExtraction',
  data() {
    return {
      loading: false,
      form: {
        method: 'auto',
        text: '',
        topK: 10
      },
      result: null,
      examples: [
        {
          title: '人物关系示例',
          text: '张三在北京大学计算机科学系工作，他主要研究人工智能和机器学习。李四是清华大学的研究生，正在研究深度学习算法。'
        },
        {
          title: '技术概念示例',
          text: '人工智能是计算机科学的一个分支，它企图了解智能的实质，并生产出一种新的能以人类智能相似的方式做出反应的智能机器。该领域的研究包括机器人、语言识别、图像识别、自然语言处理和专家系统等。'
        },
        {
          title: '长文本示例',
          text: '知识图谱（Knowledge Graph）是人工智能领域的重要技术，它以结构化的方式表示知识，通过实体、属性和关系来描述现实世界中的概念和它们之间的联系。知识图谱可以支持多种智能应用，包括智能问答、推荐系统、搜索引擎优化等。在学术界，知识图谱的研究涉及自然语言处理、机器学习、数据库等多个领域。'
        }
      ]
    }
  },
  methods: {
    /**
     * 处理抽取请求
     */
    async handleExtract() {
      if (!this.form.text.trim()) {
        this.$message.warning('请输入文本内容')
        return
      }

      this.loading = true
      try {
        let response
        const requestData = {
          text: this.form.text.trim()
        }

        if (this.form.method !== 'entities') {
          requestData.topK = this.form.topK
        }

        switch (this.form.method) {
          case 'tfidf':
            response = await agentExtractionApi.extractKeywordsByTFIDF(requestData)
            break
          case 'textrank':
            response = await agentExtractionApi.extractKeywordsByTextRank(requestData)
            break
          case 'entities':
            response = await agentExtractionApi.extractEntities(requestData)
            break
          case 'hybrid':
            response = await agentExtractionApi.hybridExtract(requestData)
            break
          case 'auto':
          default:
            response = await agentExtractionApi.autoExtract(requestData)
            break
        }

        if (response.code === 200) {
          this.result = response.data
          this.$message.success('抽取完成！')
        } else {
          this.$message.error(response.msg || '抽取失败')
        }
      } catch (error) {
        console.error('抽取失败:', error)
        console.error('错误详情:', {
          message: error.message,
          response: error.response,
          status: error.response?.status,
          data: error.response?.data
        })
        
        // 显示具体错误信息
        let errorMessage = '抽取失败，请检查网络连接或联系管理员'
        if (error.response?.data?.msg) {
          errorMessage = error.response.data.msg
        } else if (error.message) {
          errorMessage = error.message
        }
        
        this.$message.error(errorMessage)
      } finally {
        this.loading = false
      }
    },

    /**
     * 清空表单
     */
    handleClear() {
      this.form.text = ''
      this.result = null
    },

    /**
     * 加载示例文本
     */
    loadExample(text) {
      this.form.text = text
    },

    /**
     * 导出结果
     */
    handleExport() {
      if (!this.result) return
      
      const data = {
        timestamp: new Date().toISOString(),
        originalText: this.result.originalText,
        extractionMethod: this.result.extractionMethod,
        keywordsTFIDF: this.result.keywordsTFIDF,
        keywordsTextRank: this.result.keywordsTextRank,
        keySentences: this.result.keySentences,
        entities: this.result.entities,
        relations: this.result.relations
      }

      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `agent-extraction-result-${Date.now()}.json`
      a.click()
      URL.revokeObjectURL(url)
      
      this.$message.success('结果已导出')
    },

    /**
     * 获取实体类型颜色
     */
    getEntityTypeColor(type) {
      const colorMap = {
        'PERSON': 'primary',
        'LOCATION': 'success',
        'ORGANIZATION': 'warning',
        'CONCEPT': 'info'
      }
      return colorMap[type] || 'default'
    },

    /**
     * 获取置信度颜色
     */
    getConfidenceColor(confidence) {
      if (confidence >= 0.8) return '#67C23A'
      if (confidence >= 0.6) return '#E6A23C'
      return '#F56C6C'
    }
  }
}
</script>

<style scoped>
.agent-extraction-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  color: #303133;
  margin-bottom: 10px;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

.input-card, .result-card {
  height: 100%;
}

.empty-result {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-result i {
  font-size: 48px;
  margin-bottom: 20px;
}

.result-content {
  max-height: 600px;
  overflow-y: auto;
}

.result-section {
  margin-bottom: 25px;
}

.result-section h4 {
  color: #303133;
  margin-bottom: 15px;
  font-size: 16px;
}

.example-card {
  margin-bottom: 10px;
}

.example-text {
  color: #606266;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 滚动条样式 */
.result-content::-webkit-scrollbar {
  width: 6px;
}

.result-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.result-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.result-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 