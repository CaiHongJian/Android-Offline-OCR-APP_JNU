**详细分工与接口说明（细化版）**

说明：本文件在 `TEAM-DIVISION.md` 基础上补充每位成员需要对接的具体输入/输出格式、接口建议、实现思路及示例代码片段，便于并行开发与集成。

1) 成员A — 项目组长 / 最终集成
- 输入：各模块 PR、测试报告、合并请求清单
- 输出：release 分支、最终 APK、合并与发布说明
- 对接要求：统一 API 约定、确认成员接口（见下文）是否满足：异常抛出方式、线程说明、坐标系约定

2) 成员B — 摄像头与权限管理（前端相机模块）
- 输入：用户触发拍照/预览；运行时权限结果
- 输出：标准化 `Bitmap`（已做方向纠正与缩放），或 `byte[]`（模型输入格式）
- 接口建议：
  - `interface CameraCallback { void onCaptured(Bitmap bitmap); }
  - 在 `MainActivity` 中提供 `void startCapture(CameraCallback cb)` 方法
- 注意点：按设备方向纠正图片；若设备返回 YUV，应在本模块转换到 ARGB

示例伪码（CameraX）:

```java
ImageAnalysis.Analyzer analyzer = (imageProxy) -> {
    Bitmap bmp = ImageUtils.imageProxyToBitmap(imageProxy);
    bmp = ImageUtils.rotateIfNeeded(bmp);
    Bitmap scaled = Bitmap.createScaledBitmap(bmp, 1280, 720, true);
    cameraCallback.onCaptured(scaled);
    imageProxy.close();
};
```

3) 成员C — 图像预处理与后处理（影像处理）
- 输入：成员B 提供的 `Bitmap` 或原始 byte[]
- 输出：模型的输入格式（float[]/ByteBuffer/Bitmap）与后处理文本块 `List<TextBlock>`
- 接口建议：
  - `PreprocessResult preprocess(Bitmap bmp)` 返回 `{Bitmap inputBitmap, float scaleX, float scaleY}`
  - `List<TextBlock> postprocess(ModelOutput raw)`
- 实现建议：
  - Java 层实现方便调试；性能瓶颈考虑用 JNI+C++ 实现 `preprocess.cpp`
  - 预处理步骤：方向校正 -> 灰度/滤波 -> 自适应二值化（可选） -> 仿射变换（文档矫正） -> 缩放

示例代码片段（Java）：

```java
public static float[] bitmapToRGBFloat(Bitmap bmp){
    int w=bmp.getWidth(), h=bmp.getHeight();
    int[] px=new int[w*h]; bmp.getPixels(px,0,w,0,0,w,h);
    float[] out=new float[w*h*3];
    for(int i=0;i<px.length;i++){
        int c=px[i];
        out[i*3]=((c>>16)&0xFF)/255f;
        out[i*3+1]=((c>>8)&0xFF)/255f;
        out[i*3+2]=(c&0xFF)/255f;
    }
    return out;
}
```

4) 成员D — OCR 模型集成与 JNI（本地推理接口）
- 输入：预处理后的数据（Bitmap 或 float[]），模型文件路径
- 输出：模型原始输出与封装后的 `List<TextBlock>`（包含坐标、文本、置信度）
- 接口建议（Java）：
  - `void init(Context ctx, String modelDir)`
  - `List<TextBlock> predict(Bitmap bmp)` （线程不阻塞 UI）
  - `void release()`
- 实现要点：
  - JNI 层应处理内存释放、错误码返还；确保在子线程调用
  - 明确模型输出坐标的基准（基于输入图像像素坐标）并记录 scale 信息以便 UI 层映射回原图

示例调用（Java）:

```java
new Thread(() -> {
    List<TextBlock> res = OCRPredictorNative.getInstance().predict(inputBmp);
    runOnUiThread(() -> displayResults(res));
}).start();
```

5) 成员E — 识别结果展示与文字叠加（UI 层）
- 输入：`List<TextBlock>` 与原始或缩放后的 `Bitmap`
- 输出：TextView 的纯文本显示（段落），以及图片上的文字框/文字叠加
- 接口协议：
  - `void showText(List<TextBlock> list)` 更新 `TextView`
  - `void showOverlay(Bitmap bmp, List<TextBlock> list)` 更新 `OverlayView`
- 实现建议：
  - `OverlayView` 负责坐标缩放与绘制；提供 `setResults(List<TextBlock>, float scale)`
  - 提供“显示全部文本”和“逐框高亮”两种查看模式

Overlay 绘制示例（简化）：

```java
public class OverlayView extends View {
    List<TextBlock> results;
    float scale=1f;
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        for(TextBlock b: results){
            Rect r = new Rect((int)(b.box.left*scale), (int)(b.box.top*scale),
                              (int)(b.box.right*scale), (int)(b.box.bottom*scale));
            c.drawRect(r, boxPaint);
            c.drawText(b.text, r.left, r.top-4, textPaint);
        }
    }
}
```

6) 成员F — 测试与文档（用例/README）
- 测试输入：一组样例图片（放 `app/src/main/assets/images/testset/`），包括多种字体/大小/光照/倾斜
- 输出：测试报告（识别率、误识别示例）、崩溃与性能日志、APK 测试包
- 验收标准（建议）：在 640x640 输入下，常见打印体（不模糊）识别准确率 >= 90%（可根据模型能力调整）；平均单图推理<500ms（视设备而定）

---

附：建议新增文件/类（供快速原型使用）
- `app/src/main/java/com/baidu/paddle/lite/demo/ocr/TextBlock.java`（POJO）
- `app/src/main/java/com/baidu/paddle/lite/demo/ocr/OverlayView.java`（UI 模板）
- `app/src/main/java/com/baidu/paddle/lite/demo/ocr/OcrController.java`（统一入口：封装 Camera、Preprocess、Predict、UI）

下一步我可以：
- 根据你提供的 6 名成员姓名，把 `成员A-F` 替换为真实姓名并更新 `TEAM-DIVISION.md`；或
- 直接在仓库中添加 `TextBlock.java` 与 `OverlayView.java` 的最小模板以供各成员参考（会新建分支并提交）。
