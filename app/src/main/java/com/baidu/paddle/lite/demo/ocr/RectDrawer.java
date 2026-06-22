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

            // 画红色矩形框
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0xFFFF0000);
            paint.setStrokeWidth(6f);
            canvas.drawRect(left, top, right, bottom, paint);

            // 画蓝色文字
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF0000FF);
            paint.setTextSize(50f);
            canvas.drawText(label, left, top - 10, paint);
        }

        return mutableBitmap;
    }
}