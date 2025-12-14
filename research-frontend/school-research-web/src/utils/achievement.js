export const ACHIEVEMENT_COLUMNS = {
  project: [
    { prop: "name", label: "项目名称" },
    { prop: "level", label: "项目级别" },
    { prop: "funding", label: "经费(万元)" },
    { prop: "category", label: "项目类别" },
    { prop: "startDate", label: "开始日期" },
    { prop: "endDate", label: "结束日期" },
    { prop:"createTime" , label: "创建时间" },
    { prop: "status", label: "状态" }
  ],
  paper: [
    { prop: "title", label: "论文标题" },
    { prop: "journal", label: "期刊" },
    { prop: "sciZone", label: "SCI分区" },
    { prop: "impactFactor", label: "影响因子" },
    { prop: "publishDate", label: "发表日期" },
   { prop: "status", label: "状态" },
    { prop:"createTime" , label: "创建时间" },
  ],
  patent: [
    { prop: "patentNo", label: "专利号" },
    { prop: "name", label: "专利名称" },
    { prop: "grantDate", label: "授权日期" },
   { prop: "status", label: "状态" },
    { prop:"createTime" , label: "创建时间" },
  ],
  software: [
    { prop: "name", label: "软件名称" },
    { prop: "registerNo", label: "登记号" },
    { prop: "grantDate", label: "授权日期" },
    { prop: "status", label: "状态" },
    { prop:"createTime" , label: "创建时间" },
    { prop: "category", label: "软件类别" },
    {prop: "classification", label: "归属" }
  ],
  book: [
    { prop: "name", label: "书名" },
    { prop: "publisher", label: "出版社" },
    { prop: "isbn", label: "ISBN号" },
    { prop: "publishDate", label: "出版日期" },
   { prop: "status", label: "状态" },
    { prop:"createTime" , label: "创建时间" },
  ]
}
