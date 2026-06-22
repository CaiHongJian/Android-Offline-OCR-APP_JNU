**项目名称：** 离线打印体文字识别（OCR）Android App

**目标环境：** Java (SDK 29)、离线运行、使用后置摄像头拍照并在 Activity 的 TextView 上显示识别结果；可选：将识别文本以标注形式叠加回原图上。

**基于工程：** 本分工基于当前工程的 OCR Demo，关键参考文件：
- [app/src/main/java/com/baidu/paddle/lite/demo/ocr/MainActivity.java](app/src/main/java/com/baidu/paddle/lite/demo/ocr/MainActivity.java)
- [app/src/main/java/com/baidu/paddle/lite/demo/ocr/OCRPredictorNative.java](app/src/main/java/com/baidu/paddle/lite/demo/ocr/OCRPredictorNative.java)
- [app/src/main/cpp/ocr_ppredictor.cpp](app/src/main/cpp/ocr_ppredictor.cpp)
- [app/src/main/cpp/ocr_crnn_process.cpp](app/src/main/cpp/ocr_crnn_process.cpp)
- [app/src/main/res/layout/activity_main.xml](app/src/main/res/layout/activity_main.xml)

---

**小组人数：6 人 — 建议分工（可按实际姓名替换成员编号）**

1. 成员A — 项目组长 / 最终集成

	- 责任：总体进度把控、任务分配、功能验收、合并 PR、对接测试

	- 交付：最终 APK 测试用例、发布说明、合并分支

	- 主要文件/工作点：总体检查所有模块并运行整体验收

2. 成员B — 摄像头与权限管理（前端相机模块）

	- 责任：实现并优化后置摄像头拍照流程、相机预览（如需要）、运行时权限（相机、存储）处理

	- 交付：稳定的拍照接口、可返回 Bitmap 给识别模块、示例调用方法

	- 主要文件/工作点：`MainActivity.java`、相机权限逻辑、与拍照回调整合

3. 成员C — 图像预处理与后处理（影像处理）

	- 责任：完成输入图片的预处理（裁剪、灰度化、二值化、缩放、去噪）、对 OCR 结果做后处理（文本清洗、布局恢复）

	- 交付：可复用的图像处理工具类、性能调优建议、处理前后效果截图

	- 主要文件/工作点：`preprocess.cpp/.h`（若使用 native）、或 Java 端工具类，集成到拍照回调链路

4. 成员D — OCR 模型集成与 JNI（本地推理接口）

	- 责任：维护与优化 `OCRPredictorNative` 与 C++ 推理桥接（JNI），确保离线模型加载、推理流程稳定且在 SDK29 下兼容

	- 交付：完善的 JNI 接口、错误处理、模型文件加载示例、性能基准数据

	- 主要文件/工作点：`OCRPredictorNative.java`、`ppredictor.cpp/h`、`ocr_ppredictor.cpp`、CMakeLists.txt

5. 成员E — 识别结果展示与文字叠加（UI 层）

	- 责任：将识别结果在 Activity 的 TextView 上展示；在原图上按位置绘制识别文字（文字框 + 文字），支持缩放/滚动查看

	- 交付：TextView 展示逻辑、Overlay 绘制控件（自定义 View）、与原图对齐示例

	- 主要文件/工作点：`activity_main.xml`、`MainActivity.java`、新增自定义 `OverlayView` 类

6. 成员F — 测试与文档（用例/README）

	- 责任：编写测试计划（包含常见字体/尺寸/光照的测试用例）、功能测试、辅助文档与使用说明、打包 APK 测试报告

	- 交付：`README.md` 的使用与构建步骤补充、测试用例清单、问题回归列表

	- 主要文件/工作点：工程根 README、测试图片样例放在 `app/src/main/assets/images/` 下

---

**时间线（建议，按周）**
- 第1周：成员对模块熟悉，成员B/C 完成基础拍照与预处理原型，成员D 搭通 JNI 与离线模型加载
- 第2周：成员E 实现结果展示与 Overlay，成员F 编写测试用例，成员A 做中期集成与验收
- 第3周：性能优化、bug 修复、最终集成、文档完善与演示准备

**交付要求（最小可交付）**
- 能在离线环境下（无网络）用后置摄像头拍照并完成文字识别
- 识别文本显示在 Activity 的 TextView 中
- 提交一个可安装的 APK 或可运行的分支

**沟通与提交流程建议**
- 每日站会 10-15 分钟同步进度（或每两天一次）
- 使用 Git 分支：每人一个 feature 分支 `feature/<member>-ocr-xxx`，完成后发 PR 给成员A 审核
- 将关键 issue 和任务写入仓库 Issue 页面并在 PR 中关联

---

如需我把文档根据实际姓名替换成员标签（比如把成员A替换为张三），或把每项拆成更细的子任务并创建 Issue 分配，请回复要替换的姓名列表或确认创建 Issue。
