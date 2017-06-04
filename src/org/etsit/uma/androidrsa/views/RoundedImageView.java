package org.etsit.uma.androidrsa.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {
	//you can change the radius to modify the circular shape into oval or rounded rectangle

	public static float radius = 100.0f;

	public RoundedImageView(Context context) {
	super(context);
	}

	public RoundedImageView(Context context, AttributeSet attrs) {
	super(context, attrs);
	}

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
	Path clipPath = new Path();
	RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
	clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
	canvas.clipPath(clipPath);
	super.onDraw(canvas);
	}

}
