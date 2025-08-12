<template>
  <div class="batch-extraction-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>📦 批量抽取工具</h2>
      <p class="subtitle">支持批量处理多个文本，提高抽取效率</p>
    </div>

    <!-- 主要内容区域 -->
    <el-row :gutter="20">
      <!-- 左侧：批量输入区域 -->
      <el-col :span="12">
        <el-card class="input-card">
          <div slot="header">
            <span>📝 批量文本输入</span>
          </div>
          
          <!-- 配置选项 -->
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
            
            <el-form-item label="输入方式">
              <el-radio-group v-model="form.inputType">
                <el-radio label="textarea">文本区域</el-radio>
                <el-radio label="file">文件上传</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <!-- 文本区域输入 -->
            <el-form-item v-if="form.inputType === 'textarea'" label="文本列表">
              <el-input
                v-model="form.textList"
                type="textarea"
                :rows="10"
                placeholder="请输入多个文本，每行一个文本..."
                :disabled="loading">
              </el-input>
              <div class="text-count">
                已输入 {{ textCount }} 个文本
              </div>
            </el-form-item>
            
            <!-- 文件上传 -->
            <el-form-item v-if="form.inputType === 'file'" label="文件上传">
              <el-upload
                class="upload-demo"
                drag
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                :file-list="fileList"
                accept=".txt,.csv">
                <i class="el-icon-upload"></i>
                <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                <div class="el-upload__tip" slot="tip">只能上传txt/csv文件</div>
              </el-upload>
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleBatchExtract" 
                :loading="loading"
                :disabled="!hasValidInput">
                🚀 开始批量抽取
              </el-button>
              <el-button @click="handleClear">清空</el-button>
              <el-button @click="handleLoadSample">加载示例</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：进度和结果区域 -->
      <el-col :span="12">
        <el-card class="result-card">
          <div slot="header">
            <span>📊 批量处理结果</span>
            <el-button 
              style="float: right; padding: 3px 0" 
              type="text"
              @click="handleExportAll"
              v-if="batchResults.length > 0">
              导出所有结果
            </el-button>
          </div>
          
          <div v-if="batchResults.length === 0" class="empty-result">
            <i class="el-icon-document"></i>
            <p>请输入文本并点击"开始批量抽取"按钮</p>
          </div>
          
          <div v-else class="batch-result-content">
            <!-- 进度条 -->
            <div v-if="loading" class="progress-section">
              <el-progress 
                :percentage="progressPercentage" 
                :format="progressFormat"
                status="success">
              </el-progress>
              <p class="progress-text">正在处理第 {{ currentIndex + 1 }} 个文本，共 {{ totalCount }} 个</p>
            </div>

            <!-- 统计信息 -->
            <el-alert
              :title="`已处理 ${batchResults.length} 个文本 | 成功 ${successCount} 个 | 失败 ${failCount} 个`"
              :type="failCount > 0 ? 'warning' : 'success'"
              :closable="false"
              style="margin-bottom: 20px">
            </el-alert>

            <!-- 结果列表 -->
            <el-collapse v-model="activeNames">
              <el-collapse-item 
                v-for="(result, index) in batchResults" 
                :key="index"
                :title="`文本 ${index + 1}: ${getTextPreview(result.originalText)}`"
                :name="index">
                <div class="result-item">
                  <el-tag 
                    :type="result.success ? 'success' : 'danger'"
                    style="margin-bottom: 10px">
                    {{ result.success ? '成功' : '失败' }}
                  </el-tag>
                  
                  <div v-if="result.success">
                    <div class="result-summary">
                      <span>方法: {{ result.data.extractionMethod }}</span>
                      <span>关键词: {{ getKeywordCount(result.data) }} 个</span>
                      <span>实体: {{ result.data.entities ? result.data.entities.length : 0 }} 个</span>
                      <span>关系: {{ result.data.relations ? result.data.relations.length : 0 }} 个</span>
                    </div>
                    
                    <!-- 关键词展示 -->
                    <div v-if="getKeywords(result.data).length > 0" class="keywords-section">
                      <h5>关键词:</h5>
                      <el-tag 
                        v-for="keyword in getKeywords(result.data)" 
                        :key="keyword"
                        size="mini"
                        style="margin: 2px">
                        {{ keyword }}
                      </el-tag>
                    </div>
                  </div>
                  
                  <div v-else class="error-message">
                    <el-alert :title="result.message" type="error" :closable="false"></el-alert>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import agentExtractionApi from '@/api/modules/agentExtractionApi'

