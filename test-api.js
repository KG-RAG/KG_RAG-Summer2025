const axios = require('axios');

// 测试后端API连接
async function testAPI() {
  try {
    console.log('🔍 测试后端API连接...');
    
    // 测试基础连接
    const response = await axios.get('http://localhost:8080/api/agent/methods', {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    console.log('✅ API连接成功:', response.data);
  } catch (error) {
    console.log('❌ API连接失败:', error.message);
    
    if (error.response) {
      console.log('状态码:', error.response.status);
      console.log('响应数据:', error.response.data);
    }
  }
}

testAPI(); 