
<template>
  <div class="mind-box">
    <!-- 左侧 -->
    <el-scrollbar class="mind-l">
      <div class="ml-m">
        <div class="guanzhu" style="padding: 20px;">
          <h2 class="hometitle ml-ht">图谱列表</h2>
          <div class="ml-a-box" style="min-height:280px">
            <el-tag
              class="tag-ml-5"
              @click="createDomain"
              >新建图谱</el-tag
            >
            <el-tag
              class="tag-ml-5"
              @click="updateDomain"
            >
              修改图谱
            </el-tag>

            <el-tag
              class="tag-ml-5"
              @click="deleteDomain"
              >删除图谱</el-tag
            >

            <!-- Agent智能抽取模块入口 -->
            <el-tag
              class="tag-ml-5"
              type="success"
              @click="goToAgentExtraction"
              style="background-color: #67C23A; border-color: #67C23A;"
            >
              🤖 智能抽取
            </el-tag>

            <el-tag
              class="tag-ml-5"
              type="warning"
              @click="goToBatchExtraction"
              style="background-color: #E6A23C; border-color: #E6A23C;"
            >
              📦 批量抽取
            </el-tag>

            <el-tag
              @click="matchDomainGraph(m)"
              v-for="(m, index) in pageModel.nodeList"
              :key="index"
              :type="m.type"
              effect="dark"
              :title="m.name"
              class="tag-ml-5"
            >
              {{ m.name }}
            </el-tag>
          </div>
           <div class="fr">
            <a
              href="javascript:void(0)"
              class="svg-a-sm"
              v-show="pageModel.pageIndex > 1"
              @click="prev"
              >上一页</a
            >
            <a
              href="javascript:void(0)"
              class="svg-a-sm"
              v-show="pageModel.pageIndex < pageModel.totalPage"
              @click="next"
              >下一页</a
            >
          </div>
        </div>
      </div>
    </el-scrollbar>
    <!-- 左侧over -->
    <!-- 右侧 -->
    <div class="mind-con">
      <!-- 头部工具栏 -->
      <div class="mind-top clearfix">
        <span>
          <span class="dibmr">
            <span>当前领域:</span>
            <span style="color:blue">{{ domainAlia }}</span>
          </span>
        </span>
        <div v-show="domain != ''" class="fl" style="display: flex">
          <div class="search">
            <el-button @click="getDomainGraph(0)">
              <svg class="icon" aria-hidden="true">
                <use xlink:href="#icon-search"></use>
              </svg>
            </el-button>
            <el-input
              placeholder="请输入关键词"
              v-model="nodeName"
              @keyup.enter.native="getDomainGraph"
            ></el-input>
          </div>
          <span class="dibmr">显示节点个数:</span>
            <span class="dibmr">

              <el-tag
                v-for="(m, index) in pageSizeList"
                size="mini"
                :key="index"  :type="m.isActive?'success':''"
                class="tag-ml-5"
                @click="setMatchSize(m)"
                >{{ m.size }}</el-tag
              >

            </span>

        </div>
        <div class="fr">
          <a href="javascript:void(0)" @click="showJsonData" class="svg-a-sm">
            <i class="el-icon-tickets">查看数据</i>
          </a>

          <!-- <a href="javascript:void(0)" @click="saveImage" class="svg-a-sm">
            <i class="el-icon-camera-solid">截图</i>
          </a> -->

          <a href="javascript:void(0)" @click="importGraph" class="svg-a-sm">
            <i class="el-icon-upload">导入</i>
          </a>
          <a href="javascript:void(0)" @click="exportGraph" class="svg-a-sm">
            <i class="el-icon-download">导出</i>
          </a>
          <!-- <a
            href="javascript:void(0)"
            @click="requestFullScreen"
            class="svg-a-sm"
          >
            <i class="el-icon-monitor">全屏</i>
          </a> -->
          <a href="javascript:void(0)" @click="help" class="svg-a-sm">
            <i class="el-icon-info">帮助</i>
          </a>
          <!-- <a href="javascript:void(0)" @click="wanted" class="svg-a-sm">
            <i class="el-icon-question">反馈</i>
          </a> -->
        </div>
      </div>
      <!-- 头部over -->
      <!-- 中部 -->
      <el-scrollbar class="mind-cen" id="graphcontainerdiv">
        <div id="nodeDetail" class="node_detail">
          <h5>详细数据</h5>
          <span class="node_pd" v-for="(m, k) in nodeDetail" :key="k"
            >{{ k }}:{{ m }}</span
          >
        </div>
        <!-- 中部图谱画布 -->
        <div id="graphContainer" class="graphContainer">
          <kgbuilder
            ref="kg_builder"
            :styles="style"
            :initData="graphData"
            :domain="domain"
            :domainId="domainId"
            :ring-function="RingFunction"
            @editForm="editForm"
          />
        </div>
      </el-scrollbar>
      <!-- 中部over -->
      <div class="svg-set-box"></div>
      <!-- 底部 -->

      <!-- 底部over -->
    </div>
    <!-- 右侧over -->
    <!--编辑窗口-->
    <div>
      <kg-form
        ref="kg_form"
        @batchCreateNode="batchCreateNode"
        @batchCreateChildNode="batchCreateChildNode"
        @batchCreateSameNode="batchCreateSameNode"
        @createNode="createNode"
        @initNodeImage="initNodeImage"
        @initNodeContent="initNodeContent"
        @saveNodeImage="saveNodeImage"
        @saveNodeContent="saveNodeContent"
        @getDomain="getDomain"
      >
      </kg-form>
    </div>
    <!-- 富文本展示 -->
    <div>
      <node-richer ref="node_richer"></node-richer>
    </div>
    <div>
      <kg-json ref="kg_json" :data="graphData"></kg-json>
    </div>
    <div>
      <kg-help ref="kg_help"></kg-help>
    </div>
    <div>
      <kg-wanted ref="kg_wanted"></kg-wanted>
    </div>
  </div>
