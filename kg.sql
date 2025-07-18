/*
Navicat MySQL Data Transfer

Source Server         : 练习
Source Server Version : 80023
Source Host           : localhost:3306
Source Database       : kg

Target Server Type    : MYSQL
Target Server Version : 80023
File Encoding         : 65001

Date: 2025-04-24 10:26:34
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for kg_category
-- ----------------------------
DROP TABLE IF EXISTS `kg_category`;
CREATE TABLE `kg_category` (
  `CategoryNodeId` int NOT NULL AUTO_INCREMENT,
  `CategoryNodeName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `CategoryNodeCode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `SystemCode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `CategoryId` bigint NOT NULL,
  `ParentId` int DEFAULT NULL,
  `ParentCode` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `TreeLevel` int DEFAULT NULL,
  `IsLeaf` int NOT NULL DEFAULT '1',
  `Status` int DEFAULT NULL,
  `FileUuid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '附件uuid',
  `FileName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '节点来源附件',
  `CreateUser` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `CreateTime` datetime DEFAULT NULL,
  `UpdateUser` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `UpdateTime` datetime DEFAULT NULL,
  `Color` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  PRIMARY KEY (`CategoryNodeId`) USING BTREE,
  KEY `categoryId` (`CategoryId`) USING BTREE,
  KEY `parentId` (`ParentId`) USING BTREE,
  KEY `parentCode` (`ParentCode`) USING BTREE,
  KEY `categoryCode` (`CategoryNodeCode`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=102609 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for kg_domain
-- ----------------------------
DROP TABLE IF EXISTS `kg_domain`;
CREATE TABLE `kg_domain` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `label` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `type` int DEFAULT NULL COMMENT '0=手动创建，1=三元组导入，2=excel导入，3=er图构建',
  `nodecount` int NOT NULL DEFAULT '0',
  `shipcount` int NOT NULL,
  `commend` int DEFAULT '0' COMMENT '推荐',
  `status` int NOT NULL,
  `createuser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `createtime` datetime DEFAULT NULL,
  `modifyTime` datetime DEFAULT NULL,
  `modifyUser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for kg_nodedetail
-- ----------------------------
DROP TABLE IF EXISTS `kg_nodedetail`;
CREATE TABLE `kg_nodedetail` (
  `ID` int NOT NULL AUTO_INCREMENT COMMENT '领域关系主键',
  `DomainId` int DEFAULT NULL COMMENT '知识图谱领域主键',
  `NodeId` int DEFAULT NULL COMMENT '关系定义主键',
  `Status` int DEFAULT '1',
  `Content` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `CreateUser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `CreateTime` datetime DEFAULT NULL,
  `ModifyUser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `ModifyTime` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `domainid` (`DomainId`) USING BTREE,
  KEY `nodeid` (`NodeId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=139 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for kg_nodedetail_file
-- ----------------------------
DROP TABLE IF EXISTS `kg_nodedetail_file`;
CREATE TABLE `kg_nodedetail_file` (
  `ID` int NOT NULL AUTO_INCREMENT COMMENT '领域关系主键',
  `DomainId` int DEFAULT NULL COMMENT '知识图谱领域主键',
  `NodeId` int DEFAULT NULL COMMENT '关系定义主键',
  `FileName` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '若是本地上传则为文件名称，若是网络链接则保存为链接',
  `ImageType` int DEFAULT '0' COMMENT '0=本地上传,1=网络链接',
  `Status` int DEFAULT '1',
  `CreateUser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `CreateTime` datetime DEFAULT NULL,
  `ModifyUser` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `ModifyTime` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`) USING BTREE,
  KEY `domainid` (`DomainId`) USING BTREE,
  KEY `nodeid` (`NodeId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=204 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `isteacher` int NOT NULL DEFAULT '0',
  `createtime` datetime DEFAULT NULL,
  `updatetime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1130 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
