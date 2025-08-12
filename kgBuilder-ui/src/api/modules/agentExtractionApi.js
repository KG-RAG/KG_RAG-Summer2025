import request from '@/utils/request'

/**
 * Agent智能抽取模块API接口
 */
export default {
  /**
   * TF-IDF关键词提取
   * @param {Object} data - 请求参数
   * @param {string} data.text - 文本内容
   * @param {number} data.topK - 关键词数量
   */
  extractKeywordsByTFIDF(data) {
    return request({
      url: 'http://localhost:8080/api/agent/extract/tfidf',
      method: 'post',
      params: data
    })
  },

  /**
   * TextRank关键词提取
   * @param {Object} data - 请求参数
   * @param {string} data.text - 文本内容
   * @param {number} data.topK - 关键词数量
   */
  extractKeywordsByTextRank(data) {
    return request({
      url: 'http://localhost:8080/api/agent/extract/textrank',
      method: 'post',
      params: data
    })
  },

  /**
   * HanLP实体识别
   * @param {Object} data - 请求参数
   * @param {string} data.text - 文本内容
   */
  extractEntities(data) {
    return request({
      url: 'http://localhost:8080/api/agent/extract/entities',
      method: 'post',
      params: data
    })
  },

  /**
   * 混合抽取策略
   * @param {Object} data - 请求参数
   * @param {string} data.text - 文本内容
   * @param {number} data.topK - 关键词数量
   */
  hybridExtract(data) {
    return request({
      url: 'http://localhost:8080/api/agent/extract/hybrid',
      method: 'post',
      params: data
    })
  },

  /**
   * 自动抽取 (推荐)
   * @param {Object} data - 请求参数
   * @param {string} data.text - 文本内容
   */
  autoExtract(data) {
    return request({
      url: 'http://localhost:8080/api/agent/extract/auto',
      method: 'post',
      params: data
    })
  },

  /**
   * 获取抽取方法列表
   */
  getExtractionMethods() {
    return request({
      url: 'http://localhost:8080/api/agent/methods',
      method: 'get'
    })
  }
} 