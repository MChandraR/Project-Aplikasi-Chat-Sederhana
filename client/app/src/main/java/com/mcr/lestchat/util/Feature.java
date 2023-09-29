package com.mcr.lestchat.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.mcr.lestchat.R;

public class Feature {
    Context contex;
    Snackbar snackbar;
    View snackbarView ;
    TextView mTextView ;
    public Feature(Context context,View parent){
        this.contex = context;
        snackbar = Snackbar.make(parent,"",Snackbar.LENGTH_LONG);
        snackbarView = snackbar.getView();
        mTextView = (TextView) snackbarView.findViewById(R.id.snackbar_text);
        mTextView.setTextSize(14f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        } else {
            mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        snackbarView.setBackgroundColor(contex.getResources().getColor(R.color.white, Resources.getSystem().newTheme()));
        snackbar.setTextColor(contex.getResources().getColor(R.color.black, Resources.getSystem().newTheme()));

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP;
        snackbarView.setLayoutParams(params);
        snackbarView.startAnimation(AnimationUtils.loadAnimation(contex, R.anim.snack_slide_in));
    }

    public  void showSnackbar(String message){
        mTextView.setText(Html.fromHtml(message));
        snackbarView.animate();
        snackbar.show();
    }
}
