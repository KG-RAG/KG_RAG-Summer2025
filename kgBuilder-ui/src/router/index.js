import Vue from "vue";
import VueRouter from "vue-router";
import Home from "../views/Home.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "Login",
    component: () => import("../components/login.vue")
  },
  {
    path: "/home",
    name: "Home",
    component: () => import("../views/kgbuilder/index_v1.vue")
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("../components/register.vue")
  },
  // Agent智能抽取模块路由
  {
    path: "/agent/extraction",
    name: "AgentExtraction",
    component: () => import("../views/agent/AgentExtraction.vue")
  },
  {
    path: "/agent/batch",
    name: "BatchExtraction",
    component: () => import("../views/agent/BatchExtraction.vue")
  },
  // {
  //   path: "/builder",
  //   name: "builder",
  //   component: () => import("../views/kgbuilder/index.vue")
  // },
  // {
  //   path: "/kg",
  //   name: "kg",
  //   component: () => import("../views/kgbuilder/index_v1.vue")
  // },
  // {
  //   path: "/er",
  //   name: "er",
  //   component: () => import("../views/erbuilder/index.vue")
  // },
  // {
  //   path: "/ds",
  //   name: "ds",
  //   component: () => import("../views/datasource/index.vue")
  // },
  // {
  //   path: "/icon",
  //   name: "icon",
  //   component: () => import("../views/icon/index.vue")
  // },
  // {
  //   path: "/about",
  //   name: "About",
  //   // route level code-splitting
  //   // this generates a separate chunk (about.[hash].js) for this route
  //   // which is lazy-loaded when the route is visited.
  //   component: () =>
  //     import(/* webpackChunkName: "about" */ "../views/About.vue")
  // }
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes
});

export default router;
