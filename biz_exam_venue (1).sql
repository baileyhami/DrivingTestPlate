/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50728 (5.7.28)
 Source Host           : localhost:3306
 Source Schema         : jiakao_yidiantong

 Target Server Type    : MySQL
 Target Server Version : 50728 (5.7.28)
 File Encoding         : 65001

 Date: 17/05/2026 14:20:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_exam_venue
-- ----------------------------
DROP TABLE IF EXISTS `biz_exam_venue`;
CREATE TABLE `biz_exam_venue`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区县',
  `latitude` decimal(10, 6) NULL DEFAULT NULL,
  `longitude` decimal(10, 6) NULL DEFAULT NULL,
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '考场' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of biz_exam_venue
-- ----------------------------
INSERT INTO `biz_exam_venue` VALUES (1, '武汉市八分山科目二科目三考场', '武汉市江夏区黄家湖大道', '027-59595537', '江夏区', 30.368363, 114.300485, '科目二三考试场地', '2026-05-08 08:28:03');
INSERT INTO `biz_exam_venue` VALUES (2, '武汉市大花岭科目二科目三考场', '武汉市江夏区大桥新区十字岭岭南路', '13487071987', '江夏区', 30.396797, 114.303068, '科目三多条线路', '2026-05-08 08:28:03');
INSERT INTO `biz_exam_venue` VALUES (3, '武汉市后官湖科目三考场', '武汉市经济开发区东风大道514号枫树产业园研发楼', '13871523023', '蔡甸区', 30.465889, 114.122242, '科目三', '2026-05-08 08:28:37');
INSERT INTO `biz_exam_venue` VALUES (4, '武汉市机动车驾驶人科目三薇湖考场', '武汉市汉南区纱帽街科技大道武汉市机动车驾驶人科目三薇湖考场', '13477071825', '汉南区', 30.309528, 114.063777, '科目三', '2026-05-08 08:28:37');
INSERT INTO `biz_exam_venue` VALUES (5, '武汉机动车驾驶人卓耀科目三考场', '武汉市黄陂区中环路8号武湖台创园瑞昌鑫科技产业园研发楼', '17786444656', '黄陂区', 30.713157, 114.438218, '科目三道路驾驶技能', '2026-05-08 08:28:37');

SET FOREIGN_KEY_CHECKS = 1;
