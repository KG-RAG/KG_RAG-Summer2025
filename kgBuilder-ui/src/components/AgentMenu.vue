<template>
  <div class="agent-menu">
    <el-menu
      :default-active="activeIndex"
      class="agent-menu-list"
      @select="handleSelect"
      background-color="#545c64"
      text-color="#fff"
      active-text-color="#ffd04b">
      
      <el-menu-item index="extraction">
        <i class="el-icon-magic-stick"></i>
        <span slot="title">智能抽取</span>
      </el-menu-item>
      
      <el-menu-item index="batch">
        <i class="el-icon-document-copy"></i>
        <span slot="title">批量抽取</span>
      </el-menu-item>
      
      <el-menu-item index="back">
        <i class="el-icon-back"></i>
        <span slot="title">返回主页</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
export default {
  name: 'AgentMenu',
  data() {
    return {
      activeIndex: 'extraction'
    }
  },
  methods: {
    handleSelect(key) {
      switch (key) {
        case 'extraction':
          this.$router.push('/agent/extraction')
          break
        case 'batch':
          this.$router.push('/agent/batch')
          break
        case 'back':
          this.$router.push('/home')
          break
      }
    }
  },
  mounted() {
    // 根据当前路由设置激活菜单项
    const path = this.$route.path
    if (path.includes('/agent/extraction')) {
      this.activeIndex = 'extraction'
    } else if (path.includes('/agent/batch')) {
      this.activeIndex = 'batch'
    }
  }
}
</script>

<style scoped>
.agent-menu {
  position: fixed;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  z-index: 1000;
}

.agent-menu-list {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-menu-item {
  text-align: center;
  padding: 15px 20px;
}

.el-menu-item i {
  margin-right: 8px;
  font-size: 16px;
}
</style> 