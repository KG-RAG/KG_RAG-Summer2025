
<template>
  <div class="menu">
    <nav class="nav" id="topnav">
      <h1 class="logo"><a href="/">֪KGTeach</a></h1>
      <ul style="float: left;margin-left: 60px;padding: 10px;">
        <template v-for="nav in navList">
          <li @mouseover="selectStyle(nav)" >
            <a
              :href="nav.linkUrl"
              >{{ nav.title }}</a
            >
            <ul class="sub-nav" v-if="nav.childrens" v-show="nav.active" @mouseout="outStyle(nav)">
              <li v-for="children in nav.childrens">
                <a :href="children.linkUrl">{{ children.title }}</a>
              </li>
            </ul>
          </li>
        </template>
      </ul>
    </nav>

  </div>
</template>

<script>
export default {
  props: {
    data: Object
  },
  data() {
    return {
      navList: [
        // {
        //   title: "登录",
        //   icon: "glyphicon glyphicon-cog",
        //   linkUrl: "http://localhost/",
        //   active: false,
        //   childrens: []
        // },
        // {
        //   title: "图谱",
        //   icon: "glyphicon glyphicon-cog",
        //   linkUrl: "http://localhost/home",
        //   active: false,
        //   childrens: []
        // },

      ]
    };
  },
  mounted() {},
  components: {},
  methods: {
    selectStyle(nav) {
      var _this = this;
      this.$nextTick(function() {
        _this.navList.forEach(function(item) {
          item.active = false;
        });
        nav.active = true;
      });
    },
    outStyle(nav) {
      nav.active = false;
    },
    changeIcon() {
      this.isOpen = !this.isOpen;
    },
    searchActive() {
      this.search_active = !this.search_active;
    },
    clickNav(nav) {
      nav.active = !nav.active;
    }
  }
};
</script>
<style>
#topnav_current { color: #00A7EB; }
.menu { height: 76px; width: 100%; background-color: #00b8eb; }
.nav { height: 80px; width: 100%; margin: 0 auto; }
.nav li { float: left; position: relative; }
.nav li a { color: #bdbdbd; padding: 0 10px; display: inline-block; text-decoration:none}
.nav li a:hover { color: #fff; }
.nav li .sub-nav { position: absolute; top: 30px; width: 120px; background: #FFF; left: -20px; /* display: none;  */z-index: 9999}
.nav li .sub-nav li { clear: left; height: 20px; line-height: 35px; position: relative; width: 200px; padding: 5px 20px }
.nav li .sub-nav li a { font-size: 15px; font-weight: 400; color: #404040; line-height: 28px; }
.nav li .sub-nav li a:hover { color: #000; border-left: 2px solid #000; }
.a_active { color: #00A7EB !important;  }
.logo { float: left;margin-left: 70px;width: 260px;font-size: 26px;}
.logo a { color: #1047eb;text-decoration: none; }
.hometitle {
    font-size: 18px;
    color: #5076e8;
    font-weight: 600;
    margin: 0;
    text-transform: uppercase;
    padding-bottom: 15px;
    margin-bottom: 25px;
    position: relative;
}
.hometitle:after {
    content: "";
    background-color: #4991ef;
    left: 0;
    width: 50px;
    height: 2px;
    bottom: 0;
    position: absolute;
    -webkit-transition: 0.5s;
    -moz-transition: 0.5s;
    -ms-transition: 0.5s;
    -o-transition: 0.5s;
    transition: 0.5s;
}
</style>
