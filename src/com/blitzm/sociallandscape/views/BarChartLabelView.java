package com.blitzm.sociallandscape.views;

import java.util.ArrayList;
import com.blitzm.sociallandscape.models.Info;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * 
 * Author: Siyuan Zhang 
 * Date: April 27, 2013 This class defines the label for
 * the histogram as a view
 */
@SuppressLint("DrawAllocation")
public class BarChartLabelView extends View {
	// Paint for drawing the label
	private Paint font_paint;
	// Path that helps the label displayed aslant
	private Path path;
	// where to draw the label
	private float startHeight;
	// the width of each partition
	private float partitionWidth;
	//margin of the view
	private float viewMargin;
	//the data node type
	private String DataNodeType;
	// the input list
	private ArrayList<Info> list = new ArrayList<Info>();

	/**
	 * 
	 * @param context
	 */
	public BarChartLabelView(Context context){
	    super(context);
	}
	/**
	 * 
	 * @param context
	 * @param height
	 * @param viewWidth
	 * @param list
	 */
	public BarChartLabelView(Context context, float height, float viewWidth, float viewMargin,
			ArrayList<Info> list, String DataNodeType) {
		super(context);
		// TODO Auto-generated constructor stub
		startHeight = height;
		this.list = list;
		this.partitionWidth = viewWidth;
		this.viewMargin = viewMargin;
		this.DataNodeType = DataNodeType;
		init();
	}

	/*
	 * initialize the paint
	 */
	public void init() {
		font_paint = new Paint();
		font_paint.setStrokeWidth(3);
		font_paint.setARGB(255, 180, 180, 180);
		font_paint.setAntiAlias(true);
		if(DataNodeType.equals("category")&&list.size()>5){
		    font_paint.setTextSize((int)(partitionWidth/3.0));
		}else{
		    font_paint.setTextSize((int)(partitionWidth/5.0));
		}
	}

	public void onDraw(Canvas canvas) {

		// draw label for each bar
		for (int i = 0; i < list.size(); i++) {
			String string = list.get(i).getLabel();
			float length = font_paint.measureText(string);
			float xpos=i * partitionWidth + viewMargin+5;
			float topMargin = viewMargin/2;
			float ypos=0;
			float shift = 16;
			if(string.equals("Selected Area")){
			    xpos += partitionWidth/2;
			    ypos += startHeight+topMargin;
			    DrawText("Selected", xpos, ypos, canvas);
			    			    
			    xpos += shift;
			    ypos += shift;
			    DrawText("Area", xpos, ypos, canvas);			    
			}else{
			    String[]array = string.split(" ");
			    int arrayLength = array.length;
			    if(arrayLength>1){
			        if(length>100){
			            String lastWord = array[arrayLength-1];
			            int index = string.length()-lastWord.length();
			            xpos += partitionWidth/2;
		                ypos += startHeight+topMargin;
		                String firstLine = string.substring(0, index-1);
		                DrawText(firstLine, xpos, ypos, canvas);
		                
		                xpos += shift;
		                ypos += shift; 
		                String secondLine =string.substring(index, string.length()); 
		                DrawText(secondLine, xpos, ypos, canvas);
			        }else{
			            xpos += partitionWidth/2;
                        ypos += startHeight+topMargin;
                        DrawText(string, xpos, ypos, canvas);
			        }
			    }else{
			        xpos += partitionWidth/2;
                    ypos += startHeight+topMargin;
                    DrawText(string, xpos, ypos, canvas);
			    }
			}			
		}
		this.invalidate();
	}
	
	public void DrawText(String string,float xpos, float ypos, Canvas canvas){
	    float length = font_paint.measureText(string);
	    path = new Path();
        path.moveTo(xpos, ypos);
        float moveDistance = (float) ((float) length / Math.sqrt(2));
        path.rMoveTo(-moveDistance, moveDistance);
        path.lineTo(xpos, ypos);

        // draw the label along the path
        canvas.drawTextOnPath(string, path, 0, 0, font_paint);
	}

}