export default {
  name: 'BatchExtraction',
  data() {
    return {
      loading: false,
      form: {
        method: 'auto',
        topK: 10,
        inputType: 'textarea',
        textList: ''
      },
      fileList: [],
      batchResults: [],
      activeNames: [],
      currentIndex: 0,
      totalCount: 0,
      successCount: 0,
      failCount: 0
    }
  },
  computed: {
    /**
     * 文本数量
     */
    textCount() {
      if (!this.form.textList.trim()) return 0
      return this.form.textList.split('\n').filter(text => text.trim()).length
    },
    
    /**
     * 是否有有效输入
     */
    hasValidInput() {
      if (this.form.inputType === 'textarea') {
        return this.textCount > 0
      } else {
        return this.fileList.length > 0
      }
    },
    
    /**
     * 进度百分比
     */
    progressPercentage() {
      if (this.totalCount === 0) return 0
      return Math.round((this.currentIndex / this.totalCount) * 100)
    }
  },
  methods: {
    /**
     * 处理批量抽取
     */
    async handleBatchExtract() {
      const texts = this.getTextList()
      if (texts.length === 0) {
        this.$message.warning('请输入文本内容')
        return
      }

      this.loading = true
      this.batchResults = []
      this.currentIndex = 0
      this.totalCount = texts.length
      this.successCount = 0
      this.failCount = 0

      for (let i = 0; i < texts.length; i++) {
        this.currentIndex = i
        const text = texts[i].trim()
        
        if (!text) continue

        try {
          let response
          const requestData = { text }

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
            this.batchResults.push({
              originalText: text,
              data: response.data,
              success: true
            })
            this.successCount++
          } else {
            this.batchResults.push({
              originalText: text,
              message: response.msg || '抽取失败',
              success: false
            })
            this.failCount++
          }
        } catch (error) {
          console.error('批量抽取失败:', error)
          this.batchResults.push({
            originalText: text,
            message: '网络错误或服务器异常',
            success: false
          })
          this.failCount++
        }
      }

      this.loading = false
      this.$message.success(`批量抽取完成！成功 ${this.successCount} 个，失败 ${this.failCount} 个`)
    },

    /**
     * 获取文本列表
     */
    getTextList() {
      if (this.form.inputType === 'textarea') {
        return this.form.textList.split('\n').filter(text => text.trim())
      } else {
        // 从文件内容中解析文本
        return this.fileList.map(file => file.content).filter(text => text.trim())
      }
    },

    /**
     * 处理文件上传
     */
    handleFileChange(file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        file.content = e.target.result
        this.fileList = [file]
      }
      reader.readAsText(file.raw)
    },

    /**
     * 清空表单
     */
    handleClear() {
      this.form.textList = ''
      this.fileList = []
      this.batchResults = []
    },

    /**
     * 加载示例
     */
    handleLoadSample() {
      this.form.textList = `张三在北京大学计算机科学系工作，主要研究人工智能和机器学习。
李四是清华大学的研究生，正在研究深度学习算法。
人工智能是计算机科学的一个分支，它企图了解智能的实质。
知识图谱是人工智能的重要技术，支持智能问答、推荐系统等应用。`
    },

    /**
     * 导出所有结果
     */
    handleExportAll() {
      const data = {
        timestamp: new Date().toISOString(),
        method: this.form.method,
        totalCount: this.batchResults.length,
        successCount: this.successCount,
        failCount: this.failCount,
        results: this.batchResults
      }

      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `batch-extraction-result-${Date.now()}.json`
      a.click()
      URL.revokeObjectURL(url)
      
      this.$message.success('所有结果已导出')
    },

    /**
     * 获取文本预览
     */
    getTextPreview(text) {
      return text.length > 30 ? text.substring(0, 30) + '...' : text
    },

    /**
     * 获取关键词
     */
    getKeywords(data) {
      const keywords = []
      if (data.keywordsTFIDF) keywords.push(...data.keywordsTFIDF)
      if (data.keywordsTextRank) keywords.push(...data.keywordsTextRank)
      return [...new Set(keywords)] // 去重
    },

    /**
     * 获取关键词数量
     */
    getKeywordCount(data) {
      return this.getKeywords(data).length
    },

    /**
     * 进度格式化
     */
    progressFormat(percentage) {
      return `${this.currentIndex + 1}/${this.totalCount}`
    }
  }
}
</script>

<style scoped>
.batch-extraction-container {
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

.text-count {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
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

.batch-result-content {
  max-height: 600px;
  overflow-y: auto;
}

.progress-section {
  margin-bottom: 20px;
}

.progress-text {
  text-align: center;
  color: #606266;
  font-size: 14px;
  margin-top: 10px;
}

.result-item {
  padding: 10px 0;
}

.result-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
}

.result-summary span {
  font-size: 12px;
  color: #606266;
  background-color: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
}

.keywords-section {
  margin-top: 10px;
}

.keywords-section h5 {
  margin-bottom: 8px;
  color: #303133;
  font-size: 14px;
}

.error-message {
  margin-top: 10px;
}

/* 滚动条样式 */
.batch-result-content::-webkit-scrollbar {
  width: 6px;
}

.batch-result-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.batch-result-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.batch-result-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style> 