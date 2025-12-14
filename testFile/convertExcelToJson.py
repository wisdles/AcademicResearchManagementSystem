import pandas as pd

# 读取 Excel 文件
df = pd.read_excel("中科院文献情报中心期刊分区表.xlsx", sheet_name=0, engine="openpyxl")

# 规范化列名（去掉换行和空格）
df.columns = [str(c).strip().replace("\n", "") for c in df.columns]

# 选择需要的列
sel = df[["刊名全称", "ISSN", "上一年影响因子", "最高分区"]].copy()

# 去掉期刊名为空的行
sel = sel[sel["刊名全称"].notna() & (sel["刊名全称"].astype(str).str.strip() != "")]

# 重命名列为英文键
sel = sel.rename(columns={
    "刊名全称": "name",
    "ISSN": "issn",
    "上一年影响因子": "impactFactor",
    "最高分区": "partition"
})

# 转换影响因子为数值（保留原始也可以）
sel["impactFactor"] = pd.to_numeric(sel["impactFactor"], errors="coerce")

# 导出 JSON
sel.to_json("journal-data.json", orient="records", force_ascii=False)

print(f"Exported {len(sel)} records to journal-data.json")
