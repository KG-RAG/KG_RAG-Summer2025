<template>
  <div id="app">
     <kg-header ref="header"></kg-header>
    <router-view />
  </div>
</template>
<script>
import KgHeader from "@/components/KGHeader";
import login from "./components/login.vue";
export default {
components: {
    KgHeader,
    login,
  }
}
</script>


<style lang="scss">

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
 // text-align: center;
  color: #2c3e50;
}

#nav {
  //padding: 30px;

  a {
    font-weight: bold;
    color: #2c3e50;

    &.router-link-exact-active {
      color: #42b983;
    }
  }
}
body{ margin: 0px; }
</style>
