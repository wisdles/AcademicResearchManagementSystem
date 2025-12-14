<template>
  <!-- 路由出口：URL 匹配到什么页面，就显示在这里 -->
  <router-view></router-view>
</template>

<style>
/* 全局样式重置 */
/* body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #f0f2f5;
} */
/* 1. 覆盖 Element Plus 的全局 CSS 变量 */
:root {
  /* 基础字体大小：默认是14px，改成 16px 或 17px */
  --el-font-size-base: 18px; 
  /* 小号字体（如辅助文字）：改大一点 */
  --el-font-size-small: 16px; 
  /* 大号字体（如标题）：改大一点 */
  --el-font-size-large: 20px;
  /* 超大号字体 */
  --el-font-size-extra-large: 26px;

  /* 调整一下行高，让文字不那么挤 */
  --el-font-line-height-primary: 1.6;
}

/* 2. 全局基础样式重置 */
html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  
  /* 强制全局字体变大 */
  font-size: 24px; 

  /* 优化字体族：优先使用无衬线字体，并针对中文优化 (微软雅黑/苹方) */
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;

  /* 字体抗锯齿渲染 (让字体看起来更清晰、不发虚) */
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;

  /* 字体颜色加深一点点，防止发灰看不清 */
  color: #2c3e50; 
  background-color: #f0f2f5;
}

/* 3. 针对 Element Plus 表格特别加粗一点，方便阅读数据 */
.el-table {
  font-size: 18px !important; /* 强制表格内容也用 16px */
}
.el-table th {
  font-weight: bold; /* 表头加粗 */
  color: #333 !important;
}

/* 4. 让表单里的输入框文字也大一点 */
.el-input, .el-select, .el-textarea {
  font-size: 18px !important;
}

/* 5. 侧边栏菜单文字加大 */
.el-menu-item, .el-sub-menu__title {
  font-size: 20px !important;
}
</style>