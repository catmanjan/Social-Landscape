package com.blitzm.sociallandscape.views;

import java.util.ArrayList;

import com.blitzm.sociallandscape.models.Info;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.View;
/**
 * Author: Siyuan Zhang 
 * Date: May 4, 2013 A view representing a pie chart. Once
 * it has created, it can draw itself.
 * 
 */
public class PieChartView extends View {

    /*
     * Paint for drawing staff
     */
    Paint paint;
    Paint borderPaint;
    Paint textPaint;
    Paint labelPaint;

    
    
    /*
     * attributes of the pie
     */
    int centerX = 200;
    int centerY = 200;
    int radius = 200;

    // size of the text
    int textSize;
    // the data
    ArrayList<Info> list;

    //the sum of all values
    float valueSum=0;
    // the Rectangle area that contains the pie
    RectF Pie;
    
    
    
    /*
     * the Rectangle area that contains the label
     */
    RectF Label_Container;
    Rect Label_Female;
    Rect Label_Male;
    
    
    
    /*
     * Shade colors
     */
    LinearGradient red, green;
    LinearGradient[] shade_colors;
    
    
    
    /*
     * attributes of the label area
     */
    int labelWidth;
    int labelHeight;

    
    /*
     * Color
     */
    int red_from = Color.argb(255, 100, 0, 0);
    int red_to = Color.argb(255, 255, 128, 128);
    
    int aqua_from = Color.argb(255, 0, 50, 50);
    int aqua_to = Color.argb(255, 50, 230, 230);
    
    int yellow_from = Color.argb(255, 50, 50, 0);
    int yellow_to = Color.argb(255, 230, 230, 50);
    
    int green_from = Color.argb(255, 0, 102, 0);
    int green_to = Color.argb(255, 128, 255, 128);
    
    int pink_from = Color.argb(255, 50, 0, 50);
    int pink_to = Color.argb(255, 230, 50, 230);
    
    int blue_from = Color.argb(255, 0, 0, 102);
    int blue_to = Color.argb(255, 128, 128, 255);
    
    int grey_from = Color.argb(255, 128, 128, 128);
    int grey_to = Color.argb(255, 25, 25, 25);
    
    
    
