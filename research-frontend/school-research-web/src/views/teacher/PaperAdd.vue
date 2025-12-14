<template>
  <div class="paper-add-container">
    <el-card>
      <template #header>
        <span style="font-size: 18px; font-weight: bold;">新增论文</span>
      </template>

      <el-form :model="form" ref="formRef" label-width="120px" class="paper-form">
        <!-- 论文标题 -->
        <el-form-item label="论文标题">
          <el-input v-model="form.title" placeholder="请输入论文标题" />
        </el-form-item>

        <!-- 期刊名称 -->
        <el-form-item label="期刊名称">
          <el-input v-model="form.journal_name" placeholder="请输入期刊名称" />
        </el-form-item>

        <!-- 发表日期 -->
        <el-form-item label="发表日期">
          <el-date-picker v-model="form.publish_date" type="date" placeholder="选择日期" />
        </el-form-item>

        <!-- 是否SCI -->
        <el-form-item label="是否SCI">
          <el-switch v-model="form.is_sci" active-text="是" inactive-text="否" />
        </el-form-item>

        <!-- SCI分区 -->
        <el-form-item label="SCI分区">
          <el-select v-model="form.sci_partition" placeholder="请选择分区">
            <el-option label="一区" value="1" />
            <el-option label="二区" value="2" />
            <el-option label="三区" value="3" />
            <el-option label="四区" value="4" />
          </el-select>
        </el-form-item>

        <!-- 影响因子 -->
        <el-form-item label="影响因子">
          <el-input v-model="form.impact_factor" type="number" placeholder="请输入影响因子" />
        </el-form-item>

        <!-- 页码范围 -->
        <el-form-item label="页码范围">
          <el-input v-model="form.page_range" placeholder="如 12-20" />
        </el-form-item>

        <!-- 作者角色 -->
        <el-form-item label="作者角色">
          <el-select v-model="form.author_role" placeholder="请选择角色">
            <el-option label="第一作者" value="first" />
            <el-option label="通讯作者" value="corresponding" />
            <el-option label="共同作者" value="coauthor" />
          </el-select>
        </el-form-item>

        <!-- 文件上传 + OCR识别 -->
        <el-form-item label="上传文件">
          <el-upload
            :on-change="file => handleOCR(file.raw)"
            :auto-upload="false"
          >
            <el-button type="primary">上传论文文件进行OCR识别</el-button>
          </el-upload>
          <div v-if="form.file_url" style="margin-top: 10px;">
            <a :href="form.file_url" target="_blank">已上传文件下载</a>
          </div>
        </el-form-item>

        <!-- OCR识别结果 -->
        <el-form-item label="OCR识别内容">
          <el-input type="textarea" v-model="form.ocr_content" rows="6" />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitForm(true)">提交审核</el-button>
          <el-button @click="submitForm(false)">保存草稿</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import Tesseract from 'tesseract.js'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import journalData from '../../data/journal-data.json' // 本地期刊数据（可选）

const fetchJournalInfoLocal = (name) => {
  if (!name) return

  const n = name.trim().toLowerCase()
  let match = journalData.find(j => j.name.toLowerCase() === n)
  if (!match) match = journalData.find(j => j.name.toLowerCase().includes(n))

  if (match) {
    form.impact_factor = match.impactFactor ?? ''
    form.sci_partition = match.partition ?? ''
    form.is_sci = true
    ElMessage.success('期刊信息已自动填充')
  } else {
    ElMessage.warning('本地分区表中未找到该期刊，请手动填写')
  }
}

// 1) 预处理：统一换行、去多余空白、修复中间断裂空格（保守）
const normalizeText = (text) => {
  return text
    .replace(/\r/g, '')
    .replace(/[ \t]+\n/g, '\n')    // 行尾多余空格
    .replace(/\n{2,}/g, '\n')      // 多余空行
    .replace(/[ \t]{2,}/g, ' ')    // 连续空格
}

// 2) 在指定标签区域内提取“从引号到引号”的值
const getQuotedValueInSection = (section) => {
  const m = section.match(/[“"]\s*(.+?)\s*[”"]/s)
  return m ? m[1].trim() : ''
}

// 3) 提取标签后的值（直到下一个标签或行尾），再尝试引号匹配
const extractByLabel = (text, label) => {
  const labelRegex = new RegExp(`${label}\\s*[:：]\\s*`, 'i')
  const idx = text.search(labelRegex)
  if (idx < 0) return ''
  const after = text.slice(idx).replace(labelRegex, '')
  // 截到下一个可能标签（粗略枚举）
  const nextLabelRegex = /(Journal Title|Article Title|Authors|DOI|Publication Date|SCI Status)\s*[:：]/i
  const nextMatch = after.search(nextLabelRegex)
  const section = nextMatch > -1 ? after.slice(0, nextMatch) : after

  // 优先引号值，否则取整段去除换行
  const quoted = getQuotedValueInSection(section)
  if (quoted) return quoted

  return section.replace(/\n/g, ' ').trim()
}

// 4) 规范 DOI：去掉内部空格
const normalizeDOI = (val) => {
  if (!val) return ''
  return val.replace(/\s+/g, '')
}

// 5) 英文日期转 YYYY-MM-DD（若失败则返回原值）
const normalizeDate = (raw) => {
  if (!raw) return ''
  const d = new Date(raw)
  if (!isNaN(d)) {
    return d.toISOString().slice(0, 10)
  }
  return raw.trim()
}

