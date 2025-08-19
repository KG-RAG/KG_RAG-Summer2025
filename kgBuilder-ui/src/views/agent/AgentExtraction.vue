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
            <div style="float:right">
              <el-button type="text" @click="handleImport" :disabled="!canImport">导入图谱</el-button>
              <el-button 
                style="padding: 3px 0" 
                type="text"
                @click="handleExport"
                v-if="result">
                导出结果
              </el-button>
            </div>
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

            <!-- 三元组结果 -->
            <div v-if="result.triples && result.triples.length > 0" class="result-section">
              <h4>🧩 三元组</h4>
              <el-table :data="result.triples" size="mini" style="width: 100%">
                <el-table-column prop="subject" label="主语"></el-table-column>
                <el-table-column prop="predicate" label="谓语"></el-table-column>
                <el-table-column prop="object" label="宾语"></el-table-column>
                <el-table-column prop="confidence" label="置信度">
                  <template slot-scope="scope">
                    <el-progress 
                      :percentage="Math.round((scope.row.confidence || 0) * 100)"
                      :color="getConfidenceColor(scope.row.confidence || 0)">
                    </el-progress>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div v-else class="result-section">
              <el-empty description="未识别到三元组"></el-empty>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 导入弹窗 -->
    <el-dialog title="导入图谱" :visible.sync="importVisible" width="480px">
      <el-form :model="importForm" label-width="100px">
        <el-form-item label="领域/图谱名">
          <el-input v-model="importForm.domain" placeholder="例如: 教育图谱"/>
        </el-form-item>
        <el-form-item label="来源标记">
          <el-input v-model="importForm.source" placeholder="默认 agent"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="importVisible=false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="confirmImport">确定导入</el-button>
      </div>
    </el-dialog>

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
      importing: false,
      importVisible: false,
      importForm: {
        domain: '',
        source: 'agent'
      },
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
  computed: {
    canImport() {
      return this.result && Array.isArray(this.result.triples) && this.result.triples.length > 0
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
        let errorMessage = error.response?.data?.msg || error.message || '抽取失败，请检查网络连接或联系管理员'
        this.$message.error(errorMessage)
      } finally {
        this.loading = false
      }
    },

    handleClear() {
      this.form.text = ''
      this.result = null
    },

    loadExample(text) {
      this.form.text = text
    },

    handleExport() {
      if (!this.result) return
      const data = {
        timestamp: new Date().toISOString(),
        originalText: this.result.originalText,
        extractionMethod: this.result.extractionMethod,
        triples: this.result.triples || []
      }
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `agent-extraction-triples-${Date.now()}.json`
      a.click()
      URL.revokeObjectURL(url)
      this.$message.success('三元组已导出')
    },

    handleImport() {
      if (!this.canImport) {
        this.$message.warning('暂无可导入的三元组')
        return
      }
      this.importVisible = true
    },

    async confirmImport() {
      if (!this.importForm.domain.trim()) {
        this.$message.warning('请填写领域/图谱名')
        return
      }
      this.importing = true
      try {
        const payload = {
          domain: this.importForm.domain.trim(),
          source: this.importForm.source || 'agent',
          triples: (this.result.triples || []).map(t => ({
            subject: t.subject,
            predicate: t.predicate,
            object: t.object,
            confidence: t.confidence,
            subjectType: t.subjectType,
            objectType: t.objectType
          }))
        }
        const res = await agentExtractionApi.importTriples(payload)
        if (res.code === 200) {
          this.$message.success('导入成功')
          this.importVisible = false
        } else {
          this.$message.error(res.msg || '导入失败')
        }
      } catch (e) {
        console.error('导入失败', e)
        this.$message.error(e.response?.data?.msg || e.message || '导入失败')
      } finally {
        this.importing = false
      }
    },

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