    /**
     * 
     * @param context
     */
    public PieChartView(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param colors
     * @param shade_colors
     * @param percent
     */
    public PieChartView(Context context, int centerX, int centerY, int radius,
            ArrayList<Info> list, int fontScale) {
        super(context);
        this.list = list;
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.textSize = (int)(radius/12.0);
        init();
    }

    /**
     * initialize the staff
     */
    private void init() {
        /*
         * initialize paints
         */
        paint = new Paint();
        borderPaint = new Paint();
        borderPaint.setStyle(Style.STROKE);
        borderPaint.setStrokeWidth(1);
        borderPaint.setColor(Color.BLACK);
        textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(Color.WHITE);
        labelPaint = new Paint();
        labelPaint.setColor(Color.BLACK);
        labelPaint.setStyle(Style.FILL);

        /*
         * initialize the shade colors
         */
        this.shade_colors = new LinearGradient[] {
                new LinearGradient(centerX - radius, centerY, centerX + radius,
                        centerY, red_from, red_to, Shader.TileMode.MIRROR),
                new LinearGradient(centerX - radius, centerY, centerX + radius,
                        centerY, aqua_from, aqua_to, Shader.TileMode.MIRROR),
                new LinearGradient(centerX - radius, centerY, centerX + radius,
                        centerY, yellow_from, yellow_to, Shader.TileMode.MIRROR),
                new LinearGradient(centerX - radius, centerY, centerX + radius,
                        centerY, green_from, green_to, Shader.TileMode.MIRROR),                                               
                new LinearGradient(centerX - radius, centerY, centerX + radius,
                        centerY, blue_from, blue_to, Shader.TileMode.MIRROR),
                new LinearGradient(centerX - radius, centerY, centerX + radius,
                        centerY, grey_from, grey_to, Shader.TileMode.MIRROR),
        };

        /*
         * initialize the containers
         */
        Pie = new RectF(centerX - radius, centerY - radius, centerX + radius,
                centerY + radius);

        // get the width of the label area
        int maxWidth = 0;
        for (int j = 0; j < list.size(); j++) {
            String currentText = list.get(j).getLabel();
            float currentLength = textPaint.measureText(currentText);
            if(currentLength>maxWidth) maxWidth = (int)currentLength;
        }
        
        this.labelWidth = 2*maxWidth+80;
        this.labelHeight = 40;
        
    }

    /**
     * draw the pie chart
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.drawChart(canvas);                   
    }
    
    private void drawChart(Canvas canvas){
        /*
         * draw the pie
         */

        // start from 45 degree
        int tempAngle = -45;
      
        // drawing for each sector

        valueSum = 0;
        for (int j = 0; j < list.size(); j++) {
            valueSum+=Float.parseFloat(list.get(j).getValue());;
        }
        for (int j = 0; j < list.size(); j++) {
            
            paint.setShader(shade_colors[j]);
            Float value = Float.parseFloat(list.get(j).getValue())/valueSum;
            float angle;
            
            if(value<100&&value>0){                        
                angle = 360 * value +1;
                //draw sector
                canvas.drawArc(Pie, tempAngle, angle, true, paint);
                //draw sector border
                canvas.drawArc(Pie, tempAngle, angle, true, borderPaint);  
            
                //draw the sector label
                textPaint.setTextSize(textSize);
                float label_angle = angle/2 + tempAngle;
                double lable_radians = label_angle/180*Math.PI;
                float label_xcoord = centerX + (float)(radius*Math.cos(lable_radians))/2;
                float label_ycoord = centerY + (float)(radius*Math.sin(lable_radians))/2;

                if(value==100){
                    label_xcoord = centerX;
                    label_ycoord = centerY;
                }
                canvas.drawText(Math.round(value*100) + "%", label_xcoord, label_ycoord, textPaint);
                
                tempAngle += angle;            
            }
            
            /*
             * draw the labels
             */
            
            textPaint.setTextSize(textSize);
            
            if(this.labelWidth>= 2*radius + 20){
                labelWidth = 2 * radius + 20;
                textPaint.setTextSize((float) (radius/16.0));
            }
            
            int currentRow = j/2;          
            int totalRows = 1;
            int size = list.size();
            if(size%2 == 0){
                totalRows = size/2;
            }else{
                totalRows = (size+1)/2;
            }
            
            int shift = 20*(totalRows-1);
            
            if(j==0){
                //only draw the container once, otherwise it will overlap
                Label_Container = new RectF(centerX - labelWidth / 2,
                                           centerY + radius + 50 - shift, 
                                           centerX + labelWidth / 2, 
                                           centerY + radius + 50 - shift + (int)((1+(totalRows-1)*0.75)*labelHeight)
                                           );
                canvas.drawRoundRect(Label_Container, labelHeight/6, labelHeight/6, labelPaint);
            }
            
            paint.setShader(shade_colors[j]);
            
            Rect label_square = null;
            int nextRowShift = (int)(currentRow*0.75*labelHeight);
            int label_top = centerY + radius + 50 + labelHeight / 6;
            int label_bottom = centerY + radius + 50 + labelHeight - labelHeight/6;
            String label_text = list.get(j).getLabel();
            

            
            if(j%2==0){
                //draw the colored square
                label_square = new Rect(centerX - labelWidth / 2 + labelHeight / 6,
                                       label_top + nextRowShift - shift,
                                       centerX - labelWidth / 2 + labelHeight - labelHeight / 6, 
                                       label_bottom + nextRowShift - shift);
                //draw the label text
                canvas.drawText(label_text, centerX - labelWidth / 2 + labelHeight,
                        centerY + radius + 50 + labelHeight - labelHeight / 3 + nextRowShift - shift,
                        textPaint);
            }
            if(j%2==1){
                //draw the colored square
                label_square = new Rect(centerX + labelHeight / 6, 
                                       label_top + nextRowShift - shift, 
                                       centerX + labelHeight - labelHeight / 6,
                                       label_bottom + nextRowShift - shift);
                //draw the label text
                canvas.drawText(label_text, centerX + labelHeight, 
                        centerY + radius + 50 + labelHeight - labelHeight / 3 + nextRowShift - shift,
                        textPaint);
            }
            canvas.drawRect(label_square, paint);

        }  
    }

}