// 6) 总解析函数
const parseOcrResult = (rawText) => {
  const text = normalizeText(rawText)

  const journal = extractByLabel(text, 'Journal Title')
  const title = extractByLabel(text, 'Article Title')
  const authors = extractByLabel(text, 'Authors')
  let doi = extractByLabel(text, 'DOI')
  const pubDate = extractByLabel(text, 'Publication Date')
  const sciStatus = extractByLabel(text, 'SCI Status')

  doi = normalizeDOI(doi)

  return {
    title,
    journal_name: journal,
    publish_date: normalizeDate(pubDate),
    is_sci: /indexed|yes|收录/i.test(sciStatus),
    file_url: doi,              // 按你的需求：用 file_url 代替 DOI
    ocr_content: rawText,       // 保留原始 OCR 文本
    authors
  }
}

// const parseOcrResult = (text) => {
//   // 初始化匹配结果
//   let parsed = {
//     title: '',
//     journal_name: '',
//     publish_date: '',
//     is_sci: false,
//     file_url: '',
//     ocr_content: text, // 保留原始识别文本
//     authors: ''
//   }

//   // 通用正则匹配
//   const patterns = {
//     title: /Article Title[:：]?\s*["“]?(.+?)["”]?(\n|$)/i,
//     journal_name: /Journal Title[:：]?\s*(.+?)(\n|$)/i,
//     publish_date: /Publication Date[:：]?\s*(.+?)(\n|$)/i,
//     doi: /DOI[:：]?\s*(10\.\d{4,9}\/[^\s]+)(\n|$)/i,
//     authors: /Authors[:：]?\s*(.+?)(\n|$)/i,
//     sci_status: /SCI Status[:：]?\s*(Indexed|收录|Yes)/i
//   }

//   // 标题
//   const titleMatch = text.match(patterns.title)
//   if (titleMatch) parsed.title = titleMatch[1].trim()

//   // 期刊名
//   const journalMatch = text.match(patterns.journal_name)
//   if (journalMatch) parsed.journal_name = journalMatch[1].trim()

//   // 发表日期
//   const dateMatch = text.match(patterns.publish_date)
//   if (dateMatch) {
//     // 尝试转成 yyyy-MM-dd 格式
//     const rawDate = dateMatch[1].trim()
//     const parsedDate = new Date(rawDate)
//     if (!isNaN(parsedDate)) {
//       parsed.publish_date = parsedDate.toISOString().split('T')[0]
//     } else {
//       parsed.publish_date = rawDate
//     }
//   }

//   // DOI → 存到 file_url
//   const doiMatch = text.match(patterns.doi)
//   if (doiMatch) parsed.file_url = doiMatch[1].trim()

//   // 作者
//   const authorMatch = text.match(patterns.authors)
//   if (authorMatch) parsed.authors = authorMatch[1].trim()

//   // SCI状态
//   const sciMatch = text.match(patterns.sci_status)
//   if (sciMatch) parsed.is_sci = true

//   return parsed
// }

const formRef = ref(null)
const form = reactive({
  id: null,
  user_id: null, // 后端可从登录用户自动填充
  title: '',
  journal_name: '',
  publish_date: '',
  is_sci: false,
  file_url: '',
  ocr_content: '',
  status: 0, // 0草稿，1待审核
  sci_partition: '',
  impact_factor: '',
  page_range: '',
  author_role: ''
})

// OCR识别
const handleOCR = async (file) => {
  console.log(Tesseract)
  try {
    const { data: { text } } = await Tesseract.recognize(file, 'eng') // 英文识别
    console.log('OCR结果:', text)

    let success = false

    // 提取字段
    const parsed = parseOcrResult(text)

    // 将解析结果填充到表单
    form.title = parsed.title
    form.journal_name = parsed.journal_name
    form.publish_date = parsed.publish_date
    form.is_sci = parsed.is_sci
    form.file_url = parsed.file_url
    form.ocr_content = parsed.ocr_content
    form.authors = parsed.authors
    // 保留原始识别文本
    form.ocr_content = text
    success = !!(parsed.title || parsed.journal_name || parsed.file_url)
    // 自动查询期刊信息
    if (form.journal_name) {
      console.log('尝试自动填充期刊信息:', form.journal_name)
      fetchJournalInfoLocal(form.journal_name)
    }
    if (!success) {
      ElMessage.warning('OCR识别失败，请手动填写信息')
    } else {
      ElMessage.success('OCR识别成功，请确认表单内容')
    }
  } catch (error) {
    ElMessage.error('OCR识别出错，请重新上传或手动填写')
  }
}

const submitForm = (isSubmit) => {
  formRef.value.validate((valid) => {
    if (valid) {
      const payload = {
        ...form,
        isSubmit: isSubmit
      }
      const actionText = isSubmit ? '提交审核' : '保存草稿'
      ElMessageBox.confirm('请确认表单信息是否准确，OCR识别结果可能存在误差。是否继续提交？',
        '提交确认', { type: 'warning' }).then(() => {
        request.post('/paper/add', payload).then(() => {
          ElMessage.success(`${actionText}成功`)
        })
      })
    }
  })
}
</script>

<style scoped>
.paper-form {
  max-width: 600px;
}
.el-input, .el-select {
  width: 100%;
}
</style>
