package com.moac.android.opensecretsanta.adapter;

import android.graphics.*;
import com.squareup.picasso.Transformation;

public class RoundEdgeTransformation implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {

        Bitmap roundEdgeBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Canvas c = new Canvas(roundEdgeBitmap);
        c.drawRoundRect((new RectF(0.0f, 0.0f, source.getWidth(), source.getHeight())), 10, 10, paint);

        if(roundEdgeBitmap != source) {
            source.recycle();
        }
        return roundEdgeBitmap;
    }

    @Override
    public String key() { return "roundEdge()"; }
}