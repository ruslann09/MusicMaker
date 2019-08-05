package com.drumpads.drumpad.musicmaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Colors implements Serializable {
    private ArrayList<Integer> colors;

    public Colors(Context context, LinearLayout[] linears) {
        colors = new ArrayList<>();

        for (LinearLayout linear : linears) {
            if (linear.getTag() != null && linear.getTag().equals(new Integer(1)))
                colors.add(R.drawable.red_btn);
            else if (linear.getTag() != null && linear.getTag().equals(new Integer(2)))
                colors.add(R.drawable.pink_btn);
            else if (linear.getTag() != null && linear.getTag().equals(new Integer(3)))
                colors.add(R.drawable.blue_btn);
            else if (linear.getTag() != null && linear.getTag().equals(new Integer(4)))
                colors.add(R.drawable.yellow_btn);
            else if (linear.getTag() != null && linear.getTag().equals(new Integer(5)))
                colors.add(R.drawable.gray_btn);
        }
    }

    public void makeBtnBackground (Context context, LinearLayout[] linears) {
        for (int i = 0; i < linears.length; i++) {
            linears[i].setBackground(context.getResources().getDrawable(colors.get(i)));

            if (colors.get(i) == R.drawable.red_btn)
                linears[i].setTag(new Integer(1));
            else if (colors.get(i) == R.drawable.pink_btn)
                linears[i].setTag(new Integer(2));
            else if (colors.get(i) == R.drawable.blue_btn)
                linears[i].setTag(new Integer(3));
            else if (colors.get(i) == R.drawable.yellow_btn)
                linears[i].setTag(new Integer(4));
            else if (colors.get(i) == R.drawable.gray_btn)
                linears[i].setTag(new Integer(5));
        }
    }
}