</template>
<script>
import _ from "lodash";
import { kgBuilderApi } from "@/api";
import KgForm from "@/views/kgbuilder/components/kg_form";
import NodeRicher from "@/views/kgbuilder/components/node_richer";
import KgFocus from "@/components/KGFocus";
import KgWanted from "@/components/KGWanted";
import KgJson from "@/views/kgbuilder/components/kg_json";
import KgHelp from "@/views/kgbuilder/components/kg_help";
import html2canvas from "html2canvas";
import kgbuilder from "@/components/KGBuilder_v1";
import { EventBus } from "@/utils/event-bus.js";

import { getIsTeacher } from "../../config";

// import KgHeader from "@/components/KGHeader";
export default {
  name: "kgBuilderv1",
  components: {
    KgForm,
    NodeRicher,
    KgFocus,
    KgJson,
    KgHelp,
    KgWanted,
    kgbuilder,
    // KgHeader
  },
  provide() {
    return {
      _thisKey: this._thisKey,
      Dset: this.Dset,
      updateLinkName: this.updateLinkName,
      editLinkName: this.editLinkName,
      deleteLinkName: this.deleteLinkName,
      quickAddNodes: this.btnQuickAddNode,
      createSingleNode: this.createSingleNode,
      updateCoordinateOfNode: this.updateCoordinateOfNode,
      getNodeDetail: this.getNodeDetail
    };
  },
  data() {
    return {
      style: null,
      width: null,
      height: null,
      RingFunction: [
        {
          title: "新建",
          icon: {
            type: "icon",
            content: "#icon-add"
          },
          defaultEvent: (d, _this, d3) => {
            console.log("level2Group");
            this.$refs.kg_form.initBatchAddChild(
                  true,
                  "batchAddChild",
                  d,
                  this.domain
                );
          },

        },
        {
          title: "编辑",
          icon: {
            type: "icon",
            content: "#icon-editor"
          },
          defaultEvent: (d, _this, d3) => {
            _this.$nextTick(() => {
              let formNode = {
                uuid: d.uuid,
                name: d.name,
                r: parseInt(d.r),
                color: d.color
              };
              _this.$emit(
                "editForm",
                true,
                "nodeEdit",
                formNode,
                _this.domainId
              );
            });
          },
          childrens: []
        },
        {
          title: "展开",
          icon: {
            type: "icon",
            content: "#icon-salescenter-fill"
          },
          defaultEvent: (d, _this, d3) => {
            let data = { domain: _this.domain, nodeId: d.uuid };
            kgBuilderApi.getMoreRelationNode(data).then(result => {
              if (result.code == 200) {
                //把不存在于画布的节点添加到画布
                this.mergeNodeAndLink(
                  result.data.node,
                  result.data.relationship
                );

                //重新绘制
                //_this.updateGraph();
              } else {
                _this.$message.error("展开失败 :" + item.executionTime);
              }
            });
          },
          childrens: []
        },
        {
          title: "删除",
          icon: {
            type: "icon",
            content: "#icon-ashbin-fill"
          },
          defaultEvent: (d, _this, d3) => {
            if(!getIsTeacher()){
              _this.$message({
                message: "您没有权限删除节点",
                type: "warning"
              });
              return;
            }
            let data = { domain: _this.domain, nodeId: d.uuid };
            kgBuilderApi.deleteNode(data).then(result => {
              if (result.code == 200) {
                //let rShips = result.data;
                // 删除节点对应的关系
                for (let i = 0; i < _this.graph.links.length; i++) {
                  if (_this.graph.links[i].uuid == d.uuid) {
                    _this.graph.links.splice(i, 1);
                    i = i - 1;
                  }
                }
                // 找到对应的节点索引
                let j = -1;
                for (let i = 0; i < _this.graph.nodes.length; i++) {
                  if (_this.graph.nodes[i].uuid == d.uuid) {
                    j = i;
                    break;
                  }
                }
                if (j >= 0) {
                  _this.graph.nodes.splice(j, 1); // 根据索引删除该节点
                  _this.updateGraph();
                  _this.$message.success("操作成功!");
                }
              }
            });
          },
          childrens: []
        },
        {
          title: "连线",
          icon: {
            type: "icon",
            content: "#icon-link"
          },
          defaultEvent: (data, _this, d3) => {
            if(!getIsTeacher()){
              _this.$message({
                message: "您没有权限创建关系",
                type: "warning"
              });
              return;
            }
            this.createLink(data);
            //_this.updateGraph();
          },
          childrens: []
        },

      ],
      _thisView: null,
      timer: null,
      tooltip: null,
      nodeDetail: null,
      pageSizeList: [
        { size: 10, isActive: false },
        { size: 30, isActive: false },
        { size: 100, isActive: false },
        { size: 300, isActive: false },
        { size: 500, isActive: false },
         { size: 1000, isActive: true }
      ],
      domain: "",
      domainId: 0,
      domainAlia:"",
      nodeName: "",
      pageSize: 500,
      activeNode: null,
      nodeImageList: [],
      showImageList: [],
      editorContent: "",
      pageModel: {
        pageIndex: 1,
        pageSize: 30,
        totalCount: 0,
        totalPage: 0,
        nodeList: []
      },
      graphData: {
        nodes: [],
        links: []
      },
      jsonShow: false,
      helpShow: false
    };
  },
  filters: {
    labelFormat: function(value) {
      let domain = value.substring(1, value.length - 1);
      return domain;
    }
  },
  mounted() {},
  created() {
    console.log("index_v1"+getIsTeacher());
    this.getDomain();
    this.$nextTick(() => {
      this.width = document.getElementsByClassName(
        "graphContainer"
      )[0].offsetWidth;
      //this.height = document.getElementsByClassName('graphContainer')[0].offsetHeight
      this.height = window.screen.height;
      this.style = {
        width: this.width + "px",
        height: this.height + "px"
      };
      //console.log(this.width, this.height)
      EventBus.$emit("DIV", this.width, this.height);
    });
  },
  methods: {
    _thisKey(item) {
      this._thisView = item;
    },
    Dset(item) {
      this.d3 = item;
    },
    prev() {
      if (this.pageModel.pageIndex > 1) {
        this.pageModel.pageIndex--;
        this.getDomain();
      }
    },
    next() {
      if (this.pageModel.pageIndex < this.pageModel.totalPage) {
        this.pageModel.pageIndex++;
        this.getDomain();
      }
    },
    editForm(flag, action, data, domainId) {
      this.$refs.kg_form.initNode(flag, action, data, domainId);
    },
    //创建节点
    createNode(graphNode) {
      if(getIsTeacher() == false){
        this.$message({
          message: '您不是老师，没有权限',
          type: 'warning'
        });
        return;
      }
      let data = graphNode;
      data.domain = this.domain;
      let _this = this;
      kgBuilderApi.createNode(data).then(result => {
        if (result.code == 200) {
          //删除旧节点，由于我们改变的是属性，不是uuid,此处我们需要更新属性，或者删除节点重新添加
          let newNode = result.data;
          for (let i = 0; i < _this.graphData.nodes.length; i++) {
            if (_this.graphData.nodes[i].uuid == graphNode.uuid) {
              _this.graphData.nodes.splice(i, 1);
            }
          }
          _this.graphData.nodes.push(newNode);
        }
      });
    },
    saveNodeImage(data) {
      if(getIsTeacher() == false){
        this.$message({
          message: '您不是老师，没有权限',
          type: 'warning'
        });
        return;
      }
      let image = data.imagePath;
      let nodeId = data.nodeId;
      let _this = this;
      // console.log("####"+image);
      kgBuilderApi.saveNodeImage(JSON.stringify(data)).then(result => {
        if (result.code == 200) {
          _this.graphData.nodes
            .filter(n => n.uuid == nodeId)
            .map(m => {
              m.image = image;
              return m;
            });
          _this.$message({
            message: "操作成功",
            type: "success"
          });
        }
      });
    },
    //上传富文本
    saveNodeContent(data) {
      if(getIsTeacher() == false){
        this.$message({
          message: '您不是老师，没有权限',
          type: 'warning'
        });
        return;
      }
      kgBuilderApi.saveNodeContent(JSON.stringify(data)).then(result => {
        if (result.code == 200) {
          this.$message({ message: "操作成功", type: "success" });
        }
      });
    },
    //画布直接添加节点
    createSingleNode(left, top) {
      if(getIsTeacher() == false){
        this.$message({
          message: '您不是老师，没有权限',
          type: 'warning'
        });
        return;
      }
      let data = { name: "", r: 30 };
      data.domain = this.domain;
      kgBuilderApi.createNode(data).then(result => {
        if (result.code == 200) {
          let newNode = result.data;
          _.assignIn(newNode, {
            x: left,
            y: top,
            fx: left,
            fy: top,
            r: parseInt(newNode.r),
            image: ""
          });
          this.graphData.nodes.push(newNode);
        }
      });
    },
    updateCoordinateOfNode(nodes) {
      // if(getIsTeacher() == false){
      //   this.$message({
      //     message: '您不是老师，没有权限3',
      //     type: 'warning'
      //   });
      //   return;
      // }
      let data = { domain: this.domain, nodes: nodes };
      kgBuilderApi.updateCoordinateOfNode(data).then(result => {});
    },
    //删除节点
    deleteNode(out_buttongroup_id) {
      let _this = this;
      _this
        .$confirm(
          "此操作将删除该节点及周边关系(不可恢复), 是否继续?",
          "三思而后行",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning"
          }
        )
        .then(function() {
          let data = { domain: _this.domain, nodeId: _this.selectNode.nodeId };
          kgBuilderApi.deleteNode(data).then(result => {
            if (result.code == 200) {
              _this.svg.selectAll(out_buttongroup_id).remove();
              let rShips = result.data;
              // 删除节点对应的关系
              for (let m = 0; m < rShips.length; m++) {
                for (let i = 0; i < _this.graphData.links.length; i++) {
                  if (_this.graphData.links[i].uuid == rShips[m].uuid) {
                    _this.graphData.links.splice(i, 1);
                    i = i - 1;
                  }
                }
              }
              // 找到对应的节点索引
              let j = -1;
              for (let i = 0; i < _this.graphData.nodes.length; i++) {
                if (_this.graphData.nodes[i].uuid == _this.selectNode.nodeId) {
                  j = i;
                  break;
                }
              }
              if (j >= 0) {
                _this.selectNode.nodeId = 0;
                _this.graphData.nodes.splice(j, 1); // 根据索引删除该节点
                //_this.updateGraph();
                _this.$message({
                  type: "success",
                  message: "操作成功!"
                });
              }
            }
          });
        })
        .catch(function() {
          _this.$message({
            type: "info",
            message: "已取消删除"
          });
        });
    },
    //删除连线
    deleteLinkName(sdata) {
      if(getIsTeacher() == false){
        this.$message({
          message: '您不是老师，没有权限',
          type: 'warning'
        });
        return;
      }
      let _this = this;
      _this
        .$confirm("此操作将删除该关系(不可恢复), 是否继续?", "三思而后行", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        })
        .then(function() {
          let data = { domain: _this.domain, shipId: sdata.uuid };
          kgBuilderApi.deleteLink(data).then(result => {
            if (result.code == 200) {
              let j = -1;
              for (let i = 0; i < _this.graphData.links.length; i++) {
                if (_this.graphData.links[i].uuid == sdata.uuid) {
                  j = i;
                  break;
                }
              }
              if (j >= 0) {
                _this.graphData.links.splice(j, 1);
              }
            }
          });
        })
        .catch(function() {
          _this.$message({
            type: "info",
            message: "已取消删除"
          });
        });
    },
    //添加连线
    createLink(data) {
      if(!getIsTeacher()){
        this.$message({
          message: '您不是老师，没有权限',
          type: 'warning'
        });
        return;
      }
      kgBuilderApi.createLink(data).then(result => {
        if (result.code == 200) {
          let newShip = result.data;
          this.graphData.links.push(newShip);
        }
      });
    },
    //更新连线名称
    updateLinkName(sdata) {
      if(!getIsTeacher()){
        this.$message({
          message: '您不是老师，没有权限',
          type: 'warning'
        });
        return;
      }
      let _this = this;
      this.$prompt("请输入关系名称", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputValue: sdata.cname
      })
        .then(function(res) {
          let value = res.value;
          let data = {
            domain: _this.domain,
            shipId: sdata.uuid,
            shipName: value
          };
          kgBuilderApi.updateLink(data).then(result => {
            if (result.code == 200) {
              let newShip = result.data;
              _this.graphData.links.forEach(function(m) {
                if (m.uuid == newShip.uuid) {
                  m.name = newShip.name;
                }
              });
            }
          });
        })
        .catch(function() {});
    },
    //更新节点名称
    updateNodeName(d) {
      let _this = this;
      _this
        .$prompt("编辑节点名称", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          inputValue: d.name
        })
        .then(function(res) {
          let value = res.value;
          let data = { domain: _this.domain, nodeId: d.uuid, nodeName: value };
          kgBuilderApi.updateNodeName(data).then(result => {
            if (result.code == 200) {
              if (d.uuid != 0) {
                for (let i = 0; i < _this.graphData.nodes.length; i++) {
                  if (_this.graphData.nodes[i].uuid == d.uuid) {
                    _this.graphData.nodes[i].name = value;
                  }
                }
              }
              //_this.updateGraph();
              _this.$message({
                message: "操作成功",
                type: "success"
              });
            }
          });
        })
        .catch(function() {
          _this.$message({
            type: "info",
            message: "取消操作"
          });
        });
    },
    //初始化节点富文本内容
    initNodeContent(data) {
      let param = { domainId: data.domainId, nodeId: data.nodeId };
      kgBuilderApi.getNodeContent(param).then(response => {
        if (response.code == 200) {
          if (response.data) {
            this.$refs.kg_form.initContent(response.data.content);
          } else {
            this.$message.warning("暂时没有更多数据");
          }
        }
      });
    },
    //初始化节点添加的图片
    initNodeImage(data) {


      let param = { domainId: data.domainId, nodeId: data.nodeId };
      kgBuilderApi.getNodeImage(param).then(response => {
        if (response.code == 200) {
          if (response.data) {
            let nodeImageList = [];
            for (let i = 0; i < response.data.length; i++) {
              nodeImageList.push({
                file: response.data[i].fileName,
                imageType: response.data[i].imageType
              });
              this.$refs.kg_form.initImage(nodeImageList);
            }
          } else {
            this.$message.warning("暂时没有更多数据");
          }
        }
      });
    },
    //一次性获取富文本和图片
    getNodeDetail(nodeId, left, top) {
      let data = { domainId: this.domainId, nodeId: nodeId };
      kgBuilderApi.getNodeDetail(data).then(result => {
        if (result.code == 200) {
          if (result.data) {
            this.$refs.node_richer.init(
              result.data.content,
              result.data.imageList,
              left,
              top
            );
          } else {
            this.$message.warning("暂时没有更多数据");
          }
        }
      });
    },
    //全屏
    requestFullScreen() {
      let element = document.getElementById("graphcontainerdiv");
      let width = window.screen.width;
      let height = window.screen.height;
      this.svg.attr("width", width);
      this.svg.attr("height", height);
      if (element.requestFullscreen) {
        element.requestFullscreen();
      }
      // FireFox
      else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      }
      // Chrome等
      else if (element.webkitRequestFullScreen) {
        element.webkitRequestFullScreen();
      }
      // IE11
      else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    },
    //获取图谱节点及关系
    getDomainGraph() {
      //this.loading = true;
      let data = {
        domain: this.domain,
        nodeName: this.nodeName,
        pageSize: this.pageSize
      };
      let _this = this;

      kgBuilderApi.getDomainGraph(data).then(result => {
        if (result.code == 200) {
          if (result.data != null) {
            _this.graphData = { nodes: [], links: [] };
            _this.graphData.nodes = result.data.node;
            _this.graphData.links = result.data.relationship;
          }
        }
      });
    },
    //展开更多节点
    getMoreNode() {
      let data = { domain: this.domain, nodeId: this.selectNode.nodeId };
      kgBuilderApi.getMoreRelationNode(data).then(result => {
        if (result.code == 200) {
          //把不存在于画布的节点添加到画布
          this.mergeNodeAndLink(result.data.node, result.data.relationship);
          //重新绘制
          //this.updateGraph();
        }
      });
    },
    //快速添加
    btnQuickAddNode() {
      if(getIsTeacher() == false){
        this.$message.warning("您不是老师，没有权限快速添加");
        return;
      }
      this.$refs.kg_form.init(true, "batchAdd", this.domain);
    },
    //修改领域

    updateDomain(id, value) {
      if(getIsTeacher() == false){
        this.$message.warning("您不是老师，没有权限修改领域");
        return;
      }
      if (!this.domain || this.domain === "") {
    this.$message.warning("请选择一个领域");
    return;
  }
  this.$prompt('请输入新的领域名称', '修改领域', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^[a-zA-Z0-9\u4e00-\u9fa5]+$/,
    inputErrorMessage: '领域名称不能为空'
  }).then(({ value }) => {
    // 用户点击了“确定”，并且输入了新的领域名称
    let data = { domainId: this.domainId, newDomain: value };
    kgBuilderApi.updateDomain(data).then(result => {
      // API调用成功
      if (result.code == 200) {
        this.getDomain(); // 刷新领域列表
        this.$message({
          type: 'success',
          message: '修改成功'
        });
      } else {
        // API调用返回了错误代码
        this.$message.error('修改失败：' + result.msg);
      }
    }).catch(error => {
      // API调用失败，这里不应该显示“已取消修改”
      // 我们在这里处理错误，但不抛出异常，避免触发外层的.catch()
      this.$message.error('修改操作出错：' + error);
    });
  }).catch(() => {
    // 用户点击了“取消”按钮
    // 我们确保只有在这个情况下显示“已取消修改”的消息
    this.$message({
      type: 'info',
      message: '已取消修改'
    });
  });
},
    //删除领域
    deleteDomain(id, value) {
      if(getIsTeacher() == false){
        this.$message.warning("您不是老师，没有权限删除领域");
        return;
      }
    if (!this.domain || this.domain === "") {
      this.$message.warning("请选择一个领域");
      return;
    }
  this.$confirm(
    "此操作将删除该标签及其下节点和关系(不可恢复), 是否继续?",
    "三思而后行",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    }
  )
  .then(() => {
    // 使用箭头函数确保this指向当前Vue实例
    let data = { domainId: this.domainId, domain: this.domain };
    kgBuilderApi.deleteDomain(data).then(result => {
      if (result.code == 200) {
        this.getDomain();
        this.domain = "";
      } else {
        this.$message.error("删除失败：" + result.msg);
      }
    }).catch(error => {
      this.$message.error("删除操作出错：" + error);
    });
  })
  .catch(() => {
    this.$message({
      type: "info",
      message: "已取消删除"
    });
  });
},

    //创建新领域
    createDomain(value) {
      if(getIsTeacher() == false){
        this.$message.warning("您不是老师，无法创建领域");
        return;
      }
      this.$prompt("请输入领域名称", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消"
      })
        .then(res => {
          value = res.value;
          let data = { domain: value, type: 0 };
          kgBuilderApi.createDomain(data).then(result => {
            if (result.code == 200) {
              this.getDomain();
              this.domain = value;
              this.domainAlia = value;
              this.getDomainGraph();
            }
          });
        })
        .catch(() => {});
    },
    //获取领域标签
    getLabels(data) {
      kgBuilderApi.getDomains(data).then(result => {
        if (result.code == 200) {
          this.pageModel = result.data;
          this.pageModel.totalPage =
            parseInt((result.data.totalCount - 1) / result.data.pageSize) + 1;
          this.pageModel.nodeList.map(n => {
            n.type = "";
            return n;
          });
        }
      });
    },
    getDomain(pageIndex) {
      this.pageModel.pageIndex = pageIndex
        ? pageIndex
        : this.pageModel.pageIndex;
      let data = {
        pageIndex: this.pageModel.pageIndex,
        pageSize: this.pageModel.pageSize,
        command: 0
      };
      this.getLabels(data);
    },
    matchDomainGraph(domain) {
      console.log("matchDomainGraph:  "+domain);
      this.domain = domain.label;
      this.domainAlia = domain.name;
      this.domainId = domain.id;
      this.getDomainGraph();
      this.pageModel.nodeList.map(n => {
        if (n.name == domain.name) {
          n.type = "success";
        } else {
          n.type = "";
        }
        return n;
      });
    },
    //保存图片
    saveImage() {
      html2canvas(document.querySelector(".graphContainer"), {
        width: document.querySelector(".graphContainer").offsetWidth, // canvas画板的宽度 一般都是要保存的那个dom的宽度
        height: document.querySelector(".graphContainer").offsetHeight, // canvas画板的高度  同上
        scale: 1
      }).then(function(canvas) {
        let a = document.createElement("a");
        a.href = canvas.toDataURL("image/png"); //将画布内的信息导出为png图片数据
        let timeStamp = Date.parse(new Date());
        a.download = timeStamp; //设定下载名称
        a.click(); //点击触发下载
      });
    },
    showJsonData() {
      this.$refs.kg_json.init();
    },
    wanted() {
      this.$refs.kg_wanted.init();
    },
    //导入图谱
    importGraph() {
      if(getIsTeacher() == false){
        this.$message.warning("您不是老师，没有权限导入图谱");
        return;
      }
      if (!this.domain || this.domain == "") {
        this.$message.warning("请选择一个领域");
        return;
      }
      this.$refs.kg_form.init(true, "import", this.domain);
      // this.matchDomainGraph(this.domain);

    },

    //导出图谱
    exportGraph() {
  if (!this.domain || this.domain === "") {
    this.$message.warning("请选择一个领域");
    return;
  }
  let data = { domain: this.domain };

  // 发送请求到后端
  kgBuilderApi.exportGraph(data).then(result => {
    if (result.code === 200) {
      // 获取后端返回的下载链接
      const downloadUrl = "http://localhost:8080"+result.fileName;
      console.log("downloadUrl: "+downloadUrl);
      // 创建一个a标签用于下载
      const downloadLink = document.createElement('a');
      downloadLink.href = downloadUrl;
      downloadLink.download = downloadUrl.split('/').pop(); // 设置下载文件名
      document.body.appendChild(downloadLink);
      downloadLink.click(); // 模拟点击下载
      document.body.removeChild(downloadLink); // 移除临时创建的a标签
    }
  }).catch(error => {
    console.error("导出失败:", error);
  });
},
    // exportGraph() {
    //   if (!this.domain || this.domain == "") {
    //     this.$message.warning("请选择一个领域");
    //     return;
    //   }
    //   let data = { domain: this.domain };
    //   kgBuilderApi.exportGraph(data).then(result => {
    //     if (result.code == 200) {
    //       console.log("result.fileName: "+result.fileName);

    //       window.location.href = result.fileName;

    //     }
    //   });
    // },
    help() {
      this.$refs.kg_help.init();
    },
    //设置画布内最大的点个数
    setMatchSize(m) {
      for (let i = 0; i < this.pageSizeList.length; i++) {
        this.pageSizeList[i].isActive = false;
        if (this.pageSizeList[i].size == m.size) {
          this.pageSizeList[i].isActive = true;
        }
      }
      this.pageSize = m.size;
      this.getDomainGraph();
    },
    //合并节点和连线
    mergeNodeAndLink(newNodes, newLinks) {
      let _this = this;
      newNodes.forEach(function(m) {
        let sobj = _this.graphData.nodes.find(function(x) {
          return x.uuid === m.uuid;
        });
        if (typeof sobj == "undefined") {
          _this.graphData.nodes.push(m);
        }
      });
      newLinks.forEach(function(m) {
        let sobj = _this.graphData.links.find(function(x) {
          return x.uuid === m.uuid;
        });
        if (typeof sobj == "undefined") {
          _this.graphData.links.push(m);
        }
      });
    },
    //批量添加节点
    batchCreateNode(param) {
      if(!getIsTeacher()){
        this.$message.warning("您不是老师，没有权限添加节点");
        return;
      }
      let data = {
        domain: this.domain,
        sourceName: param.sourceNodeName,
        targetNames: param.targetNodeNames,
        relation: param.relation
      };
      kgBuilderApi.batchCreateNode(data).then(result => {
        if (result.code == 200) {
          //把不存在于画布的节点添加到画布
          this.mergeNodeAndLink(result.data.nodes, result.data.ships);
          //重新绘制
          //this.updateGraph();
          this.$message({
            message: "操作成功",
            type: "success"
          });
        }
      });
    },
    //批量添加子节点
    batchCreateChildNode(param) {
      if(!getIsTeacher()){
        this.$message.warning("您不是老师，没有权限添加节点");
        return;
      }
      let data = {
        domain: this.domain,
        sourceId: param.sourceUuid,
        targetNames: param.targetNodeNames,
        relation: param.relation
      };
      kgBuilderApi.batchCreateChildNode(data).then(result => {
        if (result.code == 200) {
          //把不存在于画布的节点添加到画布
          this.mergeNodeAndLink(result.data.nodes, result.data.ships);
          //重新绘制
          this.$message({
            message: "操作成功",
            type: "success"
          });
        }
      });
    },
    //批量添加同级节点
    batchCreateSameNode(param) {
      if(!getIsTeacher()){
        this.$message.warning("您不是老师，没有权限添加节点");
        return;
      }
      let data = {
        domain: this.domain,
        sourceNames: param.sourceNodeName
      };
      kgBuilderApi.batchCreateSameNode(data).then(result => {
        if (result.code == 200) {
          //把不存在于画布的节点添加到画布
          this.mergeNodeAndLink(result.data, null);
          this.$message({
            message: "操作成功",
            type: "success"
          });
        }
      });
    },
    goToAgentExtraction() {
      this.$router.push({ name: 'AgentExtraction' });
    },
    goToBatchExtraction() {
      this.$router.push({ name: 'BatchExtraction' });
    }
  }
};
</script>
<style>
.graphContainer {
  height: 100vh -50px;
}
.mind-box {
  height: calc(100vh - 85px);
  overflow: hidden;
}
.mind-l {
  width: 300px;
  float: left;
  background: #f7f9fc;
  height: 100%;
  border-right: 1px solid #d3e2ec;
}
.ml-ht {
  padding-top: 20px;
  line-height: 50px;
  font-size: 16px;
  font-weight: 400;
  text-align: center;
  color: #0713ea;
  border-bottom: 1px solid #d3e2ec;
}
.ml-a-box {
  margin: 10px;
}
.ml-a {
  display: inline-block;
  min-width: 46px;
  line-height: 1;
  padding: 6px 8px 6px 8px;
  margin: 0 4px 5px 0;
  background: #fff;
  border: 1px solid #e3e3e3;
  box-sizing: border-box;
  transition: 0.3s;
}
.ml-a span {
  max-width: 190px;
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}
.ml-a-all {
  display: block;
  margin: 10px 10px 0;
  text-align: center;
}
.ml-a span:empty:before {
  content: "閺堫亜鎳￠崥锟�";
  color: #adadad;
}
.ml-a small {
  color: #999;
}
.ml-a:hover {
  background: #f4f4f4;
}
.ml-a.cur,
.ml-a.cur small {
  background: #156498;
  color: #fff;
}
.ml-btn-box {
  text-align: right;
  padding: 0 10px;
  margin-bottom: 20px;
}
.ml-btn {
  padding: 0 5px;
  color: #156498;
}
.mind-con {
  height: calc(100vh - 40px);
  overflow: hidden;
  background: #fff;
  display: -webkit-flex;
  display: flex;
  flex-direction: column;
  padding: 5px;
}
.mind-top {
  /* line-height: 70px;
  height: 70px; */
  padding: 0 22px;
  border-bottom: 1px solid #ededed;
}
.mt-m {
  color: #666;
  margin-right: 30px;
}
.mt-m i {
  font-size: 18px;
  color: #333;
  font-weight: 700;
  font-style: normal;
}
.mb-con .search,
.mind-top .search {
  border: 1px solid #e2e2e2;
}
.svg-a-sm {
  font-size: 14px;
  color: #156498;
  margin-right: 30px;
  cursor: pointer;
}
.mind-cen {
  height: calc(100% - 70px);
}
.half-auto {
  height: 40%;
}
.mind-bottom {
  height: 490px;
  box-sizing: border-box;
  border-top: 1px solid #ededed;
}
.ss-d {
  display: inline-block;
  vertical-align: middle;
  margin-right: 10px;
  border-radius: 50%;
  background: #dedede;
}
.sd {
  margin: 2px;
}
.sd-active {
  color: red !important;
  background: none !important;
}
.btn-line + .btn-line {
  margin-left: 10px;
}
.co {
  color: #ee8407 !important;
}
a {
  text-decoration: none;
}
.a {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.fl {
  float: left;
}
.fr {
  float: right;
  margin: 7px;
}
.tl {
  text-align: left;
}
.pl-20 {
  padding-left: 20px;
}
text {
  cursor: pointer;
  max-width: 25px;
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
}
circle {
  cursor: pointer;
}
#graphcontainerdiv {
  background: #fff;
}
.el-color-picker__panel {
  left: 812px !important;
}
.wange-toolbar {
  border: 1px solid #ccc;
}
.wangeditor-form {
  border: 1px solid #ccc;
  height: 350px;
  min-height: 340px;
}
.el-tag {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mind-fj-box {
  display: inline-block;
  width: 290px;
  padding: 5px;
  border: 1px solid #e6e6e6;
  box-shadow: 0 0 8px rgba(206, 205, 201, 0.38);
}
.mind-fj-p {
  color: #666;
  line-height: 24px;
  padding: 5px;
  background: rgba(255, 255, 255, 0.85);
}
.mind-carousel + .mind-fj-p .el-scrollbar__wrap {
  height: auto;
  max-height: 220px;
  min-height: 0;
}
.carous-img {
  height: 100%;
  background: rgba(0, 0, 0, 0.1);
  line-height: 197px;
  text-align: center;
}
.carous-img img {
  max-width: 100%;
  max-height: 100%;
  line-height: 197px;
  vertical-align: middle;
}

.node_detail {
  position: absolute;
  width: 100%;
  line-height: 35px;
  -webkit-border-radius: 10px;
  -moz-border-radius: 10px;
  border-radius: 10px;
  font-size: 12px;
  padding-bottom: 10px;
  background: rgba(198, 226, 255, 0.2);
  display: none;
}
.node_pd {
  padding: 4px;
  font-size: 13px;
  font-family: -webkit-body;
  font-weight: 600;
}
.operatetips {
  position: absolute;
  right: 10px;
  float: right;
  top: 0;
  width: 335px;
  padding: 30px;
  border: 2px #ee7942 solid;
  border-radius: 4px;
}
.jsoncontainer {
  position: absolute;
  right: 30%;
  float: right;
  top: 0;
  width: 60%;
  height: 60%;
  padding: 30px;
  border: 2px #ee7942 solid;
  border-radius: 4px;
  background: #fff;
}
.cypher_toolbar {
  line-height: 70px;
  height: 85px;
  padding: 0 22px;
  border-bottom: 1px solid #ededed;
}
.hometitle {
  font-size: 18px;
  color: #282828;
  font-weight: 600;
  margin: 0;
  text-transform: uppercase;
  padding-bottom: 15px;
  margin-bottom: 25px;
  position: relative;
}

.el-scrollbar {
  overflow: hidden;
  position: relative;
}
ul {
  padding: 0px;
}
.icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}
.el-button {
  display: inline-block;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  background: #fff;
  border: 1px solid #d8dce5;
  color: #5a5e66;
  -webkit-appearance: none;
  text-align: center;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  outline: 0;
  margin: 0;
  -webkit-transition: 0.1s;
  transition: 0.1s;
  font-weight: 500;
  padding: 12px 20px;
  font-size: 14px;
  border-radius: 4px;
}
.search {
  position: relative;
  width: 220px;
  height: 32px;
  border-radius: 32px;
  overflow: hidden;
}
.search .el-input__inner {
  box-sizing: border-box;
  padding-left: 15px;
  height: 32px;
  line-height: 32px;
  padding-right: 40px;
  background: transparent;
  border-radius: 32px;
  border: none;
  transition: background 0.3s;
}
.search .el-button--default {
  position: absolute;
  right: 1px;
  float: right;
  padding: 0 10px;
  font-size: 22px;
  line-height: 29px;
  color: #7c9cb2;
  background: transparent;
  border: none;
  z-index: 1;
}
.search .el-button--default:hover {
  color: #156498;
  background: transparent;
  border: none;
}
.top .search {
  margin-left: 30px;
  background: rgba(0, 0, 0, 0.25);
  display: none;
}
.circle_none {
  display: none;
}
.dibmr {
  padding: 4px;
  display: inline-block;
  line-height: 30px;
}
.tag-ml-5{
 margin:5px;
 cursor:pointer;
 float:left
}

</style>
