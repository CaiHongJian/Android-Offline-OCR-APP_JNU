package com.baidu.paddle.lite.demo.ocr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.List;

public class RectDrawer {

    public static Bitmap drawRect(Bitmap originalBitmap, List<OcrResultModel> resultList) {
        if (originalBitmap == null || resultList == null || resultList.isEmpty()) {
            return originalBitmap;
        }

        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();

        // 获取图片尺寸，用于边界检测
        int imageWidth = mutableBitmap.getWidth();
        int imageHeight = mutableBitmap.getHeight();

        // 文字大小改小（从50f改为28f）
        float textSize = 28f;

        for (OcrResultModel result : resultList) {
            List<Point> points = result.getPoints();
            String label = result.getLabel();

            if (points == null || points.size() < 2) {
                continue;
            }

            // 计算矩形边界
            int left = Integer.MAX_VALUE;
            int top = Integer.MAX_VALUE;
            int right = Integer.MIN_VALUE;
            int bottom = Integer.MIN_VALUE;

            for (Point p : points) {
                if (p.x < left) left = p.x;
                if (p.y < top) top = p.y;
                if (p.x > right) right = p.x;
                if (p.y > bottom) bottom = p.y;
            }

            // ---- 画红色矩形框 ----
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0xFFFF0000);
            paint.setStrokeWidth(6f);
            canvas.drawRect(left, top, right, bottom, paint);

            // ---- 画蓝色识别文字（自动避让图片边界） ----
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF0000FF);
            paint.setTextSize(textSize);

            // 计算文字位置：优先画在框的左上角外侧
            float textX = left;
            float textY = top - 10;

            // 如果文字超出左边界，改到框内左边界+5
            if (textX < 0) {
                textX = 5;
            }

            // 如果文字超出上边界，改到框内左上角
            if (textY < 0) {
                textY = top + textSize + 5;
            }

            // ⭐ 新增：如果文字超出右边界，往左挪（让文字左对齐，显示在框内右侧）
            // 先估算文字宽度（中文字每个字约等于textSize宽度）
            float textWidth = label.length() * textSize * 0.8f;
            if (textX + textWidth > imageWidth) {
                textX = imageWidth - textWidth - 5;
                // 如果这样还是超出，就强行把文字画到框的左上角内部
                if (textX < 0) {
                    textX = 5;
                    textY = top + textSize + 5;
                }
            }

            // ⭐ 新增：如果文字超出下边界，把文字往上移
            if (textY > imageHeight) {
                textY = imageHeight - 5;
            }

            canvas.drawText(label, textX, textY, paint);
        }

        return mutableBitmap;
    }
}