export const ACHIEVEMENT_COLUMNS = {
  project: [
    { prop: "name", label: "项目名称" },
    { prop: "level", label: "项目级别" },
    { prop: "funds", label: "经费(万元)" },
    { prop: "startDate", label: "开始日期" },
    { prop: "endDate", label: "结束日期" },
    { prop:"createTime" , label: "创建时间" },
    { prop: "status", label: "状态" },
    { prop: "tags", label: "标签" },
    { prop: "classification", label: "成果分类" }
  ],
  paper: [
    { prop: "title", label: "论文标题" },
    { prop: "journalName", label: "期刊" },
    {prop: "issn", label: "ISSN" },
    {prop:"isSci" , label: "是否SCI" },
    { prop: "sciPartition", label: "SCI分区" },
    { prop: "impactFactor", label: "影响因子" },
    { prop: "publishDate", label: "发表日期" },
   { prop: "status", label: "状态" },
    { prop:"createTime" , label: "创建时间" },
    { prop: "tags", label: "标签" },
    { prop: "classification", label: "成果分类" }
  ],
  patent: [
    { prop: "patentNo", label: "专利号" },
    { prop: "name", label: "专利名称" },
    { prop: "grantDate", label: "授权日期" },
   { prop: "status", label: "状态" },
    { prop:"createTime" , label: "创建时间" },
    { prop: "tags", label: "标签" },
    { prop: "classification", label: "成果分类" }
  ],
  software: [
    { prop: "name", label: "软件名称" },
    { prop: "registerNo", label: "登记号" },
    { prop: "grantDate", label: "授权日期" },
    { prop: "status", label: "状态" },
    { prop:"createTime" , label: "创建时间" },
    { prop: "softwareType", label: "软件类别" },
    { prop: "tags", label: "标签" },
    {prop: "classification", label: "成果分类" }
  ],
  book: [
    { prop: "name", label: "书名" },
    {prop:"bookType" , label: "图书类别" },
    { prop: "publisher", label: "出版社" },
    { prop: "isbn", label: "ISBN号" },
    { prop: "publishDate", label: "出版日期" },
   { prop: "status", label: "状态" },
    { prop:"createTime" , label: "创建时间" },
    { prop: "tags", label: "标签" },
    { prop: "classification", label: "成果分类" }
  ],
  award: [
    { prop: "awardName", label: "获奖名称" },
    { prop: "awardLevel", label: "获奖级别" },
    { prop: "awardGrade", label: "获奖等级" },
    { prop: "awardUnit", label: "颁奖单位" },
    { prop: "awardDate", label: "获奖日期" },
    { prop: "ranking", label: "本人排名" },
    { prop: "status", label: "状态" },
    { prop: "createTime", label: "创建时间" },
    { prop: "tags", label: "标签" },
    { prop: "classification", label: "成果分类" }
  ],
  competition: [
    { prop: "name", label: "竞赛名称" },
    { prop: "competitionLevel", label: "竞赛级别" },
    { prop: "awardLevel", label: "获奖级别" },
    { prop: "awardGrade", label: "获奖等级" },
    { prop: "awardDate", label: "获奖日期" },
    { prop: "studentName", label: "学生姓名" },
    { prop: "status", label: "状态" },
    { prop: "createTime", label: "创建时间" },
    { prop: "tags", label: "标签" },
    { prop: "classification", label: "成果分类" }
  ],
  course: [
    { prop: "courseName", label: "课程名称" },
    { prop: "courseType", label: "课程类型" },
    { prop: "courseLevel", label: "课程级别" },
    { prop: "startDate", label: "开始日期" },
    { prop: "status", label: "状态" },
    { prop: "createTime", label: "创建时间" },
    { prop: "tags", label: "标签" },
    { prop: "classification", label: "成果分类" }
  ]
}
