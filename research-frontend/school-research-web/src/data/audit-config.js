// src/data/audit-config.js

export const AUDIT_CONFIG = {
  // 1. 项目 (Project)
  project: {
    label: '项目',
    idKey: 'projectId', // 后端 AuditDto 接收的 ID 字段名,统一接收，不需要根据专利、软著论文等区分
    fileField: 'closeFileUrl', // 附件字段名
    columns: [
      { prop: 'name', label: '项目名称', minWidth: 250 },
      { prop: 'funds', label: '经费(万元)', minwidth: 120 },
      { prop: 'level', label: '级别', width: 120 }, // 国家级/省级
      { prop: 'startDate', label: '开始日期', width: 140 },
      { prop: 'endDate', label: '结束日期', width: 140 }
    ]
  },
  
  // 2. 论文 (Paper)
  paper: {
    label: '论文',
    idKey: 'projectId', // 注意：如果你的Paper审核接口复用了AuditDto且字段叫projectId，就填这个
    fileField: 'proofFile', // 论文的附件字段通常是 proofFile 或 fileUrl
    columns: [
      { prop: 'title', label: '论文标题', minWidth: 200 },
      { prop: 'journalName', label: '期刊名称', minwidth: 150 },
      {prop: 'pages', label: '页码范围', width: 120 },
        {prop:'isSci' , label: '是否SCI', width: 100 },
      { prop: 'sciPartition', label: 'SCI分区', minwidth: 200 },
        { prop: 'impactFactor', label: '影响因子', width: 100 },
        { prop: 'issn', label: 'ISSN', width: 120 },
        { prop: 'authorRole', label: '作者排名', width: 100},

        { prop: 'publishDate', label: '发表日期', width: 140 },
      
      

    ]
  },

  // 3. 专利 (Patent)
  patent: {
    label: '专利',
    idKey: 'projectId', 
    fileField: 'proofFile',
    columns: [
      { prop: 'name', label: '专利名称', minWidth: 180 }, 
      { prop: 'patentNo', label: '专利号', minwidth: 150 },
      { prop: 'patentType', label: '类型', minwidth: 100 }
    ]
  },

  // 4. 软著 (Software)
  software: {
    label: '软著',
    idKey: 'projectId',
    fileField: 'proofFile',
    columns: [
      { prop: 'name', label: '软件名称', minWidth: 180 },
      { prop: 'registerNo', label: '登记号', width: 150 },
      { prop: 'softwareType', label: '软件类别', width: 120 },
      { prop: 'grantDate', label: '授权日期', width: 140 }
    ]
  },

  // 5. 专著 (Book)
  book: {
    label: '专著',
    idKey: 'projectId',
    fileField: 'proofFile',
    columns: [
      { prop: 'name', label: '书名', minWidth: 180 },
      { prop: 'isbn', label: 'ISBN', width: 150 },
      { prop: 'publisher', label: '出版社', width: 150 },
      { prop: 'publishDate', label: '出版日期', width: 120 }
    ]
  }
}