<template>
  <div class="register-container">
    <h1>注册页面</h1>
    <form @submit.prevent="register" class="register-form">
      <div class="form-group">
        <label for="userName">用户名:</label>
        <input type="text" id="userName" v-model="userName" required>
      </div>
      <div class="form-group">
        <label for="password">密码:</label>
        <input type="password" id="password" v-model="password" required>
      </div>
      <div class="form-group">
        <label for="role">角色:</label>
        <select id="role" v-model="role">
          <option value="0">学生</option>
          <option value="1">老师</option>
        </select>
      </div>
      <button type="submit">注册</button>
    </form>
    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    <p>已有账号？<router-link to="/">登录</router-link></p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      userName: '',
      password: '',
      role: '0',
      errorMessage: ''
    };
  },
  methods: {
    async register() {
      try {
        const response = await axios.post('/api/register', {
          userName: this.userName,
          password: this.password,
          isTeacher: parseInt(this.role)
        });
        console.log(response.data);
        if (response.data.success === true) {
          // 注册成功，跳转到登录页面
          window.alert('注册成功，请登录！');
          this.$router.push('/');
        } else {
          this.errorMessage = response.data.message;
        }
      } catch (error) {
        this.errorMessage = '注册失败，请稍后重试';
      }
    }
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f4f4f9;
}

.register-form {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 300px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #0056b3;
}

.error-message {
  color: red;
  margin-top: 10px;
}
</style>
