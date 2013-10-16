package com.blitzm.sociallandscape.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

/**
 * Author: Siyuan Zhang 
 * Date: April 27, 2013 A view representing a bar in the
 * bar chart. Once it has created, it can draw itself.
 * 
 */
public class BarChartBarView extends View {

    private String dataType;
    private String DataNodeType;
	/*
	 * Paint for drawing staff
	 */
	private Paint paint; // paint for drawing a bar
	private Paint border_paint; // paint for drawing bar border
	private Paint font_paint; // paint for drawing index number


	
	/*
	 * variables that needed to draw the bar
	 */
	// margin of the index number
	private float index_margin;
	// used for set the highest height the bar can reach
	private int topSpace;
	// margin of the bar
	private float margin;
	// the bottom line of the bar
	private float startHeight;
	// the top line of the bar, if the animation switch is on, it will change
	// every iteration
	private float endHeight;
	// the width of each partition
	private float partitionWidth;
	// the speed of the animation, the larger the number, the slower the speed
	private float speed = 50;
	
	
	
	/*
	 * values that needed to draw the bar
	 */
	// the value of the bar
	private float itemValue = 0;
	// the current value of the bar (if the animation switch is on)
	private float indexValue = 0;
	// the largest value of all inputs
	private float maxValue = 0;	
	// the thread switch
	private boolean display = true;
	// the animation switch
	private boolean animMode = true;
	
	
	
	/*
	 * Color
	 */
	int blue_from = Color.argb(255, 0, 0, 100);
	int blue_to = Color.argb(255, 128, 128, 255);	
	int green_from = Color.argb(255, 0, 102, 0);
    int green_to = Color.argb(255, 128, 255, 128);

    
    
	public BarChartBarView(Context context) {
		super(context);
	}

	/**
	 * 
	 * @param context
	 * @param itemValue
	 * @param maxValue
	 * @param viewWidth
	 */
	public BarChartBarView(Context context, float itemValue, float maxValue,
			float viewWidth, float height, String dataType, String DataNodeType) {
		super(context);
		this.itemValue = itemValue;
		this.maxValue = maxValue;
		this.margin = viewWidth / 8;
		this.partitionWidth = viewWidth - margin;
		this.startHeight = height;
		this.endHeight = this.startHeight;
		this.topSpace = (int) (startHeight / 10);
		this.dataType = dataType;
		this.DataNodeType = DataNodeType;
		init();
	}

	/**
	 * 
	 * @param context
	 * @param itemValue
	 * @param maxValue
	 * @param viewWidth
	 * @param animMode
	 */
	public BarChartBarView(Context context, float itemValue, float maxValue,
			float viewWidth, float height, String dataType, boolean animMode, String DataNodeType) {
		super(context);
		this.itemValue = itemValue;
		this.maxValue = maxValue;
		this.animMode = animMode;
		this.margin = viewWidth / 8;
		this.partitionWidth = viewWidth - margin;
		this.startHeight = height;
		this.endHeight = this.startHeight;
        this.dataType = dataType;		
        this.DataNodeType = DataNodeType;
		init();
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/*
		 * draw the bar
		 */
		canvas.drawRect(margin, endHeight, margin + partitionWidth,
				startHeight, paint);
		canvas.drawLine(margin, endHeight, margin + partitionWidth, endHeight,
				border_paint);
		canvas.drawLine(margin, endHeight, margin, startHeight, border_paint);
		canvas.drawLine(margin + partitionWidth, endHeight, margin
				+ partitionWidth, startHeight, border_paint);

		/*
		 * draw the index number
		 */
		int roundedIndexValue = (int) Math.round(indexValue);

		float textWidth = paint.measureText(Float.toString(roundedIndexValue));
		index_margin = ((partitionWidth - textWidth) / 2 + margin);
		String text = roundedIndexValue + "";
		//if data type is dollars, add dollar symbol as prefix
        if (this.dataType.equals("dollars")) {
            text = "$" + text;
        }
        //if data type is percentage, add percentage symbol as suffix
        if (this.dataType.equals("percentage")) {
            text = text + "%";
        }
		canvas.drawText(text, index_margin, endHeight - 5, font_paint);

	}

	private void init() {
		/*
		 * initialize the Paints
		 */
		paint = new Paint();
		int color_from = blue_from;
		int color_to = blue_to;
		if(DataNodeType.equals("category")){
		    color_from = green_from;
	        color_to = green_to;
		}
		LinearGradient lg = new LinearGradient(0, 0, margin + partitionWidth,
				0, color_from,
				color_to, Shader.TileMode.MIRROR);
		paint.setShader(lg);

		font_paint = new Paint();
		font_paint.setARGB(255, 138, 138, 138);
		if(DataNodeType.equals("category")){
		    
		    font_paint.setTextSize((int)(partitionWidth/2.0));
		    
		}else{
		    font_paint.setTextSize((int)(partitionWidth/4.0));
		}
		border_paint = new Paint();
		border_paint.setColor(Color.BLACK);
		border_paint.setStrokeWidth(2);
		border_paint.setStyle(Paint.Style.STROKE);

		/*
		 * if animation switch is on then animate it, otherwise draw it
		 */
		if (animMode) {
			// start the thread
			thread.start();
		} else {
			// draw the bar
			display = false;
			indexValue = itemValue;
			if (itemValue == maxValue) {
				endHeight = topSpace;
			} else {
				endHeight = (1 - (itemValue / maxValue))
						* (startHeight - topSpace) + topSpace;
			}
			invalidate();
		}
	}

	/*
	 * response and draw if the thread is alive
	 */
	Handler handler = new MyHandler(this);

	// thread that animates the bar
	private Thread thread = new Thread() {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted() && display) {
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);// tell the handler that the thread is
											// alive
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					System.err.println("InterruptedException");
					this.interrupt();
				}
			}
		}
	};
	
	//handler to handler the animation
	static class MyHandler extends Handler{
	    BarChartBarView view;
	    
	    public MyHandler(BarChartBarView view) {
	        this.view = view;
        }

        public MyHandler(Looper L, BarChartBarView view) {
            super(L);
            this.view = view;
        }
        
	    @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            if (view.itemValue == view.maxValue) {// the item has max value
                float maxHeight = view.startHeight - view.topSpace;
                // animation is running
                if (msg.what == 1 && view.indexValue < view.itemValue
                        && view.endHeight >= view.topSpace) {
                    view.endHeight -= maxHeight / view.speed; // iterate 100 times
                    view.indexValue += view.itemValue / view.speed;
                } else {
                    // if animation is over,then kill the thread
                    view.display = false;
                }
                view.invalidate();
            } else {// items that have less values
                float itemHeight = (view.itemValue / view.maxValue)
                        * (view.startHeight - view.topSpace);
                float disparity = (1 - (view.itemValue / view.maxValue))
                        * (view.startHeight - view.topSpace);
                // animation is running
                if (msg.what == 1 && view.indexValue < view.itemValue
                        && view.endHeight >= view.topSpace + disparity) {
                    view.endHeight -= itemHeight / view.speed;
                    view.indexValue += view.itemValue / view.speed;
                } else {
                    // if animation is over,then kill the thread
                    view.display = false;
                }
                view.invalidate();
            }

        }
	}
}
