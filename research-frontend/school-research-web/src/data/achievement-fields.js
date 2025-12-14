export const FORM_FIELDS = {
  paper: [
    { key: "title", label: "论文标题", type: "input", required: true },
    { key: "journal_name", label: "期刊名称", type: "input" },
    { key: "issn", label: "ISSN", type: "input" },
    { key: "impact_factor", label: "影响因子", type: "input" },
    { key: "pages", label: "页码范围", type: "input" },
    { key: "partition", label: "分区", type: "select", options: ["一区","二区","三区","四区"] },
    { key: "publish_date", label: "发表日期", type: "date" },
    { key: "authors", label: "作者", type: "input" },
    { key: "corresponding_author", label: "通讯作者", type: "input" },
    { key: "discipline", label: "学科分类", type: "input" },
    { key: "category", label: "成果类别", type: "select", options: ["SCI","EI","核心期刊"] },
    { key: "proof_file", label: "证明文件", type: "upload" },
    { key: "remark", label: "备注说明", type: "textarea" },
     { 
      key: "classification", // 对应后端字段
      label: "成果归属", 
      type: "select", 
      required: true,
      options: [
        { label: "科研成果", value: "RESEARCH" }, // 选这个 -> 发给科研秘书
        { label: "教学成果", value: "TEACHING" }  // 选这个 -> 发给教学秘书
      ]
    },
  ],
  project: [
    { key: "project_name", label: "项目名称", type: "input", required: true },
    { key: "project_source", label: "项目来源", type: "input" },
    { key: "level", label: "级别", type: "select", options: ["国家级","省部级","校级"] },
    { key: "project_number", label: "项目编号", type: "input" },
    { key: "budget", label: "经费预算", type: "input" },
    { key: "start_date", label: "开始日期", type: "date" },
    { key: "end_date", label: "结束日期", type: "date" },
    { key: "leader", label: "项目负责人", type: "input" },
    { key: "participants", label: "参与人员", type: "input" },
    { key: "discipline", label: "学科分类", type: "input" },
    { key: "open_file", label: "开题证明", type: "upload" },
    { key: "close_file", label: "结题证明", type: "upload" },
    { key: "remark", label: "备注说明", type: "textarea" }
  ],
  software: [
    { key: "software_name", label: "软件名称", type: "input", required: true },
    { key: "register_no", label: "登记号", type: "input" },
    { key: "grant_date", label: "授权日期", type: "date" },
    { key: "authors", label: "作者", type: "input" },
    { key: "category", label: "软件类别", type: "select", options: ["应用软件","系统软件","工具软件"] },
    { key: "proof_file", label: "证书文件", type: "upload" },
    { key: "remark", label: "备注说明", type: "textarea" },
     { 
      key: "classification", // 对应后端字段
      label: "成果归属", 
      type: "select", 
      required: true,
      options: [
        { label: "科研成果", value: "RESEARCH" }, // 选这个 -> 发给科研秘书
        { label: "教学成果", value: "TEACHING" }  // 选这个 -> 发给教学秘书
      ]
    },
  ],
  patent: [
    { key: "patent_name", label: "专利名称", type: "input", required: true },
    { key: "patent_no", label: "专利号", type: "input" },
    { key: "patent_type", label: "专利类型", type: "select", options: ["发明专利","实用新型","外观设计"] },
    { key: "apply_date", label: "申请日期", type: "date" },
    { key: "grant_date", label: "授权日期", type: "date" },
    { key: "owner", label: "专利权人", type: "input" },
    { key: "inventors", label: "发明人", type: "input" },
    // { key: "status", label: "专利状态", type: "select", options: ["已授权","审中","已失效"] },
    { 
      key: "classification", // 对应后端字段
      label: "成果归属", 
      type: "select", 
      required: true,
      options: [
        { label: "科研成果", value: "RESEARCH" }, // 选这个 -> 发给科研秘书
        { label: "教学成果", value: "TEACHING" }  // 选这个 -> 发给教学秘书
      ]
    },
    { key: "proof_file", label: "授权证明文件", type: "upload" },
    { key: "remark", label: "备注说明", type: "textarea" }
  ],
  book: [
    { key: "name", label: "书名", type: "input", required: true },
    { key: "isbn", label: "ISBN", type: "input" },
    { key: "publisher", label: "出版社", type: "input" },
    { key: "publish_date", label: "出版日期", type: "date" },
    { key: "authors", label: "作者", type: "input" },
    { key: "editor", label: "主编/副主编", type: "input" },
    { key: "category", label: "出版类别", type: "select", options: ["教材","学术专著","普及读物"] },
    { key: "level", label: "出版级别", type: "select", options: ["国家级出版社","地方出版社"] },
    { key: "proof_file", label: "证明文件", type: "upload" },
    { key: "remark", label: "备注说明", type: "textarea" },
     { 
      key: "classification", // 对应后端字段
      label: "成果归属", 
      type: "select", 
      required: true,
      options: [
        { label: "科研成果", value: "RESEARCH" }, // 选这个 -> 发给科研秘书
        { label: "教学成果", value: "TEACHING" }  // 选这个 -> 发给教学秘书
      ]
    },
  ]
}
