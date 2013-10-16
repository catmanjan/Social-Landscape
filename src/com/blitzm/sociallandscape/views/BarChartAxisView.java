package com.blitzm.sociallandscape.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Author: Siyuan Zhang 
 * Date: April 27, 2013 This class defines the x-axis and
 * y-axis of the histogram
 */
public class BarChartAxisView extends View {

	// Paint that drawing the axis
	private Paint paint;
	// the height of the bar that has the largest value
	private int topSpace = 50;
	// the y-coordinate of the origin
	private float startHeight;;
	// the length of the y-axis
	private float endHeight = topSpace / 2;
	// the margin of the bar chart
	private float margin;
	private float width;

	public BarChartAxisView(Context context) {
		super(context);
	}

	/**
	 * 
	 * @param context
	 * @param width
	 * @param height
	 */
	public BarChartAxisView(Context context, float width, float height,
			float margin) {
		super(context);

		// TODO Auto-generated constructor stub
		startHeight = height;
		this.width = width;
		this.topSpace = (int) (startHeight / 10);
		this.margin = margin;
		init();
	}

	// initialize the paint
	public void init() {
		paint = new Paint();
		paint.setStrokeWidth(3);
		paint.setARGB(255, 140, 140, 140);
	}

	public void onDraw(Canvas canvas) {
		// draw the y-axis
		canvas.drawLine(margin, startHeight+margin, margin, endHeight+margin, paint);
		// draw the x-axis
		canvas.drawLine(margin, startHeight+margin, width - margin, startHeight+margin, paint);
		this.invalidate();
	}

}
