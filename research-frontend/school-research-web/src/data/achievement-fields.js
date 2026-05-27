export const FORM_FIELDS = {
  paper: [
    { key: "title", label: "论文标题", type: "input", required: true },
    { key: "journalName", label: "期刊名称", type: "input" , required: true },
    { key: "issn", label: "ISSN", type: "input" , required: true },
    { key: "impactFactor", label: "影响因子", type: "input" , required: true },
    { key: "pages", label: "页码范围", type: "input", required: true },
    {key : "isSci" , label: "是否SCI", type: "select", options: ["是","否"] , required: true },
    { key: "sciPartition", label: "分区", type: "select", options: ["一区","二区","三区","四区","无"] , required: true },
    { key: "publishDate", label: "发表日期", type: "date", required: true },
    { key: "authors", label: "第一作者", type: "input",   required: true },
    { key: "correspondingAuthor", label: "通讯作者", type: "input" ,   required: true },
    { key: "discipline", label: "学科分类", type: "input",   },
    { 
      key: "classification", // 对应后端字段
      label: "成果分类", 
      type: "select", 
      required: true,
      options: [
        { label: "科研成果", value: "科研" }, // 选这个 -> 发给科研秘书
        { label: "教学成果", value: "教学" }  // 选这个 -> 发给教学秘书
      ]
    },
    { key: "proofFile", label: "证明文件", type: "upload", required: true },
    { key: "tags", label: "标签", type: "input", placeholder: "多个标签用逗号分隔，如：重点,核心期刊" },
    { key: "remark", label: "备注说明", type: "textarea" },

  ],
  project: [
    { key: "name", label: "项目名称", type: "input", required: true },
    { key: "projectSource", label: "项目来源", type: "input" , required: true },
    { key: "level", label: "级别", type: "select", options: ["国家级","省部级","市级","校级"] , required: true },
    { key: "projectNumber", label: "项目编号", type: "input", required: true },
    { key: "funds", label: "经费预算", type: "input",  required: true },
    { key: "startDate", label: "开始日期", type: "date" , required: true },
    { key: "endDate", label: "结束日期", type: "date",  required: true },
    { key: "leader", label: "项目负责人", type: "input",  required: true },
    { key: "participants", label: "参与人员", type: "input",  required: true },
    { key: "discipline", label: "学科分类", type: "input" , required: true },
     { 
      key: "classification", // 对应后端字段
      label: "成果分类", 
      type: "select", 
      required: true,
      options: [
        { label: "科研成果", value: "科研" }, // 选这个 -> 发给科研秘书
        { label: "教学成果", value: "教学" }  // 选这个 -> 发给教学秘书
      ]
    },
    { key: "openFileUrl", label: "开题证明", type: "upload" },
    { key: "closeFileUrl", label: "结题证明", type: "upload" ,   required: true },
    { key: "tags", label: "标签", type: "input", placeholder: "多个标签用逗号分隔" },
    { key: "remark", label: "备注说明", type: "textarea" },
   

  ],
  software: [
    { key: "name", label: "软件名称", type: "input", required: true },
    { key: "registerNo", label: "登记号", type: "input", required: true },
    { key: "grantDate", label: "授权日期", type: "date",  required: true },
    { key: "authors", label: "作者", type: "input",  required: true },
    { key: "softwareType", label: "软件类别", type: "select", options: ["应用软件","系统软件","工具软件"],  required: true },
     { 
      key: "classification", // 对应后端字段
      label: "成果分类", 
      type: "select", 
      required: true,
      options: [
        { label: "科研成果", value: "科研" }, // 选这个 -> 发给科研秘书
        { label: "教学成果", value: "教学" }  // 选这个 -> 发给教学秘书
      ]
    },
    { key: "proofFile", label: "证书文件", type: "upload" ,required: true },
    { key: "tags", label: "标签", type: "input", placeholder: "多个标签用逗号分隔" },
    { key: "remark", label: "备注说明", type: "textarea" },

  ],
  patent: [
    { key: "name", label: "专利名称", type: "input", required: true },
    { key: "patentNo", label: "专利号", type: "input" , required: true },
    { key: "patentType", label: "专利类型", type: "select", options: ["发明专利","实用新型","外观设计"] , required: true },
    { key: "applyDate", label: "申请日期", type: "date" , required: true },
    { key: "grantDate", label: "授权日期", type: "date",  required: true },
    { key: "owner", label: "专利权人", type: "input" ,  required: true },
   
    // { key: "inventors", label: "发明人", type: "input" },
    // { key: "status", label: "专利状态", type: "select", options: ["已授权","审中","已失效"] },
     { 
      key: "classification", // 对应后端字段
      label: "成果分类", 
      type: "select", 
      required: true,
      options: [
        { label: "科研成果", value: "科研" }, // 选这个 -> 发给科研秘书
        { label: "教学成果", value: "教学" }  // 选这个 -> 发给教学秘书
      ]
    },
    { key: "proofFile", label: "授权证明文件", type: "upload", required: true },
    { key: "tags", label: "标签", type: "input", placeholder: "多个标签用逗号分隔" },
    { key: "remark", label: "备注说明", type: "textarea" }
  ],
  book: [
    { key: "name", label: "书名", type: "input", required: true },
    { key: "isbn", label: "ISBN", type: "input" , required: true },
    { key: "publisher", label: "出版社", type: "input", required: true },
    { key: "publishDate", label: "出版日期", type: "date" , required: true },
    { key: "authors", label: "作者", type: "input" , required: true },
    { key: "editor", label: "主编/副主编", type: "input" , required: true },
    { key: "bookType", label: "出版类别", type: "select", options: ["教材","学术专著","普及读物"] , required: true },
    { key: "level", label: "出版级别", type: "select", options: ["国家级出版社","地方出版社"] , required: true },
    { 
      key: "classification", // 对应后端字段
      label: "成果分类", 
      type: "select", 
      required: true,
      options: [
        { label: "科研成果", value: "科研" }, // 选这个 -> 发给科研秘书
        { label: "教学成果", value: "教学" }  // 选这个 -> 发给教学秘书
      ]
    },
    { key: "proofFile", label: "证明文件", type: "upload" , required: true },
    { key: "tags", label: "标签", type: "input", placeholder: "多个标签用逗号分隔" },
    { key: "remark", label: "备注说明", type: "textarea" }

  ],
  award: [
    { key: "awardName", label: "获奖名称", type: "input", required: true },
    { key: "awardLevel", label: "获奖级别", type: "select", options: ["国家级","省部级","市厅级","校级"], required: true },
    { key: "awardGrade", label: "获奖等级", type: "select", options: ["一等奖","二等奖","三等奖"], required: true },
    { key: "awardUnit", label: "颁奖单位", type: "input", required: true },
    { key: "awardDate", label: "获奖日期", type: "date", required: true },
    { key: "ranking", label: "本人排名", type: "input" },
    {
      key: "classification",
      label: "成果分类",
      type: "select",
      required: true,
      options: [
        { label: "科研成果", value: "科研" },
        { label: "教学成果", value: "教学" }
      ]
    },
    { key: "proofFile", label: "证明文件", type: "upload", required: true },
    { key: "tags", label: "标签", type: "input", placeholder: "多个标签用逗号分隔" },
    { key: "remark", label: "备注说明", type: "textarea" }
  ],
  competition: [
    { key: "name", label: "竞赛名称", type: "input", required: true },
    { key: "competitionLevel", label: "竞赛级别", type: "select", options: ["国家级","省部级","校级"], required: true },
    { key: "awardLevel", label: "获奖级别", type: "select", options: ["国家级","省部级","校级"], required: true },
    { key: "awardGrade", label: "获奖等级", type: "select", options: ["一等奖","二等奖","三等奖"], required: true },
    { key: "awardDate", label: "获奖日期", type: "date", required: true },
    { key: "studentName", label: "学生姓名", type: "input" },
    { key: "ranking", label: "本人排名", type: "input" },
    {
      key: "classification",
      label: "成果分类",
      type: "select",
      required: true,
      options: [
        { label: "科研成果", value: "科研" },
        { label: "教学成果", value: "教学" }
      ]
    },
    { key: "certFileUrl", label: "证书文件", type: "upload", required: true },
    { key: "tags", label: "标签", type: "input", placeholder: "多个标签用逗号分隔" },
    { key: "remark", label: "备注说明", type: "textarea" }
  ],
  course: [
    { key: "courseName", label: "课程名称", type: "input", required: true },
    { key: "courseType", label: "课程类型", type: "select", options: ["精品课程","一流课程","思政示范课","教改课程"], required: true },
    { key: "courseLevel", label: "课程级别", type: "select", options: ["国家级","省级","校级"], required: true },
    { key: "startDate", label: "开始日期", type: "date", required: true },
    { key: "description", label: "课程描述", type: "textarea" },
    {
      key: "classification",
      label: "成果分类",
      type: "select",
      required: true,
      options: [
        { label: "科研成果", value: "科研" },
        { label: "教学成果", value: "教学" }
      ]
    },
    { key: "proofFile", label: "证明文件", type: "upload", required: true },
    { key: "tags", label: "标签", type: "input", placeholder: "多个标签用逗号分隔" },
    { key: "remark", label: "备注说明", type: "textarea" }
  ]
}
