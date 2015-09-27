package com.moac.android.opensecretsanta.ui.common;

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
        int shortestAxis = Math.min(source.getWidth(), source.getHeight());
        c.drawCircle(source.getWidth() / 2, source.getHeight() / 2, (float) shortestAxis / 2f, paint);

        if(roundEdgeBitmap != source) {
            source.recycle();
        }
        return roundEdgeBitmap;
    }

    @Override
    public String key() { return "roundEdge()"; }
}
