# ocr_server.py
from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from paddleocr import PaddleOCR
import shutil
import os
import uuid  # 引入 uuid 库生成随机文件名

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

print("正在加载 OCR 模型，请稍候...")
# 初始化模型
ocr = PaddleOCR(use_angle_cls=True, lang="ch")
print("OCR 模型加载完成！")

@app.post("/ocr/predict")
async def predict(file: UploadFile = File(...)):
    # === 关键修改点：生成随机纯英文文件名 ===
    # 获取文件后缀 (如 .png, .jpg)
    file_ext = os.path.splitext(file.filename)[1]
    if not file_ext:
        file_ext = ".png" # 默认后缀
    
    # 生成类似 "temp_5f4dcc3b5aa765d61d8327deb882cf99.png" 的文件名
    # 这样完全避开了中文路径问题
    temp_filename = f"temp_{uuid.uuid4().hex}{file_ext}"
    
    try:
        # 1. 保存文件
        with open(temp_filename, "wb") as buffer:
            shutil.copyfileobj(file.file, buffer)
        
        # 2. 识别 (传入纯英文路径)
        result = ocr.ocr(temp_filename)
        
        # 3. 提取文字
        txts = []
        if result and result[0]:
            txts = [line[1][0] for line in result[0]]
        
        full_text = "\n".join(txts)
        return {"code": 200, "data": full_text, "msg": "success"}
        
    except Exception as e:
        print(f"识别出错: {e}")
        return {"code": 500, "msg": str(e)}
    finally:
        # 4. 删除临时文件
        if os.path.exists(temp_filename):
            try:
                os.remove(temp_filename)
            except:
                pass

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)