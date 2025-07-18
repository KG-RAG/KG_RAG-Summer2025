
## 技术栈
前台是基于vue + d3.js ,后台是springboot配合Neo4j.+MySql

## 运行与启动
### 安装jdk
可参考：[https://blog.csdn.net/qq_42003566/article/details/82629570](https://blog.csdn.net/qq_42003566/article/details/82629570)
### 安装Neo4j
可参考：[https://www.cnblogs.com/ljhdo/p/5521577.html](https://www.cnblogs.com/ljhdo/p/5521577.html)
### IDEA 导入项目 
导入成功后对着项目根目录，右键->maven->reimport

### 找到目录Neo4j-KGBuilder-dev/kgBuilder-pro /src/main/resources  
修改application-dev.yml,neo4j配置url，password,改成自己的，同理修改mysql（mysql脚本在根目录下，kg.sql）

### 安装nodejs
可参考[https://blog.csdn.net/qq_46351233/article/details/120314928](https://blog.csdn.net/qq_46351233/article/details/120314928)
启动前端

```
1.npm install // 安装依赖
2.npm run serve //启动
3.npm run build //发布
启动后访问http://localhost
```



